package patrick.kelleter.angularcliquickswitch;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public final class QuickSwitchConfigurable implements Configurable {
    private JCheckBox closePreviousTabCheckBox;

    @Override
    public @Nls String getDisplayName() {
        return "Angular CLI QuickSwitch";
    }

    @Override
    public @NotNull JComponent createComponent() {
        closePreviousTabCheckBox = new JCheckBox("Close the previous tab after switching");
        closePreviousTabCheckBox.setToolTipText(
            "The target opens first; IntelliJ may also close copies of the previous file in other editor splits."
        );
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(closePreviousTabCheckBox, BorderLayout.NORTH);
        return panel;
    }

    @Override
    public boolean isModified() {
        return closePreviousTabCheckBox != null
            && Configurable.isCheckboxModified(
                closePreviousTabCheckBox,
                QuickSwitchSettings.getInstance().isClosePreviousTab()
            );
    }

    @Override
    public void apply() {
        if (closePreviousTabCheckBox != null) {
            QuickSwitchSettings.getInstance().setClosePreviousTab(closePreviousTabCheckBox.isSelected());
        }
    }

    @Override
    public void reset() {
        if (closePreviousTabCheckBox != null) {
            closePreviousTabCheckBox.setSelected(QuickSwitchSettings.getInstance().isClosePreviousTab());
        }
    }

    @Override
    public void disposeUIResources() {
        closePreviousTabCheckBox = null;
    }
}
