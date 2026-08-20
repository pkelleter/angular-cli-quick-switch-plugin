package patrick.kelleter.angularcliquickswitch;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class QuickSwitchAction extends DumbAwareAction {
    private static final List<QuickSwitchFileType> BUILT_IN_TYPES = List.of(QuickSwitchFileType.values());
    private static final List<FileTypeDefinition> BUILT_IN_DEFINITIONS = BUILT_IN_TYPES.stream()
        .map(fileType -> new FileTypeDefinition(fileType.category(), fileType.suffix(), fileType))
        .toList();

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        event.getPresentation().setEnabled(
            project != null
                && !project.isDisposed()
                && isSwitchable(file, QuickSwitchSettings.getInstance())
        );
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile currentFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || project.isDisposed() || currentFile == null) {
            return;
        }

        QuickSwitchSettings settings = QuickSwitchSettings.getInstance();
        VirtualFile targetFile = findTargetFile(currentFile, settings);
        if (targetFile == null) {
            return;
        }

        navigate(project, currentFile, targetFile, settings.isClosePreviousTab());
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
        return findTargetFile(
            currentFile,
            BUILT_IN_DEFINITIONS,
            definition -> isEnabled.test(definition.builtInType())
        );
    }

    static @Nullable VirtualFile findTargetFile(
        @NotNull VirtualFile currentFile,
        @NotNull QuickSwitchSettings settings
    ) {
        List<FileTypeDefinition> definitions = createFileTypeDefinitions(settings);
        return findTargetFile(currentFile, definitions, definition -> isEnabled(definition, settings));
    }

    static @Nullable VirtualFile findTargetFile(
        @NotNull VirtualFile currentFile,
        @NotNull QuickSwitchFileType.Category category
    ) {
        return findTargetFile(currentFile, category, BUILT_IN_DEFINITIONS, definition -> true);
    }

    static @Nullable VirtualFile findTargetFile(
        @NotNull VirtualFile currentFile,
        @NotNull QuickSwitchFileType.Category category,
        @NotNull QuickSwitchSettings settings
    ) {
        List<FileTypeDefinition> definitions = createFileTypeDefinitions(settings);
        return findTargetFile(
            currentFile,
            category,
            definitions,
            definition -> definition.builtInType() != null || isEnabled(definition, settings)
        );
    }

    static boolean isSwitchable(@Nullable VirtualFile file, @NotNull QuickSwitchSettings settings) {
        return isSwitchable(file, createFileTypeDefinitions(settings));
    }

    private static @Nullable VirtualFile findTargetFile(
        @NotNull VirtualFile currentFile,
        @NotNull List<FileTypeDefinition> definitions,
        @NotNull Predicate<FileTypeDefinition> isEnabled
    ) {
        FileContext context = getFileContext(currentFile, definitions);
        if (context == null) {
            return null;
        }

        int currentIndex = definitions.indexOf(context.currentType());
        for (int offset = 1; offset < definitions.size(); offset++) {
            FileTypeDefinition targetType = definitions.get((currentIndex + offset) % definitions.size());
            if (!isEnabled.test(targetType)) {
                continue;
            }

            VirtualFile target = findSibling(context.baseName(), targetType, context.siblings());
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    private static @Nullable VirtualFile findTargetFile(
        @NotNull VirtualFile currentFile,
        @NotNull QuickSwitchFileType.Category category,
        @NotNull List<FileTypeDefinition> definitions,
        @NotNull Predicate<FileTypeDefinition> isEnabled
    ) {
        FileContext context = getFileContext(currentFile, definitions);
        if (context == null) {
            return null;
        }

        List<FileTypeDefinition> categoryTypes = definitions.stream()
            .filter(definition -> definition.category() == category)
            .toList();
        int currentIndex = categoryTypes.indexOf(context.currentType());

        if (currentIndex < 0) {
            for (FileTypeDefinition targetType : categoryTypes) {
                if (!isEnabled.test(targetType)) {
                    continue;
                }
                VirtualFile target = findSibling(context.baseName(), targetType, context.siblings());
                if (target != null) {
                    return target;
                }
            }
            return null;
        }

        for (int offset = 1; offset < categoryTypes.size(); offset++) {
            FileTypeDefinition targetType = categoryTypes.get((currentIndex + offset) % categoryTypes.size());
            if (!isEnabled.test(targetType)) {
                continue;
            }
            VirtualFile target = findSibling(context.baseName(), targetType, context.siblings());
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    private static boolean isSwitchable(
        @Nullable VirtualFile file,
        @NotNull List<FileTypeDefinition> definitions
    ) {
        return file != null && getFileContext(file, definitions) != null;
    }

    private static @Nullable FileContext getFileContext(
        @NotNull VirtualFile file,
        @NotNull List<FileTypeDefinition> definitions
    ) {
        if (!file.isValid() || file.isDirectory()) {
            return null;
        }

        VirtualFile parent = file.getParent();
        FileMatch match = matchFile(file, definitions);
        if (parent == null || match == null) {
            return null;
        }
        return new FileContext(match.fileType(), match.baseName(), parent.getChildren());
    }

    private static @Nullable FileMatch matchFile(
        @NotNull VirtualFile file,
        @NotNull List<FileTypeDefinition> definitions
    ) {
        String fileName = file.getName();
        List<FileTypeDefinition> longestSuffixFirst = definitions.stream()
            .sorted(Comparator.comparingInt((FileTypeDefinition definition) -> definition.suffix().length()).reversed())
            .toList();

        for (FileTypeDefinition definition : longestSuffixFirst) {
            String suffix = "." + definition.suffix();
            int suffixStart = fileName.length() - suffix.length();
            if (suffixStart > 0 && fileName.regionMatches(true, suffixStart, suffix, 0, suffix.length())) {
                return new FileMatch(definition, fileName.substring(0, suffixStart));
            }
        }
        return null;
    }

    private static @Nullable VirtualFile findSibling(
        @NotNull String baseName,
        @NotNull FileTypeDefinition targetType,
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

    private static List<FileTypeDefinition> createFileTypeDefinitions(QuickSwitchSettings settings) {
        List<FileTypeDefinition> definitions = new ArrayList<>();
        Set<String> usedSuffixes = new HashSet<>();
        for (QuickSwitchFileType.Category category : QuickSwitchFileType.Category.values()) {
            for (QuickSwitchFileType fileType : BUILT_IN_TYPES) {
                if (fileType.category() == category) {
                    definitions.add(new FileTypeDefinition(category, fileType.suffix(), fileType));
                    usedSuffixes.add(fileType.suffix().toLowerCase(java.util.Locale.ROOT));
                }
            }

            String customSuffix = settings.getCustomSuffix(category);
            if (!customSuffix.isBlank()
                && usedSuffixes.add(customSuffix.toLowerCase(java.util.Locale.ROOT))) {
                definitions.add(new FileTypeDefinition(category, customSuffix, null));
            }
        }
        return definitions;
    }

    private static boolean isEnabled(FileTypeDefinition definition, QuickSwitchSettings settings) {
        return definition.builtInType() != null
            ? settings.isFileTypeEnabled(definition.builtInType())
            : settings.isCustomSuffixEnabled(definition.category());
    }

    private record FileTypeDefinition(
        QuickSwitchFileType.Category category,
        String suffix,
        @Nullable QuickSwitchFileType builtInType
    ) { }

    private record FileMatch(FileTypeDefinition fileType, String baseName) { }

    private record FileContext(
        FileTypeDefinition currentType,
        String baseName,
        VirtualFile[] siblings
    ) { }
}
