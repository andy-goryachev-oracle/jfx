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
package com.oracle.test.manual.util;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Future;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.oracle.test.manual.text.EmojiTest;

public class TestRunnerApp extends Application {

    private static final String javaExecutablePath = initJavaExecutablePath();
    private TableView<DataRow> table;
    private TextArea log;
    
    public static void main(String args[]) throws Exception {
        Application.launch(TestRunnerApp.class, args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        {
            TableColumn<DataRow, String> c = new TableColumn<>("Name");
            c.setResizable(true);
            c.setCellValueFactory(data -> data.getValue().name);
            c.setPrefWidth(300);
            table.getColumns().add(c);
        }
        {
            TableColumn<DataRow, String> c = new TableColumn<>("Status");
            c.setResizable(true);
            c.setCellValueFactory(data -> data.getValue().status);
            c.setPrefWidth(50);
            table.getColumns().add(c);
        }
        {
            TableColumn<DataRow, String> c = new TableColumn<>("Last Run");
            c.setResizable(true);
            c.setCellValueFactory(data -> data.getValue().lastRun);
            c.setPrefWidth(70);
            table.getColumns().add(c);
        }
        
        Button runButton = new Button("▶ Run");
        runButton.disableProperty().bind(Bindings.createBooleanBinding(
            () -> {
                return table.getSelectionModel().getSelectedIndices().size() != 1;
            },
            table.getSelectionModel().selectedItemProperty()
        ));
        runButton.setOnAction((_) -> {
            runTest();
        });

        ToolBar tb = new ToolBar();
        tb.getItems().addAll(
            runButton
        );

        log = new TextArea();
        log.setEditable(false);

        MenuBar mb = new MenuBar();
        Menu m;
        MenuItem mi;
        // file
        mb.getMenus().add(m = new Menu("File"));
        mb.getMenus().add(m = new Menu("Log"));
        m.getItems().add(mi = new MenuItem("Clear"));
        mi.setOnAction((_) -> clearLog());
        // test
        mb.getMenus().add(m = new Menu("Test"));
        m.getItems().add(mi = new MenuItem("Run a Snippet"));
        mi.setOnAction((_) -> openSnippetWindow(stage));

        SplitPane split = new SplitPane(table, log);
        split.setOrientation(Orientation.VERTICAL);
        
        BorderPane bp = new BorderPane(split);
        bp.setTop(new VBox(mb, tb));
        Scene scene = new Scene(bp, 1100, 500);
        
        stage.setTitle("Manual Test Runner");
        stage.setScene(scene);
        stage.show();

        Platform.runLater(this::loadTestPlan);
    }

    private void loadTestPlan() {
        // TODO
        table.getItems().addAll(
            new DataRow(EmojiTest.class)
        );
    }
    
    private void clearLog() {
        log.clear();
    }

    private void runTest() {
        DataRow d = table.getSelectionModel().getSelectedItem();
        execute(d);
    }

    private static class DataRow {
        public final Class<?> test;
        public final StringProperty name = new SimpleStringProperty();
        public final StringProperty status = new SimpleStringProperty();
        public final StringProperty lastRun = new SimpleStringProperty();

        public DataRow(Class<?> test) {
            this.test = test;
            name.set(test.getSimpleName());
        }
    }

    private void execute(DataRow d) {
        // locations
        File processDir = new File(".");
        String classPath = "build/classes";
        
        String[] cmd = {
            javaExecutablePath,
            "-ea",
            "-Djavafx.enablePreview=true",
            "--enable-native-access=javafx.graphics",
            "-Dfile.encoding=UTF-8",
            "-Dstdout.encoding=UTF-8",
            "-Dstderr.encoding=UTF-8",
            "-p", "../../build/sdk/lib",
            "--add-modules=javafx.base,javafx.graphics,javafx.controls,javafx.fxml",
            "-cp", classPath,
            d.test.getName()
        };
        String[] env = {
        };
        try {
            Process p = Runtime.getRuntime().exec(cmd, env, processDir);
            new Monitor(p.getErrorStream(), System.err).start();
            new Monitor(p.getInputStream(), System.out).start();
            new StatusTracker(p.onExit(), d).start();
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    private class StatusTracker extends Thread {
        private final Future<Process> future;
        private final DataRow data;
        
        public StatusTracker(Future<Process> f, DataRow d) {
            this.future = f;
            this.data = d;
        }
        
        @Override
        public void run() {
            try {
                Process p = future.get();
                int result = p.exitValue();
                setResult(result == 0 ? "Pass" : "Fail");
            } catch(Throwable e) {
                e.printStackTrace();
                setResult("Error");
            }
        }

        private void setResult(String result) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            String time = f.format(LocalDateTime.now());
            
            Platform.runLater(() -> {
                data.status.set(result);
                data.lastRun.set(time);
            });
        }
    }
    
    private class Monitor extends Thread {
        private final InputStream in;
        private final PrintStream out;
        
        public Monitor(InputStream in, PrintStream out) {
            this.in = in;
            this.out = out;
        }
        
        @Override
        public void run() {
            try {
                for (;;) {
                    int c = in.read();
                    if (c < 0) {
                        return;
                    }
                    char ch = (char)c;
                    out.append(ch);
                    
                    // TODO optimize
                    Platform.runLater(() -> {
                        log.appendText(String.valueOf(ch));
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static String initJavaExecutablePath() {
        String path = ProcessHandle.current().info().command().orElseThrow();
        IO.println(path);
        return path;
    }

    private void openSnippetWindow(Stage parent) {
        new SnippetWindow(parent).show();
    }
}