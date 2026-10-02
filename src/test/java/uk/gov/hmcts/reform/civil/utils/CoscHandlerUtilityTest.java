package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CoscHandlerUtilityTest {

    private static final Long PARENT_CASE_REFERENCE = 1594901956117591L;
    private static final String COSC_CASE_REFERENCE = "1644495739087775";

    @Mock
    private DashboardNotificationService dashboardNotificationService;

    @Test
    void shouldDeleteApplicantNotificationWhenCoscApplicationHasCaseLink() {
        CaseLink caseLink = new CaseLink();
        caseLink.setCaseReference(COSC_CASE_REFERENCE);

        CoscHandlerUtility.addBeforeRecordScenario(
            caseDataWithCoscApplication(caseLink),
            dashboardNotificationService
        );

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            COSC_CASE_REFERENCE,
            "APPLICANT"
        );
    }

    @Test
    void shouldSkipNotificationCleanupWhenCoscApplicationHasNoCaseLink() {
        CoscHandlerUtility.addBeforeRecordScenario(
            caseDataWithCoscApplication(null),
            dashboardNotificationService
        );

        verifyNoInteractions(dashboardNotificationService);
    }

    @Test
    void shouldSkipNotificationCleanupWhenCoscCaseLinkHasNoCaseReference() {
        CoscHandlerUtility.addBeforeRecordScenario(
            caseDataWithCoscApplication(new CaseLink()),
            dashboardNotificationService
        );

        verifyNoInteractions(dashboardNotificationService);
    }

    private CaseData caseDataWithCoscApplication(CaseLink caseLink) {
        GAApplicationType applicationType = new GAApplicationType();
        applicationType.setTypes(singletonList(CONFIRM_CCJ_DEBT_PAID));
        GeneralApplication application = new GeneralApplication();
        application.setGeneralAppType(applicationType);
        application.setCaseLink(caseLink);

        return CaseData.builder()
            .ccdCaseReference(PARENT_CASE_REFERENCE)
            .generalApplications(wrapElements(application))
            .build();
    }
}
