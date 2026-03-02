package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDateTime;
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
        HearingNoticeVariables camundaVariables =
            camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId());

        Long ccdCaseReference = caseData.getCcdCaseReference();
        String hearingId = camundaVariables.getHearingId();
        int requestVersion = camundaVariables.getRequestVersion().intValue();
        LocalDateTime receivedDateTime = camundaVariables.getResponseDateTime();

        PartiesNotified partiesNotified = buildPartiesNotified(camundaVariables);
        logRequestPayload(partiesNotified, ccdCaseReference, hearingId);

        try {
            hearingsService.updatePartiesNotifiedResponse(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                hearingId,
                requestVersion,
                receivedDateTime,
                partiesNotified
            );

            log.info("Successfully updated parties notified for caseId {}, hearingId {}, requestVersion {}, receivedDateTime {}",
                     ccdCaseReference, hearingId, requestVersion, receivedDateTime);

        } catch (Exception ex) {
            if (isHearingResponseNotifiedForRequestVersion(hearingId, requestVersion, receivedDateTime)) {
                log.info("Update succeeded despite exception for caseId {}, hearingId {}, requestVersion {}",
                         ccdCaseReference, hearingId, requestVersion);
            } else {
                log.error("HearingsService.updatePartiesNotifiedResponse failed for caseId {}, hearingId {}, requestVersion {}",
                          ccdCaseReference, hearingId, requestVersion, ex);
                throw ex;
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private PartiesNotified buildPartiesNotified(HearingNoticeVariables camundaVariables) {
        return new PartiesNotified()
            .setServiceData(new PartiesNotifiedServiceData()
                             .setHearingNoticeGenerated(true)
                             .setHearingLocation(camundaVariables.getHearingLocationEpims())
                             .setDays(camundaVariables.getDays()));
    }

    private void logRequestPayload(PartiesNotified payload, Long caseId, String hearingId) {
        try {
            log.info("Request payload {}, for caseId {} and hearingId {}",
                     mapper.writeValueAsString(payload), caseId, hearingId);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize PartiesNotified payload for caseId {}, hearingId {}",
                     caseId, hearingId, ex);
        }
    }

    private boolean isHearingResponseNotifiedForRequestVersion(String hearingId, int requestVersion, LocalDateTime receivedDateTime) {
        try {
            var partiesNotified = hearingsService.getPartiesNotifiedResponses(
                getSystemUpdateUser().getUserToken(), hearingId);

            PartiesNotifiedResponse response =
                HmcDataUtils.getLatestHearingResponseForRequestVersion(partiesNotified, requestVersion);

            if (response != null && response.getResponseReceivedDateTime() != null) {
                LocalDateTime partiesNotifiedDateTimeFromHMC = response.getResponseReceivedDateTime();
                return !partiesNotifiedDateTimeFromHMC.isBefore(receivedDateTime);
            }

            return false;

        } catch (Exception ex) {
            log.error("Failed to fetch parties notified responses from HMC for hearingId {}, requestVersion {}",
                      hearingId, requestVersion, ex);
            return false;
        }
    }

    private UserAuthContent getSystemUpdateUser() {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String userId = userService.getUserInfo(userToken).getUid();
        return new UserAuthContent().setUserToken(userToken).setUserId(userId);
    }
}
