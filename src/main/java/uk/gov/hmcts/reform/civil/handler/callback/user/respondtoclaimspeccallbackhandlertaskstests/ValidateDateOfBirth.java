package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

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
        log.info("Executing ValidateDateOfBirth task");
        CaseData caseData = callbackParams.getCaseData();
        Party respondent = getRespondent(caseData);
        log.debug("Respondent data: {}", respondent);

        List<String> errors = dateOfBirthValidator.validate(respondent);
        log.debug("Date of birth validation errors: {}", errors);

        errors.addAll(correspondenceAddressCorrect(caseData));
        log.debug("Total errors after address validation: {}", errors);

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        updateSameSolicitorResponse(caseData, updatedData, callbackParams);

        log.info("Validation completed with errors: {}", errors);
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
        log.debug("Selected respondent: {}", respondent);
        return respondent;
    }

    private void updateSameSolicitorResponse(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, CallbackParams callbackParams) {
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData)) && YES.equals(caseData.getAddRespondent2())) {
            if (respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
                && respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
                updatedData.sameSolicitorSameResponse(YES).build();
                log.info("Both respondents have the same solicitor");
            } else {
                updatedData.sameSolicitorSameResponse(NO).build();
                log.info("Respondents have different solicitors");
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData)) && YES.equals(caseData.getAddRespondent2())) {
            if (NO.equals(caseData.getRespondentResponseIsSame())) {
                updatedData.sameSolicitorSameResponse(NO).build();
                log.info("Respondents have different responses");
            } else {
                updatedData.sameSolicitorSameResponse(YES).build();
                log.info("Respondents have the same response");
            }
        }
    }

    private List<String> correspondenceAddressCorrect(CaseData caseData) {
        if (caseData.getIsRespondent1() == YesOrNo.YES) {
            log.debug("Validating respondent 1 address");
            return validateRespondent1Address(caseData);
        } else if (caseData.getIsRespondent2() == YesOrNo.YES) {
            log.debug("Validating respondent 2 address");
            return validateRespondent2Address(caseData);
        }
        return Collections.emptyList();
    }

    private List<String> validateRespondent1Address(CaseData caseData) {
        if (caseData.getSpecAoSRespondentCorrespondenceAddressRequired() == YesOrNo.NO) {
            log.debug("Respondent 1 correspondence address is not required");
            return postcodeValidator.validate(
                Optional.ofNullable(caseData.getSpecAoSRespondentCorrespondenceAddressdetails())
                    .map(Address::getPostCode)
                    .orElse(null)
            );
        }
        log.debug("Validating respondent 1 correspondence address");
        return Collections.emptyList();
    }

    private List<String> validateRespondent2Address(CaseData caseData) {
        if (caseData.getSpecAoSRespondent2CorrespondenceAddressRequired() == YesOrNo.NO) {
            log.debug("Respondent 2 correspondence address is not required");
            return postcodeValidator.validate(
                Optional.ofNullable(caseData.getSpecAoSRespondent2CorrespondenceAddressdetails())
                    .map(Address::getPostCode)
                    .orElse(null)
            );
        }
        log.debug("Validating respondent 2 correspondence address");
        return Collections.emptyList();
    }
}
