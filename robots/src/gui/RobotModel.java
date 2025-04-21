package gui;

import java.util.ArrayList;
import java.util.List;

public class RobotModel {
    private volatile double robotPositionX = 100;
    private volatile double robotPositionY = 100;
    private volatile double robotDirection = 0;
    private volatile int targetPositionX = 150;
    private volatile int targetPositionY = 100;
    
    private static final double MAX_VELOCITY = 0.1;
    private static final double MAX_ANGULAR_VELOCITY = 0.001;
    
    private final List<RobotModelObserver> observers = new ArrayList<>();

    public void update() {
        double distance = distance(targetPositionX, targetPositionY, robotPositionX, robotPositionY);
        if (distance < 0.5) {
            return;
        }
        
        double angleToTarget = angleTo(robotPositionX, robotPositionY, targetPositionX, targetPositionY);
        double angularVelocity = calculateAngularVelocity(angleToTarget);
        
        moveRobot(MAX_VELOCITY, angularVelocity, 10);
    }

    private double calculateAngularVelocity(double angleToTarget) {
        double angleDiff = asNormalizedRadians(angleToTarget - robotDirection);
        return angleDiff > Math.PI ? -MAX_ANGULAR_VELOCITY : MAX_ANGULAR_VELOCITY;
    }

    private void moveRobot(double velocity, double angularVelocity, double duration) {
        velocity = applyLimits(velocity, 0, MAX_VELOCITY);
        angularVelocity = applyLimits(angularVelocity, -MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY);
        
        double newX = robotPositionX + velocity / angularVelocity * 
            (Math.sin(robotDirection + angularVelocity * duration) - Math.sin(robotDirection));
        double newY = robotPositionY - velocity / angularVelocity * 
            (Math.cos(robotDirection + angularVelocity * duration) - Math.cos(robotDirection));
        
        if (Double.isFinite(newX) && Double.isFinite(newY)) {
            robotPositionX = newX;
            robotPositionY = newY;
        } else {
            robotPositionX += velocity * duration * Math.cos(robotDirection);
            robotPositionY += velocity * duration * Math.sin(robotDirection);
        }
        
        robotDirection = asNormalizedRadians(robotDirection + angularVelocity * duration);
        
        notifyObservers();
    }

    // Вспомогательные методы
    private static double distance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        return asNormalizedRadians(Math.atan2(dy, dx));
    }

    private static double applyLimits(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double asNormalizedRadians(double angle) {
        return (angle + 2 * Math.PI) % (2 * Math.PI);
    }

    // Геттеры и сеттеры
    public void setTargetPosition(int x, int y) {
        targetPositionX = x;
        targetPositionY = y;
    }

    public double getRobotX() { return robotPositionX; }
    public double getRobotY() { return robotPositionY; }
    public double getDirection() { return robotDirection; }
    public int getTargetX() { return targetPositionX; }
    public int getTargetY() { return targetPositionY; }
    
    public interface RobotModelObserver {
        void update(double x, double y, double direction);
    }

    public void addObserver(RobotModelObserver o) {
        observers.add(o);
    }

    private void notifyObservers() {
        for (RobotModelObserver o : observers) {
            o.update(robotPositionX, robotPositionY, robotDirection);
        }
    }
}