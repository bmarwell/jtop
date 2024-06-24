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
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.concurrent.Callable;
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

    JtopMainView jtopMainView = new JtopMainView();

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

            while (true) {
                terminal.clearScreen();
                terminal.setCursorVisible(false);
                TerminalSize terminalSize = terminal.getTerminalSize();
                int rows = terminalSize.getRows();
                int columns = terminalSize.getColumns();

                jtopMainView.printProcessListHeader(terminal, rows, columns);
                jtopMainView.printProcessList(terminal, rows, columns, this.user);
                jtopMainView.printFooter(terminal, rows, columns);

                try {
                    terminal.flush();

                    TimeUnit.MILLISECONDS.sleep(2000L);
                    return 0;
                } catch (final IOException ioException2) {
                    ioException2.printStackTrace();
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        } catch (final IOException ioException) {
            throw ioException;
        }
    }
}
