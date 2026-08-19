package uk.gov.hmcts.reform.civil.service.docmosis.dj.nondivergent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DjWelshTextService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.JudgmentAmountsCalculator;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NonDivergentSpecDefaultJudgementFormBuilderTest {

    private static final String RESPONDENT_1 = "respondent1";

    @Mock
    private InterestCalculator interestCalculator;
    @Mock
    private JudgmentAmountsCalculator judgmentAmountsCalculator;
    @Mock
    private OrganisationService organisationService;

    private NonDivergentSpecDefaultJudgementFormBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new NonDivergentSpecDefaultJudgementFormBuilder(
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
    void shouldUseActiveJudgmentAmountsWhenProvided() {
        CaseData caseData = caseDataWithActiveJudgment(new JudgmentDetails()
                                                           .setOrderedAmount("14000")
                                                           .setTotalAmount("15000"));
        when(judgmentAmountsCalculator.getClaimFee(any())).thenReturn(new BigDecimal("10.00"));

        DefaultJudgmentForm form = builder.getDefaultJudgmentForm(caseData, RESPONDENT_1);

        assertThat(form.getDebt()).isEqualTo("140.00");
        assertThat(form.getCosts()).isEqualTo("10.00");
        assertThat(form.getTotalCost()).isEqualTo("150.00");
    }

    @Test
    void shouldUseCalculatedAmountsWhenActiveJudgmentIsNull() {
        CaseData caseData = caseDataWithActiveJudgment(null);
        when(judgmentAmountsCalculator.getClaimFee(any())).thenReturn(new BigDecimal("10.00"));
        when(judgmentAmountsCalculator.getDebtAmount(any())).thenReturn(new BigDecimal("140.00"));

        DefaultJudgmentForm form = builder.getDefaultJudgmentForm(caseData, RESPONDENT_1);

        assertThat(form.getDebt()).isEqualTo("140.00");
        assertThat(form.getCosts()).isEqualTo("10.00");
        assertThat(form.getTotalCost()).isEqualTo("150.00");
    }

    @Test
    void shouldUseCalculatedDebtWhenActiveJudgmentOrderedAmountIsNull() {
        CaseData caseData = caseDataWithActiveJudgment(new JudgmentDetails()
                                                           .setTotalAmount("15000"));
        when(judgmentAmountsCalculator.getClaimFee(any())).thenReturn(new BigDecimal("10.00"));
        when(judgmentAmountsCalculator.getDebtAmount(any())).thenReturn(new BigDecimal("140.00"));

        DefaultJudgmentForm form = builder.getDefaultJudgmentForm(caseData, RESPONDENT_1);

        assertThat(form.getDebt()).isEqualTo("140.00");
        assertThat(form.getCosts()).isEqualTo("10.00");
        assertThat(form.getTotalCost()).isEqualTo("150.00");
    }

    @Test
    void shouldUseCalculatedTotalWhenActiveJudgmentTotalAmountIsNull() {
        CaseData caseData = caseDataWithActiveJudgment(new JudgmentDetails()
                                                           .setOrderedAmount("14000"));
        when(judgmentAmountsCalculator.getClaimFee(any())).thenReturn(new BigDecimal("10.00"));

        DefaultJudgmentForm form = builder.getDefaultJudgmentForm(caseData, RESPONDENT_1);

        assertThat(form.getDebt()).isEqualTo("140.00");
        assertThat(form.getCosts()).isEqualTo("10.00");
        assertThat(form.getTotalCost()).isEqualTo("150.00");
    }

    private CaseData caseDataWithActiveJudgment(JudgmentDetails activeJudgment) {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
            .totalClaimAmount(new BigDecimal("140.00"))
            .claimFee(new Fee().setCalculatedAmountInPence(new BigDecimal("1000")))
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();
        caseData.setActiveJudgment(activeJudgment);
        return caseData;
    }
}
