package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.beans.PropertyVetoException;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RobotCoordWindow extends JInternalFrame 
        implements Saveable, RobotModel.RobotModelObserver, LocalizedWindow {
    private final JLabel xLabel = new JLabel("0.00");
    private final JLabel yLabel = new JLabel("0.00");
    private final JLabel dirLabel = new JLabel("0.00°");

    public RobotCoordWindow(RobotModel model) {
        super("Координаты робота", true, true, true, true);
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("X:"));
        panel.add(xLabel);
        panel.add(new JLabel("Y:"));
        panel.add(yLabel);
        panel.add(new JLabel("Направление:"));
        panel.add(dirLabel);
        getContentPane().add(panel, BorderLayout.CENTER);
        model.addObserver(this);
        pack();
    }

    @Override
    public void update(double x, double y, double direction) {
        SwingUtilities.invokeLater(() -> {
            xLabel.setText(String.format("%.2f", x));
            yLabel.setText(String.format("%.2f", y));
            double degrees = Math.toDegrees(direction);
            dirLabel.setText(String.format("%.2f°", degrees));
        });
    }

    @Override
    public String getIdentifier() {
        return "coordWindow";
    }

    @Override
    public void saveState(Map<String, Object> state) {
        state.put("x", this.getX());
        state.put("y", this.getY());
        state.put("width", this.getWidth());
        state.put("height", this.getHeight());
        state.put("isIcon", this.isIcon());
        state.put("isMaximum", this.isMaximum());
    }

    @Override
    public void loadState(Map<String, Object> state) {
        if (state.containsKey("x") && state.containsKey("y") 
            && state.containsKey("width") && state.containsKey("height")) {
            int x = (int) state.get("x");
            int y = (int) state.get("y");
            int width = (int) state.get("width");
            int height = (int) state.get("height");
            setBounds(x, y, width, height);
        }
        
        if (state.containsKey("isIcon")) {
            boolean isIcon = (boolean) state.get("isIcon");
            try {
                this.setIcon(isIcon);
            } catch (PropertyVetoException ignored) {}
        }
        
        if (state.containsKey("isMaximum")) {
            boolean isMaximum = (boolean) state.get("isMaximum");
            try {
                this.setMaximum(isMaximum);
            } catch (PropertyVetoException ignored) {}
        }
    }
    
    @Override
    public void updateLocalization() {
        setTitle(LocalizationManager.getString("window.coords"));
        SwingUtilities.invokeLater(() -> {
            Component[] components = ((JPanel)getContentPane().getComponent(0)).getComponents();
            ((JLabel)components[0]).setText(LocalizationManager.getString("label.x"));
            ((JLabel)components[2]).setText(LocalizationManager.getString("label.y"));
            ((JLabel)components[4]).setText(LocalizationManager.getString("label.direction"));
        });
    }
}