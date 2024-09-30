package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Experts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureReport;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGeneratorWithAuth;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGeneratorTasks.DQGeneratorFormBuilderTask;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGeneratorTasks.GetRespondentsForDQGeneratorTask;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGeneratorTasks.SetApplicantsForDQGeneratorTask;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V1_FAST_TRACK_INT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V2_DS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V2_DS_FAST_TRACK_INT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.CIVIL_COURT_TYPE_ID;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Service
@Getter
@RequiredArgsConstructor
public class DirectionsQuestionnaireGenerator implements TemplateDataGeneratorWithAuth<DirectionsQuestionnaireForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final IStateFlowEngine stateFlowEngine;
    private final RepresentativeService representativeService;
    private final FeatureToggleService featureToggleService;
    private final LocationReferenceDataService locationRefDataService;
    private final GetRespondentsForDQGeneratorTask respondentsForDQGeneratorTask;
    private final SetApplicantsForDQGeneratorTask setApplicantsForDQGeneratorTask;
    private final DQGeneratorFormBuilderTask dqGeneratorFormBuilderTask;

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
            templateData = getRespondent1TemplateData(caseData, "ONE", authorisation);
        } else if (respondent.equals("TWO")) {
            templateData = getRespondent2TemplateData(caseData, "TWO", authorisation);
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
            templateData = getRespondent1TemplateData(caseData, "ONE", authorisation);
        } else {
            // TWO
            templateData = getRespondent2TemplateData(caseData, "TWO", authorisation);
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
        boolean isRespondent = isRespondentState(caseData);
        String userPrefix = isRespondent ? DEFENDANT : "claimant";
        return String.format(templateId.getDocumentTitle(), userPrefix, caseData.getLegacyCaseReference());
    }

    @Override
    public DirectionsQuestionnaireForm getTemplateData(CaseData caseData, String authorisation) {
        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder = dqGeneratorFormBuilderTask.getDirectionsQuestionnaireFormBuilder(
            caseData,
            authorisation
        );

        return builder.build();
    }

    private String getClaimTrack(CaseData caseData) {
        return UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? caseData.getAllocatedTrack().name() : caseData.getResponseClaimTrack();
    }

    private boolean shouldDisplayDisclosureReport(CaseData caseData) {
        // This is to hide disclosure report from prod
        if (MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);
        } else if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && FAST_CLAIM.equals(caseData.getAllocatedTrack())) {
            return false;
        }
        return true;
    }

    static final String SMALL_CLAIM = "SMALL_CLAIM";
    static final String organisationName = "Organisation name";

    protected RequestedCourt getRequestedCourt(DQ dq, String authorisation) {
        RequestedCourt rc = dq.getRequestedCourt();
        if (rc != null && null !=  rc.getCaseLocation()) {
            List<LocationRefData> courtLocations = (locationRefDataService
                .getCourtLocationsByEpimmsIdAndCourtType(authorisation,
                    rc.getCaseLocation().getBaseLocation()
                ));
            RequestedCourt.RequestedCourtBuilder builder = RequestedCourt.builder()
                .requestHearingAtSpecificCourt(YES)
                .reasonForHearingAtSpecificCourt(rc.getReasonForHearingAtSpecificCourt());
            courtLocations.stream()
                .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
                .findFirst().ifPresent(court -> builder
                    .responseCourtCode(court.getCourtLocationCode())
                    .responseCourtName(court.getCourtName()));
            return builder.build();
        } else {
            return RequestedCourt.builder()
                .requestHearingAtSpecificCourt(NO)
                .build();
        }
    }

    private DisclosureReport getDisclosureReport(DQ dq) {
        DisclosureReport dr = dq.getDisclosureReport();
        if (dr == null) {
            return DisclosureReport.builder().disclosureProposalAgreed(NO)
                .disclosureFormFiledAndServed(NO)
                .build();
        } else {
            return DisclosureReport.builder()
                .disclosureFormFiledAndServed(ofNullable(dr.getDisclosureFormFiledAndServed()).orElse(NO))
                .disclosureProposalAgreed(ofNullable(dr.getDisclosureProposalAgreed()).orElse(NO))
                .draftOrderNumber(dr.getDraftOrderNumber())
                .build();
        }
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


    private DirectionsQuestionnaireForm getRespondent2TemplateData(CaseData caseData, String defendantIdentifier, String authorisation) {
        DQ dq = caseData.getRespondent2DQ();

        return  DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData))
            .submittedOn(caseData.getRespondent2SameLegalRepresentative().equals(YES)
                ? caseData.getRespondent1ResponseDate().toLocalDate()
                             : caseData.getRespondent2ResponseDate().toLocalDate())
            .applicant(setApplicantsForDQGeneratorTask.getApplicant1DQParty(caseData))
            .respondents(respondentsForDQGeneratorTask.getRespondents(caseData, defendantIdentifier))
            .fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .fixedRecoverableCosts(FixedRecoverableCostsSection.from(INTERMEDIATE_CLAIM.toString().equals(getClaimTrack(caseData))
                                                                         ? dq.getFixedRecoverableCostsIntermediate()
                                                                         : dq.getFixedRecoverableCosts()))
            .disclosureOfElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                 ? dq.getDisclosureOfElectronicDocuments() : dq.getSpecDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                    ? dq.getDisclosureOfNonElectronicDocuments() : dq.getSpecDisclosureOfNonElectronicDocuments())
            .disclosureReport(shouldDisplayDisclosureReport(caseData) ? dq.getDisclosureReport() : null)
            .experts(SMALL_CLAIM.equals(caseData.getResponseClaimTrack())
                         ? getSmallClaimExperts(dq, caseData, defendantIdentifier) : getExperts(dq))
            .witnesses(getWitnesses(dq))
            .hearing(getHearing(dq))
            .hearingSupport(getHearingSupport(dq))
            .support(dq.getHearingSupport())
            .furtherInformation(dq.getFurtherInformation())
            .welshLanguageRequirements(getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .vulnerabilityQuestions(dq.getVulnerabilityQuestions())
            .allocatedTrack(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                ? caseData.getAllocatedTrack().name() : caseData.getResponseClaimTrack())
            .requestedCourt(getRequestedCourt(dq, authorisation))
            .build();
    }

    private DirectionsQuestionnaireForm getRespondent1TemplateData(CaseData caseData, String defendantIdentifier, String authorisation) {
        DQ dq = caseData.getRespondent1DQ();

        return DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData))
            .submittedOn(caseData.getRespondent1ResponseDate().toLocalDate())
            .applicant(setApplicantsForDQGeneratorTask.getApplicant1DQParty(caseData))
            .respondents(respondentsForDQGeneratorTask.getRespondents(caseData, defendantIdentifier))
            .fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .fixedRecoverableCosts(FixedRecoverableCostsSection.from(INTERMEDIATE_CLAIM.toString().equals(getClaimTrack(caseData))
                                                                         ? dq.getFixedRecoverableCostsIntermediate()
                                                                         : dq.getFixedRecoverableCosts()))
            .disclosureOfElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                 ? dq.getDisclosureOfElectronicDocuments() : dq.getSpecDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                    ? dq.getDisclosureOfNonElectronicDocuments() : dq.getSpecDisclosureOfNonElectronicDocuments())
            .disclosureReport(shouldDisplayDisclosureReport(caseData) ? dq.getDisclosureReport() : null)
            .experts(SMALL_CLAIM.equals(caseData.getResponseClaimTrack())
                         ? getSmallClaimExperts(dq, caseData, defendantIdentifier) : getExperts(dq))
            .witnesses(getWitnesses(dq))
            .hearing(getHearing(dq))
            .hearingSupport(getHearingSupport(dq))
            .support(dq.getHearingSupport())
            .furtherInformation(dq.getFurtherInformation())
            .welshLanguageRequirements(getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .vulnerabilityQuestions(dq.getVulnerabilityQuestions())
            .allocatedTrack(getClaimTrack(caseData))
            .requestedCourt(getRequestedCourt(dq, authorisation))
            .build();
    }

    private Boolean isRespondentState(CaseData caseData) {
        if (isClaimantResponse(caseData)) {
            return false;
        }
        String state = stateFlowEngine.evaluate(caseData).getState().getName();

        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION
            || state.equals(FULL_DEFENCE.fullName())
            || state.equals(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName())
            || state.equals(ALL_RESPONSES_RECEIVED.fullName())
            || state.equals(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName())
            || state.equals(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName())
            || state.equals(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.fullName());
    }


    private Experts getExperts(DQ dq) {
        var experts = dq.getExperts();
        if (experts == null) {
            return Experts.builder().expertRequired(NO)
                .details(Collections.emptyList())
                .build();
        }
        return Experts.builder()
            .expertRequired(experts.getExpertRequired())
            .expertReportsSent(
                ofNullable(experts.getExpertReportsSent())
                    .map(ExpertReportsSent::getDisplayedValue)
                    .orElse(""))
            .jointExpertSuitable(experts.getJointExpertSuitable())
            .details(getExpertsDetails(dq))
            .build();
    }

    private Experts getSmallClaimExperts(DQ dq, CaseData caseData, String defendantIdentifier) {
        var experts = dq.getSmallClaimExperts();
        YesOrNo expertRequired = defendantIdentifier == null || defendantIdentifier.equals("ONE")
            ? caseData.getResponseClaimExpertSpecRequired()
            : caseData.getResponseClaimExpertSpecRequired2();
        if (isClaimantResponse(caseData)) {
            expertRequired = caseData.getApplicantMPClaimExpertSpecRequired() != null
                ? caseData.getApplicantMPClaimExpertSpecRequired() : caseData.getApplicant1ClaimExpertSpecRequired();
        }
        Expert expertDetails;
        if (experts != null) {
            expertDetails = Expert.builder()
                //ToDo: Remove redundant name mapping when hnl toggle removed
                .name(experts.getExpertName())
                //===========================================================
                .firstName(experts.getFirstName())
                .lastName(experts.getLastName())
                .phoneNumber(experts.getPhoneNumber())
                .emailAddress(experts.getEmailAddress())
                .formattedCost(MonetaryConversions.penniesToPounds(experts.getEstimatedCost()).toString())
                .fieldOfExpertise(experts.getFieldofExpertise())
                .whyRequired(experts.getWhyRequired())
                .build();
        } else {
            expertDetails = Expert.builder().build();
        }

        return Experts.builder()
            .expertRequired(caseData.isRespondent1NotRepresented() ? YesOrNo.NO : expertRequired)
            .expertReportsSent(null)
            .jointExpertSuitable(null)
            .details(caseData.isRespondent1NotRepresented() && dq.getExperts() != null ? getExpertsDetails(dq) : List.of(expertDetails))
            .build();
    }

    private List<Expert> getExpertsDetails(DQ dq) {
        if (dq.getExperts().getDetails() == null) {
            return Collections.emptyList();
        }
        return unwrapElements(dq.getExperts().getDetails())
            .stream()
            .map(expert -> Expert.builder()
                //ToDo: Remove redundant name mapping when hnl toggle removed
                .name(expert.getName())
                //===========================================================
                .firstName(expert.getFirstName())
                .lastName(expert.getLastName())
                .phoneNumber(expert.getPhoneNumber())
                .emailAddress(expert.getEmailAddress())
                .fieldOfExpertise(expert.getFieldOfExpertise())
                .whyRequired(expert.getWhyRequired())
                .formattedCost(NumberFormat.getCurrencyInstance(Locale.UK)
                                   .format(MonetaryConversions.penniesToPounds(expert.getEstimatedCost())))
                .build())
            .collect(toList());
    }

    private Witnesses getWitnesses(DQ dq) {
        var witnesses = dq.getWitnesses();
        if (witnesses == null) {
            return Witnesses.builder().witnessesToAppear(NO)
                .details(Collections.emptyList())
                .build();
        }
        List<Witness> witnessesList = ofNullable(witnesses.getDetails())
            .map(ElementUtils::unwrapElements)
            .orElseGet(Collections::emptyList);
        return Witnesses.builder()
            .witnessesToAppear(witnesses.getWitnessesToAppear())
            .details(witnessesList)
            .build();
    }

    private Hearing getHearing(DQ dq) {
        var hearing = dq.getHearing();
        if (hearing != null) {
            return Hearing.builder()
                .hearingLength(getHearingLength(dq))
                .unavailableDatesRequired(hearing.getUnavailableDatesRequired())
                .unavailableDates(unwrapElements(hearing.getUnavailableDates()))
                .build();
        } else {
            return null;
        }
    }

    private String getHearingLength(DQ dq) {
        var hearing = dq.getHearing();
        if (hearing == null || hearing.getHearingLength() == null) {
            return null;
        }
        switch (hearing.getHearingLength()) {
            case LESS_THAN_DAY:
                return hearing.getHearingLengthHours() + " hours";
            case ONE_DAY:
                return "One day";
            default:
                return hearing.getHearingLengthDays() + " days";
        }
    }

    private String getHearingSupport(DQ dq) {
        var stringBuilder = new StringBuilder();
        ofNullable(dq.getHearingSupport())
            .map(HearingSupport::getRequirements)
            .orElse(List.of())
            .forEach(requirement -> {
                var hearingSupport = dq.getHearingSupport();
                stringBuilder.append(requirement.getDisplayedValue());
                switch (requirement) {
                    case SIGN_INTERPRETER:
                        stringBuilder.append(" - ").append(hearingSupport.getSignLanguageRequired());
                        break;
                    case LANGUAGE_INTERPRETER:
                        stringBuilder.append(" - ").append(hearingSupport.getLanguageToBeInterpreted());
                        break;
                    case OTHER_SUPPORT:
                        stringBuilder.append(" - ").append(hearingSupport.getOtherSupport());
                        break;
                    default:
                        break;
                }
                stringBuilder.append("\n");
            });
        return stringBuilder.toString().trim();
    }

    private WelshLanguageRequirements getWelshLanguageRequirements(DQ dq) {
        var welshLanguageRequirements = dq.getWelshLanguageRequirements();
        if (welshLanguageRequirements == null) {
            return WelshLanguageRequirements.builder()
                .evidence("")
                .court("")
                .documents("")
                .build();
        }
        return WelshLanguageRequirements.builder()
            .evidence(ofNullable(
                welshLanguageRequirements.getEvidence()).map(Language::getDisplayedValue).orElse(""))
            .court(ofNullable(
                welshLanguageRequirements.getCourt()).map(Language::getDisplayedValue).orElse(""))
            .documents(ofNullable(
                welshLanguageRequirements.getDocuments()).map(Language::getDisplayedValue).orElse(""))
            .build();
    }
}
