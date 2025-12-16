package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManageDocument {

    private Document documentLink;
    private String documentName;
    private ManageDocumentType documentType;
    private String documentTypeOther;
    @Builder.Default
    private LocalDateTime createdDatetime = LocalDateTime.now();
}
