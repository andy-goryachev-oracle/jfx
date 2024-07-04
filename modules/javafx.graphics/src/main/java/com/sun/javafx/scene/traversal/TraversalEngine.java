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

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.incubator.traversal.TraversalDirection;
import javafx.scene.incubator.traversal.TraversalPolicy;

/**
 * This is abstract class for a traversal engine. There are 2 types : {@link com.sun.javafx.scene.traversal.ParentTraversalEngine}
 * to be used in {@link Parent#setTraversalEngine(ParentTraversalEngine)} to override default behavior
 * and {@link com.sun.javafx.scene.traversal.TopMostTraversalEngine} that is the default traversal engine for scene and subscene.
 *
 * Every engine is basically a wrapper of an algorithm + some specific parent (or scene/subscene), which define engine's root.
 */
@Deprecated // FIX remove
public abstract class TraversalEngine {
    protected final TraversalPolicy policy;
    
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
    public final Node select(Parent root, Node from, TraversalDirection dir) {
        return policy.select(root, from, dir);
    }

    /**
     * Returns the first node in this engine's context (scene/parent) using the engine's algorithm.
     * This can be null only if there are no traversable nodes
     * @return The first node or null if none exists
     */
    public final Node selectFirst(Parent root) {
        return policy.selectFirst(root);
    }

    /**
     * Returns the last node in this engine's context (scene/parent) using the engine's algorithm.
     * This can be null only if there are no traversable nodes
     * @return The last node or null if none exists
     */
    public final Node selectLast(Parent root) {
        return policy.selectLast(root);
    }

    /**
     * Returns true only if there's specified algorithm for this engine. Otherwise, this engine cannot be used for traversal.
     * The engine might be still useful however, e.g. for listening on traversal changes.
     * @return
     */
    public final boolean canTraverse() {
        return policy != null;
    }
}
