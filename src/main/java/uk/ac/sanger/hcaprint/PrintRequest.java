package uk.ac.sanger.hcaprint;

import javax.json.*;
import javax.json.stream.JsonCollectors;
import javax.json.stream.JsonGenerator;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

/**
 * @author dr6
 */
public class PrintRequest {
    private final JsonBuilderFactory builderFactory = Json.createBuilderFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
    private final JsonReaderFactory readerFactory = Json.createReaderFactory(null);

    private final String template;
    private final List<LabelData> labelData;
    private final String printer;

    public PrintRequest(String template, List<LabelData> labelData, String printer) {
        this.template = template;
        this.labelData = labelData;
        this.printer = printer;
    }

    private JsonObjectBuilder objectBuilder() {
        return builderFactory.createObjectBuilder();
    }

    private JsonValue buildRequestJson() {
        return objectBuilder()
                .add("query", mutation())
                .add("variables", variables())
                .build();
    }

    private JsonValue mutation() {
        return Json.createValue("mutation ($printer: String!, $request: PrintRequest!) {" +
                "  print(printer: $printer, printRequest: $request) {" +
                "  jobId } }");
    }

    private JsonValue variables() {
        return objectBuilder()
                .add("printer", this.printer)
                .add("request", objectBuilder()
                        .add("layouts", layouts()))
                .build();
    }

    private JsonObject parseObject(String jsonText) {
        try (JsonReader reader = readerFactory.createReader(new StringReader(jsonText))) {
            return reader.readObject();
        }
    }

    private JsonArray layouts() {
        return labelData.stream()
                .map(this::fillTemplate)
                .map(this::parseObject)
                .collect(JsonCollectors.toJsonArray());
    }

    private String fillTemplate(LabelData labelData) {
        return template.replace("{barcode}", labelData.getBarcode())
                .replace("{name}", labelData.getName())
                .replace("{date}", labelData.getDate());
    }

    @Override
    public String toString() {
        return buildRequestJson().toString();
    }
}
