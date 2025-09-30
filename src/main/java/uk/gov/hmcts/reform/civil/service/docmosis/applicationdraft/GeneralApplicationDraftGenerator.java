package uk.gov.hmcts.reform.civil.service.docmosis.applicationdraft;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.GADraftForm;
import uk.gov.hmcts.reform.civil.model.docmosis.UnavailableDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.ListGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements.SIGN_INTERPRETER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.GENERAL_APPLICATION_DRAFT;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationDraftGenerator implements TemplateDataGenerator<GADraftForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final ListGeneratorService listGeneratorService;
    private final CoreCaseDataService coreCaseDataService;
    private static final int ONE_V_ONE = 1;
    private static final int ONE_V_TWO = 2;
    private final GaForLipService gaForLipService;

    @Override
    public GADraftForm getTemplateData(CaseData caseData)  {

        CaseDetails civilMainCase = coreCaseDataService
            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference()));
        String claimantName = listGeneratorService.claimantsName(caseData);
        String defendantName = listGeneratorService.defendantsName(caseData);

        GADraftForm.GADraftFormBuilder gaDraftFormBuilder =
            GADraftForm.builder()
                .claimNumber(caseData.getCcdCaseReference().toString())
                .claimantName(claimantName)
                .defendantName(defendantName)
                .claimantReference(getReference(civilMainCase, "applicantSolicitor1Reference"))
                .defendantReference(getReference(civilMainCase, "respondentSolicitor1Reference"))
                .date(LocalDate.now())
                .applicantPartyName(caseData.getApplicantPartyName())
                .isCasePastDueDate(validateCasePastDueDate(caseData))
                .hasAgreed(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
                .isWithNotice(isWithNoticeApplication(caseData))
                .reasonsForWithoutNotice(caseData.getGeneralAppInformOtherParty() != null ? caseData.getGeneralAppInformOtherParty()
                                             .getReasonsForWithoutNotice() : null)
                .generalAppUrgency(Objects.nonNull(caseData.getGeneralAppUrgencyRequirement())
                        ? caseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency() : null)
                .urgentAppConsiderationDate(Objects.nonNull(caseData.getGeneralAppUrgencyRequirement())
                        ? caseData.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate() : null)
                .reasonsForUrgency(Objects.nonNull(caseData.getGeneralAppUrgencyRequirement())
                        ? caseData.getGeneralAppUrgencyRequirement().getReasonsForUrgency() : null)
                .generalAppType(caseData.getGeneralAppType().getTypes().stream()
                                    .map(GeneralApplicationTypes::getDisplayedValue)
                                    .collect(Collectors.joining(", ")))
                .generalAppDetailsOfOrder(caseData.getGeneralAppDetailsOfOrder())
                .generalAppReasonsOfOrder(caseData.getGeneralAppReasonsOfOrder())
                .hearingYesorNo(Objects.nonNull(caseData.getGeneralAppHearingDate())
                                    ? caseData.getGeneralAppHearingDate().getHearingScheduledPreferenceYesNo() : null)
                .hearingDate(Objects.nonNull(caseData.getGeneralAppHearingDate())
                                 ? caseData.getGeneralAppHearingDate().getHearingScheduledDate()
                                 : null)
                .hearingPreferencesPreferredType(caseData.getGeneralAppHearingDetails()
                                                     .getHearingPreferencesPreferredType()
                                                     .getDisplayedValue())
                .reasonForPreferredHearingType(caseData.getGeneralAppHearingDetails()
                                                   .getReasonForPreferredHearingType())
                .hearingPreferredLocation(getHearingLocation(caseData))
                .hearingDetailsTelephoneNumber(caseData.getGeneralAppHearingDetails()
                                                   .getHearingDetailsTelephoneNumber())

                .hearingDetailsEmailId(caseData.getGeneralAppHearingDetails()
                                           .getHearingDetailsEmailID())
                .unavailableTrialRequiredYesOrNo(caseData.getGeneralAppHearingDetails()
                                                     .getUnavailableTrialRequiredYesOrNo())
                .unavailableTrialDates(getAppUnavailabilityDates(caseData.getGeneralAppHearingDetails()))
                .vulnerabilityQuestionsYesOrNo(caseData.getGeneralAppHearingDetails().getVulnerabilityQuestionsYesOrNo())
                .supportRequirement(getGaSupportRequirement(caseData))
                .supportRequirementSignLanguage(caseData.getGeneralAppHearingDetails().getSupportRequirementSignLanguage())
                .isSignLanguageExists(checkAdditionalSupport(caseData, SIGN_INTERPRETER))
                .supportRequirementLanguageInterpreter(caseData.getGeneralAppHearingDetails()
                                                           .getSupportRequirementLanguageInterpreter())
                .isLanguageInterpreterExists(checkAdditionalSupport(caseData, LANGUAGE_INTERPRETER))
                .supportRequirementOther(caseData.getGeneralAppHearingDetails().getSupportRequirementOther())
                .isOtherSupportExists(checkAdditionalSupport(caseData, OTHER_SUPPORT))
                .name(caseData.getGeneralAppStatementOfTruth() != null ? caseData
                    .getGeneralAppStatementOfTruth().getName() : null)
                .role(caseData.getGeneralAppStatementOfTruth() != null && caseData
                    .getGeneralAppStatementOfTruth().getRole() != null ? caseData
                    .getGeneralAppStatementOfTruth().getRole() : null)
                .date(LocalDate.now());

        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() >= ONE_V_ONE) {
            GAHearingDetails gaResp1HearingDetails = caseData.getRespondentsResponses().get(0)
                .getValue().getGaHearingDetails();
            GARespondentResponse resp1Response = caseData.getRespondentsResponses().get(0).getValue();
            gaDraftFormBuilder = gaDraftFormBuilder.build().toBuilder()
                .isVaryJudgmentApp(checkAppIsVaryJudgment(caseData))
                .isConsentOrderApp(checkAppIsConsentOrder(caseData))
                .isOneVTwoApp(caseData.getRespondentsResponses().size() >= ONE_V_TWO ? YesOrNo.YES : YesOrNo.NO)
                .resp1HasAgreed(resp1Response.getGeneralAppRespondent1Representative())
                .gaResp1Consent(resp1Response.getGeneralAppRespondent1Representative())
                .resp1DebtorOffer(caseData.getGaRespondentDebtorOffer() != null
                                      ? caseData.getGaRespondentDebtorOffer().getRespondentDebtorOffer()
                                          .getDisplayedValue() : null)
                .resp1DeclineReason(resp1Response.getGaRespondentResponseReason())
                .resp1HearingYesOrNo(gaResp1HearingDetails.getHearingYesorNo())
                .resp1HearingPreferredType(gaResp1HearingDetails
                                               .getHearingPreferencesPreferredType().getDisplayedValue())
                .resp1Hearingdate(gaResp1HearingDetails.getHearingDate())
                .resp1ReasonForPreferredType(gaResp1HearingDetails
                                                 .getReasonForPreferredHearingType())
                .resp1PreferredLocation(getRespHearingLocation(caseData, ONE_V_ONE))
                .resp1PreferredTelephone(gaResp1HearingDetails.getHearingDetailsTelephoneNumber())
                .resp1PreferredEmail(gaResp1HearingDetails.getHearingDetailsEmailID())
                .resp1UnavailableTrialRequired(gaResp1HearingDetails.getUnavailableTrialRequiredYesOrNo())
                .resp1UnavailableTrialDates(getResp1UnavailabilityDates(caseData))
                .resp1VulnerableQuestions(gaResp1HearingDetails.getVulnerabilityQuestionsYesOrNo())
                .resp1SupportRequirement(getRespSupportRequirement(caseData, ONE_V_ONE))
                .resp1SignLanguage(gaResp1HearingDetails.getSupportRequirementSignLanguage())
                .isResp1SignLanguageExists(checkResp1AdditionalSupport(caseData, SIGN_INTERPRETER))
                .resp1LanguageInterpreter(gaResp1HearingDetails
                                              .getSupportRequirementLanguageInterpreter())
                .isResp1LanguageInterpreterExists(checkResp1AdditionalSupport(caseData, LANGUAGE_INTERPRETER))
                .isResp1OtherSupportExists(checkResp1AdditionalSupport(caseData, OTHER_SUPPORT))
                .resp1Other(gaResp1HearingDetails.getSupportRequirementOther())
                .isLipCase(gaForLipService.isGaForLip(caseData) ? YesOrNo.YES : YesOrNo.NO)
                .responseSotName(caseData.getGeneralAppResponseStatementOfTruth() != null ? caseData
                    .getGeneralAppResponseStatementOfTruth().getName() : null)
                .responseSotRole(caseData.getGeneralAppResponseStatementOfTruth() != null && caseData
                    .getGeneralAppResponseStatementOfTruth().getRole() != null ? caseData
                    .getGeneralAppResponseStatementOfTruth().getRole() : null);
        }
        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() > ONE_V_ONE) {
            GAHearingDetails gaResp2HearingDetails = caseData.getRespondentsResponses().get(1)
                .getValue().getGaHearingDetails();
            GARespondentResponse resp2Response = caseData.getRespondentsResponses().get(1).getValue();
            gaDraftFormBuilder = gaDraftFormBuilder.build().toBuilder()
                .resp2HasAgreed(resp2Response.getGeneralAppRespondent1Representative())
                .gaResp2Consent(resp2Response.getGeneralAppRespondent1Representative())
                .resp2DebtorOffer(caseData.getGaRespondentDebtorOffer() != null
                                      ? caseData.getGaRespondentDebtorOffer()
                                    .getRespondentDebtorOffer().getDisplayedValue() : null)
                .resp2DeclineReason(resp2Response.getGaRespondentResponseReason())
                .resp2HearingYesOrNo(gaResp2HearingDetails.getHearingYesorNo())
                .resp2HearingPreferredType(gaResp2HearingDetails
                                               .getHearingPreferencesPreferredType().getDisplayedValue())
                .resp2Hearingdate(gaResp2HearingDetails.getHearingDate())
                .resp2ReasonForPreferredType(gaResp2HearingDetails
                                                 .getReasonForPreferredHearingType())
                .resp2PreferredLocation(getRespHearingLocation(caseData, ONE_V_TWO))
                .resp2PreferredTelephone(gaResp2HearingDetails.getHearingDetailsTelephoneNumber())
                .resp2PreferredEmail(gaResp2HearingDetails.getHearingDetailsEmailID())
                .resp2UnavailableTrialRequired(gaResp2HearingDetails.getUnavailableTrialRequiredYesOrNo())
                .resp2UnavailableTrialDates(getResp2UnavailabilityDates(caseData))
                .resp2VulnerableQuestions(gaResp2HearingDetails.getVulnerabilityQuestionsYesOrNo())
                .resp2SupportRequirement(getRespSupportRequirement(caseData, ONE_V_TWO))
                .resp2SignLanguage(gaResp2HearingDetails.getSupportRequirementSignLanguage())
                .isResp2SignLanguageExists(checkResp2AdditionalSupport(caseData, SIGN_INTERPRETER))
                .resp2LanguageInterpreter(gaResp2HearingDetails
                                              .getSupportRequirementLanguageInterpreter())
                .isResp2LanguageInterpreterExists(checkResp2AdditionalSupport(caseData, LANGUAGE_INTERPRETER))
                .resp2Other(gaResp2HearingDetails.getSupportRequirementOther())
                .isResp2OtherSupportExists(checkResp2AdditionalSupport(caseData, OTHER_SUPPORT));
        }

        return gaDraftFormBuilder.build();
    }

    private YesOrNo isWithNoticeApplication(CaseData caseData) {
        if (Objects.nonNull(caseData.getApplicationIsCloaked())
            && caseData.getApplicationIsCloaked().equals(YesOrNo.NO)) {
            return YesOrNo.YES;
        }

        return caseData.getGeneralAppInformOtherParty().getIsWithNotice();
    }

    private Boolean validateCasePastDueDate(CaseData caseData) {
        return caseData.getRespondentsResponses() == null;
    }

    private List<UnavailableDates> getAppUnavailabilityDates(GAHearingDetails hearingDetails) {
        return
            Optional.ofNullable(hearingDetails).map(GAHearingDetails::getGeneralAppUnavailableDates)
                .orElse(Collections.emptyList()).stream()
                .map(element -> element.getValue())
                .map(value -> new UnavailableDates(value.getUnavailableTrialDateFrom(), value.getUnavailableTrialDateTo()))
                .toList();
    }

    private List<UnavailableDates> getResp1UnavailabilityDates(CaseData caseData) {
        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() == ONE_V_ONE) {
            return getAppUnavailabilityDates(caseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails());
        }
        return Collections.emptyList();
    }

    private List<UnavailableDates> getResp2UnavailabilityDates(CaseData caseData) {
        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() == ONE_V_TWO) {
            return getAppUnavailabilityDates(caseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails());
        }
        return Collections.emptyList();
    }

    private Boolean checkAdditionalSupport(CaseData caseData, SupportRequirements additionalSupport) {
        String appSupportRequirement = getGaSupportRequirement(caseData);
        log.info("Check additional support for caseId: {}", caseData.getCcdCaseReference());
        return appSupportRequirement != null && appSupportRequirement.contains(additionalSupport.getDisplayedValue());
    }

    private Boolean checkResp1AdditionalSupport(CaseData caseData, SupportRequirements additionalSupport) {
        String resp1SupportRequirement = getRespSupportRequirement(caseData, ONE_V_ONE);
        log.info("Check additional support for respondent 1 for caseId: {}", caseData.getCcdCaseReference());
        return resp1SupportRequirement != null && resp1SupportRequirement.contains(additionalSupport.getDisplayedValue());
    }

    private Boolean checkResp2AdditionalSupport(CaseData caseData, SupportRequirements additionalSupport) {
        String resp2SupportRequirement = getRespSupportRequirement(caseData, ONE_V_TWO);
        log.info("Check additional support for respondent 2 for caseId: {}", caseData.getCcdCaseReference());
        return resp2SupportRequirement != null && resp2SupportRequirement.contains(additionalSupport.getDisplayedValue());

    }

    private YesOrNo checkAppIsConsentOrder(CaseData caseData) {
        YesOrNo isConsentOrderApp;
        isConsentOrderApp = caseData.getGeneralAppConsentOrder() != null ? YesOrNo.YES : YesOrNo.NO;
        return isConsentOrderApp;
    }

    private YesOrNo checkAppIsVaryJudgment(CaseData caseData) {
        YesOrNo isVaryJudgmentApp;
        isVaryJudgmentApp = caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)
            ? YesOrNo.YES : YesOrNo.NO;
        return isVaryJudgmentApp;
    }

    private String getRespSupportRequirement(CaseData caseData, int size) {
        String gaRespSupportRequirement = null;
        if (size == ONE_V_ONE  && caseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails() != null
            && caseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails()
            .getSupportRequirement() != null) {
            gaRespSupportRequirement = caseData.getRespondentsResponses().get(0).getValue()
                .getGaHearingDetails().getSupportRequirement().stream().map(
                GAHearingSupportRequirements::getDisplayedValue).collect(Collectors.joining(", "));
        } else if (size == ONE_V_TWO  && caseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails() != null
                && caseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails().getSupportRequirement() != null) {
            gaRespSupportRequirement = caseData.getRespondentsResponses().get(1).getValue()
                .getGaHearingDetails().getSupportRequirement().stream().map(
                    GAHearingSupportRequirements::getDisplayedValue).collect(Collectors.joining(", "));
        }

        return gaRespSupportRequirement;
    }

    private String getGaSupportRequirement(CaseData caseData) {
        String gaSupportRequirement = null;
        if (caseData.getGeneralAppHearingDetails() != null
            && caseData.getGeneralAppHearingDetails().getSupportRequirement() != null) {
            gaSupportRequirement = caseData.getGeneralAppHearingDetails().getSupportRequirement().stream().map(
                GAHearingSupportRequirements::getDisplayedValue).collect(Collectors.joining(", "));
        }
        return  gaSupportRequirement;
    }

    private String getHearingLocation(CaseData caseData) {
        String preferredLocation = null;
        if (caseData.getGeneralAppHearingDetails() != null
            && caseData.getGeneralAppHearingDetails().getHearingPreferredLocation() != null) {
            preferredLocation = caseData.getGeneralAppHearingDetails().getHearingPreferredLocation()
                .getValue().getLabel();
        }
        return preferredLocation;
    }

    private String getRespHearingLocation(CaseData caseData, int size) {
        String preferredLocation = null;
        GAHearingDetails resp1HearingDetails = caseData.getRespondentsResponses()
            .get(0).getValue().getGaHearingDetails();

        if (resp1HearingDetails != null
            && resp1HearingDetails.getHearingPreferredLocation() != null
            && resp1HearingDetails.getHearingPreferredLocation().getValue() != null
            && size == ONE_V_ONE) {
            preferredLocation = resp1HearingDetails.getHearingPreferredLocation()
                .getValue().getLabel();
        } else if (size == ONE_V_TWO
            && caseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails()
            .getHearingPreferredLocation() != null
            && caseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails()
            .getHearingPreferredLocation().getValue() != null) {
            preferredLocation = caseData.getRespondentsResponses().get(1).getValue()
                .getGaHearingDetails().getHearingPreferredLocation()
                .getValue().getLabel();
        }
        return preferredLocation;
    }

    @SuppressWarnings("unchecked")
    protected String getReference(CaseDetails caseData, String refKey) {
        if (nonNull(caseData.getData().get("solicitorReferences"))) {
            return ((Map<String, String>) caseData.getData().get("solicitorReferences")).get(refKey);
        }
        return null;
    }

    public CaseDocument generate(CaseData caseData, String authorisation) {
        try {
            GADraftForm templateData = getTemplateData(caseData);
            DocmosisTemplates docmosisTemplate = getDocmosisTemplate();

            DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
                templateData,
                docmosisTemplate
            );
            log.info("Generate general application draft for caseId: {}", caseData.getCcdCaseReference());

            return documentManagementService.uploadDocument(
                authorisation,
                new PDF(getFileName(docmosisTemplate), docmosisDocument.getBytes(),
                        DocumentType.GENERAL_APPLICATION_DRAFT
                )
            );
        } catch (Exception e) {
            // Catch all other exceptions
            log.error("Error generating general application draft for caseId: {}", caseData.getCcdCaseReference(), e);
            throw new RuntimeException("Error generating general application draft", e); // Rethrow or handle as needed
        }
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(docmosisTemplate.getDocumentTitle(), LocalDateTime.now().format(formatter));
    }

    private DocmosisTemplates getDocmosisTemplate() {
        return GENERAL_APPLICATION_DRAFT;
    }
}
