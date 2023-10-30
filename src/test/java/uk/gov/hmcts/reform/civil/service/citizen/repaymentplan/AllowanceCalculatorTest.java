package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChildrenByAgeGroupLRspec;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnemployedComplexTypeLRspec;
import uk.gov.hmcts.reform.civil.model.allowance.DisabilityAllowance;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.FinancialDetailsLiP;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.model.allowance.PensionerAllowance.SINGLE;
import static uk.gov.hmcts.reform.civil.model.allowance.PersonalAllowance.SINGLE_UNDER_25;
import static uk.gov.hmcts.reform.civil.service.citizen.repaymentplan.AllowanceCalculator.RETIRED;

@ExtendWith(MockitoExtension.class)
class AllowanceCalculatorTest {

    @Mock
    private CaseData caseData;
    @Mock
    private CaseDataLiP caseDataLiP;
    @Mock
    private PartnerAndDependentsLRspec partnerAndDependants;
    @Mock
    private ChildrenByAgeGroupLRspec children;
    @Mock
    private UnemployedComplexTypeLRspec unemploymentType;
    @Mock
    private FinancialDetailsLiP defendantFinancialDetailsLiP;
    @Mock
    private Party respondent1;

    private AllowanceCalculator allowanceCalculator;

    @BeforeEach
    void setUp() {
        allowanceCalculator = new AllowanceCalculator();
        given(caseData.getCaseDataLiP()).willReturn(caseDataLiP);
        given(caseDataLiP.getRespondent1LiPFinancialDetails()).willReturn(defendantFinancialDetailsLiP);
        given(caseData.getRespondent1()).willReturn(respondent1);
        given(respondent1.getDateOfBirth()).willReturn(LocalDate.of(2000, 1, 1));
    }

    @Test
    void shouldCalculateAllowanceWithNoPartnerInformation() {
        //Given
        double expectedResultWithPersonalAllowance = 250.90;
        //When
        double actualResult = allowanceCalculator.calculateAllowance(caseData);
        //Then
        assertThat(actualResult).isEqualTo(expectedResultWithPersonalAllowance);
    }

    @Test
    void shouldCalculateDependantsAllowance() {
        //Given
        given(caseData.getRespondent1PartnerAndDependent()).willReturn(partnerAndDependants);
        given(partnerAndDependants.getSupportPeopleNumber()).willReturn("2");
        given(partnerAndDependants.getHowManyChildrenByAgeGroup()).willReturn(children);
        given(children.getTotalChildren()).willReturn(3);
        double expectedResultWithDependantsAndPersonalSingleAllowance = 1700.4;
        //When
        double actualResult = allowanceCalculator.calculateAllowance(caseData);
        //Then
        assertThat(actualResult).isEqualTo(expectedResultWithDependantsAndPersonalSingleAllowance);
    }

    @Test
    void shouldCalculatePensionerAllowance() {
        //Given
        double expectedResultWithPersonalAllowanceAndPension = SINGLE.getAllowance() + SINGLE_UNDER_25.getAllowance();
        given(caseData.getRespondToClaimAdmitPartUnemployedLRspec()).willReturn(unemploymentType);
        given(unemploymentType.getUnemployedComplexTypeRequired()).willReturn(RETIRED);
        //When
        double actualResult = allowanceCalculator.calculateAllowance(caseData);
        //Then
        assertThat(actualResult).isEqualTo(expectedResultWithPersonalAllowanceAndPension);
    }

    @Test
    void shouldCalculateDisabilityAllowance() {
        //Given
        double expectedResultWithPersonalAllowanceAndSingleDisability = SINGLE_UNDER_25.getAllowance()
            + DisabilityAllowance.SINGLE.getAllowance();
        given(caseData.getDisabilityPremiumPayments()).willReturn(YesOrNo.YES);
        //When
        double actualResult = allowanceCalculator.calculateAllowance(caseData);
        //Then
        assertThat(actualResult).isEqualTo(expectedResultWithPersonalAllowanceAndSingleDisability);
    }
}
