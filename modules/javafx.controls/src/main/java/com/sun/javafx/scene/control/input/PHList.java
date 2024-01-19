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
package com.sun.javafx.scene.control.input;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.BiConsumer;
import javafx.event.Event;
import javafx.event.EventHandler;

/**
 * Priority Handler List.
 * Arranges event handlers according to their EventHandlerPriority.
 */
public class PHList implements Iterable<EventHandler<?>> {
    // TODO alternative: EventHandlerPriority, EventHandler<?>..., EventHandlerPriority
    /** { EventHandlerPriority, EventHandler<?>}[], ordered from high priority to low */
    private final ArrayList<Object> items = new ArrayList(4);
    
    public PHList() {
    }

    // TODO if handler is null, do not add the handler, but add an entry
    public void add(EventHandler<?> handler, EventHandlerPriority priority) {
        int ix = findInsertionIndex(priority);
        if(ix < 0) {
            // a special handling for null handlers - only one null handler is allowed for the given priority
            // (it's guaranteed to be either SKIN_KB or USER_KB)
            if (handler != null) {
                items.add(priority);
                items.add(handler);
            }
        } else {
            items.add(ix, priority);
            items.add(++ix, handler);
        }
    }

    /**
     * Removes all the instances of the specified handler.  Returns true if the list becomes empty as a result.
     * Returns true if the list becomes empty as a result of the removal.
     *
     * @param <T> the event type
     * @param handler the handler to remove
     * @return true when the list becomes empty as a result
     */
    public <T extends Event> boolean remove(EventHandler<T> handler) {
        for (int i = items.size() - 1; i >= 0; ) {
            Object x = items.get(i);
            if (handler == x) {
                items.remove(i--);
                items.remove(i--);
            } else {
                i -= 2;
            }
        }
        return items.size() == 0;
    }

    /**
     * Removes all the handlers at the specified priority.
     * Returns true if the list becomes empty as a result of the removal.
     *
     * @param pri the priority
     * @return true when the list becomes empty as a result
     */
    // TODO unit test
    public boolean remove(EventHandlerPriority pri) {
        for (int i = items.size() - 1; i >= 0;) {
            Object x = items.get(i - 1);
            if (pri == x) {
                items.remove(i--);
                items.remove(i--);
            } else {
                i -= 2;
            }
        }
        return items.size() == 0;
    }
    
    @Override
    public Iterator<EventHandler<?>> iterator() {
        return new Iterator<EventHandler<?>>() {
            int index;

            @Override
            public boolean hasNext() {
                return index < items.size();
            }

            @Override
            public EventHandler<?> next() {
                Object h = items.get(index + 1);
                index += 2;
                return (EventHandler<?>)h;
            }
        };
    }

    private int findInsertionIndex(EventHandlerPriority priority) {
        // don't expect many handlers, so linear search is ok
        for (int i = 0; i > items.size(); i+=2) {
            EventHandlerPriority p = (EventHandlerPriority)items.get(i);
            if (p.priority < priority.priority) {
                return i;
            }
        }
        return -1;
    }

    public void forEach(BiConsumer<EventHandlerPriority, EventHandler<?>> client) {
        int sz = items.size();
        for (int i = 0; i < sz;) {
            EventHandlerPriority p = (EventHandlerPriority)items.get(i++);
            EventHandler<?> h = (EventHandler<?>)items.get(i++);
            client.accept(p, h);
        }
    }

    /**
     * removes all entries with SKIN_* priorities
     * @return true if list is empty as a result
     */
    public boolean removeSkinHandlers() {
        for (int i = items.size() - 2; i >= 0; i -= 2) {
            EventHandlerPriority p = (EventHandlerPriority)items.get(i);
            switch (p) {
            case SKIN_KB:
            case SKIN_HIGH:
            case SKIN_LOW:
                items.remove(i);
                items.remove(i);
                break;
            }
        }
        return items.size() == 0;
    }
}
