package uk.ac.sanger.hcaprint;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.function.Function;

/**
 * A generic table model that uses specified functions to extract values.
 * The items are specified via {@link #setItems}.
 * @param <E> the class representing a row in this table
 * @author dr6
 */
public class FunctionalTableModel<E> extends AbstractTableModel {
    private List<E> items;
    private String[] headings;
    private Function<? super E, ?>[] valueFunctions;
    private Class[] columnClasses;
    private int indexColumn = -1;

    @SafeVarargs
    public FunctionalTableModel(Function<? super E, ?>... valueFunctions) {
        this.valueFunctions = valueFunctions.clone();
    }

    /**
     * Specify what items are shown in this table.
     * @param items the items to show in this table.
     */
    public void setItems(List<E> items) {
        this.items = items;
        fireTableDataChanged();
    }

    public void setHeadings(String... headings) {
        this.headings = headings.clone();
    }

    public void setColumnClasses(Class[] columnClasses) {
        this.columnClasses = columnClasses.clone();
    }

    public void setIndexColumn(int indexColumn) {
        this.indexColumn = indexColumn;
    }

    /**
     * Gets the row at the given index.
     * @exception IndexOutOfBoundsException if the index is negative or {@code >= getRowCount()}
     */
    public E getRow(int index) {
        return this.items.get(index);
    }

    @Override
    public int getRowCount() {
        return (items==null ? 0 : items.size());
    }

    @Override
    public int getColumnCount() {
        return this.valueFunctions.length;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex==indexColumn) {
            return Integer.class;
        }
        return safeIndex(columnClasses, columnIndex, Object.class);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return safeIndex(headings, columnIndex, null);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (indexColumn>=0 && columnIndex==indexColumn) {
            return rowIndex+1;
        }
        if (rowIndex >= 0 && rowIndex < getRowCount()) {
            E row = getRow(rowIndex);
            Function<? super E, ?> function = safeIndex(valueFunctions, columnIndex, null);
            if (row!=null && function!=null) {
                return function.apply(row);
            }
        }
        return null;
    }

    private static <E> E safeIndex(E[] arr, int index, E defaultValue) {
        if (arr!=null && index>=0 && index<arr.length && arr[index]!=null) {
            return arr[index];
        }
        return defaultValue;
    }
}
