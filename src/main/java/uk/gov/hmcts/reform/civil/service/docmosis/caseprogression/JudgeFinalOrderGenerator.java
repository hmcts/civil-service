package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersJudgePapers;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
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
    private final IdamClient idamClient;
    private final LocationRefDataService locationRefDataService;
    private final DocumentHearingLocationHelper locationHelper;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        JudgeFinalOrderForm templateData = getFinalOrderType(caseData, authorisation);
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
        return format(docmosisTemplate.getDocumentTitle(), LocalDate.now());
    }

    private JudgeFinalOrderForm getFinalOrderType(CaseData caseData, String authorisation) {
        return caseData.getFinalOrderSelection().equals(FREE_FORM_ORDER) ? getFreeFormOrder(caseData, authorisation) : getAssistedOrder(caseData, authorisation);
    }

    private JudgeFinalOrderForm getFreeFormOrder(CaseData caseData, String authorisation) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        LocationRefData locationRefData;

        if (hasSDOBeenMade(caseData.getCcdState())) {
            locationRefData = locationHelper.getHearingLocation(null, caseData, authorisation);
        } else {
            locationRefData = locationRefDataService.getCcmccLocation(authorisation);
        }

        var freeFormOrderBuilder = JudgeFinalOrderForm.builder()
            .caseNumber(caseData.getCcdCaseReference().toString())
            .claimant1Name(caseData.getApplicant1().getPartyName())
            .claimant2Name(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .defendant1Name(caseData.getRespondent1().getPartyName())
            .defendant2Name(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .caseName(caseData.getCaseNameHmctsInternal())
            .claimantReference(nonNull(caseData.getSolicitorReferences())
                                   ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .defendantReference(nonNull(caseData.getSolicitorReferences())
                                   ? caseData.getSolicitorReferences().getRespondentSolicitor1Reference() : null)
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
                                            ? caseData.getOrderWithoutNotice().getWithoutNoticeSelectionDate() : null)
            .judgeNameTitle(userDetails.getFullName())
            .courtName(locationRefData.getVenueName())
            .courtLocation(LocationRefDataService.getDisplayEntry(locationRefData));
        return freeFormOrderBuilder.build();
    }

    private JudgeFinalOrderForm getAssistedOrder(CaseData caseData, String authorisation) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        LocationRefData locationRefData;

        if (hasSDOBeenMade(caseData.getCcdState())) {
            locationRefData = locationHelper.getHearingLocation(null, caseData, authorisation);
        } else {
            locationRefData = locationRefDataService.getCcmccLocation(authorisation);
        }
        var assistedFormOrderBuilder = JudgeFinalOrderForm.builder()
            .caseNumber(caseData.getCcdCaseReference().toString())
            .claimant1Name(caseData.getApplicant1().getPartyName())
            .claimant2Name(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .defendant1Name(caseData.getRespondent1().getPartyName())
            .defendant2Name(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .courtName(locationRefData.getVenueName())
            .finalOrderMadeSelection(caseData.getFinalOrderMadeSelection())
            .orderMadeDate(nonNull(caseData.getFinalOrderDateHeardComplex())
                               ? orderMadeDateBuilder(caseData) : null)
            .courtLocation(LocationRefDataService.getDisplayEntry(locationRefData))
            .judgeNameTitle(userDetails.getFullName())
            .recordedToggle(nonNull(caseData.getFinalOrderRecitals()) && caseData.getFinalOrderRecitals().stream().anyMatch(
                finalOrderToggle -> finalOrderToggle.equals(
                    FinalOrderToggle.SHOW)))
            .recordedText(nonNull(caseData.getFinalOrderRecitalsRecorded())
                              ? caseData.getFinalOrderRecitalsRecorded().getText() : "")
            .orderedText(caseData.getFinalOrderOrderedThatText())
            .claimantAttendsOrRepresented(claimantAttendsOrRepresentedTextBuilder(caseData))
            .defendantAttendsOrRepresented(defendantAttendsOrRepresentedTextBuilder(caseData))
            .otherRepresentedText(nonNull(caseData.getFinalOrderRepresentation())
                                      && nonNull(caseData.getFinalOrderRepresentation().getTypeRepresentationOtherComplex())
                                      ? caseData.getFinalOrderRepresentation().getTypeRepresentationOtherComplex().getDetailsRepresentationText() : "")
            .judgeConsideredPapers(nonNull(caseData.getFinalOrderRepresentation()) && nonNull(caseData.getFinalOrderRepresentation().getTypeRepresentationJudgePapersList())
                                   ? caseData.getFinalOrderRepresentation().getTypeRepresentationJudgePapersList()
                                       .stream().anyMatch(finalOrdersJudgePapers -> finalOrdersJudgePapers.equals(
                FinalOrdersJudgePapers.CONSIDERED)) : false);

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

    private boolean hasSDOBeenMade(CaseState state) {

        return !JUDICIAL_REFERRAL.equals(state);
    }

    public String orderMadeDateBuilder(CaseData caseData) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy");
        if (caseData.getFinalOrderDateHeardComplex().getSingleDateSelection() != null) {
            String date1 = caseData.getFinalOrderDateHeardComplex().getSingleDateSelection().getSingleDate().toString();
            try {
                var convertDateString = inputFormat.parse(date1);
                String formattedDate = outputFormat.format(convertDateString);
                return format("on %s", formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            }
        if (caseData.getFinalOrderDateHeardComplex().getDateRangeSelection() != null) {
            String date1 = caseData.getFinalOrderDateHeardComplex().getDateRangeSelection().getDateRangeFrom().toString();
            String date2 = caseData.getFinalOrderDateHeardComplex().getDateRangeSelection().getDateRangeTo().toString();
            try {
                var convertDateString1 = inputFormat.parse(date1);
                var convertDateString2 = inputFormat.parse(date2);
                String formattedDate1 = outputFormat.format(convertDateString1);
                String formattedDate2 = outputFormat.format(convertDateString2);
                return format("between %s and %s", formattedDate1, formattedDate2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (caseData.getFinalOrderDateHeardComplex().getBespokeRangeSelection() != null) {
            return format("on %s", caseData.getFinalOrderDateHeardComplex().getBespokeRangeSelection().getBespokeRangeTextArea());
        }
        return null;
    }

    public String claimantAttendsOrRepresentedTextBuilder(CaseData caseData) {

        if (caseData.getFinalOrderRepresentation() != null && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantList() != null) {
            FinalOrdersClaimantRepresentationList type =
                caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantList();
            switch (type) {
                case COUNSEL_FOR_CLAIMANT:
                    return format("Counsel for %s, the claimant.", caseData.getApplicant1().getPartyName());
                case SOLICITOR_FOR_CLAIMANT:
                    return format("Solicitor for %s, the claimant.", caseData.getApplicant1().getPartyName());
                case COST_DRAFTSMAN_FOR_THE_CLAIMANT:
                    return format("Costs draftsman for %s, the claimant.", caseData.getApplicant1().getPartyName());
                case THE_CLAIMANT_IN_PERSON:
                    return format("%s, the claimant, in person.", caseData.getApplicant1().getPartyName());
                case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT:
                    return format("A lay representative for %s, the claimant.", caseData.getApplicant1().getPartyName());
                case CLAIMANT_NOT_ATTENDING:
                    return claimantNotAttendingText(caseData);
                default: return "";
            }
        }
        return "";
    }

    public String defendantAttendsOrRepresentedTextBuilder(CaseData caseData) {

        if (caseData.getFinalOrderRepresentation() != null && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantList() != null) {
            FinalOrdersDefendantRepresentationList type =
                caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantList();
            switch (type) {
                case COUNSEL_FOR_DEFENDANT:
                    return format("Counsel for %s, the defendant.", caseData.getRespondent1().getPartyName());
                case SOLICITOR_FOR_DEFENDANT:
                    return format("Solicitor for %s, the defendant.", caseData.getRespondent1().getPartyName());
                case COST_DRAFTSMAN_FOR_THE_DEFENDANT:
                    return format("Costs draftsman for %s, the defendant.", caseData.getRespondent1().getPartyName());
                case THE_DEFENDANT_IN_PERSON:
                    return format("%s, the defendant, in person.", caseData.getRespondent1().getPartyName());
                case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT:
                    return format("A lay representative for %s, the defendant.", caseData.getRespondent1().getPartyName());
                case DEFENDANT_NOT_ATTENDING:
                    return defendantNotAttendingText(caseData);
                default: return "";
            }
        }
        return "";
    }

    public String claimantNotAttendingText(CaseData caseData) {
        if (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex() != null
                && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex().getList() != null) {
            FinalOrdersClaimantDefendantNotAttending notAttendingType =
                    caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex().getList();
            switch (notAttendingType) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    return format("%s, the claimant, did not attend the trial, " +
                        "but the Judge was not satisfied that they had received notice of the hearing and it was not reasonable to proceed in their absence",
                                  caseData.getApplicant1().getPartyName());
                case SATISFIED_NOTICE_OF_TRIAL:
                    return format("%s, the claimant, did not attend the trial and whilst the Judge was satisfied that they had " +
                        "received notice of the trial it was not reasonable to proceed in their absence",
                                  caseData.getApplicant1().getPartyName());
                case SATISFIED_REASONABLE_TO_PROCEED:
                    return format("%s, the claimant, did not attend the trial, but the Judge was satisfied that they had received" +
                        " notice" +
                        " of the trial and it was reasonable to proceed in their absence",
                                  caseData.getApplicant1().getPartyName());
                default:
                    return "";
            }
        }
        return "";
    }

    public String defendantNotAttendingText(CaseData caseData) {
        if (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex().getListDef() != null) {
            FinalOrdersClaimantDefendantNotAttending notAttendingType =
                caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex().getListDef();
            switch (notAttendingType) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    return format("%s, the defendant, did not attend the trial, " +
                                      "but the Judge was not satisfied that they had received notice of the hearing and it was not reasonable to proceed in their absence",
                                  caseData.getRespondent1().getPartyName());
                case SATISFIED_NOTICE_OF_TRIAL:
                    return format("%s, the defendant, did not attend the trial and whilst the Judge was satisfied that they had " +
                                      "received notice of the trial it was not reasonable to proceed in their absence",
                                  caseData.getRespondent1().getPartyName());
                case SATISFIED_REASONABLE_TO_PROCEED:
                    return format("%s, the defendant, did not attend the trial, but the Judge was satisfied that they had received" +
                                      " notice" +
                                      " of the trial and it was reasonable to proceed in their absence",
                                  caseData.getRespondent1().getPartyName());
                default:
                    return "";
            }
        }
        return "";
    }
}

