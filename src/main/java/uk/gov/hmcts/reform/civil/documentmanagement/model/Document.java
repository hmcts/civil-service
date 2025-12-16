package uk.gov.hmcts.reform.civil.documentmanagement.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@Setter
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
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
}
