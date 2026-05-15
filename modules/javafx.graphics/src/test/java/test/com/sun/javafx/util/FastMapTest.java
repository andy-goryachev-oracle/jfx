/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package test.com.sun.javafx.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.sun.javafx.util.FastMap;
import com.sun.javafx.util.PKey;

/**
 * Tests FastMap.
 */
public class FastMapTest {
    @Test
    public void testFound() {
        int testCount = 500_000;
        IO.println("| Size | FastMap | FasterMap | HashMap |");
        IO.println("| ---: | ------: | --------: | ------: |");

        for (int n = 0; n < 8; n++) {
            int size = 1 << n;
            PKey[] keys = new PKey[size];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = new PKey();
            }

            FastMap m = FastMap.createForExperiment();
            HashMap h = new HashMap(size);
            FasterMap fm = new FasterMap(size);
            int ix;

            for (int i = 0; i < size; i++) {
                PKey k = keys[i];
                m.init(k, () -> FastMap.class);
                h.put(k, FastMap.class);
                fm.init(k, () -> FastMap.class);
            }

            // two-array FastMap
            ix = 0;
            long start = System.nanoTime();
            for (int i = 0; i < testCount; i++) {
                ix = ((ix + 1) % size);
                Object v = m.get(keys[ix]);
                if (v == null) {
                    throw new Error();
                }
            }
            double elapsed = (System.nanoTime() - start) / (double)testCount;

            // HashMap
            ix = 0;
            start = System.nanoTime();
            for (int i = 0; i < testCount; i++) {
                ix = ((ix + 1) % size);
                Object v = h.get(keys[ix]);
                if (v == null) {
                    throw new Error();
                }
            }
            double elapsed2 = (System.nanoTime() - start) / (double)testCount;
            
            // FasterMap
            ix = 0;
            start = System.nanoTime();
            for (int i = 0; i < testCount; i++) {
                ix = ((ix + 1) % size);
                Object v = fm.get(keys[ix]);
                if (v == null) {
                    throw new Error();
                }
            }
            double elapsed3 = (System.nanoTime() - start) / (double)testCount;

            IO.println("| " + size + " | " + elapsed + " | " + elapsed3 + " | " + elapsed2);
        }
        IO.println();
    }
    
    @Test
    public void testFound2() {
        testFound();
    }

    @Test
    public void testFound3() {
        testFound();
    }

    @Test
    public void testFound4() {
        testFound();
    }

    @Test
    public void testNotFound() {

    }

    public static class FasterMap {
        // PKey,value,...
        private final ArrayList<Object> elements;
        
        public FasterMap(int size) {
            elements = new ArrayList<>(size);
        }
        
        public <T> T init(PKey<T> key, Supplier<T> generator) {
            T value = generator.get();
            elements.add(key);
            elements.add(value);
            return value;
        }

        int indexOf(PKey<?> key) {
            int sz = elements.size();
            for (int i = 0; i < sz; i+=2) {
                if (elements.get(i) == key) {
                    return i + 1;
                }
            }
            return -1;
        }

        public <T> T get(PKey<T> key) {
            int ix = indexOf(key);
            if (ix < 0) {
                return null;
            }
            return (T)elements.get(ix);
        }

        public int size() {
            return elements.size() >> 1;
        }
    }
}
