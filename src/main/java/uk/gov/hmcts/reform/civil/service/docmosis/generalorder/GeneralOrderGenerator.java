package uk.gov.hmcts.reform.civil.service.docmosis.generalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgedecisionpdfdocument.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_GENERAL_ORDER_LIP;

@Service
@RequiredArgsConstructor
public class GeneralOrderGenerator implements TemplateDataGenerator<JudgeDecisionPdfDocument> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final ObjectMapper objectMapper;
    private final DocmosisService docmosisService;
    private final GaCaseDataEnricher gaCaseDataEnricher;

    public CaseDocument generate(CaseData caseData, String authorisation) {

        JudgeDecisionPdfDocument templateData = getTemplateData(null, caseData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
        return generateDocmosisDocument(templateData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
    }

    public CaseDocument generate(CaseData civilCaseData, CaseData caseData, String authorisation, FlowFlag userType) {

        JudgeDecisionPdfDocument templateData = getTemplateData(civilCaseData, caseData, authorisation, userType);
        return  generateDocmosisDocument(templateData, authorisation, userType);
    }

    public CaseDocument generate(GeneralApplicationCaseData gaCaseData, String authorisation) {
        return generate(asCaseData(gaCaseData), authorisation);
    }

    public CaseDocument generate(CaseData civilCaseData, GeneralApplicationCaseData gaCaseData, String authorisation, FlowFlag userType) {
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
                    DocumentType.GENERAL_ORDER)
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return String.format(docmosisTemplate.getDocumentTitle(), LocalDateTime.now().format(formatter));
    }

    @Override
    public JudgeDecisionPdfDocument getTemplateData(CaseData civilCaseData, CaseData caseData, String authorisation, FlowFlag userType) {

        var caseLocation = caseData.getCaseManagementLocation();

        JudgeDecisionPdfDocument.JudgeDecisionPdfDocumentBuilder judgeDecisionPdfDocumentBuilder =
            JudgeDecisionPdfDocument.builder()
                .judgeNameTitle(caseData.getJudgeTitle())
                .isMultiParty(caseData.getIsMultiParty())
                .claimant1Name(caseData.getClaimant1PartyName())
                .claimant2Name(caseData.getClaimant2PartyName() != null ? caseData.getClaimant2PartyName() : null)
                .defendant1Name(caseData.getDefendant1PartyName())
                .defendant2Name(caseData.getDefendant2PartyName() != null ? caseData.getDefendant2PartyName() : null)
                .claimNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
                .courtName(docmosisService.getCaseManagementLocationVenueName(caseData, authorisation).getExternalShortName())
                .siteName(caseLocation != null ? caseLocation.getSiteName() : null)
                .address(caseLocation != null ? caseLocation.getAddress() : null)
                .postcode(caseLocation != null ? caseLocation.getPostcode() : null)
                .judgeRecital(showRecital(caseData) ? caseData.getJudicialDecisionMakeOrder().getJudgeRecitalText() : null)
                .generalOrder(caseData.getJudicialDecisionMakeOrder().getOrderText())
                .submittedOn(LocalDate.now())
                .reasonAvailable(docmosisService.reasonAvailable(caseData))
                .reasonForDecision(docmosisService.populateJudgeReason(caseData))
                .judicialByCourtsInitiative(docmosisService.populateJudicialByCourtsInitiative(caseData));

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

    private DocmosisTemplates getDocmosisTemplate(FlowFlag userType) {

        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            return POST_JUDGE_GENERAL_ORDER_LIP;
        }
        return GENERAL_ORDER;
    }

    public static boolean showRecital(CaseData caseData) {
        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        return Objects.nonNull(judicialDecisionMakeOrder)
                && Objects.nonNull(judicialDecisionMakeOrder.getShowJudgeRecitalText())
                && Objects.nonNull(judicialDecisionMakeOrder.getShowJudgeRecitalText().get(0))
                && judicialDecisionMakeOrder.getShowJudgeRecitalText().get(0).equals(FinalOrderShowToggle.SHOW);
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
