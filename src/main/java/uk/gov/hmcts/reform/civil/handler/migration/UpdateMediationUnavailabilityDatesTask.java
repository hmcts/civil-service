package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.List;

@Component
public class UpdateMediationUnavailabilityDatesTask extends UpdateUnavailableDatesTask {

    public UpdateMediationUnavailabilityDatesTask() {
        super(
            "UpdateMediationUnavailabilityDatesTask",
            "Update mediation unavailable dates via migration task",
            "This task restores missing mediation unavailable dates on the case"
        );
    }

    @Override
    protected List<Element<UnavailableDate>> getUnavailableDates(
        CaseData caseData,
        String partyType
    ) {
        CaseDataLiP caseDataLiP = caseData.getCaseDataLiP();

        if (caseDataLiP == null) {
            return Collections.emptyList();
        }

        MediationLiPCarm response = isDefendant(partyType)
            ? caseDataLiP.getRespondent1MediationLiPResponseCarm()
            : caseDataLiP.getApplicant1LiPResponseCarm();

        if (response == null || response.getUnavailableDatesForMediation() == null) {
            return Collections.emptyList();
        }

        return response.getUnavailableDatesForMediation();
    }
}
