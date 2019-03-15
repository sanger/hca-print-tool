package uk.ac.sanger.hcaprint;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The window and controller for the application.
 * @author dr6
 */
public class AppFrame extends JFrame {
    private JTable table;
    private FunctionalTableModel<LabelData> tableModel;
    private JScrollPane scrollPane;
    private JSpinner firstIndexField, lastIndexField;
    private JButton pasteButton;
    private JComboBox<String> printerCombo;
    private JButton printButton;
    private Properties config;

    /**
     * Constructs an AppFrame using the given config.
     * The config keys should include "printers", "pmb_url" and (optionally) "proxy".
     */
    public AppFrame(Properties config) {
        super("HCA print tool");
        this.config = config;

        initComponents();
        layOutComponents();
    }

    private void initComponents() {
        tableModel = new FunctionalTableModel<>(null, LabelData::getName, LabelData::getDate);
        tableModel.setIndexColumn(0);
        tableModel.setHeadings("#", "Name/Barcode", "Date");
        table = setUpTable(tableModel);
        scrollPane = new JScrollPane(table);

        firstIndexField = setUpSpinner(1, 1, 1);
        lastIndexField = setUpSpinner(1, 1, 1);
        printButton = new JButton("Print");
        printButton.addActionListener(e -> performPrint());
        printButton.setEnabled(false);
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
        Box printerPanel = Box.createHorizontalBox();
        printerPanel.add(Box.createHorizontalGlue());
        printerPanel.add(pasteButton);
        printerPanel.add(Box.createHorizontalStrut(20));
        printerPanel.add(new JLabel("Printer:"));
        printerPanel.add(printerCombo);
        printerPanel.add(Box.createHorizontalGlue());

        Box printPanel = Box.createHorizontalBox();
        printPanel.add(Box.createHorizontalGlue());
        printPanel.add(new JLabel("Print range:"));
        printPanel.add(Box.createHorizontalStrut(5));
        printPanel.add(firstIndexField);
        printPanel.add(Box.createHorizontalStrut(5));
        printPanel.add(new JLabel("to"));
        printPanel.add(Box.createHorizontalStrut(5));
        printPanel.add(lastIndexField);
        printPanel.add(Box.createHorizontalStrut(20));
        printPanel.add(printButton);
        printPanel.add(Box.createHorizontalGlue());

        String explanation = "<html>You can paste two columns from a spreadsheet."
                +"<br>The left column should be the name/barcode."
                +"<br>The right column should be the date."
                +"</html>";

        JPanel explainPanel = new JPanel();
        explainPanel.add(new JLabel(explanation));

        Box bottomPanel = Box.createVerticalBox();
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(explainPanel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(printerPanel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(printPanel);
        bottomPanel.add(Box.createVerticalStrut(10));

        JPanel cp = new JPanel(new BorderLayout());
        cp.add(scrollPane, BorderLayout.CENTER);
        cp.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(cp);
    }

    private JTable setUpTable(TableModel tableModel) {
        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(false);
        TableColumnModel columnModel = table.getColumnModel();
        TableCellRenderer renderer = table.getDefaultRenderer(Integer.class);
        Component comp = renderer.getTableCellRendererComponent(table, 99999, false, false, 0, 0);
        int columnWidth = comp.getPreferredSize().width + 10;
        columnModel.setColumnMargin(10);
        columnModel.getColumn(0).setPreferredWidth(columnWidth);
        columnModel.getColumn(0).setMinWidth(columnWidth);
        columnModel.getColumn(1).setPreferredWidth(400);
        columnModel.getColumn(2).setPreferredWidth(400);
        table.setFillsViewportHeight(true);
        return table;
    }

    private JSpinner setUpSpinner(int value, int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setColumns(4);
        }
        spinner.addChangeListener(e -> rangeChanged());
        spinner.setMaximumSize(spinner.getPreferredSize());
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
        List<LabelData> rows = Arrays.stream(data.split("\n"))
                .map(LabelData::fromLine)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        tableModel.setItems(rows);
        updateSpinnerMax(firstIndexField, rows.size());
        updateSpinnerMax(lastIndexField, rows.size());
    }

    private void updateSpinnerMax(JSpinner spinner, int max) {
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        model.setMaximum(max);
        if (max > 0 && model.getNumber().intValue() > max) {
            model.setValue(max);
        }
    }

    private List<LabelData> getValuesToPrint() {
        Integer min = (Integer) firstIndexField.getValue();
        Integer max = (Integer) lastIndexField.getValue();
        if (min==null || max==null || min > max || min < 0 || max > tableModel.getRowCount()) {
            return Collections.emptyList();
        }
        return IntStream.range(min-1, max)
                .mapToObj(tableModel::getRow)
                .collect(Collectors.toList());
    }

    private void performPrint() {
        printButton.setEnabled(false);
        String location = config.getProperty("pmb_url", "").trim();
        String proxy = config.getProperty("proxy");
        String templateString = config.getProperty("template_id", "").trim();
        if (location.isEmpty() || templateString.isEmpty()) {
            showError("Missing config for PrintMyBarcode location.");
            return;
        }

        int templateId;
        try {
            templateId = Integer.parseInt(templateString);
        } catch (NumberFormatException e) {
            showError("Invalid template id: "+templateString);
            return;
        }

        List<LabelData> values = getValuesToPrint();
        if (values.isEmpty()) {
            showError("Nothing to print.");
            return;
        }

        PrintRequest request = new PrintRequest(values, (String) printerCombo.getSelectedItem(), templateId, config);

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
