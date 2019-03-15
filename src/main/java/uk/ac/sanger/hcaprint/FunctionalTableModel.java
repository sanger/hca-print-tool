package uk.ac.sanger.hcaprint;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A generic table model that uses specified functions to extract values.
 * The items are specified via {@link #setItems}.
 * @param <E> the class representing a row in this table
 * @author dr6
 */
public class FunctionalTableModel<E> extends AbstractTableModel {
    private List<E> items = Collections.emptyList();
    private String[] headings;
    private Function<? super E, ?>[] valueFunctions;
    private Class[] columnClasses;
    private int indexColumn = -1;

    /**
     * Constructs a model that will use the specified functions as value specifies for its columns.
     * The number of columns in this model is determined by the size of the received array.
     * The array is cloned and saved.
     * @param valueFunctions functions that give values for the respective columns in the table
     */
    @SafeVarargs
    public FunctionalTableModel(Function<? super E, ?>... valueFunctions) {
        this.valueFunctions = valueFunctions.clone();
    }

    /**
     * Specify what items are shown in this table.
     * An argument of null is treated like an empty list.
     * @param items the items to show in this table.
     */
    public void setItems(List<E> items) {
        this.items = (items==null ? Collections.emptyList() : items);
        fireTableDataChanged();
    }

    /**
     * Sets the headings for the table.
     * The number of headings will usually be the same as the number of columns,
     * but you are permitted to underspecify the column headings. They will simply be missing
     * in the rendered table.
     * @param headings the respective column headings
     */
    public void setHeadings(String... headings) {
        this.headings = headings.clone();
    }

    /**
     * Specifies the classes of the respective columns in the table. This is completely optional.
     * Any column without a specified class will have the default column class, {@code Object.class}.
     * @param columnClasses an array of classes
     */
    public void setColumnClasses(Class[] columnClasses) {
        this.columnClasses = columnClasses.clone();
    }

    /**
     * Sets a column to be an index column.
     * An index column has column-class {@code Integer.class}, and the value for each
     * row will just be the number for that row, starting from 1.
     * If {@code indexColumn} is negative, there will be no index column in the table.
     * @param indexColumn the index of the column that should be the index column
     */
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
        return items.size();
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

    /**
     * Helper method: gets an entry from an array, or a default value if such an entry is not available.
     * If {@code arr} is not null, {@code index} is inside its bounds, and {@code arr[index]} is not null,
     * then {@code arr[index]} will be returned. Otherwise, the default will be returned.
     * @param arr an array (may be null)
     * @param index an index (may be out of bounds)
     * @param defaultValue a default value to return (may be null)
     * @param <E> the type of the array
     * @return the element from the array, or the given default value
     */
    private static <E> E safeIndex(E[] arr, int index, E defaultValue) {
        if (arr!=null && index>=0 && index<arr.length && arr[index]!=null) {
            return arr[index];
        }
        return defaultValue;
    }
}
