package uk.gov.hmcts.reform.civil.service.docmosis.writtenrepresentationsequentialorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgedecisionpdfdocument.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.ListGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_WRITTEN_REPRESENTATION_SEQUENTIAL_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.WRITTEN_REPRESENTATION_SEQUENTIAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService.DATE_FORMATTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class WrittenRepresentationSequentialOrderGenerator implements TemplateDataGenerator<JudgeDecisionPdfDocument> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final ListGeneratorService listGeneratorService;

    private final DocmosisService docmosisService;
    private final GaCaseDataEnricher gaCaseDataEnricher;
    private final ObjectMapper objectMapper;

    public CaseDocument generate(CaseData caseData, String authorisation) {

        JudgeDecisionPdfDocument templateData = getTemplateData(null, caseData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
        log.info("Generate written representation sequential order with one respondent representative for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
    }

    public CaseDocument generate(CaseData civilCaseData, CaseData caseData, String authorisation, FlowFlag userType) {

        JudgeDecisionPdfDocument templateData = getTemplateData(civilCaseData, caseData, authorisation, userType);
        log.info("Generate written representation sequential order for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, authorisation, userType);

    }

    public CaseDocument generate(GeneralApplicationCaseData gaCaseData, String authorisation) {
        return generate(asCaseData(gaCaseData), authorisation);
    }

    public CaseDocument generate(CaseData civilCaseData, GeneralApplicationCaseData gaCaseData,
                                 String authorisation, FlowFlag userType) {
        return generate(civilCaseData, asCaseData(gaCaseData), authorisation, userType);
    }

    public CaseDocument generateDocmosisDocument(JudgeDecisionPdfDocument templateData, String authorisation, FlowFlag userType) {
        DocmosisTemplates docmosisTemplate = getDocmosisTemplate(userType);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
                templateData,
                docmosisTemplate
        );

        return documentManagementService.uploadDocument(
                authorisation,
                new PDF(getFileName(docmosisTemplate), docmosisDocument.getBytes(),
                        DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL)
        );
    }

    public JudgeDecisionPdfDocument getTemplateData(CaseData civilCaseData, GeneralApplicationCaseData gaCaseData,
                                                    String authorisation, FlowFlag userType) {
        return getTemplateData(civilCaseData, asCaseData(gaCaseData), authorisation, userType);
    }

    @Override
    public JudgeDecisionPdfDocument getTemplateData(CaseData civilCaseData, CaseData caseData, String authorisation, FlowFlag userType) {

        String collect = listGeneratorService.applicationType(caseData);

        var caseLocation = caseData.getCaseManagementLocation();

        JudgeDecisionPdfDocument.JudgeDecisionPdfDocumentBuilder judgeDecisionPdfDocumentBuilder =
                JudgeDecisionPdfDocument.builder()
                        .judgeNameTitle(caseData.getJudgeTitle())
                        .claimNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
                        .applicationType(collect)
                        .isMultiParty(caseData.getIsMultiParty())
                        .claimant1Name(caseData.getClaimant1PartyName())
                        .claimant2Name(caseData.getClaimant2PartyName() != null ? caseData.getClaimant2PartyName() : null)
                        .defendant1Name(caseData.getDefendant1PartyName())
                        .defendant2Name(caseData.getDefendant2PartyName() != null ? caseData.getDefendant2PartyName() : null)
                        .judgeRecital(caseData.getJudgeRecitalText())
                        .writtenOrder(caseData.getDirectionInRelationToHearingText())
                        .uploadDeadlineDate(caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                                .getWrittenSequentailRepresentationsBy())
                        .responseDeadlineDate(caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                                .getSequentialApplicantMustRespondWithin())
                        .submittedOn(LocalDate.now())
                        .courtName(docmosisService.getCaseManagementLocationVenueName(caseData, authorisation).getVenueName())
                        .siteName(caseLocation != null ? caseLocation.getSiteName() : null)
                        .address(caseLocation != null ? caseLocation.getAddress() : null)
                        .postcode(caseLocation != null ? caseLocation.getPostcode() : null)
                        .judicialByCourtsInitiativeForWrittenRep(populateJudicialByCourtsInitiative(caseData));

        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            boolean parentClaimantIsApplicant = caseData.identifyParentClaimantIsApplicant(caseData);

            judgeDecisionPdfDocumentBuilder
                    .partyName(caseData.getPartyName(parentClaimantIsApplicant, userType, civilCaseData))
                    .partyAddressAddressLine1(caseData.partyAddressAddressLine1(parentClaimantIsApplicant, userType, civilCaseData))
                    .partyAddressAddressLine2(caseData.partyAddressAddressLine2(parentClaimantIsApplicant, userType, civilCaseData))
                    .partyAddressAddressLine3(caseData.partyAddressAddressLine3(parentClaimantIsApplicant, userType, civilCaseData))
                    .partyAddressPostCode(caseData.partyAddressPostCode(parentClaimantIsApplicant, userType, civilCaseData))
                    .partyAddressPostTown(caseData.partyAddressPostTown(parentClaimantIsApplicant, userType, civilCaseData))
                    .build();
        }

        return judgeDecisionPdfDocumentBuilder.build();
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(docmosisTemplate.getDocumentTitle(), LocalDateTime.now().format(formatter));
    }

    private String populateJudicialByCourtsInitiative(CaseData caseData) {

        if (caseData.getJudicialByCourtsInitiativeForWrittenRep().equals(GAByCourtsInitiativeGAspec
                .OPTION_3)) {
            return StringUtils.EMPTY;
        }

        if (caseData.getJudicialByCourtsInitiativeForWrittenRep()
                .equals(GAByCourtsInitiativeGAspec.OPTION_1)) {
            return caseData.getOrderCourtOwnInitiativeForWrittenRep().getOrderCourtOwnInitiative() + " "
                    .concat(caseData.getOrderCourtOwnInitiativeForWrittenRep().getOrderCourtOwnInitiativeDate()
                            .format(DATE_FORMATTER));
        } else {
            return caseData.getOrderWithoutNoticeForWrittenRep().getOrderWithoutNotice() + " "
                    .concat(caseData.getOrderWithoutNoticeForWrittenRep().getOrderWithoutNoticeDate()
                            .format(DATE_FORMATTER));
        }
    }

    private DocmosisTemplates getDocmosisTemplate(FlowFlag userType) {
        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            return POST_JUDGE_WRITTEN_REPRESENTATION_SEQUENTIAL_LIP;
        }
        return WRITTEN_REPRESENTATION_SEQUENTIAL;
    }

    private CaseData asCaseData(GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData == null) {
            return CaseData.builder().build();
        }
        ObjectMapper mapperWithJavaTime = objectMapper.copy().registerModule(new JavaTimeModule());
        CaseData converted = mapperWithJavaTime.convertValue(gaCaseData, CaseData.class);
        return gaCaseDataEnricher.enrich(converted, gaCaseData);
    }
}
