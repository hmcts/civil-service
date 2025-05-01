package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MediationAgreementDocument {

    private String name;
    private DocumentType documentType;
    private Document document;
    @Builder.Default
    private LocalDateTime documentUploadedDatetime = LocalDateTime.now(ZoneId.of("Europe/London"));

}
