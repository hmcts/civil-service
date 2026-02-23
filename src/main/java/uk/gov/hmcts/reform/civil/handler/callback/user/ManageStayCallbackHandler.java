package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_STAY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STAY_LIFTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STAY_UPDATE_REQUESTED;

@Service
@RequiredArgsConstructor
public class ManageStayCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(MANAGE_STAY);

    private final FeatureToggleService featureToggleService;
    private final DeadlinesCalculator deadlinesCalculator;

    private static final String HEADER_CONFIRMATION_LIFT_STAY = "# You have lifted the stay from this \n\n # case \n\n ## All parties have been notified";
    private static final String HEADER_CONFIRMATION_REQUEST_UPDATE = "# You have requested an update on \n\n # this case \n\n ## All parties have been notified";
    private static final String BODY_CONFIRMATION = "&nbsp;";
    private static final String LIFT_STAY = "LIFT_STAY";

    private static final Map<String, CaseState> STATE_MAP = Map.of(
        CaseState.IN_MEDIATION.name(), CaseState.JUDICIAL_REFERRAL,
        CaseState.JUDICIAL_REFERRAL.name(), CaseState.JUDICIAL_REFERRAL,
        CaseState.CASE_PROGRESSION.name(), CaseState.CASE_PROGRESSION,
        CaseState.HEARING_READINESS.name(), CaseState.CASE_PROGRESSION,
        CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING.name(), CaseState.CASE_PROGRESSION,
        CaseState.DECISION_OUTCOME.name(), CaseState.CASE_PROGRESSION,
        CaseState.All_FINAL_ORDERS_ISSUED.name(), CaseState.CASE_PROGRESSION
    );

    private final ObjectMapper mapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::handleAboutToStart,
            callbackKey(ABOUT_TO_SUBMIT), this::handleAboutToSubmit,
            callbackKey(SUBMITTED), this::handleSubmitted
        );
    }

    private CallbackResponse handleAboutToSubmit(CallbackParams params) {
        return manageStay(params);
    }

    private CallbackResponse handleSubmitted(CallbackParams params) {
        return addConfirmationScreen(params);
    }

    private CallbackResponse handleAboutToStart(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        caseData.setManageStayOption(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(mapper))
            .build();
    }

    private CallbackResponse manageStay(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseState newState;
        if (nonNull(caseData.getManageStayOption()) && caseData.getManageStayOption().equals(LIFT_STAY)) {
            newState = STATE_MAP.getOrDefault(caseData.getPreStayState(), CaseState.valueOf(caseData.getPreStayState()));
            caseData.setBusinessProcess(BusinessProcess.ready(STAY_LIFTED));
            caseData.setManageStayUpdateRequestDate(null);
            updateDeadlinesAfterLiftingStay(caseData);
        } else {
            newState = caseData.getCcdState();
            caseData.setBusinessProcess(BusinessProcess.ready(STAY_UPDATE_REQUESTED));
            caseData.setManageStayUpdateRequestDate(LocalDate.now());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(mapper))
            .state(newState.name())
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private SubmittedCallbackResponse addConfirmationScreen(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        String confirmationHeader = LIFT_STAY.equals(caseData.getManageStayOption())
            ? HEADER_CONFIRMATION_LIFT_STAY
            : HEADER_CONFIRMATION_REQUEST_UPDATE;

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(confirmationHeader)
            .confirmationBody(BODY_CONFIRMATION)
            .build();

    }

    private void updateDeadlinesAfterLiftingStay(CaseData caseData) {
        if (nonNull(caseData.getCaseStayDate())) {
            long daysSinceCaseStay = ChronoUnit.DAYS.between(caseData.getCaseStayDate(), LocalDate.now());

            CaseState preStayState = CaseState.valueOf(caseData.getPreStayState());

            switch (preStayState) {
                case AWAITING_RESPONDENT_ACKNOWLEDGEMENT -> {
                    if (nonNull(caseData.getRespondent1ResponseDeadline())) {
                        caseData.setRespondent1ResponseDeadline(
                            deadlinesCalculator.plusDaysAt4pmDeadline(caseData.getRespondent1ResponseDeadline(), daysSinceCaseStay)
                        );
                    }
                    if (nonNull(caseData.getRespondent2ResponseDeadline())) {
                        caseData.setRespondent2ResponseDeadline(
                            deadlinesCalculator.plusDaysAt4pmDeadline(caseData.getRespondent2ResponseDeadline(), daysSinceCaseStay)
                        );
                    }
                }
                case AWAITING_APPLICANT_INTENTION -> {
                    if (nonNull(caseData.getApplicant1ResponseDeadline())) {
                        caseData.setApplicant1ResponseDeadline(
                            deadlinesCalculator.plusDaysAt4pmDeadline(caseData.getApplicant1ResponseDeadline(), daysSinceCaseStay)
                        );
                    }
                }
                default -> {

                }
            }
        }
    }

}
