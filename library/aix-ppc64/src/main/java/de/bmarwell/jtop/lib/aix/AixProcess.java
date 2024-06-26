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
package de.bmarwell.jtop.lib.aix;

import static java.util.Collections.emptyList;

import de.bmarwell.jtop.lib.api.ProcessInfo;
import de.bmarwell.jtop.lib.api.spi.AbstractProcessH;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class AixProcess extends AbstractProcessH {

    private static final Linker LINKER = Linker.nativeLinker();

    private MemorySegment getargs_addr;

    public AixProcess() {
        SymbolLookup stdLib = LINKER.defaultLookup();
        this.getargs_addr =
                stdLib.find("getargs").orElseThrow(() -> new IllegalStateException("function call getargs not found!"));
    }

    @Override
    public ProcessInfo mapProcessOs(ProcessInfo processInfo) {
        if (!processInfo.commandLine().isBlank()) {
            return processInfo;
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment procinfo_addr = arena.allocate(76);
            MemorySegment argsString = arena.allocate(2048);

            // Create a description of the C function
            FunctionDescriptor getargs_sig = FunctionDescriptor.of(
                    // returns int
                    ValueLayout.JAVA_LONG,
                    // struct procinfo
                    ValueLayout.ADDRESS,
                    // int length of procinfo
                    ValueLayout.JAVA_LONG,
                    // char argsBuffer
                    ValueLayout.ADDRESS,
                    // int length of argsBuffer
                    ValueLayout.JAVA_LONG);

            MemorySegment procsInfo = arena.allocate(procinfo_addr.byteSize());

            // Create a downcall handle for the C function
            MethodHandle getargs = LINKER.downcallHandle(getargs_addr, getargs_sig);

            // Call the C function directly from Java
            procsInfo.set(ValueLayout.JAVA_INT, 0, (int) processInfo.pid());
            long rc = (long) getargs.invokeExact(procsInfo, procsInfo.byteSize(), argsString, argsString.byteSize());

            if (rc != 0) {
                return processInfo;
            }

            String argsStringRead = argsString.getString(0, StandardCharsets.UTF_8);

            if (argsStringRead == null || argsStringRead.isEmpty()) {
                return processInfo;
            }

            String[] commandAndArgs = NULL_BYTE.split(argsStringRead, 2);

            final List<String> args;
            if (commandAndArgs.length != 2) {
                args = emptyList();
            } else {
                args = Arrays.asList(NULL_BYTE.split(commandAndArgs[1]));
            }

            return new ProcessInfo(
                    processInfo.pid(), processInfo.ppid(), processInfo.user(), "[n]" + commandAndArgs[0], args);
        } catch (NoSuchElementException noSuchElementException) {
            throw noSuchElementException;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
