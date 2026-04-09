package uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.FreeFormOrder;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(" d MMMM yyyy");
    private static final String FILE_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocmosisService docmosisService;

    public CaseDocument generate(GeneralApplicationCaseData caseData, String authorisation) {

        FreeFormOrder templateData = getTemplateData(
            null,
            caseData,
            authorisation,
            FlowFlag.ONE_RESPONDENT_REPRESENTATIVE
        );
        log.info(
            "Generate free form order with one respondent representative for caseId: {}",
            caseData.getCcdCaseReference()
        );
        return generateDocmosisDocument(templateData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
    }

    public CaseDocument generate(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String authorisation, FlowFlag userType) {

        FreeFormOrder templateData = getTemplateData(civilCaseData, caseData, authorisation, userType);
        log.info("Generate free form order for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, authorisation, userType);
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
    public FreeFormOrder getTemplateData(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String authorisation, FlowFlag userType) {

        FreeFormOrder freeFormOrder = new FreeFormOrder()
            .setJudgeNameTitle(caseData.getJudgeTitle())
            .setCaseNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
            .setCaseName(caseData.getCaseNameHmctsInternal())
            .setReceivedDate(getDateFormatted(LocalDate.now()))
            .setFreeFormRecitalText(caseData.getFreeFormRecitalText())
            .setFreeFormOrderedText(caseData.getFreeFormOrderedText())
            .setFreeFormOrderValue(getFreeFormOrderValue(caseData))
            .setCourtName(docmosisService.getCaseManagementLocationVenueName(
                caseData,
                authorisation
            ).getExternalShortName())
            .setSiteName(caseData.getCaseManagementLocation().getSiteName())
            .setAddress(caseData.getCaseManagementLocation().getAddress())
            .setPostcode(caseData.getCaseManagementLocation().getPostcode())
            .setIsMultiParty(caseData.getIsMultiParty())
            .setClaimant1Name(caseData.getClaimant1PartyName())
            .setClaimant2Name(caseData.getClaimant2PartyName() != null ? caseData.getClaimant2PartyName() : null)
            .setDefendant1Name(caseData.getDefendant1PartyName())
            .setDefendant2Name(caseData.getDefendant2PartyName() != null ? caseData.getDefendant2PartyName() : null);

        if (List.of(
            FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT,
            FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
        ).contains(userType)) {
            boolean parentClaimantIsApplicant = caseData.identifyParentClaimantIsApplicant(caseData);

            freeFormOrder
                .setPartyName(caseData.getPartyName(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressAddressLine1(caseData.partyAddressAddressLine1(
                    parentClaimantIsApplicant,
                    userType,
                    civilCaseData
                ))
                .setPartyAddressAddressLine2(caseData.partyAddressAddressLine2(
                    parentClaimantIsApplicant,
                    userType,
                    civilCaseData
                ))
                .setPartyAddressAddressLine3(caseData.partyAddressAddressLine3(
                    parentClaimantIsApplicant,
                    userType,
                    civilCaseData
                ))
                .setPartyAddressPostCode(caseData.partyAddressPostCode(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressPostTown(caseData.partyAddressPostTown(parentClaimantIsApplicant, userType, civilCaseData));
        }

        return freeFormOrder;
    }

    protected String getFreeFormOrderValue(GeneralApplicationCaseData caseData) {
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
        return String.format(
            template.getDocumentTitle(),
            LocalDateTime.now().format(formatter)
        );
    }

    protected String getDateFormatted(LocalDate date) {
        if (isNull(date)) {
            return null;
        }
        return DateFormatHelper.formatLocalDate(date, " d MMMM yyyy");
    }

    protected DocmosisTemplates getTemplate(FlowFlag userType) {

        if (List.of(
            FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT,
            FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
        ).contains(userType)) {
            return POST_JUDGE_FREE_FORM_ORDER_LIP;
        }

        return FREE_FORM_ORDER;
    }
}
