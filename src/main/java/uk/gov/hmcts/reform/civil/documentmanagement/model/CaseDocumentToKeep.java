package uk.gov.hmcts.reform.civil.documentmanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseDocumentToKeep {

    @JsonProperty("document_url")
    private String documentUrl;
    @JsonProperty("document_filename")
    private String documentFilename;
    @JsonProperty("document_binary_url")
    private String documentBinaryUrl;
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("upload_timestamp")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime uploadTimestamp;

    public String getDocumentUrl() {
        return this.documentUrl == null ? "" : this.documentUrl;
    }
}
