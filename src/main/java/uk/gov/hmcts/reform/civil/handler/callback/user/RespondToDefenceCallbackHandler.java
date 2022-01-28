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
import uk.gov.hmcts.reform.civil.enums.ClaimantResponseScenarioFlag;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class RespondToDefenceCallbackHandler extends CallbackHandler implements ExpertsValidator, WitnessesValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE);
    private final ExitSurveyContentService exitSurveyContentService;
    private final UnavailableDateValidator unavailableDateValidator;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final FeatureToggleService featureToggleService;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateRespondent1ClaimResponseDocumentCopy,
            callbackKey(MID, "set-applicants-proceed-intention"), this::setApplicantsProceedIntention,
            callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates,
            callbackKey(MID, "experts"), this::validateApplicantExperts,
            callbackKey(MID, "witnesses"), this::validateApplicantWitnesses,
            callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse populateRespondent1ClaimResponseDocumentCopy(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder updatedData =
            caseData.toBuilder()
                .respondent1ClaimResponseDocumentCopy(caseData.getRespondent1ClaimResponseDocument());

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        if (featureToggleService.isMultipartyEnabled()){
            switch (multiPartyScenario) {
                case TWO_V_ONE:
                    updatedData
                        .claimantResponseScenarioFlag(ClaimantResponseScenarioFlag.TWO_V_ONE)
                        .build();
                    break;
                case ONE_V_TWO_ONE_LEGAL_REP:
                    updatedData
                        .claimantResponseScenarioFlag(ClaimantResponseScenarioFlag.ONE_V_TWO_SAME_SOLICITOR)
                        .build();
                    break;
                case ONE_V_TWO_TWO_LEGAL_REP:
                    updatedData
                        .claimantResponseScenarioFlag(ClaimantResponseScenarioFlag.ONE_V_TWO_DIFFERENT_SOLICITOR)
                        .build();
                    break;
                case ONE_V_ONE:
                    updatedData
                        .claimantResponseScenarioFlag(ClaimantResponseScenarioFlag.ONE_V_ONE)
                        .build();
                    break;
                default:
                    updatedData
                        .claimantResponseScenarioFlag(ClaimantResponseScenarioFlag.UNSPECIFIED_SCENARIO)
                        .build();
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateApplicantWitnesses(CallbackParams callbackParams) {
        return validateWitnesses(callbackParams.getCaseData().getApplicant1DQ());
    }

    private CallbackResponse validateApplicantExperts(CallbackParams callbackParams) {
        return validateExperts(callbackParams.getCaseData().getApplicant1DQ());
    }

    private CallbackResponse setApplicantsProceedIntention(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData =
            caseData.toBuilder().applicantsProceedIntention(NO);

        if(YES.equals(caseData.getApplicant1ProceedWithClaim())
            || YES.equals(caseData.getApplicant2ProceedWithClaim())) {
            updatedData.applicantsProceedIntention(YES);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Hearing hearing = caseData.getApplicant1DQ().getHearing();
        List<String> errors = unavailableDateValidator.validate(hearing);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse resetStatementOfTruth(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        // resetting statement of truth field, this resets in the page, but the data is still sent to the db.
        // setting null here does not clear, need to overwrite with value.
        // must be to do with the way XUI cache data entered through the lifecycle of an event.
        CaseData updatedCaseData = caseData.toBuilder()
            .uiStatementOfTruth(StatementOfTruth.builder().role("").build())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE))
            .applicant1ResponseDate(time.now());

        if (YES.equals(caseData.getApplicant1ProceedWithClaim()) || YES.equals(caseData.getApplicant2ProceedWithClaim())) {
            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Applicant1DQ dq = caseData.getApplicant1DQ().toBuilder()
                .applicant1DQStatementOfTruth(statementOfTruth)
                .build();

            builder.applicant1DQ(dq);
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            builder.uiStatementOfTruth(StatementOfTruth.builder().build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        YesOrNo proceeding = caseData.getApplicant1ProceedWithClaim();

        String claimNumber = caseData.getLegacyCaseReference();
        String title = getTitle(proceeding);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format(title, claimNumber))
            .confirmationBody(getBody(proceeding))
            .build();
    }

    private String getTitle(YesOrNo proceeding) {
        if (proceeding == YES) {
            return "# You have chosen to proceed with the claim%n## Claim number: %s";
        }
        return "# You have chosen not to proceed with the claim%n## Claim number: %s";
    }

    private String getBody(YesOrNo proceeding) {
        String dqLink = "http://www.google.com";

        if (proceeding == YES) {
            return format(
                "<br />We will review the case and contact you to tell you what to do next.%n%n"
                    + "[Download directions questionnaire](%s)", dqLink)
                + exitSurveyContentService.applicantSurvey();
        }
        return exitSurveyContentService.applicantSurvey();
    }
}
