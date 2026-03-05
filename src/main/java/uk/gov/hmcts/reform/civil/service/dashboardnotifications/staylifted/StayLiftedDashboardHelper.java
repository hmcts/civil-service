package uk.gov.hmcts.reform.civil.service.dashboardnotifications.staylifted;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@Service
public class StayLiftedDashboardHelper {

    public boolean hadHearingScheduled(CaseData caseData) {
        return List.of(
            HEARING_READINESS,
            PREPARE_FOR_HEARING_CONDUCT_HEARING
        ).contains(CaseState.valueOf(caseData.getPreStayState()));
    }

    public boolean isNotPreCaseProgression(CaseData caseData) {
        return !List.of(
            JUDICIAL_REFERRAL,
            IN_MEDIATION
        ).contains(CaseState.valueOf(caseData.getPreStayState()));
    }
}
