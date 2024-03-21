package uk.gov.hmcts.reform.civil.service.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategoryElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCaseManagementDetailsService {

    private final LocationHelper locationHelper;
    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;

    public void updateCaseManagementDetails(CaseData.CaseDataBuilder<?, ?> builder, CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        final List<LocationRefData> availableLocations = fetchLocationData(callbackParams);

        updateApplicant1RequestedCourtDetails(caseData, builder, availableLocations);
        updateRespondent1RequestedCourtDetails(caseData, builder, availableLocations);

        caseData = builder.build();
        locationHelper.getCaseManagementLocation(caseData)
            .ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
                builder,
                requestedCourt,
                () -> locationRefDataService.getCourtLocationsForDefaultJudgments(callbackParams.getParams().get(
                    CallbackParams.Params.BEARER_TOKEN).toString())
            ));

        builder.caseNameHmctsInternal(caseParticipants(caseData).toString());

        CaseManagementCategoryElement civil =
            CaseManagementCategoryElement.builder().code("Civil").label("Civil").build();
        List<Element<CaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        builder.caseManagementCategory(
            CaseManagementCategory.builder().value(civil).list_items(itemList).build());

    }

    private void updateApplicant1RequestedCourtDetails(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder, List<LocationRefData> availableLocations) {
        Optional.ofNullable(caseData.getApplicant1DQ())
            .ifPresent(dq -> Optional.ofNullable(dq.getApplicant1DQRequestedCourt())
                .ifPresent(requestedCourt -> builder.applicant1DQ(
                    dq.toBuilder().applicant1DQRequestedCourt(correctCaseLocation(requestedCourt, availableLocations))
                        .build())));
    }

    private void updateRespondent1RequestedCourtDetails(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder, List<LocationRefData> availableLocations) {
        Optional.ofNullable(caseData.getRespondent1DQ())
            .ifPresent(dq -> Optional.ofNullable(dq.getRespondent1DQRequestedCourt())
                .ifPresent(requestedCourt -> builder.respondent1DQ(
                    dq.toBuilder().respondent1DQRequestedCourt(correctCaseLocation(requestedCourt, availableLocations))
                        .build())));
    }

    private RequestedCourt correctCaseLocation(RequestedCourt requestedCourt, List<LocationRefData> locations) {
        if (requestedCourt.getCaseLocation() == null || requestedCourt.getCaseLocation().getBaseLocation() == null) {
            return requestedCourt;
        }
        String locationLabel = requestedCourt.getCaseLocation().getBaseLocation();
        LocationRefData preferredLocation = locations.stream()
            .filter(locationRefData -> courtLocationUtils.checkLocation(locationRefData, locationLabel))
            .findFirst().orElseThrow(RuntimeException::new);
        return requestedCourt.toBuilder()
            .responseCourtCode(preferredLocation.getCourtLocationCode())
            .caseLocation(LocationHelper.buildCaseLocation(preferredLocation))
            .build();
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
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
                .append(" and ").append(caseData.getApplicant2().getPartyName())
                .append(" v ")
                .append(caseData.getRespondent1().getPartyName());

        } else {
            participantString.append(caseData.getApplicant1().getPartyName())
                .append(" v ")
                .append(caseData.getRespondent1().getPartyName());
        }
        return participantString;

    }

}
