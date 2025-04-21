package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class GameVisualizer extends JPanel {
    private RobotModel model;
    private final Timer timer = initTimer();

    private static Timer initTimer() {
        return new Timer("events generator", true);
    }

    public GameVisualizer(RobotModel p_model) {
    	this.model = p_model;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(GameVisualizer.this::repaint);
            }
        }, 0, 50);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                model.update();
            }
        }, 0, 10);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                model.setTargetPosition(e.getPoint().x, e.getPoint().y);
                repaint();
            }
        });
        setDoubleBuffered(true);
    }
    
    public GameVisualizer() {
    	model = new RobotModel();
    	
    	timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(GameVisualizer.this::repaint);
            }
        }, 0, 50);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                model.update();
            }
        }, 0, 10);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                model.setTargetPosition(e.getPoint().x, e.getPoint().y);
                repaint();
            }
        });
        setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        drawRobot(g2d);
        drawTarget(g2d);
    }

    private void drawRobot(Graphics2D g) {
        int robotX = (int) Math.round(model.getRobotX());
        int robotY = (int) Math.round(model.getRobotY());
        
        AffineTransform t = AffineTransform.getRotateInstance(
            model.getDirection(), robotX, robotY
        );
        g.setTransform(t);
        
        g.setColor(Color.MAGENTA);
        fillOval(g, robotX, robotY, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, robotX, robotY, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, robotX + 10, robotY, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, robotX + 10, robotY, 5, 5);
    }

    private void drawTarget(Graphics2D g) {
        g.setTransform(new AffineTransform());
        g.setColor(Color.GREEN);
        fillOval(g, model.getTargetX(), model.getTargetY(), 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, model.getTargetX(), model.getTargetY(), 5, 5);
    }

    private static void fillOval(Graphics g, int x, int y, int w, int h) {
        g.fillOval(x - w/2, y - h/2, w, h);
    }

    private static void drawOval(Graphics g, int x, int y, int w, int h) {
        g.drawOval(x - w/2, y - h/2, w, h);
    }
}
