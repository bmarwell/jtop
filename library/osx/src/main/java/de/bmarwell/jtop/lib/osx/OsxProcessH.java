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
package de.bmarwell.jtop.lib.osx;

import static de.bmarwell.jtop.lib.osx.ffm.sysctl_h.size_t;
import static java.lang.foreign.MemorySegment.NULL;

import de.bmarwell.jtop.lib.api.ProcessInfo;
import de.bmarwell.jtop.lib.api.spi.AbstractProcessH;
import de.bmarwell.jtop.lib.osx.ffm.errno_h;
import de.bmarwell.jtop.lib.osx.ffm.sysctl_h;
import de.bmarwell.jtop.lib.osx.ffm.sysctl_h_1;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class OsxProcessH extends AbstractProcessH {

    @Override
    public ProcessInfo mapProcessOs(ProcessInfo processInfo) {
        try (Arena arena = Arena.ofConfined()) {
            // to get the exact args, we need to call sysctl on mac.
            MemorySegment mib = arena.allocate(MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT));
            mib.setAtIndex(ValueLayout.JAVA_INT, 0, sysctl_h_1.CTL_KERN());
            mib.setAtIndex(ValueLayout.JAVA_INT, 1, sysctl_h_1.KERN_PROCARGS2());
            mib.setAtIndex(ValueLayout.JAVA_INT, 2, Math.toIntExact(processInfo.pid()));

            MemorySegment sysctlBuffer = arena.allocate(sysctl_h_1.MAXCOMLEN());
            MemorySegment procargs_size = arena.allocate(size_t);
            procargs_size.set(ValueLayout.JAVA_LONG, 0, sysctlBuffer.byteSize());

            int sysctlRc = sysctl_h.sysctl(mib, 3, sysctlBuffer, procargs_size, NULL, 0);
            if (sysctlRc != 0) {
                MemorySegment addr = errno_h.__error();
                int error = addr.get(ValueLayout.JAVA_INT, 0);
                String desc =
                        switch (error) {
                            case errno_h.EFAULT -> "EFAULT";
                            case errno_h.EINVAL -> "EINVAL";
                            case errno_h.ENOMEM -> "ENOMEM";
                            case errno_h.ENOTDIR -> "ENOTDIR";
                            case errno_h.EISDIR -> "EISDIR";
                            case errno_h.ENOENT -> "ENOENT";
                            case errno_h.EPERM -> "EPERM";
                            default -> "" + error;
                        };

                return processInfo.withCommand("[" + sysctlRc + "|" + desc + "]" + processInfo.command());
            }

            /*
             * Make a sysctl() call to get the raw argument space of the process.
             * The layout is documented in start.s, which is part of the Csu
             * project.  In summary, it looks like:
             *
             * /---------------\ 0x00000000
             * :               :
             * :               :
             * |---------------|
             * | argc          |
             * |---------------|
             * | arg[0]        |
             * |---------------|
             * :               :
             * :               :
             * |---------------|
             * | arg[argc - 1] |
             * |---------------|
             * | 0             |
             * |---------------|
             * | env[0]        |
             * |---------------|
             * :               :
             * :               :
             * |---------------|
             * | env[n]        |
             * |---------------|
             * | 0             |
             * |---------------| <-- Beginning of data returned by sysctl() is here.
             * | argc          |
             * |---------------|
             * | exec_path     |
             * |:::::::::::::::|
             * |               |
             * | String area.  |
             * |               |
             * |---------------| <-- Top of stack.
             * :               :
             * :               :
             * \---------------/ 0xffffffff
             */
            // first int in return space is the number of arguments.
            int numberOfArgs = sysctlBuffer.get(ValueLayout.JAVA_INT, 0);

            return processInfo.withCommand("[n|" + numberOfArgs + "]" + processInfo.command());
        }
    }
}
