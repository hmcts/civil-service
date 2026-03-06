package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class StandardDirectionOrderDJRespSolOneEmailDTOGenerator
    extends AbstractStandardDirectionOrderDJRespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-defendant-%s";

    public StandardDirectionOrderDJRespSolOneEmailDTOGenerator(
        OrganisationService organisationService,
        StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper
    ) {
        super(organisationService, templateHelper);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_SDO_DJ;
    }
}
