package uk.ac.sanger.hcaprint;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Map;

/**
 * A table model for viewing the contents of a map.
 * @author dr6
 */
public class MapTableModel<K, V> extends AbstractTableModel {
    private Map<? extends K, ? extends V> map;
    private List<? extends K> keys;
    private String[] headings;

    public MapTableModel(Map<? extends K, ? extends V> map, List<? extends K> keys,
                         String keyHeading, String valueHeading) {
        this.headings = new String[] { keyHeading, valueHeading };
        this.keys = keys;
        this.map = map;
    }

    @Override
    public int getRowCount() {
        return this.keys.size();
    }

    @Override
    public int getColumnCount() {
        return this.headings.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return headings[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount()) {
            return null;
        }
        K key = keys.get(rowIndex);
        if (columnIndex==0) {
            return key;
        }
        return map.get(key);
    }
}
