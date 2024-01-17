/*
 * Copyright (c) 2023, 2024, Oracle and/or its affiliates. All rights reserved.
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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.input.KeyEvent;
import com.sun.javafx.scene.control.input.EventHandlerPriority;
import com.sun.javafx.scene.control.input.PHList;

/**
 * InputMap is a class that is set on a given {@link Control}. When the Node receives
 * an input event from the system, it passes this event in to the InputMap where
 * the InputMap can check all installed mappings to see if there is any
 * suitable mapping, and if so, fire the provided {@link EventHandler}.
 *
 * @since 999 TODO
 */
public final class InputMap {
    private static final Object NULL = new Object();
    private final Control control;
    // KeyBinding -> FunctionTag
    // FunctionTag -> Runnable
    // EventType -> PHList of listeners
    private final HashMap<Object,Object> map = new HashMap<>();
    private SkinInputMap skinInputMap;
    private final EventHandler<Event> eventHandler = this::handleEvent;

    /**
     * The constructor.
     */
    public InputMap(Control control) {
        this.control = control;
    }

    /**
     * Adds an event handler for the specified event type, at the control level.
     * This mapping always consumes the matching event.
     *
     * @param <T> the actual event type
     * @param type the event type
     * @param handler the event handler
     */
    public <T extends Event> void addHandler(EventType<T> type, EventHandler<T> handler) {
        extendHandler(type, handler, EventHandlerPriority.USER_HIGH);
    }

    /**
     * Adds an event handler for the specified event type, at the control level.
     * This event handler will get invoked after all handlers added via map() methods.
     * This mapping always consumes the matching event.
     *
     * @param <T> the actual event type
     * @param type the event type
     * @param handler the event handler
     */
    public <T extends Event> void addHandlerLast(EventType<T> type, EventHandler<T> handler) {
        extendHandler(type, handler, EventHandlerPriority.USER_LOW);
    }

    /**
     * Removes the specified handler.
     *
     * @param <T> the event class
     * @param type the event type
     * @param handler the handler to remove
     */
    public <T extends Event> void removeHandler(EventType<T> type, EventHandler<T> handler) {
        Object x = map.get(type);
        if (x instanceof PHList hs) {
            hs.remove(handler);
 
            if (hs.isEmpty()) {
                // remove listener (can only be eventHandler)
                control.removeEventHandler(type, eventHandler);
            }
        }
    }

    private <T extends Event> void extendHandler(EventType<T> t, EventHandler<T> handler, EventHandlerPriority pri) {
        Object x = map.get(t);
        PHList hs;
        if(x instanceof PHList h) {
            hs = h;
        } else {
            // first entry for this event type
            hs = new PHList();
            map.put(t, hs);
            // add event listener to the control
            switch(pri) {
            case SKIN_KB:
            case USER_KB:
                control.addEventHandler(t, this::handleKeyBindingEvent);
                break;
            default:
                control.addEventHandler(t, eventHandler);
                break;
            }
        }
        hs.add(handler, pri);
    }

    private void handleEvent(Event ev) {
        if (ev == null || ev.isConsumed()) {
            return;
        }

        EventType<?> t = ev.getEventType();
        Object x = map.get(t);
        if (x instanceof PHList hs) {
            for (EventHandler h : hs) {
                h.handle(ev);
                if (ev.isConsumed()) {
                    break;
                }
            }
        }
    }

    private void handleKeyBindingEvent(Event ev) {
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
//
//        EventType<?> t = ev.getEventType();
//        PHList handlers = handlers(t, false);
//        if (handlers != null) {
//            handleKeyFunctionEnter();
//            try {
//                for (EventHandler h: handlers) {
//                    h.handle(ev);
//                    if (ev.isConsumed()) {
//                        break;
//                    }
//                }
//            } finally {
//                handleKeyFunctionExit();
//            }
//        }
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

    /**
     * Adds (or overrides) a user-specified function under the given function tag.
     * This function will override any function set by the behavior.
     * @param tag the function tag
     * @param function the function
     */
    // TODO or FunctionHandler<C> ? that accepts a Control?
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

        extendHandler(KeyEvent.ANY, null, EventHandlerPriority.USER_KB);
    }

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
            return skinInputMap.getFunction(control, tag);
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
            return skinInputMap.getFunction(control, tag);
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
     * @return the set of key bindings
     */
    public Set<KeyBinding> getKeyBindings() {
        return collectKeyBindings(null);
    }

    /**
     * Returns the set of key bindings mapped to the specified function tag.
     * @param tag the function tag
     * @return the set of KeyBindings
     */
    public Set<KeyBinding> getKeyBindingFor(FunctionTag tag) {
        return collectKeyBindings(tag);
    }

    private Set<KeyBinding> collectKeyBindings(FunctionTag tag) {
        HashSet<KeyBinding> bindings = new HashSet<>();
        for (Map.Entry<Object, Object> en : map.entrySet()) {
            if (en.getKey() instanceof KeyBinding k) {
                if ((tag == null) || (tag == en.getValue())) {
                    bindings.add(k);
                }
            }
        }

        if (skinInputMap != null) {
            skinInputMap.collectKeyBindings(bindings, tag);
        }
        return bindings;
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

    // TODO hide behind a helper (if the caller is moved to some base class)
    // or keep it public and call in every leaf skin class install().
    public void setSkinInputMap(SkinInputMap m) {
        if (skinInputMap != null) {
            // uninstall all handlers with SKIN_* priority
            Iterator<Map.Entry<Object, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Object, Object> en = it.next();
                if (en.getKey() instanceof Event ev) {
                    PHList hs = (PHList)en.getValue();
                    if (hs.removeSkinHandlers()) {
                        it.remove();
                        // TODO remove listener!
                    }
                }
            }
            
            // TODO remove key bindings listener, if present
        }

        skinInputMap = m;

        // install skin handlers with their priority
        skinInputMap.forEach((type, pri, h) -> {
            extendHandler(type, h, pri);
        });
        
        // TODO add key bindings listener, if present
    }
}
