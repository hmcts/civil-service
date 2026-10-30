package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Component
@RequiredArgsConstructor
public class BreathingSpaceEmailHelper implements NotificationData {

    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        Map<String, String> properties = new HashMap<>();
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    public String getEnterOrganisationName(OrganisationPolicy policy, Supplier<String> fallback) {
        Optional<String> name = Optional.ofNullable(policy)
            .map(OrganisationPolicy::getOrganisation)
            .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID)
            .map(organisationService::findOrganisationById)
            .flatMap(organisation -> organisation.map(Organisation::getName));
        return fallback == null ? name.orElse(null) : name.orElseGet(fallback);
    }

    public String getLiftedOrganisationName(OrganisationPolicy policy, Supplier<String> fallback) {
        String id = policy.getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }
        return fallback == null ? null : fallback.get();
    }
}
