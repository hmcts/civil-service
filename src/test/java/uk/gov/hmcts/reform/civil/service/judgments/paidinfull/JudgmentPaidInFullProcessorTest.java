package uk.gov.hmcts.reform.civil.service.judgments.paidinfull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentPaidInFullOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.judgments.CjesService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class JudgmentPaidInFullProcessorTest {

    @Mock
    CjesService cjesService;
    JudgmentPaidInFullOnlineMapper judgmentPaidInFullOnlineMapper = new JudgmentPaidInFullOnlineMapper();
    JudgmentPaidInFullProcessor judgmentPaidInFullProcessor;

    @BeforeEach
    void setUp() {
        judgmentPaidInFullProcessor = new JudgmentPaidInFullProcessor(cjesService, judgmentPaidInFullOnlineMapper);
    }

    @Test
    void shouldProcessJudgmentPaidInFullAndSendJudgmentWhenJudgmentIsRegisteredWithRTL() {
        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31Days();
        caseData.setActiveJudgment(JudgmentDetails.builder().issueDate(LocalDate.now())
            .paymentPlan(JudgmentPaymentPlan.builder()
                .type(PaymentPlanSelection.PAY_IMMEDIATELY).build())
            .orderedAmount("100")
            .costs("50")
            .totalAmount("150")
            .isRegisterWithRTL(YES)
            .build());

        CaseData result = judgmentPaidInFullProcessor.process(caseData);

        assertThat(result.getJoIsLiveJudgmentExists()).isEqualTo(YesOrNo.YES);
        assertThat(result.getActiveJudgment()).isNotNull();
        verify(cjesService).sendJudgment(caseData, true);
    }

    @Test
    void shouldProcessJudgmentPaidInFullAndNotSendJudgmentWhenJudgmentIsNotRegisteredWithRTL() {
        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31Days();
        caseData.setActiveJudgment(JudgmentDetails.builder().issueDate(LocalDate.now())
            .paymentPlan(JudgmentPaymentPlan.builder()
                .type(PaymentPlanSelection.PAY_IMMEDIATELY).build())
            .orderedAmount("100")
            .costs("50")
            .totalAmount("150")
            .isRegisterWithRTL(NO)
            .build());

        CaseData result = judgmentPaidInFullProcessor.process(caseData);

        assertThat(result.getJoIsLiveJudgmentExists()).isEqualTo(YesOrNo.YES);
        assertThat(result.getActiveJudgment()).isNotNull();
        verifyNoInteractions(cjesService);
    }
}
