package gui;

import java.util.Map;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.TextArea;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;

import java.beans.PropertyVetoException;




public class LogWindow extends JInternalFrame implements LogChangeListener, Saveable
{
    private LogWindowSource m_logSource;
    private TextArea m_logContent;

    public LogWindow(LogWindowSource logSource) 
    {
        super("Протокол работы", true, true, true, true);
        m_logSource = logSource;
        m_logSource.registerListener(this);
        m_logContent = new TextArea("");
        m_logContent.setSize(200, 500);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateLogContent();
    }

    private void updateLogContent()
    {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : m_logSource.all())
        {
            content.append(entry.getMessage()).append("\n");
        }
        m_logContent.setText(content.toString());
        m_logContent.invalidate();
    }
    
    @Override
    public void onLogChanged()
    {
        EventQueue.invokeLater(this::updateLogContent);
    }
    
    @Override
    public String getIdentifier() {
        return "logWindow";
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
        // Объединяем установку позиции и размера через setBounds
        if (state.containsKey("x") && state.containsKey("y") 
            && state.containsKey("width") && state.containsKey("height")) {
            int x = (int) state.get("x");
            int y = (int) state.get("y");
            int width = (int) state.get("width");
            int height = (int) state.get("height");
            setBounds(x, y, width, height);
            
        }
        
        // Применяем свернутое состояние
        if (state.containsKey("isIcon")) {
            boolean isIcon = (boolean) state.get("isIcon");
            try {
            	this.setIcon(isIcon);
                
            } catch (PropertyVetoException e) {
            
            }
        }
        
        // Применяем максимизацию
        if (state.containsKey("isMaximum")) {
            boolean isMaximum = (boolean) state.get("isMaximum");
            try {
            	this.setMaximum(isMaximum);
               
            } catch (PropertyVetoException e) {
               
            }
        }
    }
    
    
}
