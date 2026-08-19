package patrick.kelleter.angularcliquickswitch;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.APP)
@State(
    name = "AngularCliQuickSwitchSettings",
    storages = @Storage("angularCliQuickSwitch.xml"),
    category = SettingsCategory.PLUGINS
)
public final class QuickSwitchSettings implements PersistentStateComponent<QuickSwitchSettings.SettingsState> {
    public static final class SettingsState {
        public boolean closePreviousTab;
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
        this.state = state;
    }

    public boolean isClosePreviousTab() {
        return state.closePreviousTab;
    }

    public void setClosePreviousTab(boolean closePreviousTab) {
        SettingsState newState = new SettingsState();
        newState.closePreviousTab = closePreviousTab;
        state = newState;
    }
}
