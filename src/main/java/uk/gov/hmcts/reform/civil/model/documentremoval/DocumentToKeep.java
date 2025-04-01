package uk.gov.hmcts.reform.civil.model.documentremoval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocumentToKeep;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentToKeep {

    private String documentId;
    private CaseDocumentToKeep caseDocumentToKeep;
    private LocalDateTime uploadedDate;
    private YesOrNo systemGenerated;
}
