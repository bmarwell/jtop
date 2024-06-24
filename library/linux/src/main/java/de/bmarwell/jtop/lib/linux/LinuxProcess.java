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
package de.bmarwell.jtop.lib.linux;

import static java.util.Collections.emptyList;

import de.bmarwell.jtop.lib.api.ProcessInfo;
import de.bmarwell.jtop.lib.api.spi.AbstractProcessH;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class LinuxProcess extends AbstractProcessH {

    private static final Pattern NULL_BYTE = Pattern.compile("\u0000");

    @Override
    public ProcessInfo mapProcessOs(ProcessInfo processInfo) {
        if (!processInfo.command().isBlank() && !processInfo.args().isEmpty()) {
            return processInfo;
        }

        final var pidCmdline = Path.of("/proc", String.valueOf(processInfo.pid()), "cmdline");

        if (!Files.exists(pidCmdline)) {
            return processInfo;
        }

        try {
            final var cmdline = Files.readString(pidCmdline, StandardCharsets.UTF_8);

            if (cmdline.isBlank()) {
                return processInfo;
            }

            final var cmdArgs = NULL_BYTE.split(cmdline, 2);
            List<String> args;
            if (cmdArgs.length == 2) {
                args = Arrays.asList(cmdArgs[1].split("\u0000"));
            } else {
                args = emptyList();
            }

            return new ProcessInfo(processInfo.pid(), processInfo.ppid(), processInfo.user(), cmdArgs[0], args);
        } catch (final IOException ioException) {
            return processInfo;
        }
    }
}
