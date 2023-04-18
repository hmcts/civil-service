package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersJudgePapers;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import java.time.LocalDate;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.enums.finalorders.AppealList.OTHER;
import static uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList.CLAIMANT_NOT_ATTENDING;
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
            .finalOrderHeardDate(nonNull(caseData.getFinalOrderDateHeardComplex())
                                     ? caseData.getFinalOrderDateHeardComplex().getDate() : null)
            .finalOrderRepresented(nonNull(caseData.getFinalOrderRepresentation())
                                      ? caseData.getFinalOrderRepresentation().getTypeRepresentationList().name() : "")
            .defendantAttended(getIfAttended(caseData, true))
            .claimantAttended(getIfAttended(caseData, false))
            .judgeHeardFromClaimantTextIfAttended(getRepresentedClaimant(caseData))
            .judgeHeardFromDefendantTextIfAttended(getRepresentedDefendant(caseData))
            .judgeHeardClaimantNotAttendedText(getNotAttendedText(caseData, "CLAIMANT"))
            .judgeHeardDefendantNotAttendedText(getNotAttendedText(caseData, "DEFENDANT"))
            .otherRepresentedText(nonNull(caseData.getFinalOrderRepresentation()) && nonNull(caseData.getFinalOrderRepresentation().getTypeRepresentationOtherComplex())
                                      ? caseData.getFinalOrderRepresentation().getTypeRepresentationOtherComplex().getDetailsRepresentationText() : "")
            .judgeConsideredPapers(nonNull(caseData.getFinalOrderRepresentation()) && nonNull(caseData.getFinalOrderRepresentation().getTypeRepresentationJudgePapersList())
                                   ? caseData.getFinalOrderRepresentation().getTypeRepresentationJudgePapersList()
                                       .stream().anyMatch(finalOrdersJudgePapers -> finalOrdersJudgePapers.equals(
                FinalOrdersJudgePapers.CONSIDERED)) : false)
            .recordedToggle(nonNull(caseData.getFinalOrderRecitals())
                                ?
                                caseData.getFinalOrderRecitals().stream().anyMatch(finalOrderToggle -> finalOrderToggle.equals(
                                FinalOrderToggle.SHOW)) : false)
            .recordedText(nonNull(caseData.getFinalOrderRecitalsRecorded())
                              ? caseData.getFinalOrderRecitalsRecorded().getText() : "")
            .orderedText(caseData.getFinalOrderOrderedThatText())
            .costSelection(caseData.getAssistedOrderCostList().name())
            .costReservedText(nonNull(caseData.getAssistedOrderCostsReserved())
                                  ?
                                  caseData.getAssistedOrderCostsReserved().getDetailsRepresentationText() : "")
            .paidByDate(getPaidByDate(caseData))
            .costProtection(getCostProtection(caseData))
            .costAmount(getCostAmount(caseData))
            .bespokeText(nonNull(caseData.getAssistedOrderCostsBespoke())
                             ? caseData.getAssistedOrderCostsBespoke().getBesPokeCostDetailsText() : "")
            .furtherHearingToggle(nonNull(caseData.getFinalOrderFurtherHearingToggle())
                                      ?
                                  caseData.getFinalOrderFurtherHearingToggle().stream().anyMatch(finalOrderToggle -> finalOrderToggle.name().equals(
                                      FinalOrderToggle.SHOW.name())) : false)
            .furtherHearingFromDate(getFurtherHearingDate(caseData, true))
            .furtherHearingLength(getFurtherHearingLength(caseData))
            .furtherHearingToDate(getFurtherHearingDate(caseData, false))
            .furtherHearingLocation(nonNull(caseData.getFinalOrderFurtherHearingComplex()) && nonNull(caseData.getFinalOrderFurtherHearingComplex().getAlternativeHearingList())
                                        ?
                                        caseData.getFinalOrderFurtherHearingComplex().getAlternativeHearingList().getValue().getLabel() : "")
            .furtherHearingMethod(nonNull(caseData.getFinalOrderFurtherHearingComplex()) && nonNull(caseData.getFinalOrderFurtherHearingComplex().getHearingMethodList())
                                  ? caseData.getFinalOrderFurtherHearingComplex().getHearingMethodList().name() : "")
            .appealToggle(nonNull(caseData.getFinalOrderAppealToggle())
                              ?
                          caseData.getFinalOrderAppealToggle().stream().anyMatch(finalOrderToggle -> finalOrderToggle.equals(
                              FinalOrderToggle.SHOW)) : false)
            .appealFor(getAppealFor(caseData))
            .appealGranted(nonNull(caseData.getFinalOrderAppealComplex()) && nonNull(caseData.getFinalOrderAppealComplex().getApplicationList())
                               ?
                               caseData.getFinalOrderAppealComplex().getApplicationList().name().equals(ApplicationAppealList.GRANTED.name()) : false)
            .appealReason(getAppealReason(caseData))
            .orderWithoutNotice(caseData.getOrderMadeOnDetailsList().name())
            .isReason(caseData.getFinalOrderGiveReasonsYesNo())
            .reasonText(nonNull(caseData.getFinalOrderGiveReasonsComplex())
                            ? caseData.getFinalOrderGiveReasonsComplex().getReasonsText() : "");

        return assistedFormOrderBuilder.build();
    }

    public String getAppealFor(CaseData caseData) {
        if (caseData.getFinalOrderAppealComplex() != null && caseData.getFinalOrderAppealComplex().getList() != null) {
            if (caseData.getFinalOrderAppealComplex().getList().name().equals(OTHER.name())) {
                return caseData.getFinalOrderAppealComplex().getOtherText();
            } else {
                return caseData.getFinalOrderAppealComplex().getList().name().toLowerCase();
            }
        }
        return "";
    }

    public LocalDate getFurtherHearingDate(CaseData caseData, boolean isFromDate) {
        if (caseData.getFinalOrderFurtherHearingToggle() != null
            && caseData.getFinalOrderFurtherHearingToggle().stream().anyMatch(finalOrderToggle -> finalOrderToggle.equals(
                FinalOrderToggle.SHOW)) && caseData.getFinalOrderFurtherHearingComplex() != null) {
            if (isFromDate) {
                return caseData.getFinalOrderFurtherHearingComplex().getListFromDate();
            } else {
                return caseData.getFinalOrderFurtherHearingComplex().getDateToDate();
            }
        }
        return null;
    }

    public boolean getIfAttended(CaseData caseData, boolean isDefendant) {
        if (caseData.getFinalOrderRepresentation() != null && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null) {
            if (isDefendant) {
                return (!(caseData.getFinalOrderRepresentation().getTypeRepresentationComplex()
                    .getTypeRepresentationDefendantList()
                    .equals(DEFENDANT_NOT_ATTENDING)));
            } else {
                return (!(caseData.getFinalOrderRepresentation().getTypeRepresentationComplex()
                    .getTypeRepresentationClaimantList()
                    .equals(CLAIMANT_NOT_ATTENDING)));
            }
        }
        return false;
    }

    public String getFurtherHearingLength(CaseData caseData) {
        if (caseData.getFinalOrderFurtherHearingComplex() != null) {
            if (caseData.getFinalOrderFurtherHearingComplex().getLengthList() != null) {
                switch (caseData.getFinalOrderFurtherHearingComplex().getLengthList()) {
                    case MINUTES_15:
                        return "15 minutes";
                    case MINUTES_30:
                        return "30 minutes";
                    case HOUR_1:
                        return "1 hour";
                    case HOUR_1_5:
                        return "1.5 hours";
                    case HOUR_2:
                        return "2 hours";
                    default:
                        return "";
                }
            } else if (caseData.getFinalOrderFurtherHearingComplex().getLengthListOther() != null) {
                StringBuilder otherLength = new StringBuilder();
                if (caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherDays() != null) {
                    otherLength.append(caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherDays() + " days ");
                }
                if (caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherHours() != null) {
                    otherLength.append(caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherHours() +
                                           " hours ");
                }
                if (caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherMinutes() != null) {
                    otherLength.append(caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherMinutes() + " minutes");
                }
                return otherLength.toString();
            }
        }
        return "";
    }

    public String getAppealReason(CaseData caseData) {
        boolean isAppeal = false;
        if (caseData.getFinalOrderAppealToggle() != null) {
            isAppeal =
                caseData.getFinalOrderAppealToggle().stream().anyMatch(finalOrderToggle -> finalOrderToggle.equals(
                    FinalOrderToggle.SHOW));
        }
        if (isAppeal) {
            if (caseData.getFinalOrderAppealComplex().getApplicationList().name().equals(ApplicationAppealList.GRANTED.name())) {
                return caseData.getFinalOrderAppealComplex().getAppealGranted().getReasonsText();
            } else {
                return caseData.getFinalOrderAppealComplex().getAppealRefused().getRefusedText();
            }
        }
        return "";
    }

    public LocalDate getPaidByDate(CaseData caseData) {
        if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.DEFENDANT_COST_STANDARD_BASE)) {
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

    public YesOrNo getCostProtection(CaseData caseData) {
        if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.DEFENDANT_COST_STANDARD_BASE)) {
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

    public String getCostAmount(CaseData caseData) {
        if (caseData.getAssistedOrderCostList().equals(AssistedCostTypesList.DEFENDANT_COST_STANDARD_BASE)) {
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

    private String getNotAttendedText(CaseData caseData, String party) {
        if (caseData.getFinalOrderRepresentation() == null) {
            return "";
        }
        if (party.equals("DEFENDANT")){
            return getDefendantNotAttendedText(caseData);
        } else {
            return getClaimantNotAttendedText(caseData);
        }
    }

    public String getClaimantNotAttendedText(CaseData caseData) {
        if (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex() != null
                && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex().getList() != null) {
                FinalOrdersClaimantDefendantNotAttending notAttendingType =
                    caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex().getList();
            switch (notAttendingType) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    return "The claimant did not attend the trial, " +
                        "but the Judge was not satisfied that they had received notice of the hearing and it was not reasonable to proceed in their absence";
                case SATISFIED_NOTICE_OF_TRIAL:
                    return "The claimant did not attend the trial and whilst the Judge was satisfied that they had " +
                        "received notice of the trial it was not reasonable to proceed in their absence";
                case SATISFIED_REASONABLE_TO_PROCEED:
                    return "The claimant did not attend the trial, but the Judge was satisfied that they had received" +
                        " notice" +
                        " of the trial and it was reasonable to proceed in their absence";
                default:
                        return "";
            }
        }
        return "";
    }

    public String getDefendantNotAttendedText(CaseData caseData) {
        if (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex() != null
                && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex().getListDef() != null) {
                FinalOrdersClaimantDefendantNotAttending notAttendingType =
                    caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex().getListDef();
            switch (notAttendingType) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    return "The defendant did not attend the trial," +
                        " but the Judge was not satisfied that they had received notice of the hearing and it was not reasonable to proceed in their absence";
                case SATISFIED_NOTICE_OF_TRIAL:
                    return "The defendant did not attend the trial and whilst the Judge was satisfied " +
                        "that they had received notice of the trial it was not reasonable to proceed in their absence";
                case SATISFIED_REASONABLE_TO_PROCEED:
                    return "The defendant did not attend the trial," +
                        " but the Judge was satisfied that they had received notice of the trial and it was reasonable to proceed in their absence";
                default:
                    return "";
            }
        }
        return "";
    }

    public String getRepresentedClaimant(CaseData caseData) {
        if (caseData.getFinalOrderRepresentation() != null && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantList() != null) {
            FinalOrdersClaimantRepresentationList type =
                caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantList();
            switch (type) {
                case COUNSEL_FOR_CLAIMANT:
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
        return "";
    }

    public String getRepresentedDefendant(CaseData caseData) {
        if (caseData.getFinalOrderRepresentation() != null && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantList() != null) {
            FinalOrdersDefendantRepresentationList type =
                caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantList();
            switch (type) {
                case COUNSEL_FOR_DEFENDANT:
                    return "counsel for defendant";
                case SOLICITOR_FOR_DEFENDANT:
                    return "solicitor for defendant";
                case COST_DRAFTSMAN_FOR_THE_DEFENDANT:
                    return "costs draftsman for the defendant";
                case THE_DEFENDANT_IN_PERSON:
                    return "the defendant in person";
                case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT:
                    return "lay representative for the defendant";
                default: return "";
            }
        }
        return "";
    }
}

