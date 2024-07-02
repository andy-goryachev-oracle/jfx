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
package com.sun.javafx.scene.traversal;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.incubator.traversal.TraversalPolicy;
import com.sun.javafx.application.PlatformImpl;

public final class TraversalUtils {
    /**
     * This is the default traversal policy for the running platform.
     * It's the traversal policy that's used in TopMostTraversalEngine
     */
    public static final TraversalPolicy DEFAULT_TRAVERSAL_ALGORITHM = createDefaultTraversalAlgorithm();

    private static final Bounds INITIAL_BOUNDS = new BoundingBox(0, 0, 1, 1);

    private TraversalUtils() {
    }

    public static TraversalPolicy createDefaultTraversalAlgorithm() {
        return PlatformImpl.isContextual2DNavigation() ? new Heuristic2D() : new ContainerTabOrder();
    }

    /**
     * Gets the appropriate bounds for the given node, transformed into
     * the scene's or the specified node's coordinates.
     * @return bounds of node in {@code forParent} coordinates or scene coordinates if {@code forParent} is null
     */
    public static Bounds getLayoutBounds(Node n, Parent forParent) {
        final Bounds bounds;
        if (n != null) {
            if (forParent == null) {
                bounds = n.localToScene(n.getLayoutBounds());
            } else {
                bounds = forParent.sceneToLocal(n.localToScene(n.getLayoutBounds()));
            }
        } else {
            bounds = INITIAL_BOUNDS;
        }
        return bounds;
    }
}