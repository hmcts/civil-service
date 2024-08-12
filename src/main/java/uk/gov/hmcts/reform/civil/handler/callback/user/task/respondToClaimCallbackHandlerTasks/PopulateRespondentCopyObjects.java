package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondToClaimCallbackHandlerTasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.service.user.RespondentService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
public class PopulateRespondentCopyObjects implements CaseTask {
    private final ObjectMapper objectMapper;
    private final CourtLocationUtils courtLocationUtils;
    private final RespondentService respondentService;
    public static final String ERROR_DEFENDANT_RESPONSE_SUBMITTED =
        "There is a problem"
            + "\n"
            + "You have already submitted the defendant's response";
    private final LocationReferenceDataService locationReferenceDataService;

    public PopulateRespondentCopyObjects(ObjectMapper objectMapper,
                                         CourtLocationUtils courtLocationUtils,
                                         RespondentService respondentService, RespondentService respondentService1,
                                         LocationReferenceDataService locationReferenceDataService) {
        this.objectMapper = objectMapper;
        this.courtLocationUtils = courtLocationUtils;
        this.respondentService = respondentService;
        this.locationReferenceDataService = locationReferenceDataService;
    }

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime dateTime = LocalDateTime.now();

        CallbackResponse errorResponse = checkSubmitError(callbackParams, caseData, respondentService, dateTime);
        if (errorResponse != null) {
            return errorResponse;
        }

        var isRespondent1 = findOutIsRespondent1(callbackParams);

        List<LocationRefData> locations = locationReferenceDataService.fetchLocationData(callbackParams);

        RequestedCourt requestedCourt1 = createRequestedCourt(locations, caseData);

        CaseData.CaseDataBuilder updatedCaseData = updateCaseData(caseData, isRespondent1, requestedCourt1);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse checkSubmitError(CallbackParams callbackParams,
                                              CaseData caseData,
                                              RespondentService respondentService,
                                              LocalDateTime dateTime) {
        // Show error message if defendant tries to submit response again
        if (hasDefendantAlreadySubmitted(callbackParams, caseData)){
            return createErrorResponse(ERROR_DEFENDANT_RESPONSE_SUBMITTED);
        }

        //Show error message if defendant tries to submit a response after deadline has passed
        if (isResponseSubmissionPassedDeadline(callbackParams, caseData , respondentService, dateTime)) {
            return createErrorResponse("You cannot submit a response now as you have passed your deadline");
        }

        return null;
    }

    private YesOrNo findOutIsRespondent1(CallbackParams callbackParams) {
         YesOrNo isRespondent1 = YES;
        if (respondentService.solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            //1V2 Different Solicitors + Respondent 2 only
            isRespondent1 = NO;
        }
        return isRespondent1;
    }

    private CallbackResponse createErrorResponse(String errorMessage) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(errorMessage))
            .build();
    }

    private CaseData.CaseDataBuilder updateCaseData(CaseData caseData, YesOrNo isRespondent1, RequestedCourt requestedCourt1) {
        var updatedCaseData = caseData.toBuilder()
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
        RequestedCourt.RequestedCourtBuilder requestedCourt1 = RequestedCourt.builder();
        requestedCourt1.responseCourtLocations(courtLocationList);

        Optional.ofNullable(caseData.getCourtLocation())
            .map(CourtLocation::getApplicantPreferredCourt)
            .flatMap(applicantCourt -> locations.stream()
                .filter(locationRefData -> applicantCourt.equals(locationRefData.getCourtLocationCode()))
                .findFirst())
            .ifPresent(locationRefData -> requestedCourt1
                .otherPartyPreferredSite(locationRefData.getCourtLocationCode()
                                             + " " + locationRefData.getSiteName()));
        return requestedCourt1.build();
    }

    private boolean hasDefendantAlreadySubmitted(CallbackParams callbackParams, CaseData caseData) {
        return isSolicitorForRespondent1WithResponseDate(callbackParams, caseData)
            || isSolicitorForRespondent2WithResponseDate(callbackParams, caseData);
        }

    private boolean isSolicitorForRespondent2WithResponseDate(CallbackParams callbackParams, CaseData caseData) {
        return respondentService.solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getRespondent2ResponseDate() != null;
    }

    private boolean isSolicitorForRespondent1WithResponseDate(CallbackParams callbackParams, CaseData caseData) {
        return respondentService.solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getRespondent1ResponseDate() != null;
    }


    private boolean isResponseSubmissionPassedDeadline(CallbackParams callbackParams,
                                                       CaseData caseData,
                                                       RespondentService repondentService,
                                                       LocalDateTime dateTime) {

        var respondent1ResponseDeadline = caseData.getRespondent1ResponseDeadline();
        var respondent2ResponseDeadline = caseData.getRespondent2ResponseDeadline();

        return (isRespondent1PassedDeadline(caseData, callbackParams, respondentService, dateTime, respondent1ResponseDeadline)
            || isRespondent2PassedDeadline(caseData, callbackParams, respondentService, dateTime, respondent2ResponseDeadline));
    }

    private boolean isRespondent1PassedDeadline(CaseData caseData,
                                                       CallbackParams callbackParams,
                                                       RespondentService repondentService,
                                                       LocalDateTime dateTime,
                                                       LocalDateTime respondent1ResponseDeadline) {
        return repondentService.solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getRespondent1ResponseDate() == null
            && respondent1ResponseDeadline != null
            && dateTime.toLocalDate().isAfter(respondent1ResponseDeadline.toLocalDate());
    }

    private boolean isRespondent2PassedDeadline(CaseData caseData,
                                                       CallbackParams callbackParams,
                                                       RespondentService repondentService,
                                                       LocalDateTime dateTime,
                                                       LocalDateTime respondent2ResponseDeadline) {
        return repondentService.solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getRespondent2ResponseDate() == null
            && respondent2ResponseDeadline != null
            && dateTime.toLocalDate().isAfter(respondent2ResponseDeadline.toLocalDate());
    }
}
