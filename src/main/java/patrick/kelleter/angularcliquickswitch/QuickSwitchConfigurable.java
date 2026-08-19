package patrick.kelleter.angularcliquickswitch;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class QuickSwitchConfigurable implements Configurable {
    private JCheckBox closePreviousTabCheckBox;
    private Map<QuickSwitchFileType, JCheckBox> fileTypeCheckBoxes;
    private Map<QuickSwitchFileType.Category, JCheckBox> customEnabledCheckBoxes;
    private Map<QuickSwitchFileType.Category, JTextField> customSuffixFields;

    @Override
    public @Nls String getDisplayName() {
        return "Angular CLI QuickSwitch";
    }

    @Override
    public @NotNull JComponent createComponent() {
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));

        JPanel generalPanel = createSectionPanel("General");
        closePreviousTabCheckBox = new JCheckBox("Close the previous tab after switching");
        closePreviousTabCheckBox.setToolTipText(
            "The target opens first; IntelliJ may also close copies of the previous file in other editor splits."
        );
        generalPanel.add(closePreviousTabCheckBox);
        allowHorizontalStretch(generalPanel);
        rootPanel.add(generalPanel);
        rootPanel.add(Box.createVerticalStrut(12));

        fileTypeCheckBoxes = new EnumMap<>(QuickSwitchFileType.class);
        customEnabledCheckBoxes = new EnumMap<>(QuickSwitchFileType.Category.class);
        customSuffixFields = new EnumMap<>(QuickSwitchFileType.Category.class);
        JPanel extensionsPanel = createSectionPanel("Supported Extensions");
        JPanel categoriesPanel = new JPanel(new GridLayout(
            1,
            QuickSwitchFileType.Category.values().length,
            24,
            0
        ));
        for (QuickSwitchFileType.Category category : QuickSwitchFileType.Category.values()) {
            JPanel categoryPanel = new JPanel();
            categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
            JLabel categoryLabel = new JLabel(category.displayName());
            categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.BOLD));
            categoryPanel.add(categoryLabel);
            categoryPanel.add(Box.createVerticalStrut(3));

            JPanel extensionListPanel = new JPanel();
            extensionListPanel.setLayout(new BoxLayout(extensionListPanel, BoxLayout.Y_AXIS));

            for (QuickSwitchFileType fileType : QuickSwitchFileType.values()) {
                if (fileType.category() == category) {
                    JCheckBox checkBox = new JCheckBox("." + fileType.suffix());
                    checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                    fileTypeCheckBoxes.put(fileType, checkBox);
                    extensionListPanel.add(checkBox);
                }
            }

            extensionListPanel.add(Box.createVerticalStrut(4));
            JPanel customPanel = new JPanel();
            customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.X_AXIS));
            customPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JCheckBox customEnabledCheckBox = new JCheckBox();
            customEnabledCheckBox.getAccessibleContext().setAccessibleName(
                "Enable custom " + category.displayName().toLowerCase(Locale.ROOT) + " suffix"
            );
            JBTextField customSuffixField = new JBTextField(7);
            customSuffixField.getEmptyText().setText(".custom");
            customSuffixField.setToolTipText("Enter a suffix such as tsx or component.html");
            customEnabledCheckBox.addActionListener(event ->
                customSuffixField.setEnabled(customEnabledCheckBox.isSelected()));
            customEnabledCheckBoxes.put(category, customEnabledCheckBox);
            customSuffixFields.put(category, customSuffixField);
            customPanel.add(customEnabledCheckBox);
            customPanel.add(customSuffixField);
            customPanel.setMaximumSize(customPanel.getPreferredSize());
            extensionListPanel.add(customPanel);
            extensionListPanel.setMaximumSize(extensionListPanel.getPreferredSize());
            categoryPanel.add(extensionListPanel);

            categoriesPanel.add(categoryPanel);
        }

        allowHorizontalStretch(categoriesPanel);
        extensionsPanel.add(categoriesPanel);
        allowHorizontalStretch(extensionsPanel);
        rootPanel.add(extensionsPanel);
        rootPanel.add(Box.createVerticalGlue());
        return rootPanel;
    }

    @Override
    public boolean isModified() {
        QuickSwitchSettings settings = QuickSwitchSettings.getInstance();
        if (closePreviousTabCheckBox != null
            && Configurable.isCheckboxModified(closePreviousTabCheckBox, settings.isClosePreviousTab())) {
            return true;
        }

        if (fileTypeCheckBoxes != null && fileTypeCheckBoxes.entrySet().stream()
            .anyMatch(entry -> Configurable.isCheckboxModified(
                entry.getValue(),
                settings.isFileTypeEnabled(entry.getKey())
            ))) {
            return true;
        }

        if (customEnabledCheckBoxes == null || customSuffixFields == null) {
            return false;
        }

        for (QuickSwitchFileType.Category category : QuickSwitchFileType.Category.values()) {
            if (Configurable.isCheckboxModified(
                customEnabledCheckBoxes.get(category),
                settings.isCustomSuffixEnabled(category)
            ) || !normalizeSuffix(customSuffixFields.get(category).getText())
                .equals(settings.getCustomSuffix(category))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        Map<QuickSwitchFileType.Category, String> customSuffixes = validateCustomSuffixes();
        QuickSwitchSettings settings = QuickSwitchSettings.getInstance();
        if (closePreviousTabCheckBox != null) {
            settings.setClosePreviousTab(closePreviousTabCheckBox.isSelected());
        }
        if (fileTypeCheckBoxes != null) {
            fileTypeCheckBoxes.forEach((fileType, checkBox) ->
                settings.setFileTypeEnabled(fileType, checkBox.isSelected()));
        }
        if (customEnabledCheckBoxes != null && customSuffixFields != null) {
            customSuffixes.forEach((category, suffix) -> {
                settings.setCustomSuffix(category, suffix, customEnabledCheckBoxes.get(category).isSelected());
                customSuffixFields.get(category).setText(suffix);
            });
        }
    }

    @Override
    public void reset() {
        QuickSwitchSettings settings = QuickSwitchSettings.getInstance();
        if (closePreviousTabCheckBox != null) {
            closePreviousTabCheckBox.setSelected(settings.isClosePreviousTab());
        }
        if (fileTypeCheckBoxes != null) {
            fileTypeCheckBoxes.forEach((fileType, checkBox) ->
                checkBox.setSelected(settings.isFileTypeEnabled(fileType)));
        }
        if (customEnabledCheckBoxes != null && customSuffixFields != null) {
            for (QuickSwitchFileType.Category category : QuickSwitchFileType.Category.values()) {
                boolean enabled = settings.isCustomSuffixEnabled(category);
                customEnabledCheckBoxes.get(category).setSelected(enabled);
                customSuffixFields.get(category).setText(settings.getCustomSuffix(category));
                customSuffixFields.get(category).setEnabled(enabled);
            }
        }
    }

    @Override
    public void disposeUIResources() {
        closePreviousTabCheckBox = null;
        fileTypeCheckBoxes = null;
        customEnabledCheckBoxes = null;
        customSuffixFields = null;
    }

    private static JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        Border titleBorder = BorderFactory.createTitledBorder(title);
        Border padding = BorderFactory.createEmptyBorder(8, 12, 10, 12);
        panel.setBorder(BorderFactory.createCompoundBorder(titleBorder, padding));
        return panel;
    }

    private static void allowHorizontalStretch(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
    }

    private Map<QuickSwitchFileType.Category, String> validateCustomSuffixes() throws ConfigurationException {
        Map<QuickSwitchFileType.Category, String> normalizedSuffixes =
            new EnumMap<>(QuickSwitchFileType.Category.class);
        Set<String> usedSuffixes = new HashSet<>();
        for (QuickSwitchFileType fileType : QuickSwitchFileType.values()) {
            usedSuffixes.add(fileType.suffix().toLowerCase(Locale.ROOT));
        }

        for (QuickSwitchFileType.Category category : QuickSwitchFileType.Category.values()) {
            String suffix = normalizeSuffix(customSuffixFields.get(category).getText());
            boolean enabled = customEnabledCheckBoxes.get(category).isSelected();
            if (enabled && suffix.isEmpty()) {
                throw new ConfigurationException(category.displayName() + " custom suffix cannot be empty.");
            }
            if (!suffix.isEmpty() && !suffix.matches("[a-z0-9_-]+(?:\\.[a-z0-9_-]+)*")) {
                throw new ConfigurationException(
                    category.displayName() + " custom suffix contains invalid characters."
                );
            }
            if (!suffix.isEmpty() && !usedSuffixes.add(suffix)) {
                throw new ConfigurationException("Custom suffix ." + suffix + " is already configured.");
            }
            normalizedSuffixes.put(category, suffix);
        }
        return normalizedSuffixes;
    }

    private static String normalizeSuffix(String value) {
        String suffix = value.trim().toLowerCase(Locale.ROOT);
        while (suffix.startsWith(".")) {
            suffix = suffix.substring(1);
        }
        return suffix;
    }
}
