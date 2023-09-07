/*
 * Copyright (c) 2016, 2023, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.javafx.scene.control;

import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.input.BehaviorBase2;
import com.sun.javafx.scene.layout.RegionHelper;
import com.sun.javafx.util.Utils;

/*
 * Used to access internal methods of Control.
 */
public class ControlHelper extends RegionHelper {

    /** Provides access to private methods in Control */
    public interface ControlAccessor {
        void doProcessCSS(Node node);
        StringProperty skinClassNameProperty(Control control);
        BehaviorBase2 getBehavior(Control c);
        void setBehavior(Control c, BehaviorBase2 b);
    }

    private static final ControlHelper theInstance;
    private static ControlAccessor controlAccessor;

    static {
        theInstance = new ControlHelper();
        Utils.forceInit(Control.class);
    }

    private static ControlHelper getInstance() {
        return theInstance;
    }

    public static void initHelper(Control control) {
        setHelper(control, getInstance());
    }

    public static void superProcessCSS(Node node) {
        ((ControlHelper) getHelper(node)).superProcessCSSImpl(node);
    }

    public static StringProperty skinClassNameProperty(Control control) {
        return controlAccessor.skinClassNameProperty(control);
    }

    void superProcessCSSImpl(Node node) {
        super.processCSSImpl(node);
    }

    @Override
    protected void processCSSImpl(Node node) {
        controlAccessor.doProcessCSS(node);
    }

    public static void setControlAccessor(final ControlAccessor newAccessor) {
        if (controlAccessor != null) {
            throw new IllegalStateException();
        }
        controlAccessor = newAccessor;
    }

    public static BehaviorBase2 getBehavior(Control c) {
        return controlAccessor.getBehavior(c);
    }

    public static void setBehavior(Control c, BehaviorBase2 b) {
        controlAccessor.setBehavior(c, b);
    }
}
