package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;

import java.util.List;

@Component
public class RoboticsPartyLookup {

    private static final int SOLICITOR_REFERENCE_LIMIT = 24;

    private static final List<String> APPLICANT_IDS = List.of(
        RoboticsDataUtil.APPLICANT_ID,
        RoboticsDataUtil.APPLICANT2_ID
    );

    private static final List<String> RESPONDENT_IDS = List.of(
        RoboticsDataUtil.RESPONDENT_ID,
        RoboticsDataUtil.RESPONDENT2_ID
    );

        public String applicantId(int index) {
        return lookup(APPLICANT_IDS, index, "applicant");
    }

        public String respondentId(int index) {
        return lookup(RESPONDENT_IDS, index, "respondent");
    }

        public String truncateReference(String reference) {
        if (reference == null) {
            return null;
        }
        return reference.substring(0, Math.min(reference.length(), SOLICITOR_REFERENCE_LIMIT));
    }

    private String lookup(List<String> ids, int index, String label) {
        if (index < 0 || index >= ids.size()) {
            throw new IllegalArgumentException("Unsupported " + label + " index: " + index);
        }
        return ids.get(index);
    }
}
