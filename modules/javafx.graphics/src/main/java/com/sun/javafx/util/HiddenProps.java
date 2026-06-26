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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.util.Duration;
import com.sun.javafx.scene.NodeHelper;

/**
 * This map-like object holds properties and fields that are lazily created
 * by the Node class and its descendants (Region, etc.).
 * The main idea behind it is that on average, very few of these objects are instantiated,
 * so we could save some RAM by placing these objects into a hidden map.
 * The other benefit is that we could freely add useful, but rarely used properties
 * that require complex subscription chains, for example the visible parent Window.
 */
public class HiddenProps {
    /** Enables periodic dumping of utilization statistics to stdout */
    private static final boolean COLLECT_STATISTICS = true;
    private static final int INITIAL_CAPACITY = 1;

    private final HashMap<PKey<?>,Object> data;

    private HiddenProps() {
        this.data = new HashMap<>(INITIAL_CAPACITY);
    }

    // TODO class needed only for statistics, to obtain the PKey name
    // (and even then it can be inferred from the object)
    public static HiddenProps create(Class<? extends Node> cls) {
        return COLLECT_STATISTICS ? new HiddenPropsWithStats(cls) : new HiddenProps();
    }

    public static HiddenProps get(Node n) {
        return NodeHelper.getHiddenProps(n);
    }

    public static boolean hasProperty(Node n, PKey<?> key) {
        HiddenProps props = get(n);
        return (props.get(key) != null);
    }

    public static <T> T getProperty(Node n, PKey<T> key) {
        HiddenProps props = get(n);
        return props.get(key);
    }

    public <T> T init(PKey<T> key, Supplier<T> generator) {
        T value = generator.get();
        data.put(key, value);
        return value;
    }

    public <T> T get(PKey<T> key) {
        return (T)data.get(key);
    }

    public <T> void remove(PKey<T> key) {
        data.remove(key);
    }

    public int size() {
        return data.size();
    }

    Set<PKey<?>> keys() {
        return data.keySet();
    }

    public <P extends Property<?>> boolean isSettable(PKey<P> key) {
        P p = get(key);
        return (p == null) || !p.isBound();
    }

    public static <P extends Property<?>> boolean isSettable(Node n, PKey<P> key) {
        return HiddenProps.get(n).isSettable(key);
    }

    private static class HiddenPropsWithStats extends HiddenProps {
        record FEntry(String name, int count) { }
        record HEntry(int size, int count) { }
        
        private final int keyCount;
        private static final AtomicLong indexOfCount = new AtomicLong();
        private static HashMap<PKey<?>,String> keyNames = new HashMap<>();
        private static HashMap<Class<?>,Integer> counts = new HashMap<>();
        private static WeakHashMap<HiddenPropsWithStats,Object> all = new WeakHashMap(1000);
        private static Comparator<FEntry> freqComp;
        private static Comparator<HEntry> histComp;
        static {
            init();
        }

        public HiddenPropsWithStats(Class<?> cls) {
            this.keyCount = countKeys(cls);
            all.put(this, null);
        }

        private static void init() {
            freqComp = new Comparator<FEntry>() {
                @Override
                public int compare(FEntry a, FEntry b) {
                    return b.count - a.count;
                }
            };
            histComp = new Comparator<HEntry>() {
                @Override
                public int compare(HEntry a, HEntry b) {
                    return a.size - b.size;
                }
            };
            
            Platform.runLater(() -> {
                Timeline t = new Timeline(new KeyFrame(Duration.seconds(10), (ev) -> dump()));
                t.setDelay(Duration.seconds(5));
                t.setCycleCount(Timeline.INDEFINITE);
                t.play();
            });
        }

        private static int countKeys(Class<?> cls) {
            Integer cached = counts.get(cls);
            if(cached != null) {
                return cached.intValue();
            }
    
            Class<?> c = cls;
            int count = 0;
            for (;;) {
                Field[] fs = c.getDeclaredFields();
                for (Field f : fs) {
                    if ((f.getType() == PKey.class) && Modifier.isStatic(f.getModifiers())) {
                        // frequency dump needs the key name 
                        String name = c.getSimpleName() + "." + f.getName();
                        if(!name.startsWith("Node")) {
                            int zz = 0;
                        }
                        try {
                            f.setAccessible(true);
                            PKey k = (PKey)f.get(null);
                            keyNames.put(k, name);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        count++;
                    }
                }
    
                if (c == Node.class) {
                    counts.put(cls, Integer.valueOf(count));
                    System.out.println(cls + " properties=" + count);
                    return count;
                }
                c = c.getSuperclass();
            }
        }
    
        private static String getName(PKey k) {
            String s = keyNames.get(k);
            return (s == null) ? k.toString() : s;
        }
    
        private static void dump() {
            int count = 0; // number of nodes
            int used = 0; // number of pointers actually used
            int max = 0; // max number of possible pointers
            int top = 0; // largest map size
            HashMap<PKey, AtomicInteger> freq = new HashMap<>();
            HashMap<Integer, AtomicInteger> hist = new HashMap<>();
            for (HiddenPropsWithStats m : all.keySet()) {
                if (m != null) {
                    int sz = m.size();
                    count++;
                    used += m.size();
                    max += m.keyCount;
                    if (sz > top) {
                        top = sz;
                    }
    
                    for (PKey<?> k : m.keys()) {
                        AtomicInteger ct = freq.get(k);
                        if (ct == null) {
                            ct = new AtomicInteger(1);
                            freq.put(k, ct);
                        } else {
                            ct.incrementAndGet();
                        }
                    }

                    AtomicInteger ct = hist.get(sz);
                    if (ct == null) {
                        ct = new AtomicInteger(1);
                        hist.put(sz, ct);
                    } else {
                        ct.incrementAndGet();
                    }
                }
            }
    
            float ut = used / ((float)max);
            int sv = (max - used) * 8; // 64 bit pointers
            float av = used / (float)count;
            long indof = indexOfCount.get();
            String s = MessageFormat.format(
                "Nodes={0} utilization={1} average={2} top={3} indexOf={4} saved={5} bytes",
                count,
                ut,
                av,
                top,
                indof,
                sv);
            System.out.println(s);
            System.out.println(frequencies(freq));
            System.out.println(histogram(hist));
        }

        private static String histogram(HashMap<Integer, AtomicInteger> hist) {
            ArrayList<HEntry> entries = new ArrayList<>();
            for (Integer k : hist.keySet()) {
                int ct = hist.get(k).intValue();
                entries.add(new HEntry(k, ct));
            }
            entries.sort(histComp);

            StringBuilder sb = new StringBuilder();
            for (HEntry en : entries) {
                String name = String.valueOf(en.size);
                switch (name.length()) {
                case 1:
                    name = " " + name;
                    break;
                }
                sb.append("   ");
                sb.append(name);
                sb.append(": ");
                sb.append(en.count);
                sb.append("\n");
            }
            return sb.toString();
        }

        private static String frequencies(HashMap<PKey, AtomicInteger> freq) {
            ArrayList<FEntry> entries = new ArrayList<>();
            for (PKey k : freq.keySet()) {
                int ct = freq.get(k).intValue();
                String name = getName(k);
                entries.add(new FEntry(name, ct));
            }
            entries.sort(freqComp);

            StringBuilder sb = new StringBuilder();
            for (FEntry en : entries) {
                sb.append("   ");
                sb.append(en.name);
                sb.append(": ");
                sb.append(en.count);
                sb.append("\n");
            }
            return sb.toString();
        }
    }
}
