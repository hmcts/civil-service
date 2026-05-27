package uk.gov.hmcts.reform.civil.workflow.ga.fixture;

import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDate;
import java.util.List;

public final class CreateDashboardNotificationForGaApplicantFixtures {

    private static final String APPLICATION_SUBMITTED = "ga/application-submitted";

    private CreateDashboardNotificationForGaApplicantFixtures() {
    }

    public static GeneralApplicationCaseData caseData() {
        return CaseDataTemplates.load(APPLICATION_SUBMITTED, GeneralApplicationCaseData.class);
    }

    public static GeneralApplicationCaseData freeApplicationCaseData() {
        GeneralApplicationCaseData caseData = CaseDataTemplates.load(APPLICATION_SUBMITTED, GeneralApplicationCaseData.class);

        GAHearingDateGAspec hearingDate = new GAHearingDateGAspec();
        hearingDate.setHearingScheduledDate(LocalDate.now().plusDays(20));

        return caseData.copy()
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.ADJOURN_HEARING)))
            .generalAppRespondentAgreement(caseData.getGeneralAppRespondentAgreement())
            .generalAppHearingDate(hearingDate)
            .build();
    }
}
