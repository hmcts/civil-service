package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
public class JudgmentVariedDeterminationOfMeansClaimantEmailDTOGenerator extends EmailDTOGenerator implements NotificationData {

    private static final String REFERENCE_TEMPLATE = "claimant-judgment-varied-determination-of-means-%s";

    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration signatureConfig;
    private final FeatureToggleService featureToggleService;

    public JudgmentVariedDeterminationOfMeansClaimantEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService,
            NotificationsSignatureConfiguration signatureConfig,
            FeatureToggleService featureToggleService
    ) {
        this.notificationsProperties = notificationsProperties;
        this.organisationService = organisationService;
        this.signatureConfig = signatureConfig;
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        String recipient = caseData.isApplicantLiP()
                ? caseData.getApplicant1Email()
                : caseData.getApplicantSolicitor1UserDetails().getEmail();
        return nonNull(recipient);
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.isApplicantLiP()
                ? caseData.getApplicant1Email()
                : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            return caseData.isClaimantBilingual()
                    ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                    : notificationsProperties.getNotifyLipUpdateTemplate();
        }
        return notificationsProperties.getNotifyClaimantJudgmentVariedDeterminationOfMeansTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            return Map.of(
                    CLAIMANT_V_DEFENDANT,   getAllPartyNames(caseData),
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    PARTY_NAME,             caseData.getApplicant1().getPartyName()
            );
        }

        Map<String, String> props = new HashMap<>();
        props.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        props.put(LEGAL_ORG_NAME,         getApplicantLegalOrganizationName(caseData, organisationService));
        props.put(DEFENDANT_NAME,         getDefendantNameBasedOnCaseType(caseData));
        props.put(PARTY_REFERENCES,       buildPartiesReferencesEmailSubject(caseData));
        props.put(CASEMAN_REF,            caseData.getLegacyCaseReference());

        addCommonFooterSignature(props, signatureConfig);
        addSpecAndUnspecContact(
                caseData, props, signatureConfig,
                featureToggleService.isQueryManagementLRsEnabled()
        );

        return props;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        return properties;
    }
}
