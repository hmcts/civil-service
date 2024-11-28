package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.finalorders.DatesFinalOrders;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.CourtOfficerOrderGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Service
@RequiredArgsConstructor
public class CourtOfficerOrderHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(COURT_OFFICER_ORDER);

    private final LocationReferenceDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final WorkingDayIndicator workingDayIndicator;
    private final DocumentHearingLocationHelper locationHelper;
    private final CourtOfficerOrderGenerator courtOfficerOrderGenerator;
    private final UserService userService;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;
    private String ext = "";

    public static final String HEADER = "## Your order has been issued \n ### Case number \n ### #%s";

    @Override
    protected Map<String, Callback> callbacks() {
        Callback aboutToSubmit = featureToggleService.isCaseEventsEnabled() ? this::handleAboutToSubmit : this::emptyCallbackResponse;
        return Map.of(
            callbackKey(ABOUT_TO_START), this::prePopulateValues,
            callbackKey(MID, "validateValues"), this::validateFormValuesAndGenerateDocument,
            callbackKey(ABOUT_TO_SUBMIT), aboutToSubmit,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse handleAboutToSubmit(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        caseDataBuilder.businessProcess(BusinessProcess.ready(COURT_OFFICER_ORDER));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse prePopulateValues(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = (locationRefDataService.getHearingCourtLocations(authToken));

        caseDataBuilder
            .courtOfficerFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                   .datesToAvoidDateDropdown(DatesFinalOrders.builder()
                                                                 .datesToAvoidDates(workingDayIndicator
                                                                                        .getNextWorkingDay(
                                                                                            LocalDate.now().plusDays(
                                                                                                7))).build())
                                                   .hearingLocationList(populateCurrentHearingLocation(caseData, authToken))
                                                   .alternativeHearingList(getLocationsFromList(locations)).build())
            .courtOfficerGiveReasonsYesNo(YesOrNo.NO);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private DynamicList populateCurrentHearingLocation(CaseData caseData, String authorisation) {
        LocationRefData locationRefData = locationHelper.getHearingLocation(null, caseData, authorisation);

        return DynamicList.builder().listItems(List.of(DynamicListElement.builder()
                                                           .code("LOCATION_LIST")
                                                           .label(locationRefData.getSiteName())
                                                           .build(),
                                                       DynamicListElement.builder()
                                                           .code("OTHER_LOCATION")
                                                           .label("Other location")
                                                           .build()))
            .value(DynamicListElement.builder()
                       .code("LOCATION_LIST")
                       .label(locationRefData.getSiteName())
                       .build())
            .build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .sorted()
                            .toList());
    }

    private CallbackResponse validateFormValuesAndGenerateDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        if (nonNull(caseData.getCourtOfficerFurtherHearingComplex().getListFromDate())
            && caseData.getCourtOfficerFurtherHearingComplex().getListFromDate().isBefore(LocalDate.now())) {
            errors.add("List from date cannot be in the past");
        }

        CaseDocument courtOfficerDocument = courtOfficerOrderGenerator
            .generate(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
        UserDetails userDetails = userService.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        String officerName = userDetails.getFullName();
        assignCategoryId.assignCategoryIdToCaseDocument(courtOfficerDocument, "caseManagementOrders");

        StringBuilder updatedFileName = new StringBuilder();
        ext = FilenameUtils.getExtension(courtOfficerDocument.getDocumentLink().getDocumentFileName());
        courtOfficerDocument.getDocumentLink().setDocumentFileName(updatedFileName
                                                                      .append(courtOfficerDocument.getCreatedDatetime().toLocalDate().toString())
                                                                      .append("_").append(officerName).append(".").append(ext).toString());

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.previewCourtOfficerOrder(courtOfficerDocument);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .build();
    }

    private String getHeader(CaseData caseData) {
        return format(HEADER, caseData.getCcdCaseReference());
    }

}
