package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.CaseState.ADDITIONAL_RESPONSE_TIME_EXPIRED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ADDITIONAL_RESPONSE_TIME_PROVIDED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_DIRECTIONS_ORDER_DOCS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.LISTING_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION;

public abstract class TaskListUpdateHandler extends DashboardCallbackHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public static final List<String> APPLICANT_ACTION_NEEDED_GA_STATES = Stream.of(
        APPLICATION_PAYMENT_FAILED,
        APPLICATION_ADD_PAYMENT,
        AWAITING_APPLICATION_PAYMENT,
        AWAITING_DIRECTIONS_ORDER_DOCS,
        AWAITING_WRITTEN_REPRESENTATIONS,
        AWAITING_ADDITIONAL_INFORMATION,
        RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION
    ).map(CaseState::getDisplayedValue).toList();

    public static final List<String> RESPONDENT_ACTION_NEEDED_GA_STATES = Stream.of(
        AWAITING_DIRECTIONS_ORDER_DOCS,
        AWAITING_RESPONDENT_RESPONSE,
        ADDITIONAL_RESPONSE_TIME_PROVIDED,
        AWAITING_WRITTEN_REPRESENTATIONS,
        AWAITING_ADDITIONAL_INFORMATION,
        RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION
    ).map(CaseState::getDisplayedValue).toList();

    public static final List<String> APPLICANT_IN_PROGRESS_GA_STATES = Stream.of(
        ADDITIONAL_RESPONSE_TIME_EXPIRED,
        ADDITIONAL_RESPONSE_TIME_PROVIDED,
        PENDING_APPLICATION_ISSUED,
        HEARING_SCHEDULED,
        APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION,
        LISTING_FOR_A_HEARING,
        AWAITING_RESPONDENT_RESPONSE
    ).map(CaseState::getDisplayedValue).toList();

    public static final List<String> RESPONDENT_IN_PROGRESS_GA_STATES = Stream.of(
        APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION,
        ADDITIONAL_RESPONSE_TIME_EXPIRED,
        LISTING_FOR_A_HEARING,
        HEARING_SCHEDULED
    ).map(CaseState::getDisplayedValue).toList();

    public TaskListUpdateHandler(DashboardScenariosService dashboardScenariosService,
                                 DashboardNotificationsParamsMapper mapper,
                                 CoreCaseDataService coreCaseDataService,
                                 CaseDetailsConverter caseDetailsConverter,
                                 FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
    }


    public boolean isMainCase() {
        return true;
    }

    protected CaseData getParentCaseData(String parentCaseReference) {
        CaseDetails caseDetails = coreCaseDataService.getCase(Long.parseLong(parentCaseReference));
        return caseDetailsConverter.toCaseDataGA(caseDetails);
    }
}
