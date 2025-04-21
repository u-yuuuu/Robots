package gui;

import java.util.Map;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

public class GameWindow extends JInternalFrame implements Saveable
{
    private final GameVisualizer m_visualizer;
    public GameWindow() 
    {
        super("Игровое поле", true, true, true, true);
        m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
    }
    @Override
    public String getIdentifier() {
        return "gameWindow";
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
        if (state.containsKey("x") && state.containsKey("y")) {
            this.setLocation((int) state.get("x"), (int) state.get("y"));
        }
        if (state.containsKey("width") && state.containsKey("height")) {
        	this.setSize((int) state.get("width"), (int) state.get("height"));
        }
        if (state.containsKey("isIcon")) {
            try {
            	this.setIcon((boolean) state.get("isIcon"));
            } catch (PropertyVetoException ignored) {}
        }
        if (state.containsKey("isMaximum")) {
            try {
            	this.setMaximum((boolean) state.get("isMaximum"));
            } catch (PropertyVetoException ignored) {}
        }
    }
}
