package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Component
public class JudgmentVariedDeterminationOfMeansRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator implements NotificationData {

    private static final String REFERENCE_TEMPLATE = "defendant-judgment-varied-determination-of-means-%s";

    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration signatureConfig;
    private final FeatureToggleService featureToggleService;

    public JudgmentVariedDeterminationOfMeansRespSolTwoEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService,
            NotificationsSignatureConfiguration signatureConfig,
            FeatureToggleService featureToggleService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.signatureConfig = signatureConfig;
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return nonNull(caseData.getRespondentSolicitor2EmailAddress())
                && YesOrNo.YES.equals(caseData.getRespondent1Represented())
                && YesOrNo.YES.equals(caseData.getRespondent2Represented());
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDefendantJudgmentVariedDeterminationOfMeansTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> props, CaseData caseData) {
        props.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        props.put(DEFENDANT_NAME_SPEC,
                getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService)
        );
        props.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        props.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        addCommonFooterSignature(props, signatureConfig);
        addSpecAndUnspecContact(
                caseData, props, signatureConfig,
                featureToggleService.isQueryManagementLRsEnabled()
        );
        return props;
    }
}
