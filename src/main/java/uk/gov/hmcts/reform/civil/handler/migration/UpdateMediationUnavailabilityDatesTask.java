package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Component
public class UpdateMediationUnavailabilityDatesTask extends UpdateUnavailableDatesTask {

    private static final String DEFENDANT = "defendant";

    @Override
    protected String getEventSummary() {
        return "Update mediation unavailable dates via migration task";
    }

    @Override
    protected String getTaskName() {
        return "UpdateMediationUnavailabilityDatesTask";
    }

    @Override
    protected String getEventDescription() {
        return "This task restores missing mediation unavailable dates on the case";
    }

    @Override
    protected List<Element<UnavailableDate>> getUnavailableDates(CaseData caseData, String partyType) {
        CaseDataLiP caseDataLiP = caseData.getCaseDataLiP();
        MediationLiPCarm response = null;
        if (caseDataLiP != null) {
            if (DEFENDANT.equals(partyType)) {
                response = caseDataLiP.getRespondent1MediationLiPResponseCarm();
            } else {
                response = caseDataLiP.getApplicant1LiPResponseCarm();
            }
        }
        if (response == null || response.getUnavailableDatesForMediation() == null
            || response.getUnavailableDatesForMediation().isEmpty()) {
            throw new IllegalStateException("Mediation unavailable dates must not be null or empty");
        }
        return response.getUnavailableDatesForMediation();
    }
}
