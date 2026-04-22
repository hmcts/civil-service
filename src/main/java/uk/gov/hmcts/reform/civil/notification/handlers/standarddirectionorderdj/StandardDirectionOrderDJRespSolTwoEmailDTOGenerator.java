package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StandardDirectionOrderDJRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-defendant-%s";
    private final StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper;
    private final StandardDirectionOrderDJNotificationHelper notificationHelper;

    public StandardDirectionOrderDJRespSolTwoEmailDTOGenerator(
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
        return YesOrNo.YES.equals(caseData.getAddRespondent2())
            && isOneVTwoTwoLegalRep(caseData)
            && !YesOrNo.YES.equals(caseData.getRespondent2Represented())
            && notificationHelper.isTargetDefendant(caseData, caseData.getRespondent2());
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(StandardDirectionOrderDJEmailDTOGeneratorBase.LEGAL_ORG_NAME,
                       getLegalOrganizationNameForRespondent(caseData, false, organisationService));
        templateHelper.addClaimReferenceNumber(properties, caseData);
        return properties;
    }
}
