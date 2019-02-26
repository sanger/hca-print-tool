package uk.ac.sanger.hcaprint;

import javax.json.*;
import java.util.List;

/**
 * @author dr6
 */
public class PrintRequest {
    private static final int LABEL_TEMPLATE_ID = 203;
    private static final String FIELD_NAME = "contents";

    private final JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);

    private final List<String> values;
    private final String printer;

    public PrintRequest(List<String> values, String printer) {
        this.values = values;
        this.printer = printer;
    }

    public JsonValue getJsonValue() {
        JsonArrayBuilder labels = createArrayBuilder();
        for (String value : this.values) {
            labels.add(labelData(value));
        }

        JsonObjectBuilder attributes = createObjectBuilder();
        attributes.add("printer_name", this.printer);
        attributes.add("label_template_id", LABEL_TEMPLATE_ID);
        attributes.add("labels", createObjectBuilder().add("body", labels));

        return createObjectBuilder()
                .add("data", createObjectBuilder().add("attributes", attributes))
                .build();
    }

    private JsonValue labelData(String value) {
        return createObjectBuilder()
                .add("label", createObjectBuilder().add(FIELD_NAME, value))
                .build();
    }

    private JsonObjectBuilder createObjectBuilder() {
        return builderFactory.createObjectBuilder();
    }

    private JsonArrayBuilder createArrayBuilder() {
        return builderFactory.createArrayBuilder();
    }

    @Override
    public String toString() {
        return getJsonValue().toString();
    }
}
