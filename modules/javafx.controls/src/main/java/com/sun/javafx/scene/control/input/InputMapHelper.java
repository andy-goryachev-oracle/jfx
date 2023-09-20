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
import javafx.scene.control.Skin;
import javafx.scene.control.input.EventCriteria;
import javafx.scene.control.input.FunctionTag;
import javafx.scene.control.input.InputMap2;
import javafx.scene.control.input.KeyBinding2;
import javafx.scene.input.KeyCode;
import com.sun.javafx.util.Utils;

/**
 * Provides access to the private methods in InputMap(2).
 */
public class InputMapHelper {
    public interface Accessor<C extends Control> {
        public <T extends Event> void addHandler(
            InputMap2 inputMap,
            Skin<C> skin,
            EventCriteria<T> criteria,
            boolean consume,
            boolean tail,
            EventHandler<T> handler
        );

        public <T extends Event> void addHandler(
            InputMap2 inputMap,
            Skin<C> skin,
            EventType<T> type,
            boolean consume,
            boolean tail,
            EventHandler<T> handler
        );
        
        public void regFunc(InputMap2 im, Skin<?> b, FunctionTag tag, Runnable function);
        
        public void regKey(InputMap2 im, Skin<?> b, KeyBinding2 k, FunctionTag tag);
        
        public void regKey(InputMap2 im, Skin<?> b, KeyCode code, FunctionTag tag);
        
        public void setOnKeyEventEnter(InputMap2 im, Skin<?> b, Runnable action);
        
        public void setOnKeyEventExit(InputMap2 im, Skin<?> b, Runnable action);
        
        public void unregister(InputMap2 im, Skin<?> behavior);
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
        Skin<C> skin,
        EventType<T> type,
        boolean consume,
        boolean tail,
        EventHandler<?> handler
    ) {
        InputMap2 im = inputMap(skin);
        accessor.addHandler(im, skin, type, consume, tail, handler);
    }

    public static <C extends Control, T extends Event> void addHandler(
        Skin<C> skin,
        EventCriteria<?> criteria,
        boolean consume,
        boolean tail,
        EventHandler<T> handler
    ) {
        InputMap2 im = inputMap(skin);
        accessor.addHandler(im, skin, criteria, consume, tail, handler);
    }
    
    public static <C extends Control> void regFunc(Skin<C> skin, FunctionTag tag, Runnable function) {
        InputMap2 im = inputMap(skin);
        accessor.regFunc(im, skin, tag, function);
    }
    
    public static <C extends Control> void regKey(Skin<C> skin, KeyBinding2 k, FunctionTag tag) {
        InputMap2 im = inputMap(skin);
        accessor.regKey(im, skin, k, tag);
    }
    
    public static <C extends Control> void regKey(Skin<C> skin, KeyCode code, FunctionTag tag) {
        InputMap2 im = inputMap(skin);
        accessor.regKey(im, skin, code, tag);
    }
    
    public static <C extends Control> void setOnKeyEventEnter(Skin<C> skin, Runnable action) {
        InputMap2 im = inputMap(skin);
        accessor.setOnKeyEventEnter(im, skin, action);
    }
    
    public static <C extends Control> void setOnKeyEventExit(Skin<C> skin, Runnable action) {
        InputMap2 im = inputMap(skin);
        accessor.setOnKeyEventExit(im, skin, action);
    }
    
    public static <C extends Control> void unregister(Skin<C> skin) {
        InputMap2 im = inputMap(skin);
        accessor.unregister(im, skin);
    }

    private static <C extends Control> InputMap2 inputMap(Skin<C> skin) {
        return skin.getSkinnable().getInputMap2();
    }
}
