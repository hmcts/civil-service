package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;

@Data
@Builder
public class ManageDocument {

    private final Document documentLink;
    private final String documentName;
    private final ManageDocumentType documentType;
    private final String documentTypeOther;
    private final LocalDateTime createdDatetime = LocalDateTime.now();
}
