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

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

public final class ProcInfoH {

    public static final int MAXCOMLEN = 32;

    public static final int SIGMAX32 = 63;

    public static final MemoryLayout CMD = MemoryLayout.sequenceLayout(MAXCOMLEN + 1, ValueLayout.JAVA_CHAR);

    public static final MemoryLayout SIG_FLAGS = MemoryLayout.sequenceLayout(SIGMAX32 + 1, ValueLayout.JAVA_CHAR);

    public static final StructLayout struct_procsinfo = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("pid"),
            ValueLayout.JAVA_INT.withName("ppid"), // parent pid
            ValueLayout.JAVA_INT, // session id
            ValueLayout.JAVA_INT, // proc group
            ValueLayout.JAVA_INT, // real uid
            ValueLayout.JAVA_INT, // saved uid
            // TTY info
            ValueLayout.JAVA_INT, // has term
            ValueLayout.JAVA_INT, // term
            ValueLayout.JAVA_INT, // channel
            // scheduler info
            ValueLayout.JAVA_INT, // nice
            ValueLayout.JAVA_INT, // state
            ValueLayout.JAVA_INT, // flags
            ValueLayout.JAVA_INT, // flags2
            ValueLayout.JAVA_INT, // thread count
            ValueLayout.JAVA_INT.withName("pi_cpu"), // first thread's tick count
            ValueLayout.JAVA_INT.withName("pi_pri"), // first thread's prio
            // mem
            ValueLayout.JAVA_INT, // proc addr space
            ValueLayout.JAVA_LONG, // i/o page faults
            ValueLayout.JAVA_LONG, // non i/o page faults
            ValueLayout.JAVA_LONG, // repaging count
            ValueLayout.JAVA_LONG, // size of  image (pages)

            // zombie info
            ValueLayout.JAVA_INT, // reserved
            ValueLayout.JAVA_INT, // user time
            ValueLayout.JAVA_INT, // reserved
            ValueLayout.JAVA_INT, // sysstem time

            // cred info
            ValueLayout.JAVA_INT, // TODO: struct ucred

            // accounting and profiling data
            ValueLayout.JAVA_INT, // rusage TODO: struct rusage64
            ValueLayout.JAVA_INT, // children's rusage TODO: struct rusage64
            ValueLayout.JAVA_LONG, // i/o char count
            ValueLayout.JAVA_LONG, // acccumulator memory integral
            ValueLayout.JAVA_LONG, // time start

            // resource limits info
            ValueLayout.JAVA_INT, // TODO: struct rlimit resource limits

            // file management
            ValueLayout.JAVA_SHORT, // mask for file creation
            ValueLayout.JAVA_SHORT.withName("pi_cdir"),
            ValueLayout.JAVA_SHORT.withName("pi_rdir"),
            ValueLayout.JAVA_SHORT.withName("pi_maxofile"),

            // program name
            CMD.withName("pi_comm"),

            // memory usage info
            ValueLayout.JAVA_LONG.withName("pi_drss"),
            ValueLayout.JAVA_LONG.withName("pi_trss"),
            ValueLayout.JAVA_LONG.withName("pi_dvm"),
            ValueLayout.JAVA_LONG.withName("pi_prm"),
            ValueLayout.JAVA_LONG.withName("pi_tsize"),
            ValueLayout.JAVA_LONG.withName("pi_dsize"),
            ValueLayout.JAVA_LONG.withName("pi_sdsize"),

            // signal management
            ValueLayout.JAVA_LONG.withName("pi_signal"),
            SIG_FLAGS.withName("pi_sigflags"));
}
