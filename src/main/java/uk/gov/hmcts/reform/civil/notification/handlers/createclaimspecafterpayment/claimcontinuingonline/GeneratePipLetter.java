package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimcontinuingonline;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.pip.PiPLetterGenerator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_PIP_LETTER;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;

@Service
@RequiredArgsConstructor
public class GeneratePipLetter extends CallbackHandler {

    public static final String TASK_ID = "GeneratePipLetter";
    private static final String FIRST_CONTACT_PACK_LETTER_TYPE = "first-contact-pack";
    private final ObjectMapper objectMapper;
    private final PiPLetterGenerator pipLetterGenerator;
    private final BulkPrintService bulkPrintService;
    private final Time time;
    private final FeatureToggleService featureToggleService;
    private static final List<CaseEvent> EVENTS = List.of(GENERATE_PIP_LETTER);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::generatePipLetter
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generatePipLetter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime claimNotificationDate = time.now();
        final CaseData.CaseDataBuilder caseDataBuilder
                = caseData.toBuilder().claimNotificationDate(claimNotificationDate);

        if (caseData.getRespondent1() == null || caseData.getRespondent1().getPartyEmail() == null) {
            generatePIPLetter(callbackParams);
        }

        String updateCaseState = setClaimState(caseData);
        if (!YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDataBuilder.build().toMap(objectMapper))
                    .build();
        } else if (YesOrNo.YES.equals(caseData.getRespondent2SameLegalRepresentative())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDataBuilder.build().toMap(objectMapper))
                    .state(AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name())
                    .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .state(updateCaseState)
                .build();
    }

    private void generatePIPLetter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        byte[] letterContent = pipLetterGenerator.downloadLetter(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());

        List<String> recipients = Arrays.asList(caseData.getRespondent1().getPartyName());
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(), FIRST_CONTACT_PACK_LETTER_TYPE, recipients);
    }

    private String setClaimState(CaseData caseData) {
        if (!isBilingualForLipvsLip(caseData)) {
            return AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name();
        }
        return caseData.getCcdState().name();
    }

    private boolean isBilingualForLipvsLip(CaseData caseData) {
        return caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()
                && caseData.isClaimantBilingual();
    }
}
