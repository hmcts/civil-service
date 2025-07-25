package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.online.fulldefence;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.toStringValueForEmail;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class DefendantResponseAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";

    NotificationsProperties notificationsProperties;

    public DefendantResponseAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String respondentName = switch (getMultiPartyScenario(caseData)) {
            case ONE_V_ONE, TWO_V_ONE -> getPartyNameBasedOnType(caseData.getRespondent1());
            default -> getPartyNameBasedOnType(caseData.getRespondent1()) +
                " and " +
                getPartyNameBasedOnType(caseData.getRespondent2());
        };
        properties.put(RESPONDENT_NAME, respondentName);
        properties.put(ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack()));
        return properties;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence();
    }
}
