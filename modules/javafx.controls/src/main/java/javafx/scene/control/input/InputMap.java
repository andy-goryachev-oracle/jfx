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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import com.sun.javafx.scene.control.input.HList;

/**
 * InputMap is a class that is set on a given {@link Control}. When the Node receives
 * an input event from the system, it passes this event in to the InputMap where
 * the InputMap can check all installed mappings to see if there is any
 * suitable mapping, and if so, fire the provided {@link EventHandler}.
 *
 * @since 999 TODO
 */
// TODO possibly create a base class for InputMap and SkinInputMap
public final class InputMap {
    private static final Object NULL = new Object();
    // KeyBinding -> FunctionTag
    // FunctionTag -> Runnable
    // EventType -> HList of listeners (with priority)
    private final HashMap<Object,Object> map = new HashMap<>();
    private SkinInputMap skinInputMap;

    /**
     * The constructor.
     */
    public InputMap() {
    }

    private void handleEvent(Event ev) {
        if (ev == null || ev.isConsumed()) {
            return;
        }

        EventType<?> t = ev.getEventType();
        HList handlers = getHandlers(t);
        if (handlers != null) {
            for (EventHandler h: handlers) {
                h.handle(ev);
                if (ev.isConsumed()) {
                    break;
                }
            }
        }
        // TODO skin
    }

    private void handleKeyEvent(Event ev) {
        if (ev == null || ev.isConsumed()) {
            return;
        }

        KeyBinding k = KeyBinding.from((KeyEvent)ev);
        Runnable f = getFunction(k);
        if (f != null) {
            handleKeyFunctionEnter();
            try {
                f.run();
                ev.consume();
            } finally {
                handleKeyFunctionExit();
            }
            return;
        }

        EventType<?> t = ev.getEventType();
        HList handlers = getHandlers(t);
        if (handlers != null) {
            handleKeyFunctionEnter();
            try {
                for (EventHandler h: handlers) {
                    h.handle(ev);
                    if (ev.isConsumed()) {
                        break;
                    }
                }
            } finally {
                handleKeyFunctionExit();
            }
        }
        
        // TODO skin
    }

    private void handleKeyFunctionEnter() {
        if (skinInputMap != null) {
            skinInputMap.handleKeyFunctionEnter();
        }
    }

    private void handleKeyFunctionExit() {
        if (skinInputMap != null) {
            skinInputMap.handleKeyFunctionExit();
        }
    }

//    /**
//     * Removes all the mappings set by the behavior.
//     * Behavior developers do not need to call this method directly, as it is being called in BehaviorBase.dispose().
//     *
//     * @param behavior
//     */
//    void unregister(BehaviorBase behavior) {
//        Objects.nonNull(behavior);
//
//        for (Entry en: map.values()) {
//            if (en.behavior == behavior) {
//                en.behavior = null;
//                en.behaviorValue = null;
//            }
//        }
//    }

//    <T extends Event> void addHandler(
//        BehaviorBase behavior,
//        EventType<T> type,
//        boolean consume,
//        boolean tail,
//        EventHandler<T> handler
//    ) {
//        if (consume) {
//            extendHandlers(behavior, type, tail, new EventHandler<T>() {
//                @Override
//                public void handle(T ev) {
//                    handler.handle(ev);
//                    ev.consume();
//                }
//            });
//        } else {
//            extendHandlers(behavior, type, tail, handler);
//        }
//    }

//    <T extends Event> void addHandler(
//        BehaviorBase behavior,
//        EventCriteria<T> criteria,
//        boolean consume,
//        boolean tail,
//        EventHandler<T> handler
//    ) {
//        EventType<T> type = criteria.getEventType();
//        extendHandlers(behavior, type, tail, new EventHandler<T>() {
//            @Override
//            public void handle(T ev) {
//                if (criteria.isEventAcceptable(ev)) {
//                    handler.handle(ev);
//                    if (consume) {
//                        ev.consume();
//                    }
//                }
//            }
//        });
//    }

//    private <T extends Event> void extendHandlers(
//        BehaviorBase behavior,
//        EventType<T> t,
//        boolean tail,
//        EventHandler<T> h
//    ) {
//        Objects.nonNull(behavior);
//        Entry en = addListenerIfNeeded(t);
//
//        HList handlers = HList.from(en.behaviorValue);
//        handlers.add(h, tail);
//        en.behavior = behavior;
//        en.behaviorValue = handlers;
//    }

    /**
     * Adds (or overrides) a user-specified function under the given function tag.
     * This function will override any function set by the behavior.
     * @param tag the function tag
     * @param function the function
     */
    public void registerFunction(FunctionTag tag, Runnable function) {
        Objects.requireNonNull(tag, "function tag must not be null");
        Objects.requireNonNull(function, "function must not be null");
        map.put(tag, function);
    }

    /**
     * Link a key binding to the specified function tag.
     * This method will override a mapping set by the behavior.
     *
     * @param k the key binding
     * @param tag the function tag
     */
    public void registerKey(KeyBinding k, FunctionTag tag) {
        Objects.requireNonNull(k, "KeyBinding must not be null");
        Objects.requireNonNull(tag, "function tag must not be null");
        map.put(k, tag);
    }

    /**
     * Maps a key binding to the specified function tag, for use by the behavior.
     * A null key binding will result in no change to this input map.
     * This method will not override a user mapping added by {@link #registerKey(KeyBinding,FunctionTag)}.
     *
     * @param behavior the owner
     * @param k the key binding, can be null TODO variant: KeyBinding.NA
     * @param tag the function tag
     */
//    void registerKey(BehaviorBase behavior, KeyBinding k, FunctionTag tag) {
//        if (k == null) {
//            return;
//        }
//        Objects.requireNonNull(behavior, "behavior must not be null");
//        Objects.requireNonNull(tag, "function tag must not be null");
//        addBinding(behavior, k, tag);
//    }

    /**
     * Maps a key binding to the specified function tag, as a part of the behavior.
     * This method will not override a user mapping added by {@link #registerKey(KeyBinding,FunctionTag)}.
     *
     * @param behavior the owner
     * @param code the key code to construct a {@link KeyBinding}
     * @param tag the function tag
     */
//    void registerKey(BehaviorBase behavior, KeyCode code, FunctionTag tag) {
//        registerKey(behavior, KeyBinding.of(code), tag);
//    }

    /**
     * Returns a {@code Runnable} mapped to the specified function tag, or null if no such mapping exists.
     *
     * @param tag the function tag
     * @return the function, or null
     */
    public Runnable getFunction(FunctionTag tag) {
        Object x = map.get(tag);
        if (x instanceof Runnable r) {
            return r;
        }
        if (skinInputMap != null) {
            return skinInputMap.getFunction(tag);
        }
        return null;
    }

    /**
     * Returns a default {@code Runnable} mapped to the specified function tag, or null if no such mapping exists.
     *
     * @implNote the return value might be a lambda, i.e. it will return a new instance each time this method is called.
     *
     * @param tag the function tag
     * @return the function, or null
     */
    public Runnable getDefaultFunction(FunctionTag tag) {
        if (skinInputMap != null) {
            return skinInputMap.getFunction(tag);
        }
        return null;
    }

    /**
     * Returns a {@code Runnable} mapped to the specified {@link KeyBinding},
     * or null if no such mapping exists.
     * <p>
     * @implNote
     * This method is a functional equivalent of calling {@link #getFunctionTag(KeyBinding)}
     * followed by {@link #getFunction(FunctionTag)} (if the tag is not null).
     *
     * @param k the key binding
     * @return the function, or null
     */
    public Runnable getFunction(KeyBinding k) {
        FunctionTag tag = getFunctionTag(k);
        if (tag != null) {
            return getFunction(tag);
        }
        return null;
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
        if (x instanceof FunctionTag tag) {
            return tag;
        }
        if (skinInputMap != null) {
            return skinInputMap.getFunctionTag(k);
        }
        return null;
    }

    /**
     * Returns a default {@code Runnable} mapped to the specified {@link KeyBinding},
     * or null if no such mapping exists.
     *
     * @param k the key binding
     * @return the function, or null
     */
    // TODO
//    public Runnable getDefaultFunction(KeyBinding k) {
//        if (skinInputMap != null) {
//            return skinInputMap.getFunction(k);
//        }
//        return null;
//    }

    private HList getHandlers(EventType<?> t) {
        Object x = map.get(t);
        if (x instanceof HList list) {
            return list;
        }
        return null;
    }

    /**
     * Unbinds the specified key binding.
     *
     * @param k the key binding
     */
    public void unbind(KeyBinding k) {
        map.put(k, NULL);
    }

    /**
     * Resets all key bindings set by user to the values set by the behavior, if any.
     */
    public void resetKeyBindings() {
        Iterator<Map.Entry<Object, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> me = it.next();
            if (me.getKey() instanceof KeyBinding) {
                it.remove();
            }
        }
    }

    /**
     * Restores the specified key binding to the value set by the behavior, if any.
     *
     * @param k the key binding
     */
    public void restoreDefaultKeyBinding(KeyBinding k) {
        Object x = map.get(k);
        if (x != null) {
            map.remove(k);
        }
    }

    /**
     * Restores the specified function tag to the value set by the behavior, if any.
     *
     * @param tag the function tag
     */
    public void restoreDefaultFunction(FunctionTag tag) {
        Objects.requireNonNull(tag, "function tag must not be null");
        map.remove(tag);
    }

    /**
     * Collects all mapped key bindings (set either by the user or the behavior).
     *
     * @return a Set of key bindings
     */
    public Set<KeyBinding> getKeyBindings() {
        return map.keySet().stream().
            filter((k) -> (k instanceof KeyBinding)).
            map((x) -> (KeyBinding)x).
            collect(Collectors.toSet());
    }

    /**
     * Returns the set of key bindings mapped to the specified function tag.
     * @param tag the function tag
     * @return the set of KeyBindings
     */
    public Set<KeyBinding> getKeyBindingFor(FunctionTag tag) {
        HashSet<KeyBinding> set = new HashSet<>();
        // TODO skin first, then user
        for (Map.Entry<Object, Object> k : map.entrySet()) {
            if (k.getKey() instanceof KeyBinding kb) {
                Object x = k.getValue();
                if (x == tag) {
                    set.add(kb);
                }
            }
        }
        return set;
    }

    /**
     * Removes all the key bindings mapped to the specified function tag, either by the application or by the skin.
     * This is an irreversible operation.
     * @param tag the function tag
     */
    public void unbind(FunctionTag tag) {
        if (skinInputMap != null) {
            skinInputMap.unbind(tag);
        }
        Iterator<Map.Entry<Object, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> en = it.next();
            if (tag == en.getValue()) {
                // the entry must be KeyBinding -> FunctionTag
                it.remove();
            }
        }
    }

    // TODO hide behind a helper
    public void setSkinInputMap(Skin<?> skin, SkinInputMap m) {
        if (skinInputMap != null) {
            skinInputMap.uninstall(skin);
        }
        // TODO add handlers
        skinInputMap = m;
    }
}
