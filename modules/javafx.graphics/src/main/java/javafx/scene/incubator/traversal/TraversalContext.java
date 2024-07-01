/*
 * Copyright (c) 2011, 2024, Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * The traversal context provides contextual information to implementations of {@link TraversalPolicy}, such that the
 * traversal algorithm can appropriately select the correct scenegraph element to traverse to. There is no expectation
 * that this interface be implemented by developers - the specific implementation details are handled by JavaFX, and the
 * relevant implementation is based to {@link TraversalPolicy} implementations.
 *
 * @see TraversalDirection
 * @see TraversalPolicy
 * @since 999 TODO
 */
public interface TraversalContext {
    /**
     * Returns all possible targets within the context.
     *
     * @return A List of all possible targets within the context
     */
    List<Node> getAllTargetNodes();

    /**
     * Returns layout bounds of the Node in the relevant (Sub)Scene. Note that these bounds are the most important for
     * traversal as they define the final position within the scene.
     *
     * @param node the Node
     * @return the layout bounds of the Node in the relevant (Sub)Scene
     */
    Bounds getSceneLayoutBounds(Node node);

    /**
     * The root for this context - traversal should be done only within the root.
     *
     * @return the root for this context
     */
    Parent getRoot();

    /**
     * Returns the first {@link Node} that is traversable from the given Parent node.
     *
     * @param parent the Parent
     * @return the first {@link Node} that is traversable from the given Parent node
     */
    Node selectFirstInParent(Parent parent);

    /**
     * Returns the last {@link Node} that is traversable from the given Parent node.
     *
     * @param parent the Parent
     * @return the last {@link Node} that is traversable from the given Parent node
     */
    Node selectLastInParent(Parent parent);

    /**
     * Returns the next suitable {@link Node} that is traversable from the given Parent node, in the direction
     * represented by the provided {@link TraversalDirection}, or null if there is no valid result.
     *
     * @param subTreeRoot this will be used as a root of the traversal. This will be a Node that is handled by the
     *        current TraversalEngine, but its content is not
     * @param from a descendant of the given root node, from which traversal should commence in the given direction
     * @param dir the direction of the traversal
     * @return the next suitable {@link Node} that is traversable, or null if there is no valid result
     */
    Node selectInSubtree(Parent subTreeRoot, Node from, TraversalDirection dir);
}
