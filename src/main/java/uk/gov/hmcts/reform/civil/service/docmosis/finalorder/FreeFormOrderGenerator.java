package uk.gov.hmcts.reform.civil.service.docmosis.finalorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.FreeFormOrder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList.ORDER_ON_COURT_INITIATIVE;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList.ORDER_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_FREE_FORM_ORDER_LIP;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreeFormOrderGenerator implements TemplateDataGenerator<FreeFormOrder> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocmosisService docmosisService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(" d MMMM yyyy");
    private static final String FILE_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public CaseDocument generate(CaseData caseData, String authorisation) {

        FreeFormOrder templateData = getTemplateData(null, caseData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
        log.info("Generate free form order with one respondent representative for caseId: {}", caseData.getCcdCaseReference());
        return  generateDocmosisDocument(templateData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
    }

    public CaseDocument generate(CaseData civilCaseData, CaseData caseData, String authorisation, FlowFlag userType) {

        FreeFormOrder templateData = getTemplateData(civilCaseData, caseData, authorisation, userType);
        log.info("Generate free form order for caseId: {}", caseData.getCcdCaseReference());
        return  generateDocmosisDocument(templateData, authorisation, userType);
    }

    public CaseDocument generateDocmosisDocument(FreeFormOrder templateData, String authorisation, FlowFlag userType) {

        DocmosisTemplates template = getTemplate(userType);
        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData, template);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(template),
                document.getBytes(),
                DocumentType.GENERAL_ORDER
            )
        );
    }

    @Override
    public FreeFormOrder getTemplateData(CaseData civilCaseData, CaseData caseData, String authorisation, FlowFlag userType) {

        FreeFormOrder.FreeFormOrderBuilder freeFormOrderBuilder = FreeFormOrder.builder()
            .judgeNameTitle(caseData.getJudgeTitle())
            .caseNumber(caseData.getCcdCaseReference().toString())
            .caseName(caseData.getCaseNameHmctsInternal())
            .receivedDate(getDateFormatted(LocalDate.now()))
            .freeFormRecitalText(caseData.getFreeFormRecitalText())
            .freeFormOrderedText(caseData.getFreeFormOrderedText())
            .freeFormOrderValue(getFreeFormOrderValue(caseData))
            .courtName(docmosisService.getCaseManagementLocationVenueName(caseData, authorisation).getExternalShortName())
            .siteName(caseData.getGaCaseManagementLocation().getSiteName())
            .address(caseData.getGaCaseManagementLocation().getAddress())
            .postcode(caseData.getGaCaseManagementLocation().getPostcode())
            .isMultiParty(caseData.getIsMultiParty())
            .claimant1Name(caseData.getClaimant1PartyName())
            .claimant2Name(caseData.getClaimant2PartyName() != null ? caseData.getClaimant2PartyName() : null)
            .defendant1Name(caseData.getDefendant1PartyName())
            .defendant2Name(caseData.getDefendant2PartyName() != null ? caseData.getDefendant2PartyName() : null);

        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            boolean parentClaimantIsApplicant = caseData.identifyParentClaimantIsApplicant(caseData);

            freeFormOrderBuilder
                .partyName(caseData.getPartyName(parentClaimantIsApplicant, userType, civilCaseData))
                .partyAddressAddressLine1(caseData.partyAddressAddressLine1(parentClaimantIsApplicant, userType, civilCaseData))
                .partyAddressAddressLine2(caseData.partyAddressAddressLine2(parentClaimantIsApplicant, userType, civilCaseData))
                .partyAddressAddressLine3(caseData.partyAddressAddressLine3(parentClaimantIsApplicant, userType, civilCaseData))
                .partyAddressPostCode(caseData.partyAddressPostCode(parentClaimantIsApplicant, userType, civilCaseData))
                .partyAddressPostTown(caseData.partyAddressPostTown(parentClaimantIsApplicant, userType, civilCaseData))
                .build();
        }

        return freeFormOrderBuilder.build();
    }

    protected String getFreeFormOrderValue(CaseData caseData) {
        StringBuilder orderValueBuilder = new StringBuilder();
        if (caseData.getOrderOnCourtsList().equals(ORDER_ON_COURT_INITIATIVE)) {
            orderValueBuilder.append(caseData
                    .getOrderOnCourtInitiative().getOnInitiativeSelectionTextArea());
            orderValueBuilder.append(DATE_FORMATTER.format(caseData
                    .getOrderOnCourtInitiative().getOnInitiativeSelectionDate()));
            orderValueBuilder.append(".");
        } else if (caseData.getOrderOnCourtsList().equals(ORDER_WITHOUT_NOTICE)) {
            orderValueBuilder.append(caseData
                    .getOrderWithoutNotice().getWithoutNoticeSelectionTextArea());
            orderValueBuilder.append(DATE_FORMATTER.format(caseData
                    .getOrderWithoutNotice().getWithoutNoticeSelectionDate()));
            orderValueBuilder.append(".");
        }
        return orderValueBuilder.toString();
    }

    protected String getFileName(DocmosisTemplates template) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FILE_TIMESTAMP_FORMAT);
        return String.format(template.getDocumentTitle(),
                LocalDateTime.now().format(formatter));
    }

    protected String getDateFormatted(LocalDate date) {
        if (isNull(date)) {
            return null;
        }
        return DateFormatHelper.formatLocalDate(date, " d MMMM yyyy");
    }

    protected DocmosisTemplates getTemplate(FlowFlag userType) {

        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            return POST_JUDGE_FREE_FORM_ORDER_LIP;
        }

        return FREE_FORM_ORDER;
    }
}
