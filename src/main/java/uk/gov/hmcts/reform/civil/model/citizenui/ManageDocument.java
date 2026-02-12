package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ManageDocument {

    private Document documentLink;
    private String documentName;
    private ManageDocumentType documentType;
    private String documentTypeOther;
    private LocalDateTime createdDatetime = LocalDateTime.now();
}
