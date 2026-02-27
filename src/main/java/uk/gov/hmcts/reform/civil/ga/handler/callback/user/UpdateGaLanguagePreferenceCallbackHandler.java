package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_GA_LANGUAGE_UPDATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateGaLanguagePreferenceCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(TRIGGER_GA_LANGUAGE_UPDATE);
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;

    private static final List<String> BILINGUAL_TYPES = Arrays.asList("BOTH", "WELSH");

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::updateLanguagePreference
        );
    }

    private CallbackResponse updateLanguagePreference(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData civilCaseData = caseDetailsConverter
            .toGeneralApplicationCaseData(coreCaseDataService
                            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));
        boolean claimantBilingual = BILINGUAL_TYPES.contains(civilCaseData.getClaimantBilingualLanguagePreference());
        boolean defendantBilingual = civilCaseData.getRespondent1LiPResponse() != null
            && BILINGUAL_TYPES.contains(civilCaseData.getRespondent1LiPResponse().getRespondent1ResponseLanguage());
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        if (caseData.getParentClaimantIsApplicant() == YesOrNo.YES) {
            caseDataBuilder.applicantBilingualLanguagePreference(claimantBilingual ? YesOrNo.YES : YesOrNo.NO);
            caseDataBuilder.respondentBilingualLanguagePreference(defendantBilingual ? YesOrNo.YES : YesOrNo.NO);
        } else {
            caseDataBuilder.applicantBilingualLanguagePreference(defendantBilingual ? YesOrNo.YES : YesOrNo.NO);
            caseDataBuilder.respondentBilingualLanguagePreference(claimantBilingual ? YesOrNo.YES : YesOrNo.NO);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
