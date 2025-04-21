package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();

    public MainApplicationFrame() {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);
        setContentPane(desktopPane);

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);
        
        RobotModel robot = new RobotModel();
        GameWindow gameWindow = new GameWindow(robot);
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);
        
        RobotCoordWindow coordWindow = new RobotCoordWindow(robot);
        addWindow(coordWindow);
        
        loadWindowStates();
        
        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndExit();
            }
        });
    }

    // Метод для создания окна лога (должен быть protected)
    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    // Метод для добавления окон (должен принимать JInternalFrame)
    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private void confirmAndExit() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Вы действительно хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
        	saveWindowStates();
            System.exit(0);
        }
    }

    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("Выход");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem exitItem = new JMenuItem("Выход", KeyEvent.VK_X);
        exitItem.addActionListener(e -> confirmAndExit());
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription("Управление режимом отображения приложения");

        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(systemLookAndFeel);

        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
        crossplatformLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(crossplatformLookAndFeel);

        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription("Тестовые команды");

        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> {
            Logger.debug("Новая строка");
        });
        testMenu.add(addLogMessageItem);

        menuBar.add(lookAndFeelMenu);
        menuBar.add(testMenu);
        return menuBar;
    }

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Игнорируем ошибку
        }
    }
    
    private void saveWindowStates() {
        Map<String, Map<String, Object>> states = new HashMap<>();
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof Saveable) {
                Saveable saveable = (Saveable) frame;
                Map<String, Object> state = new HashMap<>();
                saveable.saveState(state);
                states.put(saveable.getIdentifier(), state);
            }
        }
        File configFile = new File(System.getProperty("user.home"), "app_config.ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(configFile))) {
            oos.writeObject(states);
        } catch (IOException e) {
            Logger.error("Ошибка сохранения: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadWindowStates() {
    	File configFile = new File(System.getProperty("user.home"), "app_config.ser");
        Logger.debug("Путь к конфигу: " + configFile.getAbsolutePath());
        
        if (!configFile.exists()) {
            Logger.debug("Файл конфигурации не найден");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(configFile))) {
            Map<String, Map<String, Object>> states = (Map<String, Map<String, Object>>) ois.readObject();
            Logger.debug("Загружено состояний: " + states.size());
            
            for (JInternalFrame frame : desktopPane.getAllFrames()) {
                if (frame instanceof Saveable) {
                    Saveable saveable = (Saveable) frame;
                    Map<String, Object> state = states.get(saveable.getIdentifier());
                    if (state != null) {
                        Logger.debug("Загрузка окна: " + saveable.getIdentifier());
                        saveable.loadState(state);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            Logger.error("Ошибка загрузки: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}