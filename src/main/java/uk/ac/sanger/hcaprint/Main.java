package uk.ac.sanger.hcaprint;

import javax.swing.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * @author dr6
 */
public class Main {
    public static void main(String[] args) {
        final Properties props = loadProperties("config.properties");
        if (props==null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame(props);
            frame.setBounds(100, 100, 500, 500);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

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

    private static List<String> loadFileData(String filename) {
        try {
            URL url = Main.class.getClassLoader().getResource(filename);
            if (url!=null) {
                return Files.readAllLines(Paths.get(url.toURI()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "Could not load file", "Error",
                JOptionPane.ERROR_MESSAGE);
        return null;
    }
}
