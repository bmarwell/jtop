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

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class JtopCmd {

    public static void main(String[] args) throws IOException {

        // parse args

        JtopMainView jtopMainView = new JtopMainView();

        try (Terminal terminal = new DefaultTerminalFactory().createTerminal()) {
            terminal.enterPrivateMode();

            while (true) {
                terminal.clearScreen();
                terminal.setCursorVisible(false);

                jtopMainView.printProcessListHeader(terminal);
                jtopMainView.printProcessList(terminal);
                jtopMainView.printFooter(terminal);

                try {
                    terminal.flush();

                    TimeUnit.MILLISECONDS.sleep(2000L);
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
