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

import java.util.Locale;
import java.util.ResourceBundle;
import gui.LocalizationManager;

import log.Logger;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private JMenuBar menuBar;
    
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
        
        updateLocalization();
        
        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndExit();
            }
        });
    }
    
    private void updateLocalization() {
        SwingUtilities.updateComponentTreeUI(this);
        updateWindowTitles();
        regenerateMenuBar();
    }
    
    private void regenerateMenuBar() {
        if (menuBar != null) {
            setJMenuBar(generateMenuBar());
            revalidate();
            repaint();
        }
    }
    
    private void updateWindowTitles() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof LocalizedWindow) {
                ((LocalizedWindow) frame).updateLocalization();
            }
        }
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
                LocalizationManager.getString("exit.confirm"),
                LocalizationManager.getString("exit.title"),
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            saveWindowStates();
            System.exit(0);
        }
    }

    private JMenuBar generateMenuBar() {
        menuBar = new JMenuBar();
        
        // Меню Файл
        JMenu fileMenu = new JMenu(LocalizationManager.getString("menu.file"));
        JMenuItem exitItem = new JMenuItem(LocalizationManager.getString("menu.exit"));
        exitItem.addActionListener(e -> confirmAndExit());
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Меню Вид
        JMenu viewMenu = new JMenu(LocalizationManager.getString("menu.view"));
        
        // Подменю Режим отображения
        JMenu lookAndFeelMenu = new JMenu(LocalizationManager.getString("menu.lookandfeel"));
        JMenuItem systemLookAndFeel = new JMenuItem(LocalizationManager.getString("menu.system_theme"));
        systemLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            invalidate();
        });
        lookAndFeelMenu.add(systemLookAndFeel);

        JMenuItem crossplatformLookAndFeel = new JMenuItem(LocalizationManager.getString("menu.crossplatform_theme"));
        crossplatformLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            invalidate();
        });
        lookAndFeelMenu.add(crossplatformLookAndFeel);
        viewMenu.add(lookAndFeelMenu);

        // Подменю Язык
        JMenu languageMenu = new JMenu(LocalizationManager.getString("menu.language"));
        JMenuItem russianLang = new JMenuItem(LocalizationManager.getString("menu.lang_russian"));
        russianLang.addActionListener(e -> {
            LocalizationManager.setLocale(new Locale("ru", "RU"));
            updateLocalization();
        });
        languageMenu.add(russianLang);

        JMenuItem englishLang = new JMenuItem(LocalizationManager.getString("menu.lang_english"));
        englishLang.addActionListener(e -> {
            LocalizationManager.setLocale(new Locale("en", "US"));
            updateLocalization();
        });
        languageMenu.add(englishLang);
        viewMenu.add(languageMenu);

        menuBar.add(viewMenu);

        // Меню Тесты
        JMenu testMenu = new JMenu(LocalizationManager.getString("menu.tests"));
        JMenuItem addLogMessageItem = new JMenuItem(LocalizationManager.getString("menu.test_log"));
        addLogMessageItem.addActionListener((event) -> Logger.debug("New log message"));
        testMenu.add(addLogMessageItem);
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