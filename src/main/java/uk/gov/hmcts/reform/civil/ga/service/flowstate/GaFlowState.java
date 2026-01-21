package uk.gov.hmcts.reform.civil.ga.service.flowstate;

import static org.springframework.util.StringUtils.hasLength;

public interface GaFlowState {

    String fullName();

    static GaFlowState fromFullName(String fullName) {
        if (!hasLength(fullName)) {
            throw new IllegalArgumentException("Invalid full name:" + fullName);
        }
        int lastIndexOfDot = fullName.lastIndexOf('.');
        String flowStateName = fullName.substring(lastIndexOfDot + 1);
        String flowName = fullName.substring(0, lastIndexOfDot);
        if (flowName.equals("MAIN")) {
            return Main.valueOf(flowStateName);
        } else {
            throw new IllegalArgumentException("Invalid flow name:" + flowName);
        }
    }

    enum Main implements GaFlowState {
        DRAFT,
        APPLICATION_SUBMITTED,
        PROCEED_GENERAL_APPLICATION,
        ORDER_MADE,
        AWAITING_RESPONDENT_RESPONSE,
        APPLICATION_SUBMITTED_JUDICIAL_DECISION,
        LISTED_FOR_HEARING,
        ADDITIONAL_INFO,
        JUDGE_DIRECTIONS,
        HEARING_SCHEDULED,
        JUDGE_WRITTEN_REPRESENTATION;

        public static final String FLOW_NAME = "MAIN";

        @Override
        public String fullName() {
            return FLOW_NAME + "." + name();
        }
    }
}
