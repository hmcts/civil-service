package uk.gov.hmcts.reform.civil.model.mediation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediationDocumentsReferredInStatement {

    private String documentType;
    private LocalDate documentDate;
    private Document document;
    private LocalDateTime documentUploadedDatetime = LocalDateTime.now(ZoneId.of("Europe/London"));
}
