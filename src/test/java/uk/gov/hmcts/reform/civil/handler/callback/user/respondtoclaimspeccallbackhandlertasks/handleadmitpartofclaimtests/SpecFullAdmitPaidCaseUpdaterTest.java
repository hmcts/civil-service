package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.SpecFullAdmitPaidCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SpecFullAdmitPaidCaseUpdaterTest {

    @InjectMocks
    private SpecFullAdmitPaidCaseUpdater updater;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldSetSpecFullAdmitPaidToNo_whenConditionsAreMet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setSpecDefenceFullAdmittedRequired(NO);

        updater.update(caseData);

        assertThat(caseData.getSpecFullAdmitPaid()).isEqualTo(NO);
    }

    @Test
    void shouldNotSetSpecFullAdmitPaid_whenConditionsAreNotMet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setSpecDefenceFullAdmittedRequired(YES);

        updater.update(caseData);

        assertThat(caseData.getSpecFullAdmitPaid()).isNull();
    }
}
