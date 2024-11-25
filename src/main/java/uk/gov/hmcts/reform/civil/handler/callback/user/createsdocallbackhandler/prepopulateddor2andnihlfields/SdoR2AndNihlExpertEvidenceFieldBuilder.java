package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ExpertEvidence;

@Component
public class SdoR2AndNihlExpertEvidenceFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2ExpertEvidence(SdoR2ExpertEvidence.builder()
                .sdoClaimantPermissionToRelyTxt(SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY)
                .build());
    }
}
