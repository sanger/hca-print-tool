package uk.ac.sanger.hcaprint;

import javax.json.*;
import java.util.List;

/**
 * A class representing the information sent in a print request.
 * @author dr6
 */
public class PrintRequest {
    private final JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);

    private int templateId;
    private String fieldName;
    private final List<String> values;
    private final String printer;

    /**
     * Constructs a print request for sending the specified values to the indicated printer.
     * @param values the values to send
     * @param printer the name of the printer
     * @param templateId the template id to include in the print request
     * @param fieldName the name of the field to supply in the label data
     */
    public PrintRequest(List<String> values, String printer, int templateId, String fieldName) {
        this.values = values;
        this.printer = printer;
        this.templateId = templateId;
        this.fieldName = fieldName;
    }

    /**
     * Gets the data for the print request, built into a representation of a JSON object.
     * @return a representation of a JSON object
     */
    public JsonValue getJsonValue() {
        JsonArrayBuilder labels = createArrayBuilder();
        for (String value : this.values) {
            labels.add(labelData(value));
        }

        JsonObjectBuilder attributes = createObjectBuilder();
        attributes.add("printer_name", this.printer);
        attributes.add("label_template_id", templateId);
        attributes.add("labels", createObjectBuilder().add("body", labels));

        return createObjectBuilder()
                .add("data", createObjectBuilder().add("attributes", attributes))
                .build();
    }

    private JsonValue labelData(String value) {
        return createObjectBuilder()
                .add("label", createObjectBuilder().add(fieldName, value))
                .build();
    }

    private JsonObjectBuilder createObjectBuilder() {
        return builderFactory.createObjectBuilder();
    }

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
