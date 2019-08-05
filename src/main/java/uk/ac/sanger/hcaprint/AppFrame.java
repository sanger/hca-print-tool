package uk.ac.sanger.hcaprint;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * The window and controller for the HCA label printing application.
 * @author dr6
 */
public class AppFrame extends JFrame {
    enum Field {
        NAME, BARCODE, DATE
    }

    private JTable table;
    private FunctionalTableModel<LabelData> tableModel;
    private JScrollPane scrollPane;
    private JSpinner firstIndexField, lastIndexField;
    private JButton pasteButton;
    private JButton clearButton;
    private JComboBox<String> printerCombo;
    private JButton printButton;
    private Properties config;
    private String template;

    /**
     * Constructs an AppFrame using the given config.
     * The config keys should include "printers", "print_service" and (optionally) "proxy".
     */
    public AppFrame(Properties config, String template) {
        super(config.getProperty("app_title", "Print tool"));
        this.config = config;
        this.template = template;

        initComponents();
        layOutComponents();
        setUpMenuBar();
    }

    /**
     * Creates the graphical components used in the frame.
     */
    private void initComponents() {
        Set<Field> enabledFields = findEnabledFields();
        tableModel = createTableModel(enabledFields);
        table = setUpTable(tableModel);
        scrollPane = new JScrollPane(table);

        firstIndexField = setUpSpinner(1, 1, 1);
        lastIndexField = setUpSpinner(1, 1, 1);
        printButton = new JButton("Print");
        printButton.addActionListener(e -> performPrint());
        printButton.setEnabled(false);
        pasteButton = new JButton("Paste");
        pasteButton.addActionListener(e -> performPaste());
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> performClear());
        clearButton.setVisible(false);

        String[] printers = Arrays.stream(config.getProperty("printers", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        printerCombo = new JComboBox<>(printers);
        printerCombo.setEditable(false);
    }

    private void setUpMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu helpMenu = menuBar.add(new JMenu("Help"));
        helpMenu.add("View config").addActionListener(e -> viewConfig());
        setJMenuBar(menuBar);
    }

    /**
     * Lays out the components in panels and adds them to the frame.
     */
    private void layOutComponents() {
        Box printerPanel = Box.createHorizontalBox();
        printerPanel.add(Box.createHorizontalGlue());
        printerPanel.add(pasteButton);
        printerPanel.add(clearButton);
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

        JPanel explainPanel = new JPanel();
        explainPanel.add(new JLabel(getExplanation()));

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

    private void viewConfig() {
        ConfigPanel configPanel = new ConfigPanel(this.config);
        configPanel.showDialog(this);
    }

    private String getExplanation() {
        switch (tableModel.getColumnCount()) {
            case 0: case 1:
                return "";
            case 2:
                return "You can paste a column from a spreadsheet.";
            case 3:
                return "<html>You can paste two columns from a spreadsheet."
                        + "<br>The left column should be the name/barcode."
                        + "<br>The right column should be the date."
                        + "</html>";
            default:
                return "You can paste columns from a spreadsheet.";
        }
    }

    private Set<Field> findEnabledFields() {
        String propValue = config.getProperty("fields", "");
        if (propValue.isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(propValue.replace(',',' ').split("\\s+"))
                .filter(s -> !s.isEmpty())
                .map(s -> Field.valueOf(s.toUpperCase()))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Field.class)));
    }

    private FunctionalTableModel<LabelData> createTableModel(Set<Field> enabledFields) {
        int numColumns = 1 + ((enabledFields.contains(Field.BARCODE) || enabledFields.contains(Field.NAME)) ? 1 : 0)
                + (enabledFields.contains(Field.DATE) ? 1 : 0);
        @SuppressWarnings("unchecked")
        Function<LabelData, String>[] columnFunctions = new Function[numColumns];
        String[] headings = new String[numColumns];
        headings[0] = "#";
        int index = 1;
        if (enabledFields.contains(Field.BARCODE) && enabledFields.contains(Field.NAME)) {
            columnFunctions[index] = LabelData::getName;
            headings[index] = "Name/Barcode";
            ++index;
        } else if (enabledFields.contains(Field.BARCODE)) {
            columnFunctions[index] = LabelData::getBarcode;
            headings[index] = "Barcode";
            ++index;
        } else if (enabledFields.contains(Field.NAME)) {
            columnFunctions[index] = LabelData::getName;
            headings[index] = "Name";
            ++index;
        }
        if (enabledFields.contains(Field.DATE)) {
            columnFunctions[index] = LabelData::getDate;
            headings[index] = "Date";
            ++index;
        }
        FunctionalTableModel<LabelData> model = new FunctionalTableModel<>(columnFunctions);
        model.setIndexColumn(0);
        model.setHeadings(headings);
        return model;
    }


    /**
     * Creates a table, sets it up suitably and returns it.
     * Setting up includes adjusting column sizes and viewport behaviour.
     * @param tableModel the model for the table
     * @return the new table
     */
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
        int columnCount = tableModel.getColumnCount();
        for (int col = 1; col < columnCount; ++col) {
            columnModel.getColumn(col).setPreferredWidth(800 / (columnCount - 1));
        }
        table.setFillsViewportHeight(true);
        return table;
    }

    /**
     * Creates a int spinner and returns it.
     * Setting up includes adding {@link #rangeChanged} as a ChangeListener, and adjusting the
     * size of the spinner component.
     * @param value the initial value of the spinner
     * @param min the minimum value of the spinner
     * @param max the maximum value of the spinner
     * @return the new spinner
     */
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

    /**
     * Are we ready to print?
     * We are ready to print if the table has rows, and the min and max spinners specify a
     * nonempty range of rows.
     * @return true if we're ready to print, otherwise false
     */
    private boolean canPrint() {
        Integer min = (Integer) firstIndexField.getValue();
        Integer max = (Integer) lastIndexField.getValue();
        return (min != null && max != null && min <= max && min > 0 && max <= tableModel.getRowCount());
    }

    /**
     * This is triggered when the spinners are adjusted.
     * It enables or disables the print button, according to {@link #canPrint}.
     */
    private void rangeChanged() {
        printButton.setEnabled(canPrint());
    }

    /**
     * Is a warm-up label enabled?
     * Checks config for the property "warm_up".
     * Defaults to false.
     * @return true if warm-up is enabled, otherwise false
     */
    private boolean isWarmUpEnabled() {
        return Boolean.parseBoolean(config.getProperty("warm_up"));
    }

    /**
     * The behaviour of the paste button.
     * Tries to read the clipbaord. Tries to convert it to a list of label data,
     * and to put that in the table.
     * If reading the clipboard fails, an error will be shown to the user.
     */
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
        rangeChanged();
        boolean gotRows = !rows.isEmpty();
        clearButton.setVisible(gotRows);
        pasteButton.setVisible(!gotRows);
    }

    private void performClear() {
        tableModel.setItems(Collections.emptyList());
        rangeChanged();
        clearButton.setVisible(false);
        pasteButton.setVisible(true);
    }

    /**
     * Adjusts the max value of the given int spinner.
     * If the new max is below the spinner's current value, the value is brought down.
     * @param spinner an int spinner
     * @param max the new max value for the spinner
     */
    private void updateSpinnerMax(JSpinner spinner, int max) {
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        model.setMaximum(max);
        if (max > 0 && model.getNumber().intValue() > max) {
            model.setValue(max);
        }
    }

    /**
     * Gets the labeldata for printing.
     * This is a range of rows from the table. The range is determined by the values in the range spinners.
     * If the range is empty or invalid, an empty list will be returned.
     * @return a list of labeldata for printing
     */
    private List<LabelData> getValuesToPrint() {
        Integer min = (Integer) firstIndexField.getValue();
        Integer max = (Integer) lastIndexField.getValue();
        if (min==null || max==null || min > max || min < 0 || max > tableModel.getRowCount()) {
            return Collections.emptyList();
        }
        Stream<LabelData> labelStream = IntStream.range(min-1, max).mapToObj(tableModel::getRow);
        if (isWarmUpEnabled()) {
            labelStream = Stream.concat(Stream.of(LabelData.warmUp()), labelStream);
        }
        return labelStream.collect(Collectors.toList());
    }

    /**
     * The behaviour of the print button.
     * This reads some values from the application's config, creates a
     * {@link PrintRequest} and posts it using {@link SPrintClient}.
     * In the event of an error (missing/invalid config, failed post request),
     * an error message will be shown to the user.
     * If the post succeeds, a success message will be shown.
     */
    private void performPrint() {
        printButton.setEnabled(false);
        String location = config.getProperty("print_service", "").trim();
        String proxy = config.getProperty("proxy");
        if (location.isEmpty()) {
            showError("Missing config for print service location.");
            return;
        }

        List<LabelData> values = getValuesToPrint();
        if (values.isEmpty()) {
            showError("Nothing to print.");
            return;
        }

        PrintRequest request = new PrintRequest(template, values, (String) printerCombo.getSelectedItem());

        SPrintClient client = new SPrintClient(location, proxy);

        try {
            client.print(request);
        } catch (Exception e) {
            e.printStackTrace();
            showError("There was an error when trying to print: "+e);
            return;
        }
        JOptionPane.showMessageDialog(this, "Request sent.");
    }

    /**
     * Shows an error message to the user using {@link JOptionPane}.
     * @param message the message to display
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
