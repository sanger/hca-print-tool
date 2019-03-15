package uk.ac.sanger.hcaprint;

import javax.json.*;
import java.util.*;
import java.util.function.Function;

/**
 * A class representing the information sent in a print request.
 * @author dr6
 */
public class PrintRequest {
    /**
     * Fields included on the label.
     * Each field provides a {@link #propertyName} and a way of getting a value from an instance of {@link LabelData}.
     */
    public enum Field implements Function<LabelData, String> {
        NAME(LabelData::getName),
        BARCODE(LabelData::getBarcode),
        DATE(LabelData::getDate),
        ;

        private final Function<LabelData, String> function;

        Field(Function<LabelData, String> function) {
            this.function = function;
        }

        /**
         * Gets the property name for this field.
         * The property name should be associated with a string (the field name) in the application's config.
         * The field name is the key used in the JSON property representing this field in the print request.
         * @return the property name for this field
         */
        public String propertyName() {
            return "field_"+this.name().toLowerCase();
        }

        /**
         * Gets the value for this field from the given {@code LabelData} instance
         * @param labelData the instance to extract the field value from
         * @return the extracted field value
         */
        @Override
        public String apply(LabelData labelData) {
            return this.function.apply(labelData);
        }
    }

    private final JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);

    private int templateId;
    private Map<Field, String> fieldNames;
    private final List<LabelData> values;
    private final String printer;

    /**
     * Constructs a print request for sending the specified values to the indicated printer.
     * @param values the values to send
     * @param printer the name of the printer
     * @param templateId the template id to include in the print request
     * @param fieldProps properties containing the names of the fields to supply in the label data
     */
    public PrintRequest(List<LabelData> values, String printer, int templateId, Properties fieldProps) {
        this.values = values;
        this.printer = printer;
        this.templateId = templateId;
        this.fieldNames = new EnumMap<>(Field.class);
        for (Field field : Field.values()) {
            String value = fieldProps.getProperty(field.propertyName(), "").trim();
            if (!value.isEmpty()) {
                this.fieldNames.put(field, value);
            }
        }
    }

    /**
     * Gets the data for the print request, built into a representation of a JSON object.
     * @return a representation of a JSON object
     */
    public JsonValue getJsonValue() {
        JsonArrayBuilder labels = createArrayBuilder();
        for (LabelData data : this.values) {
            labels.add(labelData(data));
        }

        JsonObjectBuilder attributes = createObjectBuilder();
        attributes.add("printer_name", this.printer);
        attributes.add("label_template_id", templateId);
        attributes.add("labels", createObjectBuilder().add("body", labels));

        return createObjectBuilder()
                .add("data", createObjectBuilder().add("attributes", attributes))
                .build();
    }

    /**
     * Gets a JSON value representing the fields for a single label.
     * This is incorporated into the {@link #getJsonValue combined JSON value object} for the whole request.
     * @param data the data for a single label
     * @return a JSON value object
     */
    private JsonValue labelData(LabelData data) {
        JsonObjectBuilder fieldData = createObjectBuilder();
        for (Map.Entry<Field, String> entry : fieldNames.entrySet()) {
            Field field = entry.getKey();
            String fieldName = entry.getValue();
            if (fieldName!=null) {
                String fieldValue = field.apply(data);
                if (fieldValue!=null) {
                    fieldData.add(fieldName, field.apply(data));
                }
            }
        }
        return createObjectBuilder()
                .add("label", fieldData)
                .build();
    }

    /**
     * Gets a new JSON object builder from the builder factory.
     */
    private JsonObjectBuilder createObjectBuilder() {
        return builderFactory.createObjectBuilder();
    }

    /**
     * Gets a new JSON array builder from the builder factory.
     */
    private JsonArrayBuilder createArrayBuilder() {
        return builderFactory.createArrayBuilder();
    }

    /**
     * The data for the request, serialised as JSON.
     */
    @Override
    public String toString() {
        return getJsonValue().toString();
    }
}
