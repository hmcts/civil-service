package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ValidateDateOfBirth implements CaseTask {

    private final DateOfBirthValidator dateOfBirthValidator;
    private final PostcodeValidator postcodeValidator;
    private final ObjectMapper objectMapper;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    public CallbackResponse execute(CallbackParams callbackParams) {
        Party respondent = getRespondent(callbackParams);
        List<String> errors = dateOfBirthValidator.validate(respondent);

        CaseData caseData = callbackParams.getCaseData();
        errors.addAll(correspondenceAddressCorrect(caseData));
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        updateSolicitorResponse(callbackParams, caseData, updatedData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private Party getRespondent(CallbackParams callbackParams) {
        Party respondent = callbackParams.getCaseData().getRespondent1();
        if (respondent == null && callbackParams.getCaseData().getRespondent2() != null) {
            respondent = callbackParams.getCaseData().getRespondent2();
        }
        return respondent;
    }

    private void updateSolicitorResponse(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (isTwoLegalRepsScenario(caseData) && YES.equals(caseData.getAddRespondent2())) {
            updatedData.sameSolicitorSameResponse(
                respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
                    && respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE) ? YES : NO
            ).build();
        } else if (isOneLegalRepScenario(caseData) && YES.equals(caseData.getAddRespondent2())) {
            updatedData.sameSolicitorSameResponse(
                NO.equals(caseData.getRespondentResponseIsSame()) ? NO : YES
            ).build();
        }
    }

    private boolean isTwoLegalRepsScenario(CaseData caseData) {
        return ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData));
    }

    private boolean isOneLegalRepScenario(CaseData caseData) {
        return ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData));
    }

    private List<String> correspondenceAddressCorrect(CaseData caseData) {
        if (isCorrespondenceAddressRequired(caseData.getIsRespondent1(), caseData.getSpecAoSRespondentCorrespondenceAddressRequired())) {
            return validatePostcode(caseData.getSpecAoSRespondentCorrespondenceAddressdetails());
        } else if (isCorrespondenceAddressRequired(caseData.getIsRespondent2(), caseData.getSpecAoSRespondent2CorrespondenceAddressRequired())) {
            return validatePostcode(caseData.getSpecAoSRespondent2CorrespondenceAddressdetails());
        }
        return Collections.emptyList();
    }

    private boolean isCorrespondenceAddressRequired(YesOrNo isRespondent, YesOrNo isAddressRequired) {
        return isRespondent == YesOrNo.YES && isAddressRequired == YesOrNo.NO;
    }

    private List<String> validatePostcode(Address address) {
        return postcodeValidator.validate(Optional.ofNullable(address).map(Address::getPostCode).orElse(null));
    }
}
