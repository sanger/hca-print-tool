package uk.ac.sanger.hcaprint;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Panel for showing the application's config (in a modal dialog)
 * @author dr6
 */
public class ConfigPanel extends JPanel {
    private Properties properties;
    private JButton okButton;
    private JTable table;
    private JDialog dialog;

    public ConfigPanel(Properties properties) {
        this.properties = properties;
        initComponents();
        layOutComponents();
    }

    private void initComponents() {
        table = new JTable(makeTableModel(properties));
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, table.getFont().getSize()));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        sizeColumns(table);
        okButton = new JButton("OK");
        okButton.addActionListener(e -> okPressed());
    }

    private static TableModel makeTableModel(Map<?, ?> map) {
        List<Map.Entry<?,?>> mapEntries = new ArrayList<>(map.entrySet());
        mapEntries.sort(Comparator.comparing(e -> e.getKey().toString()));
        FunctionalTableModel<Map.Entry<?, ?>> tableModel = new FunctionalTableModel<>(Map.Entry::getKey, Map.Entry::getValue);
        tableModel.setHeadings("Property", "Value");
        tableModel.setItems(mapEntries);
        return tableModel;
    }

    private void sizeColumns(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        final int numRows = table.getRowCount();
        for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); ++columnIndex) {
            int width = 15;
            for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
                TableCellRenderer renderer = table.getCellRenderer(rowIndex, columnIndex);
                Component comp = table.prepareRenderer(renderer, rowIndex, columnIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }
            width += 5;
            columnModel.getColumn(columnIndex).setPreferredWidth(width);
        }
    }

    private void layOutComponents() {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        this.setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bp = new JPanel();
        bp.add(okButton);
        add(bp, BorderLayout.SOUTH);
    }

    private void okPressed() {
        if (dialog!=null) {
            dialog.dispose();
        }
    }

    public void showDialog(Frame owner) {
        this.dialog = new JDialog(owner, "Config", true);
        this.dialog.setContentPane(this);
        this.dialog.pack();
        this.dialog.setLocationRelativeTo(owner);
        this.dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.dialog.setVisible(true);
    }
}
