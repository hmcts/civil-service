package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlQuestionsClaimantExpertFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlQuestionsClaimantExpertFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlQuestionsClaimantExpertFieldBuilder sdoR2AndNihlQuestionsClaimantExpertFieldBuilder;

    @Test
    void shouldBuildSdoR2QuestionsClaimantExpert() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlQuestionsClaimantExpertFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2QuestionsClaimantExpert questionsClaimantExpert = caseData.getSdoR2QuestionsClaimantExpert();

        assertEquals(SdoR2UiConstantFastTrack.DEFENDANT_MAY_ASK, questionsClaimantExpert.getSdoDefendantMayAskTxt());
        assertEquals(LocalDate.now().plusDays(126), questionsClaimantExpert.getSdoDefendantMayAskDate());
        assertEquals(SdoR2UiConstantFastTrack.QUESTIONS_SHALL_BE_ANSWERED, questionsClaimantExpert.getSdoQuestionsShallBeAnsweredTxt());
        assertEquals(LocalDate.now().plusDays(147), questionsClaimantExpert.getSdoQuestionsShallBeAnsweredDate());
        assertEquals(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL, questionsClaimantExpert.getSdoUploadedToDigitalPortalTxt());
        assertEquals(NO, questionsClaimantExpert.getSdoApplicationToRelyOnFurther().getDoRequireApplicationToRely());
        assertEquals(SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS,
                questionsClaimantExpert.getSdoApplicationToRelyOnFurther().getApplicationToRelyOnFurtherDetails().getApplicationToRelyDetailsTxt());
        assertEquals(LocalDate.now().plusDays(161),
                questionsClaimantExpert.getSdoApplicationToRelyOnFurther().getApplicationToRelyOnFurtherDetails().getApplicationToRelyDetailsDate());
    }
}