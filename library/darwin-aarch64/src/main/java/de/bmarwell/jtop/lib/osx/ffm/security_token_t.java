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
package de.bmarwell.jtop.lib.osx.ffm; // Generated by jextract

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.MemoryLayout.PathElement.sequenceElement;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

/**
 * {@snippet lang=c :
 * struct {
 *     unsigned int val[2];
 * }
 * }
 */
public class security_token_t {

    security_token_t() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
                    MemoryLayout.sequenceLayout(2, libproc_h.C_INT).withName("val"))
            .withName("$anon$462:9");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final SequenceLayout val$LAYOUT = (SequenceLayout) $LAYOUT.select(groupElement("val"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int val[2]
     * }
     */
    public static final SequenceLayout val$layout() {
        return val$LAYOUT;
    }

    private static final long val$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int val[2]
     * }
     */
    public static final long val$offset() {
        return val$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int val[2]
     * }
     */
    public static MemorySegment val(MemorySegment struct) {
        return struct.asSlice(val$OFFSET, val$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int val[2]
     * }
     */
    public static void val(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, val$OFFSET, val$LAYOUT.byteSize());
    }

    private static long[] val$DIMS = {2};

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * unsigned int val[2]
     * }
     */
    public static long[] val$dimensions() {
        return val$DIMS;
    }

    private static final VarHandle val$ELEM_HANDLE = val$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * unsigned int val[2]
     * }
     */
    public static int val(MemorySegment struct, long index0) {
        return (int) val$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * unsigned int val[2]
     * }
     */
    public static void val(MemorySegment struct, long index0, int fieldValue) {
        val$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this struct
     */
    public static long sizeof() {
        return layout().byteSize();
    }

    /**
     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(layout());
    }

    /**
     * Allocate an array of size {@code elementCount} using {@code allocator}.
     * The returned segment has size {@code elementCount * layout().byteSize()}.
     */
    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return reinterpret(addr, 1, arena, cleanup);
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code elementCount * layout().byteSize()}
     */
    public static MemorySegment reinterpret(
            MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
    }
}