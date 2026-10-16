package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.SpecPartAdmitPaidCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SpecPartAdmitPaidCaseUpdaterTest {

    @InjectMocks
    private SpecPartAdmitPaidCaseUpdater updater;

    @Test
    void shouldSetSpecPartAdmitPaidToNo_whenConditionsAreMet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmittedRequired(NO);

        updater.update(caseData);

        assertThat(caseData.getSpecPartAdmitPaid()).isEqualTo(NO);
    }

    @Test
    void shouldNotSetSpecPartAdmitPaid_whenConditionsAreNotMet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmittedRequired(YES);

        updater.update(caseData);

        assertThat(caseData.getSpecPartAdmitPaid()).isNull();
    }

    @Test
    void shouldSetSpecPartAdmitPaidToNo_whenRespondent2PartAdmissionWillPay() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YES);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmitted2Required(NO);

        updater.update(caseData);

        assertThat(caseData.getSpecPartAdmitPaid()).isEqualTo(NO);
    }

    @Test
    void shouldNotUseRespondent1Flags_whenCurrentDefendantIsRespondent2() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmittedRequired(NO);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmitted2Required(YES);

        updater.update(caseData);

        assertThat(caseData.getSpecPartAdmitPaid()).isNull();
    }

    @Test
    void shouldNotUseRespondent1PartAdmitFlags_whenCurrentDefendantIsRespondent2FullAdmit() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmittedRequired(NO);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setSpecDefenceAdmitted2Required(NO);

        updater.update(caseData);

        assertThat(caseData.getSpecPartAdmitPaid()).isNull();
    }
}
