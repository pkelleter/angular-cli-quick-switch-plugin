package patrick.kelleter.angularcliquickswitch;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public abstract class QuickSwitchCategoryAction extends DumbAwareAction {
    private final QuickSwitchFileType.Category category;

    QuickSwitchCategoryAction(@NotNull QuickSwitchFileType.Category category) {
        this.category = category;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        event.getPresentation().setEnabled(
            project != null && !project.isDisposed() && QuickSwitchAction.isSwitchable(file)
        );
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile currentFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || project.isDisposed() || currentFile == null) {
            return;
        }

        VirtualFile targetFile = QuickSwitchAction.findTargetFile(currentFile, category);
        if (targetFile == null) {
            return;
        }

        QuickSwitchAction.navigate(
            project,
            currentFile,
            targetFile,
            QuickSwitchSettings.getInstance().isClosePreviousTab()
        );
    }
}
