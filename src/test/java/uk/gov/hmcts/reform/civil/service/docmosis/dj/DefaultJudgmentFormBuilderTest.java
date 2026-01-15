package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultJudgmentFormBuilderTest {

    @Mock
    private InterestCalculator interestCalculator;
    @Mock
    private JudgmentAmountsCalculator judgmentAmountsCalculator;
    @Mock
    private OrganisationService organisationService;

    private DefaultJudgmentFormBuilder defaultJudgmentFormBuilder;

    @BeforeEach
    void setUp() {
        defaultJudgmentFormBuilder = new DefaultJudgmentFormBuilder(
            interestCalculator,
            judgmentAmountsCalculator,
            organisationService,
            new DjWelshTextService()
        );

        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setAddressLine2("addressLine2");
        contactInformation.setAddressLine3("addressLine3");
        contactInformation.setPostCode("postCode");
        Organisation organisation = new Organisation();
        organisation.setName("org name");
        organisation.setContactInformation(Collections.singletonList(contactInformation));
        when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(organisation));
    }

    @Test
    void shouldReturnDefaultJudgmentFormWithCorrectAmounts() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal("1000.00"))
            .legacyCaseReference("12345")
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(1000)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();
        uk.gov.hmcts.reform.civil.model.Party respondent = PartyBuilder.builder().individual().build();
        when(judgmentAmountsCalculator.getClaimFee(any())).thenReturn(new BigDecimal("50.00"));
        when(judgmentAmountsCalculator.getDebtAmount(any())).thenReturn(new BigDecimal("1006.00"));

        DefaultJudgmentForm form = defaultJudgmentFormBuilder.getDefaultJudgmentForm(caseData, respondent, CaseEvent.GENERATE_DJ_FORM_SPEC.name(), false);

        assertThat(form.getCaseNumber()).isEqualTo("12345");
        assertThat(form.getDebt()).isEqualTo("1006.00");
        assertThat(form.getCosts()).isEqualTo("50.00");
        assertThat(form.getTotalCost()).isEqualTo("1056.00");
    }

    @Test
    void shouldReturnAllocateDebtAmountToCostsIfDebtAmountAfterPartialPaymentIsNegative() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal("1000.00"))
            .legacyCaseReference("12345")
            .partialPaymentAmount("200000")
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(1000)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();
        uk.gov.hmcts.reform.civil.model.Party respondent = PartyBuilder.builder().individual().build();
        when(judgmentAmountsCalculator.getClaimFee(any())).thenReturn(new BigDecimal("50.00"));
        when(judgmentAmountsCalculator.getDebtAmount(any())).thenReturn(new BigDecimal("-45.00"));

        DefaultJudgmentForm form = defaultJudgmentFormBuilder.getDefaultJudgmentForm(caseData, respondent, CaseEvent.GENERATE_DJ_FORM_SPEC.name(), false);

        assertThat(form.getCaseNumber()).isEqualTo("12345");
        assertThat(form.getDebt()).isEqualTo("0");
        assertThat(form.getCosts()).isEqualTo("5.00");
        assertThat(form.getTotalCost()).isEqualTo("5.00");
    }

    @Test
    void shouldReturnDefaultJudgmentFormWithApplicantAndRespondentReferences_whenSolicitorReferencesAreProvided() {
        Party applicant1 = new Party();
        applicant1.setOrganisationName("Applicant1 name");
        applicant1.setType(Party.Type.ORGANISATION);
        Party applicant2 = new Party();
        applicant2.setOrganisationName("Applicant2 name");
        applicant2.setType(Party.Type.ORGANISATION);
        Party respondent1 = new Party();
        respondent1.setOrganisationName("Respondent1 name");
        respondent1.setType(Party.Type.ORGANISATION);
        Party respondent2 = new Party();
        respondent2.setOrganisationName("Respondent2 name");
        respondent2.setType(Party.Type.ORGANISATION);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .applicant1(applicant1)
            .applicant2(applicant2)
            .respondent1(respondent1)
            .respondent2(respondent2)
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
