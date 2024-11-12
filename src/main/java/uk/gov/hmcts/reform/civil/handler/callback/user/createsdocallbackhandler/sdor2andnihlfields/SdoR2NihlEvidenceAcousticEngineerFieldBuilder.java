package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.sdor2andnihlfields;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;

import java.time.LocalDate;

@Slf4j
@Component
public class SdoR2NihlEvidenceAcousticEngineerFieldBuilder implements SdoR2AndNihlFieldsCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder()
                .sdoEvidenceAcousticEngineerTxt(SdoR2UiConstantFastTrack.EVIDENCE_ACOUSTIC_ENGINEER)
                .sdoInstructionOfTheExpertTxt(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT)
                .sdoInstructionOfTheExpertDate(LocalDate.now().plusDays(42))
                .sdoInstructionOfTheExpertTxtArea(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT_TA)
                .sdoExpertReportTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT)
                .sdoExpertReportDate(LocalDate.now().plusDays(280))
                .sdoExpertReportDigitalPortalTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT_DIGITAL_PORTAL)
                .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS)
                .sdoWrittenQuestionsDate(LocalDate.now().plusDays(294))
                .sdoWrittenQuestionsDigitalPortalTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS_DIGITAL_PORTAL)
                .sdoRepliesTxt(SdoR2UiConstantFastTrack.REPLIES)
                .sdoRepliesDate(LocalDate.now().plusDays(315))
                .sdoRepliesDigitalPortalTxt(SdoR2UiConstantFastTrack.REPLIES_DIGITAL_PORTAL)
                .sdoServiceOfOrderTxt(SdoR2UiConstantFastTrack.SERVICE_OF_ORDER)
                .build());
    }
}
