package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToRespondentWitnesses;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondToClaimCuiCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DEFENDANT_RESPONSE_CUI);
    private static final int DEFENDANT_RESPONSE_CUI_DEADLINE_EXTENSION_MONTHS = 36;

    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;
    private final Time time;
    private final FeatureToggleService featureToggleService;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    @Value("${case-flags.logging.enabled:false}")
    private boolean caseFlagsLoggingEnabled;
    private final UpdateCaseManagementDetailsService updateCaseManagementLocationDetailsService;
    private final RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        if (caseFlagsLoggingEnabled) {
            log.info(
                "case id: {}, defendant response cui before about to submit: {}",
                callbackParams.getRequest().getCaseDetails().getId(),
                callbackParams.getCaseData().getRespondent1().getFlags()
            );
        }

        CaseData caseData = getUpdatedCaseData(callbackParams);

        populateDQPartyIds(caseData);
        addEventAndDateAddedToRespondentExperts(caseData);
        addEventAndDateAddedToRespondentWitnesses(caseData);

        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE_CUI, caseData);
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(caseData);

        updateCaseManagementLocationDetailsService.updateRespondent1RequestedCourtDetails(
            callbackParams.getCaseData(), updateCaseManagementLocationDetailsService.fetchLocationData(callbackParams));

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1(callbackParams, caseData);

        caseData.setClaimDismissedDeadline(
            deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(
                DEFENDANT_RESPONSE_CUI_DEADLINE_EXTENSION_MONTHS,
                LocalDate.now()
            ));

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper));

        boolean needsTranslating = featureToggleService.isWelshEnabledForMainCase()
            ? (callbackParams.getCaseData().isRespondentResponseBilingual() || callbackParams.getCaseData().isClaimantBilingual())
            : callbackParams.getCaseData().isRespondentResponseBilingual();

        if (!needsTranslating) {
            responseBuilder.state(CaseState.AWAITING_APPLICANT_INTENTION.name());
        }

        if (caseFlagsLoggingEnabled) {
            log.info(
                "case id: {}, defendant response cui after about to submit: {}",
                callbackParams.getRequest().getCaseDetails().getId(),
                caseData.getRespondent1().getFlags()
            );
        }

        return responseBuilder.build();
    }

    private CaseData getUpdatedCaseData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        final BigDecimal respondToAdmittedClaimOwingAmount = caseData.getRespondToAdmittedClaimOwingAmount();
        if (respondToAdmittedClaimOwingAmount != null) {
            log.info(
                "case id: {}, respondToAdmittedClaimOwingAmount: {}",
                callbackParams.getRequest().getCaseDetails().getId(),
                respondToAdmittedClaimOwingAmount
            );
        }
        CaseDocument dummyDocument = new CaseDocument(null, null, null, 0, null, null, null);
        boolean needsTranslating = (caseData.isRespondentResponseBilingual() || caseData.isClaimantBilingual());
        LocalDateTime responseDate = time.now();

        LocalDateTime applicantDeadline = !needsTranslating ? deadlinesCalculator.calculateApplicantResponseDeadline(
            responseDate
        ) : null;

        caseData.setBusinessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_CUI));
        caseData.setRespondent1ResponseDate(responseDate);
        caseData.setRespondent1GeneratedResponseDocument(dummyDocument);
        caseData.setRespondent1ClaimResponseDocumentSpec(dummyDocument);
        caseData.setResponseClaimTrack(AllocatedTrack.getAllocatedTrack(
                caseData.getTotalClaimAmount(),
                null,
                null,
                featureToggleService,
                caseData
            ).name());
        caseData.setApplicant1ResponseDeadline(applicantDeadline);
        caseData.setNextDeadline(applicantDeadline != null ? applicantDeadline.toLocalDate() : null);

        if (featureToggleService.isWelshEnabledForMainCase()) {
            Optional<Language> optionalLanguage = Optional.ofNullable(caseData.getRespondent1DQ())
                .map(Respondent1DQ::getRespondent1DQLanguage).map(WelshLanguageRequirements::getDocuments);
            String respondentLanguageString = optionalLanguage.map(Language::name).orElse(null);
            optionalLanguage.ifPresent(docLanguage -> {
                CaseDataLiP caseDataLiP = caseData.getCaseDataLiP();
                RespondentLiPResponse respondent1LiPResponse = caseDataLiP != null && caseDataLiP.getRespondent1LiPResponse() != null
                    ? caseDataLiP.getRespondent1LiPResponse()
                    : new RespondentLiPResponse();

                respondent1LiPResponse.setRespondent1ResponseLanguage(docLanguage.name());

                CaseDataLiP updatedCaseDataLiP = caseDataLiP != null ? caseDataLiP : new CaseDataLiP();
                updatedCaseDataLiP.setRespondent1LiPResponse(respondent1LiPResponse);
                caseData.setCaseDataLiP(updatedCaseDataLiP);
            });
            if (respondentLanguageString == null) {
                respondentLanguageString = Optional.ofNullable(caseData.getCaseDataLiP())
                    .map(CaseDataLiP::getRespondent1LiPResponse)
                    .map(RespondentLiPResponse::getRespondent1ResponseLanguage)
                    .orElse(null);
            }
            caseData.setDefendantLanguagePreferenceDisplay(PreferredLanguage.fromString(respondentLanguageString));
        }
        return caseData;
    }
}
