package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@RequiredArgsConstructor
public class DjNotificationPropertiesService {

    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    public Map<String, String> buildClaimantProperties(CaseData caseData) {
        return buildProperties(
            caseData,
            resolveOrganisationName(caseData, PartyRole.CLAIMANT)
        );
    }

    public Map<String, String> buildDefendant1Properties(CaseData caseData) {
        return buildProperties(
            caseData,
            resolveOrganisationName(caseData, PartyRole.DEFENDANT_ONE)
        );
    }

    public Map<String, String> buildDefendant2Properties(CaseData caseData) {
        return buildProperties(
            caseData,
            resolveOrganisationName(caseData, PartyRole.DEFENDANT_TWO)
        );
    }

    private Map<String, String> buildProperties(CaseData caseData, String organisationName) {
        HashMap<String, String> properties = new HashMap<>();
        properties.put(LEGAL_ORG_NAME, organisationName);
        properties.put(CLAIM_NUMBER, Optional.ofNullable(caseData.getCcdCaseReference())
            .map(Object::toString)
            .orElse(""));
        properties.put(PARTY_REFERENCES, Optional.ofNullable(buildPartiesReferencesEmailSubject(caseData)).orElse(""));
        properties.put(CASEMAN_REF, Optional.ofNullable(caseData.getLegacyCaseReference()).orElse(""));
        addAllFooterItems(caseData, properties, configuration,
            featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    private String resolveOrganisationName(CaseData caseData, PartyRole role) {
        return switch (role) {
            case CLAIMANT -> organisationName(
                caseData.getApplicant1OrganisationPolicy() != null
                    ? caseData.getApplicant1OrganisationPolicy().getOrganisation()
                    : null,
                Optional.ofNullable(caseData.getApplicant1()).map(p -> p.getPartyName()).orElse(null)
            );
            case DEFENDANT_ONE -> organisationName(
                caseData.getRespondent1OrganisationPolicy() != null
                    ? caseData.getRespondent1OrganisationPolicy().getOrganisation()
                    : null,
                Optional.ofNullable(caseData.getRespondent1()).map(p -> p.getPartyName()).orElse(null)
            );
            case DEFENDANT_TWO -> organisationName(
                caseData.getRespondent2OrganisationPolicy() != null
                    ? caseData.getRespondent2OrganisationPolicy().getOrganisation()
                    : null,
                Optional.ofNullable(caseData.getRespondent2()).map(p -> p.getPartyName()).orElse(null)
            );
        };
    }

    private String organisationName(Organisation organisation, String fallback) {
        if (organisation != null && organisation.getOrganisationID() != null) {
            Optional<uk.gov.hmcts.reform.civil.prd.model.Organisation> org =
                organisationService.findOrganisationById(organisation.getOrganisationID());
            if (org.isPresent()) {
                return org.get().getName();
            }
        }
        return fallback;
    }

    private enum PartyRole {
        CLAIMANT,
        DEFENDANT_ONE,
        DEFENDANT_TWO
    }
}
