package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopulateRespondent1Copy implements CaseTask {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final FeatureToggleService toggleService;
    private final CourtLocationUtils courtLocationUtils;
    private final ObjectMapper objectMapper;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing PopulateRespondent1Copy task with case data: {}", callbackParams.getCaseData().getCcdCaseReference());
        CaseData caseData = callbackParams.getCaseData();
        Set<DefendantResponseShowTag> initialShowTags = getInitialShowTags(callbackParams);
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = initialiseCaseDataBuilder(caseData, initialShowTags);

        updateCarmFields(caseData, updatedCaseData);
        updateRespondentDetails(caseData, updatedCaseData);
        updateCourtLocations(callbackParams, initialShowTags, updatedCaseData);

        CallbackResponse response = buildCallbackResponse(updatedCaseData);
        log.info("Completed PopulateRespondent1Copy task for case data: {}", callbackParams.getCaseData().getCcdCaseReference());
        return response;
    }

    private CaseData.CaseDataBuilder<?, ?> initialiseCaseDataBuilder(CaseData caseData, Set<DefendantResponseShowTag> initialShowTags) {
        log.debug("Initialising case data builder with initial show tags: {}", initialShowTags);
        return caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1())
            .respondent1ClaimResponseTestForSpec(caseData.getRespondent1ClaimResponseTypeForSpec())
            .respondent2ClaimResponseTestForSpec(caseData.getRespondent2ClaimResponseTypeForSpec())
            .showConditionFlags(initialShowTags);
    }

    private void updateCarmFields(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (toggleService.isCarmEnabledForCase(caseData)) {
            log.debug("CARM is enabled for case: {}", caseData.getCcdCaseReference());
            updatedCaseData.showCarmFields(YES);
        } else {
            log.debug("CARM is not enabled for case: {}", caseData.getCcdCaseReference());
            updatedCaseData.showCarmFields(NO);
        }
    }

    private void updateRespondentDetails(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating respondent details for case: {}", caseData.getCcdCaseReference());
        updatedCaseData.respondent1DetailsForClaimDetailsTab(caseData.getRespondent1().toBuilder().flags(null).build());

        ofNullable(caseData.getRespondent2())
            .ifPresent(r2 -> {
                log.debug("Updating respondent 2 details for case: {}", caseData.getCcdCaseReference());
                updatedCaseData.respondent2Copy(r2)
                    .respondent2DetailsForClaimDetailsTab(r2.toBuilder().flags(null).build());
            });
    }

    private void updateCourtLocations(CallbackParams callbackParams, Set<DefendantResponseShowTag> initialShowTags, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating court locations for case: {}", callbackParams.getCaseData().getCcdCaseReference());
        DynamicList courtLocationList = courtLocationUtils.getLocationsFromList(respondToClaimSpecUtils.getLocationData(callbackParams));
        if (initialShowTags.contains(CAN_ANSWER_RESPONDENT_1)) {
            updatedCaseData.respondent1DQ(Respondent1DQ.builder()
                                              .respondToCourtLocation(
                                                  RequestedCourt.builder()
                                                      .responseCourtLocations(courtLocationList)
                                                      .build())
                                              .build());
        }
        if (initialShowTags.contains(CAN_ANSWER_RESPONDENT_2)) {
            updatedCaseData.respondent2DQ(Respondent2DQ.builder()
                                              .respondToCourtLocation2(
                                                  RequestedCourt.builder()
                                                      .responseCourtLocations(courtLocationList)
                                                      .build())
                                              .build());
        }
    }

    private CallbackResponse buildCallbackResponse(CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Building callback response for case: {}", updatedCaseData.build().getCcdCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private Set<DefendantResponseShowTag> getInitialShowTags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        log.debug("Determining initial show tags for case: {} with scenario: {}", caseData.getCcdCaseReference(), mpScenario);

        return switch (mpScenario) {
            case ONE_V_ONE, TWO_V_ONE -> handleOneOrTwoVOneScenario();
            case ONE_V_TWO_ONE_LEGAL_REP -> handleOneVTwoOneLegalRepScenario();
            case ONE_V_TWO_TWO_LEGAL_REP -> handleOneVTwoTwoLegalRepScenario(callbackParams);
        };
    }

    private Set<DefendantResponseShowTag> handleOneOrTwoVOneScenario() {
        log.debug("Handling ONE_V_ONE or TWO_V_ONE scenario");
        Set<DefendantResponseShowTag> set = EnumSet.noneOf(DefendantResponseShowTag.class);
        addRespondent1Tag(set);
        return set;
    }

    private Set<DefendantResponseShowTag> handleOneVTwoOneLegalRepScenario() {
        log.debug("Handling ONE_V_TWO_ONE_LEGAL_REP scenario");
        Set<DefendantResponseShowTag> set = EnumSet.noneOf(DefendantResponseShowTag.class);
        addRespondent1Tag(set);
        addRespondent2Tag(set);
        return set;
    }

    private Set<DefendantResponseShowTag> handleOneVTwoTwoLegalRepScenario(CallbackParams callbackParams) {
        log.debug("Handling ONE_V_TWO_TWO_LEGAL_REP scenario");
        Set<DefendantResponseShowTag> set = EnumSet.noneOf(DefendantResponseShowTag.class);
        UserInfo userInfo = getUserInfo(callbackParams);
        List<String> roles = getUserRoles(callbackParams, userInfo);

        if (isRolePresent(roles, RESPONDENTSOLICITORONE)) {
            addRespondent1Tag(set);
        }
        if (isRolePresent(roles, RESPONDENTSOLICITORTWO)) {
            addRespondent2Tag(set);
        }

        return set;
    }

    private UserInfo getUserInfo(CallbackParams callbackParams) {
        log.debug("Fetching user info for token");
        return userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private List<String> getUserRoles(CallbackParams callbackParams, UserInfo userInfo) {
        log.debug("Fetching user roles for case: {}", callbackParams.getCaseData().getCcdCaseReference());
        return coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );
    }

    private boolean isRolePresent(List<String> roles, CaseRole caseRole) {
        log.debug("Checking if role {} is present", caseRole);
        return roles.contains(caseRole.getFormattedName());
    }

    private void addRespondent1Tag(Set<DefendantResponseShowTag> set) {
        log.debug("Adding CAN_ANSWER_RESPONDENT_1 tag");
        set.add(CAN_ANSWER_RESPONDENT_1);
    }

    private void addRespondent2Tag(Set<DefendantResponseShowTag> set) {
        log.debug("Adding CAN_ANSWER_RESPONDENT_2 tag");
        set.add(CAN_ANSWER_RESPONDENT_2);
    }
}
