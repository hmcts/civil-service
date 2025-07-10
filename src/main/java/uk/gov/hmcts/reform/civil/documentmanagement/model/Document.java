package uk.gov.hmcts.reform.civil.documentmanagement.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Document {

    @JsonAlias("document_url")
    String documentUrl;
    @JsonAlias("document_binary_url")
    String documentBinaryUrl;
    @JsonAlias("document_filename")
    String documentFileName;
    @JsonAlias("document_hash")
    String documentHash;
    @JsonAlias("category_id")
    String categoryID;
    @JsonAlias("upload_timestamp")
    String uploadTimestamp;

    @JsonCreator
    public Document(@JsonProperty("document_url") String documentUrl,
                    @JsonProperty("document_binary_url") String documentBinaryUrl,
                    @JsonProperty("document_filename") String documentFileName,
                    @JsonProperty("document_hash") String documentHash,
                    @JsonProperty("category_id") String categoryID,
                    @JsonProperty("upload_timestamp") String uploadTimestamp) {
        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFileName = documentFileName;
        this.documentHash = documentHash;
        this.categoryID = categoryID;
        this.uploadTimestamp = uploadTimestamp;
    }

}
