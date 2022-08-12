package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.sdo.*;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.*;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NotSuitable_SDO;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Service
@RequiredArgsConstructor
public class NotSuitableSDOCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NotSuitable_SDO);

    public static final String NotSuitableSDO_CONFIRMATION_BODY = "<br />If a Judge has submitted this information, "
        + "a notification will be sent to the listing officer to look at this case offline."
        + "%n%nIf a legal adviser has submitted this information a notification will be sent to a judge for review.";

    private final ObjectMapper objectMapper;
    private final LocationRefDataService locationRefDataService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(MID, "not-suitableSDO-details"), this::prePopulateReasonNotSuitableSDODetailsPages)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitNotSuitableSDO)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    // This is currently a mid event but once pre states are defined it should be moved to an about to start event.
    // Once it has been moved to an about to start event the following file will need to be updated:
    //  FlowStateAllowedEventService.java.
    // This way pressing previous on the ccd page won't end up calling this method again and thus
    // repopulating the fields if they have been changed.
    // There is no reason to add conditionals to avoid this here since having it as an about to start event will mean
    // it is only ever called once.
    // Then any changes to fields in ccd will persist in ccd regardless of backwards or forwards page navigation.
    private CallbackResponse prePopulateReasonNotSuitableSDODetailsPages(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();


        ReasonNotSuitableSDO tempReasonNotSuitableSDO = ReasonNotSuitableSDO.builder()
            .input("")
            .build();

        updatedData.reasonNotSuitableSDO(tempReasonNotSuitableSDO).build();



        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }



    private CallbackResponse submitNotSuitableSDO(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder dataBuilder = getSharedData(callbackParams);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CaseData.CaseDataBuilder getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();

        dataBuilder.businessProcess(BusinessProcess.ready(NotSuitable_SDO));

        return dataBuilder;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private String getHeader(CaseData caseData) {
        return format("# Your request was accepted%n## Case has now moved offline");
    }

    private String getBody(CaseData caseData) {
        return format(NotSuitableSDO_CONFIRMATION_BODY);
    }


}
