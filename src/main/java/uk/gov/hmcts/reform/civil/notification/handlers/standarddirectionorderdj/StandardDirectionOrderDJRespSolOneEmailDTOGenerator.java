package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoLegalRep;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StandardDirectionOrderDJRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-defendant-%s";
    private final StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper;
    private final StandardDirectionOrderDJNotificationHelper notificationHelper;

    public StandardDirectionOrderDJRespSolOneEmailDTOGenerator(
        OrganisationService organisationService,
        StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper,
        StandardDirectionOrderDJNotificationHelper notificationHelper
    ) {
        super(organisationService);
        this.templateHelper = templateHelper;
        this.notificationHelper = notificationHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return templateHelper.getEmailTemplateId(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_SDO_DJ;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return false;
        }
        if (notificationHelper.isTargetDefendant(caseData, caseData.getRespondent1())) {
            return true;
        }
        // In 1v2 with a single legal rep, respondent 1's solicitor also represents respondent 2,
        // so they must be notified when respondent 2 is the selected defendant.
        return isOneVTwoLegalRep(caseData)
            && notificationHelper.isTargetDefendant(caseData, caseData.getRespondent2());
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(StandardDirectionOrderDJEmailDTOGeneratorBase.LEGAL_ORG_NAME,
                       getLegalOrganizationNameForRespondent(caseData, true, organisationService));
        templateHelper.addClaimReferenceNumber(properties, caseData);
        return properties;
    }
}
