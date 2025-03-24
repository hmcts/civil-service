package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultJudgmentFormBuilderTest {

    @Mock
    private InterestCalculator interestCalculator;
    @Mock
    private JudgmentAndSettlementAmountsCalculator judgmentAndSettlementAmountsCalculator;
    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private DefaultJudgmentFormBuilder defaultJudgmentFormBuilder;

    @BeforeEach
    void setUp() {

        when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder().name("org name")
            .contactInformation(Arrays.asList(ContactInformation.builder()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .postCode("postCode")
                .build())).build()));
    }

    @Test
    void shouldReturnDefaultJudgmentFormWithCorrectAmounts() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(1000.00))
            .legacyCaseReference("12345")
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(1000)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();
        uk.gov.hmcts.reform.civil.model.Party respondent = PartyBuilder.builder().individual().build();
        when(judgmentAndSettlementAmountsCalculator.getClaimFee(any())).thenReturn(new BigDecimal("50.00"));
        when(judgmentAndSettlementAmountsCalculator.getDebtAmount(any())).thenReturn(new BigDecimal("1006.00"));

        DefaultJudgmentForm form = defaultJudgmentFormBuilder.getDefaultJudgmentForm(caseData, respondent, CaseEvent.GENERATE_DJ_FORM_SPEC.name(), false);

        assertThat(form.getCaseNumber()).isEqualTo("12345");
        assertThat(form.getDebt()).isEqualTo("1006.00");
        assertThat(form.getCosts()).isEqualTo("50.00");
        assertThat(form.getTotalCost()).isEqualTo("1056.00");
    }

    @Test
    void shouldReturnAllocateDebtAmountToCostsIfDebtAmountAfterPartialPaymentIsNegative() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(1000.00))
            .legacyCaseReference("12345")
            .partialPaymentAmount("200000")
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(1000)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();
        uk.gov.hmcts.reform.civil.model.Party respondent = PartyBuilder.builder().individual().build();
        when(judgmentAndSettlementAmountsCalculator.getClaimFee(any())).thenReturn(new BigDecimal("50.00"));
        when(judgmentAndSettlementAmountsCalculator.getDebtAmount(any())).thenReturn(new BigDecimal("-45.00"));

        DefaultJudgmentForm form = defaultJudgmentFormBuilder.getDefaultJudgmentForm(caseData, respondent, CaseEvent.GENERATE_DJ_FORM_SPEC.name(), false);

        assertThat(form.getCaseNumber()).isEqualTo("12345");
        assertThat(form.getDebt()).isEqualTo("0");
        assertThat(form.getCosts()).isEqualTo("5.00");
        assertThat(form.getTotalCost()).isEqualTo("5.00");
    }

    @Test
    void shouldReturnDefaultJudgmentFormWithApplicantAndRespondentReferences_whenSolicitorReferencesAreProvided() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .applicant1(Party.builder().organisationName("Applicant1 name").type(Party.Type.ORGANISATION).build())
            .applicant2(Party.builder().organisationName("Applicant2 name").type(Party.Type.ORGANISATION).build())
            .respondent1(Party.builder().organisationName("Respondent1 name").type(Party.Type.ORGANISATION).build())
            .respondent2(Party.builder().organisationName("Respondent2 name").type(Party.Type.ORGANISATION).build())
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();

        DefaultJudgmentForm form = defaultJudgmentFormBuilder.getDefaultJudgmentForm(caseData, caseData.getRespondent1(), "event", true);

        assertThat(form.getApplicantReference()).isEqualTo("12345");
        assertThat(form.getRespondentReference()).isEqualTo("6789");
        assertThat(form.getApplicant().size()).isEqualTo(2);
        assertThat(form.getApplicant().get(0).getName()).isEqualTo("Applicant1 name");
        assertThat(form.getApplicant().get(1).getName()).isEqualTo(" and Applicant2 name");

    }
}
