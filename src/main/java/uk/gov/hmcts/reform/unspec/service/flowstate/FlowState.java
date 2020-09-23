package uk.gov.hmcts.reform.unspec.service.flowstate;

import static org.springframework.util.StringUtils.isEmpty;

public interface FlowState {

    String fullName();

    static FlowState fromFullName(String fullName) {
        if (isEmpty(fullName)) {
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
        CLAIM_ISSUED,
        CLAIM_STAYED,
        SERVICE_CONFIRMED,
        SERVICE_ACKNOWLEDGED,
        EXTENSION_REQUESTED,
        EXTENSION_RESPONDED,
        RESPONDED_TO_CLAIM,
        FULL_DEFENCE;

        public static final String FLOW_NAME = "MAIN";

        @Override
        public String fullName() {
            return FLOW_NAME + "." + name();
        }
    }
}
