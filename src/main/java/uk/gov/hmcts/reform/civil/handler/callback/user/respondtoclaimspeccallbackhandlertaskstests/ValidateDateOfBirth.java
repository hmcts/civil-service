package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@RequiredArgsConstructor
public class ValidateDateOfBirth implements CaseTask {

    private final DateOfBirthValidator dateOfBirthValidator;
    private final PostcodeValidator postcodeValidator;
    private final ObjectMapper objectMapper;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Party respondent = getRespondent(caseData);
        List<String> errors = dateOfBirthValidator.validate(respondent);

        errors.addAll(correspondenceAddressCorrect(caseData));
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        updateSameSolicitorResponse(caseData, updatedData, callbackParams);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }


    private Party getRespondent(CaseData caseData) {
        Party respondent = caseData.getRespondent1();
        if (respondent == null && caseData.getRespondent2() != null) {
            respondent = caseData.getRespondent2();
        }
        return respondent;
    }

    private void updateSameSolicitorResponse(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, CallbackParams callbackParams) {
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData)) && YES.equals(caseData.getAddRespondent2())) {
            if (respondToClaimSpecUtils.solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
                && respondToClaimSpecUtils.solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
                updatedData.sameSolicitorSameResponse(YES).build();
            } else {
                updatedData.sameSolicitorSameResponse(NO).build();
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData)) && YES.equals(caseData.getAddRespondent2())) {
            if (NO.equals(caseData.getRespondentResponseIsSame())) {
                updatedData.sameSolicitorSameResponse(NO).build();
            } else {
                updatedData.sameSolicitorSameResponse(YES).build();
            }
        }
    }

    /**
     * Checks that the address of case data was ok when the applicant set it, or that its postcode is correct
     * if the defendant has modified.
     *
     * @param caseData the case data
     * @return errors of the correspondence address (if any)
     */
    private List<String> correspondenceAddressCorrect(CaseData caseData) {
        if (caseData.getIsRespondent1() == YesOrNo.YES) {
            return validateRespondent1Address(caseData);
        } else if (caseData.getIsRespondent2() == YesOrNo.YES) {
            return validateRespondent2Address(caseData);
        }
        return Collections.emptyList();
    }

    private List<String> validateRespondent1Address(CaseData caseData) {
        if (caseData.getSpecAoSRespondentCorrespondenceAddressRequired() == YesOrNo.NO) {
            return postcodeValidator.validate(
                Optional.ofNullable(caseData.getSpecAoSRespondentCorrespondenceAddressdetails())
                    .map(Address::getPostCode)
                    .orElse(null)
            );
        }
        return Collections.emptyList();
    }

    private List<String> validateRespondent2Address(CaseData caseData) {
        if (caseData.getSpecAoSRespondent2CorrespondenceAddressRequired() == YesOrNo.NO) {
            return postcodeValidator.validate(
                Optional.ofNullable(caseData.getSpecAoSRespondent2CorrespondenceAddressdetails())
                    .map(Address::getPostCode)
                    .orElse(null)
            );
        }
        return Collections.emptyList();
    }
}
