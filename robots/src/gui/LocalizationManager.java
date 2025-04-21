package gui;

import java.util.Locale;
import java.util.ResourceBundle;

@SuppressWarnings("deprecation")
public class LocalizationManager {
    private static ResourceBundle bundle;
    private static Locale currentLocale;

    static {
        setLocale(new Locale("ru", "RU"));
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    public static String getString(String key) {
        return bundle.getString(key);
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }
}