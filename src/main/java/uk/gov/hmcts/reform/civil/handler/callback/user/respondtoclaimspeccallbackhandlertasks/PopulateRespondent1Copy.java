package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
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
import java.util.Optional;
import java.util.Set;

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
    private final RespondToClaimSpecUtilsCourtLocation respondToClaimSpecUtilsCourtLocation;
    private final ObjectMapper objectMapper;

    private static final String UNKNOWN_MP_SCENARIO = "Unknown mp scenario";

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Set<DefendantResponseShowTag> initialShowTags = getInitialShowTags(callbackParams);

        CaseData.CaseDataBuilder<?, ?> updatedCaseDataBuilder = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1())
            .respondent1ClaimResponseTestForSpec(caseData.getRespondent1ClaimResponseTypeForSpec())
            .respondent2ClaimResponseTestForSpec(caseData.getRespondent2ClaimResponseTypeForSpec())
            .showConditionFlags(initialShowTags)
            .showCarmFields(toggleService.isCarmEnabledForCase(caseData) ? YES : NO)
            .respondent1DetailsForClaimDetailsTab(caseData.getRespondent1().toBuilder().flags(null).build());

        Optional.ofNullable(caseData.getRespondent2())
            .ifPresent(r2 -> updatedCaseDataBuilder
                .respondent2Copy(r2)
                .respondent2DetailsForClaimDetailsTab(r2.toBuilder().flags(null).build()));

        DynamicList courtLocationList = courtLocationUtils.getLocationsFromList(respondToClaimSpecUtilsCourtLocation.fetchLocationData(callbackParams));

        if (initialShowTags.contains(CAN_ANSWER_RESPONDENT_1)) {
            updatedCaseDataBuilder.respondent1DQ(buildRespondent1DQ(courtLocationList));
        }

        if (initialShowTags.contains(CAN_ANSWER_RESPONDENT_2)) {
            updatedCaseDataBuilder.respondent2DQ(buildRespondent2DQ(courtLocationList));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private Respondent1DQ buildRespondent1DQ(DynamicList courtLocationList) {
        return Respondent1DQ.builder()
            .respondToCourtLocation(RequestedCourt.builder()
                                        .responseCourtLocations(courtLocationList)
                                        .build())
            .build();
    }

    private Respondent2DQ buildRespondent2DQ(DynamicList courtLocationList) {
        return Respondent2DQ.builder()
            .respondToCourtLocation2(RequestedCourt.builder()
                                         .responseCourtLocations(courtLocationList)
                                         .build())
            .build();
    }

    private Set<DefendantResponseShowTag> getInitialShowTags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        Set<DefendantResponseShowTag> showTags = EnumSet.noneOf(DefendantResponseShowTag.class);

        switch (mpScenario) {
            case ONE_V_ONE, TWO_V_ONE -> showTags.add(CAN_ANSWER_RESPONDENT_1);
            case ONE_V_TWO_ONE_LEGAL_REP -> {
                showTags.add(CAN_ANSWER_RESPONDENT_1);
                showTags.add(CAN_ANSWER_RESPONDENT_2);
            }
            case ONE_V_TWO_TWO_LEGAL_REP -> addUserRolesShowTags(callbackParams, showTags);
            default -> throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }

        return showTags;
    }

    private void addUserRolesShowTags(CallbackParams callbackParams, Set<DefendantResponseShowTag> showTags) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );
        if (roles.contains(RESPONDENTSOLICITORONE.getFormattedName())) {
            showTags.add(CAN_ANSWER_RESPONDENT_1);
        }
        if (roles.contains(RESPONDENTSOLICITORTWO.getFormattedName())) {
            showTags.add(CAN_ANSWER_RESPONDENT_2);
        }
    }
}
