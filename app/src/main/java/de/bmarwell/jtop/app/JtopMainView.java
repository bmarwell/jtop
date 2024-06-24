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
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.Terminal;
import de.bmarwell.jtop.lib.api.ProcessInfo;
import de.bmarwell.jtop.lib.api.spi.ProcessH;
import de.bmarwell.jtop.lib.api.spi.ProcessInfoServiceLoader;
import java.io.IOException;
import java.util.ServiceLoader;
import java.util.function.Function;

public class JtopMainView {

    record Column(String name, int length, Function<ProcessInfo, Object> extractor, boolean rightAlighed) {
        Column(String name, int length, Function<ProcessInfo, Object> extractor) {
            this(name, length, extractor, true);
        }

        static Column empty() {
            return new Column(" ", 1, (it) -> " ");
        }
    }

    private static final Column[] DEFAULT_COLUMNS = new Column[] {
        new Column("PID", 8, ProcessInfo::pid),
        Column.empty(),
        new Column("User", 10, ProcessInfo::user, false),
        Column.empty(),
        new Column("Pri", 3, (it) -> ""),
        Column.empty(),
        new Column("Ni", 3, (it) -> ""),
        Column.empty(),
        new Column("Virt", 6, (it) -> ""),
        Column.empty(),
        new Column("Res", 6, (it) -> ""),
        Column.empty(),
        new Column("S", 2, (it) -> "", false),
        Column.empty(),
        new Column("CPU%", 6, (it) -> ""),
        Column.empty(),
        new Column("Mem%", 6, (it) -> ""),
        Column.empty(),
        new Column("Time+", 10, (it) -> ""),
        Column.empty(),
        new Column("Command", -1, ProcessInfo::commandLine, false),
    };

    private final ProcessH processH;

    public JtopMainView() {
        final var processInfoServiceLoader = ServiceLoader.load(ProcessInfoServiceLoader.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(pi -> pi.isOs(System.getProperty("os.name")))
                .findFirst()
                .orElseThrow(
                        () -> new UnsupportedOperationException("OS not supported: " + System.getProperty("os.name")));

        this.processH = processInfoServiceLoader.newProcessH();
    }

    private static int getHeaderRows() {
        return 0;
    }

    public void printProcessListHeader(Terminal terminal) throws IOException {
        final TextGraphics textGraphics = newTextGraphics(terminal);

        int headerRows = getHeaderRows();
        int getProcessesHeaderRow = headerRows + 1;

        textGraphics.putString(
                0,
                getProcessesHeaderRow,
                String.format("%s", " ".repeat(terminal.getTerminalSize().getColumns() - 1)));

        int col = 0;
        for (Column column : DEFAULT_COLUMNS) {
            final int length;
            if (column.length <= 0) {
                length = terminal.getTerminalSize().getColumns() - col;
            } else {
                length = column.length;
            }

            String format = "%" + (column.rightAlighed ? "" : "-") + length + "s";
            textGraphics.putString(col, getProcessesHeaderRow, String.format(format, column.name), SGR.REVERSE);
            col += column.length;
        }
    }

    public void printProcessList(Terminal terminal) throws IOException {
        final TextGraphics textGraphics = newTextGraphics(terminal);

        int headerRows = getHeaderRows();
        int getProcessesStartRow = headerRows + 2;

        final var processInfos = processH.listAllProcesses();
        int currentProcRow = getProcessesStartRow;

        for (ProcessInfo processInfo : processInfos) {
            if (currentProcRow >= terminal.getTerminalSize().getRows()) {
                break;
            }

            int col = 0;

            for (Column column : DEFAULT_COLUMNS) {
                final int length;
                if (column.length <= 0) {
                    length = terminal.getTerminalSize().getColumns() - col;
                } else {
                    length = column.length;
                }

                final String format = "%" + (column.rightAlighed ? "" : "-") + length + "s";
                textGraphics.putString(col, currentProcRow, String.format(format, column.extractor.apply(processInfo)));

                col += column.length;
            }

            currentProcRow++;
        }
    }

    public void printFooter(Terminal terminal) throws IOException {
        final TextGraphics textGraphics = newTextGraphics(terminal);

        int lastRow = terminal.getTerminalSize().getRows() - 1;

        textGraphics.putString(0, lastRow, "F1");
        textGraphics.putString(2, lastRow, "Help  ", SGR.REVERSE);
        textGraphics.putString(8, lastRow, "F2");
        textGraphics.putString(10, lastRow, "Setup ", SGR.REVERSE);
    }

    private static TextGraphics newTextGraphics(Terminal terminal) throws IOException {
        final TextGraphics textGraphics = terminal.newTextGraphics();
        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        textGraphics.setForegroundColor(TextColor.ANSI.GREEN_BRIGHT);
        return textGraphics;
    }
}
