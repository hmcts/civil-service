package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.GADraftForm;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.UnavailableDates;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
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
    private final ObjectMapper objectMapper;
    private static final int ONE_V_ONE = 1;
    private static final int ONE_V_TWO = 2;
    private final GaForLipService gaForLipService;

    @Override
    public GADraftForm getTemplateData(GeneralApplicationCaseData caseData)  {

        CaseDetails civilMainCase = coreCaseDataService
            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference()));
        String claimantName = listGeneratorService.claimantsName(caseData);
        String defendantName = listGeneratorService.defendantsName(caseData);

        GADraftForm gaDraftForm = new GADraftForm()
            .setApplicationId(caseData.getCcdCaseReference().toString())
            .setClaimNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
            .setClaimantName(claimantName)
            .setDefendantName(defendantName)
            .setClaimantReference(getReference(civilMainCase, "applicantSolicitor1Reference"))
            .setDefendantReference(getReference(civilMainCase, "respondentSolicitor1Reference"))
            .setDate(LocalDate.now())
            .setApplicantPartyName(caseData.getApplicantPartyName())
            .setIsCasePastDueDate(validateCasePastDueDate(caseData))
            .setHasAgreed(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
            .setIsWithNotice(isWithNoticeApplication(caseData))
            .setReasonsForWithoutNotice(caseData.getGeneralAppInformOtherParty() != null ? caseData.getGeneralAppInformOtherParty()
                .getReasonsForWithoutNotice() : null)
            .setGeneralAppUrgency(Objects.nonNull(caseData.getGeneralAppUrgencyRequirement())
                ? caseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency() : null)
            .setUrgentAppConsiderationDate(Objects.nonNull(caseData.getGeneralAppUrgencyRequirement())
                ? caseData.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate() : null)
            .setReasonsForUrgency(Objects.nonNull(caseData.getGeneralAppUrgencyRequirement())
                ? caseData.getGeneralAppUrgencyRequirement().getReasonsForUrgency() : null)
            .setGeneralAppType(caseData.getGeneralAppType().getTypes().stream()
                .map(GeneralApplicationTypes::getDisplayedValue)
                .collect(Collectors.joining(", ")))
            .setGeneralAppDetailsOfOrder(caseData.getGeneralAppDetailsOfOrder())
            .setGeneralAppReasonsOfOrder(caseData.getGeneralAppReasonsOfOrder())
            .setHearingYesorNo(Objects.nonNull(caseData.getGeneralAppHearingDate())
                ? caseData.getGeneralAppHearingDate().getHearingScheduledPreferenceYesNo() : null)
            .setHearingDate(Objects.nonNull(caseData.getGeneralAppHearingDate())
                ? caseData.getGeneralAppHearingDate().getHearingScheduledDate()
                : null)
            .setHearingPreferencesPreferredType(caseData.getGeneralAppHearingDetails()
                .getHearingPreferencesPreferredType()
                .getDisplayedValue())
            .setReasonForPreferredHearingType(caseData.getGeneralAppHearingDetails()
                .getReasonForPreferredHearingType())
            .setHearingPreferredLocation(getHearingLocation(caseData))
            .setHearingDetailsTelephoneNumber(caseData.getGeneralAppHearingDetails()
                .getHearingDetailsTelephoneNumber())
            .setHearingDetailsEmailId(caseData.getGeneralAppHearingDetails()
                .getHearingDetailsEmailID())
            .setUnavailableTrialRequiredYesOrNo(caseData.getGeneralAppHearingDetails()
                .getUnavailableTrialRequiredYesOrNo())
            .setUnavailableTrialDates(getAppUnavailabilityDates(caseData.getGeneralAppHearingDetails()))
            .setVulnerabilityQuestionsYesOrNo(caseData.getGeneralAppHearingDetails().getVulnerabilityQuestionsYesOrNo())
            .setSupportRequirement(getGaSupportRequirement(caseData))
            .setSupportRequirementSignLanguage(caseData.getGeneralAppHearingDetails().getSupportRequirementSignLanguage())
            .setIsSignLanguageExists(checkAdditionalSupport(caseData, SIGN_INTERPRETER))
            .setSupportRequirementLanguageInterpreter(caseData.getGeneralAppHearingDetails()
                .getSupportRequirementLanguageInterpreter())
            .setIsLanguageInterpreterExists(checkAdditionalSupport(caseData, LANGUAGE_INTERPRETER))
            .setSupportRequirementOther(caseData.getGeneralAppHearingDetails().getSupportRequirementOther())
            .setIsOtherSupportExists(checkAdditionalSupport(caseData, OTHER_SUPPORT))
            .setName(caseData.getGeneralAppStatementOfTruth() != null ? caseData.getGeneralAppStatementOfTruth().getName() : null)
            .setRole(caseData.getGeneralAppStatementOfTruth() != null && caseData.getGeneralAppStatementOfTruth().getRole() != null
                ? caseData.getGeneralAppStatementOfTruth().getRole() : null)
            .setSubmittedDate(getSubmittedDate(civilMainCase, caseData.getGeneralAppSubmittedDateGAspec()))
            .setIssueDate(getPaymentDate(civilMainCase, caseData));

        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() >= ONE_V_ONE) {
            GAHearingDetails gaResp1HearingDetails = caseData.getRespondentsResponses().get(0)
                .getValue().getGaHearingDetails();
            GARespondentResponse resp1Response = caseData.getRespondentsResponses().get(0).getValue();
            gaDraftForm
                .setIsVaryJudgmentApp(checkAppIsVaryJudgment(caseData))
                .setIsConsentOrderApp(checkAppIsConsentOrder(caseData))
                .setIsOneVTwoApp(caseData.getRespondentsResponses().size() >= ONE_V_TWO ? YesOrNo.YES : YesOrNo.NO)
                .setResp1HasAgreed(resp1Response.getGeneralAppRespondent1Representative())
                .setGaResp1Consent(resp1Response.getGeneralAppRespondent1Representative())
                .setResp1DebtorOffer(caseData.getGaRespondentDebtorOffer() != null
                    ? caseData.getGaRespondentDebtorOffer().getRespondentDebtorOffer().getDisplayedValue() : null)
                .setResp1DeclineReason(resp1Response.getGaRespondentResponseReason())
                .setResp1HearingYesOrNo(gaResp1HearingDetails.getHearingYesorNo())
                .setResp1HearingPreferredType(gaResp1HearingDetails
                    .getHearingPreferencesPreferredType().getDisplayedValue())
                .setResp1Hearingdate(gaResp1HearingDetails.getHearingDate())
                .setResp1ReasonForPreferredType(gaResp1HearingDetails.getReasonForPreferredHearingType())
                .setResp1PreferredLocation(getRespHearingLocation(caseData, ONE_V_ONE))
                .setResp1PreferredTelephone(gaResp1HearingDetails.getHearingDetailsTelephoneNumber())
                .setResp1PreferredEmail(gaResp1HearingDetails.getHearingDetailsEmailID())
                .setResp1UnavailableTrialRequired(gaResp1HearingDetails.getUnavailableTrialRequiredYesOrNo())
                .setResp1UnavailableTrialDates(getResp1UnavailabilityDates(caseData))
                .setResp1VulnerableQuestions(gaResp1HearingDetails.getVulnerabilityQuestionsYesOrNo())
                .setResp1SupportRequirement(getRespSupportRequirement(caseData, ONE_V_ONE))
                .setResp1SignLanguage(gaResp1HearingDetails.getSupportRequirementSignLanguage())
                .setIsResp1SignLanguageExists(checkResp1AdditionalSupport(caseData, SIGN_INTERPRETER))
                .setResp1LanguageInterpreter(gaResp1HearingDetails.getSupportRequirementLanguageInterpreter())
                .setIsResp1LanguageInterpreterExists(checkResp1AdditionalSupport(caseData, LANGUAGE_INTERPRETER))
                .setIsResp1OtherSupportExists(checkResp1AdditionalSupport(caseData, OTHER_SUPPORT))
                .setResp1Other(gaResp1HearingDetails.getSupportRequirementOther())
                .setIsLipCase(gaForLipService.isGaForLip(caseData) ? YesOrNo.YES : YesOrNo.NO)
                .setResponseSotName(caseData.getGeneralAppResponseStatementOfTruth() != null
                    ? caseData.getGeneralAppResponseStatementOfTruth().getName() : null)
                .setResponseSotRole(caseData.getGeneralAppResponseStatementOfTruth() != null
                    && caseData.getGeneralAppResponseStatementOfTruth().getRole() != null
                    ? caseData.getGeneralAppResponseStatementOfTruth().getRole() : null);
        }
        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() > ONE_V_ONE) {
            GAHearingDetails gaResp2HearingDetails = caseData.getRespondentsResponses().get(1)
                .getValue().getGaHearingDetails();
            GARespondentResponse resp2Response = caseData.getRespondentsResponses().get(1).getValue();
            gaDraftForm
                .setResp2HasAgreed(resp2Response.getGeneralAppRespondent1Representative())
                .setGaResp2Consent(resp2Response.getGeneralAppRespondent1Representative())
                .setResp2DebtorOffer(caseData.getGaRespondentDebtorOffer() != null
                    ? caseData.getGaRespondentDebtorOffer().getRespondentDebtorOffer().getDisplayedValue() : null)
                .setResp2DeclineReason(resp2Response.getGaRespondentResponseReason())
                .setResp2HearingYesOrNo(gaResp2HearingDetails.getHearingYesorNo())
                .setResp2HearingPreferredType(gaResp2HearingDetails
                    .getHearingPreferencesPreferredType().getDisplayedValue())
                .setResp2Hearingdate(gaResp2HearingDetails.getHearingDate())
                .setResp2ReasonForPreferredType(gaResp2HearingDetails.getReasonForPreferredHearingType())
                .setResp2PreferredLocation(getRespHearingLocation(caseData, ONE_V_TWO))
                .setResp2PreferredTelephone(gaResp2HearingDetails.getHearingDetailsTelephoneNumber())
                .setResp2PreferredEmail(gaResp2HearingDetails.getHearingDetailsEmailID())
                .setResp2UnavailableTrialRequired(gaResp2HearingDetails.getUnavailableTrialRequiredYesOrNo())
                .setResp2UnavailableTrialDates(getResp2UnavailabilityDates(caseData))
                .setResp2VulnerableQuestions(gaResp2HearingDetails.getVulnerabilityQuestionsYesOrNo())
                .setResp2SupportRequirement(getRespSupportRequirement(caseData, ONE_V_TWO))
                .setResp2SignLanguage(gaResp2HearingDetails.getSupportRequirementSignLanguage())
                .setIsResp2SignLanguageExists(checkResp2AdditionalSupport(caseData, SIGN_INTERPRETER))
                .setResp2LanguageInterpreter(gaResp2HearingDetails.getSupportRequirementLanguageInterpreter())
                .setIsResp2LanguageInterpreterExists(checkResp2AdditionalSupport(caseData, LANGUAGE_INTERPRETER))
                .setResp2Other(gaResp2HearingDetails.getSupportRequirementOther())
                .setIsResp2OtherSupportExists(checkResp2AdditionalSupport(caseData, OTHER_SUPPORT));
        }

        return gaDraftForm;
    }

    private LocalDate getPaymentDate(CaseDetails civilMainCase, GeneralApplicationCaseData caseData) {
        GeneralApplicationPbaDetails generalAppPBADetails = caseData.getGeneralAppPBADetails();
        if (generalAppPBADetails != null && generalAppPBADetails.getPaymentSuccessfulDate() != null) {
            return generalAppPBADetails.getPaymentSuccessfulDate().toLocalDate();
        }
        return getSubmittedDate(civilMainCase, caseData.getGeneralAppSubmittedDateGAspec());
    }

    private LocalDate getSubmittedDate(CaseDetails civilMainCase, LocalDateTime generalAppSubmittedDate) {
        if (generalAppSubmittedDate != null) {
            return generalAppSubmittedDate.toLocalDate();
        } else if (nonNull(civilMainCase.getData().get("generalAppSubmittedDateGAspec"))) {
            objectMapper.registerModule(new JavaTimeModule());
            LocalDateTime submittedDate = objectMapper.convertValue(civilMainCase.getData().get("generalAppSubmittedDateGAspec"), new TypeReference<>() {
            });
            return submittedDate.toLocalDate();
        }
        return null;
    }

    private YesOrNo isWithNoticeApplication(GeneralApplicationCaseData caseData) {
        if (Objects.nonNull(caseData.getApplicationIsCloaked())
            && caseData.getApplicationIsCloaked().equals(YesOrNo.NO)) {
            return YesOrNo.YES;
        }

        return caseData.getGeneralAppInformOtherParty().getIsWithNotice();
    }

    private Boolean validateCasePastDueDate(GeneralApplicationCaseData caseData) {
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

    private List<UnavailableDates> getResp1UnavailabilityDates(GeneralApplicationCaseData caseData) {
        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() == ONE_V_ONE) {
            return getAppUnavailabilityDates(caseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails());
        }
        return Collections.emptyList();
    }

    private List<UnavailableDates> getResp2UnavailabilityDates(GeneralApplicationCaseData caseData) {
        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() == ONE_V_TWO) {
            return getAppUnavailabilityDates(caseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails());
        }
        return Collections.emptyList();
    }

    private Boolean checkAdditionalSupport(GeneralApplicationCaseData caseData, SupportRequirements additionalSupport) {
        String appSupportRequirement = getGaSupportRequirement(caseData);
        log.info("Check additional support for caseId: {}", caseData.getCcdCaseReference());
        return appSupportRequirement != null && appSupportRequirement.contains(additionalSupport.getDisplayedValue());
    }

    private Boolean checkResp1AdditionalSupport(GeneralApplicationCaseData caseData, SupportRequirements additionalSupport) {
        String resp1SupportRequirement = getRespSupportRequirement(caseData, ONE_V_ONE);
        log.info("Check additional support for respondent 1 for caseId: {}", caseData.getCcdCaseReference());
        return resp1SupportRequirement != null && resp1SupportRequirement.contains(additionalSupport.getDisplayedValue());
    }

    private Boolean checkResp2AdditionalSupport(GeneralApplicationCaseData caseData, SupportRequirements additionalSupport) {
        String resp2SupportRequirement = getRespSupportRequirement(caseData, ONE_V_TWO);
        log.info("Check additional support for respondent 2 for caseId: {}", caseData.getCcdCaseReference());
        return resp2SupportRequirement != null && resp2SupportRequirement.contains(additionalSupport.getDisplayedValue());

    }

    private YesOrNo checkAppIsConsentOrder(GeneralApplicationCaseData caseData) {
        YesOrNo isConsentOrderApp;
        isConsentOrderApp = caseData.getGeneralAppConsentOrder() != null ? YesOrNo.YES : YesOrNo.NO;
        return isConsentOrderApp;
    }

    private YesOrNo checkAppIsVaryJudgment(GeneralApplicationCaseData caseData) {
        YesOrNo isVaryJudgmentApp;
        isVaryJudgmentApp = caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)
            ? YesOrNo.YES : YesOrNo.NO;
        return isVaryJudgmentApp;
    }

    private String getRespSupportRequirement(GeneralApplicationCaseData caseData, int size) {
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

    private String getGaSupportRequirement(GeneralApplicationCaseData caseData) {
        String gaSupportRequirement = null;
        if (caseData.getGeneralAppHearingDetails() != null
            && caseData.getGeneralAppHearingDetails().getSupportRequirement() != null) {
            gaSupportRequirement = caseData.getGeneralAppHearingDetails().getSupportRequirement().stream().map(
                GAHearingSupportRequirements::getDisplayedValue).collect(Collectors.joining(", "));
        }
        return  gaSupportRequirement;
    }

    private String getHearingLocation(GeneralApplicationCaseData caseData) {
        String preferredLocation = null;
        if (caseData.getGeneralAppHearingDetails() != null
            && caseData.getGeneralAppHearingDetails().getHearingPreferredLocation() != null) {
            preferredLocation = caseData.getGeneralAppHearingDetails().getHearingPreferredLocation()
                .getValue().getLabel();
        }
        return preferredLocation;
    }

    private String getRespHearingLocation(GeneralApplicationCaseData caseData, int size) {
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

    public CaseDocument generate(GeneralApplicationCaseData caseData, String authorisation) {
        try {
            GADraftForm templateData = getTemplateData(caseData);
            log.info("Generate general application draft for caseId: {} and Submitted Date {}", caseData.getCcdCaseReference(), templateData.getSubmittedDate());
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
