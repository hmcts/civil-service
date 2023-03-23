package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
public class BundleDetails {

    private String id;
    private String title;
    private String description;
    private String stitchStatus;
    private Document stitchedDocument;
    private String stitchingFailureMessage;
    private String fileName;

    private LocalDateTime createdOn;
    private LocalDate bundleHearingDate;

    @JsonCreator
    public BundleDetails(@JsonProperty("id") String id,
                         @JsonProperty("title") String title,
                         @JsonProperty("description") String description,
                         @JsonProperty("stitchStatus") String stitchStatus,
                         @JsonProperty("stitchedDocument") Document stitchedDocument,
                         @JsonProperty("stitchingFailureMessage") String stitchingFailureMessage,
                         @JsonProperty("fileName") String fileName,
                         @JsonProperty("createdOn") LocalDateTime createdOn,
                         @JsonProperty("bundleHearingDate") LocalDate bundleHearingDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.stitchStatus = stitchStatus;
        this.stitchedDocument = stitchedDocument;
        this.stitchingFailureMessage = stitchingFailureMessage;
        this.fileName = fileName;
        this.createdOn = createdOn;
        this.bundleHearingDate = bundleHearingDate;
    }
}
