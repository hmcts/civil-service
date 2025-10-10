package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DocUploadNotificationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.SolicitorEmailValidation;
import uk.gov.hmcts.reform.civil.utils.JudicialDecisionNotificationUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.JudicialDecisionNotificationUtil.areRespondentSolicitorsPresent;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslatedDocumentUploadedRespondentNotificationHandler extends CallbackHandler
    implements NotificationDataGA {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final CaseDetailsConverter caseDetailsConverter;
    private final GaForLipService gaForLipService;
    private final CoreCaseDataService coreCaseDataService;
    private final SolicitorEmailValidation solicitorEmailValidation;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_RESPONDENT_TRANSLATED_DOCUMENT_UPLOADED_GA);
    private static final String REFERENCE_TEMPLATE = "translated-document-uploaded-applicant-notification-%s";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondent
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData, CaseData mainCaseData) {
        return Map.of();
    }

    public Map<String, String> addPropertiesForRespondent(CaseData caseData, CaseData mainCaseData,
                                                          Element<GASolicitorDetailsGAspec> respondentSolicitor) {
        if (gaForLipService.isLipResp(caseData)) {
            String caseTitle = DocUploadNotificationService.getAllPartyNames(caseData);
            String isLipResName =
                caseData.getParentClaimantIsApplicant().equals(NO) ? caseData.getClaimant1PartyName() :
                    caseData.getDefendant1PartyName();
            HashMap<String, String> properties = new HashMap<>(Map.of(
                CASE_TITLE, Objects.requireNonNull(caseTitle),
                GA_LIP_RESP_NAME, Objects.requireNonNull(isLipResName),
                CASE_REFERENCE, caseData.getParentCaseReference()
            ));
            addAllFooterItems(caseData, mainCaseData, properties, configuration,
                              featureToggleService.isPublicQueryManagementEnabled(caseData));
            return properties;
        }
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CASE_REFERENCE, caseData.getParentCaseReference(),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(respondentSolicitor)
        ));
        addAllFooterItems(caseData, mainCaseData, properties, configuration,
                           featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    private CallbackResponse notifyRespondent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        log.info("Translated document uploaded for respondent for case: {}", caseData.getCcdCaseReference());
        CaseData civilCaseData = caseDetailsConverter
            .toCaseDataGA(coreCaseDataService
                            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        CaseData validatedCaseData = solicitorEmailValidation.validateSolicitorEmail(civilCaseData, caseData);

        if (areRespondentSolicitorsPresent(validatedCaseData)
            && (JudicialDecisionNotificationUtil.isWithNotice(caseData)
            || caseData.getGeneralAppConsentOrder() == YesOrNo.YES)) {
            validatedCaseData.getGeneralAppRespondentSolicitors().forEach(respondentSolicitor ->
                                                                              notificationService.sendMail(
                                                                                  respondentSolicitor.getValue()
                                                                                      .getEmail(),
                                                                                  addTemplate(
                                                                                      caseData
                                                                                  ),
                                                                                  addPropertiesForRespondent(
                                                                                      caseData,
                                                                                      civilCaseData,
                                                                                      respondentSolicitor
                                                                                  ),
                                                                                  String.format(
                                                                                      REFERENCE_TEMPLATE,
                                                                                      caseData.getCcdCaseReference()
                                                                                  )
                                                                              )
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String addTemplate(CaseData caseData) {
        if (gaForLipService.isLipResp(caseData)) {
            if (caseData.isRespondentBilingual()) {
                return notificationsProperties.getNotifyRespondentLiPTranslatedDocumentUploadedWhenParentCaseInBilingual();
            }
            return notificationsProperties.getLipGeneralAppRespondentEmailTemplate();
        }
        return notificationsProperties.getNotifyLRTranslatedDocumentUploaded();

    }

    public String getApplicantLegalOrganizationName(Element<GASolicitorDetailsGAspec> respondentSolicitor) {
        var organisationId = respondentSolicitor.getValue().getOrganisationIdentifier();
        return organisationService.findOrganisationById(organisationId)
                .map(Organisation::getName)
                .orElseThrow(() -> new RuntimeException("Organisation not found for id: " + organisationId));
    }

}
