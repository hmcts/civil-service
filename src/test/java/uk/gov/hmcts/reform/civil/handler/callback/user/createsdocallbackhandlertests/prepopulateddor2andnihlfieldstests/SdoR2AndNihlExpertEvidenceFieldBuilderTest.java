package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlExpertEvidenceFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ExpertEvidence;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlExpertEvidenceFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlExpertEvidenceFieldBuilder sdoR2AndNihlExpertEvidenceFieldBuilder;

    @Test
    void shouldBuildSdoR2ExpertEvidence() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlExpertEvidenceFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2ExpertEvidence expertEvidence = caseData.getSdoR2ExpertEvidence();

        assertEquals(SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY, expertEvidence.getSdoClaimantPermissionToRelyTxt());
    }
}