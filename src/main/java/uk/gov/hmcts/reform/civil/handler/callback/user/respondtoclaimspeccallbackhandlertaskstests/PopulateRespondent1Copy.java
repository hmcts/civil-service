package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
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

    private static final String UNKNOWN_MP_SCENARIO = "Unknown mp scenario";

    public CallbackResponse execute(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        Set<DefendantResponseShowTag> initialShowTags = getInitialShowTags(callbackParams);
        var updatedCaseData = updateCaseData(caseData, initialShowTags, callbackParams);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CaseData.CaseDataBuilder<?, ?> updateCaseData(CaseData caseData, Set<DefendantResponseShowTag> initialShowTags, CallbackParams callbackParams) {
        var updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1())
            .respondent1ClaimResponseTestForSpec(caseData.getRespondent1ClaimResponseTypeForSpec())
            .respondent2ClaimResponseTestForSpec(caseData.getRespondent2ClaimResponseTypeForSpec())
            .showConditionFlags(initialShowTags);

        updateCarmFields(caseData, updatedCaseData);
        updateRespondentDetails(caseData, updatedCaseData);
        updateCourtLocation(initialShowTags, updatedCaseData, callbackParams);

        return updatedCaseData;
    }

    private void updateCarmFields(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (toggleService.isCarmEnabledForCase(caseData)) {
            updatedCaseData.showCarmFields(YES);
        } else {
            updatedCaseData.showCarmFields(NO);
        }
    }

    private void updateRespondentDetails(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        updatedCaseData.respondent1DetailsForClaimDetailsTab(caseData.getRespondent1().toBuilder().flags(null).build());

        ofNullable(caseData.getRespondent2())
            .ifPresent(r2 -> updatedCaseData.respondent2Copy(r2)
                .respondent2DetailsForClaimDetailsTab(r2.toBuilder().flags(null).build())
            );
    }

    private void updateCourtLocation(Set<DefendantResponseShowTag> initialShowTags, CaseData.CaseDataBuilder<?, ?> updatedCaseData, CallbackParams callbackParams) {
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

    private Set<DefendantResponseShowTag> getInitialShowTags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        Set<DefendantResponseShowTag> set = EnumSet.noneOf(DefendantResponseShowTag.class);

        switch (mpScenario) {
            case ONE_V_ONE, TWO_V_ONE:
                addSingleRespondentTags(set);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                addBothRespondentsTags(set);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                addLegalRepRespondentTags(callbackParams, set);
                break;
            default:
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }
        return set;
    }

    private void addSingleRespondentTags(Set<DefendantResponseShowTag> set) {
        set.add(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1);
    }

    private void addBothRespondentsTags(Set<DefendantResponseShowTag> set) {
        set.add(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1);
        set.add(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2);
    }

    private void addLegalRepRespondentTags(CallbackParams callbackParams, Set<DefendantResponseShowTag> set) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );
        if (roles.contains(RESPONDENTSOLICITORONE.getFormattedName())) {
            set.add(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1);
        }
        if (roles.contains(RESPONDENTSOLICITORTWO.getFormattedName())) {
            set.add(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2);
        }
    }
}
