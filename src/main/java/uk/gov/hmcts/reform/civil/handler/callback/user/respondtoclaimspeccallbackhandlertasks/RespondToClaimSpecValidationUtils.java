package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.DefendantAddressValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Component
@RequiredArgsConstructor
public class RespondToClaimSpecValidationUtils implements DefendantAddressValidator, ExpertsValidator, WitnessesValidator {

    private final DateOfBirthValidator dateOfBirthValidator;
    private final UnavailableDateValidator unavailableDateValidator;
    private final PostcodeValidator postcodeValidator;
    private final PaymentDateValidator paymentDateValidator;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;

    public CallbackResponse validateMediationUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        if (caseData.getResp1MediationAvailability() != null
            && YES.equals(caseData.getResp1MediationAvailability().getIsMediationUnavailablityExists())) {
            RespondToClaimSpecUtilsDisputeDetails.checkUnavailable(errors, caseData.getResp1MediationAvailability().getUnavailableDatesForMediation());
        } else if (caseData.getResp2MediationAvailability() != null
            && YES.equals(caseData.getResp2MediationAvailability().getIsMediationUnavailablityExists())) {
            RespondToClaimSpecUtilsDisputeDetails.checkUnavailable(errors, caseData.getResp2MediationAvailability().getUnavailableDatesForMediation());
        }

        return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
    }

    public CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors;
        if (SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            SmallClaimHearing smallClaimHearing = getSmallClaimHearing(caseData);
            errors = unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing);
        } else {
            Hearing hearingLRspec = caseData.getRespondent1DQ().getRespondent1DQHearingFastClaim();
            errors = unavailableDateValidator.validateFastClaimHearing(hearingLRspec);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
    }

    private SmallClaimHearing getSmallClaimHearing(CaseData caseData) {
        return YES.equals(caseData.getIsRespondent2()) ? caseData.getRespondent2DQ().getRespondent2DQHearingSmallClaim()
            : caseData.getRespondent1DQ().getRespondent1DQHearingSmallClaim();
    }

    public CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Party respondent = Optional.ofNullable(caseData.getRespondent1()).orElse(caseData.getRespondent2());
        List<String> errors = dateOfBirthValidator.validate(respondent);

        errors.addAll(correspondenceAddressCorrect(caseData));
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        handleMultiPartyScenario(callbackParams, caseData, updatedData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private void handleMultiPartyScenario(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        if ((ONE_V_TWO_TWO_LEGAL_REP.equals(scenario) || ONE_V_TWO_ONE_LEGAL_REP.equals(scenario)) && YES.equals(caseData.getAddRespondent2())) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
                && solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
                updatedData.sameSolicitorSameResponse(YES);
            } else {
                updatedData.sameSolicitorSameResponse(NO);
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(scenario) && YES.equals(caseData.getAddRespondent2())) {
            updatedData.sameSolicitorSameResponse(NO.equals(caseData.getRespondentResponseIsSame()) ? NO : YES);
        }
    }

    public CallbackResponse validateRespondentPaymentDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = paymentDateValidator.validate(
            Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec()).orElse(RespondToClaimAdmitPartLRspec.builder().build())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
    }

    public CallbackResponse validateLengthOfUnemployment(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (caseData.getRespondToClaimAdmitPartUnemployedLRspec() != null
            && caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment() != null
            && (caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment()
            .getNumberOfYearsInUnemployment().contains(".")
            || caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment()
            .getNumberOfMonthsInUnemployment().contains("."))) {
            errors.add("Length of time unemployed must be a whole number, for example, 10.");
        }

        return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
    }

    public CallbackResponse validateDefendant1RepaymentPlan(CallbackParams callbackParams) {
        return validateRepaymentPlan(callbackParams.getCaseData().getRespondent1RepaymentPlan());
    }

    public CallbackResponse validateDefendant2RepaymentPlan(CallbackParams callbackParams) {
        return validateRepaymentPlan(callbackParams.getCaseData().getRespondent2RepaymentPlan());
    }

    public CallbackResponse validateRepaymentPlan(RepaymentPlanLRspec repaymentPlan) {
        List<String> errors = repaymentPlan != null && repaymentPlan.getFirstRepaymentDate() != null
            ? unavailableDateValidator.validateFuturePaymentDate(repaymentPlan.getFirstRepaymentDate())
            : new ArrayList<>();
        return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
    }

    public CallbackResponse validateCorrespondenceApplicantAddress(CallbackParams callbackParams) {
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            return validateCorrespondenceApplicantAddress(callbackParams, postcodeValidator);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    public CallbackResponse validateRespondentWitnesses(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!ONE_V_ONE.equals(getMultiPartyScenario(caseData))) {
            return validateWitnessesForMultipleRespondents(callbackParams, caseData);
        }
        return validateR1Witnesses(caseData);
    }

    private CallbackResponse validateWitnessesForMultipleRespondents(CallbackParams callbackParams, CaseData caseData) {
        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            return validateR1Witnesses(caseData);
        } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            return validateWitnesses(caseData.getRespondent2DQ());
        } else if (RespondToClaimSpecUtilsDisputeDetails.respondent2HasSameLegalRep(caseData)
            && NO.equals(caseData.getRespondentResponseIsSame())
            && caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null) {
            return validateWitnesses(caseData.getRespondent2DQ());
        }
        return validateR1Witnesses(caseData);
    }

    private CallbackResponse validateR1Witnesses(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (YES.equals(caseData.getRespondent1DQWitnessesRequiredSpec())
            && caseData.getRespondent1DQWitnessesDetailsSpec() == null) {
            errors.add("Witness details required");
        }
        return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
    }

    public CallbackResponse validateRespondentExperts(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!ONE_V_ONE.equals(getMultiPartyScenario(caseData))) {
            return validateExpertsForMultipleRespondents(callbackParams, caseData);
        }
        return validateExperts(caseData.getRespondent1DQ());
    }

    private CallbackResponse validateExpertsForMultipleRespondents(CallbackParams callbackParams, CaseData caseData) {
        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            return validateExperts(caseData.getRespondent1DQ());
        } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            return validateExperts(caseData.getRespondent2DQ());
        } else if (RespondToClaimSpecUtilsDisputeDetails.respondent2HasSameLegalRep(caseData)
            && NO.equals(caseData.getRespondentResponseIsSame())
            && caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondent2DQExperts() != null) {
            return validateExperts(caseData.getRespondent2DQ());
        }
        return validateExperts(caseData.getRespondent1DQ());
    }

    public List<String> correspondenceAddressCorrect(CaseData caseData) {
        if (YES.equals(caseData.getIsRespondent1())
            && NO.equals(caseData.getSpecAoSRespondentCorrespondenceAddressRequired())) {
            return postcodeValidator.validate(Optional.ofNullable(caseData.getSpecAoSRespondentCorrespondenceAddressdetails())
                                                  .map(Address::getPostCode).orElse(null));
        } else if (YES.equals(caseData.getIsRespondent2())
            && NO.equals(caseData.getSpecAoSRespondent2CorrespondenceAddressRequired())) {
            return postcodeValidator.validate(Optional.ofNullable(caseData.getSpecAoSRespondent2CorrespondenceAddressdetails())
                                                  .map(Address::getPostCode).orElse(null));
        }
        return Collections.emptyList();
    }

    public boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }
}
