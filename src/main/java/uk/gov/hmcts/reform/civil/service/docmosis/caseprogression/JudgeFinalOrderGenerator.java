package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums;
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

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.enums.finalorders.AppealList.OTHER;
import static uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList.CIRCUIT_COURT;
import static uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList.GRANTED;
import static uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList.REFUSED;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.ASSISTED_ORDER_PDF;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FREE_FORM_ORDER_PDF;

@Slf4j
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
        return caseData.getFinalOrderSelection().equals(FREE_FORM_ORDER) ? getFreeFormOrder(
            caseData,
            authorisation
        ) : getAssistedOrder(caseData, authorisation);
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
            .recordedToggle(nonNull(caseData.getFinalOrderRecitals())
                                && caseData.getFinalOrderRecitals().stream().anyMatch(finalOrderToggle -> finalOrderToggle.equals(
                FinalOrderToggle.SHOW)))
            .recordedText(nonNull(caseData.getFinalOrderRecitalsRecorded())
                              ? caseData.getFinalOrderRecitalsRecorded().getText() : "")
            .orderedText(caseData.getFinalOrderOrderedThatText())
            .claimantAttendsOrRepresented(claimantAttendsOrRepresentedTextBuilder(caseData, false))
            .claimantTwoAttendsOrRepresented(nonNull(caseData.getApplicant2())
                                                 ? claimantAttendsOrRepresentedTextBuilder(caseData, true) : null)
            .defendantAttendsOrRepresented(defendantAttendsOrRepresentedTextBuilder(caseData, false))
            .defendantTwoAttendsOrRepresented(nonNull(caseData.getRespondent2())
                                                  ? defendantAttendsOrRepresentedTextBuilder(caseData, true) : null)
            .otherRepresentedText(nonNull(caseData.getFinalOrderRepresentation())
                                      && nonNull(caseData.getFinalOrderRepresentation().getTypeRepresentationOtherComplex())
                                      ? caseData.getFinalOrderRepresentation().getTypeRepresentationOtherComplex().getDetailsRepresentationText() : "")
            .judgeConsideredPapers(nonNull(caseData.getFinalOrderRepresentation()) && nonNull(caseData.getFinalOrderRepresentation().getTypeRepresentationJudgePapersList())
                                       ? caseData.getFinalOrderRepresentation().getTypeRepresentationJudgePapersList()
                .stream().anyMatch(finalOrdersJudgePapers -> finalOrdersJudgePapers.equals(
                    FinalOrdersJudgePapers.CONSIDERED)) : false)
            .furtherHearingToggle(nonNull(caseData.getFinalOrderFurtherHearingToggle())
                                      ?
                                      caseData.getFinalOrderFurtherHearingToggle().stream().anyMatch(finalOrderToggle -> finalOrderToggle.name().equals(
                                          FinalOrderToggle.SHOW.name())) : false)
            .furtherHearingToToggle(nonNull(getFurtherHearingDate(caseData, false)))
            .furtherHearingFromDate(getFurtherHearingDate(caseData, true))
            .furtherHearingToDate(getFurtherHearingDate(caseData, false))
            .furtherHearingLength(getFurtherHearingLength(caseData))
            .datesToAvoid(nonNull(caseData.getFinalOrderFurtherHearingToggle())
                             && nonNull(caseData.getFinalOrderFurtherHearingComplex().getDatesToAvoidDateDropdown())
                             ? caseData.getFinalOrderFurtherHearingComplex().getDatesToAvoidDateDropdown().getDatesToAvoidDates() : null)
            .showFurtherHearingLocationAlt(isDefaultCourt(caseData))
            .furtherHearingLocationDefault(LocationRefDataService.getDisplayEntry(locationRefData))
            .furtherHearingLocationAlt(nonNull(caseData.getFinalOrderFurtherHearingToggle())
                                           && nonNull(caseData.getFinalOrderFurtherHearingComplex())
                                           && nonNull(caseData.getFinalOrderFurtherHearingComplex().getAlternativeHearingList())
                                           ? caseData.getFinalOrderFurtherHearingComplex().getAlternativeHearingList().getValue().getLabel() : null)
            .furtherHearingMethod(nonNull(caseData.getFinalOrderFurtherHearingComplex()) && nonNull(caseData.getFinalOrderFurtherHearingComplex().getHearingMethodList())
                                      ? caseData.getFinalOrderFurtherHearingComplex().getHearingMethodList().name() : "")
            .hearingNotes(nonNull(caseData.getFinalOrderFurtherHearingToggle())
                              && nonNull(caseData.getFinalOrderFurtherHearingComplex().getHearingNotesText())
                              ? caseData.getFinalOrderFurtherHearingComplex().getHearingNotesText() : null)
            .costSelection(caseData.getAssistedOrderCostList().name())
            .costsReservedText(nonNull(caseData.getAssistedOrderCostsReserved())
                                   ? caseData.getAssistedOrderCostsReserved().getDetailsRepresentationText() : null)
            .bespokeCostText(nonNull(caseData.getAssistedOrderCostsBespoke())
                                 ? caseData.getAssistedOrderCostsBespoke().getBesPokeCostDetailsText() : null)
            .summarilyAssessed(nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
                                   && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
                                   && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderClaimantDefendantFirstDropdown().equals(
                CostEnums.COSTS)
                                   ? populateSummarilyAssessedText(caseData) : null)
            .summarilyAssessedDate(nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
                                       && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
                                       && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderClaimantDefendantFirstDropdown().equals(
                CostEnums.COSTS)
                                       ? caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownDate() : null)
            .detailedAssessment(nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
                                    && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
                                    && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderClaimantDefendantFirstDropdown().equals(
                CostEnums.SUBJECT_DETAILED_ASSESSMENT)
                                    ? populateDetailedAssessmentText(caseData) : null)
            .interimPayment(nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
                                && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
                                && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderClaimantDefendantFirstDropdown().equals(
                CostEnums.SUBJECT_DETAILED_ASSESSMENT)
                                && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentSecondDropdownList2().equals(
                CostEnums.YES)
                                ? populateInterimPaymentText(caseData) : null)
            .interimPaymentDate(nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
                                    && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
                                    && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderClaimantDefendantFirstDropdown().equals(
                CostEnums.SUBJECT_DETAILED_ASSESSMENT)
                                    ? caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentThirdDropdownDate() : null)
            .qcosProtection(nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
                                && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsYesOrNo())
                                && caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsYesOrNo().equals(
                YES) ? "true" : null)
            .costsProtection(caseData.getPublicFundingCostsProtection().equals(YES) ? "true" : null)
            //appeal section
            .claimantOrDefendantAppeal(getAppealFor(caseData))
            .appealGranted(nonNull(caseData.getFinalOrderAppealComplex()) && nonNull(caseData.getFinalOrderAppealComplex().getApplicationList())
                               ? caseData.getFinalOrderAppealComplex().getApplicationList().name().equals(ApplicationAppealList.GRANTED.name()) : false)
            .tableAorB(circuitOrHighCourt(caseData))
            .appealDate(getAppealDate(caseData))
            // InitiativeOrWithoutNotice section
            .showInitiativeOrWithoutNotice(getInitiativeOrWithoutNotice(caseData))
            .initiativeDate(caseData.getOrderMadeOnDetailsList().equals(OrderMadeOnTypes.COURTS_INITIATIVE)
                                ? caseData.getOrderMadeOnDetailsOrderCourt().getOwnInitiativeDate() : null)
            .withoutNoticeDate(caseData.getOrderMadeOnDetailsList().equals(OrderMadeOnTypes.WITHOUT_NOTICE)
                                   ? caseData.getOrderMadeOnDetailsOrderWithoutNotice().getWithOutNoticeDate() : null)
            .reasonsText(nonNull(caseData.getFinalOrderGiveReasonsComplex())
                             ? caseData.getFinalOrderGiveReasonsComplex().getReasonsText() : null);

        return assistedFormOrderBuilder.build();
    }

    public String getInitiativeOrWithoutNotice(CaseData caseData) {
        if (caseData.getOrderMadeOnDetailsList().equals(OrderMadeOnTypes.COURTS_INITIATIVE)) {
            return caseData.getOrderMadeOnDetailsOrderCourt().getOwnInitiativeText();
        }
        if (caseData.getOrderMadeOnDetailsList().equals(OrderMadeOnTypes.WITHOUT_NOTICE)) {
            return caseData.getOrderMadeOnDetailsOrderWithoutNotice().getWithOutNoticeText();
        }
        return null;
    }

    public LocalDate getAppealDate(CaseData caseData) {
        if (caseData.getFinalOrderAppealComplex() != null
            && caseData.getFinalOrderAppealComplex().getApplicationList() == GRANTED) {
            if (caseData.getFinalOrderAppealComplex().getAppealGrantedDropdown().getCircuitOrHighCourtList().equals(
                CIRCUIT_COURT)) {
                return caseData.getFinalOrderAppealComplex().getAppealGrantedDropdown().getAppealChoiceSecondDropdownA().getAppealGrantedRefusedDate();
            } else {
                return caseData.getFinalOrderAppealComplex().getAppealGrantedDropdown().getAppealChoiceSecondDropdownB().getAppealGrantedRefusedDate();
            }
        }
        if (caseData.getFinalOrderAppealComplex() != null
            && caseData.getFinalOrderAppealComplex().getApplicationList() == REFUSED) {
            if (caseData.getFinalOrderAppealComplex().getAppealRefusedDropdown().getCircuitOrHighCourtListRefuse().equals(CIRCUIT_COURT)) {
                return caseData.getFinalOrderAppealComplex().getAppealRefusedDropdown().getAppealChoiceSecondDropdownA().getAppealGrantedRefusedDate();
            } else {
                return caseData.getFinalOrderAppealComplex().getAppealRefusedDropdown().getAppealChoiceSecondDropdownB().getAppealGrantedRefusedDate();
            }
        }
        return null;
    }

    public String circuitOrHighCourt(CaseData caseData) {
        if (caseData.getFinalOrderAppealComplex() != null
            && caseData.getFinalOrderAppealComplex().getApplicationList() == GRANTED
            && caseData.getFinalOrderAppealComplex().getAppealGrantedDropdown().getCircuitOrHighCourtList().equals(CIRCUIT_COURT)) {
            return "a";
        }
        if (caseData.getFinalOrderAppealComplex() != null
            && caseData.getFinalOrderAppealComplex().getApplicationList() == REFUSED
            && caseData.getFinalOrderAppealComplex().getAppealRefusedDropdown().getCircuitOrHighCourtListRefuse().equals(CIRCUIT_COURT)) {
            return "a";
        } else {
            return "b";
        }
    }

    public String populateInterimPaymentText(CaseData caseData) {
        return format(
            "An interim payment of £%s on account of costs shall be paid by 4pm on ",
            caseData.getAssistedOrderMakeAnOrderForCosts()
                .getAssistedOrderAssessmentThirdDropdownAmount()
        );
    }

    public String populateSummarilyAssessedText(CaseData caseData) {
        if (caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList().equals(CostEnums.CLAIMANT)) {
            return format(
                "The claimant shall pay the defendant's costs (both fixed and summarily assessed as appropriate) "
                    + "in the sum of £%s. Such a sum shall be made by 4pm on",
                caseData.getAssistedOrderMakeAnOrderForCosts()
                    .getAssistedOrderCostsFirstDropdownAmount()
            );
        } else {
            return format(
                "The defendant shall pay the claimant's costs (both fixed and summarily assessed as appropriate) "
                    + "in the sum of £%s. Such a sum shall be made by 4pm on",
                caseData.getAssistedOrderMakeAnOrderForCosts()
                    .getAssistedOrderCostsFirstDropdownAmount()
            );
        }
    }

    public String populateDetailedAssessmentText(CaseData caseData) {
        String standardOrIndemnity;
        if (caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentSecondDropdownList1().equals(
            CostEnums.INDEMNITY_BASIS)) {
            standardOrIndemnity = "on the indemnity basis if not agreed";
        } else {
            standardOrIndemnity = "on the standard basis if not agreed";
        }

        if (caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList().equals(CostEnums.CLAIMANT)) {
            return format(
                "The claimant shall pay the defendant's costs to be subject to a detailed assessment %s",
                standardOrIndemnity
            );
        }
        return format(
            "The defendant shall pay the claimant's costs to be subject to a detailed assessment %s",
            standardOrIndemnity
        );
    }

    public Boolean isDefaultCourt(CaseData caseData) {
        if (caseData.getFinalOrderFurtherHearingToggle() != null
            && caseData.getFinalOrderFurtherHearingComplex() != null
            && caseData.getFinalOrderFurtherHearingComplex().getHearingLocationList() != null) {
            return caseData.getFinalOrderFurtherHearingComplex()
                .getHearingLocationList().getValue().getCode().equals("LOCATION_LIST");
        }
        return null;
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
                    case OTHER:
                        StringBuilder otherLength = new StringBuilder();
                        otherLength.append(caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherDays() + " days ").append(
                            caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherHours()).append(
                            " hours ").append(caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherMinutes()).append(
                            " minutes");
                        return otherLength.toString();
                    default:
                        return "";
                }
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
                log.error("Date formatting failed");
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
                log.error("Date formatting failed");
            }
        }
        if (caseData.getFinalOrderDateHeardComplex().getBespokeRangeSelection() != null) {
            return format(
                "on %s",
                caseData.getFinalOrderDateHeardComplex().getBespokeRangeSelection().getBespokeRangeTextArea()
            );
        }
        return null;
    }

    public String claimantAttendsOrRepresentedTextBuilder(CaseData caseData, Boolean isClaimant2) {
        String name;
        if (!isClaimant2) {
            name = caseData.getApplicant1().getPartyName();
            if (caseData.getFinalOrderRepresentation() != null && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
                && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantList() != null) {
                FinalOrdersClaimantRepresentationList type =
                    caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantList();
                switch (type) {
                    case COUNSEL_FOR_CLAIMANT:
                        return format("Counsel for %s, the claimant.", name);
                    case SOLICITOR_FOR_CLAIMANT:
                        return format("Solicitor for %s, the claimant.", name);
                    case COST_DRAFTSMAN_FOR_THE_CLAIMANT:
                        return format("Costs draftsman for %s, the claimant.", name);
                    case THE_CLAIMANT_IN_PERSON:
                        return format("%s, the claimant, in person.", name);
                    case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT:
                        return format("A lay representative for %s, the claimant.", name);
                    case CLAIMANT_NOT_ATTENDING:
                        return claimantNotAttendingText(caseData, isClaimant2, name);
                    default:
                        return "";
                }
            }
        }
        if (isClaimant2) {
            name = caseData.getApplicant2().getPartyName();
            if (caseData.getFinalOrderRepresentation() != null && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
                && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantListTwo() != null) {
                FinalOrdersClaimantRepresentationList type =
                    caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantListTwo();
                switch (type) {
                    case COUNSEL_FOR_CLAIMANT:
                        return format("Counsel for %s, the claimant.", name);
                    case SOLICITOR_FOR_CLAIMANT:
                        return format("Solicitor for %s, the claimant.", name);
                    case COST_DRAFTSMAN_FOR_THE_CLAIMANT:
                        return format("Costs draftsman for %s, the claimant.", name);
                    case THE_CLAIMANT_IN_PERSON:
                        return format("%s, the claimant, in person.", name);
                    case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT:
                        return format("A lay representative for %s, the claimant.", name);
                    case CLAIMANT_NOT_ATTENDING:
                        return claimantNotAttendingText(caseData, isClaimant2, name);
                    default:
                        return "";
                }
            }
        }
        return "";
    }

    public String defendantAttendsOrRepresentedTextBuilder(CaseData caseData, Boolean isDefendant2) {
        String name;
        if (!isDefendant2) {
            name = caseData.getRespondent1().getPartyName();
            if (caseData.getFinalOrderRepresentation() != null && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
                && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantList() != null) {
                FinalOrdersDefendantRepresentationList type =
                    caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantList();
                switch (type) {
                    case COUNSEL_FOR_DEFENDANT:
                        return format("Counsel for %s, the defendant.", name);
                    case SOLICITOR_FOR_DEFENDANT:
                        return format("Solicitor for %s, the defendant.", name);
                    case COST_DRAFTSMAN_FOR_THE_DEFENDANT:
                        return format("Costs draftsman for %s, the defendant.", name);
                    case THE_DEFENDANT_IN_PERSON:
                        return format("%s, the defendant, in person.", name);
                    case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT:
                        return format("A lay representative for %s, the defendant.", name);
                    case DEFENDANT_NOT_ATTENDING:
                        return defendantNotAttendingText(caseData, isDefendant2, name);
                    default:
                        return "";
                }
            }
        }
        if (isDefendant2) {
            name = caseData.getRespondent2().getPartyName();
            if (caseData.getFinalOrderRepresentation() != null && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
                && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantTwoList() != null) {
                FinalOrdersDefendantRepresentationList type =
                    caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantTwoList();
                switch (type) {
                    case COUNSEL_FOR_DEFENDANT:
                        return format("Counsel for %s, the defendant.", name);
                    case SOLICITOR_FOR_DEFENDANT:
                        return format("Solicitor for %s, the defendant.", name);
                    case COST_DRAFTSMAN_FOR_THE_DEFENDANT:
                        return format("Costs draftsman for %s, the defendant.", name);
                    case THE_DEFENDANT_IN_PERSON:
                        return format("%s, the defendant, in person.", name);
                    case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT:
                        return format("A lay representative for %s, the defendant.", name);
                    case DEFENDANT_NOT_ATTENDING:
                        return defendantNotAttendingText(caseData, isDefendant2, name);
                    default:
                        return "";
                }
            }
        }
        return "";
    }

    public String claimantNotAttendingText(CaseData caseData, Boolean isClaimant2, String name) {
        if (!isClaimant2 && (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex().getList() != null)) {
            FinalOrdersClaimantDefendantNotAttending notAttendingType =
                caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex().getList();
            switch (notAttendingType) {
                case SATISFIED_REASONABLE_TO_PROCEED:
                    return format(
                        "%s, the claimant, did not attend the trial. The Judge was satisfied that they had "
                            + "received notice of the trial and determined that it was reasonable to proceed in their absence.",
                        name
                    );
                case SATISFIED_NOTICE_OF_TRIAL:
                    return format(
                        "%s, the claimant, did not attend the trial and, whilst the Judge was satisfied that they had "
                            + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
                        name
                    );
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    return format(
                        "%s, the claimant, did not attend the trial. "
                            + "The Judge was not satisfied that they had received notice of the hearing "
                            + "and it was not reasonable to proceed in their absence.",
                        name
                    );

                default:
                    return "";
            }
        }
        if (isClaimant2 && (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedClaimTwoComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedClaimTwoComplex().getListClaimTwo() != null)) {
            FinalOrdersClaimantDefendantNotAttending notAttendingType =
                caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedClaimTwoComplex().getListClaimTwo();
            switch (notAttendingType) {

                case SATISFIED_REASONABLE_TO_PROCEED:
                    return format(
                        "%s, the claimant, did not attend the trial. The Judge was satisfied that they had "
                            + "received notice of the trial and determined that it was reasonable to proceed in their absence.",
                        name
                    );
                case SATISFIED_NOTICE_OF_TRIAL:
                    return format(
                        "%s, the claimant, did not attend the trial and, whilst the Judge was satisfied that they had "
                            + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
                        name
                    );
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    return format(
                        "%s, the claimant, did not attend the trial. "
                            + "The Judge was not satisfied that they had received notice of the hearing "
                            + "and it was not reasonable to proceed in their absence.",
                        name
                    );

                default:
                    return "";
            }
        }
        return "";
    }

    public String defendantNotAttendingText(CaseData caseData, Boolean isDefendant2, String name) {
        if (!isDefendant2 && (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex().getListDef() != null)) {
            FinalOrdersClaimantDefendantNotAttending notAttendingType =
                caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex().getListDef();
            switch (notAttendingType) {
                case SATISFIED_REASONABLE_TO_PROCEED:
                    return format(
                        "%s, the defendant, did not attend the trial. The Judge was satisfied that they had "
                            + "received notice of the trial and determined that it was reasonable to proceed in their absence.",
                        name
                    );
                case SATISFIED_NOTICE_OF_TRIAL:
                    return format(
                        "%s, the defendant, did not attend the trial and, whilst the Judge was satisfied that they had "
                            + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
                        name
                    );
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    return format(
                        "%s, the defendant, did not attend the trial. "
                            + "The Judge was not satisfied that they had received notice of the hearing "
                            + "and it was not reasonable to proceed in their absence.",
                        name
                    );
                default:
                    return "";
            }

        }
        if (isDefendant2 && (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureDefTwoComplex() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureDefTwoComplex().getListDefTwo() != null)) {
            FinalOrdersClaimantDefendantNotAttending notAttendingType =
                caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureDefTwoComplex().getListDefTwo();
            switch (notAttendingType) {

                case SATISFIED_REASONABLE_TO_PROCEED:
                    return format(
                        "%s, the defendant, did not attend the trial. The Judge was satisfied that they had "
                            + "received notice of the trial and determined that it was reasonable to proceed in their absence.",
                        name
                    );
                case SATISFIED_NOTICE_OF_TRIAL:
                    return format(
                        "%s, the defendant, did not attend the trial and, whilst the Judge was satisfied that they had "
                            + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
                        name
                    );
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    return format(
                        "%s, the defendant, did not attend the trial. "
                            + "The Judge was not satisfied that they had received notice of the hearing "
                            + "and it was not reasonable to proceed in their absence.",
                        name
                    );
                default:
                    return "";
            }
        }
        return "";
    }
}

