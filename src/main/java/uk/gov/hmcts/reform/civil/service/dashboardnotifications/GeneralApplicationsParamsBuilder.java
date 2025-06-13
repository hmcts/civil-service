package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;

@Component
@AllArgsConstructor
public class GeneralApplicationsParamsBuilder extends DashboardNotificationsParamsBuilder {

    private final FeatureToggleService featureToggleService;

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (featureToggleService.isGeneralApplicationsEnabled()) {
            params.put("djClaimantNotificationMessage", "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to vary the judgment</a>");
            params.put("djClaimantNotificationMessageCy", "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">wneud cais i amrywio’r dyfarniad</a>");
            params.put("djDefendantNotificationMessage", "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to set aside (remove) or vary the judgment</a>");
            params.put("djDefendantNotificationMessageCy", "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">wneud cais i roi’r dyfarniad o’r naill du (ei ddileu) neu amrywio’r dyfarniad</a>");
        } else {
            params.put("djClaimantNotificationMessage", "<u>make an application to vary the judgment</u>");
            params.put("djClaimantNotificationMessageCy", "<u>wneud cais i amrywio’r dyfarniad</u>");
            params.put("djDefendantNotificationMessage", "<u>make an application to set aside (remove) or vary the judgment</u>");
            params.put("djDefendantNotificationMessageCy", "<u>wneud cais i roi’r dyfarniad o’r naill du (ei ddileu) neu amrywio’r dyfarniad</u>");
        }
    }
}
