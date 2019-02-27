package uk.ac.sanger.hcaprint;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;

/**
 * Clipboard helpers.
 * @author dr6
 */
public class Clip {
    /**
     * Gets the (plain string) contents of the clipboard.
     * @return the contents of the clipboard
     * @exception IOException the clipboard contents cannot be retrieved
     */
    public static String paste() throws IOException {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            throw new IOException("Clipboard could not be read.", e);
        }
    }
}
