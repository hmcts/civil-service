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

@Slf4j
@Component
public class ValidateExpertsTask {

    private final ObjectMapper objectMapper;
    private final UserService userService;

    private static final List<String> ADMIN_ROLES = List.of(
        "caseworker-civil-admin", "caseworker-civil-staff");
    private static final String CREATE_ORDER_ERROR_EXPERTS = "Adding a new expert is not permitted in this screen. Please delete any new experts.";

    public ValidateExpertsTask(ObjectMapper objectMapper, UserService userService) {
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    public CallbackResponse validateExperts(CaseData caseData, String authToken) {
        CaseData.CaseDataBuilder builder = caseData.toBuilder();
        log.info("Validate experts for case ID {}", caseData.getCcdCaseReference());
        List<String> errors = new ArrayList<>();

        // Legal Reps should not be able to add or delete experts and witnesses.
        // Have to add "CRU" for LRs for updateExpertsDetailsForm or else we see a No Field Found error upon submission.
        if (!isAdmin(authToken)) {
            List<UpdatePartyDetailsForm> expertsWithoutPartyId = unwrapElements(caseData.getUpdateDetailsForm().getUpdateExpertsDetailsForm())
                .stream()
                .filter(e -> e.getPartyId() == null)
                .toList();

            if (!expertsWithoutPartyId.isEmpty()) {
                errors.add(CREATE_ORDER_ERROR_EXPERTS);
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
