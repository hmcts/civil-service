package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGeneratorWithAuth;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.builders.DQGeneratorFormBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.RespondentTemplateForDQGenerator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V1_FAST_TRACK_INT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V2_DS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V2_DS_FAST_TRACK_INT;

@Service
@RequiredArgsConstructor
public class DirectionsQuestionnaireGenerator implements TemplateDataGeneratorWithAuth<DirectionsQuestionnaireForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    protected final FeatureToggleService featureToggleService;
    protected final DQGeneratorFormBuilder dqGeneratorFormBuilder;
    private final RespondentTemplateForDQGenerator respondentTemplateForDQGenerator;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        DocmosisTemplates templateId;
        DocmosisDocument docmosisDocument;
        DirectionsQuestionnaireForm templateData;
        templateId = getTemplateId(caseData);

        templateData = getTemplateData(caseData, authorisation);
        docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, templateId);

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData, templateId), docmosisDocument.getBytes(),
                    DocumentType.DIRECTIONS_QUESTIONNAIRE
            )
        );
    }

    protected DocmosisTemplates getTemplateId(CaseData caseData) {
        boolean isFastTrackOrMinti = featureToggleService.isFastTrackUpliftsEnabled() || featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);
        DocmosisTemplates templateId;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            if (isClaimantResponse(caseData)) {
                templateId = isFastTrackOrMinti
                    ? DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_FAST_TRACK_INT : DocmosisTemplates.CLAIMANT_RESPONSE_SPEC;
            } else {
                templateId = featureToggleService.isFastTrackUpliftsEnabled()
                    ? DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT : DocmosisTemplates.DEFENDANT_RESPONSE_SPEC;
            }
        } else {
            templateId = getDocmosisTemplate(caseData);
        }
        return templateId;
    }

    private DocmosisTemplates getDocmosisTemplate(CaseData caseData) {
        boolean isFastTrackOrMinti = featureToggleService.isFastTrackUpliftsEnabled() || featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);
        DocmosisTemplates templateId = isFastTrackOrMinti ? DQ_RESPONSE_1V1_FAST_TRACK_INT : DQ_RESPONSE_1V1;
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                if (isClaimantResponse(caseData) && isClaimantMultipartyProceed(caseData)) {
                    templateId = isFastTrackOrMinti
                        ? DQ_RESPONSE_1V2_DS_FAST_TRACK_INT : DQ_RESPONSE_1V2_DS;
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                if (!isClaimantResponse(caseData)
                    || (isClaimantResponse(caseData) && isClaimantMultipartyProceed(caseData))) {
                    templateId = isFastTrackOrMinti
                        ? DocmosisTemplates.DQ_RESPONSE_1V2_SS_FAST_TRACK_INT : DocmosisTemplates.DQ_RESPONSE_1V2_SS;
                }
                break;
            case TWO_V_ONE:
                if (!isClaimantResponse(caseData)
                    || (isClaimantResponse(caseData) && isClaimantMultipartyProceed(caseData))) {
                    templateId = isFastTrackOrMinti
                        ? DocmosisTemplates.DQ_RESPONSE_2V1_FAST_TRACK_INT : DocmosisTemplates.DQ_RESPONSE_2V1;
                }
                break;
            default:
        }
        return templateId;
    }

    public CaseDocument generateDQFor1v2SingleSolDiffResponse(CaseData caseData,
                                                              String authorisation,
                                                              String respondent) {
        boolean isFastTrackOrMinti = featureToggleService.isFastTrackUpliftsEnabled() || featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);
        DocmosisTemplates templateId = isFastTrackOrMinti ? DQ_RESPONSE_1V1_FAST_TRACK_INT : DQ_RESPONSE_1V1;
        DirectionsQuestionnaireForm templateData;

        if (respondent.equals("ONE")) {
            templateData = respondentTemplateForDQGenerator.getRespondent1TemplateData(caseData, "ONE", authorisation);
        } else if (respondent.equals("TWO")) {
            templateData = respondentTemplateForDQGenerator.getRespondent2TemplateData(caseData, "TWO", authorisation);
        } else {
            throw new IllegalArgumentException("Respondent argument is expected to be one of ONE or TWO");
        }

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData, templateId);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData, templateId), docmosisDocument.getBytes(),
                    DocumentType.DIRECTIONS_QUESTIONNAIRE
            )
        );
    }

    // return optional, if you get an empty optional, you didn't need to generate the doc
    public Optional<CaseDocument> generateDQFor1v2DiffSol(CaseData caseData,
                                                          String authorisation,
                                                          String respondent) {
        boolean isFastTrackOrMinti = featureToggleService.isFastTrackUpliftsEnabled() || featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);
        DocmosisTemplates templateId = isFastTrackOrMinti ? DQ_RESPONSE_1V1_FAST_TRACK_INT : DQ_RESPONSE_1V1;

        String fileName = getFileName(caseData, templateId);
        LocalDateTime responseDate = getResponseDate(caseData, respondent);

        // Check if the DQ is already generated for this response date and file name
        if (isDQAlreadyGenerated(caseData, responseDate, fileName)) {
            // DQ is already generated, return empty optional
            return Optional.empty();
        }

        DirectionsQuestionnaireForm templateData =
            getDirectionsQuestionnaireForm(caseData, authorisation, respondent);

        // Generate docmosis document and upload it
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData, templateId);
        CaseDocument document = documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData, templateId), docmosisDocument.getBytes(),
                    DocumentType.DIRECTIONS_QUESTIONNAIRE
            )
        );

        // set the create date time equal to the response date time, so we can check it afterwards
        return Optional.of(document.toBuilder().createdDatetime(responseDate).build());
    }

    private DirectionsQuestionnaireForm getDirectionsQuestionnaireForm(CaseData caseData, String authorisation, String respondent) {
        // Generate DQ based on respondent and template data
        DirectionsQuestionnaireForm templateData;
        if (respondent.equals("ONE")) {
            templateData = respondentTemplateForDQGenerator.getRespondent1TemplateData(caseData, "ONE", authorisation);
        } else {
            // TWO
            templateData = respondentTemplateForDQGenerator.getRespondent2TemplateData(caseData, "TWO", authorisation);
        }
        return templateData;
    }

    @NotNull
    private static LocalDateTime getResponseDate(CaseData caseData, String respondent) {
        LocalDateTime responseDate;
        if ("ONE".equals(respondent)) {
            responseDate = caseData.getRespondent1ResponseDate();
        } else if ("TWO".equals(respondent)) {
            responseDate = caseData.getRespondent2ResponseDate();
        } else {
            throw new IllegalArgumentException("Respondent argument is expected to be one of ONE or TWO");
        }
        if (responseDate == null) {
            throw new NullPointerException("Response date should not be null");
        }
        return responseDate;
    }

    // Method to check if DQ is already generated for the given response date and file name
    private boolean isDQAlreadyGenerated(CaseData caseData, LocalDateTime responseDate, String fileName) {
        return caseData.getSystemGeneratedCaseDocuments().stream()
            .anyMatch(element ->
                          Objects.equals(element.getValue().getCreatedDatetime(), responseDate)
                              && fileName.equals(element.getValue().getDocumentName()));
    }

    static final String DEFENDANT = "defendant";

    private String getFileName(CaseData caseData, DocmosisTemplates templateId) {
        boolean isRespondent = dqGeneratorFormBuilder.isRespondentState(caseData);
        String userPrefix = isRespondent ? DEFENDANT : "claimant";
        return String.format(templateId.getDocumentTitle(), userPrefix, caseData.getLegacyCaseReference());
    }

    @Override
    public DirectionsQuestionnaireForm getTemplateData(CaseData caseData, String authorisation) {
        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder = dqGeneratorFormBuilder.getDirectionsQuestionnaireFormBuilder(
            caseData,
            authorisation
        );

        return builder.build();
    }

    public static boolean isClaimantResponse(CaseData caseData) {
        var businessProcess = ofNullable(caseData.getBusinessProcess())
            .map(BusinessProcess::getCamundaEvent)
            .orElse(null);
        return "CLAIMANT_RESPONSE".equals(businessProcess)
                || "CLAIMANT_RESPONSE_SPEC".equals(businessProcess)
            || "CLAIMANT_RESPONSE_CUI".equals(businessProcess);
    }

    private boolean isClaimantMultipartyProceed(CaseData caseData) {
        return (YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                    && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())) // 2v1 scenario
            || (YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()) // 1v2 scenario
                    && YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()));
    }
}
