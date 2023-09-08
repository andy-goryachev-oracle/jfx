/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javafx.scene.control.input;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.input.EventCriteria;
import javafx.scene.control.input.FunctionTag;
import javafx.scene.control.input.InputMap2;
import javafx.scene.control.input.KeyBinding2;
import javafx.scene.input.KeyCode;
import com.sun.javafx.util.Utils;

/**
 * Provides access to private methods in InputMap(2).
 */
public class InputMapHelper {
    public interface Accessor<C extends Control> {
        public <T extends Event> void addHandler(
            InputMap2 inputMap,
            BehaviorBase2<C> behavior,
            EventCriteria<T> criteria,
            boolean consume,
            boolean tail,
            EventHandler<T> handler
        );

        public <T extends Event> void addHandler(
            InputMap2 inputMap,
            BehaviorBase2<C> behavior,
            EventType<T> type,
            boolean consume,
            boolean tail,
            EventHandler<T> handler
        );
        
        public void regFunc(InputMap2 im, BehaviorBase2<?> b, FunctionTag tag, Runnable function);
        
        public void regKey(InputMap2 im, BehaviorBase2<?> b, KeyBinding2 k, FunctionTag tag);
        
        public void regKey(InputMap2 im, BehaviorBase2<?> b, KeyCode code, FunctionTag tag);
        
        public void setOnKeyEventEnter(InputMap2 im, BehaviorBase2<?> b, Runnable action);
        
        public void setOnKeyEventExit(InputMap2 im, BehaviorBase2<?> b, Runnable action);
        
        public void unregister(InputMap2 im, BehaviorBase2<?> behavior);
    }

    static {
        Utils.forceInit(InputMap2.class);
    }

    private static Accessor accessor;

    private InputMapHelper() {
    }

    public static void setAccessor(Accessor a) {
        if (accessor != null) {
            throw new IllegalStateException();
        }
        accessor = a;
    }

    public static <C extends Control, T extends Event> void addHandler(
        InputMap2 im,
        BehaviorBase2<C> behavior,
        EventType<T> type,
        boolean consume,
        boolean tail,
        EventHandler<?> handler
    ) {
        accessor.addHandler(im, behavior, type, consume, tail, handler);
    }

    public static <C extends Control, T extends Event> void addHandler(
        InputMap2 im,
        BehaviorBase2<?> behavior,
        EventCriteria<?> criteria,
        boolean consume,
        boolean tail,
        EventHandler<T> handler
    ) {
        accessor.addHandler(im, behavior, criteria, consume, tail, handler);
    }
    
    public static void regFunc(InputMap2 im, BehaviorBase2<?> b, FunctionTag tag, Runnable function) {
        accessor.regFunc(im, b, tag, function);
    }
    
    public static void regKey(InputMap2 im, BehaviorBase2<?> b, KeyBinding2 k, FunctionTag tag) {
        accessor.regKey(im, b, k, tag);
    }
    
    public static void regKey(InputMap2 im, BehaviorBase2<?> b, KeyCode code, FunctionTag tag) {
        accessor.regKey(im, b, code, tag);
    }
    
    public static void setOnKeyEventEnter(InputMap2 im, BehaviorBase2<?> b, Runnable action) {
        accessor.setOnKeyEventEnter(im, b, action);
    }
    
    public static void setOnKeyEventExit(InputMap2 im, BehaviorBase2<?> b, Runnable action) {
        accessor.setOnKeyEventExit(im, b, action);
    }
    
    public static void unregister(InputMap2 im, BehaviorBase2<?> b) {
        accessor.unregister(im, b);
    }
}
