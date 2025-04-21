package gui;

import java.util.Map;

public interface Saveable {
    String getIdentifier();
    void saveState(Map<String, Object> state);
    void loadState(Map<String, Object> state);
}