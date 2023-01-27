package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
public class BundleDetails {

    private String id;
    private String title;
    private String description;
    private String stitchStatus;
    private DocumentLink stitchedDocument;
    private String stitchingFailureMessage;
    private String fileName;

    @JsonCreator
    public BundleDetails(@JsonProperty("id") String id,
                         @JsonProperty("title") String title,
                         @JsonProperty("description") String description,
                         @JsonProperty("stitchStatus") String stitchStatus,
                         @JsonProperty("stitchedDocument") DocumentLink stitchedDocument,
                         @JsonProperty("stitchingFailureMessage") String stitchingFailureMessage,
                         @JsonProperty("fileName") String fileName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.stitchStatus = stitchStatus;
        this.stitchedDocument = stitchedDocument;
        this.stitchingFailureMessage = stitchingFailureMessage;
        this.fileName = fileName;
    }
}
