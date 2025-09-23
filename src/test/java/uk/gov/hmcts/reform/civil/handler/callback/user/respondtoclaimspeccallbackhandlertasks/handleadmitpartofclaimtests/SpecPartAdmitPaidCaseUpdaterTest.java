package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.SpecPartAdmitPaidCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SpecPartAdmitPaidCaseUpdaterTest {

    @InjectMocks
    private SpecPartAdmitPaidCaseUpdater updater;

    private CaseData.CaseDataBuilder<?, ?> caseDataBuilder;

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldSetSpecPartAdmitPaidToNo_whenConditionsAreMet() {
        CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceAdmittedRequired(NO)
                .build();

        updater.update(caseData, caseDataBuilder);

        assertThat(caseDataBuilder.build().getSpecPartAdmitPaid()).isEqualTo(NO);
    }

    @Test
    void shouldNotSetSpecPartAdmitPaid_whenConditionsAreNotMet() {
        CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceAdmittedRequired(YES)
                .build();

        updater.update(caseData, caseDataBuilder);

        assertThat(caseDataBuilder.build().getSpecPartAdmitPaid()).isNull();
    }
}