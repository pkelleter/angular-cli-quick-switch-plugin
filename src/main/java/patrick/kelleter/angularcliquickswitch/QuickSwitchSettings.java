package patrick.kelleter.angularcliquickswitch;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Service(Service.Level.APP)
@State(
    name = "AngularCliQuickSwitchSettings",
    storages = @Storage("angularCliQuickSwitch.xml"),
    category = SettingsCategory.PLUGINS
)
public final class QuickSwitchSettings implements PersistentStateComponent<QuickSwitchSettings.SettingsState> {
    public static final class SettingsState {
        public boolean closePreviousTab;
        public Set<String> disabledFileTypes = new HashSet<>();
        public Set<String> enabledOptInFileTypes = new HashSet<>();
    }

    private volatile SettingsState state = new SettingsState();

    public static @NotNull QuickSwitchSettings getInstance() {
        return ApplicationManager.getApplication().getService(QuickSwitchSettings.class);
    }

    @Override
    public @NotNull SettingsState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull SettingsState state) {
        if (state.disabledFileTypes == null) {
            state.disabledFileTypes = new HashSet<>();
        }
        if (state.enabledOptInFileTypes == null) {
            state.enabledOptInFileTypes = new HashSet<>();
        }
        this.state = state;
    }

    public boolean isClosePreviousTab() {
        return state.closePreviousTab;
    }

    public void setClosePreviousTab(boolean closePreviousTab) {
        SettingsState newState = copyState();
        newState.closePreviousTab = closePreviousTab;
        state = newState;
    }

    boolean isFileTypeEnabled(@NotNull QuickSwitchFileType fileType) {
        SettingsState currentState = state;
        return fileType.enabledByDefault()
            ? !currentState.disabledFileTypes.contains(fileType.name())
            : currentState.enabledOptInFileTypes.contains(fileType.name());
    }

    void setFileTypeEnabled(@NotNull QuickSwitchFileType fileType, boolean enabled) {
        SettingsState newState = copyState();
        if (fileType.enabledByDefault()) {
            if (enabled) {
                newState.disabledFileTypes.remove(fileType.name());
            } else {
                newState.disabledFileTypes.add(fileType.name());
            }
        } else if (enabled) {
            newState.enabledOptInFileTypes.add(fileType.name());
        } else {
            newState.enabledOptInFileTypes.remove(fileType.name());
        }
        state = newState;
    }

    private SettingsState copyState() {
        SettingsState currentState = state;
        SettingsState newState = new SettingsState();
        newState.closePreviousTab = currentState.closePreviousTab;
        newState.disabledFileTypes = new HashSet<>(currentState.disabledFileTypes);
        newState.enabledOptInFileTypes = new HashSet<>(currentState.enabledOptInFileTypes);
        return newState;
    }
}
