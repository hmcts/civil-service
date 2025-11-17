package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;

@Data
@Builder
public class ManageDocument {

    private Document documentLink;
    private String documentName;
    private ManageDocumentType documentType;
    private String documentTypeOther;
    private LocalDateTime createdDatetime = LocalDateTime.now();
}
