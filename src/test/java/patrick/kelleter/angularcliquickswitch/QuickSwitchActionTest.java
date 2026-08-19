package patrick.kelleter.angularcliquickswitch;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Arrays;

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

    public void testSkipsDisabledFileType() {
        VirtualFile html = addFile("example.component.html");
        addFile("example.component.scss");
        VirtualFile typescript = addFile("example.component.ts");

        VirtualFile target = QuickSwitchAction.findTargetFile(
            html,
            fileType -> fileType.enabledByDefault() && fileType != QuickSwitchFileType.SCSS
        );

        assertTarget(typescript, target);
    }

    public void testCanSwitchAwayFromDisabledFileType() {
        VirtualFile stylesheet = addFile("example.component.scss");
        VirtualFile typescript = addFile("example.component.ts");

        VirtualFile target = QuickSwitchAction.findTargetFile(
            stylesheet,
            fileType -> fileType.enabledByDefault() && fileType != QuickSwitchFileType.SCSS
        );

        assertTarget(typescript, target);
    }

    public void testSkipsSpecFileByDefault() {
        VirtualFile typescript = addFile("example.component.ts");
        addFile("example.component.spec.ts");
        VirtualFile html = addFile("example.component.html");

        assertTarget(html, QuickSwitchAction.findTargetFile(typescript));
    }

    public void testCyclesThroughEnabledSpecFile() {
        VirtualFile typescript = addFile("example.component.ts");
        VirtualFile spec = addFile("example.component.spec.ts");

        VirtualFile target = QuickSwitchAction.findTargetFile(
            typescript,
            fileType -> fileType.enabledByDefault() || fileType == QuickSwitchFileType.TYPESCRIPT_SPEC
        );

        assertTarget(spec, target);
        assertTarget(typescript, QuickSwitchAction.findTargetFile(
            spec,
            fileType -> fileType.enabledByDefault() || fileType == QuickSwitchFileType.TYPESCRIPT_SPEC
        ));
    }

    public void testJumpsDirectlyToFirstTestFile() {
        VirtualFile typescript = addFile("example.component.ts");
        VirtualFile typescriptSpec = addFile("example.component.spec.ts");
        addFile("example.component.spec.js");

        VirtualFile target = QuickSwitchAction.findTargetFile(
            typescript,
            QuickSwitchFileType.Category.TESTS
        );

        assertTarget(typescriptSpec, target);
    }

    public void testCyclesOnlyWithinTestFiles() {
        VirtualFile typescriptSpec = addFile("example.component.spec.ts");
        VirtualFile javascriptSpec = addFile("example.component.spec.js");
        addFile("example.component.ts");

        assertTarget(javascriptSpec, QuickSwitchAction.findTargetFile(
            typescriptSpec,
            QuickSwitchFileType.Category.TESTS
        ));
        assertTarget(typescriptSpec, QuickSwitchAction.findTargetFile(
            javascriptSpec,
            QuickSwitchFileType.Category.TESTS
        ));
    }

    public void testJumpsDirectlyToFirstControllerFile() {
        VirtualFile html = addFile("example.component.html");
        VirtualFile typescript = addFile("example.component.ts");
        addFile("example.component.js");

        assertTarget(typescript, QuickSwitchAction.findTargetFile(
            html,
            QuickSwitchFileType.Category.CONTROLLER
        ));
    }

    public void testJumpsDirectlyToFirstStyleFile() {
        VirtualFile typescript = addFile("example.component.ts");
        VirtualFile css = addFile("example.component.css");
        addFile("example.component.scss");

        assertTarget(css, QuickSwitchAction.findTargetFile(
            typescript,
            QuickSwitchFileType.Category.STYLE
        ));
    }

    public void testJumpsDirectlyToFirstMarkupFile() {
        VirtualFile typescript = addFile("example.component.ts");
        VirtualFile html = addFile("example.component.html");
        addFile("example.component.pug");

        assertTarget(html, QuickSwitchAction.findTargetFile(
            typescript,
            QuickSwitchFileType.Category.TEMPLATE
        ));
    }

    public void testCategoryShortcutDoesNotReopenOnlyCurrentFile() {
        VirtualFile typescript = addFile("example.component.ts");

        assertNull(QuickSwitchAction.findTargetFile(
            typescript,
            QuickSwitchFileType.Category.CONTROLLER
        ));
    }

    public void testKeepsPreviousTabOpen() {
        VirtualFile typescript = addFile("example.component.ts");
        VirtualFile html = addFile("example.component.html");
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
        fileEditorManager.openFile(typescript, true);

        QuickSwitchAction.navigate(getProject(), typescript, html, false);

        assertOpen(fileEditorManager, typescript);
        assertOpen(fileEditorManager, html);
    }

    public void testClosesPreviousTabAfterOpeningTarget() {
        VirtualFile typescript = addFile("example.component.ts");
        VirtualFile html = addFile("example.component.html");
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
        fileEditorManager.openFile(typescript, true);

        QuickSwitchAction.navigate(getProject(), typescript, html, true);

        assertNotOpen(fileEditorManager, typescript);
        assertOpen(fileEditorManager, html);
    }

    private VirtualFile addFile(String path) {
        return myFixture.addFileToProject(path, "").getVirtualFile();
    }

    private void assertTarget(VirtualFile expected, VirtualFile actual) {
        assertNotNull(actual);
        assertEquals(expected.getPath(), actual.getPath());
    }

    private void assertOpen(FileEditorManager fileEditorManager, VirtualFile file) {
        assertTrue(Arrays.stream(fileEditorManager.getOpenFiles())
            .anyMatch(openFile -> openFile.getPath().equals(file.getPath())));
    }

    private void assertNotOpen(FileEditorManager fileEditorManager, VirtualFile file) {
        assertFalse(Arrays.stream(fileEditorManager.getOpenFiles())
            .anyMatch(openFile -> openFile.getPath().equals(file.getPath())));
    }
}
