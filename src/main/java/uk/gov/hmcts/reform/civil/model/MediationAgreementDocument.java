package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediationAgreementDocument {

    private String name;
    private DocumentType documentType;
    private Document document;
}
