package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationissued;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.GaDashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;

import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT;

@Service
public class ApplicationIssuedRespondentDashboardService extends GaDashboardScenarioService {

    private final GeneralAppFeesService generalAppFeesService;

    public ApplicationIssuedRespondentDashboardService(DashboardApiClient dashboardApiClient,
                                                       GaDashboardNotificationsParamsMapper mapper,
                                                       GeneralAppFeesService generalAppFeesService) {
        super(dashboardApiClient, mapper);
        this.generalAppFeesService = generalAppFeesService;
    }

    public void notifyApplicationIssued(GeneralApplicationCaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected boolean shouldRecordScenario(GeneralApplicationCaseData caseData) {
        return (Objects.nonNull(caseData.getIsGaRespondentOneLip())
            && caseData.getIsGaRespondentOneLip().equals(YES))
            || (caseData.getIsMultiParty().equals(YES)
            && Objects.nonNull(caseData.getIsGaRespondentTwoLip())
            && caseData.getIsGaRespondentTwoLip().equals(YES));
    }

    @Override
    protected String getScenario(GeneralApplicationCaseData caseData) {
        return generalAppFeesService.isFreeApplication(caseData)
            ? SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT.getScenario()
            : "";
    }
}
