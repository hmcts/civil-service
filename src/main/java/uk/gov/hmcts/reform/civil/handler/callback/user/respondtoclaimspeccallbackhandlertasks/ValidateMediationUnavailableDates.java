package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.MediationUnavailableDatesUtils.checkUnavailable;
import static uk.gov.hmcts.reform.civil.utils.MediationUnavailableDatesUtils.normalizeUnavailableDates;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateMediationUnavailableDates implements CaseTask {

    private final ObjectMapper objectMapper;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing mediation unavailable dates validation for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());

        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        validateMediationAvailability(caseData.getResp1MediationAvailability(), errors);
        validateMediationAvailability(caseData.getResp2MediationAvailability(), errors);

        if (errors.isEmpty()) {
            normalizeMediationUnavailableDates(caseData);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
    }

    private void validateMediationAvailability(MediationAvailability mediationAvailability, List<String> errors) {
        if (mediationAvailability != null && YES.equals(mediationAvailability.getIsMediationUnavailablityExists())) {
            checkUnavailable(errors, mediationAvailability.getUnavailableDatesForMediation());
        }
    }

    public void normalizeMediationUnavailableDates(CaseData caseData) {
        MediationAvailability resp1MediationAvailability = caseData.getResp1MediationAvailability();
        if (resp1MediationAvailability != null && YES.equals(resp1MediationAvailability.getIsMediationUnavailablityExists())) {
            normalizeUnavailableDates(resp1MediationAvailability.getUnavailableDatesForMediation());
        }
        MediationAvailability resp2MediationAvailability = caseData.getResp2MediationAvailability();
        if (resp2MediationAvailability != null && YES.equals(resp2MediationAvailability.getIsMediationUnavailablityExists())) {
            normalizeUnavailableDates(resp2MediationAvailability.getUnavailableDatesForMediation());
        }
    }
}
