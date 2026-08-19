package patrick.kelleter.angularcliquickswitch;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;

public final class QuickSwitchConfigurable implements Configurable {
    private JCheckBox closePreviousTabCheckBox;
    private Map<QuickSwitchFileType, JCheckBox> fileTypeCheckBoxes;

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

            for (QuickSwitchFileType fileType : QuickSwitchFileType.values()) {
                if (fileType.category() == category) {
                    JCheckBox checkBox = new JCheckBox("." + fileType.suffix());
                    fileTypeCheckBoxes.put(fileType, checkBox);
                    categoryPanel.add(checkBox);
                }
            }

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

        return fileTypeCheckBoxes != null && fileTypeCheckBoxes.entrySet().stream()
            .anyMatch(entry -> Configurable.isCheckboxModified(
                entry.getValue(),
                settings.isFileTypeEnabled(entry.getKey())
            ));
    }

    @Override
    public void apply() {
        QuickSwitchSettings settings = QuickSwitchSettings.getInstance();
        if (closePreviousTabCheckBox != null) {
            settings.setClosePreviousTab(closePreviousTabCheckBox.isSelected());
        }
        if (fileTypeCheckBoxes != null) {
            fileTypeCheckBoxes.forEach((fileType, checkBox) ->
                settings.setFileTypeEnabled(fileType, checkBox.isSelected()));
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
    }

    @Override
    public void disposeUIResources() {
        closePreviousTabCheckBox = null;
        fileTypeCheckBoxes = null;
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
}
