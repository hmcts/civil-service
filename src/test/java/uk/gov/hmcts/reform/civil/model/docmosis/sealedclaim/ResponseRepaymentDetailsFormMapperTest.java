package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.JudgmentAndSettlementAmountsCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;

class ResponseRepaymentDetailsFormMapperTest {

    @Mock
    private JudgmentAndSettlementAmountsCalculator judgmentAndSettlementAmountsCalculator;

    @InjectMocks
    private ResponseRepaymentDetailsFormMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldMapToResponsePaymentDetails_whenFullAdmissionAndPayImmediately() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                .whenWillThisAmountBePaid(LocalDate.now().plusDays(7))
                .build())
            .build();
        when(judgmentAndSettlementAmountsCalculator.getSettlementAmount(any(CaseData.class)))
            .thenReturn(new BigDecimal("1000.00"));

        ResponseRepaymentDetailsForm form = mapper.toResponsePaymentDetails(caseData);

        assertThat(form.getResponseType()).isEqualTo(FULL_ADMISSION);
        assertThat(form.getPayBy()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(form.getAmountToPay()).isEqualTo("1000.00");
    }

    @Test
    void shouldMapToResponsePaymentDetails_whenFullAdmissionAndPayImmediatelyRepsondent2() {
        CaseData caseData = CaseData.builder()
            .respondent2ClaimResponseTypeForSpec(FULL_DEFENCE)
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .respondent2(Party.builder().build())
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondent2ResponseDate(LocalDateTime.now())
            .respondent1ResponseDate(LocalDateTime.now().minusDays(3))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                .whenWillThisAmountBePaid(LocalDate.now().plusDays(7))
                .build())
            .build();
        when(judgmentAndSettlementAmountsCalculator.getSettlementAmount(any(CaseData.class)))
            .thenReturn(new BigDecimal("1000.00"));

        ResponseRepaymentDetailsForm form = mapper.toResponsePaymentDetails(caseData);

        assertThat(form.getResponseType()).isEqualTo(FULL_ADMISSION);
        assertThat(form.getPayBy()).isNull();
    }
}
