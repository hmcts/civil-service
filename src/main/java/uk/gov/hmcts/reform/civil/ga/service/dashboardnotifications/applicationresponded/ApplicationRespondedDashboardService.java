package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationresponded;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;

import java.util.function.Consumer;

@Service
public class ApplicationRespondedDashboardService {

    private final DocUploadDashboardNotificationService dashboardNotificationService;
    private final GaForLipService gaForLipService;

    public ApplicationRespondedDashboardService(DocUploadDashboardNotificationService dashboardNotificationService,
                                                GaForLipService gaForLipService) {
        this.dashboardNotificationService = dashboardNotificationService;
        this.gaForLipService = gaForLipService;
    }

    public void notifyApplicationResponded(GeneralApplicationCaseData caseData, String authToken) {
        boolean isOffline = YesOrNo.NO.equals(caseData.getParentClaimantIsApplicant())
            && caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT);

        Consumer<String> notify = role -> {
            if (isOffline) {
                dashboardNotificationService.createOfflineResponseDashboardNotification(caseData, role, authToken);
            } else {
                dashboardNotificationService.createResponseDashboardNotification(caseData, role, authToken);
            }
        };

        if (gaForLipService.isLipApp(caseData)) {
            notify.accept(DocUploadDashboardNotificationService.APPLICANT);
        }
        if (gaForLipService.isLipResp(caseData)) {
            notify.accept(DocUploadDashboardNotificationService.RESPONDENT);
        }
    }
}
