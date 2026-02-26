package uk.gov.hmcts.reform.civil.model.docmosis.settlementagreement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.AdditionalLipPartyDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SettlementAgreementFormMapperTest {

    private static final String INDIVIDUAL_TITLE = "Mr.";
    private static final String INDIVIDUAL_FIRST_NAME = "FirstName";
    private static final String INDIVIDUAL_LAST_NAME = "LastName";
    private static final String EMAIL = "test@email.com";
    private static final LocalDateTime SUBMITTED_DATE = LocalDateTime.of(2023, 12, 1, 0, 0, 0);
    @InjectMocks
    private SettlementAgreementFormMapper settlementAgreementFormMapper;

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
                .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                        .setFirstRepaymentDate(LocalDate.now().plusDays(5))
                        .setPaymentAmount(BigDecimal.valueOf(200))
                        .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                        )
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
                .caseDataLiP(new CaseDataLiP()
                        .setApplicant1AdditionalLipPartyDetails(new AdditionalLipPartyDetails())
                        .setRespondent1AdditionalLipPartyDetails(new AdditionalLipPartyDetails()))
                .build();
        return caseData;
    }
}
