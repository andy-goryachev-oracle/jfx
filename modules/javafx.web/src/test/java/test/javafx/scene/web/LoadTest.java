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

package test.javafx.scene.web;

import static javafx.concurrent.Worker.State.READY;
import static javafx.concurrent.Worker.State.RUNNING;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class LoadTest extends TestBase {

    private State getLoadState() {
        return submit(() -> getEngine().getLoadWorker().getState());
    }

    @Test public void testLoadGoodUrl() {
        final String FILE = "src/test/resources/test/html/ipsum.html";
        load(new File(FILE));
        WebEngine web = getEngine();

        assertTrue(getLoadState() == SUCCEEDED, "Load task completed successfully");
        assertTrue(web.getLocation().endsWith(FILE), "Location.endsWith(FILE)");
        assertNotNull(web.getDocument(),"Document should not be null");
    }

    @Test
    public void testLoadBadUrl() {
        final String URL = "bad://url";
        load(URL);
        WebEngine web = getEngine();

        assertTrue(getLoadState() == FAILED, "Load task failed");
        assertEquals(URL, web.getLocation(), "Location");
        assertNull(web.getDocument(), "Document should be null");
    }

    @Test
    public void testLoadHtmlContent() {
        final String TITLE = "In a Silent Way";
        loadContent("<html><head><title>" + TITLE + "</title></head></html>");
        WebEngine web = getEngine();

        assertTrue(getLoadState() == SUCCEEDED, "Load task completed successfully");
        assertEquals("", web.getLocation(), "Location");
        assertNotNull(web.getDocument(), "Document should not be null");
    }

    @Test
    public void testLoadPlainContent() {
        final String TEXT =
                "<html><head><title>Hidden Really Well</title></head></html>";
        loadContent(TEXT, "text/plain");
        final WebEngine web = getEngine();

        assertTrue(getLoadState() == SUCCEEDED, "Load task completed successfully");
        assertEquals("", web.getLocation(), "Location");

        // DOM access should happen on FX thread
        submit(() -> {
            Document doc = web.getDocument();
            assertNotNull(doc, "Document should not be null");
            Node el = // html -> body -> pre -> text
                    doc.getDocumentElement().getLastChild().getFirstChild().getFirstChild();
            String text = ((Text)el).getNodeValue();
            assertEquals(TEXT, text,
                    "Plain text should not be interpreted as HTML");
        });
    }

    @Test
    public void testLoadEmpty() {
        testLoadEmpty(null);
        testLoadEmpty("");
        testLoadEmpty("about:blank");
    }

    private void testLoadEmpty(String url) {
        load(url);
        final WebEngine web = getEngine();

        assertTrue(getLoadState() == SUCCEEDED, "Load task completed successfully");
        assertEquals("about:blank", web.getLocation(), "Location");
        assertNull(web.getTitle(), "Title should be null");

        submit(() -> {
            Document doc = web.getDocument();
            assertNotNull(doc, "Document should not be null");

            Element html = doc.getDocumentElement();
            assertNotNull(html, "There should be an HTML element");
            assertEquals("HTML", html.getTagName(), "HTML element should have tag HTML");

            NodeList htmlNodes = html.getChildNodes();
            assertNotNull(htmlNodes, "HTML element should have two children");
            assertEquals(2, htmlNodes.getLength(), "HTML element should have two children");

            Element head = (Element) htmlNodes.item(0);
            NodeList headNodes = head.getChildNodes();
            assertNotNull(head, "There should be a HEAD element");
            assertEquals("HEAD", head.getTagName(), "HEAD element should have tag HEAD");
            assertTrue(headNodes == null || headNodes.getLength() == 0,
                    "HEAD element should have no children");

            Element body = (Element) htmlNodes.item(1);
            NodeList bodyNodes = body.getChildNodes();
            assertNotNull(body, "There should be a BODY element");
            assertEquals("BODY", body.getTagName(), "BODY element should have tag BODY");
            assertTrue(bodyNodes == null || bodyNodes.getLength() == 0,
                    "BODY element should have no children");
        });
    }

    @Test
    public void testLoadUrlWithEncodedSpaces() {
        final String URL = "http://localhost/test.php?param=a%20b%20c";
        load(URL);
        WebEngine web = getEngine();

        assertEquals(URL, web.getLocation(), "Unexpected location");
    }

    @Test
    public void testLoadUrlWithUnencodedSpaces() {
        final String URL = "http://localhost/test.php?param=a b c";
        load(URL);
        WebEngine web = getEngine();

        assertEquals(URL.replace(" ", "%20"), web.getLocation(),
                "Unexpected location");
    }

    @Test
    public void testLoadContentWithLocalScript() {
        WebEngine webEngine = getEngine();

        final StringBuilder result = new StringBuilder();
        webEngine.setOnAlert(event -> {
            result.append("ALERT: ").append(event.getData());
        });

        String scriptUrl =
                new File("src/test/resources/test/html/invoke-alert.js").toURI().toASCIIString();
        String html =
                "<html>\n" +
                "<head><script src=\"" + scriptUrl + "\"></script></head>\n" +
                "<body><script>invokeAlert('foo');</script></body>\n" +
                "</html>";
        loadContent(html);

        assertEquals("ALERT: foo", result.toString(), "Unexpected result");
        assertEquals(SUCCEEDED, getLoadState(), "Unexpected load state");
        assertEquals("", webEngine.getLocation(), "Unexpected location");
        assertNotNull(webEngine.getDocument(), "Document is null");
    }

    @Test
    public void testLoadLocalCSS() {
        load(new File("src/test/resources/test/html/dom.html"));
        submit(() -> {
            assertEquals("700", getEngine().executeScript(
                "window.getComputedStyle(document.getElementById('p3')).getPropertyValue('font-weight')"),
                    "Font weight should be bold");
            assertEquals("italic", getEngine().executeScript(
                "window.getComputedStyle(document.getElementById('p3')).getPropertyValue('font-style')"),
                    "font style should be italic");
        });
    }

    @Test
    public void testLoadTitleChanged() {
        final String FILE = "src/test/resources/test/html/ipsum.html";
        final CountDownLatch latch = new CountDownLatch(1);

        submit(() -> {
            WebEngine webEngine = new WebEngine();
            webEngine.titleProperty().addListener((observable, oldValue, newValue) -> {
                assertTrue(webEngine.getLoadWorker().getState() == SUCCEEDED, "loadContent in SUCCEEDED State");
                assertEquals("Lorem Ipsum", webEngine.getTitle(), "Title");
                latch.countDown();
            });

            webEngine.load(new File(FILE).toURI().toASCIIString());
        });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void testLoadContentTitleChanged() {
        final String TITLE = "Title Test";
        final CountDownLatch latch = new CountDownLatch(1);

        submit(() -> {
            WebEngine webEngine = new WebEngine();
            webEngine.titleProperty().addListener((observable, oldValue, newValue) -> {
                assertTrue(webEngine.getLoadWorker().getState() == SUCCEEDED, "loadContent in SUCCEEDED State");
                assertEquals(TITLE, webEngine.getTitle(), "Title");
                latch.countDown();
            });

            webEngine.loadContent("<html><head><title>" + TITLE + "</title></head></html>");
        });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * @test
     * @bug 8140501
     * summary loadContent on location changed
     */
    @Test
    public void loadContentOnLocationChange() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);

        submit(() -> {
            WebEngine webEngine = new WebEngine();
            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
                // NOTE: blank url == about:blank
                // loading a empty or null url to WebKit
                // will be treated as blank url
                // ref : https://html.spec.whatwg.org
                if (newValue.equalsIgnoreCase("about:blank")) {
                    webEngine.loadContent("");
                    assertTrue(webEngine.getLoadWorker().getState() == READY, "loadContent in READY State");
                }
            });

            webEngine.getLoadWorker().stateProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue == SUCCEEDED) {
                    latch.countDown();
                }
            }));

            webEngine.load("");
            assertTrue(webEngine.getLoadWorker().getState() == SUCCEEDED, "load task completed successfully");
        });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * @test
     * @bug 8140501
     * summary load url on location changed
     */
    @Test
    public void loadUrlOnLocationChange() throws Exception {
        // Cancelling loadContent is synchronous,
        // there wont be 2 SUCCEEDED event
        final CountDownLatch latch = new CountDownLatch(1);

        submit(() -> {
            WebEngine webEngine = new WebEngine();
            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.equalsIgnoreCase("")) {
                    webEngine.load("");
                    assertTrue(webEngine.getLoadWorker().getState() == READY, "Load in READY State");
                }
            });

            webEngine.getLoadWorker().stateProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue == SUCCEEDED) {
                    latch.countDown();
                }
            }));

            webEngine.loadContent("");
            assertTrue(webEngine.getLoadWorker().getState() == RUNNING, "loadContent task running");
        });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        }
    }

   /**
     * @test
     * @bug 8152420
     * summary loading relative sub-resource from jar
     */
    @Test
    public void loadJarFile() throws Exception {

        // archive-root0.html -- src archive-r0.js, c/archive-c0.js
        load("jar:" + new File(System.getProperties().get("WEB_ARCHIVE_JAR_TEST_DIR").toString()
                + "/webArchiveJar.jar").toURI().toASCIIString() + "!/archive-root0.html");
        assertEquals("loaded", executeScript("jsr0()").toString(),
                "archive-root0.html failed to load src='archive-r0.js'");

        assertEquals("loaded", executeScript("jsc0()").toString(),
                "archive-root0.html failed to load src='c/archive-c0.js'");

        // archive-root1.html -- src ./archive-r0.js, ./c/archive-c0.js
        load("jar:" + new File(System.getProperties().get("WEB_ARCHIVE_JAR_TEST_DIR").toString()
                + "/webArchiveJar.jar").toURI().toASCIIString() + "!/archive-root1.html");
        assertEquals("loaded", executeScript("jsr0()").toString(),
                "archive-root1.html failed to load src='./archive-r0.js'");

        assertEquals("loaded", executeScript("jsc0()").toString(),
                "archive-root1.html failed to load src='./c/archive-c0.js'");

        // archive-root2.html -- src ./c/../archive-r0.js, ./c/./././archive-c0.js
        load("jar:" + new File(System.getProperties().get("WEB_ARCHIVE_JAR_TEST_DIR").toString()
                + "/webArchiveJar.jar").toURI().toASCIIString() + "!/archive-root2.html");
        assertEquals("loaded", executeScript("jsr0()").toString(),
                "archive-root2.html failed to load src='./c/../archive-r0.js'");

        assertEquals("loaded", executeScript("jsc0()").toString(),
                "archive-root2.html failed to load src='./c/./././archive-c0.js'");
    }

    /**
     * 8153681 html "img" tag event listener
     */
    public final class ImageEvent {
        public void onLoad() {
            ++loaded;
        }

        public void onError() {
            ++failed;
        }

        int loaded;
        int failed;
    }

    /**
     * @test
     * @bug 8153681
     * summary testing jrt url scheme support in WebView
     */
    @Test
    @Timeout(30)
    public void loadJrtResource() throws Exception {
        assumeTrue(isJigsawMode());

        final String[] jrtResources = {
                "jrt:/javafx.web/javafx/scene/web/AlignLeft_16x16_JFX.png",
                "jrt:/javafx.web/./javafx/scene/web/Strikethrough_16x16_JFX.png",
                "jrt:/javafx.web/./javafx/scene/../../javafx/scene/web/FontColor_16x16_JFX.png",
                "jrt:/javafx.web/./javafx/./scene/./web/./DrawHorizontalLine_16x16_JFX.png",
                "jrt:/javafx.web/javafx/scene/web/../../../javafx/scene/web/OrderedListNumbers_16x16_JFX-rtl.png"
        };

        // Load single resource and check for image is being rendered
        // Check the natural width of rendered image
        load(jrtResources[0]);
        assertEquals(1, executeScript("document.getElementsByTagName('img').length"),
                "Failed to load " + jrtResources[0]);

        Integer actualWidth = (Integer) executeScript("document.getElementsByTagName('img')[0].naturalWidth");
        assertEquals(16, actualWidth, "Failed to Render " + jrtResources[0]);

        // LoadContent with multiple jrt resource which needs to
        // resolve path navigation i.e ./ or ../ in native url handler
        final ImageEvent imageEvent = new ImageEvent();

        // Wait till contents are loaded (SUCCEEDED)
        final CountDownLatch latch = new CountDownLatch(1);
        submit(() -> {
            WebEngine webEngine = new WebEngine();
            webEngine.getLoadWorker().stateProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue == SUCCEEDED) {
                    final String msg = String.format(
                            "Failed to load : %d / %d resources",
                            imageEvent.failed, jrtResources.length);
                    assertTrue(imageEvent.loaded == jrtResources.length, msg);
                    latch.countDown();
                }
            }));

            StringBuffer jrtContent = new StringBuffer();
            for (int i = 0; i < jrtResources.length; ++i) {
                jrtContent.append("<img src=" +  jrtResources[i] +
                        " onload='imageStatus.onLoad()'" +
                        " onerror='imageStatus.onError()'/>");
            }
            final JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("imageStatus", imageEvent );
            webEngine.loadContent(jrtContent.toString());
        });

        try {
            latch.await();
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        }
    }

    // JDK-8282134 Certain regex can cause a JS trap in WebView
    @Test
    public void jsRegexpTrapTest() {
        final String FILE = "src/test/resources/test/html/unicode.html";
        load(new File(FILE));
        WebEngine web = getEngine();
        assertTrue(getLoadState() == SUCCEEDED, "Load task completed successfully");
    }

    // JDK-8311097 Synchronous XMLHttpRequest not receiving data
    @Test
    public void testSynchronousDataRequest() {
        final String TEXT = "pass";
        final String FILE = "src/test/resources/test/html/sync-request.html";
        load(new File(FILE));
        final WebEngine web = getEngine();

        assertTrue(getLoadState() == SUCCEEDED, "Load task completed successfully");

        // DOM access should happen on FX thread
        submit(() -> {
            Document doc = web.getDocument();
            assertNotNull(doc, "Document should not be null");

            var body = doc.getDocumentElement().getLastChild();
            String text = getTargetText(body);
            assertEquals(TEXT, text, "Found expected text");
        });
    }

    private String getId(Node n) {
        if (Node.ELEMENT_NODE == n.getNodeType()) {
            return ((Element)n).getAttribute("id");
        }
        return "";
    }

    private String getTargetText(Node body) {
        Node c = body.getFirstChild();
        while (!"target".equals(getId(c))) {
            c = c.getNextSibling();
        }
        assertNotNull(c, "Found target element");
        return c.getTextContent();
    }
}
