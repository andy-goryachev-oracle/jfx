/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.tools.demo.editor;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.incubator.scene.control.rich.RichTextArea;
import javafx.incubator.scene.control.rich.TextPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.oracle.tools.demo.rich.FX;
import com.oracle.tools.demo.rich.RichTextAreaWindow;

/**
 * Rich Editor Demo window
 */
public class RichEditorDemoWindow extends Stage {
    public final RichEditorDemoPane demoPane;
    public final Label status;
    
    public RichEditorDemoWindow() {
        demoPane = new RichEditorDemoPane();
        
        MenuBar mb = new MenuBar();
        // file
        FX.menu(mb, "File");
        //FX.separator(mb);
        FX.item(mb, "Quit", () -> Platform.exit());
        // edit
        FX.menu(mb, "Edit");
        // TODO undo/redo
        // TODO bold/etc or Format?
        // view
        FX.menu(mb, "View");
        // line spacing
        // help
        FX.menu(mb, "Help");
        
        status = new Label();
        status.setPadding(new Insets(2, 10, 2, 10));
        
        BorderPane bp = new BorderPane();
        bp.setTop(mb);
        bp.setCenter(demoPane);
        bp.setBottom(status);
        
        Scene scene = new Scene(bp);
        scene.getStylesheets().addAll(
            RichTextAreaWindow.class.getResource("RichTextArea-Modena.css").toExternalForm()
        );

        setScene(scene);
        setTitle("Rich Text Editor Demo  JFX:" + System.getProperty("javafx.runtime.version") + "  JDK:" + System.getProperty("java.version"));
        setWidth(1200);
        setHeight(600);

        demoPane.control.caretPositionProperty().addListener((x) -> updateStatus());
    }

    protected void updateStatus() {
        RichTextArea t = demoPane.control;
        TextPos p = t.getCaretPosition();

        StringBuilder sb = new StringBuilder();

        if (p != null) {
            sb.append(" L: ").append(p.index() + 1);
            sb.append(" C: ").append(p.offset() + 1);
        }

        status.setText(sb.toString());
    }
}
