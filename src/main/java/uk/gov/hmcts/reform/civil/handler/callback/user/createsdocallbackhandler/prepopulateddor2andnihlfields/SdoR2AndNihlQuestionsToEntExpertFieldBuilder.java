package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;

import java.time.LocalDate;

@Component
public class SdoR2AndNihlQuestionsToEntExpertFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS)
                .sdoWrittenQuestionsDate(LocalDate.now().plusDays(336))
                .sdoWrittenQuestionsDigPortalTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL)
                .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED)
                .sdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(350))
                .sdoShallBeUploadedTxt(SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED)
                .build());
    }
}
