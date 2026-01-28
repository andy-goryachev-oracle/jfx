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
package com.sun.javafx.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * This map-like object holds properties that are lazily created
 * by the Node class and its descendants (Region, etc.).
 * The main idea behind it is that on average, very few of these properties are instantiated,
 * so by placing these properties into an elastic map we could save some memory.
 * The lookup here is implemented by a super-fast == comparison, so every PKey must be
 * statically declared.
 */
public class FastMap {
    private final int keyCount;
    private final ArrayList<PKey<?>> keys;
    private final ArrayList<Object> values;
    private static WeakHashMap<FastMap,Object> all = new WeakHashMap(1000);
    static {
        init();
    }

    public FastMap(Node node) {
        // TODO cache the info
        this.keyCount = countKeys(node);
        int capacity = 4; // TODO compute from type
        keys = new ArrayList<>(capacity);
        values = new ArrayList<>(capacity);
        all.put(this, null);
    }

    public <T> T init(PKey<T> key, Supplier<T> generator) {
        T value = generator.get();
        keys.add(key);
        values.add(value);
        return value;
    }

    private int indexOf(PKey<?> key) {
        int sz = keys.size();
        for (int i = 0; i < sz; i++) {
            if (keys.get(i) == key) {
                return i;
            }
        }
        return -1;
    }

    public <T> T get(PKey<T> key) {
        int ix = indexOf(key);
        if (ix < 0) {
            return null;
        }
        return (T)values.get(ix);
    }

    public <T> void remove(PKey<T> key) {
        int ix = indexOf(key);
        if (ix >= 0) {
            keys.remove(ix);
            values.remove(ix);
        }
    }

    public int size() {
        return keys.size();
    }

    private static void init() {
        Platform.runLater(() -> {
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(5), (ev) -> dump()));
            //t.setDelay(Duration.seconds(5));
            t.setCycleCount(Timeline.INDEFINITE);
            t.play();
        });
    }

    private static int countKeys(Node node) {
        int count = 0;
        Class<?> c = node.getClass();
        for (;;) {
            Field[] fs = c.getDeclaredFields();
            for (Field f : fs) {
                if ((f.getType() == PKey.class) && Modifier.isStatic(f.getModifiers())) {
                    count++;
                }
            }

            if (c == Node.class) {
                return count;
            }
            c = c.getSuperclass();
        }
    }

    private static void dump() {
        int count = 0; // number of nodes
        int used = 0; // number of pointers actually used
        int max = 0; // max number of possible pointers
        for (FastMap m : all.keySet()) {
            if (m != null) {
                count++;
                used += m.size();
                max += m.keyCount;
            }
        }

        // TODO histogram of keys

        float ut = used / ((float)max);
        int sv = (max - used) * 8; // 64 bit pointers
        float av = used / (float)count;
        String s = MessageFormat.format("Nodes={0} utilization={1} average={2} saved={3} bytes", count, ut, av, sv);
        System.out.println(s);
    }
}
