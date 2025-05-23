package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.TemplateCommonPropertiesHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Component
public class ClaimContinuingOnlineRespondentSpecRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    public ClaimContinuingOnlineRespondentSpecRespSolTwoEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService,
            TemplateCommonPropertiesHelper templateCommonPropertiesHelper
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.organisationService = organisationService;
        this.templateCommonPropertiesHelper = templateCommonPropertiesHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return YesOrNo.YES.equals(caseData.getAddRespondent2());
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        Map<String, String> props = super.addProperties(caseData);
        props.put(
                CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE)
        );
        return props;
    }

    @Override
    protected Map<String, String> addCustomProperties(
            Map<String, String> properties,
            CaseData caseData
    ) {
        boolean sameRep = YesOrNo.YES.equals(caseData.getRespondent2SameLegalRepresentative());
        String orgId = sameRep
                ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
                : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();

        Optional<String> orgName = organisationService.findOrganisationById(orgId)
                .map(Organisation::getName);
        orgName.ifPresent(name ->
                properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, name)
        );

        return properties;
    }
}
