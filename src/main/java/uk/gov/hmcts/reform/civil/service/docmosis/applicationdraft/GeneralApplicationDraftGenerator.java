package uk.gov.hmcts.reform.civil.service.docmosis.applicationdraft;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.GADraftForm;
import uk.gov.hmcts.reform.civil.model.docmosis.UnavailableDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentDebtorOfferGAspec;
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
    private final ObjectMapper objectMapper;
    private static final int ONE_V_ONE = 1;
    private static final int ONE_V_TWO = 2;
    private final GaForLipService gaForLipService;

    @Override
    public GADraftForm getTemplateData(CaseData caseData)  {
        GeneralApplicationCaseData gaCaseData = objectMapper.convertValue(caseData, GeneralApplicationCaseData.class);
        return buildTemplate(gaCaseData);
    }

    private GADraftForm buildTemplate(GeneralApplicationCaseData gaCaseData) {
        CaseDetails civilMainCase = coreCaseDataService
            .getCase(Long.parseLong(gaCaseData.getGeneralAppParentCaseLink().getCaseReference()));
        String claimantName = listGeneratorService.claimantsName(gaCaseData);
        String defendantName = listGeneratorService.defendantsName(gaCaseData);

        GADraftForm.GADraftFormBuilder gaDraftFormBuilder =
            GADraftForm.builder()
                .claimNumber(gaCaseData.getGeneralAppParentCaseLink().getCaseReference())
                .claimantName(claimantName)
                .defendantName(defendantName)
                .claimantReference(getReference(civilMainCase, "applicantSolicitor1Reference"))
                .defendantReference(getReference(civilMainCase, "respondentSolicitor1Reference"))
                .date(LocalDate.now())
                .applicantPartyName(gaCaseData.getApplicantPartyName())
                .isCasePastDueDate(validateCasePastDueDate(gaCaseData))
                .hasAgreed(gaCaseData.getGeneralAppRespondentAgreement().getHasAgreed())
                .isWithNotice(isWithNoticeApplication(gaCaseData))
                .reasonsForWithoutNotice(gaCaseData.getGeneralAppInformOtherParty() != null ? gaCaseData.getGeneralAppInformOtherParty()
                                             .getReasonsForWithoutNotice() : null)
                .generalAppUrgency(Objects.nonNull(gaCaseData.getGeneralAppUrgencyRequirement())
                        ? gaCaseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency() : null)
                .urgentAppConsiderationDate(Objects.nonNull(gaCaseData.getGeneralAppUrgencyRequirement())
                        ? gaCaseData.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate() : null)
                .reasonsForUrgency(Objects.nonNull(gaCaseData.getGeneralAppUrgencyRequirement())
                        ? gaCaseData.getGeneralAppUrgencyRequirement().getReasonsForUrgency() : null)
                .generalAppType(gaCaseData.getGeneralAppType().getTypes().stream()
                                    .map(GeneralApplicationTypes::getDisplayedValue)
                                    .collect(Collectors.joining(", ")))
                .generalAppDetailsOfOrder(gaCaseData.getGeneralAppDetailsOfOrder())
                .generalAppReasonsOfOrder(gaCaseData.getGeneralAppReasonsOfOrder())
                .hearingYesorNo(Objects.nonNull(gaCaseData.getGeneralAppHearingDate())
                                    ? gaCaseData.getGeneralAppHearingDate().getHearingScheduledPreferenceYesNo() : null)
                .hearingDate(Objects.nonNull(gaCaseData.getGeneralAppHearingDate())
                                 ? gaCaseData.getGeneralAppHearingDate().getHearingScheduledDate()
                                 : null)
                .hearingPreferencesPreferredType(gaCaseData.getGeneralAppHearingDetails()
                                                    .getHearingPreferencesPreferredType()
                                                    .getDisplayedValue())
                .reasonForPreferredHearingType(gaCaseData.getGeneralAppHearingDetails()
                                                  .getReasonForPreferredHearingType())
                .hearingPreferredLocation(getHearingLocation(gaCaseData))
                .hearingDetailsTelephoneNumber(gaCaseData.getGeneralAppHearingDetails()
                                                  .getHearingDetailsTelephoneNumber())

                .hearingDetailsEmailId(gaCaseData.getGeneralAppHearingDetails()
                                           .getHearingDetailsEmailID())
                .unavailableTrialRequiredYesOrNo(gaCaseData.getGeneralAppHearingDetails()
                                                     .getUnavailableTrialRequiredYesOrNo())
                .unavailableTrialDates(getAppUnavailabilityDates(gaCaseData.getGeneralAppHearingDetails()))
                .vulnerabilityQuestionsYesOrNo(gaCaseData.getGeneralAppHearingDetails().getVulnerabilityQuestionsYesOrNo())
                .supportRequirement(getGaSupportRequirement(gaCaseData))
                .supportRequirementSignLanguage(gaCaseData.getGeneralAppHearingDetails().getSupportRequirementSignLanguage())
                .isSignLanguageExists(checkAdditionalSupport(gaCaseData, SIGN_INTERPRETER))
                .supportRequirementLanguageInterpreter(gaCaseData.getGeneralAppHearingDetails()
                                                          .getSupportRequirementLanguageInterpreter())
                .isLanguageInterpreterExists(checkAdditionalSupport(gaCaseData, LANGUAGE_INTERPRETER))
                .supportRequirementOther(gaCaseData.getGeneralAppHearingDetails().getSupportRequirementOther())
                .isOtherSupportExists(checkAdditionalSupport(gaCaseData, OTHER_SUPPORT))
                .name(gaCaseData.getGeneralAppStatementOfTruth() != null ? gaCaseData
                    .getGeneralAppStatementOfTruth().getName() : null)
                .role(gaCaseData.getGeneralAppStatementOfTruth() != null && gaCaseData
                    .getGeneralAppStatementOfTruth().getRole() != null ? gaCaseData
                    .getGeneralAppStatementOfTruth().getRole() : null)
                .date(LocalDate.now());

        if (gaCaseData.getRespondentsResponses() != null && gaCaseData.getRespondentsResponses().size() >= ONE_V_ONE) {
            GAHearingDetails gaResp1HearingDetails = gaCaseData.getRespondentsResponses().get(0)
                .getValue().getGaHearingDetails();
            GARespondentResponse resp1Response = gaCaseData.getRespondentsResponses().get(0).getValue();
            gaDraftFormBuilder = gaDraftFormBuilder.build().toBuilder()
                .isVaryJudgmentApp(checkAppIsVaryJudgment(gaCaseData))
                .isConsentOrderApp(checkAppIsConsentOrder(gaCaseData))
                .isOneVTwoApp(gaCaseData.getRespondentsResponses().size() >= ONE_V_TWO ? YesOrNo.YES : YesOrNo.NO)
                .resp1HasAgreed(resp1Response.getGeneralAppRespondent1Representative())
                .gaResp1Consent(resp1Response.getGeneralAppRespondent1Representative())
                .resp1DebtorOffer(getRespondentDebtorOffer(gaCaseData) != null
                                      ? getRespondentDebtorOffer(gaCaseData).getRespondentDebtorOffer()
                                          .getDisplayedValue() : null)
                .resp1DeclineReason(resp1Response.getGaRespondentResponseReason())
                .resp1HearingYesOrNo(gaResp1HearingDetails.getHearingYesorNo())
                .resp1HearingPreferredType(gaResp1HearingDetails
                                               .getHearingPreferencesPreferredType().getDisplayedValue())
                .resp1Hearingdate(gaResp1HearingDetails.getHearingDate())
                .resp1ReasonForPreferredType(gaResp1HearingDetails
                                                .getReasonForPreferredHearingType())
                .resp1PreferredLocation(getRespHearingLocation(gaCaseData, ONE_V_ONE))
                .resp1PreferredTelephone(gaResp1HearingDetails.getHearingDetailsTelephoneNumber())
                .resp1PreferredEmail(gaResp1HearingDetails.getHearingDetailsEmailID())
                .resp1UnavailableTrialRequired(gaResp1HearingDetails.getUnavailableTrialRequiredYesOrNo())
                .resp1UnavailableTrialDates(getResp1UnavailabilityDates(gaCaseData))
                .resp1VulnerableQuestions(gaResp1HearingDetails.getVulnerabilityQuestionsYesOrNo())
                .resp1SupportRequirement(getRespSupportRequirement(gaCaseData, ONE_V_ONE))
                .resp1SignLanguage(gaResp1HearingDetails.getSupportRequirementSignLanguage())
                .isResp1SignLanguageExists(checkResp1AdditionalSupport(gaCaseData, SIGN_INTERPRETER))
                .resp1LanguageInterpreter(gaResp1HearingDetails
                                              .getSupportRequirementLanguageInterpreter())
                .isResp1LanguageInterpreterExists(checkResp1AdditionalSupport(gaCaseData, LANGUAGE_INTERPRETER))
                .isResp1OtherSupportExists(checkResp1AdditionalSupport(gaCaseData, OTHER_SUPPORT))
                .resp1Other(gaResp1HearingDetails.getSupportRequirementOther())
                .isLipCase(gaForLipService.isGaForLip(gaCaseData) ? YesOrNo.YES : YesOrNo.NO)
                .responseSotName(gaCaseData.getGeneralAppResponseStatementOfTruth() != null ? gaCaseData
                    .getGeneralAppResponseStatementOfTruth().getName() : null)
                .responseSotRole(gaCaseData.getGeneralAppResponseStatementOfTruth() != null && gaCaseData
                    .getGeneralAppResponseStatementOfTruth().getRole() != null ? gaCaseData
                    .getGeneralAppResponseStatementOfTruth().getRole() : null);
        }
        if (gaCaseData.getRespondentsResponses() != null && gaCaseData.getRespondentsResponses().size() > ONE_V_ONE) {
            GAHearingDetails gaResp2HearingDetails = gaCaseData.getRespondentsResponses().get(1)
                .getValue().getGaHearingDetails();
            GARespondentResponse resp2Response = gaCaseData.getRespondentsResponses().get(1).getValue();
            gaDraftFormBuilder = gaDraftFormBuilder.build().toBuilder()
                .resp2HasAgreed(resp2Response.getGeneralAppRespondent1Representative())
                .gaResp2Consent(resp2Response.getGeneralAppRespondent1Representative())
                .resp2DebtorOffer(getRespondentDebtorOffer(gaCaseData) != null
                                      ? getRespondentDebtorOffer(gaCaseData)
                                    .getRespondentDebtorOffer().getDisplayedValue() : null)
                .resp2DeclineReason(resp2Response.getGaRespondentResponseReason())
                .resp2HearingYesOrNo(gaResp2HearingDetails.getHearingYesorNo())
                .resp2HearingPreferredType(gaResp2HearingDetails
                                               .getHearingPreferencesPreferredType().getDisplayedValue())
                .resp2Hearingdate(gaResp2HearingDetails.getHearingDate())
                .resp2ReasonForPreferredType(gaResp2HearingDetails
                                                .getReasonForPreferredHearingType())
                .resp2PreferredLocation(getRespHearingLocation(gaCaseData, ONE_V_TWO))
                .resp2PreferredTelephone(gaResp2HearingDetails.getHearingDetailsTelephoneNumber())
                .resp2PreferredEmail(gaResp2HearingDetails.getHearingDetailsEmailID())
                .resp2UnavailableTrialRequired(gaResp2HearingDetails.getUnavailableTrialRequiredYesOrNo())
                .resp2UnavailableTrialDates(getResp2UnavailabilityDates(gaCaseData))
                .resp2VulnerableQuestions(gaResp2HearingDetails.getVulnerabilityQuestionsYesOrNo())
                .resp2SupportRequirement(getRespSupportRequirement(gaCaseData, ONE_V_TWO))
                .resp2SignLanguage(gaResp2HearingDetails.getSupportRequirementSignLanguage())
                .isResp2SignLanguageExists(checkResp2AdditionalSupport(gaCaseData, SIGN_INTERPRETER))
                .resp2LanguageInterpreter(gaResp2HearingDetails
                                              .getSupportRequirementLanguageInterpreter())
                .isResp2LanguageInterpreterExists(checkResp2AdditionalSupport(gaCaseData, LANGUAGE_INTERPRETER))
                .resp2Other(gaResp2HearingDetails.getSupportRequirementOther())
                .isResp2OtherSupportExists(checkResp2AdditionalSupport(gaCaseData, OTHER_SUPPORT));
        }

        return gaDraftFormBuilder.build();
    }

    private YesOrNo isWithNoticeApplication(GeneralApplicationCaseData gaCaseData) {
        if (Objects.nonNull(gaCaseData.getApplicationIsCloaked())
            && gaCaseData.getApplicationIsCloaked().equals(YesOrNo.NO)) {
            return YesOrNo.YES;
        }

        return gaCaseData.getGeneralAppInformOtherParty().getIsWithNotice();
    }

    private Boolean validateCasePastDueDate(GeneralApplicationCaseData gaCaseData) {
        return gaCaseData.getRespondentsResponses() == null || gaCaseData.getRespondentsResponses().isEmpty();
    }

    private List<UnavailableDates> getAppUnavailabilityDates(GAHearingDetails hearingDetails) {
        return
            Optional.ofNullable(hearingDetails).map(GAHearingDetails::getGeneralAppUnavailableDates)
                .orElse(Collections.emptyList()).stream()
                .map(element -> element.getValue())
                .map(value -> new UnavailableDates(value.getUnavailableTrialDateFrom(), value.getUnavailableTrialDateTo()))
                .toList();
    }

    private List<UnavailableDates> getResp1UnavailabilityDates(GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData.getRespondentsResponses() != null && gaCaseData.getRespondentsResponses().size() == ONE_V_ONE) {
            return getAppUnavailabilityDates(gaCaseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails());
        }
        return Collections.emptyList();
    }

    private List<UnavailableDates> getResp2UnavailabilityDates(GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData.getRespondentsResponses() != null && gaCaseData.getRespondentsResponses().size() == ONE_V_TWO) {
            return getAppUnavailabilityDates(gaCaseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails());
        }
        return Collections.emptyList();
    }

    private Boolean checkAdditionalSupport(GeneralApplicationCaseData gaCaseData, SupportRequirements additionalSupport) {
        String appSupportRequirement = getGaSupportRequirement(gaCaseData);
        log.info("Check additional support for caseId: {}", gaCaseData.getCcdCaseReference());
        return appSupportRequirement != null && appSupportRequirement.contains(additionalSupport.getDisplayedValue());
    }

    private Boolean checkResp1AdditionalSupport(GeneralApplicationCaseData gaCaseData, SupportRequirements additionalSupport) {
        String resp1SupportRequirement = getRespSupportRequirement(gaCaseData, ONE_V_ONE);
        log.info("Check additional support for respondent 1 for caseId: {}", gaCaseData.getCcdCaseReference());
        return resp1SupportRequirement != null && resp1SupportRequirement.contains(additionalSupport.getDisplayedValue());
    }

    private Boolean checkResp2AdditionalSupport(GeneralApplicationCaseData gaCaseData, SupportRequirements additionalSupport) {
        String resp2SupportRequirement = getRespSupportRequirement(gaCaseData, ONE_V_TWO);
        log.info("Check additional support for respondent 2 for caseId: {}", gaCaseData.getCcdCaseReference());
        return resp2SupportRequirement != null && resp2SupportRequirement.contains(additionalSupport.getDisplayedValue());

    }

    private YesOrNo checkAppIsConsentOrder(GeneralApplicationCaseData gaCaseData) {
        YesOrNo isConsentOrderApp;
        isConsentOrderApp = gaCaseData.getGeneralAppConsentOrder() != null ? YesOrNo.YES : YesOrNo.NO;
        return isConsentOrderApp;
    }

    private YesOrNo checkAppIsVaryJudgment(GeneralApplicationCaseData gaCaseData) {
        YesOrNo isVaryJudgmentApp;
        isVaryJudgmentApp = gaCaseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)
            ? YesOrNo.YES : YesOrNo.NO;
        return isVaryJudgmentApp;
    }

    private String getRespSupportRequirement(GeneralApplicationCaseData gaCaseData, int size) {
        String gaRespSupportRequirement = null;
        if (size == ONE_V_ONE  && gaCaseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails() != null
            && gaCaseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails()
            .getSupportRequirement() != null) {
            gaRespSupportRequirement = gaCaseData.getRespondentsResponses().get(0).getValue()
                .getGaHearingDetails().getSupportRequirement().stream().map(
                GAHearingSupportRequirements::getDisplayedValue).collect(Collectors.joining(", "));
        } else if (size == ONE_V_TWO  && gaCaseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails() != null
                && gaCaseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails().getSupportRequirement() != null) {
            gaRespSupportRequirement = gaCaseData.getRespondentsResponses().get(1).getValue()
                .getGaHearingDetails().getSupportRequirement().stream().map(
                    GAHearingSupportRequirements::getDisplayedValue).collect(Collectors.joining(", "));
        }

        return gaRespSupportRequirement;
    }

    private String getGaSupportRequirement(GeneralApplicationCaseData gaCaseData) {
        String gaSupportRequirement = null;
        if (gaCaseData.getGeneralAppHearingDetails() != null
            && gaCaseData.getGeneralAppHearingDetails().getSupportRequirement() != null) {
            gaSupportRequirement = gaCaseData.getGeneralAppHearingDetails().getSupportRequirement().stream().map(
                GAHearingSupportRequirements::getDisplayedValue).collect(Collectors.joining(", "));
        }
        return  gaSupportRequirement;
    }

    private String getHearingLocation(GeneralApplicationCaseData gaCaseData) {
        String preferredLocation = null;
        if (gaCaseData.getGeneralAppHearingDetails() != null
            && gaCaseData.getGeneralAppHearingDetails().getHearingPreferredLocation() != null) {
            preferredLocation = gaCaseData.getGeneralAppHearingDetails().getHearingPreferredLocation()
                .getValue().getLabel();
        }
        return preferredLocation;
    }

    private String getRespHearingLocation(GeneralApplicationCaseData gaCaseData, int size) {
        String preferredLocation = null;
        GAHearingDetails resp1HearingDetails = gaCaseData.getRespondentsResponses()
            .get(0).getValue().getGaHearingDetails();

        if (resp1HearingDetails != null
            && resp1HearingDetails.getHearingPreferredLocation() != null
            && resp1HearingDetails.getHearingPreferredLocation().getValue() != null
            && size == ONE_V_ONE) {
            preferredLocation = resp1HearingDetails.getHearingPreferredLocation()
                .getValue().getLabel();
        } else if (size == ONE_V_TWO
            && gaCaseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails()
            .getHearingPreferredLocation() != null
            && gaCaseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails()
            .getHearingPreferredLocation().getValue() != null) {
            preferredLocation = gaCaseData.getRespondentsResponses().get(1).getValue()
                .getGaHearingDetails().getHearingPreferredLocation()
                .getValue().getLabel();
        }
        return preferredLocation;
    }

    private GARespondentDebtorOfferGAspec getRespondentDebtorOffer(GeneralApplicationCaseData gaCaseData) {
        return gaCaseData.getGaRespondentDebtorOffer();
    }

    @SuppressWarnings("unchecked")
    protected String getReference(CaseDetails caseData, String refKey) {
        if (nonNull(caseData.getData().get("solicitorReferences"))) {
            return ((Map<String, String>) caseData.getData().get("solicitorReferences")).get(refKey);
        }
        return null;
    }

    public CaseDocument generate(GeneralApplicationCaseData gaCaseData, String authorisation) {
        try {
            GADraftForm templateData = buildTemplate(gaCaseData);
            DocmosisTemplates docmosisTemplate = getDocmosisTemplate();

            DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
                templateData,
                docmosisTemplate
            );
            log.info("Generate general application draft for caseId: {}", gaCaseData.getCcdCaseReference());

            return documentManagementService.uploadDocument(
                authorisation,
                new PDF(getFileName(docmosisTemplate), docmosisDocument.getBytes(),
                        DocumentType.GENERAL_APPLICATION_DRAFT
                )
            );
        } catch (Exception e) {
            // Catch all other exceptions
            log.error("Error generating general application draft for caseId: {}", gaCaseData.getCcdCaseReference(), e);
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
