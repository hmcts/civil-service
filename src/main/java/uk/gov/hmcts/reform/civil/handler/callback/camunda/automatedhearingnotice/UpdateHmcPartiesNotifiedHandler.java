package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.data.UserAuthContent;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateHmcPartiesNotifiedHandler extends CallbackHandler {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.UPDATE_PARTIES_NOTIFIED_HMC
    );

    private final HearingNoticeCamundaService camundaService;
    private final HearingsService hearingsService;
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;

    private static final String TASK_ID = "UpdatePartiesNotifiedHmc";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updatePartiesNotified);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse updatePartiesNotified(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        HearingNoticeVariables camundaVariables = camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId());

        PartiesNotified partiesNotified = PartiesNotified.builder()
            .serviceData(PartiesNotifiedServiceData.builder()
                             .hearingNoticeGenerated(true)
                             .hearingLocation(camundaVariables.getHearingLocationEpims())
                             .days(camundaVariables.getDays())
                             .build())
            .build();

        Long ccdCaseReference = caseData.getCcdCaseReference();
        String hearingId = camundaVariables.getHearingId();
        int requestVersion = camundaVariables.getRequestVersion().intValue();

        PartiesNotifiedResponse latestPartiesNotifiedResponse = getLatestPartiesNotifiedResponse(hearingId, requestVersion);
        if (latestPartiesNotifiedResponse != null && latestPartiesNotifiedResponse.getResponseReceivedDateTime() != null) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        try {
            log.info("Request payload {}, for caseId {} and hearingId {}",
                     mapper.writeValueAsString(partiesNotified), ccdCaseReference, hearingId);

            ResponseEntity<?> responseEntity = hearingsService.updatePartiesNotifiedResponse(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                hearingId, requestVersion,
                camundaVariables.getResponseDateTime(), partiesNotified
            );

            if (responseEntity == null) {
                log.error("Null response received from HearingsService for caseId {}, hearingId {}", ccdCaseReference, hearingId);
            } else if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Non-success status from HearingsService for caseId {}, hearingId {}: {} error [{}]",
                         ccdCaseReference, hearingId, responseEntity.getStatusCode(), responseEntity.getBody());
            } else {
                log.info("Successfully updated parties notified for caseId {}, hearingId {} with status {}",
                         ccdCaseReference, hearingId, responseEntity.getStatusCode());
            }
        } catch (org.springframework.web.client.RestClientException | JsonProcessingException ex) {
            log.error("Failed to call HearingsService.updatePartiesNotifiedResponse for caseId {}, hearingId {}: {}",
                      ccdCaseReference, hearingId, ex.getMessage(), ex);
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private PartiesNotifiedResponse getLatestPartiesNotifiedResponse(String hearingId, int requestVersion) {
        var partiesNotified = hearingsService.getPartiesNotifiedResponses(
            getSystemUpdateUser().getUserToken(), hearingId);
        return HmcDataUtils.getLatestHearingNoticeDetails(partiesNotified, requestVersion);
    }

    private UserAuthContent getSystemUpdateUser() {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String userId = userService.getUserInfo(userToken).getUid();
        return new UserAuthContent().setUserToken(userToken).setUserId(userId);
    }
}
