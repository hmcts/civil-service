package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlEvidenceAcousticEngineerFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlEvidenceAcousticEngineerFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlEvidenceAcousticEngineerFieldBuilder sdoR2AndNihlEvidenceAcousticEngineerFieldBuilder;

    @Test
    void shouldBuildSdoR2EvidenceAcousticEngineer() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlEvidenceAcousticEngineerFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2EvidenceAcousticEngineer evidenceAcousticEngineer = caseData.getSdoR2EvidenceAcousticEngineer();

        assertEquals(SdoR2UiConstantFastTrack.EVIDENCE_ACOUSTIC_ENGINEER, evidenceAcousticEngineer.getSdoEvidenceAcousticEngineerTxt());
        assertEquals(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT, evidenceAcousticEngineer.getSdoInstructionOfTheExpertTxt());
        assertEquals(LocalDate.now().plusDays(42), evidenceAcousticEngineer.getSdoInstructionOfTheExpertDate());
        assertEquals(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT_TA, evidenceAcousticEngineer.getSdoInstructionOfTheExpertTxtArea());
        assertEquals(SdoR2UiConstantFastTrack.EXPERT_REPORT, evidenceAcousticEngineer.getSdoExpertReportTxt());
        assertEquals(LocalDate.now().plusDays(280), evidenceAcousticEngineer.getSdoExpertReportDate());
        assertEquals(SdoR2UiConstantFastTrack.EXPERT_REPORT_DIGITAL_PORTAL, evidenceAcousticEngineer.getSdoExpertReportDigitalPortalTxt());
        assertEquals(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS, evidenceAcousticEngineer.getSdoWrittenQuestionsTxt());
        assertEquals(LocalDate.now().plusDays(294), evidenceAcousticEngineer.getSdoWrittenQuestionsDate());
        assertEquals(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS_DIGITAL_PORTAL, evidenceAcousticEngineer.getSdoWrittenQuestionsDigitalPortalTxt());
        assertEquals(SdoR2UiConstantFastTrack.REPLIES, evidenceAcousticEngineer.getSdoRepliesTxt());
        assertEquals(LocalDate.now().plusDays(315), evidenceAcousticEngineer.getSdoRepliesDate());
        assertEquals(SdoR2UiConstantFastTrack.REPLIES_DIGITAL_PORTAL, evidenceAcousticEngineer.getSdoRepliesDigitalPortalTxt());
        assertEquals(SdoR2UiConstantFastTrack.SERVICE_OF_ORDER, evidenceAcousticEngineer.getSdoServiceOfOrderTxt());
    }
}