package uk.gov.hmcts.reform.civil.service.flowstate;

import static org.springframework.util.StringUtils.hasLength;

public interface FlowState {

    String fullName();

    static FlowState fromFullName(String fullName) {
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

    enum Main implements FlowState {
        DRAFT,
        CLAIM_SUBMITTED,
        CLAIM_ISSUED_PAYMENT_SUCCESSFUL,
        CLAIM_ISSUED_PAYMENT_FAILED,
        PENDING_CLAIM_ISSUED,
        CLAIM_ISSUED,
        CLAIM_NOTIFIED,
        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION,
        CLAIM_DETAILS_NOTIFIED,
        NOTIFICATION_ACKNOWLEDGED,
        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION,
        FULL_DEFENCE,
        FULL_ADMISSION,
        PART_ADMISSION,
        COUNTER_CLAIM,
        FULL_DEFENCE_PROCEED,
        FULL_DEFENCE_NOT_PROCEED,
        TAKEN_OFFLINE_BY_STAFF,
        TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT,
        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT,
        TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT,
        TAKEN_OFFLINE_UNREGISTERED_DEFENDANT,
        CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE,
        CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE,
        CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;

        public static final String FLOW_NAME = "MAIN";

        @Override
        public String fullName() {
            return FLOW_NAME + "." + name();
        }
    }
}
