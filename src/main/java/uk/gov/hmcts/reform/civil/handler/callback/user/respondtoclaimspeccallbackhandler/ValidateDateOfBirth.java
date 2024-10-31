package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

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
        CaseData caseData = callbackParams.getCaseData();
        log.info("Executing ValidateDateOfBirth");

        Party respondent = getRespondent(caseData);
        List<String> errors = dateOfBirthValidator.validate(respondent);
        errors.addAll(correspondenceAddressCorrect(caseData));

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        updateSameSolicitorSameResponse(caseData, updatedData, callbackParams);

        log.info("Completed ValidateDateOfBirth");
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

    private void updateSameSolicitorSameResponse(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, CallbackParams callbackParams) {
        log.info("Updating sameSolicitorSameResponse");

        if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData)) && YES.equals(caseData.getAddRespondent2())) {
            if (respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
                && respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
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

    private List<String> correspondenceAddressCorrect(CaseData caseData) {
        log.info("Validating correspondence address");

        if (isRespondent1AddressRequired(caseData)) {
            return validatePostcode(caseData.getSpecAoSRespondentCorrespondenceAddressdetails());
        } else if (isRespondent2AddressRequired(caseData)) {
            return validatePostcode(caseData.getSpecAoSRespondent2CorrespondenceAddressdetails());
        }
        return Collections.emptyList();
    }

    private boolean isRespondent1AddressRequired(CaseData caseData) {
        return caseData.getIsRespondent1() == YesOrNo.YES && caseData.getSpecAoSRespondentCorrespondenceAddressRequired() == YesOrNo.NO;
    }

    private boolean isRespondent2AddressRequired(CaseData caseData) {
        return caseData.getIsRespondent2() == YesOrNo.YES && caseData.getSpecAoSRespondent2CorrespondenceAddressRequired() == YesOrNo.NO;
    }

    private List<String> validatePostcode(Address address) {
        return postcodeValidator.validate(Optional.ofNullable(address).map(Address::getPostCode).orElse(null));
    }
}
