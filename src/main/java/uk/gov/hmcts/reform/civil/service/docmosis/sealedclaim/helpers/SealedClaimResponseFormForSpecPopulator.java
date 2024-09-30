package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;

@Component
@RequiredArgsConstructor
public class SealedClaimResponseFormForSpecPopulator {

    private final ReferenceNumberPopulator referenceNumberPopulator;
    private final StatementOfTruthPopulator statementOfTruthPopulator;

    public SealedClaimResponseFormForSpec populateForm(CaseData caseData, String authorisation) {

        var sealedClaimResponseFormSpecBuilder = SealedClaimResponseFormForSpec.builder();

        referenceNumberPopulator.populateDetails(sealedClaimResponseFormSpecBuilder, caseData, authorisation);
        statementOfTruthPopulator.populateDetails(sealedClaimResponseFormSpecBuilder, caseData);

        return sealedClaimResponseFormSpecBuilder.build();
    }
}
