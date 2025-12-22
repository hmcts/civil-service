package uk.gov.hmcts.reform.civil.ga.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.stateflow.GaStateFlow;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPROVE_CONSENT_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_GA_ROLES;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_GENERAL_APPLICATION_CASE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_BUSINESS_PROCESS_GASPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_HEARING_SCHEDULED_PROCESS_GASPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_JUDGE_BUSINESS_PROCESS_GASPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FEE_PAYMENT_OUTCOME_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_NOTICE_DOCUMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGES_FORM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAIN_CASE_CLOSED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_PAYMENT_SERVICE_REQ_GASPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_GENERAL_APPLICATION_RESPONDENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HEARING_NOTICE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HEARING_NOTICE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.OBTAIN_ADDITIONAL_FEE_VALUE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.OBTAIN_ADDITIONAL_PAYMENT_REF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_LEGAL_ADVISOR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_APPLICATION_URGENT_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_JUDGE_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_JUDGE_DIRECTIONS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_APPLICANT_NOTIFICATION_PROCESS_MAKE_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_GA_BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_RESPONDENT_NOTIFICATION_PROCESS_MAKE_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_LOCATION_UPDATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.VALIDATE_FEE_GASPEC;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.APPLICATION_SUBMITTED_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.JUDGE_DIRECTIONS;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.JUDGE_WRITTEN_REPRESENTATION;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.LISTED_FOR_HEARING;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.PROCEED_GENERAL_APPLICATION;

@Service
@RequiredArgsConstructor
public class GaFlowStateAllowedEventService {

    private final GaStateFlowEngine stateFlowEngine;

    private static final Map<String, List<CaseEvent>> ALLOWED_EVENTS_ON_FLOW_STATE = Map.ofEntries(
        entry(DRAFT.fullName(), List.of(INITIATE_GENERAL_APPLICATION)),

        entry(APPLICATION_SUBMITTED.fullName(),
              List.of(CREATE_GENERAL_APPLICATION_CASE,
                  LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE,
                      ASSIGN_GA_ROLES,
                      VALIDATE_FEE_GASPEC,
                      MAKE_PAYMENT_SERVICE_REQ_GASPEC,
                      END_BUSINESS_PROCESS_GASPEC,
                      APPROVE_CONSENT_ORDER,
                      REFER_TO_JUDGE,
                      REFER_TO_LEGAL_ADVISOR,
                      TRIGGER_LOCATION_UPDATE,
                      APPLICATION_PROCEEDS_IN_HERITAGE,
                      NO_REMISSION_HWF_GA,
                      INVALID_HWF_REFERENCE_GA,
                      UPDATE_HELP_WITH_FEE_NUMBER_GA,
                      MORE_INFORMATION_HWF_GA,
                      FULL_REMISSION_HWF_GA,
                      PARTIAL_REMISSION_HWF_GA,
                      FEE_PAYMENT_OUTCOME_GA,
                      MAIN_CASE_CLOSED)
        ),

        entry(
            PROCEED_GENERAL_APPLICATION.fullName(),
            List.of(START_GA_BUSINESS_PROCESS,
                    NOTIFY_GENERAL_APPLICATION_RESPONDENT,
                    RESPOND_TO_APPLICATION,
                    RESPOND_TO_APPLICATION_URGENT_LIP,
                    MAKE_DECISION,
                    APPROVE_CONSENT_ORDER,
                    MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID,
                    END_BUSINESS_PROCESS_GASPEC,
                    TRIGGER_LOCATION_UPDATE,
                    REFER_TO_JUDGE,
                    REFER_TO_LEGAL_ADVISOR,
                    APPLICATION_PROCEEDS_IN_HERITAGE,
                    MAIN_CASE_CLOSED)
        ),

        entry(
            APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
            List.of(OBTAIN_ADDITIONAL_FEE_VALUE,
                    OBTAIN_ADDITIONAL_PAYMENT_REF,
                    GENERATE_JUDGES_FORM,
                    START_APPLICANT_NOTIFICATION_PROCESS_MAKE_DECISION,
                    START_RESPONDENT_NOTIFICATION_PROCESS_MAKE_DECISION,
                    END_JUDGE_BUSINESS_PROCESS_GASPEC,
                    START_GA_BUSINESS_PROCESS,
                    NOTIFY_GENERAL_APPLICATION_RESPONDENT,
                    RESPOND_TO_APPLICATION,
                    RESPOND_TO_APPLICATION_URGENT_LIP,
                    MAKE_DECISION,
                    REFER_TO_JUDGE,
                    REFER_TO_LEGAL_ADVISOR,
                    APPROVE_CONSENT_ORDER,
                    MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID,
                    TRIGGER_LOCATION_UPDATE,
                    APPLICATION_PROCEEDS_IN_HERITAGE,
                    MAIN_CASE_CLOSED)
        ),

        entry(LISTED_FOR_HEARING.fullName(),
              List.of(HEARING_SCHEDULED_GA,
                      MAKE_DECISION,
                      GENERATE_HEARING_NOTICE_DOCUMENT,
                      NOTIFY_HEARING_NOTICE_CLAIMANT,
                      NOTIFY_HEARING_NOTICE_DEFENDANT,
                      END_HEARING_SCHEDULED_PROCESS_GASPEC,
                      GENERATE_DIRECTIONS_ORDER,
                      TRIGGER_LOCATION_UPDATE,
                      APPLICATION_PROCEEDS_IN_HERITAGE,
                      MAIN_CASE_CLOSED)
        ),
        entry(ORDER_MADE.fullName(),
              List.of(HEARING_SCHEDULED_GA,
                      GENERATE_DIRECTIONS_ORDER)
        ),

        entry(ADDITIONAL_INFO.fullName(),
              List.of(RESPOND_TO_JUDGE_ADDITIONAL_INFO,
                      APPLICATION_PROCEEDS_IN_HERITAGE,
                      RESPOND_TO_APPLICATION,
                      RESPOND_TO_APPLICATION_URGENT_LIP,
                      MAKE_DECISION,
                      TRIGGER_LOCATION_UPDATE,
                      MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID,
                      NO_REMISSION_HWF_GA,
                      INVALID_HWF_REFERENCE_GA,
                      UPDATE_HELP_WITH_FEE_NUMBER_GA,
                      MORE_INFORMATION_HWF_GA,
                      FULL_REMISSION_HWF_GA,
                      PARTIAL_REMISSION_HWF_GA,
                      FEE_PAYMENT_OUTCOME_GA,
                      MAIN_CASE_CLOSED,
                      REFER_TO_JUDGE)
        ),

        entry(JUDGE_DIRECTIONS.fullName(),
              List.of(RESPOND_TO_JUDGE_DIRECTIONS,
                      APPLICATION_PROCEEDS_IN_HERITAGE,
                      TRIGGER_LOCATION_UPDATE,
                      MAKE_DECISION,
                      MAIN_CASE_CLOSED)
        ),

        entry(JUDGE_WRITTEN_REPRESENTATION.fullName(),
              List.of(RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION,
                      APPLICATION_PROCEEDS_IN_HERITAGE,
                      MAKE_DECISION,
                      TRIGGER_LOCATION_UPDATE,
                      MAIN_CASE_CLOSED)
        )
    );

    public GaFlowState getFlowState(GeneralApplicationCaseData caseData) {
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return GaFlowState.fromFullName(stateFlow.getState().getName());
    }

    public boolean isAllowed(CaseDetails caseDetails, CaseEvent caseEvent) {
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseDetails);
        return isAllowedOnState(stateFlow.getState().getName(), caseEvent);
    }

    public boolean isAllowedOnState(String stateFullName, CaseEvent caseEvent) {
        return ALLOWED_EVENTS_ON_FLOW_STATE
            .getOrDefault(stateFullName, emptyList())
            .contains(caseEvent);
    }
}
