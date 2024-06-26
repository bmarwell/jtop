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
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.Terminal;
import de.bmarwell.jtop.lib.api.ProcessInfo;
import de.bmarwell.jtop.lib.api.spi.ProcessH;
import de.bmarwell.jtop.lib.api.spi.ProcessInfoServiceLoader;
import java.io.IOException;
import java.util.ServiceLoader;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

public class JtopMainView {

    record Column(String name, int length, Function<ProcessInfo, String> extractor, boolean rightAligned) {

        Column(String name, int length, Function<ProcessInfo, String> extractor) {
            this(name, length, extractor, true);
        }

        static Column empty() {
            return new Column(" ", 1, (it) -> " ");
        }
    }

    private static final Column[] DEFAULT_COLUMNS = new Column[] {
        new Column("PID", 8, processInfo -> String.valueOf(processInfo.pid())),
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

    record FooterEntry(String key, String text) {}

    private static final FooterEntry[] FOOTER_ENTRIES = new FooterEntry[] {
        new FooterEntry("F1", "Help ❔"),
        new FooterEntry("F2", "Setup"),
        new FooterEntry("F3", "Search"),
        new FooterEntry("F4", "Filter"),
        new FooterEntry("F5", "Tree 🌲"),
        new FooterEntry("F6", "SortBy ↕️"),
        new FooterEntry("F7", "Nice +"),
        new FooterEntry("F8", "Nice -"),
        new FooterEntry("F9", "Kill ☠️"),
        new FooterEntry("F10", "Quit 🚪"),
    };

    private final String currentUser;

    private final ProcessH processH;

    public JtopMainView() {
        final var processInfoServiceLoader = ServiceLoader.load(ProcessInfoServiceLoader.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(pi -> pi.isOs(System.getProperty("os.name")))
                .filter(pi -> pi.isArch(System.getProperty("os.arch")))
                .findFirst()
                .orElseThrow(
                        () -> new UnsupportedOperationException("OS not supported: " + System.getProperty("os.name")));

        this.processH = processInfoServiceLoader.newProcessH();
        this.currentUser = System.getenv("USER");
    }

    private static int getHeaderRows() {
        return 0;
    }

    public void initScreenRefresh(Terminal terminal) throws IOException {
        final var textGraphics = newTextGraphics(terminal);
    }

    public void printProcessListHeader(Terminal terminal, int rows, int columns) throws IOException {
        final TextGraphics textGraphics = newTextGraphics(terminal);

        int headerRows = getHeaderRows();
        int getProcessesHeaderRow = headerRows + 1;

        textGraphics.putString(0, getProcessesHeaderRow, String.format("%s", " ".repeat(columns - 1)));

        int col = 0;
        for (Column column : DEFAULT_COLUMNS) {
            final int length;
            if (column.length <= 0) {
                length = columns - col;
            } else {
                length = column.length;
            }

            String format = "%" + (column.rightAligned ? "" : "-") + length + "s";
            textGraphics.putString(col, getProcessesHeaderRow, String.format(format, column.name), SGR.REVERSE);
            col += column.length;
        }
    }

    public void printProcessList(Terminal terminal, int rows, int cols, final @Nullable String user)
            throws IOException {
        final TextGraphics textGraphics = newTextGraphics(terminal);

        int headerRows = getHeaderRows();
        int getProcessesStartRow = headerRows + 2;

        final var processInfos = processH.listAllProcesses(user);
        int currentProcRow = getProcessesStartRow;

        for (ProcessInfo processInfo : processInfos) {
            if (currentProcRow >= rows - 1) {
                break;
            }

            if (processInfo.commandLine().isBlank()) {
                continue;
            }

            int col = 0;

            for (Column column : DEFAULT_COLUMNS) {
                final int length;
                if (column.length <= 0) {
                    length = cols - col;
                } else {
                    length = column.length;
                }

                final String format = "%" + (column.rightAligned ? "" : "-") + length + "s";
                final String colText = column.extractor.apply(processInfo);
                int maxLength = Math.min(length, colText.length());
                final var cmdstring = String.format(format, colText.substring(0, maxLength));
                if (!this.currentUser.equals(processInfo.user())) {
                    textGraphics.setForegroundColor(ANSI.BLACK_BRIGHT);
                } else {
                    textGraphics.setForegroundColor(ANSI.GREEN_BRIGHT);
                }
                textGraphics.putString(col, currentProcRow, cmdstring);

                col += column.length;
            }

            currentProcRow++;
        }
    }

    public void printFooter(Terminal terminal, int rows, int columns) throws IOException {
        final TextGraphics textGraphics = newTextGraphics(terminal);

        int lastRow = rows - 1;
        int currentCol = 0;

        textGraphics.putString(0, lastRow, String.format("%s", " ".repeat(columns - 1)));

        for (FooterEntry footerEntry : FOOTER_ENTRIES) {
            textGraphics.setForegroundColor(ANSI.GREEN_BRIGHT);
            textGraphics.setBackgroundColor(ANSI.BLACK);
            textGraphics.putString(currentCol, lastRow, footerEntry.key);
            currentCol += footerEntry.key.length();

            textGraphics.setForegroundColor(ANSI.BLACK);
            textGraphics.setBackgroundColor(ANSI.BLUE_BRIGHT);
            textGraphics.putString(currentCol, lastRow, footerEntry.text + " ");
            currentCol += footerEntry.text.length() + 1;
        }
    }

    private static TextGraphics newTextGraphics(Terminal terminal) throws IOException {
        final TextGraphics textGraphics = terminal.newTextGraphics();
        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        textGraphics.setForegroundColor(TextColor.ANSI.GREEN_BRIGHT);
        return textGraphics;
    }
}
