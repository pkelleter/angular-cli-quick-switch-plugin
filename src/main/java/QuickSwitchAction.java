import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Arrays;
import java.util.List;

public class QuickSwitchAction extends AnAction {

    private AnActionEvent latestEvent;

    private List<String> extensions = Arrays.asList(
      "ts", "tsx",  "js", "jsx",
      "html", "php", "haml", "jade", "pug", "slim",
      "css", "sass", "scss", "less", "styl"
    );

    public QuickSwitchAction() {
        super("QuickSwitch");
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        this.latestEvent = event;

        VirtualFile currentFile = (VirtualFile) event.getDataContext().getData("virtualFile");
        if (currentFile == null) return;

        String currentFilePath = currentFile.getCanonicalPath();
        if (currentFilePath == null) return;

        String basePath = this.removeFilePathExtension(currentFilePath);
        String extension = this.getFilePathExtension(currentFilePath);
        if (basePath == null || extension == null) return;

        String newPath = this.cycleExtension(basePath, extension);
        this.switchFile(currentFilePath, newPath);
    }

    private String getFilePathExtension(String filePath) {
        int extensionPosition = filePath.lastIndexOf(".");
        if (extensionPosition == -1) return null;
        return filePath.substring(extensionPosition + 1);
    }

    private String removeFilePathExtension(String filePath) {
        int extensionPosition = filePath.lastIndexOf(".");
        if (extensionPosition == -1) return filePath;
        return filePath.substring(0, extensionPosition);
    }

    private String cycleExtension(String basePath, String currentExtension) {
        String currentPath = basePath + "." + currentExtension;
        int currentIndex = this.extensions.indexOf(currentExtension);
        if (currentIndex == -1) return currentPath;
        for (int i = 0; i < this.extensions.size(); i++) {
            int newIndex = ((currentIndex + 1 + i) % this.extensions.size());
            String newExtension = this.extensions.get(newIndex);
            String newPath = basePath + "." + newExtension;
            VirtualFile newFile = this.getFileByPath(newPath);
            if (newFile != null && newFile.exists()) return newPath;
        }
        return currentPath;
    }

    private void switchFile(String currentFilePath, String newFilePath) {
        Project project = this.latestEvent.getProject();
        if (project == null) return;
        VirtualFile currentFile = this.getFileByPath(currentFilePath);
        VirtualFile newFile = this.getFileByPath(newFilePath);
        if (!currentFile.exists() || !newFile.exists()) return;
        FileEditorManager.getInstance(project).closeFile(currentFile);
        new OpenFileDescriptor(project, newFile).navigate(true);
    }

    private VirtualFile getFileByPath(String path) {
        return LocalFileSystem.getInstance().findFileByPath(path);
    }

}
