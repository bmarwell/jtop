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

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import de.bmarwell.jtop.lib.api.ProcessInfo;
import de.bmarwell.jtop.lib.api.spi.ProcessInfoServiceLoader;
import java.io.IOException;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.concurrent.TimeUnit;

public class JtopMain {

    public static void main(String[] args) throws IOException {

        final var processInfoServiceLoader = ServiceLoader.load(ProcessInfoServiceLoader.class).stream()
                .map(Provider::get)
                .filter(pi -> pi.isOs(System.getProperty("os.name")))
                .findFirst()
                .orElseThrow(
                        () -> new UnsupportedOperationException("OS not supported: " + System.getProperty("os.name")));

        final var processH = processInfoServiceLoader.newProcessH();

        try (Terminal terminal = new DefaultTerminalFactory().createTerminal()) {
            terminal.enterPrivateMode();
            terminal.clearScreen();
            terminal.setCursorVisible(false);

            int headerRows = getHeaderRows();
            int getProcessesHeaderRow = headerRows + 1;
            int getProcessesStartRow = getProcessesHeaderRow + 1;
            int rowsForProcesses = terminal.getTerminalSize().getRows() - getProcessesStartRow;

            final TextGraphics textGraphics = terminal.newTextGraphics();
            textGraphics.setBackgroundColor(ANSI.GREEN);
            textGraphics.setForegroundColor(ANSI.BLACK);
            textGraphics.putString(5, getProcessesHeaderRow, "PID", SGR.REVERSE);
            textGraphics.putString(10, getProcessesHeaderRow, "USER", SGR.REVERSE);

            final var processInfos = processH.listAllProcesses();
            int currentProcRow = getProcessesStartRow;
            for (ProcessInfo processInfo : processInfos) {
                if (currentProcRow >= terminal.getTerminalSize().getRows()) {
                    break;
                }

                textGraphics.putString(0, currentProcRow, processInfo.toString());

                currentProcRow++;
            }

            try {
                terminal.flush();

                TimeUnit.MILLISECONDS.sleep(4000L);
            } catch (final IOException ioException2) {
                ioException2.printStackTrace();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

        } catch (final IOException ioException) {
            throw ioException;
        }
    }

    private static int getHeaderRows() {
        return 0;
    }
}
