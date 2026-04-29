package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
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

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V1_FAST_TRACK_INT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V2_DS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V2_DS_FAST_TRACK_INT;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionsQuestionnaireGenerator implements TemplateDataGeneratorWithAuth<DirectionsQuestionnaireForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    protected final FeatureToggleService featureToggleService;
    protected final DQGeneratorFormBuilder dqGeneratorFormBuilder;
    private final RespondentTemplateForDQGenerator respondentTemplateForDQGenerator;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        log.info("Starting generation of directions questionnaire for caseId {}", caseData.getCcdCaseReference());

        DocmosisTemplates templateId;
        DocmosisDocument docmosisDocument;
        DirectionsQuestionnaireForm templateData;
        templateId = getTemplateId(caseData);

        templateData = getTemplateData(caseData, authorisation);
        docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, templateId);

        CaseDocument uploadedDocument = documentManagementService.uploadDocument(
                authorisation,
                new PDF(getFileName(caseData, templateId), docmosisDocument.getBytes(),
                        DocumentType.DIRECTIONS_QUESTIONNAIRE
                ));

        log.info("Completed upload of directions questionnaire for caseId {}", caseData.getCcdCaseReference());
        return uploadedDocument;
    }

    protected DocmosisTemplates getTemplateId(CaseData caseData) {
        DocmosisTemplates templateId = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? getSpecTemplate(caseData)
            : getUnspecTemplate(caseData);
        log.info("{} {}", caseData.getCcdCaseReference(), templateId);
        return templateId;
    }

    private DocmosisTemplates getSpecTemplate(CaseData caseData) {
        if (!isClaimantResponse(caseData)) {
            return DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT;
        }
        return isMintiEnabled(caseData)
            ? DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_FAST_TRACK_INT
            : DocmosisTemplates.CLAIMANT_RESPONSE_SPEC;
    }

    private DocmosisTemplates getUnspecTemplate(CaseData caseData) {
        boolean isMinti = isMintiEnabled(caseData);
        boolean isClaimantResponse = isClaimantResponse(caseData);
        boolean claimantMultipartyProceed = isClaimantMultipartyProceed(caseData);

        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP -> shouldUseClaimantProceedTemplate(isClaimantResponse, claimantMultipartyProceed)
                    ? getOneVTwoTwoLegalRepTemplate(isMinti)
                    : getDefaultTemplate(isMinti);
            case ONE_V_TWO_ONE_LEGAL_REP -> shouldUseRespondentOrClaimantProceedTemplate(isClaimantResponse, claimantMultipartyProceed)
                    ? getOneVTwoOneLegalRepTemplate(isMinti)
                    : getDefaultTemplate(isMinti);
            case TWO_V_ONE -> shouldUseRespondentOrClaimantProceedTemplate(isClaimantResponse, claimantMultipartyProceed)
                    ? getTwoVOneTemplate(isMinti)
                    : getDefaultTemplate(isMinti);
            default -> getDefaultTemplate(isMinti);
        };
    }

    public CaseDocument generateDQFor1v2SingleSolDiffResponse(CaseData caseData,
                                                              String authorisation,
                                                              String respondent) {
        log.info("Starting 1v2 single-solicitor different-response DQ for caseId {} respondent {}", caseData.getCcdCaseReference(), respondent);

        DocmosisTemplates templateId = getSingleSolicitorDifferentResponseTemplate(caseData);
        DirectionsQuestionnaireForm templateData = getRespondentTemplateData(caseData, authorisation, respondent);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData, templateId);

        log.info("Completed upload of 1v2 single-solicitor DQ for caseId {} respondent {}", caseData.getCcdCaseReference(), respondent);

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
        DocmosisTemplates templateId = getSingleSolicitorDifferentResponseTemplate(caseData);

        log.info("Starting 1v2 different-solicitor DQ check for caseId {} respondent {}", caseData.getCcdCaseReference(), respondent);
        String fileName = getFileName(caseData, templateId);
        LocalDateTime responseDate = getResponseDate(caseData, respondent);

        // Check if the DQ is already generated for this response date and file name
        if (isDQAlreadyGenerated(caseData, responseDate, fileName)) {
            log.info("DQ already exists for caseId {} respondent {} responseDate {}", caseData.getCcdCaseReference(), respondent, responseDate);
            return Optional.empty();
        }

        DirectionsQuestionnaireForm templateData = getRespondentTemplateData(caseData, authorisation, respondent);

        // Generate docmosis document and upload it
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData, templateId);
        CaseDocument document = documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData, templateId), docmosisDocument.getBytes(),
                    DocumentType.DIRECTIONS_QUESTIONNAIRE
            )
        );

        log.info("Uploaded new 1v2 different-solicitor DQ for caseId {} respondent {} responseDate {}", caseData.getCcdCaseReference(), respondent, responseDate);
        document.setCreatedDatetime(responseDate);
        return Optional.of(document);
    }

    private DirectionsQuestionnaireForm getRespondentTemplateData(CaseData caseData, String authorisation, String respondent) {
        if ("ONE".equals(respondent)) {
            return respondentTemplateForDQGenerator.getRespondent1TemplateData(caseData, "ONE", authorisation);
        }
        if ("TWO".equals(respondent)) {
            return respondentTemplateForDQGenerator.getRespondent2TemplateData(caseData, "TWO", authorisation);
        }
        throw new IllegalArgumentException("Respondent argument is expected to be one of ONE or TWO");
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

    private boolean isMintiEnabled(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);
    }

    private DocmosisTemplates getDefaultTemplate(boolean isMinti) {
        return isMinti ? DQ_RESPONSE_1V1_FAST_TRACK_INT : DQ_RESPONSE_1V1;
    }

    private DocmosisTemplates getOneVTwoTwoLegalRepTemplate(boolean isMinti) {
        return isMinti ? DQ_RESPONSE_1V2_DS_FAST_TRACK_INT : DQ_RESPONSE_1V2_DS;
    }

    private DocmosisTemplates getOneVTwoOneLegalRepTemplate(boolean isMinti) {
        return isMinti ? DocmosisTemplates.DQ_RESPONSE_1V2_SS_FAST_TRACK_INT : DocmosisTemplates.DQ_RESPONSE_1V2_SS;
    }

    private DocmosisTemplates getTwoVOneTemplate(boolean isMinti) {
        return isMinti ? DocmosisTemplates.DQ_RESPONSE_2V1_FAST_TRACK_INT : DocmosisTemplates.DQ_RESPONSE_2V1;
    }

    private boolean shouldUseClaimantProceedTemplate(boolean isClaimantResponse, boolean claimantMultipartyProceed) {
        return isClaimantResponse && claimantMultipartyProceed;
    }

    private boolean shouldUseRespondentOrClaimantProceedTemplate(boolean isClaimantResponse, boolean claimantMultipartyProceed) {
        return !isClaimantResponse || claimantMultipartyProceed;
    }

    private DocmosisTemplates getSingleSolicitorDifferentResponseTemplate(CaseData caseData) {
        return getDefaultTemplate(isMintiEnabled(caseData));
    }

    private String getFileName(CaseData caseData, DocmosisTemplates templateId) {
        boolean isRespondent = dqGeneratorFormBuilder.isRespondentState(caseData);
        String userPrefix = isRespondent ? DEFENDANT : "claimant";
        return String.format(templateId.getDocumentTitle(), userPrefix, caseData.getLegacyCaseReference());
    }

    @Override
    public DirectionsQuestionnaireForm getTemplateData(CaseData caseData, String authorisation) {
        return dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(
            caseData,
            authorisation
        );
    }

    public static boolean isClaimantResponse(CaseData caseData) {
        return DQGeneratorFormBuilder.isClaimantResponse(caseData);
    }

    private boolean isClaimantMultipartyProceed(CaseData caseData) {
        return (YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                    && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())) // 2v1 scenario
            || (YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()) // 1v2 scenario
                    && YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()));
    }
}
