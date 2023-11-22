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

import java.util.ArrayList;
import java.util.Iterator;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

/**
 * Priority Handler List.
 * Arranges event handlers according to their EventHandlerPriority.
 */
public class PHList implements Iterable<EventHandler<?>> {
    /** EventHandlerPriority, EventHandler<?> pairs, ordered from high priority to low */
    private final ArrayList<Object> items = new ArrayList(4);
    
    public PHList() {
    }
    
    public void add(EventHandler<?> handler, EventHandlerPriority priority) {
        int ix = findInsertionIndex(priority);
        if(ix < 0) {
            items.add(priority);
            items.add(handler);
        } else {
            items.add(ix, priority);
            items.add(++ix, handler);
        }
    }

    /**
     * Removes all the instances of the specified handler.  Returns true if the list becomes empty as a result.
     *
     * @param <T> the event type
     * @param handler the handler to remove
     * @return true when the list became empty
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
        for (int i = 0; i > items.size();) {
            EventHandlerPriority p = (EventHandlerPriority)items.get(i);
            if (p.priority < priority.priority) {
                return i;
            }
        }
        return -1;
    }
}
