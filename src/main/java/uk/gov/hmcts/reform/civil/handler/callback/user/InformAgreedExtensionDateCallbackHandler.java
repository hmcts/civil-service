package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DeadlineExtensionValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Slf4j
@Service
@RequiredArgsConstructor
public class InformAgreedExtensionDateCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(INFORM_AGREED_EXTENSION_DATE);

    private final ExitSurveyContentService exitSurveyContentService;
    private final DeadlineExtensionValidator validator;
    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;
    private final Time time;
    private final CoreCaseUserService coreCaseUserService;
    private final IStateFlowEngine stateFlowEngine;
    private final UserService userService;
    private final FeatureToggleService toggleService;
    public static final String SPEC_ACKNOWLEDGEMENT_OF_SERVICE = "ACKNOWLEDGEMENT_OF_SERVICE";
    public static final String ERROR_EXTENSION_DATE_SUBMITTED =
        "This action cannot currently be performed because it has already been completed";
    public static final String ERROR_DEADLINE_PAST =
        "You can no longer request an \"Inform agreed 28 day extension\" as the deadline has passed.";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateIsRespondent1Flag,
            callbackKey(MID, "extension-date"), this::validateExtensionDate,
            callbackKey(ABOUT_TO_SUBMIT), this::setResponseDeadline,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateIsRespondent1Flag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        LocalDateTime currentResponseDeadline = caseData.getRespondent1ResponseDeadline();

        if (multiPartyScenario.equals(ONE_V_TWO_TWO_LEGAL_REP) && solicitorRepresentsOnlyRespondent2(callbackParams)) {
            currentResponseDeadline = caseData.getRespondent2ResponseDeadline();
        }

        if ((caseData.getNextDeadline() != null && caseData.getNextDeadline().isBefore(LocalDate.now()))
            || (currentResponseDeadline != null && currentResponseDeadline.toLocalDate().isBefore(LocalDate.now()))) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_DEADLINE_PAST))
                .build();
        }

        var isRespondent1 = YES;
        if (solicitorRepresentsOnlyRespondent2(callbackParams)) {
            isRespondent1 = NO;
        }

        if ((multiPartyScenario.equals(ONE_V_ONE) || multiPartyScenario.equals(TWO_V_ONE)
            || multiPartyScenario.equals(ONE_V_TWO_ONE_LEGAL_REP))
            && caseData.getRespondent1TimeExtensionDate() != null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_EXTENSION_DATE_SUBMITTED))
                .build();
        }
        // Show error message if defendant tries to extend date again ONE_V_TWO_TWO_LEGAL_REP
        if ((!solicitorRepresentsOnlyRespondent2(callbackParams) && caseData.getRespondent1TimeExtensionDate() != null)
            || (solicitorRepresentsOnlyRespondent2(callbackParams)
            && caseData.getRespondent2TimeExtensionDate() != null)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_EXTENSION_DATE_SUBMITTED))
                .build();
        }

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder().isRespondent1(isRespondent1);
        if (caseData.getCaseAccessCategory() == CaseCategory.UNSPEC_CLAIM) {
            setMaxAllowedDate(callbackParams, caseData, builder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    /**
     * Sets the agreed deadline extension to be the maximum allowed.
     *
     * @param callbackParams callback parameters
     * @param caseData       original case data
     * @param builder        builder for new case data
     */
    private void setMaxAllowedDate(CallbackParams callbackParams,
                                   CaseData caseData,
                                   CaseData.CaseDataBuilder<?, ?> builder) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            RESPONDENTSOLICITORONE
        )) {
            builder.respondentSolicitor1AgreedDeadlineExtension(validator.getMaxDate(
                caseData.getClaimDetailsNotificationDate(),
                caseData.getRespondent1AcknowledgeNotificationDate()
            ));
        }
        if (coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            RESPONDENTSOLICITORTWO
        )) {
            builder.respondentSolicitor2AgreedDeadlineExtension(validator.getMaxDate(
                caseData.getClaimDetailsNotificationDate(),
                caseData.getRespondent2AcknowledgeNotificationDate()
            ));
        }
    }

    private CallbackResponse validateExtensionDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate agreedExtension = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        LocalDateTime currentResponseDeadline = caseData.getRespondent1ResponseDeadline();

        if (solicitorRepresentsOnlyRespondent2(callbackParams)) {
            agreedExtension = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
        }
        if (multiPartyScenario.equals(ONE_V_TWO_TWO_LEGAL_REP) && solicitorRepresentsOnlyRespondent2(callbackParams)) {
            currentResponseDeadline = caseData.getRespondent2ResponseDeadline();
        }
        //TODO: update to get correct deadline as a part of CMC-1346
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            var isAoSApplied = SPEC_ACKNOWLEDGEMENT_OF_SERVICE.equals(caseData.getBusinessProcess().getCamundaEvent());
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(validator.specValidateProposedDeadline(agreedExtension, currentResponseDeadline, isAoSApplied))
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(validator.validateProposedDeadline(agreedExtension, currentResponseDeadline))
                .build();
        }
    }

    private CallbackResponse setResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate agreedExtension = solicitorRepresentsOnlyRespondent2(callbackParams)
            ? caseData.getRespondentSolicitor2AgreedDeadlineExtension()
            : caseData.getRespondentSolicitor1AgreedDeadlineExtension();
        log.info("Setting response deadline  Respondent2 Present ({}) AgreedExtensionDate({})",
                 solicitorRepresentsOnlyRespondent2(callbackParams), agreedExtension
        );
        if (agreedExtension == null) {
            throw new IllegalArgumentException("Agreed extension date cannot be null");
        }
        LocalDateTime newDeadline = deadlinesCalculator.calculateFirstWorkingDay(agreedExtension)
            .atTime(END_OF_BUSINESS_DAY);

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder().isRespondent1(null);

        if (caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES) {

            caseDataBuilder
                .businessProcess(BusinessProcess.ready(INFORM_AGREED_EXTENSION_DATE))
                .respondent1TimeExtensionDate(time.now())
                .respondent1ResponseDeadline(newDeadline)
                .respondent2TimeExtensionDate(time.now())
                .respondent2ResponseDeadline(newDeadline)
                .respondentSolicitor2AgreedDeadlineExtension(caseData.getRespondentSolicitor1AgreedDeadlineExtension())
                .nextDeadline(newDeadline.toLocalDate());
        } else if (solicitorRepresentsOnlyRespondent2(callbackParams)) {
            caseDataBuilder
                .businessProcess(BusinessProcess.ready(INFORM_AGREED_EXTENSION_DATE))
                .respondent2TimeExtensionDate(time.now())
                .respondent2ResponseDeadline(newDeadline)
                .nextDeadline(deadlinesCalculator.nextDeadline(
                    Arrays.asList(newDeadline, caseData.getRespondent1ResponseDeadline())).toLocalDate());
        } else {
            caseDataBuilder
                .businessProcess(BusinessProcess.ready(INFORM_AGREED_EXTENSION_DATE))
                .respondent1TimeExtensionDate(time.now())
                .respondent1ResponseDeadline(newDeadline)
                // null safe - so will work on 1v1 and multiparty scenarios
                .nextDeadline(deadlinesCalculator.nextDeadline(
                    Arrays.asList(newDeadline, caseData.getRespondent2ResponseDeadline())).toLocalDate());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String body;
        LocalDateTime responseDeadline = !solicitorRepresentsOnlyRespondent2(callbackParams)
            ? caseData.getRespondent1ResponseDeadline() : caseData.getRespondent2ResponseDeadline();
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            body = format(
                "<h2 class=\"govuk-heading-m\">What happens next</h2>You need to respond before %s",
                formatLocalDateTime(responseDeadline, DATE_TIME_AT)
            ) + exitSurveyContentService.respondentSurvey();
        } else {
            body = format(
                "<br />You must respond to the claimant by %s",
                formatLocalDateTime(responseDeadline, DATE_TIME_AT)
            ) + exitSurveyContentService.respondentSurvey();
        }
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Extension deadline submitted")
            .confirmationBody(body)
            .build();
    }

    private boolean solicitorRepresentsOnlyRespondent2(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            RESPONDENTSOLICITORTWO
        );
    }
}
