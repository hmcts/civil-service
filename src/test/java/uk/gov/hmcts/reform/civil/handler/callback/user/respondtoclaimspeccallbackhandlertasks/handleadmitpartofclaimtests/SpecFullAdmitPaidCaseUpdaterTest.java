package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.SpecFullAdmitPaidCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SpecFullAdmitPaidCaseUpdaterTest {

    @InjectMocks
    private SpecFullAdmitPaidCaseUpdater updater;

    private CaseData.CaseDataBuilder<?, ?> caseDataBuilder;

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldSetSpecFullAdmitPaidToNo_whenConditionsAreMet() {
        CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(NO)
                .build();

        updater.update(caseData, caseDataBuilder);

        assertThat(caseDataBuilder.build().getSpecFullAdmitPaid()).isEqualTo(NO);
    }

    @Test
    void shouldNotSetSpecFullAdmitPaid_whenConditionsAreNotMet() {
        CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YES)
                .build();

        updater.update(caseData, caseDataBuilder);

        assertThat(caseDataBuilder.build().getSpecFullAdmitPaid()).isNull();
    }
}