package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Component
@Slf4j
public class PopulateRespondentCopyObjects implements CaseTask {

    private final ObjectMapper objectMapper;
    private final CourtLocationUtils courtLocationUtils;
    public static final String ERROR_DEFENDANT_RESPONSE_SUBMITTED =
        "There is a problem"
            + "\n"
            + "You have already submitted the defendant's response";
    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final LocationReferenceDataService locationRefDataService;

    public PopulateRespondentCopyObjects(ObjectMapper objectMapper,
                                         CourtLocationUtils courtLocationUtils,
                                         IStateFlowEngine stateFlowEngine,
                                         CoreCaseUserService coreCaseUserService,
                                         UserService userService,
                                         LocationReferenceDataService locationRefDataService) {

        this.objectMapper = objectMapper;
        this.courtLocationUtils = courtLocationUtils;
        this.stateFlowEngine = stateFlowEngine;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;
        this.locationRefDataService = locationRefDataService;
    }

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Populating respondent copy objects", caseData.getCcdCaseReference());
        LocalDateTime dateTime = LocalDateTime.now();

        CallbackResponse errorResponse = getErrorResponse(callbackParams, caseData, dateTime);
        if (errorResponse != null) {
            return errorResponse;
        }

        YesOrNo isRespondent1 = checkRespondent1YesOrNo(callbackParams);

        List<LocationRefData> locations = getLocationData(callbackParams);

        RequestedCourt requestedCourt1 = createRequestedCourt(locations, caseData);

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = updateCaseData(caseData, isRespondent1, requestedCourt1);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse getErrorResponse(CallbackParams callbackParams,
                                              CaseData caseData,
                                              LocalDateTime dateTime) {
        if (isSubmissionForDefendantAlreadySubmitted(callbackParams, caseData)) {
            return createErrorResponse(ERROR_DEFENDANT_RESPONSE_SUBMITTED);
        }

        if (isResponseSubmissionPassedDeadline(callbackParams, caseData, dateTime)) {
            return createErrorResponse("You cannot submit a response now as you have passed your deadline");
        }

        return null;
    }

    private YesOrNo checkRespondent1YesOrNo(CallbackParams callbackParams) {
        YesOrNo isRespondent1 = YES;
        if (isSolicitorRepresentingOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            isRespondent1 = NO;
        }
        return isRespondent1;
    }

    private CallbackResponse createErrorResponse(String errorMessage) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(errorMessage))
            .build();
    }

    private CaseData.CaseDataBuilder<?, ?> updateCaseData(CaseData caseData, YesOrNo isRespondent1, RequestedCourt requestedCourt1) {
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1())
            .isRespondent1(isRespondent1)
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   requestedCourt1)
                               .build());

        if (caseData.getRespondent2() != null) {
            updatedCaseData.respondent2DQ(
                Respondent2DQ.builder()
                    .respondent2DQRequestedCourt(requestedCourt1).build());
        }

        updatedCaseData.respondent1DetailsForClaimDetailsTab(updatedCaseData.build().getRespondent1()
                                                                 .toBuilder().flags(null).build());

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedCaseData
                .respondent2Copy(caseData.getRespondent2())
                .respondent2DetailsForClaimDetailsTab(updatedCaseData.build().getRespondent2()
                                                          .toBuilder().flags(null).build());
        }
        return updatedCaseData;
    }

    private RequestedCourt createRequestedCourt(List<LocationRefData> locations, CaseData caseData) {
        DynamicList courtLocationList = courtLocationUtils.getLocationsFromList(locations);
        RequestedCourt.RequestedCourtBuilder requestedCourt = RequestedCourt.builder();
        requestedCourt.responseCourtLocations(courtLocationList);

        Optional.ofNullable(caseData.getCourtLocation())
            .map(CourtLocation::getApplicantPreferredCourt)
            .flatMap(applicantCourt -> locations.stream()
                .filter(locationRefData -> applicantCourt.equals(locationRefData.getCourtLocationCode()))
                .findFirst())
            .ifPresent(locationRefData -> requestedCourt
                .otherPartyPreferredSite(locationRefData.getCourtLocationCode()
                                             + " " + locationRefData.getSiteName()));
        return requestedCourt.build();
    }

    private boolean isSubmissionForDefendantAlreadySubmitted(CallbackParams callbackParams, CaseData caseData) {
        return isSolicitorForRespondent1WithResponseDate(callbackParams, caseData)
            || isSolicitorForRespondent2WithResponseDate(callbackParams, caseData);
    }

    private boolean isSolicitorForRespondent2WithResponseDate(CallbackParams callbackParams, CaseData caseData) {
        return isSolicitorRepresentingOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getRespondent2ResponseDate() != null;
    }

    private boolean isSolicitorForRespondent1WithResponseDate(CallbackParams callbackParams, CaseData caseData) {
        return isSolicitorRepresentingOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getRespondent1ResponseDate() != null;
    }

    private boolean isResponseSubmissionPassedDeadline(CallbackParams callbackParams,
                                                       CaseData caseData,
                                                       LocalDateTime dateTime) {

        LocalDateTime respondent1ResponseDeadline = caseData.getRespondent1ResponseDeadline();
        LocalDateTime respondent2ResponseDeadline = caseData.getRespondent2ResponseDeadline();

        return (isRespondent1PassedDeadline(caseData, callbackParams, dateTime, respondent1ResponseDeadline)
            || isRespondent2PassedDeadline(caseData, callbackParams, dateTime, respondent2ResponseDeadline));
    }

    private boolean isRespondent1PassedDeadline(CaseData caseData,
                                                CallbackParams callbackParams,
                                                LocalDateTime dateTime,
                                                LocalDateTime respondent1ResponseDeadline) {
        return isSolicitorRepresentingOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getRespondent1ResponseDate() == null
            && respondent1ResponseDeadline != null
            && dateTime.toLocalDate().isAfter(respondent1ResponseDeadline.toLocalDate());
    }

    private boolean isRespondent2PassedDeadline(CaseData caseData,
                                                CallbackParams callbackParams,
                                                LocalDateTime dateTime,
                                                LocalDateTime respondent2ResponseDeadline) {
        return isSolicitorRepresentingOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getRespondent2ResponseDate() == null
            && respondent2ResponseDeadline != null
            && dateTime.toLocalDate().isAfter(respondent2ResponseDeadline.toLocalDate());
    }

    private boolean isSolicitorRepresentingOneOrBothRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }

    private List<LocationRefData> getLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }
}
