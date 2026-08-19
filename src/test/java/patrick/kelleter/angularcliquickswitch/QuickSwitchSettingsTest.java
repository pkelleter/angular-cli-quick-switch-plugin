package patrick.kelleter.angularcliquickswitch;

import junit.framework.TestCase;

public final class QuickSwitchSettingsTest extends TestCase {
    public void testKeepsTabsOpenByDefault() {
        QuickSwitchSettings settings = new QuickSwitchSettings();

        assertFalse(settings.isClosePreviousTab());
    }

    public void testUsesFileTypeDefaults() {
        QuickSwitchSettings settings = new QuickSwitchSettings();

        for (QuickSwitchFileType fileType : QuickSwitchFileType.values()) {
            assertEquals(fileType.suffix(), fileType.enabledByDefault(), settings.isFileTypeEnabled(fileType));
        }
    }

    public void testCustomSuffixesAreDisabledAndEmptyByDefault() {
        QuickSwitchSettings settings = new QuickSwitchSettings();

        for (QuickSwitchFileType.Category category : QuickSwitchFileType.Category.values()) {
            assertFalse(settings.isCustomSuffixEnabled(category));
            assertEquals("", settings.getCustomSuffix(category));
        }
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
        settings.setFileTypeEnabled(QuickSwitchFileType.SCSS, false);

        settings.setClosePreviousTab(true);

        assertTrue(settings.isClosePreviousTab());
        assertTrue(settings.getState().closePreviousTab);
        assertFalse(settings.isFileTypeEnabled(QuickSwitchFileType.SCSS));
    }

    public void testPersistsFileTypeOverrides() {
        QuickSwitchSettings settings = new QuickSwitchSettings();

        settings.setFileTypeEnabled(QuickSwitchFileType.SCSS, false);
        settings.setFileTypeEnabled(QuickSwitchFileType.TYPESCRIPT_SPEC, true);

        assertFalse(settings.isFileTypeEnabled(QuickSwitchFileType.SCSS));
        assertTrue(settings.isFileTypeEnabled(QuickSwitchFileType.TYPESCRIPT_SPEC));
        assertTrue(settings.getState().disabledFileTypes.contains(QuickSwitchFileType.SCSS.name()));
        assertTrue(settings.getState().enabledOptInFileTypes.contains(QuickSwitchFileType.TYPESCRIPT_SPEC.name()));
    }

    public void testLoadsPersistedFileTypeOverrides() {
        QuickSwitchSettings settings = new QuickSwitchSettings();
        QuickSwitchSettings.SettingsState state = new QuickSwitchSettings.SettingsState();
        state.disabledFileTypes.add(QuickSwitchFileType.HTML.name());
        state.enabledOptInFileTypes.add(QuickSwitchFileType.JAVASCRIPT_SPEC.name());

        settings.loadState(state);

        assertFalse(settings.isFileTypeEnabled(QuickSwitchFileType.HTML));
        assertTrue(settings.isFileTypeEnabled(QuickSwitchFileType.JAVASCRIPT_SPEC));
    }

    public void testPersistsCustomSuffix() {
        QuickSwitchSettings settings = new QuickSwitchSettings();

        settings.setCustomSuffix(QuickSwitchFileType.Category.CONTROLLER, "tsx", true);

        assertTrue(settings.isCustomSuffixEnabled(QuickSwitchFileType.Category.CONTROLLER));
        assertEquals("tsx", settings.getCustomSuffix(QuickSwitchFileType.Category.CONTROLLER));
        assertEquals(
            "tsx",
            settings.getState().customSuffixes.get(QuickSwitchFileType.Category.CONTROLLER.name())
        );
    }

    public void testDisabledCustomSuffixRetainsItsValue() {
        QuickSwitchSettings settings = new QuickSwitchSettings();
        settings.setCustomSuffix(QuickSwitchFileType.Category.STYLE, "pcss", true);

        settings.setCustomSuffix(QuickSwitchFileType.Category.STYLE, "pcss", false);

        assertFalse(settings.isCustomSuffixEnabled(QuickSwitchFileType.Category.STYLE));
        assertEquals("pcss", settings.getCustomSuffix(QuickSwitchFileType.Category.STYLE));
    }
}
