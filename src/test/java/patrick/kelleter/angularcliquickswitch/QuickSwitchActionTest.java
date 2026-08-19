package patrick.kelleter.angularcliquickswitch;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public final class QuickSwitchActionTest extends BasePlatformTestCase {
    public void testCyclesThroughExistingCounterparts() {
        VirtualFile typescript = addFile("example.component.ts");
        VirtualFile html = addFile("example.component.html");
        VirtualFile stylesheet = addFile("example.component.scss");

        assertTarget(html, QuickSwitchAction.findTargetFile(typescript));
        assertTarget(stylesheet, QuickSwitchAction.findTargetFile(html));
        assertTarget(typescript, QuickSwitchAction.findTargetFile(stylesheet));
    }

    public void testUsesConfiguredExtensionOrder() {
        VirtualFile typescript = addFile("example.component.ts");
        VirtualFile javascript = addFile("example.component.js");
        addFile("example.component.html");

        assertTarget(javascript, QuickSwitchAction.findTargetFile(typescript));
    }

    public void testReturnsNullForUnsupportedExtension() {
        VirtualFile markdown = addFile("example.component.md");
        addFile("example.component.ts");

        assertNull(QuickSwitchAction.findTargetFile(markdown));
    }

    public void testReturnsNullWithoutCounterpart() {
        VirtualFile typescript = addFile("example.component.ts");

        assertNull(QuickSwitchAction.findTargetFile(typescript));
    }

    public void testSupportsUppercaseExtensions() {
        VirtualFile typescript = addFile("example.component.TS");
        VirtualFile html = addFile("example.component.HTML");

        assertTarget(html, QuickSwitchAction.findTargetFile(typescript));
    }

    public void testHandlesDotsInParentDirectory() {
        VirtualFile typescript = addFile("feature.v2/example.component.ts");
        VirtualFile html = addFile("feature.v2/example.component.html");

        assertTarget(html, QuickSwitchAction.findTargetFile(typescript));
    }

    public void testSkipsDirectoriesNamedLikeCounterparts() {
        VirtualFile typescript = addFile("example.component.ts");
        myFixture.addFileToProject("example.component.html/nested.txt", "");
        VirtualFile stylesheet = addFile("example.component.css");

        assertTarget(stylesheet, QuickSwitchAction.findTargetFile(typescript));
    }

    private VirtualFile addFile(String path) {
        return myFixture.addFileToProject(path, "").getVirtualFile();
    }

    private void assertTarget(VirtualFile expected, VirtualFile actual) {
        assertNotNull(actual);
        assertEquals(expected.getPath(), actual.getPath());
    }
}
