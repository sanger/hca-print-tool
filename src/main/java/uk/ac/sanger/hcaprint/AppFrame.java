package uk.ac.sanger.hcaprint;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author dr6
 */
public class AppFrame extends JFrame {
    private JTable table;
    private SimpleTableModel tableModel;
    private JScrollPane scrollPane;
    private JSpinner firstIndexField, lastIndexField;
    private JButton pasteButton;
    private JComboBox<String> printerCombo;
    private JButton printButton;
    private Properties config;

    public AppFrame(Properties config) {
        super("HCA print tool");
        this.config = config;

        initComponents();
        layOutComponents();
    }

    private void initComponents() {
        tableModel = new SimpleTableModel();
        table = setupTable(tableModel);
        scrollPane = new JScrollPane(table);

        firstIndexField = setupSpinner(1, 1, 1);
        lastIndexField = setupSpinner(1, 1, 1);
        printButton = new JButton("Print");
        printButton.addActionListener(e -> performPrint());
        pasteButton = new JButton("Paste");
        pasteButton.addActionListener(e -> performPaste());

        String[] printers = Arrays.stream(config.getProperty("printers", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        printerCombo = new JComboBox<>(printers);
        printerCombo.setEditable(false);
    }

    private void layOutComponents() {
        JPanel printerPanel = new JPanel();
        printerPanel.add(new JLabel("Set label texts from clipboard:"));
        printerPanel.add(pasteButton);
        printerPanel.add(new JLabel("Printer:"));
        printerPanel.add(printerCombo);

        JPanel printPanel = new JPanel();
        printPanel.add(new JLabel("Print range:"));
        printPanel.add(firstIndexField);
        printPanel.add(new JLabel("to"));
        printPanel.add(lastIndexField);
        printPanel.add(printButton);

        Box bottomPanel = Box.createVerticalBox();
        bottomPanel.add(printerPanel);
        bottomPanel.add(printPanel);

        JPanel cp = new JPanel(new BorderLayout());
        cp.add(scrollPane, BorderLayout.CENTER);
        cp.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(cp);
    }

    private JTable setupTable(TableModel tableModel) {
        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(false);
        TableColumnModel columnModel = table.getColumnModel();
        TableCellRenderer renderer = table.getDefaultRenderer(Integer.class);
        Component comp = renderer.getTableCellRendererComponent(table, 99999, false, false, 0, 0);
        int columnWidth = comp.getPreferredSize().width + 10;
        columnModel.setColumnMargin(10);
        columnModel.getColumn(0).setPreferredWidth(columnWidth);
        columnModel.getColumn(1).setPreferredWidth(400);
        table.setFillsViewportHeight(true);
        return table;
    }

    private JSpinner setupSpinner(int value, int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setColumns(4);
        }
        spinner.addChangeListener(e -> rangeChanged());
        return spinner;
    }

    private boolean canPrint() {
        Integer min = (Integer) firstIndexField.getValue();
        Integer max = (Integer) lastIndexField.getValue();
        return (min!=null && max!=null && min <= max && min > 0 && max <= tableModel.getRowCount());
    }

    private void rangeChanged() {
        printButton.setEnabled(canPrint());
    }

    private void performPaste() {
        String data;
        try {
            data = Clip.paste();
        } catch (Exception e) {
            e.printStackTrace();
            showError("The clipboard could not be read.");
            return;
        }
        List<String> lines = Arrays.stream(data.split("\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        tableModel.setItems(lines);
        updateSpinnerMax(firstIndexField, lines.size());
        updateSpinnerMax(lastIndexField, lines.size());
    }

    private void updateSpinnerMax(JSpinner spinner, int max) {
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        model.setMaximum(max);
        if (max > 0 && model.getNumber().intValue() > max) {
            model.setValue(max);
        }
    }

    private List<String> getValuesToPrint() {
        Integer min = (Integer) firstIndexField.getValue();
        Integer max = (Integer) lastIndexField.getValue();
        if (min==null || max==null || min > max || min < 0 || max > tableModel.getRowCount()) {
            return Collections.emptyList();
        }
        return IntStream.range(min-1, max)
                .mapToObj(tableModel::getItem)
                .collect(Collectors.toList());
    }

    private void performPrint() {
        printButton.setEnabled(false);
        String location = config.getProperty("pmb_url");
        String proxy = config.getProperty("proxy");
        if (location==null) {
            showError("Missing config for PrintMyBarcode location.");
            return;
        }

        List<String> values = getValuesToPrint();
        if (values.isEmpty()) {
            showError("Nothing to print.");
            return;
        }

        PrintRequest request = new PrintRequest(values, (String) printerCombo.getSelectedItem());

        PMBClient pmb = new PMBClient(location, proxy);

        try {
            pmb.print(request);
        } catch (Exception e) {
            e.printStackTrace();
            showError("There was an error when trying to print: "+e);
            return;
        }
        JOptionPane.showMessageDialog(this, "Request sent.");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
