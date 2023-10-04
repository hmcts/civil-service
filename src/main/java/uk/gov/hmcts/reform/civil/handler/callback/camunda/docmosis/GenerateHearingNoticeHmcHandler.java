package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.hearing.HearingNoticeHmcGenerator;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.utils.HearingFeeUtils;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.convertFromUTC;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getHearingDays;

@Service
@RequiredArgsConstructor
public class GenerateHearingNoticeHmcHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.GENERATE_HEARING_NOTICE_HMC
    );
    private static final String TASK_ID = "GenerateHearingNotice";

    private final HearingNoticeCamundaService camundaService;

    private final HearingsService hearingsService;
    private final HearingNoticeHmcGenerator hearingNoticeHmcGenerator;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateHearingNotice);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse generateHearingNotice(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();

        var camundaVars = camundaService.getProcessVariables(processInstanceId);
        var hearing = hearingsService.getHearingResponse(
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            camundaVars.getHearingId()
        );

        var hearingStartDay = HmcDataUtils.getHearingStartDay(hearing);
        var hearingStartDate = convertFromUTC(hearingStartDay.getHearingStartDateTime());

        buildDocument(callbackParams, caseDataBuilder, hearing);

        camundaService.setProcessVariables(
            processInstanceId,
            camundaVars.toBuilder()
                .hearingStartDateTime(hearingStartDate)
                .hearingLocationEpims(hearingStartDay.getHearingVenueId())
                .days(getHearingDays(hearing))
                .requestVersion(hearing.getRequestDetails().getVersionNumber())
                .caseState(caseData.getCcdState().name())
                .responseDateTime(hearing.getHearingResponse().getReceivedDateTime())
                .build()
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder
                      .hearingDate(hearingStartDate.toLocalDate())
                      .hearingDueDate(HearingFeeUtils.calculateHearingDueDate(LocalDate.now(), hearingStartDate.toLocalDate()))
                      .build().toMap(objectMapper))
            .build();
    }

    private void buildDocument(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> caseDataBuilder, HearingGetResponse hearing) {
        CaseData caseData = callbackParams.getCaseData();
        List<CaseDocument> caseDocuments = hearingNoticeHmcGenerator.generate(
            caseData,
            hearing,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(caseDocuments.get(0)));
        if (!isEmpty(caseData.getHearingDocuments())) {
            systemGeneratedCaseDocuments.addAll(caseData.getHearingDocuments());
        }
        caseDataBuilder.hearingDocuments(systemGeneratedCaseDocuments);
    }
}
