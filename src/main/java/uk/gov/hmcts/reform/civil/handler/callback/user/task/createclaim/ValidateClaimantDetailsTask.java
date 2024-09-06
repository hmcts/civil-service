package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PartyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class ValidateClaimantDetailsTask {

    private final DateOfBirthValidator dateOfBirthValidator;
    private final PartyValidator partyValidator;
    private final PostcodeValidator postcodeValidator;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;

    private Function<CaseData, Party> getApplicant;

    @Autowired
    public ValidateClaimantDetailsTask(DateOfBirthValidator dateOfBirthValidator,
                                       PartyValidator partyValidator,
                                       PostcodeValidator postcodeValidator,
                                       FeatureToggleService featureToggleService,
                                       ObjectMapper objectMapper) {
        this.dateOfBirthValidator = dateOfBirthValidator;
        this.partyValidator = partyValidator;
        this.postcodeValidator = postcodeValidator;
        this.featureToggleService = featureToggleService;
        this.objectMapper = objectMapper;
    }

    public void setGetApplicant(Function<CaseData, Party> getApplicant) {
        this.getApplicant = getApplicant;
    }

    public CallbackResponse validateClaimantDetails(CaseData caseData, String eventId) {
        Party applicant = getApplicant.apply(caseData);

        List<String> errors = validateApplicant(applicant);

        if (errors.isEmpty() && eventId != null) {
            validatePostcode(applicant, errors);
        }

        return buildCallbackResponse(caseData, errors);
    }

    private List<String> validateApplicant(Party applicant) {
        List<String> errors = new ArrayList<>();
        errors.addAll(dateOfBirthValidator.validate(applicant));

        if (featureToggleService.isJudgmentOnlineLive()) {
            validateAddressAndName(applicant, errors);
        }
        return errors;
    }

    private void validateAddressAndName(Party applicant, List<String> errors) {
        if (applicant.getPrimaryAddress() != null) {
            partyValidator.validateAddress(applicant.getPrimaryAddress(), errors);
            partyValidator.validateName(applicant.getPartyName(), errors);
        }
    }

    private void validatePostcode(Party applicant, List<String> errors) {
        if (applicant.getPrimaryAddress() != null) {
            errors.addAll(postcodeValidator.validate(applicant.getPrimaryAddress().getPostCode()));
        }
    }

    private CallbackResponse buildCallbackResponse(CaseData caseData, List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.isEmpty() ? caseData.toMap(objectMapper) : null)
            .build();
    }
}
