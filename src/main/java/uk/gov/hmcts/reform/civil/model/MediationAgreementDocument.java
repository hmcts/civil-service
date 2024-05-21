package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import java.time.LocalDateTime;

@Data
@Builder
public class MediationAgreementDocument {

    private final String name;
    private final DocumentType documentType;
    private final Document document;
    private final LocalDateTime createdDatetime = LocalDateTime.now();

}
