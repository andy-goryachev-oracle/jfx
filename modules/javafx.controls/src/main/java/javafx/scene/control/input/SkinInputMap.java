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
package javafx.scene.control.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.input.KeyCode;
import com.sun.javafx.scene.control.input.EventHandlerPriority;
import com.sun.javafx.scene.control.input.PHList;

/**
 * Input Map for use by the Skins.
 */
public abstract class SkinInputMap<C extends Skinnable, F> {
    private static final Object ON_KEY_ENTER = new Object();
    private static final Object ON_KEY_EXIT = new Object();
    // KeyBinding -> FunctionTag
    // FunctionTag -> Runnable
    // ON_KEY_ENTER/ON_KEY_EXIT -> Runnable
    // EventType -> HList of listeners (with priority)
    final HashMap<Object,Object> map = new HashMap<>();

    // use the factory methods to create an instance of SkinInputMap
    private SkinInputMap() {
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
    public <T extends Event> void addHandler(EventType<T> type, EventHandler<T> handler) {
        addHandler(type, true, EventHandlerPriority.SKIN_HIGH, handler);
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
    public <T extends Event> void addHandler(EventType<T> type, boolean consume, EventHandler<T> handler) {
        addHandler(type, consume, EventHandlerPriority.SKIN_HIGH, handler);
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
    public <T extends Event> void addHandlerLast(EventType<T> type, EventHandler<T> handler) {
        addHandler(type, true, EventHandlerPriority.SKIN_LOW, handler);
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
    public <T extends Event> void addHandlerLast(EventType<T> type, boolean consume, EventHandler<T> handler) {
        addHandler(type, consume, EventHandlerPriority.SKIN_LOW, handler);
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
    public <T extends Event> void addHandler(EventCriteria<T> criteria, boolean consume, EventHandler<T> handler) {
        addHandler(criteria, consume, EventHandlerPriority.SKIN_HIGH, handler);
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
    public <T extends Event> void addHandlerLast(
        EventCriteria<T> criteria,
        boolean consume,
        EventHandler<T> handler
    ) {
        addHandler(criteria, consume, EventHandlerPriority.SKIN_HIGH, handler);
    }

    // FIX remove
    private <T extends Event> void addHandler(
        EventType<T> type,
        boolean consume,
        EventHandlerPriority pri,
        EventHandler<T> handler)
    {
        if (consume) {
            // FIX remove
            extendHandlers(type, pri, new EventHandler<T>() {
                @Override
                public void handle(T ev) {
                    handler.handle(ev);
                    ev.consume();
                }
            });
        } else {
            extendHandlers(type, pri, handler);
        }
    }

    // FIX remove
    private <T extends Event> void addHandler(
        EventCriteria<T> criteria,
        boolean consume,
        EventHandlerPriority pri,
        EventHandler<T> handler
    ) {
        EventType<T> type = criteria.getEventType();
        extendHandlers(type, pri, new EventHandler<T>() {
            @Override
            public void handle(T ev) {
                if (criteria.isEventAcceptable(ev)) {
                    handler.handle(ev);
                    if (consume) {
                        ev.consume();
                    }
                }
            }
        });
    }

    private <T extends Event> void extendHandlers(
        EventType<T> type,
        EventHandlerPriority priority,
        EventHandler<T> handler)
    {
        Object x = map.get(type);
        PHList hs;
        if(x instanceof PHList h) {
            hs = h;
        } else {
            hs = new PHList();
            map.put(type, hs);
        }
        hs.add(handler, priority);
    }

    /**
     * Maps a key binding to the specified function tag.
     *
     * @param k the key binding
     * @param tag the function tag
     */
    public void registerKey(KeyBinding k, FunctionTag tag) {
        map.put(k, tag);
    }
    
    /**
     * Maps a key binding to the specified function tag.
     *
     * @param code the key code to construct a {@link KeyBinding}
     * @param tag the function tag
     */
    public void registerKey(KeyCode code, FunctionTag tag) {
        registerKey(KeyBinding.of(code), tag);
    }

    /**
     * Maps a function to the specified function tag.
     * This method will not override any previous mapping added by {@link #registerFunction(FunctionTag,Runnable)}.
     *
     * @param tag the function tag
     * @param function the function
     */
    public final void registerFunction(FunctionTag tag, F function) {
        map.put(tag, function);
    }
    
    /**
     * This convenience method maps the function tag to the specified function, and at the same time
     * maps the specified key binding to that function tag.
     * @param tag the function tag
     * @param k the key binding
     * @param func the function
     */
    public void register(FunctionTag tag, KeyBinding k, F func) {
        registerFunction(tag, func);
        registerKey(k, tag);
    }

    /**
     * This convenience method maps the function tag to the specified function, and at the same time
     * maps the specified key binding to that function tag.
     * @param tag the function tag
     * @param code the key code
     * @param func the function
     */
    public void register(FunctionTag tag, KeyCode code, F func) {
        registerFunction(tag, func);
        registerKey(KeyBinding.of(code), tag);
    }
    
    /**
     * Returns a {@code FunctionTag} mapped to the specified {@link KeyBinding},
     * or null if no such mapping exists.
     *
     * @param k the key binding
     * @return the function tag, or null
     */
    public FunctionTag getFunctionTag(KeyBinding k) {
        Object x = map.get(k);
        if (x instanceof FunctionTag t) {
            return t;
        }
        return null;
    }
    
    /**
     * Sets the code to be executed just before handling of the key events.
     * @param action the action or null
     */
    public final void setOnKeyEventEnter(F function) {
        map.put(ON_KEY_ENTER, function);
    }

    /**
     * Sets the code to be executed just after handling of the key events.
     * @param action the action or null
     */
    public final void setOnKeyEventExit(F function) {
        map.put(ON_KEY_EXIT, function);
    }

    /**
     * Collects the key bindings mapped by the skin.
     *
     * @return a Set of key bindings
     */
    public Set<KeyBinding> getKeyBindings() {
        return collectKeyBindings(null, null);
    }

    /**
     * Returns the set of key bindings mapped to the specified function tag.
     * @param tag the function tag
     * @return the set of KeyBindings
     */
    public Set<KeyBinding> getKeyBindingFor(FunctionTag tag) {
        return collectKeyBindings(null, tag);
    }

    Set<KeyBinding> collectKeyBindings(Set<KeyBinding> bindings, FunctionTag tag) {
        if (bindings == null) {
            bindings = new HashSet<>();
        }
        for (Map.Entry<Object, Object> en : map.entrySet()) {
            if (en.getKey() instanceof KeyBinding k) {
                if ((tag == null) || (tag == en.getValue())) {
                    bindings.add(k);
                }
            }
        }
        return bindings;
    }

    /**
     * This convenience method registers a copy of the behavior-specific mappings from one key binding to another.
     * The method does nothing if no behavior specific mapping can be found.
     * @param existing the existing key binding
     * @param newk the new key binding
     */
    public void duplicateMapping(KeyBinding existing, KeyBinding newk) {
        Object x = map.get(existing);
        if (x != null) {
            map.put(newk, x);
        }
    }

    final void install(InputMap parent) {
        // TODO for each event type: merge phlist
    }
    
    final void uninstall(InputMap parent) {
        // TODO for each event type: remove all with priority=SKIN_* (and move it to input map)
    }

    final Runnable getFunction(FunctionTag tag) {
        Object x = map.get(tag);
        return toRunnable(x);
    }

    abstract Runnable toRunnable(Object x);

    // TODO control arg and generics are not needed, but are needed for createStateless().
    // maybe have a base class and two final classes instead?
    public static <K extends Control> SkinInputMap<K, Runnable> createStateful(K control) {
        return new SkinInputMap<K, Runnable>() {
            @Override
            Runnable toRunnable(Object x) {
                if (x instanceof Runnable r) {
                    return r;
                }
                return null;
            }
        };
    }

    public static <K extends Control> SkinInputMap<K, Consumer<K>> createStateless(K control) {
        return new SkinInputMap<K, Consumer<K>>() {
            @Override
            Runnable toRunnable(Object x) {
                if (x instanceof Consumer f) {
                    return () -> {
                        f.accept(control);
                    };
                }
                return null;
            }
        };
    }

    void unbind(FunctionTag tag) {
        Iterator<Map.Entry<Object, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> en = it.next();
            if (tag == en.getValue()) {
                // the entry must be KeyBinding -> FunctionTag
                it.remove();
            }
        }
    }
    
    void handleKeyFunctionEnter() {
        Object x = map.get(ON_KEY_ENTER);
        Runnable r = toRunnable(x);
        if (r != null) {
            r.run();
        }
    }

    void handleKeyFunctionExit() {
        Object x = map.get(ON_KEY_EXIT);
        Runnable r = toRunnable(x);
        if (r != null) {
            r.run();
        }
    }
}
