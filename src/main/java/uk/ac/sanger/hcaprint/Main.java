package uk.ac.sanger.hcaprint;

import javax.swing.*;
import java.net.URL;
import java.util.Properties;

/**
 * Main entry point for the application.
 * Loads the properties from a file, and launches the {@link AppFrame}.
 * @author dr6
 */
public class Main {
    /**
     * Runs the application.
     */
    public static void main(String[] args) {
        final Properties props = loadProperties("config.properties");
        if (props==null) {
            return;
        }
        for (String arg : args) {
            int c = arg.indexOf('=');
            if (c > 0) {
                String key = arg.substring(0, c).trim();
                String value = arg.substring(c+1).trim();
                props.put(key, value);
            }
        }
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", props.getProperty("app_title", "HCA Print Tool"));
        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame(props);
            frame.setBounds(100, 100, 500, 500);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    /**
     * Loads a properties file from the named resource.
     * If the file cannot be read, an error message will be shown, and the method will return null.
     * @param filename filename to load
     * @return the properties loaded, or null if there was an error
     */
    private static Properties loadProperties(String filename) {
        try {
            URL url = Main.class.getClassLoader().getResource(filename);
            if (url!=null) {
                Properties props = new Properties();
                props.load(url.openStream());
                return props;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "Could not load file", "Error",
                JOptionPane.ERROR_MESSAGE);
        return null;
    }
}
