package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.ga.handler.callback.user.JudicialFinalDecisionHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.ga.helpers.DateFormatHelper.JUDICIAL_FORMATTER;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.areRespondentSolicitorsPresent;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isGeneralAppConsentOrder;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.notificationCriterion;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.requiredGAType;

@Service
@RequiredArgsConstructor
public class JudicialNotificationService implements NotificationDataGA {

    private static final String RESPONDENT = "respondent";
    private static final String APPLICANT = "applicant";
    private static final Logger log = LoggerFactory.getLogger(JudicialNotificationService.class);

    private final NotificationsProperties notificationProperties;
    private final NotificationService notificationService;
    private final Map<String, String> customProps = new HashMap<>();
    private static final String REFERENCE_TEMPLATE = "general-apps-judicial-notification-make-decision-%s";
    private static final String EMPTY_SOLICITOR_REFERENCES_1V1 = "Claimant Reference: Not provided - Defendant Reference: Not provided";

    private final DeadlinesCalculator deadlinesCalculator;
    private static final int NUMBER_OF_DEADLINE_DAYS = 5;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final GaForLipService gaForLipService;

    private final SolicitorEmailValidation solicitorEmailValidation;
    private final JudicialDecisionHelper judicialDecisionHelper;

    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;

    public GeneralApplicationCaseData sendNotification(GeneralApplicationCaseData caseData, String solicitorType) throws NotificationException {
        GeneralApplicationCaseData civilCaseData = caseDetailsConverter
            .toGeneralApplicationCaseData(coreCaseDataService
                            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        caseData = solicitorEmailValidation.validateSolicitorEmail(civilCaseData, caseData);

        switch (notificationCriterion(caseData)) {
            case CONCURRENT_WRITTEN_REP:
                concurrentWrittenRepNotification(caseData, civilCaseData, solicitorType);
                break;
            case SEQUENTIAL_WRITTEN_REP:
                sequentialWrittenRepNotification(caseData, civilCaseData, solicitorType);
                break;
            case LIST_FOR_HEARING:
                applicationListForHearing(caseData, civilCaseData, solicitorType);
                break;
            case JUDGE_FREE_FORM_ORDER:
                applicationFreeFormOrder(caseData, civilCaseData, solicitorType);
                break;
            case JUDGE_APPROVED_THE_ORDER:
                applicationApprovedNotification(caseData, civilCaseData, solicitorType);
                break;
            case JUDGE_DISMISSED_APPLICATION:
                applicationDismissedByJudge(caseData, civilCaseData, solicitorType);
                break;
            case JUDGE_DIRECTION_ORDER:
                applicationDirectionOrder(caseData, civilCaseData, solicitorType);
                break;
            case REQUEST_FOR_INFORMATION:
                caseData = applicationRequestForInformation(caseData, civilCaseData, solicitorType);
                break;
            default:
            case NON_CRITERION:
        }
        return caseData;
    }

    @Override
    public Map<String, String> addProperties(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData) {
        customProps.put(
            GENAPP_REFERENCE,
            String.valueOf(Objects.requireNonNull(caseData.getCcdCaseReference()))
        );
        customProps.put(
            PARTY_REFERENCE,
            Objects.requireNonNull(getSolicitorReferences(caseData.getEmailPartyReference()))
        );
        customProps.put(
            CASE_REFERENCE,
            Objects.requireNonNull(caseData.getGeneralAppParentCaseLink().getCaseReference())
        );
        customProps.put(
            GA_APPLICATION_TYPE,
            Objects.requireNonNull(requiredGAType(caseData))
        );

        if (gaForLipService.isGaForLip(caseData)) {
            String caseTitle = JudicialFinalDecisionHandler.getAllPartyNames(caseData);
            customProps.put(
                CASE_TITLE,
                Objects.requireNonNull(caseTitle)
            );
        }
        if (gaForLipService.isLipApp(caseData)) {
            String isLipAppName = caseData.getApplicantPartyName();

            customProps.put(GA_LIP_APPLICANT_NAME, Objects.requireNonNull(isLipAppName));
            customProps.remove(GA_LIP_RESP_NAME);
        }

        if (gaForLipService.isLipResp(caseData)
            && isRespondentNotificationMakeDecisionEvent(caseData)) {
            String isLipRespondentName =
                caseData.getParentClaimantIsApplicant() == NO ? caseData.getClaimant1PartyName() :
                    caseData.getDefendant1PartyName();
            customProps.put(
                GA_LIP_RESP_NAME,
                Objects.requireNonNull(isLipRespondentName)
            );
            customProps.remove(GA_LIP_APPLICANT_NAME);
        }

        if (gaForLipService.isGaForLip(caseData)) {
            String caseTitle = JudicialFinalDecisionHandler.getAllPartyNames(caseData);
            customProps.put(
                CASE_TITLE,
                Objects.requireNonNull(caseTitle));
        } else {
            customProps.remove(GA_LIP_APPLICANT_NAME);
            customProps.remove(GA_LIP_RESP_NAME);
            customProps.remove(CASE_TITLE);
        }
        addAllFooterItems(caseData, mainCaseData, customProps, configuration,
                           featureToggleService.isPublicQueryManagementEnabledGa(caseData));
        return customProps;
    }

    private void sendNotificationForJudicialDecision(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String recipient, String template)
        throws NotificationException {
        try {
            log.info("JudicialNotificationService.class::sendNotificationForJudicialDecision::templateId: {}",
                     template);
            notificationService.sendMail(recipient, template, addProperties(caseData, mainCaseData),
                                         String.format(REFERENCE_TEMPLATE,
                                                       caseData.getGeneralAppParentCaseLink().getCaseReference()));
        } catch (NotificationException e) {
            throw new NotificationException(e);
        }
    }

    private void concurrentWrittenRepNotification(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {
        var concurrentDateText = Optional.ofNullable(caseData
                                                         .getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                                                         .getWrittenConcurrentRepresentationsBy()).orElse(null);
        customProps.put(
            GA_JUDICIAL_CONCURRENT_DATE_TEXT,
            Objects.nonNull(concurrentDateText)
                ? DateFormatHelper
                .formatLocalDate(
                    LocalDate.parse(
                        concurrentDateText.toString(),
                        JUDICIAL_FORMATTER
                    ), DATE) : null
        );

        if (solicitorType.equals(RESPONDENT) && areRespondentSolicitorsPresent(caseData)) {
            sendEmailToRespondent(
                    caseData,
                    mainCaseData,
                    useGaForLipRespondentTemplate(caseData)
                        ? getLiPRespondentTemplate(caseData)
                        : notificationProperties.getWrittenRepConcurrentRepresentationRespondentEmailTemplate()
            );
        }

        if (solicitorType.equals(APPLICANT)) {
            sendNotificationForJudicialDecision(
                caseData,
                mainCaseData,
                caseData.getGeneralAppApplnSolicitor().getEmail(),
                gaForLipService.isLipApp(caseData) ? getLiPApplicantTemplate(caseData)
                    : notificationProperties.getWrittenRepConcurrentRepresentationApplicantEmailTemplate()
            );
        }

        customProps.remove(GA_JUDICIAL_CONCURRENT_DATE_TEXT);
    }

    private String getLiPApplicantTemplate(GeneralApplicationCaseData caseData) {
        return caseData.isApplicantBilingual()
            ? notificationProperties.getLipGeneralAppApplicantEmailTemplateInWelsh()
            : notificationProperties.getLipGeneralAppApplicantEmailTemplate();
    }

    private String getLiPRespondentTemplate(GeneralApplicationCaseData caseData) {
        return caseData.isRespondentBilingual()
            ? notificationProperties.getLipGeneralAppRespondentEmailTemplateInWelsh()
            : notificationProperties.getLipGeneralAppRespondentEmailTemplate();
    }

    private void sequentialWrittenRepNotification(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {

        var sequentialDateTextRespondent = Optional
            .ofNullable(caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                            .getSequentialApplicantMustRespondWithin()).orElse(null);

        customProps.put(
            GA_JUDICIAL_SEQUENTIAL_DATE_TEXT_RESPONDENT,
            Objects.nonNull(sequentialDateTextRespondent)
                ? DateFormatHelper
                .formatLocalDate(
                    LocalDate.parse(
                        sequentialDateTextRespondent.toString(),
                        JUDICIAL_FORMATTER
                    ), DATE) : null
        );

        if (solicitorType.equals(RESPONDENT)
            && areRespondentSolicitorsPresent(caseData)) {
            sendEmailToRespondent(
                    caseData,
                    mainCaseData,
                    useGaForLipRespondentTemplate(caseData)
                        ? getLiPRespondentTemplate(caseData)
                        : notificationProperties.getWrittenRepSequentialRepresentationRespondentEmailTemplate()
            );
        }

        if (solicitorType.equals(APPLICANT)) {
            sendNotificationForJudicialDecision(
                caseData,
                mainCaseData,
                caseData.getGeneralAppApplnSolicitor().getEmail(),
                gaForLipService.isLipApp(caseData) ? getLiPApplicantTemplate(caseData)
                    : notificationProperties.getWrittenRepSequentialRepresentationApplicantEmailTemplate()
            );
        }

        customProps.remove(GA_JUDICIAL_SEQUENTIAL_DATE_TEXT_RESPONDENT);
    }

    private GeneralApplicationCaseData applicationRequestForInformation(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {

        if (solicitorType.equals(RESPONDENT)
            && (caseData.getCcdState().equals(APPLICATION_ADD_PAYMENT))) {

            // Send notification to respondent if payment is made
            caseData = addDeadlineForMoreInformationUncloakedApplication(caseData);
            var requestForInformationDeadline = caseData.getGeneralAppNotificationDeadlineDate();

            customProps.put(
                GA_NOTIFICATION_DEADLINE,
                Objects.nonNull(requestForInformationDeadline)
                    ? DateFormatHelper
                    .formatLocalDateTime(requestForInformationDeadline, DATE) : null);

            if (areRespondentSolicitorsPresent(caseData)) {
                String template = notificationProperties.getGeneralApplicationRespondentEmailTemplate();
                if (useGaForLipRespondentTemplate(caseData)) {
                    template = getLiPRespondentTemplate(caseData);
                }
                sendEmailToRespondent(
                    caseData,
                    mainCaseData,
                    template
                );
            }
            customProps.remove(GA_NOTIFICATION_DEADLINE);

        } else if ((isSendUncloakAdditionalFeeEmailForWithoutNotice(caseData)
            || isSendUncloakAdditionalFeeEmailConsentOrder(caseData))
            && caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption() == SEND_APP_TO_OTHER_PARTY) {
            // Send notification to applicant only if it's without notice application
            if (solicitorType.equals(APPLICANT)) {
                String appSolicitorEmail = caseData.getGeneralAppApplnSolicitor().getEmail();

                String template = notificationProperties.getJudgeUncloakApplicationEmailTemplate();
                if (useGaForLipApplicantTemplate(caseData)) {
                    template = getLiPApplicantTemplate(caseData);
                }
                sendNotificationForJudicialDecision(
                    caseData,
                    mainCaseData,
                    appSolicitorEmail,
                        template
                );
            }
        } else {
            // send notification to applicant and respondent if it's with notice application
            sendToBoth(caseData, mainCaseData, solicitorType);
        }
        return caseData;
    }

    private void sendToBoth(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {
        addCustomPropsForRespondDeadline(caseData.getJudicialDecisionRequestMoreInfo()
                .getJudgeRequestMoreInfoByDate());

        if (solicitorType.equals(RESPONDENT) && areRespondentSolicitorsPresent(caseData)) {
            sendEmailToRespondent(
                    caseData,
                    mainCaseData,
                    useGaForLipRespondentTemplate(caseData)
                        ? getLiPRespondentTemplate(caseData)
                        : notificationProperties.getJudgeRequestForInformationRespondentEmailTemplate()
            );
        }

        if (solicitorType.equals(APPLICANT)) {
            sendNotificationForJudicialDecision(
                    caseData,
                    mainCaseData,
                    caseData.getGeneralAppApplnSolicitor().getEmail(),
                    useGaForLipApplicantTemplate(caseData)
                        ? getLiPApplicantTemplate(caseData)
                        : notificationProperties.getJudgeRequestForInformationApplicantEmailTemplate()
            );
        }
        customProps.remove(GA_REQUEST_FOR_INFORMATION_DEADLINE);
    }

    private GeneralApplicationCaseData applicationRequestForInformationCloak(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {

        if (solicitorType.equals(APPLICANT)) {
            addCustomPropsForRespondDeadline(caseData.getJudicialDecisionRequestMoreInfo()
                                                 .getJudgeRequestMoreInfoByDate());
            sendNotificationForJudicialDecision(
                caseData,
                mainCaseData,
                caseData.getGeneralAppApplnSolicitor().getEmail(),
                useGaForLipApplicantTemplate(caseData) ? getLiPApplicantTemplate(caseData)
                    : notificationProperties.getJudgeRequestForInformationApplicantEmailTemplate()
            );

            customProps.remove(GA_REQUEST_FOR_INFORMATION_DEADLINE);
        }

        return caseData;
    }

    private void applicationApprovedNotification(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {

        if (solicitorType.equals(RESPONDENT)) {
            boolean sendEmailToDefendant = areRespondentSolicitorsPresent(caseData);

            if (sendEmailToDefendant) {
                if (useDamageTemplate(caseData)) {
                    sendEmailToRespondent(
                        caseData,
                        mainCaseData,
                        notificationProperties.getJudgeApproveOrderToStrikeOutDamages()
                    );
                } else if (useOcmcTemplate(caseData)) {
                    sendEmailToRespondent(
                        caseData,
                        mainCaseData,
                        notificationProperties.getJudgeApproveOrderToStrikeOutOCMC()
                    );
                } else if (useGaForLipRespondentTemplate(caseData)) {
                    sendEmailToRespondent(
                        caseData,
                        mainCaseData,
                        getLiPRespondentTemplate(caseData)
                    );
                } else {
                    sendEmailToRespondent(
                        caseData,
                        mainCaseData,
                        notificationProperties.getJudgeForApproveRespondentEmailTemplate()
                    );
                }
            }
        }

        if (solicitorType.equals(APPLICANT)) {
            String appSolicitorEmail = caseData.getGeneralAppApplnSolicitor().getEmail();

            if (useDamageTemplate(caseData)) {
                sendNotificationForJudicialDecision(
                    caseData,
                    mainCaseData,
                    appSolicitorEmail,
                    notificationProperties.getJudgeApproveOrderToStrikeOutDamages()
                );
            } else if (useOcmcTemplate(caseData)) {
                sendNotificationForJudicialDecision(
                    caseData,
                    mainCaseData,
                    appSolicitorEmail,
                    notificationProperties.getJudgeApproveOrderToStrikeOutOCMC()
                );
            } else if (useGaForLipApplicantTemplate(caseData)) {
                sendNotificationForJudicialDecision(
                    caseData,
                    mainCaseData,
                    appSolicitorEmail,
                    getLiPApplicantTemplate(caseData)
                );
            } else {
                sendNotificationForJudicialDecision(
                    caseData,
                    mainCaseData,
                    appSolicitorEmail,
                    notificationProperties.getJudgeForApprovedCaseApplicantEmailTemplate()
                );
            }
        }

    }

    private void applicationListForHearing(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {

        if (solicitorType.equals(RESPONDENT)) {
            /*
            * Respondent should receive notification only if it's with notice application
            *  */
            if (areRespondentSolicitorsPresent(caseData)) {

                sendEmailToRespondent(
                    caseData,
                    mainCaseData,
                    gaForLipService.isLipResp(caseData)
                        ? getLiPRespondentTemplate(caseData)
                        : notificationProperties.getJudgeListsForHearingRespondentEmailTemplate()
                );
            }
        }

        if (solicitorType.equals(APPLICANT)) {
            sendNotificationForJudicialDecision(
                caseData,
                mainCaseData,
                caseData.getGeneralAppApplnSolicitor().getEmail(),
                gaForLipService.isLipApp(caseData) ? getLiPApplicantTemplate(caseData)
                    : notificationProperties.getJudgeListsForHearingApplicantEmailTemplate()
            );
        }
    }

    private void applicationFreeFormOrder(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {

        if (solicitorType.equals(RESPONDENT)) {
            if (areRespondentSolicitorsPresent(caseData)) {
                sendEmailToRespondent(
                    caseData,
                    mainCaseData,
                    gaForLipService.isLipResp(caseData)
                        ? getLiPRespondentTemplate(caseData)
                        : notificationProperties.getJudgeFreeFormOrderRespondentEmailTemplate()
                );
            }
        }

        if (solicitorType.equals(APPLICANT)) {
            sendNotificationForJudicialDecision(
                caseData,
                mainCaseData,
                caseData.getGeneralAppApplnSolicitor().getEmail(),
                gaForLipService.isLipApp(caseData)
                    ? getLiPApplicantTemplate(caseData)
                    : notificationProperties.getJudgeFreeFormOrderApplicantEmailTemplate()
            );
        }
    }

    private void applicationDismissedByJudge(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {

        if (solicitorType.equals(RESPONDENT)
            && areRespondentSolicitorsPresent(caseData)) {
            String template  = notificationProperties.getJudgeDismissesOrderRespondentEmailTemplate();
            if (useGaForLipRespondentTemplate(caseData)) {
                template = getLiPRespondentTemplate(caseData);
            }
            sendEmailToRespondent(
                    caseData,
                    mainCaseData,
                    template
            );
        }

        if (solicitorType.equals(APPLICANT)) {
            String appSolicitorEmail = caseData.getGeneralAppApplnSolicitor().getEmail();

            sendNotificationForJudicialDecision(
                caseData,
                mainCaseData,
                appSolicitorEmail,
                useGaForLipApplicantTemplate(caseData) ? getLiPApplicantTemplate(caseData)
                    : notificationProperties.getJudgeDismissesOrderApplicantEmailTemplate()
            );
        }
    }

    private void applicationDirectionOrder(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {
        if (solicitorType.equals(RESPONDENT)
            && areRespondentSolicitorsPresent(caseData)) {
            String template = notificationProperties.getJudgeForDirectionOrderRespondentEmailTemplate();
            if (useGaForLipRespondentTemplate(caseData)) {
                template = getLiPRespondentTemplate(caseData);
            }
            sendEmailToRespondent(
                    caseData,
                    mainCaseData,
                    template
            );
        }

        if (solicitorType.equals(APPLICANT)) {
            String appSolicitorEmail = caseData.getGeneralAppApplnSolicitor().getEmail();

            sendNotificationForJudicialDecision(
                caseData,
                mainCaseData,
                appSolicitorEmail,
                useGaForLipApplicantTemplate(caseData) ? getLiPApplicantTemplate(caseData)
                    : notificationProperties.getJudgeForDirectionOrderApplicantEmailTemplate()
            );
        }
    }

    private void judgeApprovedOrderApplicationCloak(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {

        if (solicitorType.equals(APPLICANT)) {
            String appSolicitorEmail = caseData.getGeneralAppApplnSolicitor().getEmail();
            if (useDamageTemplate(caseData)) {
                sendNotificationForJudicialDecision(caseData,
                                                    mainCaseData,
                                                    appSolicitorEmail,
                                                    notificationProperties.getJudgeApproveOrderToStrikeOutDamages());
            } else if (useOcmcTemplate(caseData)) {
                sendNotificationForJudicialDecision(caseData,
                                                    mainCaseData,
                                                    appSolicitorEmail,
                                                    notificationProperties.getJudgeApproveOrderToStrikeOutOCMC());
            } else if (useGaForLipApplicantTemplate(caseData)) {
                sendNotificationForJudicialDecision(caseData,
                                                    mainCaseData,
                                                    appSolicitorEmail,
                                                    notificationProperties.getLipGeneralAppApplicantEmailTemplate());
            } else {
                sendNotificationForJudicialDecision(caseData,
                                                    mainCaseData,
                                                    appSolicitorEmail,
                                                    notificationProperties
                                                        .getJudgeForApprovedCaseApplicantEmailTemplate());
            }
        }
    }

    private void judgeDismissedOrderApplicationCloak(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {
        if (solicitorType.equals(APPLICANT)) {
            sendNotificationForJudicialDecision(
                caseData,
                mainCaseData,
                caseData.getGeneralAppApplnSolicitor().getEmail(),
                useGaForLipApplicantTemplate(caseData) ? notificationProperties.getLipGeneralAppApplicantEmailTemplate()
                    : notificationProperties.getJudgeDismissesOrderApplicantEmailTemplate()
            );
        }
    }

    private void applicationDirectionOrderCloak(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String solicitorType) {
        if (solicitorType.equals(APPLICANT)) {
            sendNotificationForJudicialDecision(
                caseData,
                mainCaseData,
                caseData.getGeneralAppApplnSolicitor().getEmail(),
                useGaForLipApplicantTemplate(caseData) ? notificationProperties.getLipGeneralAppApplicantEmailTemplate()
                    : notificationProperties.getJudgeForDirectionOrderApplicantEmailTemplate()
            );
        }
    }

    private void sendEmailToRespondent(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String notificationProperties) {
        caseData.getGeneralAppRespondentSolicitors().forEach(
            respondentSolicitor -> sendNotificationForJudicialDecision(caseData,
                                                                       mainCaseData,
                                                                       respondentSolicitor.getValue().getEmail(),
                                                                       notificationProperties
            ));
    }

    private boolean isSendUncloakAdditionalFeeEmailForWithoutNotice(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(NO)
            && caseData.getGeneralAppInformOtherParty().getIsWithNotice().equals(NO)
            && caseData.getGeneralAppPBADetails().getAdditionalPaymentDetails() == null;
    }

    private boolean isSendUncloakAdditionalFeeEmailConsentOrder(GeneralApplicationCaseData caseData) {
        return isGeneralAppConsentOrder(caseData)
            && SEND_APP_TO_OTHER_PARTY.equals(caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption())
            && caseData.getGeneralAppPBADetails().getAdditionalPaymentDetails() == null;
    }

    private String getSolicitorReferences(String emailPartyReference) {
        if (emailPartyReference != null) {
            return emailPartyReference;
        } else {
            return EMPTY_SOLICITOR_REFERENCES_1V1;
        }
    }

    private  void addCustomPropsForRespondDeadline(LocalDate requestForInformationDeadline) {
        customProps.put(
            GA_REQUEST_FOR_INFORMATION_DEADLINE,
            Objects.nonNull(requestForInformationDeadline)
                ? DateFormatHelper
                .formatLocalDate(
                    LocalDate.parse(
                        requestForInformationDeadline.toString(),
                        JUDICIAL_FORMATTER
                    ), DATE) : null
        );
    }

    private GeneralApplicationCaseData addDeadlineForMoreInformationUncloakedApplication(GeneralApplicationCaseData caseData) {

        GAJudicialRequestMoreInfo judicialRequestMoreInfo = caseData.getJudicialDecisionRequestMoreInfo();

        if (SEND_APP_TO_OTHER_PARTY.equals(judicialRequestMoreInfo.getRequestMoreInfoOption())) {

            LocalDateTime deadlineForMoreInfoSubmission = deadlinesCalculator
                .calculateApplicantResponseDeadline(
                    LocalDateTime.now(), NUMBER_OF_DEADLINE_DAYS);

            caseData = caseData.copy()
                .generalAppNotificationDeadlineDate(deadlineForMoreInfoSubmission)
                .build();
        }

        return caseData;
    }

    public static boolean useDamageTemplate(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppType().getTypes().contains(STRIKE_OUT)
            && caseData.getGeneralAppSuperClaimType().equals("UNSPEC_CLAIM");
    }

    public static boolean useOcmcTemplate(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppType().getTypes().contains(STRIKE_OUT)
            && caseData.getGeneralAppSuperClaimType().equals("SPEC_CLAIM");
    }

    public boolean useGaForLipRespondentTemplate(GeneralApplicationCaseData caseData) {
        return gaForLipService.isLipResp(caseData);
    }

    public boolean useGaForLipApplicantTemplate(GeneralApplicationCaseData caseData) {
        return gaForLipService.isLipApp(caseData);
    }

    private static boolean isRespondentNotificationMakeDecisionEvent(GeneralApplicationCaseData caseData) {

        // Case Event should be START_RESPONDENT_NOTIFICATION_PROCESS_MAKE_DECISION (OR)
        // CCD state is APPLICATION ADDLN Payment
        log.info("--- JudicialNotificationService::isRespondentNotificationMakeDecisionEvent::camundaEvent: {} ---",
                 caseData.getBusinessProcess().getCamundaEvent());
        log.info("--- JudicialNotificationService::isRespondentNotificationMakeDecisionEvent::ActivityID: {} ---",
                 caseData.getBusinessProcess().getActivityId());
        log.info("--- JudicialNotificationService::isRespondentNotificationMakeDecisionEvent::Ccdstate: {} ---",
                 caseData.getCcdState());
        var judicialDecision = Optional.ofNullable(caseData.getBusinessProcess())
            .map(BusinessProcess::getCamundaEvent).orElse(null);
        return caseData.getCcdState().equals(APPLICATION_ADD_PAYMENT)
            || (Objects.nonNull(judicialDecision)
                && (caseData.getBusinessProcess().getCamundaEvent().equals("MAKE_DECISION")
                    || caseData.getBusinessProcess().getCamundaEvent().equals("UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION"))
                && caseData.getBusinessProcess().getActivityId()
                .equals("StartRespondentNotificationProcessMakeDecision"));
    }

}
