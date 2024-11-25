package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlQuestionsToEntExpertFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlQuestionsToEntExpertFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlQuestionsToEntExpertFieldBuilder sdoR2AndNihlQuestionsToEntExpertFieldBuilder;

    @Test
    void shouldBuildSdoR2QuestionsToEntExpert() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlQuestionsToEntExpertFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2QuestionsToEntExpert questionsToEntExpert = caseData.getSdoR2QuestionsToEntExpert();

        assertEquals(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS, questionsToEntExpert.getSdoWrittenQuestionsTxt());
        assertEquals(LocalDate.now().plusDays(336), questionsToEntExpert.getSdoWrittenQuestionsDate());
        assertEquals(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL, questionsToEntExpert.getSdoWrittenQuestionsDigPortalTxt());
        assertEquals(SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED, questionsToEntExpert.getSdoQuestionsShallBeAnsweredTxt());
        assertEquals(LocalDate.now().plusDays(350), questionsToEntExpert.getSdoQuestionsShallBeAnsweredDate());
        assertEquals(SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED, questionsToEntExpert.getSdoShallBeUploadedTxt());
    }
}