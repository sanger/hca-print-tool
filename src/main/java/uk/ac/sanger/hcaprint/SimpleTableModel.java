package uk.ac.sanger.hcaprint;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author dr6
 */
public class SimpleTableModel extends AbstractTableModel {
    public static final int INDEX_COL = 0, VALUE_COL = 1;

    private List<String> items;

    public void setItems(List<String> items) {
        this.items = items;
        fireTableDataChanged();
    }

    public String getItem(int index) {
        return this.items.get(index);
    }

    public int getRowCount() {
        return (items==null ? 0 : items.size());
    }

    public int getColumnCount() {
        return 2;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case INDEX_COL: return Integer.class;
            case VALUE_COL: return String.class;
        }
        return Object.class;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case INDEX_COL: return "#";
            case VALUE_COL: return "Text";
        }
        return null;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case INDEX_COL: return rowIndex + 1;
            case VALUE_COL:
                if (rowIndex >= 0 && rowIndex < getRowCount()) {
                    return items.get(rowIndex);
                }
                break;
        }
        return null;
    }
}
