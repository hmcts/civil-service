package uk.gov.hmcts.reform.civil.model.documents;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CaseDocument {

    private final Document documentLink;
    private final String documentName;
    private final DocumentType documentType;
    private final long documentSize;
    private final LocalDateTime createdDatetime;
    private final String createdBy;
    @JsonIgnore
    private List<String> error;
}
