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

package javafx.scene.incubator.traversal;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import com.sun.javafx.scene.NodeHelper;
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
 *
 * <p>This ensures that the next direction will traverse the same nodes as previous (in the opposite order).</p>
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
     * Returns all possible targets within the traversal root.
     *
     * @param root the traversal root
     * @return the List of all possible targets within the traversal root
     */
    // TODO move to utils? make static?
    protected static final List<Node> getAllTargetNodes(Parent root) {
        final List<Node> targetNodes = new ArrayList<>();
        addFocusableChildrenToList(targetNodes, root);
        return targetNodes;
    }

    /**
     * Returns layout bounds of the Node in the relevant (Sub)Scene. Note that these bounds are the most important for
     * traversal as they define the final position within the scene.
     *
     * @param node the Node
     * @return the layout bounds of the Node in the relevant (Sub)Scene
     */
    // TODO can be moved to the only caller? make static?
    protected static final Bounds getSceneLayoutBounds(Node node) {
        return TraversalUtils.getLayoutBounds(node, null);
    }

    private static final void addFocusableChildrenToList(List<Node> list, Parent parent) {
        List<Node> parentsNodes = parent.getChildrenUnmodifiable();
        for (Node n : parentsNodes) {
            if (n.isFocusTraversable() && !n.isFocused() && NodeHelper.isTreeVisible(n) && !n.isDisabled()) {
                list.add(n);
            }
            if (n instanceof Parent p) {
                addFocusableChildrenToList(list, p);
            }
        }
    }

    // TODO perhaps the next 3 methods can be replaced by calls to TraversalPolicy.getDefault(). ...
    /**
     * Returns the first {@link Node} that is traversable from the given Parent node.
     *
     * @param parent the Parent
     * @return the first {@link Node} that is traversable from the given Parent node
     */
    public final Node selectFirstInParent(Parent parent) {
        return getDefault().selectFirst(parent);
    }

    /**
     * Returns the last {@link Node} that is traversable from the given Parent node.
     *
     * @param parent the Parent
     * @return the last {@link Node} that is traversable from the given Parent node
     */
    public final Node selectLastInParent(Parent parent) {
        return getDefault().selectLast(parent);
    }

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
    public final Node selectInSubtree(Parent subTreeRoot, Node from, TraversalDirection dir) {
        return getDefault().select(subTreeRoot, from, dir);
    }

    /**
     * Returns the platform's default traversal policy singleton.
     *
     * @return the default traversal policy
     */
    public static TraversalPolicy getDefault() {
        return TraversalUtils.DEFAULT_POLICY;
    }

    /**
     * Determines whether the root is traversable.
     * @param root the root
     * @return true if the root is traversable
     */
    public boolean isParentTraversable(Parent root) {
        return root.isFocusTraversable();
    }
}
