package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class GeneralApplicationsParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        params.put("djClaimantNotificationMessage",
            "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to vary the judgment</a>");
        params.put("djClaimantNotificationMessageCy",
            "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">wneud cais i amrywio’r dyfarniad</a>");
        params.put("djDefendantNotificationMessage",
            "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to set aside (remove) or vary the judgment</a>");
        params.put("djDefendantNotificationMessageCy",
            "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">wneud cais i roi’r dyfarniad o’r naill du (ei ddileu) neu amrywio’r dyfarniad</a>");
    }
}
