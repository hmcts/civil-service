package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.validation.PartyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class ValidateRespondentDetailsTask {

    private final PostcodeValidator postcodeValidator;
    private final PartyValidator partyValidator;
    private final ObjectMapper objectMapper;
    private Function<CaseData, Party> getRespondent;

    @Autowired
    public ValidateRespondentDetailsTask(PostcodeValidator postcodeValidator, PartyValidator partyValidator, ObjectMapper objectMapper) {
        this.postcodeValidator = postcodeValidator;
        this.partyValidator = partyValidator;
        this.objectMapper = objectMapper;
    }

    public void setGetRespondent(Function<CaseData, Party> getRespondent) {
        this.getRespondent = getRespondent;
    }

    public CallbackResponse validateRespondentDetails(CaseData caseData) {
        return validateRespondentDetails(caseData, true);
    }

    public CallbackResponse validateRespondentDetails(CaseData caseData, boolean validatePostcode) {
        Party respondent = getRespondent.apply(caseData);

        List<String> errors = validatePostcode
            ? postcodeValidator.validate(respondent.getPrimaryAddress().getPostCode())
            : new ArrayList<>();
        if (respondent.getPrimaryAddress() != null) {
            partyValidator.validateAddress(respondent.getPrimaryAddress(), errors);
        }
        partyValidator.validateName(respondent.getPartyName(), errors);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.isEmpty()
                      ? caseData.toMap(objectMapper) : null)
            .build();
    }
}
