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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
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

    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;
    private final Time time;
    private final FeatureToggleService featureToggleService;
    private final CaseFlagsInitialiser caseFlagsInitialiser;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateRespondentCopyObjects,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateRespondentCopyObjects(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = getUpdatedCaseData(callbackParams);
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        if (featureToggleService.isHmcEnabled()) {
            populateDQPartyIds(builder);
        }

        if (featureToggleService.isUpdateContactDetailsEnabled()) {
            addEventAndDateAddedToRespondentExperts(builder);
            addEventAndDateAddedToRespondentWitnesses(builder);
        }
        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE_CUI, builder);
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(
            builder, featureToggleService.isUpdateContactDetailsEnabled());

        if (ofNullable(caseData.getRespondent1Copy()).isPresent()) {
            Party updatedRespondent1 = caseData.getRespondent1().toBuilder()
                .flags(caseData.getRespondent1Copy().getFlags())
                .partyID(caseData.getRespondent1Copy().getPartyID())
                .build();
            builder.respondent1(updatedRespondent1)
                .respondent1Copy(null);
        }

        CaseData updatedData = builder.build();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder().data(updatedData.toMap(objectMapper));

        if (!caseData.isRespondentResponseBilingual()) {
            responseBuilder.state(CaseState.AWAITING_APPLICANT_INTENTION.name());
        }

        return responseBuilder.build();
    }

    private CaseData getUpdatedCaseData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseDocument dummyDocument = new CaseDocument(null, null, null, 0, null, null, null);
        LocalDateTime responseDate = time.now();
        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();
        CaseData updatedData = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_CUI))
            .respondent1ResponseDate(responseDate)
            .respondent1GeneratedResponseDocument(dummyDocument)
            .respondent1ClaimResponseDocumentSpec(dummyDocument)
            .responseClaimTrack(AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null).name())
            .applicant1ResponseDeadline(deadlinesCalculator.calculateApplicantResponseDeadline(
                responseDate,
                allocatedTrack
            ))
            .build();
        return updatedData;
    }
}
