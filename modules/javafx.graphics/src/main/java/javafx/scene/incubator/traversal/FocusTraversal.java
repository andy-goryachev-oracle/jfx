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
import com.sun.javafx.scene.SceneHelper;
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
     * Traverse focus downward as a response to pressing a key.
     *
     * @param node the origin node
     * @return true if traversal was successful
     */
    public static boolean traverseDown(Node node) {
        return traverse(node, TraversalDirection.DOWN, TraversalMethod.KEY);
    }

    /**
     * Traverse focus left as a response to pressing a key.
     *
     * @param node the origin node
     * @return true if traversal was successful
     */
    public static boolean traverseLeft(Node node) {
        return traverse(node, TraversalDirection.LEFT, TraversalMethod.KEY);
    }

    /**
     * Traverse focus to the next focuseable Node as a response to pressing a key.
     *
     * @param node the origin node
     * @return true if traversal was successful
     */
    public static boolean traverseNext(Node node) {
        return traverse(node, TraversalDirection.NEXT, TraversalMethod.KEY);
    }

    /**
     * Traverse focus tothe next focuseable Node as a response to pressing a key.
     * This method does not traverse into the current parent.
     *
     * @param node the origin node
     * @return true if traversal was successful
     */
    public static boolean traverseNextInLine(Node node) {
        return traverse(node, TraversalDirection.NEXT_IN_LINE, TraversalMethod.KEY);
    }

    /**
     * Traverse focus to the previous focusable Node as a response to pressing a key.
     *
     * @param node the origin node
     * @return true if traversal was successful
     */
    public static boolean traversePrevious(Node node) {
        return traverse(node, TraversalDirection.PREVIOUS, TraversalMethod.KEY);
    }

    /**
     * Traverse focus right as a response to pressing a key.
     *
     * @param node the origin node
     * @return true if traversal was successful
     */
    public static boolean traverseRight(Node node) {
        return traverse(node, TraversalDirection.RIGHT, TraversalMethod.KEY);
    }

    /**
     * Traverse focus upward as a response to pressing a key.
     *
     * @param node the origin node
     * @return true if traversal was successful
     */
    public static boolean traverseUp(Node node) {
        return traverse(node, TraversalDirection.UP, TraversalMethod.KEY);
    }

    /**
     * Traverses focus to the adjacent node as specified by the direction.
     *
     * @param node the node to traverse focus from
     * @param direction the direction of traversal
     * @param method the method which initiated the traversal
     * @return true if traversal was successful
     */
    public static boolean traverse(Node node, TraversalDirection direction, TraversalMethod method) {
        if (node != null) {
            return SceneHelper.traverse(node, direction, method);
        }
        return false;
    }

    // TODO these two methods can be moved to Parent... or not

    // TODO or make Parent.traversalPolicyProperty public
//    public static void setTraversalPolicy(Parent parent, TraversalPolicy policy) {
//        // TODO
//    }
//    
//    public static TraversalPolicy getTraversalPolicy(Parent parent, TraversalPolicy policy) {
//        // TODO
//        return null;
//    }
    
    // TODO static focusOwnerProperty

    // TODO static focusedWindow/SceneProperty

    private FocusTraversal() {
    }
}
