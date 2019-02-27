package uk.ac.sanger.hcaprint;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * A table model with two columns: index and item (a string).
 * The items are specified via {@link #setItems}
 * @author dr6
 */
public class SimpleTableModel extends AbstractTableModel {
    public static final int INDEX_COL = 0, ITEM_COL = 1;

    private List<String> items;

    /**
     * Specify what items are shown in this table.
     * @param items the items to show in this table.
     */
    public void setItems(List<String> items) {
        this.items = items;
        fireTableDataChanged();
    }

    /**
     * Gets the item at the given index.
     * @exception IndexOutOfBoundsException if the index is negative or {@code >= getRowCount()}
     */
    public String getItem(int index) {
        return this.items.get(index);
    }

    @Override
    public int getRowCount() {
        return (items==null ? 0 : items.size());
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case INDEX_COL: return Integer.class;
            case ITEM_COL: return String.class;
        }
        return Object.class;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case INDEX_COL: return "#";
            case ITEM_COL: return "Text";
        }
        return null;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case INDEX_COL: return rowIndex + 1;
            case ITEM_COL:
                if (rowIndex >= 0 && rowIndex < getRowCount()) {
                    return items.get(rowIndex);
                }
                break;
        }
        return null;
    }
}
