package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.sdor2andnihlfields;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurtherDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Slf4j
@Component
public class SdoR2NihlQuestionsClaimantExpertFieldBuilder implements SdoR2AndNihlFieldsCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                .sdoDefendantMayAskTxt(SdoR2UiConstantFastTrack.DEFENDANT_MAY_ASK)
                .sdoDefendantMayAskDate(LocalDate.now().plusDays(126))
                .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.QUESTIONS_SHALL_BE_ANSWERED)
                .sdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(147))
                .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL)
                .sdoApplicationToRelyOnFurther(SdoR2ApplicationToRelyOnFurther.builder()
                        .doRequireApplicationToRely(NO)
                        .applicationToRelyOnFurtherDetails(SdoR2ApplicationToRelyOnFurtherDetails.builder()
                                .applicationToRelyDetailsTxt(SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS)
                                .applicationToRelyDetailsDate(LocalDate.now().plusDays(161))
                                .build())
                        .build())
                .build());
    }
}
