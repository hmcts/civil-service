package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
public class PopulateRespondent1Copy implements CaseTask {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final FeatureToggleService toggleService;
    private final CourtLocationUtils courtLocationUtils;
    private final ObjectMapper objectMapper;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        Set<DefendantResponseShowTag> initialShowTags = getInitialShowTags(callbackParams);
        var updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1())
            .respondent1ClaimResponseTestForSpec(caseData.getRespondent1ClaimResponseTypeForSpec())
            .respondent2ClaimResponseTestForSpec(caseData.getRespondent2ClaimResponseTypeForSpec())
            .showConditionFlags(initialShowTags);

        if (toggleService.isCarmEnabledForCase(caseData)) {
            updatedCaseData.showCarmFields(YES);
        } else {
            updatedCaseData.showCarmFields(NO);
        }

        updatedCaseData.respondent1DetailsForClaimDetailsTab(caseData.getRespondent1().toBuilder().flags(null).build());

        ofNullable(caseData.getRespondent2())
            .ifPresent(r2 -> updatedCaseData.respondent2Copy(r2)
                .respondent2DetailsForClaimDetailsTab(r2.toBuilder().flags(null).build())
            );

        DynamicList courtLocationList = courtLocationUtils.getLocationsFromList(respondToClaimSpecUtils.fetchLocationData(callbackParams));
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

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private Set<DefendantResponseShowTag> getInitialShowTags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);

        return switch (mpScenario) {
            case ONE_V_ONE, TWO_V_ONE -> handleOneOrTwoVOneScenario();
            case ONE_V_TWO_ONE_LEGAL_REP -> handleOneVTwoOneLegalRepScenario();
            case ONE_V_TWO_TWO_LEGAL_REP -> handleOneVTwoTwoLegalRepScenario(callbackParams);
        };
    }

    private Set<DefendantResponseShowTag> handleOneOrTwoVOneScenario() {
        Set<DefendantResponseShowTag> set = EnumSet.noneOf(DefendantResponseShowTag.class);
        addRespondent1Tag(set);
        return set;
    }

    private Set<DefendantResponseShowTag> handleOneVTwoOneLegalRepScenario() {
        Set<DefendantResponseShowTag> set = EnumSet.noneOf(DefendantResponseShowTag.class);
        addRespondent1Tag(set);
        addRespondent2Tag(set);
        return set;
    }

    private Set<DefendantResponseShowTag> handleOneVTwoTwoLegalRepScenario(CallbackParams callbackParams) {
        Set<DefendantResponseShowTag> set = EnumSet.noneOf(DefendantResponseShowTag.class);
        UserInfo userInfo = getUserInfo(callbackParams);
        List<String> roles = getUserRoles(callbackParams, userInfo);

        if (hasRole(roles, RESPONDENTSOLICITORONE)) {
            addRespondent1Tag(set);
        }
        if (hasRole(roles, RESPONDENTSOLICITORTWO)) {
            addRespondent2Tag(set);
        }

        return set;
    }

    private UserInfo getUserInfo(CallbackParams callbackParams) {
        return userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private List<String> getUserRoles(CallbackParams callbackParams, UserInfo userInfo) {
        return coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );
    }

    private boolean hasRole(List<String> roles, CaseRole caseRole) {
        return roles.contains(caseRole.getFormattedName());
    }

    private void addRespondent1Tag(Set<DefendantResponseShowTag> set) {
        set.add(CAN_ANSWER_RESPONDENT_1);
    }

    private void addRespondent2Tag(Set<DefendantResponseShowTag> set) {
        set.add(CAN_ANSWER_RESPONDENT_2);
    }
}
