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

import java.util.Objects;
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

/**
 * Class provides a foundation for behaviors.
 * <p>
 * A concrete behavior implementation should do three things:
 * 1. provide default behavior methods (a.k.a. functions)
 * 2. in install() method, called from Skin.install(), map control's function tags to
 *    behavior methods, map key bindings to function tags, and add additional event handlers,
 *    using func(), key(), and hand() methods correspondingly.
 *    Important: no mapping should be made in the behavior constructor, only in install().
 * <p>
 * The base class adds a dispose() method (called from Skin.dispose()),
 * which undoes the mapping done in install().
 * <p>
 * TODO rename BehaviorBase/Behavior
 */
public abstract class BehaviorBase2<C extends Control> {
    private C control;

    /** The constructor. */
    public BehaviorBase2() {
    }
    
    /**
     * Returns the associated Control instance.
     * TODO rename getControl()
     * @return the owner
     */
    protected final C getNode() {
        return control;
    }

    /**
     * Returns the input map of the associated Control.
     * TODO rename getInputMap()
     * @return the input map
     */
    protected final InputMap2 getInputMap2() {
        return control.getInputMap2();
    }

    /**
     * Installs this behavior.
     * This method must be called in Skin.install() to actually install all the default mappings.
     * @param skin the skin
     */
    public void install(Skin<C> skin) {
        Objects.nonNull(skin);
        this.control = skin.getSkinnable();
    }

    /**
     * Disposes of this behavior.
     */
    public void dispose() {
        InputMapHelper.unregister(control.getInputMap2(), this);
    }
    
    /**
     * Maps a function to the function tag.
     * This method will not override any previous mapping added by {@link #regFunc(FunctionTag,Runnable)}.
     *
     * @param tag the function tag
     * @param function the function
     */
    protected void regFunc(FunctionTag tag, Runnable function) {
        InputMapHelper.regFunc(getInputMap2(), this, tag, function);
    }

    /**
     * Maps a key binding to the specified function tag.
     * A null key binding will result in no change to this input map.
     * This method will not override a user mapping.
     *
     * @param k the key binding, can be null (TODO or KB.NA)
     * @param tag the function tag
     */
    protected void regKey(KeyBinding2 k, FunctionTag tag) {
        InputMapHelper.regKey(getInputMap2(), this, k, tag);
    }

    /**
     * Maps a key binding to the specified function tag.
     * This method will not override a user mapping added by {@link #regKey(KeyBinding2,FunctionTag)}.
     *
     * @param code the key code to construct a {@link KeyBinding2}
     * @param tag the function tag
     */
    protected void regKey(KeyCode code, FunctionTag tag) {
        InputMapHelper.regKey(getInputMap2(), this, code, tag);
    }

    /**
     * This convenience method maps the function tag to the specified function, and at the same time
     * maps the specified key binding to that function tag.
     * @param tag the function tag
     * @param k the key binding
     * @param func the function
     */
    protected void reg(FunctionTag tag, KeyBinding2 k, Runnable func) {
        getInputMap2().regFunc(tag, func);
        getInputMap2().regKey(k, tag);
    }

    /**
     * This convenience method maps the function tag to the specified function, and at the same time
     * maps the specified key binding to that function tag.
     * @param tag the function tag
     * @param code the key code
     * @param func the function
     */
    protected void reg(FunctionTag tag, KeyCode code, Runnable func) {
        getInputMap2().regFunc(tag, func);
        getInputMap2().regKey(KeyBinding2.of(code), tag);
    }

    /**
     * Adds an event handler for the specified event type, in the context of this Behavior.
     * The handler will get removed in {@link#dispose()} method.
     * This mapping always consumes the matching event.
     *
     * @param <T> the actual event type
     * @param type the event type
     * @param handler the event handler
     */
    protected <T extends Event> void addHandler(EventType<T> type, EventHandler<T> handler) {
        InputMapHelper.addHandler(getInputMap2(), this, type, true, false, handler);
    }
    
    /**
     * Adds an event handler for the specified event type, in the context of this Behavior.
     * The handler will get removed in {@link#dispose()} method.
     *
     * @param <T> the actual event type
     * @param type the event type
     * @param consume determines whether the matching event is consumed or not
     * @param handler the event handler
     */
    protected <T extends Event> void addHandler(EventType<T> type, boolean consume, EventHandler<T> handler) {
        InputMapHelper.addHandler(getInputMap2(), this, type, consume, false, handler);
    }
    
    /**
     * Adds an event handler for the specified event type, in the context of this Behavior.
     * This event handler will get invoked after all handlers added via map() methods.
     * The handler will get removed in {@link#dispose()} method.
     * This mapping always consumes the matching event.
     *
     * @param <T> the actual event type
     * @param type the event type
     * @param handler the event handler
     */
    protected <T extends Event> void addHandlerTail(EventType<T> type, EventHandler<T> handler) {
        InputMapHelper.addHandler(getInputMap2(), this, type, true, true, handler);
    }
    
    /**
     * Adds an event handler for the specified event type, in the context of this Behavior.
     * This event handler will get invoked after all handlers added via map() methods.
     * The handler will get removed in {@link#dispose()} method.
     *
     * @param <T> the actual event type
     * @param type the event type
     * @param consume determines whether the matching event is consumed or not
     * @param handler the event handler
     */
    protected <T extends Event> void addHandlerTail(EventType<T> type, boolean consume, EventHandler<T> handler) {
        InputMapHelper.addHandler(getInputMap2(), this, type, consume, true, handler);
    }

    /**
     * Adds an event handler for the specific event criteria, in the context of this Behavior.
     * This is a more specific version of {@link #addHandler(EventType,EventHandler)} method.
     * The handler will get removed in {@link#dispose()} method.
     *
     * @param <T> the actual event type
     * @param criteria the matching criteria
     * @param consume determines whether the matching event is consumed or not
     * @param handler the event handler
     */
    protected <T extends Event> void addHandler(EventCriteria<T> criteria, boolean consume, EventHandler<T> handler) {
        InputMapHelper.addHandler(getInputMap2(), this, criteria, consume, false, handler);
    }

    /**
     * Adds an event handler for the specific event criteria, in the context of this Behavior.
     * This event handler will get invoked after all handlers added via map() methods.
     * The handler will get removed in {@link#dispose()} method.
     *
     * @param <T> the actual event type
     * @param criteria the matching criteria
     * @param consume determines whether the matching event is consumed or not
     * @param handler the event handler
     */
    protected <T extends Event> void addHandlerTail(
        EventCriteria<T> criteria,
        boolean consume,
        EventHandler<T> handler
    ) {
        InputMapHelper.addHandler(getInputMap2(), this, criteria, consume, true, handler);
    }

    /**
     * Sets the code to be executed just before handling of the key events.
     * @param action the action or null
     */
    protected void setOnKeyEventEnter(Runnable action) {
        InputMapHelper.setOnKeyEventEnter(getInputMap2(), this, action);
    }

    /**
     * Sets the code to be executed just after handling of the key events.
     * @param action the action or null
     */
    protected void setOnKeyEventExit(Runnable action) {
        InputMapHelper.setOnKeyEventExit(getInputMap2(), this, action);
    }
}
