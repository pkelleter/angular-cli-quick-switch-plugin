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

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class QuickSwitchAction extends DumbAwareAction {
    private static final List<QuickSwitchFileType> FILE_TYPES = List.of(QuickSwitchFileType.values());
    private static final List<QuickSwitchFileType> FILE_TYPES_BY_SUFFIX_LENGTH = FILE_TYPES.stream()
        .sorted(Comparator.comparingInt((QuickSwitchFileType fileType) -> fileType.suffix().length()).reversed())
        .toList();

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

        QuickSwitchSettings settings = QuickSwitchSettings.getInstance();
        VirtualFile targetFile = findTargetFile(currentFile, settings::isFileTypeEnabled);
        if (targetFile == null) {
            return;
        }

        navigate(
            project,
            currentFile,
            targetFile,
            settings.isClosePreviousTab()
        );
    }

    static void navigate(
        @NotNull Project project,
        @NotNull VirtualFile currentFile,
        @NotNull VirtualFile targetFile,
        boolean closePreviousTab
    ) {
        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, targetFile)
            .setUseCurrentWindow(true);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        boolean targetOpened = !fileEditorManager.openEditor(descriptor, true).isEmpty();

        if (targetOpened && closePreviousTab && currentFile.isValid() && !currentFile.equals(targetFile)) {
            fileEditorManager.closeFile(currentFile);
        }
    }

    static @Nullable VirtualFile findTargetFile(@NotNull VirtualFile currentFile) {
        return findTargetFile(currentFile, QuickSwitchFileType::enabledByDefault);
    }

    static @Nullable VirtualFile findTargetFile(
        @NotNull VirtualFile currentFile,
        @NotNull Predicate<QuickSwitchFileType> isEnabled
    ) {
        if (!currentFile.isValid() || currentFile.isDirectory()) {
            return null;
        }

        VirtualFile parent = currentFile.getParent();
        FileMatch currentMatch = matchFile(currentFile);
        if (parent == null || currentMatch == null) {
            return null;
        }

        int currentIndex = FILE_TYPES.indexOf(currentMatch.fileType());
        VirtualFile[] siblings = parent.getChildren();

        for (int offset = 1; offset < FILE_TYPES.size(); offset++) {
            QuickSwitchFileType targetType = FILE_TYPES.get((currentIndex + offset) % FILE_TYPES.size());
            if (!isEnabled.test(targetType)) {
                continue;
            }

            String targetName = currentMatch.baseName() + "." + targetType.suffix();
            for (VirtualFile sibling : siblings) {
                if (sibling.isValid()
                    && !sibling.isDirectory()
                    && targetName.equalsIgnoreCase(sibling.getName())) {
                    return sibling;
                }
            }
        }

        return null;
    }

    static @Nullable VirtualFile findTargetFile(
        @NotNull VirtualFile currentFile,
        @NotNull QuickSwitchFileType.Category category
    ) {
        if (!currentFile.isValid() || currentFile.isDirectory()) {
            return null;
        }

        VirtualFile parent = currentFile.getParent();
        FileMatch currentMatch = matchFile(currentFile);
        if (parent == null || currentMatch == null) {
            return null;
        }

        List<QuickSwitchFileType> categoryTypes = FILE_TYPES.stream()
            .filter(fileType -> fileType.category() == category)
            .toList();
        int currentIndex = categoryTypes.indexOf(currentMatch.fileType());
        VirtualFile[] siblings = parent.getChildren();

        if (currentIndex < 0) {
            for (QuickSwitchFileType targetType : categoryTypes) {
                VirtualFile target = findSibling(currentMatch.baseName(), targetType, siblings);
                if (target != null) {
                    return target;
                }
            }
            return null;
        }

        for (int offset = 1; offset < categoryTypes.size(); offset++) {
            QuickSwitchFileType targetType = categoryTypes.get((currentIndex + offset) % categoryTypes.size());
            VirtualFile target = findSibling(currentMatch.baseName(), targetType, siblings);
            if (target != null) {
                return target;
            }
        }

        return null;
    }

    static boolean isSwitchable(@Nullable VirtualFile file) {
        return file != null
            && file.isValid()
            && !file.isDirectory()
            && file.getParent() != null
            && matchFile(file) != null;
    }

    private static @Nullable VirtualFile findSibling(
        @NotNull String baseName,
        @NotNull QuickSwitchFileType targetType,
        VirtualFile @NotNull [] siblings
    ) {
        String targetName = baseName + "." + targetType.suffix();
        for (VirtualFile sibling : siblings) {
            if (sibling.isValid()
                && !sibling.isDirectory()
                && targetName.equalsIgnoreCase(sibling.getName())) {
                return sibling;
            }
        }
        return null;
    }

    private static @Nullable FileMatch matchFile(@NotNull VirtualFile file) {
        String fileName = file.getName();
        for (QuickSwitchFileType fileType : FILE_TYPES_BY_SUFFIX_LENGTH) {
            String suffix = "." + fileType.suffix();
            int suffixStart = fileName.length() - suffix.length();
            if (suffixStart > 0 && fileName.regionMatches(true, suffixStart, suffix, 0, suffix.length())) {
                return new FileMatch(fileType, fileName.substring(0, suffixStart));
            }
        }
        return null;
    }

    private record FileMatch(QuickSwitchFileType fileType, String baseName) { }
}
