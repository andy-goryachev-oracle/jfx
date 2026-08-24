/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
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
package test.jfx.incubator.scene.control.richtext;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import javafx.scene.Scene;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.sun.jfx.incubator.scene.control.richtext.CaretInfo;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.RichTextModel;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import jfx.incubator.scene.control.richtext.skin.RichTextAreaSkin;
import test.jfx.incubator.scene.util.StageLoader;
import test.jfx.incubator.scene.util.TUtil;

/**
 * Tests RichTextArea navigation logic using StubTextLayout.
 */
public class RichTextAreaNavigationTest {

    private RichTextArea control;
    private StageLoader stageLoader;

    @BeforeEach
    public void beforeEach() {
        TUtil.setUncaughtExceptionHandler();
        control = new RichTextArea();
        control.setSkin(new RichTextAreaSkin(control));
        stageLoader = new StageLoader(new Scene(control, 100, 100));
    }

    @AfterEach
    public void afterEach() {
        if (stageLoader != null) {
            stageLoader.dispose();
            stageLoader = null;
        }
        TUtil.removeUncaughtExceptionHandler();
    }

    // test the handling of the paragraph spacing JDK-8390913
    @Test
    public void testDown() {
        RichTextModel m = new RichTextModel();
        StyleAttributeMap a = StyleAttributeMap.builder()
            .setFontSize(20.0)
            .setBold(true)
            .setSpaceAbove(12.0)
            .setSpaceBelow(8.0)
            .build();
        control.setModel(m);
        control.appendText("This is the heading\nThis is some text.");
        control.applyStyle(TextPos.ofLeading(0, 0), control.getParagraphEnd(0), a);
        control.select(TextPos.ofLeading(0, 2));
        TextPos p1 = control.getCaretPosition();
        CaretInfo ci1 = RichTestUtil.getCaretInfo(control, p1);
        assertNotNull(ci1);

        control.moveDown();
        TextPos p2 = control.getCaretPosition();
        CaretInfo ci2 = RichTestUtil.getCaretInfo(control, p2);
        assertNotNull(ci2);

        assertEquals(1, p2.index());
        assertTrue(ci2.getMaxY() > ci1.getMaxY());
    }
}
