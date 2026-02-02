package uk.gov.hmcts.reform.civil.model.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DocumentAndNote {

    private String documentName;
    private Document document;
    private String documentNote;
    private LocalDateTime createdDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));
    private String createdBy;
    private String documentNoteForTab;

}
