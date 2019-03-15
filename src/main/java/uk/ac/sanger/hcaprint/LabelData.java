package uk.ac.sanger.hcaprint;

import java.util.Arrays;

/**
 * A class representing the information for one label.
 * As it happens, the values for the {@link #getBarcode barcode}
 * and the {@link #getName name} are the same.
 * @author dr6
 */
public class LabelData {
    private String name;
    private String date;

    /**
     * Constructs an instance with the given values.
     * @param name the name (which is also the barcode)
     * @param date the date (treated just as a string)
     */
    public LabelData(String name, String date) {
        this.name = name;
        this.date = date;
    }

    /**
     * Gets the name value for this label
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the date value for this label
     */
    public String getDate() {
        return this.date;
    }

    /**
     * Gets the barcode value for this label
     */
    public String getBarcode() {
        return this.name;
    }

    /**
     * Helper method: add a string to a stringbuilder.
     * If the string is null, add {@code null}.
     * Otherwise, add the string with quotemarks around it.
     * @param sb a stringbuilder
     * @param text a string (or null)
     * @return the same stringbuilder
     */
    private static StringBuilder quote(StringBuilder sb, String text) {
        if (text==null) {
            sb.append("null");
        } else {
            sb.append('"').append(text).append('"');
        }
        return sb;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LabelData(");
        quote(sb, name);
        sb.append(", ");
        quote(sb, date);
        sb.append(")");
        return sb.toString();
    }

    /**
     * Creates an instance of {@code LabelData} from the given string.
     * The string should contain two values separated by a tab character.
     * The first is the name/barcode. The second is the date.
     * The values are trimmed. If there are not two values, null may be used in the place of one.
     * If we cannot make sense of the given line, null will be returned.
     * @param line the string containing the label information
     * @return a new instance of {@code LabelData}, or null if the the line could not be understood
     */
    public static LabelData fromLine(String line) {
        String[] parts = line.split("\t");
        if (parts.length==0) {
            return null;
        }
        if (parts.length==1) {
            String name = parts[0].trim();
            if (name.isEmpty()) {
                return null;
            }
            return new LabelData(name, null);
        }
        if (parts.length==2) {
            return new LabelData(parts[0].trim(), parts[1].trim());
        }
        String[] nonEmptyParts = Arrays.stream(parts)
                .map(String::trim)
                .toArray(String[]::new);
        if (nonEmptyParts.length==1) {
            return new LabelData(nonEmptyParts[0], null);
        }
        if (nonEmptyParts.length==2) {
            return new LabelData(nonEmptyParts[0], nonEmptyParts[1]);
        }
        return null;
    }
}
