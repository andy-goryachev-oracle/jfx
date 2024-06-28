/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
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
package javafx.scene.incubator.traversal;

import javafx.scene.Node;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.TraversalMethod;

/**
 * To be moved to an incubating module javafx.incubator.scene.traversal.
 *
 * Provides a centralized facility to control the focus traversal in the JavaFX application.
 *
 * @since 999 TODO
 */
public final class FocusTraversal {
    /**
     * Traverse focus to the next Node as a response to pressing a key.
     *
     * @param node the origin node
     * @return true if traversal was successful
     */
    public static boolean traverseNext(Node node) {
        return traverse(node, TraversalDirection.NEXT, true);
    }

    /**
     * Traverses focus to the adjacent node as specified by the direction.
     *
     * @param node the node to traverse focus from
     * @param direction the direction of traversal
     * @param byKeyboard true if traversal was initiated by pressing a key, false if traversal
     *        was initiated programmatically or by clicking
     * @return true if traversal was successful
     */
    public static boolean traverse(Node node, TraversalDirection direction, boolean byKeyboard) {
        if(node != null) {
            Direction dir = translateDirection(direction);
            TraversalMethod m = byKeyboard ? TraversalMethod.KEY : TraversalMethod.DEFAULT;
            return NodeHelper.traverse(node, dir, m);
        }
        return false;
    }

    // will become unnecessary once Direction is replaced with TraversalDirection
    private static Direction translateDirection(TraversalDirection direction) {
        switch(direction) {
        case DOWN:
            return Direction.DOWN;
        case LEFT:
            return Direction.LEFT;
        case NEXT:
            return Direction.NEXT;
        case NEXT_IN_LINE:
            return Direction.NEXT_IN_LINE;
        case PREVIOUS:
            return Direction.PREVIOUS;
        case RIGHT:
            return Direction.RIGHT;
        default:
            throw new Error("?" + direction);
        }
    }

    private FocusTraversal() {
    }
}
