package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediationAgreementDocument {

    private String title;
    private Document document;
}
