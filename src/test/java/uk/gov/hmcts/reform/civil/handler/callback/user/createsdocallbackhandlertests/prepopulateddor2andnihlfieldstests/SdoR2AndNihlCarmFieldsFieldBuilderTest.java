package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlCarmFieldsFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlCarmFieldsFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlCarmFieldsFieldBuilder sdoR2AndNihlCarmFieldsFieldBuilder;

    @Test
    void shouldBuildSdoR2SmallClaimsMediationSection() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlCarmFieldsFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2SmallClaimsMediation mediationSection = caseData.getSdoR2SmallClaimsMediationSectionStatement();

        assertEquals(SdoR2UiConstantSmallClaim.CARM_MEDIATION_TEXT, mediationSection.getInput());
    }
}