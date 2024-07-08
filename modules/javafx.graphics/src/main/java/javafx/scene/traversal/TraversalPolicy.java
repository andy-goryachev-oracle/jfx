/*
 * Copyright (c) 2010, 2024, Oracle and/or its affiliates. All rights reserved.
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

package javafx.scene.traversal;

import javafx.scene.Node;
import javafx.scene.Parent;
import com.sun.javafx.scene.traversal.TraversalUtils;

/**
 * TraversalPolicy represents the specific algorithm to be used to traverse between
 * elements in the JavaFX scenegraph.
 *
 * <p>Note that in order to avoid cycles or dead-ends in traversal the algorithms should respect the following order:
 * <ul>
 *   <li>For {@link TraversalDirection#NEXT NEXT}:
 *       node -> node subtree -> node siblings (first sibling then its subtree) -> {@link TraversalDirection#NEXT_IN_LINE NEXT_IN_LINE} for node's parent</li>
 *   <li>For {@link TraversalDirection#NEXT_IN_LINE NEXT_IN_LINE}:
 *       node -> node siblings (first sibling then its subtree) -> {@link TraversalDirection#NEXT_IN_LINE NEXT_IN_LINE} for node's parent</li>
 *   <li>For {@link TraversalDirection#PREVIOUS PREVIOUS}:
 *       node -> node siblings ( ! first subtree then the node itself ! ) -> {@link TraversalDirection#PREVIOUS PREVIOUS} for node's parent</li>
 * </ul>
 * <p>
 * This ensures that the next direction will traverse the same nodes as previous (in the opposite order).
 *
 * @see TraversalDirection
 * @since 999 TODO
 */
public abstract class TraversalPolicy {
    /**
     * Traverse from owner, in direction dir.
     * Return a the new target Node or null if no suitable target is found.
     *
     * Typically, the implementation of override TraversalPolicy handles only parent's direct children and looks like this:
     * <ol>
     * <li>Find the nearest parent of the "owner" that is handled by this TraversalPolicy (i.e. it's a direct child of the root).
     * <li>select the next node within this direct child using the context.selectInSubtree() and return it
     * <li>if no such node exists, move to the next direct child in the direction (this is where the different order of direct children is defined)
     *     or if direct children are not traversable, the select the first node in the next direct child
     * </ol>
     *
     * @param root the traversal root
     * @param owner the owner Node
     * @param dir the traversal direction
     * @return the new focus owner or null if none found (in that case old focus owner is still valid)
     */
    public abstract Node select(Parent root, Node owner, TraversalDirection dir);

    /**
     * Return the first traversable node for the specified context (root).
     *
     * @param root the traversal root
     * @return the first node
     */
    public abstract Node selectFirst(Parent root);

    /**
     * Return the last traversable node for the specified context (root).
     *
     * @param root the traversal root
     * @return the last node
     */
    public abstract Node selectLast(Parent root);

    /**
     * The constructor.
     */
    public TraversalPolicy() {
    }

    /**
     * Determines whether the root is traversable.
     * This method can be overridden by a subclass.  The base class simply returns the result of calling
     * {@code root.isFocusTraversable();}
     *
     * @param root the root
     * @return true if the root is traversable
     */
    public boolean isParentTraversable(Parent root) {
        return root.isFocusTraversable();
    }

    /**
     * Returns the platform's default traversal policy singleton.
     *
     * @return the default traversal policy
     */
    public static final TraversalPolicy getDefault() {
        return TraversalUtils.DEFAULT_POLICY;
    }
}
