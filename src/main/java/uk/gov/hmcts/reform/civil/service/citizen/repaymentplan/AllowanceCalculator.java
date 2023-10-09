package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChildrenByAgeGroupLRspec;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnemployedComplexTypeLRspec;
import uk.gov.hmcts.reform.civil.model.allowance.DisabilityAllowance;
import uk.gov.hmcts.reform.civil.model.allowance.DisabilityParam;
import uk.gov.hmcts.reform.civil.model.allowance.PensionerAllowance;
import uk.gov.hmcts.reform.civil.model.allowance.PersonalAllowance;
import uk.gov.hmcts.reform.civil.model.citizenui.FinancialDetailsLiP;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Component
public class AllowanceCalculator {

    public static final double DEPENDANT_MONTHLY_ALLOWANCE = 289.90;
    public static final String RETIRED = "RETIRED";

    public double calculateAllowance(CaseData caseData) {
        Optional<PartnerAndDependentsLRspec> partnerAndDependantInformation = Optional.ofNullable(caseData.getRespondent1PartnerAndDependent());
        FinancialDetailsLiP defendantFinancialDetailsLiP = caseData.getCaseDataLiP().getRespondent1LiPFinancialDetails();
        double personalAllowance = calculatePersonalAllowance(
            partnerAndDependantInformation,
            caseData.getRespondent1()
        );
        double dependantsAllowance = calculateDependantsAllowance(partnerAndDependantInformation);
        double disabilityAllowance = calculateDisabilityAllowance(
            partnerAndDependantInformation,
            caseData.getDisabilityPremiumPayments(),
            caseData.getSevereDisabilityPremiumPayments(),
            caseData.getRespondent1DQ(),
            defendantFinancialDetailsLiP
        );
        double pensionerAllowance = calculatePensionerAllowance(
            defendantFinancialDetailsLiP,
            caseData.getRespondToClaimAdmitPartUnemployedLRspec()
        );
        return personalAllowance + dependantsAllowance + disabilityAllowance + pensionerAllowance;
    }

    private double calculatePersonalAllowance(Optional<PartnerAndDependentsLRspec> partnerAndDependantInformation, Party defendant) {
        int defendantAge = calculateDefendantAge(defendant);
        boolean hasPartner = getHasPartner(partnerAndDependantInformation);
        boolean partnerOver18 = YesOrNo.YES == partnerAndDependantInformation.map(PartnerAndDependentsLRspec::getPartnerAgedOver).orElse(null);
        return PersonalAllowance.getPersonalAllowance(defendantAge, hasPartner, partnerOver18).getAllowance();
    }

    private boolean getHasPartner(Optional<PartnerAndDependentsLRspec> partnerAndDependantInformation) {
        return partnerAndDependantInformation.map(PartnerAndDependentsLRspec::hasPartner).orElse(false);
    }

    private double calculateDependantsAllowance(Optional<PartnerAndDependentsLRspec> partnerAndDependantInformation) {
        int numberOfSupportedPeople = partnerAndDependantInformation.map(PartnerAndDependentsLRspec::getSupportPeopleNumber)
            .map(Integer::parseInt).orElse(0);
        int numberOfChildren = partnerAndDependantInformation.map(PartnerAndDependentsLRspec::getHowManyChildrenByAgeGroup)
            .map(ChildrenByAgeGroupLRspec::getTotalChildren).orElse(0);
        return DEPENDANT_MONTHLY_ALLOWANCE * (numberOfSupportedPeople + numberOfChildren);
    }


    private double calculateDisabilityAllowance(Optional<PartnerAndDependentsLRspec> partnerAndDependantInformation,
                                                YesOrNo disabilityPremiumPayments,
                                                YesOrNo severeDisability,
                                                Respondent1DQ respondent1DQ,
                                                FinancialDetailsLiP financialDetailsLiP) {
        boolean disabled = YesOrNo.YES == disabilityPremiumPayments;
        boolean severelyDisabled = YesOrNo.YES == severeDisability
            || YesOrNo.YES == financialDetailsLiP.getPartnerSevereDisabilityLiP();
        boolean hasPartner = getHasPartner(partnerAndDependantInformation);
        boolean dependantDisabled = YesOrNo.YES == partnerAndDependantInformation.map(PartnerAndDependentsLRspec::getReceiveDisabilityPayments).orElse(YesOrNo.NO);
        YesOrNo carerOption = getCarerOption(respondent1DQ);
        boolean carer = YesOrNo.YES == carerOption;
        return DisabilityAllowance
            .getDisabilityAllowance(new DisabilityParam(
                disabled,
                hasPartner,
                severelyDisabled,
                dependantDisabled,
                carer
            ));

    }

    private YesOrNo getCarerOption(Respondent1DQ respondent1DQ) {
        if(respondent1DQ == null) {
            return YesOrNo.NO;
        }
        return Optional.ofNullable(respondent1DQ.getRespondent1DQCarerAllowanceCreditFullAdmission())
            .orElse(respondent1DQ.getRespondent1DQCarerAllowanceCredit());
    }

    private double calculatePensionerAllowance(FinancialDetailsLiP financialDetailsLiP, UnemployedComplexTypeLRspec uneployedType) {
        boolean partnerPensioner = YesOrNo.YES == financialDetailsLiP.getPartnerPensionLiP();
        boolean defendantPensioner = RETIRED == Optional.ofNullable(uneployedType)
            .map(UnemployedComplexTypeLRspec::getUnemployedComplexTypeRequired).orElse("");
        return PensionerAllowance.getPensionerAllowance(defendantPensioner, partnerPensioner);
    }

    private int calculateDefendantAge(Party defendant) {
        LocalDate dateOfBirth = defendant.getDateOfBirth();
        LocalDate now = LocalDate.now();
        return dateOfBirth != null ? Period.between(now, dateOfBirth).getYears() : 0;
    }

}
