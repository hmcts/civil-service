package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.ga.service.JudicialTimeEstimateHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_HEARING_ORDER_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService.DATE_FORMATTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingOrderGenerator implements TemplateDataGenerator<JudgeDecisionPdfDocument> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocmosisService docmosisService;
    private final JudicialTimeEstimateHelper timeEstimateHelper;

    public CaseDocument generate(GeneralApplicationCaseData caseData, String authorisation) {

        JudgeDecisionPdfDocument templateData = getTemplateData(null, caseData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
        log.info("Generate hearing order form with one respondent representative for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
    }

    public CaseDocument generate(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String authorisation, FlowFlag userType) {

        JudgeDecisionPdfDocument templateData = getTemplateData(civilCaseData, caseData, authorisation, userType);
        log.info("Generate hearing order form for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, authorisation, userType);
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
                    DocumentType.HEARING_ORDER)
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return String.format(docmosisTemplate.getDocumentTitle(), LocalDateTime.now().format(formatter));
    }

    @Override
    public JudgeDecisionPdfDocument getTemplateData(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String authorisation, FlowFlag userType) {

        JudgeDecisionPdfDocument judgeDecisionPdfDocument = new JudgeDecisionPdfDocument()
            .setJudgeNameTitle(caseData.getJudgeTitle())
            .setClaimNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
            .setIsMultiParty(caseData.getIsMultiParty())
            .setClaimant1Name(caseData.getClaimant1PartyName())
            .setClaimant2Name(caseData.getClaimant2PartyName() != null ? caseData.getClaimant2PartyName() : null)
            .setDefendant1Name(caseData.getDefendant1PartyName())
            .setDefendant2Name(caseData.getDefendant2PartyName() != null ? caseData.getDefendant2PartyName() : null)
            .setJudgeRecital(caseData.getJudicialGeneralHearingOrderRecital())
            .setHearingOrder(caseData.getJudicialGOHearingDirections())
            .setHearingPrefType(caseData.getJudicialListForHearing()
                                    .getHearingPreferencesPreferredType().getDisplayedValue())
            .setEstimatedHearingLength(timeEstimateHelper.getEstimatedHearingLength(caseData))
            .setSubmittedOn(LocalDate.now())
            .setCourtName(docmosisService.getCaseManagementLocationVenueName(caseData, authorisation).getExternalShortName())
            .setJudgeHearingLocation(caseData.getJudicialListForHearing()
                                         .getHearingPreferencesPreferredType() == GAJudicialHearingType.IN_PERSON
                                         ? caseData.getJudicialListForHearing()
                .getHearingPreferredLocation().getValue().getLabel() : null)
            .setSiteName(caseData.getCaseManagementLocation().getSiteName())
            .setAddress(caseData.getCaseManagementLocation().getAddress())
            .setPostcode(caseData.getCaseManagementLocation().getPostcode())
            .setJudicialByCourtsInitiativeListForHearing(populateJudicialByCourtsInitiative(caseData));

        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            boolean parentClaimantIsApplicant = caseData.identifyParentClaimantIsApplicant(caseData);

            judgeDecisionPdfDocument
                .setPartyName(caseData.getPartyName(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressAddressLine1(caseData.partyAddressAddressLine1(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressAddressLine2(caseData.partyAddressAddressLine2(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressAddressLine3(caseData.partyAddressAddressLine3(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressPostCode(caseData.partyAddressPostCode(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressPostTown(caseData.partyAddressPostTown(parentClaimantIsApplicant, userType, civilCaseData));
        }

        return judgeDecisionPdfDocument;
    }

    private String populateJudicialByCourtsInitiative(GeneralApplicationCaseData caseData) {

        if (caseData.getJudicialByCourtsInitiativeListForHearing().equals(GAByCourtsInitiativeGAspec
                                                                                               .OPTION_3)) {
            return StringUtils.EMPTY;
        }

        if (caseData.getJudicialByCourtsInitiativeListForHearing()
            .equals(GAByCourtsInitiativeGAspec.OPTION_1)) {
            return caseData.getOrderCourtOwnInitiativeListForHearing().getOrderCourtOwnInitiative() + " "
                .concat(caseData.getOrderCourtOwnInitiativeListForHearing().getOrderCourtOwnInitiativeDate()
                            .format(DATE_FORMATTER));
        } else {
            return caseData.getOrderWithoutNoticeListForHearing().getOrderWithoutNotice() + " "
                .concat(caseData.getOrderWithoutNoticeListForHearing().getOrderWithoutNoticeDate()
                            .format(DATE_FORMATTER));
        }
    }

    private DocmosisTemplates getDocmosisTemplate(FlowFlag userType) {
        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            return POST_JUDGE_HEARING_ORDER_LIP;
        }
        return HEARING_ORDER;
    }
}
