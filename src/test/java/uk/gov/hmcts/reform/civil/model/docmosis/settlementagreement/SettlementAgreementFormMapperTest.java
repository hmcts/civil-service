package uk.gov.hmcts.reform.civil.model.docmosis.settlementagreement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.AdditionalLipPartyDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.ResponseRepaymentDetailsFormMapper;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.JudgmentAndSettlementAmountsCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettlementAgreementFormMapperTest {

    private static final String INDIVIDUAL_TITLE = "Mr.";
    private static final String INDIVIDUAL_FIRST_NAME = "FirstName";
    private static final String INDIVIDUAL_LAST_NAME = "LastName";
    private static final String EMAIL = "test@email.com";
    private static final LocalDateTime SUBMITTED_DATE = LocalDateTime.of(2023, 12, 1, 0, 0, 0);

    @Mock
    JudgmentAndSettlementAmountsCalculator judgmentAndSettlementAmountsCalculator;
    @Mock
    ResponseRepaymentDetailsFormMapper responseRepaymentDetailsFormMapper;

    @InjectMocks
    private SettlementAgreementFormMapper settlementAgreementFormMapper;

    @BeforeEach
    void setup() {
        when(judgmentAndSettlementAmountsCalculator.getSettlementAmount(any())).thenReturn(BigDecimal.valueOf(100));
    }

    @Test
    void shouldGenerateSettlementAgreementDoc_whenRepaymentTypeSetByDate() {
        //Given
        CaseData caseData = getCaseData();
        //When
        SettlementAgreementForm form = settlementAgreementFormMapper.buildFormData(caseData);
        //Then
        assertThat(form.getClaimant().name()).isEqualTo(caseData.getApplicant1().getPartyName());
        assertThat(form.getDefendant().name()).isEqualTo(caseData.getRespondent1().getPartyName());
    }

    @Test
    void shouldGenerateSettlementAgreementDoc_whenRepaymentTypeInstallments() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                .firstRepaymentDate(LocalDate.now().plusDays(5))
                .paymentAmount(BigDecimal.valueOf(200))
                .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                .build())
            .build();
        //When
        SettlementAgreementForm form = settlementAgreementFormMapper.buildFormData(caseData);
        //Then
        assertThat(form.getClaimant().name()).isEqualTo(caseData.getApplicant1().getPartyName());
        assertThat(form.getDefendant().name()).isEqualTo(caseData.getRespondent1().getPartyName());
    }

    private static CaseData getCaseData() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                .individualLastName(INDIVIDUAL_LAST_NAME)
                .individualFirstName(INDIVIDUAL_FIRST_NAME)
                .individualTitle(INDIVIDUAL_TITLE)
                .partyEmail(EMAIL)
                .type(Party.Type.INDIVIDUAL)
                .build())
            .respondent1(Party.builder()
                .individualLastName(INDIVIDUAL_LAST_NAME)
                .individualFirstName(INDIVIDUAL_FIRST_NAME)
                .individualTitle(INDIVIDUAL_TITLE)
                .partyEmail(EMAIL)
                .type(Party.Type.INDIVIDUAL)
                .build())
            .totalClaimAmount(new BigDecimal(150000))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .submittedDate(SUBMITTED_DATE)
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1AdditionalLipPartyDetails(AdditionalLipPartyDetails
                    .builder()
                    .build())
                .respondent1AdditionalLipPartyDetails(AdditionalLipPartyDetails
                    .builder()
                    .build())
                .build())
            .build();
        return caseData;
    }
}
