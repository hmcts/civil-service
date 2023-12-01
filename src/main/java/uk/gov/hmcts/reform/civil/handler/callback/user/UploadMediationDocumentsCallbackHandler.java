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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_MEDIATION_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.addApplicantOptions;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.addDefendant1Option;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.addDefendant2Option;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.addSameSolicitorDefendantOptions;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadMediationDocumentsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPLOAD_MEDIATION_DOCUMENTS);

    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "populate-party-options"), this::populatePartyOptions,
            callbackKey(ABOUT_TO_SUBMIT), this::submitData
        );
    }

    private CallbackResponse populatePartyOptions(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        UserInfo userInfo = userService.getUserInfo(authToken);
        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        List<DynamicListElement> dynamicListOptions = new ArrayList<>();

        if (isApplicantSolicitor(roles)) {
            addApplicantOptions(dynamicListOptions, caseData);
        } else if (isRespondentSolicitorOne(roles) && !isRespondentSolicitorTwo(roles)) {
            // 1v1 or 1v2DS respondent 1 solicitor
            addDefendant1Option(dynamicListOptions, caseData);
        } else if (!isRespondentSolicitorOne(roles) && isRespondentSolicitorTwo(roles)) {
            // 1v2 DS respondent 2 solicitor
            addDefendant2Option(dynamicListOptions, caseData);
        } else {
            // 1v2 SS
            addSameSolicitorDefendantOptions(dynamicListOptions, caseData);
        }

        builder.uploadMediationDocumentsPartyChosen(DynamicList.fromDynamicListElementList(dynamicListOptions));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        builder.uploadMediationDocumentsPartyChosen(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
