package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.finalorders.*;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealGrantedRefused;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import java.time.LocalDate;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList.DEFENDANT_NOT_ATTENDING;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.ASSISTED_ORDER_PDF;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FREE_FORM_ORDER_PDF;

@Service
@RequiredArgsConstructor
public class JudgeFinalOrderGenerator implements TemplateDataGenerator<JudgeFinalOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        JudgeFinalOrderForm templateData = getFinalOrderType(caseData);
        DocmosisTemplates docmosisTemplate = null;
        if (caseData.getFinalOrderSelection().equals(FREE_FORM_ORDER)) {
            docmosisTemplate = FREE_FORM_ORDER_PDF;
        } else {
            docmosisTemplate = ASSISTED_ORDER_PDF;
        }
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(docmosisTemplate),
                docmosisDocument.getBytes(),
                DocumentType.JUDGE_FINAL_ORDER
            )
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), LocalDate.now());
    }

    private JudgeFinalOrderForm getFinalOrderType(CaseData caseData) {
        return caseData.getFinalOrderSelection().equals(FREE_FORM_ORDER) ? getFreeFormOrder(caseData) : getAssistedOrder(
            caseData);
    }

    private JudgeFinalOrderForm getFreeFormOrder(CaseData caseData) {
        var freeFormOrderBuilder = JudgeFinalOrderForm.builder()
            .caseNumber(caseData.getCcdCaseReference().toString())
            .caseName(caseData.getCaseNameHmctsInternal())
            .claimantReference(nonNull(caseData.getSolicitorReferences())
                                   ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .defendantReference(nonNull(caseData.getSolicitorReferences())
                                   ? caseData.getSolicitorReferences().getRespondentSolicitor1Reference() : null)
            .freeFormRecitalText(caseData.getFreeFormRecitalTextArea())
            .freeFormRecordedText(caseData.getFreeFormRecordedTextArea())
            .freeFormOrderedText(caseData.getFreeFormOrderedTextArea())
            .orderOnCourtsList(caseData.getOrderOnCourtsList())
            .onInitiativeSelectionText(nonNull(caseData.getOrderOnCourtInitiative())
                                           ? caseData.getOrderOnCourtInitiative().getOnInitiativeSelectionTextArea() : null)
            .onInitiativeSelectionDate(nonNull(caseData.getOrderOnCourtInitiative())
                                           ? caseData.getOrderOnCourtInitiative().getOnInitiativeSelectionDate() : null)
            .withoutNoticeSelectionText(nonNull(caseData.getOrderWithoutNotice())
                                            ? caseData.getOrderWithoutNotice().getWithoutNoticeSelectionTextArea() : null)
            .withoutNoticeSelectionDate(nonNull(caseData.getOrderWithoutNotice())
                                            ? caseData.getOrderWithoutNotice().getWithoutNoticeSelectionDate() : null);
        return freeFormOrderBuilder.build();
    }

    private JudgeFinalOrderForm getAssistedOrder(CaseData caseData) {
        var assistedFormOrderBuilder = JudgeFinalOrderForm.builder()
            .caseNumber(caseData.getCcdCaseReference().toString())
            .caseName(caseData.getCaseNameHmctsInternal())
            .claimantReference(nonNull(caseData.getSolicitorReferences())
                                   ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .defendantReference(nonNull(caseData.getSolicitorReferences())
                                    ? caseData.getSolicitorReferences().getRespondentSolicitor1Reference() : null)
            .finalOrderMadeSelection(caseData.getFinalOrderMadeSelection())
            .finalOrderHeardDate(nonNull(caseData.getFinalOrderDateHeardComplex()) ?
                                     caseData.getFinalOrderDateHeardComplex().getDate() : null)
            .finalOrderRepresented(nonNull(caseData.getFinalOrderRepresentation()) ?
                                       caseData.getFinalOrderRepresentation().getTypeRepresentationList().name() : "")
            .defendantAttended(!(caseData.getFinalOrderRepresentation().getTypeRepresentationComplex()
                                   .getTypeRepresentationDefendantList()
                                   .equals(DEFENDANT_NOT_ATTENDING)))
            .claimantAttended(!(caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantList().equals(
                FinalOrdersClaimantRepresentationList.CLAIMANT_NOT_ATTENDING)))
            .judgeHeardFromClaimantTextIfAttended(getRepresentedClaimant(caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantList()))
            .judgeHeardFromDefendantTextIfAttended(getRepresentedDefendant(caseData.getFinalOrderRepresentation().getTypeRepresentationComplex()
                                      .getTypeRepresentationDefendantList()))
            .judgeHeardClaimantNotAttendedText(nonNull(caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex())
                                                   ?
                                                   getNotAttendedText(caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex().getList(), "CLAIMANT") : "")
            .judgeHeardDefendantNotAttendedText(nonNull(caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex()) ?
                                                    getNotAttendedText(caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex().getListDef(), "DEFENDANT") : "")
            .otherRepresentedText(nonNull(caseData.getFinalOrderRepresentation().getTypeRepresentationOtherComplex()) ? caseData.getFinalOrderRepresentation().getTypeRepresentationOtherComplex().getDetailsRepresentationText() : "" )
            .judgeConsideredPapers(caseData.getFinalOrderRepresentation().getTypeRepresentationJudgePapersList().stream().anyMatch(finalOrdersJudgePapers -> finalOrdersJudgePapers.equals(
                FinalOrdersJudgePapers.CONSIDERED)))
            .isRecorded(caseData.getFinalOrderRecitals().stream().anyMatch(finalOrderToggle -> finalOrderToggle.equals(
                FinalOrderToggle.SHOW)))
            .recordedText(caseData.getFinalOrderRecitalsRecorded().getText())
            .orderedText(caseData.getFinalOrderOrderedThatText())
            .costSelection(caseData.getAssistedOrderCostList().name())
            .costReservedText(nonNull(caseData.getAssistedOrderCostsReserved()) ?
                                  caseData.getAssistedOrderCostsReserved().getDetailsRepresentationText() : "")
            .paidByDate(getPaidByDate(caseData))
            .costProtection(getCostProtection(caseData))
            .costAmount(getCostAmount(caseData))
            .bespokeText(nonNull(caseData.getAssistedOrderCostsBespoke()) ?
                             caseData.getAssistedOrderCostsBespoke().getBesPokeCostDetailsText() : "")
            .furtherHearingFromDate(caseData.getFinalOrderFurtherHearingComplex().getListFromDate())
            .furtherHearingToDate(caseData.getFinalOrderFurtherHearingComplex().getDateToDate())
            .furtherHearingLocation(nonNull(caseData.getFinalOrderFurtherHearingComplex().getAlternativeHearingList()) ?
                                        caseData.getFinalOrderFurtherHearingComplex().getAlternativeHearingList().getValue().getLabel() : "")
            .furtherHearingMethod(caseData.getFinalOrderFurtherHearingComplex().getHearingMethodList().name())
            .isAppeal(caseData.getFinalOrderAppealToggle().equals(FinalOrderToggle.SHOW))
            .appealFor(caseData.getFinalOrderAppealComplex().getList().name())
            .appealGranted(caseData.getFinalOrderAppealComplex().getApplicationList().name().equals(ApplicationAppealList.GRANTED))
            .appealReason(nonNull(caseData.getFinalOrderAppealComplex().getApplicationList().name().equals(ApplicationAppealList.GRANTED))
                              ? caseData.getFinalOrderAppealComplex().getAppealGranted().getReasonsText() : caseData.getFinalOrderAppealComplex().getAppealRefused().getReasonsText())
            .orderWithoutNotice(caseData.getOrderMadeOnDetailsList().name())
            .isReason(caseData.getFinalOrderGiveReasonsYesNo())
            .reasonText(caseData.getFinalOrderGiveReasonsComplex().getReasonsText());

        return assistedFormOrderBuilder.build();


    }

    private LocalDate getPaidByDate(CaseData caseData) {
        if(caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.DEFENDANT_COST_STANDARD_BASE)) {
            return caseData.getAssistedOrderCostsDefendantPaySub().getDefendantCostStandardDate();
        } else if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.CLAIMANT_COST_STANDARD_BASE)) {
            return caseData.getAssistedOrderCostsClaimantPaySub().getClaimantCostStandardDate();
        } else if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.DEFENDANT_COST_SUMMARILY_BASE)) {
            return caseData.getAssistedOrderCostsDefendantSum().getDefendantCostSummarilyDate();
        } else if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.CLAIMANT_COST_SUMMARILY_BASE)) {
            return caseData.getAssistedOrderCostsClaimantSum().getClaimantCostSummarilyDate();
        } else {
            return null;
        }
    }

    private YesOrNo getCostProtection(CaseData caseData) {
        if(caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.DEFENDANT_COST_STANDARD_BASE)) {
            return caseData.getAssistedOrderCostsDefendantPaySub().getDefendantCostStandardProtectionOption();
        } else if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.CLAIMANT_COST_STANDARD_BASE)) {
            return caseData.getAssistedOrderCostsClaimantPaySub().getClaimantCostStandardProtectionOption();
        } else if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.DEFENDANT_COST_SUMMARILY_BASE)) {
            return caseData.getAssistedOrderCostsDefendantSum().getDefendantCostSummarilyProtectionOption();
        } else if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.CLAIMANT_COST_SUMMARILY_BASE)) {
            return caseData.getAssistedOrderCostsClaimantSum().getClaimantCostSummarilyProtectionOption();
        } else {
            return null;
        }
    }

    private String getCostAmount(CaseData caseData) {
        if(caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.DEFENDANT_COST_STANDARD_BASE)) {
            return caseData.getAssistedOrderCostsDefendantPaySub().getDefendantCostStandardText();
        } else if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.CLAIMANT_COST_STANDARD_BASE)) {
            return caseData.getAssistedOrderCostsClaimantPaySub().getClaimantCostStandardText();
        } else if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.DEFENDANT_COST_SUMMARILY_BASE)) {
            return caseData.getAssistedOrderCostsDefendantSum().getDefendantCostSummarilyText();
        } else if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.CLAIMANT_COST_SUMMARILY_BASE)) {
            return caseData.getAssistedOrderCostsClaimantSum().getClaimantCostSummarilyText();
        } else {
            return null;
        }
    }

    private String getNotAttendedText(FinalOrdersClaimantDefendantNotAttending notAttendingText, String party) {
        switch (notAttendingText) {
            case NOT_SATISFIED_NOTICE_OF_TRIAL:
                if (party.equals("CLAIMANT")) {
                    return "The claimant did not attend the trial, but the Judge was not satisfied that they had received notice of the hearing and it was not reasonable to proceed in their absence";
                } else {
                    return "The defendant did not attend the trial, but the Judge was not satisfied that they had received notice of the hearing and it was not reasonable to proceed in their absence";
                }
            case SATISFIED_NOTICE_OF_TRIAL:
                if (party.equals("CLAIMANT")) {
                    return "The claimant did not attend the trial and whilst the Judge was satisfied that they had " +
                        "received notice of the trial it was not reasonable to proceed in their absence";
                } else {
                    return "The defendant did not attend the trial and whilst the Judge was satisfied that they had received notice of the trial it was not reasonable to proceed in their absence";
                }
                case SATISFIED_REASONABLE_TO_PROCEED:
                if (party.equals("CLAIMANT")) {
                    return "The claimant did not attend the trial, but the Judge was satisfied that they had received" +
                        " notice" +
                        " of the trial and it was reasonable to proceed in their absence";
                } else {
                    return "The defendant did not attend the trial, but the Judge was satisfied that they had received notice of the trial and it was reasonable to proceed in their absence";
                }
            default: return "";
        }
    }

    private String getRepresentedClaimant(FinalOrdersClaimantRepresentationList type) {
        switch (type) {
            case COUNSEL_FOR_CLAIMANT :
                return "counsel for claimant";
            case SOLICITOR_FOR_CLAIMANT:
                return "solicitor for claimant";
            case COST_DRAFTSMAN_FOR_THE_CLAIMANT:
                return "costs draftsman for the claimant";
            case THE_CLAIMANT_IN_PERSON:
                return "the claimant in person";
            case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT:
                return "lay representative for the claimant";
            default: return "";
        }
    }

    private String getRepresentedDefendant(FinalOrdersDefendantRepresentationList type) {
        switch (type) {
            case COUNSEL_FOR_DEFENDANT:
                return "counsel for defendant";
            case SOLICITOR_FOR_DEFENDANT:
                return "solicitor for defendant ";
            case COST_DRAFTSMAN_FOR_THE_DEFENDANT:
                return "costs draftsman for the defendant";
            case THE_DEFENDANT_IN_PERSON:
                return "the defendant in person";
            case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT:
                return "lay representative for the defendant";
            default: return "";
        }
    }

}

