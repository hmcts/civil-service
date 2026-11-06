package uk.gov.hmcts.reform.civil.notification.handlers.ga.gatranslateddocumentuploaded;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CASE_TITLE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.GA_LIP_RESP_NAME;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.areRespondentSolicitorsPresent;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isWithNotice;
import static uk.gov.hmcts.reform.civil.notification.handlers.ga.gatranslateddocumentuploaded.GaTranslatedDocumentUploadedApplicantEmailGenerator.REFERENCE_TEMPLATE;

@Component
@RequiredArgsConstructor
public class GaTranslatedDocumentUploadedRespondentsEmailGenerator extends GaEmailDTOGenerator {

    private final NotificationsProperties templates;
    private final GaForLipService gaForLipService;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;

    @Override
    public Boolean getShouldNotify(GaNotificationContext context) {
        var data = context.gaCaseData();
        return areRespondentSolicitorsPresent(data) && (isWithNotice(data) || data.getGeneralAppConsentOrder() == YES);
    }

    @Override
    public Set<EmailDTO> buildEmailDTOs(GaNotificationContext context, String taskId) {
        return context.gaCaseData().getGeneralAppRespondentSolicitors().stream()
            .filter(respondent -> StringUtils.isNotBlank(respondent.getValue().getEmail()))
            .map(respondent -> new EmailDTO(respondent.getValue().getEmail(), getEmailTemplateId(context),
                properties(context, respondent.getValue()), buildEmailReference(context)))
            .collect(Collectors.toSet());
    }

    @Override
    protected String getEmailAddress(GaNotificationContext context) {
        return context.gaCaseData().getGeneralAppRespondentSolicitors().getFirst().getValue().getEmail();
    }

    @Override
    protected String getEmailTemplateId(GaNotificationContext context) {
        var data = context.gaCaseData();
        if (gaForLipService.isLipResp(data)) {
            return data.isRespondentBilingual()
                ? templates.getNotifyRespondentLiPTranslatedDocumentUploadedWhenParentCaseInBilingual()
                : templates.getLipGeneralAppRespondentEmailTemplate();
        }
        return templates.getNotifyLRTranslatedDocumentUploaded();
    }

    @Override
    protected String buildEmailReference(GaNotificationContext context) {
        return String.format(REFERENCE_TEMPLATE, context.getCcdCaseReference());
    }

    @Override
    public Map<String, String> addProperties(GaNotificationContext context) {
        return properties(context, context.gaCaseData().getGeneralAppRespondentSolicitors().getFirst().getValue());
    }

    private Map<String, String> properties(GaNotificationContext context, GASolicitorDetailsGAspec respondent) {
        var data = context.gaCaseData();
        Map<String, String> properties = new HashMap<>();
        properties.put(CASE_REFERENCE, Objects.requireNonNull(data.getParentCaseReference()));
        if (gaForLipService.isLipResp(data)) {
            properties.put(CASE_TITLE, Objects.requireNonNull(DocUploadNotificationService.getAllPartyNames(data)));
            properties.put(GA_LIP_RESP_NAME, Objects.requireNonNull(data.getParentClaimantIsApplicant().equals(NO)
                ? data.getClaimant1PartyName() : data.getDefendant1PartyName()));
        } else {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, organisationService
                .findOrganisationById(respondent.getOrganisationIdentifier())
                .map(Organisation::getName).orElseThrow(RuntimeException::new));
        }
        addAllFooterItems(data, context.mainCaseData(), properties, configuration,
            featureToggleService.isPublicQueryManagementEnabledGa(data));
        return properties;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, GaNotificationContext context) {
        return properties;
    }
}
