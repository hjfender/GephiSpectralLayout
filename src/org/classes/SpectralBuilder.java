package org.classes;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;

/**
 * Created by henry on 4/27/17.
 */
public class SpectralBuilder implements LayoutBuilder {
    private final SpectralBuilder.SpectralLayoutUI ui = new SpectralBuilder.SpectralLayoutUI();

    public SpectralBuilder(){}

    public String getName() {
        return NbBundle.getMessage(SpectralBuilder.class, "name");
    }

    public SpectralLayout buildLayout() {
        return new SpectralLayout(this);
    }

    public LayoutUI getUI() {
        return this.ui;
    }

    private static class SpectralLayoutUI implements LayoutUI {
        private SpectralLayoutUI() {}

        public String getDescription() {
            return NbBundle.getMessage(SpectralLayout.class, "description");
        }

        public Icon getIcon() {
            return null;
        }

        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        public int getQualityRank() {
            return 5;
        }

        public int getSpeedRank() {
            return 3;
        }
    }

}
