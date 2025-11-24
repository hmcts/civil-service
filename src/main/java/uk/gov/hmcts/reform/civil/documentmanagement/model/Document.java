package uk.gov.hmcts.reform.civil.documentmanagement.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Document {

    @JsonAlias("document_url")
    @com.fasterxml.jackson.annotation.JsonProperty("document_url")
    String documentUrl;
    @JsonAlias("document_binary_url")
    @com.fasterxml.jackson.annotation.JsonProperty("document_binary_url")
    String documentBinaryUrl;
    @JsonAlias("document_filename")
    @com.fasterxml.jackson.annotation.JsonProperty("document_filename")
    String documentFileName;
    @JsonAlias("document_hash")
    @com.fasterxml.jackson.annotation.JsonProperty("document_hash")
    String documentHash;
    @JsonAlias("category_id")
    @com.fasterxml.jackson.annotation.JsonProperty("category_id")
    String categoryID;
    @JsonAlias("upload_timestamp")
    @com.fasterxml.jackson.annotation.JsonProperty("upload_timestamp")
    String uploadTimestamp;

}
