package uk.gov.hmcts.reform.unspec.service.flowstate;

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
        PENDING_CASE_ISSUED,
        PAYMENT_SUCCESSFUL,
        PAYMENT_FAILED,
        CLAIM_ISSUED,
        CLAIM_STAYED,
        SERVICE_ACKNOWLEDGED,
        EXTENSION_REQUESTED,
        EXTENSION_RESPONDED,
        RESPONDED_TO_CLAIM,
        FULL_DEFENCE,
        CLAIM_WITHDRAWN,
        CLAIM_DISCONTINUED,
        CASE_PROCEEDS_IN_CASEMAN,
        PROCEEDS_WITH_OFFLINE_JOURNEY;

        public static final String FLOW_NAME = "MAIN";

        @Override
        public String fullName() {
            return FLOW_NAME + "." + name();
        }
    }
}
