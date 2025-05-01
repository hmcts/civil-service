package uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Component
@Slf4j
public class ValidateWitnessesTask {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private static final List<String> ADMIN_ROLES = List.of(
        "caseworker-civil-admin", "caseworker-civil-staff");
    private static final String CREATE_ORDER_ERROR_WITNESSES = "Adding a new witness is not permitted in this screen. Please delete any new witnesses.";

    public ValidateWitnessesTask(ObjectMapper objectMapper, UserService userService) {
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    public CallbackResponse validateWitnesses(CaseData caseData, String authToken) {
        CaseData.CaseDataBuilder builder = caseData.toBuilder();
        log.info("Validate Witnesses for case ID {}", caseData.getCcdCaseReference());
        List<String> errors = new ArrayList<>();

        // Legal Reps should not be able to add or delete experts and witnesses.
        // Have to add "CRU" for LRs for UpdateWitnessesDetailsForm or else we see a No Field Found error upon submission.
        if (!isAdmin(authToken)) {
            List<UpdatePartyDetailsForm> witnessesWithoutPartyId = unwrapElements(caseData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm())
                .stream()
                .filter(e -> e.getPartyId() == null)
                .toList();

            if (!witnessesWithoutPartyId.isEmpty()) {
                errors.add(CREATE_ORDER_ERROR_WITNESSES);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private boolean isAdmin(String userAuthToken) {
        return userService.getUserInfo(userAuthToken).getRoles()
            .stream().anyMatch(ADMIN_ROLES::contains);
    }
}
