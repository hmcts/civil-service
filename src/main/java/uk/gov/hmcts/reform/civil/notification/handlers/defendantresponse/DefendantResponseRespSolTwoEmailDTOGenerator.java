package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.toStringValueForEmail;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class DefendantResponseRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public DefendantResponseRespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }


    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()) +
            " and " +
            getPartyNameBasedOnType(caseData.getRespondent2()));
        properties.put(ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack()));
        return properties;
    }
}
