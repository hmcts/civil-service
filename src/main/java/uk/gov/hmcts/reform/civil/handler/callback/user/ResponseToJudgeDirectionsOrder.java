package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.GaCallbackDataUtil;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.utils.DocUploadUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_JUDGE_DIRECTIONS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseToJudgeDirectionsOrder extends CallbackHandler {

    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;
    private final IdamClient idamClient;
    private final DocUploadDashboardNotificationService docUploadDashboardNotificationService;
    private final GaForLipService gaForLipService;

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RESPOND_TO_JUDGE_DIRECTIONS);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
                      callbackKey(ABOUT_TO_SUBMIT), this::submitClaim,
                      callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    protected CallbackResponse submitClaim(CallbackParams callbackParams) {

        CaseData caseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        GeneralApplicationCaseData gaCaseData = GaCallbackDataUtil.toGaCaseData(caseData, objectMapper);
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String userId = idamClient.getUserInfo(authToken).getUid();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String role = DocUploadUtils.getUserRole(caseData, gaCaseData, userId);
        DocUploadUtils.addDocumentToAddl(caseData,
                                         caseDataBuilder,
                                         ElementUtils.wrapElements(caseData.getGeneralAppDirOrderUpload()),
                                         role,
                                         CaseEvent.RESPOND_TO_JUDGE_DIRECTIONS,
                                         false
        );
        caseDataBuilder.generalAppDirOrderUpload(Collections.emptyList());
        caseDataBuilder.businessProcess(BusinessProcess.ready(RESPOND_TO_JUDGE_DIRECTIONS)).build();
        CaseData updatedCaseData = caseDataBuilder.build();

        // Generate Dashboard Notification for Lip Party
        if (gaForLipService.isGaForLip(gaCaseData)) {
            log.info("General dashboard notification for Lip party for caseId: {}", caseData.getCcdCaseReference());
            docUploadDashboardNotificationService.createDashboardNotification(caseData, role, authToken, false);
            docUploadDashboardNotificationService.createResponseDashboardNotification(caseData, "APPLICANT", authToken);
            docUploadDashboardNotificationService.createResponseDashboardNotification(caseData, "RESPONDENT", authToken);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
