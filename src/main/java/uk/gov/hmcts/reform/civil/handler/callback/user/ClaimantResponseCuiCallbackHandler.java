package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategoryElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimantResponseCuiCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE_CUI);

    private final ObjectMapper objectMapper;
    private final LocationHelper locationHelper;
    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
                callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
                callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                .applicant1ResponseDate(LocalDateTime.now())
                .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE_CUI));

        final List<LocationRefData> availableLocations = fetchLocationData(callbackParams);

        //Correct the Applicant1DQRequestedCourt location with epmmsid and regionId
        Optional.ofNullable(caseData.getApplicant1DQ())
                .ifPresent(dq -> Optional.ofNullable(dq.getApplicant1DQRequestedCourt())
                        .ifPresent(requestedCourt -> builder.applicant1DQ(dq.toBuilder()
                                .applicant1DQRequestedCourt(correctCaseLocation(requestedCourt, availableLocations))
                                .build())));
        //Correct the Respondent1DQRequestedCourt location with epmmsid and regionId
        Optional.ofNullable(caseData.getRespondent1DQ())
                .ifPresent(dq -> Optional.ofNullable(dq.getRespondent1DQRequestedCourt())
                        .ifPresent(requestedCourt -> builder.respondent1DQ(dq.toBuilder()
                                .respondent1DQRequestedCourt(correctCaseLocation(requestedCourt, availableLocations))
                                .build())));

        //Adding case management location for preferred court
        locationHelper.getCaseManagementLocation(caseData)
                .ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
                        builder,
                        correctCaseLocation(requestedCourt, availableLocations),
                        () -> locationRefDataService.getCourtLocationsForDefaultJudgments(callbackParams.getParams().get(
                                CallbackParams.Params.BEARER_TOKEN).toString())
                ));

        //assign casemanagementcategory to the case and assign casenamehmctsinternal
        //casename
        builder.caseNameHmctsInternal(caseParticipants(caseData).toString());

        //case management category
        CaseManagementCategoryElement civil =
                CaseManagementCategoryElement.builder().code("Civil").label("Civil").build();
        List<Element<CaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        builder.caseManagementCategory(
                CaseManagementCategory.builder().value(civil).list_items(itemList).build());
        log.info("Case management equals: " + caseData.getCaseManagementCategory());
        log.info("CaseName equals: " + caseData.getCaseNameHmctsInternal());

        CaseData updatedData = builder.build();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
                AboutToStartOrSubmitCallbackResponse.builder()
                        .data(updatedData.toMap(objectMapper));

        updateClaimEndState(response, updatedData);

        return response.build();
    }

    private RequestedCourt correctCaseLocation(RequestedCourt requestedCourt, List<LocationRefData> locations) {
        String locationLabel = requestedCourt.getCaseLocation().getBaseLocation();
        LocationRefData preferredLocation = locations.stream()
                .filter(locationRefData -> courtLocationUtils.checkLocation(locationRefData, locationLabel))
                .findFirst().orElseThrow(RuntimeException::new);
        return requestedCourt.toBuilder().caseLocation(LocationHelper.buildCaseLocation(preferredLocation)).build();
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private void updateClaimEndState(AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response, CaseData updatedData) {

        if (updatedData.hasClaimantAgreedToFreeMediation()) {
            response.state(CaseState.IN_MEDIATION.name());
        } else {
            response.state(CaseState.JUDICIAL_REFERRAL.name());
        }
    }

    private StringBuilder caseParticipants(CaseData caseData) {
        StringBuilder participantString = new StringBuilder();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                || multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)) {
            participantString.append(caseData.getApplicant1().getPartyName())
                    .append(" v ").append(caseData.getRespondent1().getPartyName())
                    .append(" and ").append(caseData.getRespondent2().getPartyName());

        } else if (multiPartyScenario.equals(MultiPartyScenario.TWO_V_ONE)) {
            participantString.append(caseData.getApplicant1().getPartyName())
                    .append(" and ").append(caseData.getApplicant2().getPartyName()).append(" v ")
                    .append(caseData.getRespondent1()
                            .getPartyName());

        } else {
            participantString.append(caseData.getApplicant1().getPartyName()).append(" v ")
                    .append(caseData.getRespondent1()
                            .getPartyName());
        }
        return participantString;

    }
}
