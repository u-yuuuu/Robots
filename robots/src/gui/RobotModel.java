// RobotModel.java
package gui;

import java.util.ArrayList;
import java.util.List;

public class RobotModel {
    private volatile double x;
    private volatile double y;
    private volatile double direction;
    private int targetX;
    private int targetY;
    private final List<RobotModelObserver> observers = new ArrayList<>();

    public interface RobotModelObserver {
        void update(double x, double y, double direction);
    }

    public void addObserver(RobotModelObserver o) {
        observers.add(o);
    }

    public void setTarget(int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public void update(double velocity, double angularVelocity, double duration) {
        // Логика движения
        direction += angularVelocity * duration;
        x += velocity * Math.cos(direction) * duration;
        y += velocity * Math.sin(direction) * duration;
        notifyObservers();
    }

    private void notifyObservers() {
        for (RobotModelObserver o : observers) {
            o.update(x, y, direction);
        }
    }

    // Геттеры для targetX, targetY
}