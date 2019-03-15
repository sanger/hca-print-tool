package uk.ac.sanger.hcaprint;

import java.util.Arrays;

/**
 * @author dr6
 */
public class LabelData {
    private String name;
    private String date;

    public LabelData(String name, String date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return this.name;
    }

    public String getDate() {
        return this.date;
    }

    public String getBarcode() {
        return this.name;
    }

    private static StringBuilder quote(StringBuilder sb, String text) {
        if (text==null) {
            sb.append("null");
        } else {
            sb.append('"').append(text).append('"');
        }
        return sb;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("LabelData(");
        quote(sb, name);
        sb.append(", ");
        quote(sb, date);
        sb.append(")");
        return sb.toString();
    }

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
