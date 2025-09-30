package uk.gov.hmcts.reform.civil.service.docmosis.requestmoreinformation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgedecisionpdfdocument.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_REQUEST_FOR_INFORMATION_ORDER_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.REQUEST_FOR_INFORMATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY_BILINGUAL;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestForInformationGenerator implements TemplateDataGenerator<JudgeDecisionPdfDocument> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocmosisService docmosisService;
    private final GaForLipService gaForLipService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        JudgeDecisionPdfDocument templateData = getTemplateData(null, caseData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

        log.info("Generate request for information with one respondent representative for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, caseData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
    }

    public CaseDocument generate(CaseData civilCaseData, CaseData caseData, String authorisation, FlowFlag userType) {
        JudgeDecisionPdfDocument templateData = getTemplateData(civilCaseData, caseData, authorisation, userType);

        log.info("Generate request for information for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, caseData, authorisation, userType);
    }

    public CaseDocument generateDocmosisDocument(JudgeDecisionPdfDocument templateData, CaseData caseData, String authorisation, FlowFlag userType) {

        DocmosisTemplates docmosisTemplate = getDocmosisTemplate(caseData, userType);
        DocumentType documentType = getDocumentType(caseData, userType);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(docmosisTemplate), docmosisDocument.getBytes(),
                    documentType)
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(docmosisTemplate.getDocumentTitle(), LocalDateTime.now().format(formatter));
    }

    @Override
    public JudgeDecisionPdfDocument getTemplateData(CaseData civilCaseData, CaseData caseData, String authorisation, FlowFlag userType) {
        LocationRefData courtLocation  = docmosisService.getCaseManagementLocationVenueName(caseData, authorisation);
        JudgeDecisionPdfDocument.JudgeDecisionPdfDocumentBuilder judgeDecisionPdfDocumentBuilder =
            JudgeDecisionPdfDocument.builder()
                .claimNumber(caseData.getCcdCaseReference().toString())
                .isMultiParty(caseData.getIsMultiParty())
                .claimant1Name(caseData.getClaimant1PartyName())
                .claimant2Name(caseData.getClaimant2PartyName() != null ? caseData.getClaimant2PartyName() : null)
                .defendant1Name(caseData.getDefendant1PartyName())
                .defendant2Name(caseData.getDefendant2PartyName() != null ? caseData.getDefendant2PartyName() : null)
                .courtName(courtLocation.getVenueName())
                .courtNameCy(caseData.isApplicantBilingual() ? (Objects.nonNull(courtLocation.getWelshExternalShortName())
                                 ? courtLocation.getWelshExternalShortName() : courtLocation.getVenueName()) : null)
                .siteName(caseData.getGaCaseManagementLocation().getSiteName())
                .address(caseData.getGaCaseManagementLocation().getAddress())
                .postcode(caseData.getGaCaseManagementLocation().getPostcode())
                .judgeRecital(caseData.getJudicialDecisionRequestMoreInfo().getJudgeRecitalText())
                .judgeComments(caseData.getJudicialDecisionRequestMoreInfo().getJudgeRequestMoreInfoText())
                .submittedOn(LocalDate.now())
                .dateBy(caseData.getJudicialDecisionRequestMoreInfo().getJudgeRequestMoreInfoByDate())
                .additionalApplicationFee(getAdditionalApplicationFee(caseData))
                .applicationCreatedDate(caseData.getCreatedDate().toLocalDate())
                .applicationCreatedDateCy((caseData.isApplicantBilingual()) ? formatDateInWelsh(caseData.getCreatedDate().toLocalDate()) : null);

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

    private DocmosisTemplates getDocmosisTemplate(CaseData caseData, FlowFlag userType) {
        GAJudgeRequestMoreInfoOption gaJudgeRequestMoreInfoOption = getGAJudgeRequestMoreInfoOption(caseData);

        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            return gaJudgeRequestMoreInfoOption == GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY
                ? POST_JUDGE_REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY_LIP
                : POST_JUDGE_REQUEST_FOR_INFORMATION_ORDER_LIP;
        }

        if (gaForLipService.isLipApp(caseData)  && gaJudgeRequestMoreInfoOption == GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY) {
            if (caseData.isApplicantBilingual()) {
                return REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY_BILINGUAL;
            } else {
                return REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY;
            }
        }
        return REQUEST_FOR_INFORMATION;
    }

    private DocumentType getDocumentType(CaseData caseData, FlowFlag userType) {
        GAJudgeRequestMoreInfoOption gaJudgeRequestMoreInfoOption = getGAJudgeRequestMoreInfoOption(caseData);

        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            return gaJudgeRequestMoreInfoOption == GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY
                ? DocumentType.SEND_APP_TO_OTHER_PARTY
                : DocumentType.REQUEST_FOR_INFORMATION;
        }

        if (gaForLipService.isLipApp(caseData) && gaJudgeRequestMoreInfoOption == GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY) {
            return DocumentType.SEND_APP_TO_OTHER_PARTY;
        }

        return DocumentType.REQUEST_FOR_INFORMATION;
    }

    private String getAdditionalApplicationFee(CaseData caseData) {
        return Optional.ofNullable(caseData.getGeneralAppPBADetails()).map(
            GAPbaDetails::getFee).map(Fee::getCalculatedAmountInPence)
            .map(MonetaryConversions::penniesToPounds)
            .map(amount -> amount.setScale(2))
            .map(BigDecimal::toPlainString)
            .map(amount -> amount.replace(".00", ""))
            .map(amount -> "£" + amount)
            .orElse("£" + BigDecimal.ZERO.toPlainString());
    }

    private GAJudgeRequestMoreInfoOption getGAJudgeRequestMoreInfoOption(CaseData caseData) {
        return Optional.ofNullable(caseData.getJudicialDecisionRequestMoreInfo())
            .map(GAJudicialRequestMoreInfo::getRequestMoreInfoOption).orElse(null);
    }
}
