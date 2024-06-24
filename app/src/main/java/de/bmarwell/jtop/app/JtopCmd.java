/*
 * Copyright (C) 2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bmarwell.jtop.app;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;

@CommandLine.Command(
        name = "jtop",
        mixinStandardHelpOptions = true,
        description = "An interactive process viewer written in Java.")
public class JtopCmd implements Callable<Integer> {

    @CommandLine.Option(
            names = {"-u", "--user"},
            description = "only show processes owned by user")
    private @Nullable String user;

    final JtopMainView jtopMainView = new JtopMainView();

    final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(4, Thread.ofVirtual().factory());

    private ScheduledFuture<?> refresher;

    Throwable error = null;

    public static void main(String[] args) throws IOException {
        // parse args
        CommandLine commandLine = new CommandLine(new JtopCmd());
        CommandLine.ParseResult parseResult = commandLine.parseArgs(args);

        if (parseResult.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            System.exit(0);
        }

        int execute = commandLine.execute(args);
        System.exit(execute);
    }

    @Override
    public Integer call() throws Exception {
        try (Terminal terminal = new DefaultTerminalFactory().createTerminal()) {
            terminal.enterPrivateMode();
            terminal.setCursorVisible(false);

            this.refresher = this.scheduledExecutorService.scheduleWithFixedDelay(
                    () -> refreshScreen(terminal), 50L, 2_000L, TimeUnit.MILLISECONDS);
            Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));

            while (true) {
                final var keyStroke = terminal.readInput();

                if (!keyStroke.isCtrlDown() && keyStroke.getCharacter().equals('q')) {
                    return 0;
                }

                if (keyStroke.isCtrlDown() && keyStroke.getCharacter().equals('c')) {
                    return 0;
                }

                if (keyStroke.getKeyType() == KeyType.F10) {
                    return 0;
                }
            }
        }
    }

    private void cleanup() {
        this.scheduledExecutorService.shutdown();

        if (this.refresher != null) {
            this.refresher.cancel(true);
        }

        this.scheduledExecutorService.shutdownNow();
    }

    private void refreshScreen(Terminal terminal) {
        try {
            terminal.clearScreen();
            terminal.resetColorAndSGR();
            terminal.setCursorPosition(0, 0);
            TerminalSize terminalSize = terminal.getTerminalSize();
            int rows = terminalSize.getRows();
            int columns = terminalSize.getColumns();

            jtopMainView.initScreenRefresh(terminal);
            jtopMainView.printProcessListHeader(terminal, rows, columns);
            jtopMainView.printProcessList(terminal, rows, columns, this.user);
            jtopMainView.printFooter(terminal, rows, columns);

            terminal.flush();
            terminal.resetColorAndSGR();
        } catch (IOException ioException) {
            this.error = ioException;
        }
    }
}
