package patrick.kelleter.angularcliquickswitch;

import junit.framework.TestCase;

public final class QuickSwitchSettingsTest extends TestCase {
    public void testKeepsTabsOpenByDefault() {
        QuickSwitchSettings settings = new QuickSwitchSettings();

        assertFalse(settings.isClosePreviousTab());
    }

    public void testLoadsPersistedClosePreference() {
        QuickSwitchSettings settings = new QuickSwitchSettings();
        QuickSwitchSettings.SettingsState state = new QuickSwitchSettings.SettingsState();
        state.closePreviousTab = true;

        settings.loadState(state);

        assertTrue(settings.isClosePreviousTab());
    }

    public void testUpdatesClosePreference() {
        QuickSwitchSettings settings = new QuickSwitchSettings();

        settings.setClosePreviousTab(true);

        assertTrue(settings.isClosePreviousTab());
        assertTrue(settings.getState().closePreviousTab);
    }
}
