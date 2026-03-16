package uk.gov.hmcts.reform.civil.client.sendletter.api;

import java.util.Map;
import java.util.Objects;

/**
 * Document.
 */
public class Document {

    public final String template;

    public final Map<String, Object> values;

    /**
     * Constructor.
     * @param template The template
     * @param values The values
     */
    public Document(String template, Map<String, Object> values) {
        this.template = template;
        this.values = values;
    }

    /**
     * Check if the object is equal to this document.
     * @return Boolean indicating if the object is equal to this document
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Document document = (Document) obj;
        return Objects.equals(template, document.template)
                && Objects.equals(values, document.values);
    }

    /**
     * Generate a hash code using the template and values.
     * @return The hash code integer
     */
    @Override
    public int hashCode() {
        return Objects.hash(template, values);
    }
}
