package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class MediationAgreementDocument {

    private String name;
    private DocumentType documentType;
    private Document document;
    private LocalDateTime documentUploadedDatetime = LocalDateTime.now(ZoneId.of("Europe/London"));

}
