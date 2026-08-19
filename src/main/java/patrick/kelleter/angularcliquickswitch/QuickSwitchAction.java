package patrick.kelleter.angularcliquickswitch;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public final class QuickSwitchAction extends DumbAwareAction {
    private static final List<String> EXTENSIONS = List.of(
        "ts", "js",
        "html", "php", "haml", "jade", "pug", "slim",
        "css", "sass", "scss", "less", "styl"
    );

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        event.getPresentation().setEnabled(project != null && !project.isDisposed() && isSwitchable(file));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile currentFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || project.isDisposed() || currentFile == null) {
            return;
        }

        VirtualFile targetFile = findTargetFile(currentFile);
        if (targetFile == null) {
            return;
        }

        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, targetFile)
            .setUseCurrentWindow(true);
        FileEditorManager.getInstance(project).openEditor(descriptor, true);
    }

    static @Nullable VirtualFile findTargetFile(@NotNull VirtualFile currentFile) {
        if (!currentFile.isValid() || currentFile.isDirectory()) {
            return null;
        }

        VirtualFile parent = currentFile.getParent();
        String extension = currentFile.getExtension();
        if (parent == null || extension == null) {
            return null;
        }

        String currentExtension = extension.toLowerCase(Locale.ROOT);
        int currentIndex = EXTENSIONS.indexOf(currentExtension);
        if (currentIndex < 0) {
            return null;
        }

        String baseName = currentFile.getNameWithoutExtension();
        VirtualFile[] siblings = parent.getChildren();

        for (int offset = 1; offset < EXTENSIONS.size(); offset++) {
            String targetExtension = EXTENSIONS.get((currentIndex + offset) % EXTENSIONS.size());
            for (VirtualFile sibling : siblings) {
                if (sibling.isValid()
                    && !sibling.isDirectory()
                    && baseName.equals(sibling.getNameWithoutExtension())
                    && targetExtension.equalsIgnoreCase(sibling.getExtension())) {
                    return sibling;
                }
            }
        }

        return null;
    }

    private static boolean isSwitchable(@Nullable VirtualFile file) {
        if (file == null || !file.isValid() || file.isDirectory() || file.getParent() == null) {
            return false;
        }

        String extension = file.getExtension();
        return extension != null && EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
    }
}
