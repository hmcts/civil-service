package uk.gov.hmcts.reform.civil.handler.callback.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LINK_DEFENDANT_TO_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkDefendantToClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(LINK_DEFENDANT_TO_CLAIM);

    private final ValidateEmailService validateEmailService;
    private final IdamClient idamClient;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final SystemUpdateUserConfiguration systemUpdateUserConfiguration;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(MID, "confirm-defendant-email"), this::confirmDefendantEmail,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit
        );
    }

    @Override
    public CallbackResponse handle(CallbackParams callbackParams) {
        return featureToggleService.isLinkDefendantTestingEnabled()
            ? super.handle(callbackParams)
            : emptyCallbackResponse(callbackParams);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return featureToggleService.isLinkDefendantTestingEnabled() ? EVENTS : List.of();
    }

    private CallbackResponse confirmDefendantEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validateEmailService.validate(caseData.getDefendantEmailAddress()))
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String defendantEmail = caseData.getDefendantEmailAddress();

        log.info("Linking defendant to claim for case reference: {}", caseData.getCcdCaseReference());

        return getUserIdByUserEmail(defendantEmail)
            .map(defendantUserId -> linkDefendantToClaim(caseData, defendantUserId, defendantEmail))
            .orElseGet(() -> {
                log.error(
                    "No user found with the provided email address for case reference: {}",
                    caseData.getCcdCaseReference()
                );
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(List.of("No user found with the provided email address"))
                    .build();
            });
    }

    private Optional<String> getUserIdByUserEmail(String email) {
        return idamClient.searchUsers(getSystemUserAccessToken(), String.format("email:\"%s\"", email))
            .stream().findFirst().map(UserDetails::getId);
    }

    private String getSystemUserAccessToken() {
        return userService.getAccessToken(
            systemUpdateUserConfiguration.getUserName(),
            systemUpdateUserConfiguration.getPassword()
        );
    }

    private CallbackResponse linkDefendantToClaim(CaseData caseData, String defendantUserId, String defendantEmail) {
        String caseId = caseData.getCcdCaseReference().toString();
        log.info("Assigning case {} to defendant user ID: {}", caseId, defendantUserId);
        coreCaseUserService.assignCase(
            caseId,
            defendantUserId,
            null,
            CaseRole.DEFENDANT
        );

        caseData.setDefendantUserDetails(new IdamUserDetails()
                                             .setId(defendantUserId)
                                             .setEmail(defendantEmail));

        Optional.ofNullable(caseData.getRespondent1())
            .ifPresent(party -> party.setPartyEmail(defendantEmail));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(removeTemporaryFields(caseData.toMap(objectMapper)))
            .build();
    }

    private Map<String, Object> removeTemporaryFields(Map<String, Object> dataMap) {
        if (dataMap.get("respondent1PinToPostLRspec") instanceof Map<?, ?> map) {
            map.remove("accessCode");
        }
        dataMap.remove("defendantEmailAddress");
        return dataMap;
    }
}
