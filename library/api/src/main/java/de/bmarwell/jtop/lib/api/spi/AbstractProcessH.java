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
package de.bmarwell.jtop.lib.api.spi;

import de.bmarwell.jtop.lib.api.ProcessInfo;
import de.bmarwell.jtop.lib.api.ProcessInfoMapper;
import java.util.List;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

public abstract class AbstractProcessH implements ProcessH {

    private final ProcessInfoMapper processInfoMapper = new ProcessInfoMapper();

    @Override
    public List<ProcessInfo> listAllProcesses(final @Nullable String user) {
        Predicate<ProcessInfo> userFilter = processInfo -> {
            if (user == null) {
                // not filtered
                return true;
            }

            return processInfo.user().equals(user);
        };

        final var processHandleStream = ProcessHandle.allProcesses();
        return processHandleStream
                .map(processInfoMapper::getProcessInfo)
                .map(this::mapProcessOs)
                .filter(userFilter)
                .limit(256)
                .toList();
    }

    /**
     * Method which can be overridden per system to enrich / fix process information.
     * @param processInfo the processinfo to "fix"
     * @return the fixed processInfo
     */
    public ProcessInfo mapProcessOs(ProcessInfo processInfo) {
        return processInfo;
    }
}
