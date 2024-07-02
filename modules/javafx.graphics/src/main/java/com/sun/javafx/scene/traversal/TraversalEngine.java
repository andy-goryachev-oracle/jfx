/*
 * Copyright (c) 2010, 2016, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javafx.scene.traversal;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.incubator.traversal.TraversalContext;
import javafx.scene.incubator.traversal.TraversalDirection;
import javafx.scene.incubator.traversal.TraversalPolicy;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.scene.NodeHelper;

/**
 * This is abstract class for a traversal engine. There are 2 types : {@link com.sun.javafx.scene.traversal.ParentTraversalEngine}
 * to be used in {@link Parent#setTraversalEngine(ParentTraversalEngine)} to override default behavior
 * and {@link com.sun.javafx.scene.traversal.TopMostTraversalEngine} that is the default traversal engine for scene and subscene.
 *
 * Every engine is basically a wrapper of an algorithm + some specific parent (or scene/subscene), which define engine's root.
 */
public abstract class TraversalEngine {
    /**
     * This is the default algorithm for the running platform. It's the algorithm that's used in TopMostTraversalEngine
     */
    protected static final TraversalPolicy DEFAULT_POLICY = PlatformImpl.isContextual2DNavigation() ? new Heuristic2D() : new ContainerTabOrder();

    /** This is the context used in calls to this engine's algorithm */
    private final TraversalContext context = new EngineContext();
    /** This is a special context that's used when invoking select "callbacks" to default algorithm in other contexts */
    private final TempEngineContext tempEngineContext = new TempEngineContext();
    protected final TraversalPolicy policy;
    
    /**
     * The root of this engine's context. This is the node that is the root of the tree that is traversed by this engine.
     * @return This engine's root
     */
    protected abstract Parent getRoot();

    /**
     * Creates engine with the specified algorithm
     * @param p
     */
    protected TraversalEngine(TraversalPolicy p) {
        this.policy = p;
    }

    /**
     * Creates engine with no algorithm. This makes all the select* calls invalid.
     * @see #canTraverse()
     */
    protected TraversalEngine() {
        this.policy = null;
    }

    /**
     * Returns the node that is in the direction {@code dir} starting from the Node {@code from} using the engine's algorithm.
     * Null means there is no Node in that direction
     * @param from the node to start traversal from
     * @param dir the direction of traversal
     * @return the subsequent node in the specified direction or null if none
     */
    public final Node select(Node from, TraversalDirection dir) {
        return policy.select(from, dir, context);
    }

    /**
     * Returns the first node in this engine's context (scene/parent) using the engine's algorithm.
     * This can be null only if there are no traversable nodes
     * @return The first node or null if none exists
     */
    public final Node selectFirst() {
        return policy.selectFirst(context);
    }

    /**
     * Returns the last node in this engine's context (scene/parent) using the engine's algorithm.
     * This can be null only if there are no traversable nodes
     * @return The last node or null if none exists
     */
    public final Node selectLast() {
        return policy.selectLast(context);
    }

    /**
     * Returns true only if there's specified algorithm for this engine. Otherwise, this engine cannot be used for traversal.
     * The engine might be still useful however, e.g. for listening on traversal changes.
     * @return
     */
    public final boolean canTraverse() {
        return policy != null;
    }

    // This is the engine context passed algorithm on select calls
    private final class EngineContext extends BaseEngineContext {
        @Override
        public Parent getRoot() {
            return TraversalEngine.this.getRoot();
        }
    }

    // This is the engine context passed to algorithm on select callbacks from other contexts.
    // It can change the root to the node defined in "selectFirstInParent", "selectLastInParent" or
    // "selectInSubtree" methods
    private final class TempEngineContext extends BaseEngineContext {
        private Parent root;

        @Override
        public Parent getRoot() {
            return root;
        }

        public void setRoot(Parent root) {
            this.root = root;
        }
    }

    /**
     * The base class for all engine contexts
     */
    private abstract class BaseEngineContext implements TraversalContext {
        /**
         * Returns all traversable nodes in the context's (engine's) root
         */
        @Override
        public List<Node> getAllTargetNodes() {
            final List<Node> targetNodes = new ArrayList<>();
            addFocusableChildrenToList(targetNodes, getRoot());
            return targetNodes;
        }

        @Override
        public Bounds getSceneLayoutBounds(Node n) {
            return TraversalUtils.getLayoutBounds(n, null);
        }

        private void addFocusableChildrenToList(List<Node> list, Parent parent) {
            List<Node> parentsNodes = parent.getChildrenUnmodifiable();
            for (Node n : parentsNodes) {
                if (n.isFocusTraversable() && !n.isFocused() && NodeHelper.isTreeVisible(n) && !n.isDisabled()) {
                    list.add(n);
                }
                if (n instanceof Parent) {
                    addFocusableChildrenToList(list, (Parent)n);
                }
            }
        }

        // All of the methods below are callbacks from traversal context to the default algorithm.
        // They can be used to obtain "default" result for the specified subtree.
        // This is useful when there is some algorithm that overrides behavior for a Parent but parent's children
        // should be again traversed by default algorithm.
        @Override
        public Node selectFirstInParent(Parent parent) {
            tempEngineContext.setRoot(parent);
            return DEFAULT_POLICY.selectFirst(tempEngineContext);
        }

        @Override
        public Node selectLastInParent(Parent parent) {
            tempEngineContext.setRoot(parent);
            return DEFAULT_POLICY.selectLast(tempEngineContext);
        }

        @Override
        public Node selectInSubtree(Parent subTreeRoot, Node from, TraversalDirection dir) {
            tempEngineContext.setRoot(subTreeRoot);
            return DEFAULT_POLICY.select(from, dir, tempEngineContext);
        }
    }
}
