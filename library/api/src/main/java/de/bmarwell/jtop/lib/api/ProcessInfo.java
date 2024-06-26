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
package de.bmarwell.jtop.lib.api;

import java.util.List;

public record ProcessInfo(long pid, long ppid, String user, String command, List<String> args) {
    public String commandLine() {
        return command + " " + String.join(" ", args);
    }

    public ProcessInfo withCommand(final String command) {
        return new ProcessInfo(this.pid(), this.ppid(), this.user(), command, this.args());
    }

    public ProcessInfo withArgs(final List<String> args) {
        return new ProcessInfo(this.pid(), this.ppid(), this.user(), this.command(), args);
    }
}
