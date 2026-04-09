package uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.AppealOriginTypes;
import uk.gov.hmcts.reform.civil.ga.enums.dq.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.ga.enums.dq.AssistedOrderCostDropdownList;
import uk.gov.hmcts.reform.civil.ga.enums.dq.DefendantRepresentationType;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.LengthOfHearing;
import uk.gov.hmcts.reform.civil.ga.enums.dq.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.ga.enums.dq.PermissionToAppealTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.AssistedOrderForm;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HearingLength;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.ClaimantRepresentationType.CLAIMANT_NOT_ATTENDING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderConsideredToggle.CONSIDERED;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.HeardFromRepresentationTypes.OTHER_REPRESENTATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.ASSISTED_ORDER_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_ASSISTED_ORDER_FORM_LIP;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistedOrderFormGenerator implements TemplateDataGenerator<AssistedOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocmosisService docmosisService;

    private static final String FILE_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public CaseDocument generate(GeneralApplicationCaseData caseData, String authorisation) {

        AssistedOrderForm templateData = getTemplateData(null, caseData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
        log.info("Generate assisted order form with one respondent representative for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
    }

    public CaseDocument generate(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String authorisation, FlowFlag userType) {

        AssistedOrderForm templateData = getTemplateData(civilCaseData, caseData, authorisation, userType);
        log.info("Generate assisted order form for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, authorisation, userType);
    }

    public CaseDocument generateDocmosisDocument(AssistedOrderForm templateData, String authorisation, FlowFlag userType) {
        DocmosisTemplates template = getTemplate(userType);
        DocmosisDocument document = documentGeneratorService.generateDocmosisDocument(templateData, template);

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
    public AssistedOrderForm getTemplateData(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String authorisation, FlowFlag userType) {

        var caseLocation = caseData.getCaseManagementLocation();

        AssistedOrderForm assistedOrderForm = new AssistedOrderForm()
            .setCaseNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
            .setClaimant1Name(caseData.getClaimant1PartyName())
            .setClaimant2Name(caseData.getClaimant2PartyName() != null ? caseData.getClaimant2PartyName() : null)
            .setIsMultiParty(caseData.getIsMultiParty())
            .setDefendant1Name(caseData.getDefendant1PartyName())
            .setDefendant2Name(caseData
                                .getIsMultiParty().equals(YesOrNo.YES) ? caseData.getDefendant2PartyName() : null)
            .setCourtLocation(docmosisService
                               .getCaseManagementLocationVenueName(caseData, authorisation).getExternalShortName())
            .setSiteName(caseLocation != null ? caseLocation.getSiteName() : null)
            .setAddress(caseLocation != null ? caseLocation.getAddress() : null)
            .setPostcode(caseLocation != null ? caseLocation.getPostcode() : null)
            .setReceivedDate(LocalDate.now())
            .setJudgeNameTitle(caseData.getJudgeTitle())
            .setIsOrderMade(caseData.getAssistedOrderMadeSelection())
            .setIsSingleDate(checkIsSingleDate(caseData))
            .setOrderMadeSingleDate(getOrderMadeSingleDate(caseData))
            .setIsDateRange(checkIsDateRange(caseData))
            .setOrderMadeDateRangeFrom(getOrderMadeDateRangeFrom(caseData))
            .setOrderMadeDateRangeTo(getOrderMadeDateRangeTo(caseData))
            .setIsBeSpokeRange(checkIsBeSpokeRange(caseData))
            .setOrderMadeBeSpokeText(getOrderMadeBeSpokeText(caseData))
            .setJudgeHeardFromShowHide(checkJudgeHeardFromToggle(caseData))
            .setJudgeHeardSelection(getJudgeHeardFromRepresentation(caseData))
            .setClaimantRepresentation(getClaimantRepresentation(caseData))
            .setDefendantRepresentation(getDefendantRepresentation(caseData))
            .setDefendantTwoRepresentation(getDefendantTwoRepresentation(caseData))
            .setIsDefendantTwoExists(checkIsMultiparty(caseData))
            .setHeardClaimantNotAttend(getHeardClaimantNotAttend(caseData))
            .setHeardDefendantNotAttend(getHeardDefendantNotAttend(caseData))
            .setHeardDefendantTwoNotAttend(getHeardDefendantTwoNotAttend(caseData))
            .setIsOtherRepresentation(checkIsOtherRepresentation(caseData))
            .setOtherRepresentationText(getOtherRepresentationText(caseData))
            .setIsJudgeConsidered(checkIsJudgeConsidered(caseData))
            .setOrderedText(caseData.getAssistedOrderOrderedThatText())
            .setShowRecitals(checkRecitalsToggle(caseData))
            .setRecitalRecordedText(getRecitalRecordedText(caseData))
            .setShowFurtherHearing(checkFurtherHearingToggle(caseData))
            .setCheckListToDate(checkListToDate(caseData))
            .setFurtherHearingListFromDate(getFurtherHearingListFromDate(caseData))
            .setFurtherHearingListToDate(getFurtherHearingListToDate(caseData))
            .setFurtherHearingMethod(getFurtherHearingMethod(caseData))
            .setFurtherHearingDuration(getFurtherHearingDuration(caseData))
            .setCheckDatesToAvoid(checkDatesToAvoid(caseData))
            .setFurtherHearingDatesToAvoid(getFurtherHearingDatesToAvoid(caseData))
            .setFurtherHearingLocation(getFurtherHearingLocation(caseData))
            .setCostSelection(caseData.getAssistedCostTypes().name())
            .setBeSpokeCostDetailsText(getBespokeCostOrderText(caseData))
            .setCostsReservedText(getCostsReservedText(caseData))
            .setSummarilyAssessed(getSummarilyAssessed(caseData))
            .setSummarilyAssessedDate(getSummarilyAssessedDate(caseData))
            .setDetailedAssessment(getDetailedAssessment(caseData))
            .setInterimPayment(getInterimPayment(caseData))
            .setInterimPaymentDate(getInterimPaymentDate(caseData))
            .setIsQocsProtectionEnabled(checkIsQocsProtectionEnabled(caseData))
            .setCostsProtection(caseData.getPublicFundingCostsProtection())
            .setShowAppeal(checkAppealToggle(caseData))
            .setClaimantOrDefendantAppeal(getClaimantOrDefendantAppeal(caseData))
            .setIsAppealGranted(isAppealGranted(caseData))
            .setTableAorB(checkCircuitOrHighCourtJudge(caseData))
            .setAppealDate(getAppealDate(caseData))
            .setShowInitiativeOrWithoutNotice(checkInitiativeOrWithoutNotice(caseData))
            .setShowInitiative(checkInitiative(caseData))
            .setOrderMadeOnText(getOrderMadeOnText(caseData))
            .setInitiativeDate(getOrderMadeCourtInitiativeDate(caseData))
            .setWithoutNoticeDate(getOrderMadeCourtWithOutNoticeDate(caseData))
            .setReasonsText(getReasonText(caseData));

        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            boolean parentClaimantIsApplicant = caseData.identifyParentClaimantIsApplicant(caseData);

            assistedOrderForm
                .setPartyName(caseData.getPartyName(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressAddressLine1(caseData.partyAddressAddressLine1(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressAddressLine2(caseData.partyAddressAddressLine2(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressAddressLine3(caseData.partyAddressAddressLine3(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressPostCode(caseData.partyAddressPostCode(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressPostTown(caseData.partyAddressPostTown(parentClaimantIsApplicant, userType, civilCaseData));
        }
        return assistedOrderForm;
    }

    protected String getCostsReservedText(GeneralApplicationCaseData caseData) {
        return caseData.getAssistedCostTypes().getDisplayedValue().equals(AssistedCostTypesList.COSTS_RESERVED.getDisplayedValue())
            && nonNull(caseData.getCostReservedDetails()) ? caseData.getCostReservedDetails().getDetailText() : null;
    }

    protected String getBespokeCostOrderText(GeneralApplicationCaseData caseData) {
        return caseData.getAssistedCostTypes().getDisplayedValue()
                .equals(AssistedCostTypesList.BESPOKE_COSTS_ORDER.getDisplayedValue())
                && nonNull(caseData.getAssistedOrderCostsBespoke()) ? "\n\n" + caseData.getAssistedOrderCostsBespoke().getDetailText() : null;
    }

    protected Boolean checkIsOtherRepresentation(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderRepresentation())
            && caseData.getAssistedOrderRepresentation().getRepresentationType().equals(
            OTHER_REPRESENTATION);
    }

    protected Boolean checkIsMultiparty(GeneralApplicationCaseData caseData) {
        return caseData.getIsMultiParty().equals(YesOrNo.YES);
    }

    protected Boolean checkDatesToAvoid(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderFurtherHearingDetails()) && caseData.getAssistedOrderFurtherHearingDetails().getDatesToAvoid().equals(YesOrNo.YES);
    }

    protected String getFurtherHearingMethod(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderFurtherHearingDetails()) ? caseData.getAssistedOrderFurtherHearingDetails().getHearingMethods().name() : null;
    }

    protected YesOrNo checkListToDate(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderFurtherHearingDetails())
            && nonNull(caseData.getAssistedOrderFurtherHearingDetails().getListToDate()) ? YesOrNo.YES : YesOrNo.NO;
    }

    protected String getRecitalRecordedText(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderRecitalsRecorded()) ? caseData.getAssistedOrderRecitalsRecorded().getText() : null;
    }

    protected Boolean checkIsBeSpokeRange(GeneralApplicationCaseData caseData) {
        return caseData.getAssistedOrderMadeSelection().equals(YesOrNo.YES) && nonNull(caseData.getAssistedOrderMadeDateHeardDetails().getBeSpokeRangeSelection());
    }

    protected Boolean checkIsDateRange(GeneralApplicationCaseData caseData) {
        return caseData.getAssistedOrderMadeSelection().equals(YesOrNo.YES) && nonNull(caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection());
    }

    protected Boolean checkIsSingleDate(GeneralApplicationCaseData caseData) {
        return caseData.getAssistedOrderMadeSelection().equals(YesOrNo.YES) && nonNull(caseData.getAssistedOrderMadeDateHeardDetails().getSingleDateSelection());
    }

    protected Boolean checkInitiative(GeneralApplicationCaseData caseData) {
        return caseData.getOrderMadeOnOption().equals(OrderMadeOnTypes.COURTS_INITIATIVE);
    }

    protected Boolean checkInitiativeOrWithoutNotice(GeneralApplicationCaseData caseData) {
        return caseData.getOrderMadeOnOption().equals(OrderMadeOnTypes.COURTS_INITIATIVE)
            || caseData.getOrderMadeOnOption().equals(OrderMadeOnTypes.WITHOUT_NOTICE);
    }

    protected Boolean isAppealGranted(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderAppealDetails())
            && caseData.getAssistedOrderAppealDetails().getPermissionToAppeal().name().equals(PermissionToAppealTypes.GRANTED.name());
    }

    protected LocalDate getOrderMadeDateRangeTo(GeneralApplicationCaseData caseData) {
        return (caseData.getAssistedOrderMadeSelection().equals(YesOrNo.YES)
            && nonNull(caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection()))
            ? caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection().getDateRangeTo() : null;
    }

    protected LocalDate getOrderMadeDateRangeFrom(GeneralApplicationCaseData caseData) {
        return (caseData.getAssistedOrderMadeSelection().equals(YesOrNo.YES)
            && nonNull(caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection()))
            ? caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection().getDateRangeFrom() : null;
    }

    protected String getOrderMadeBeSpokeText(GeneralApplicationCaseData caseData) {
        return (caseData.getAssistedOrderMadeSelection().equals(YesOrNo.YES)
            && nonNull(caseData.getAssistedOrderMadeDateHeardDetails().getBeSpokeRangeSelection()))
            ? caseData.getAssistedOrderMadeDateHeardDetails().getBeSpokeRangeSelection().getBeSpokeRangeText() : null;
    }

    protected LocalDate getFurtherHearingListFromDate(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderFurtherHearingDetails())
            ? caseData.getAssistedOrderFurtherHearingDetails().getListFromDate() : null;
    }

    protected LocalDate getFurtherHearingListToDate(GeneralApplicationCaseData caseData) {
        return (nonNull(caseData.getAssistedOrderFurtherHearingDetails())
            && nonNull(caseData.getAssistedOrderFurtherHearingDetails().getListToDate()))
            ? caseData.getAssistedOrderFurtherHearingDetails().getListToDate() : null;
    }

    protected LocalDate getFurtherHearingDatesToAvoid(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderFurtherHearingDetails())
            && caseData.getAssistedOrderFurtherHearingDetails().getDatesToAvoid().equals(YesOrNo.YES)
            ? caseData.getAssistedOrderFurtherHearingDetails().getDatesToAvoidDateDropdown().getDatesToAvoidDates() : null;
    }

    protected LocalDate getOrderMadeSingleDate(GeneralApplicationCaseData caseData) {
        return (caseData.getAssistedOrderMadeSelection().equals(YesOrNo.YES)
            && nonNull(caseData.getAssistedOrderMadeDateHeardDetails().getSingleDateSelection()))
            ? caseData.getAssistedOrderMadeDateHeardDetails().getSingleDateSelection().getSingleDate() : null;
    }

    protected String getOtherRepresentationText(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderRepresentation())
            && caseData.getAssistedOrderRepresentation().getRepresentationType().equals(
            OTHER_REPRESENTATION) ? caseData.getAssistedOrderRepresentation().getOtherRepresentation().getDetailText() : null;
    }

    protected Boolean checkIsQocsProtectionEnabled(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsYesOrNo())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsYesOrNo().equals(
            YES);
    }

    protected Boolean checkIsJudgeConsidered(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getTypeRepresentationJudgePapersList())
            && caseData.getTypeRepresentationJudgePapersList()
            .get(0).getDisplayedValue().equals(CONSIDERED.getDisplayedValue());
    }

    protected LocalDate getOrderMadeCourtWithOutNoticeDate(GeneralApplicationCaseData caseData) {
        return caseData.getOrderMadeOnOption().equals(OrderMadeOnTypes.WITHOUT_NOTICE) ? caseData.getOrderMadeOnWithOutNotice().getDate() : null;
    }

    protected LocalDate getOrderMadeCourtInitiativeDate(GeneralApplicationCaseData caseData) {
        return caseData.getOrderMadeOnOption().equals(OrderMadeOnTypes.COURTS_INITIATIVE) ? caseData.getOrderMadeOnOwnInitiative().getDate() : null;
    }

    protected LocalDate getAppealDate(GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getAssistedOrderAppealDetails())
            && caseData.getAssistedOrderAppealDetails().getPermissionToAppeal().name().equals(PermissionToAppealTypes.GRANTED.name())) {
            if (caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForGranted().getAssistedOrderAppealJudgeSelection()
                .equals(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)) {
                return caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForGranted().getAppealChoiceOptionA().getAppealGrantedRefusedDate();
            } else {
                return caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForGranted().getAppealChoiceOptionB().getAppealGrantedRefusedDate();
            }
        }
        if (nonNull(caseData.getAssistedOrderAppealDetails())
                        && caseData.getAssistedOrderAppealDetails().getPermissionToAppeal().name().equals(PermissionToAppealTypes.REFUSED.name())) {
            if (caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForRefused().getAssistedOrderAppealJudgeSelectionRefuse()
                .equals(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)) {
                return caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForRefused().getAppealChoiceOptionA().getAppealGrantedRefusedDate();
            } else {
                return caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForRefused().getAppealChoiceOptionB().getAppealGrantedRefusedDate();
            }
        }
        return null;
    }

    protected String checkCircuitOrHighCourtJudge(GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getAssistedOrderAppealDetails())
            && caseData.getAssistedOrderAppealDetails().getPermissionToAppeal().name().equals(PermissionToAppealTypes.GRANTED.name())
            && caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForGranted().getAssistedOrderAppealJudgeSelection()
            .equals(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)) {
            return "A";
        }
        if (nonNull(caseData.getAssistedOrderAppealDetails())
            && caseData.getAssistedOrderAppealDetails().getPermissionToAppeal().name().equals(PermissionToAppealTypes.REFUSED.name())
            && caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForRefused().getAssistedOrderAppealJudgeSelectionRefuse()
            .equals(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)) {
            return "A";
        }
        return "B";
    }

    protected String getClaimantOrDefendantAppeal(GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getAssistedOrderAppealDetails()) && nonNull(caseData.getAssistedOrderAppealDetails().getAppealOrigin())) {
            if (caseData.getAssistedOrderAppealDetails().getAppealOrigin().name().equals(AppealOriginTypes.OTHER.name())) {
                return caseData.getAssistedOrderAppealDetails().getOtherOriginText();
            } else {
                return caseData.getAssistedOrderAppealDetails().getAppealOrigin().getDisplayedValue();
            }
        }
        return "";
    }

    protected Boolean checkAppealToggle(GeneralApplicationCaseData caseData) {
        return (nonNull(caseData.getAssistedOrderAppealToggle())
            && nonNull(caseData.getAssistedOrderAppealToggle().get(0))
            && caseData.getAssistedOrderAppealToggle().get(0).equals(FinalOrderShowToggle.SHOW));
    }

    protected String getSummarilyAssessed(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsMakeAnOrderTopList().equals(
            AssistedOrderCostDropdownList.COSTS)
            ? populateSummarilyAssessedText(caseData) : null;
    }

    protected LocalDate getSummarilyAssessedDate(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsMakeAnOrderTopList().equals(
            AssistedOrderCostDropdownList.COSTS)
            ? caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownDate() : null;
    }

    protected String populateSummarilyAssessedText(GeneralApplicationCaseData caseData) {
        if (caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList().equals(
            AssistedOrderCostDropdownList.CLAIMANT)) {
            return format(
                "The claimant shall pay the defendant's costs (both fixed and summarily assessed as appropriate) "
                    + "in the sum of £%s. Such sum shall be paid by 4pm on",
                MonetaryConversions.penniesToPounds(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownAmount()));
        } else {
            return format(
                "The defendant shall pay the claimant's costs (both fixed and summarily assessed as appropriate) "
                    + "in the sum of £%s. Such sum shall be paid by 4pm on",
                MonetaryConversions.penniesToPounds(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownAmount()));
        }
    }

    protected String getDetailedAssessment(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsMakeAnOrderTopList().equals(
            AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
            ? populateDetailedAssessmentText(caseData) : null;
    }

    protected String populateDetailedAssessmentText(GeneralApplicationCaseData caseData) {
        String standardOrIndemnity;
        if (caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentSecondDropdownList1().equals(
            AssistedOrderCostDropdownList.INDEMNITY_BASIS)) {
            standardOrIndemnity = "on the indemnity basis if not agreed";
        } else {
            standardOrIndemnity = "on the standard basis if not agreed";
        }

        if (caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList().equals(AssistedOrderCostDropdownList.CLAIMANT)) {
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

    protected String getInterimPayment(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsMakeAnOrderTopList().equals(
            AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentSecondDropdownList2().equals(
            AssistedOrderCostDropdownList.YES)
            ? populateInterimPaymentText(caseData) : null;
    }

    protected String populateInterimPaymentText(GeneralApplicationCaseData caseData) {
        return format(
            "An interim payment of £%s on account of costs shall be paid by 4pm on ",
            MonetaryConversions.penniesToPounds(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentThirdDropdownAmount()));
    }

    protected LocalDate getInterimPaymentDate(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsMakeAnOrderTopList().equals(
            AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
            ? caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentThirdDropdownDate() : null;
    }

    protected String getFurtherHearingLocation(GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getAssistedOrderFurtherHearingDetails())
            && (caseData.getAssistedOrderFurtherHearingDetails().getHearingLocationList()
            .getValue().getLabel().equalsIgnoreCase("Other location"))) {
            return caseData.getAssistedOrderFurtherHearingDetails().getAlternativeHearingLocation().getValue().getLabel();
        }
        return caseData.getLocationName();
    }

    protected String getFurtherHearingDuration(GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getAssistedOrderFurtherHearingDetails())
            && caseData.getAssistedOrderFurtherHearingDetails().getLengthOfNewHearing().equals(LengthOfHearing.OTHER)) {
            return getOtherLength(caseData);
        }
        return nonNull(caseData.getAssistedOrderFurtherHearingDetails())
            ? caseData.getAssistedOrderFurtherHearingDetails().getLengthOfNewHearing().getDisplayedValue() : null;
    }

    private String getOtherLength(GeneralApplicationCaseData caseData) {
        StringBuilder otherLength = new StringBuilder();
        HearingLength other = caseData.getAssistedOrderFurtherHearingDetails().getLengthOfHearingOther();
        if (Objects.nonNull(other)) {
            int otherDay = other.getLengthListOtherDays();
            int otherHour = other.getLengthListOtherHours();
            int otherMinute = other.getLengthListOtherMinutes();
            otherLength.append(otherDay > 0 ? (otherDay + " days ") : "")
                    .append(otherHour > 0 ? (otherHour + " hours ") : "")
                    .append(otherMinute > 0 ? (otherMinute + " minutes") : "");
        }
        return otherLength.toString().trim();
    }

    protected Boolean checkFurtherHearingToggle(GeneralApplicationCaseData caseData) {
        return (nonNull(caseData.getAssistedOrderFurtherHearingToggle())
            && nonNull(caseData.getAssistedOrderFurtherHearingToggle().get(0))
            && caseData.getAssistedOrderFurtherHearingToggle().get(0).equals(FinalOrderShowToggle.SHOW));
    }

    protected String getHeardClaimantNotAttend(GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getAssistedOrderRepresentation())
            && caseData.getAssistedOrderRepresentation().getRepresentationType().equals(CLAIMANT_AND_DEFENDANT)
            && caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation()
            .getClaimantRepresentation().equals(CLAIMANT_NOT_ATTENDING)) {
            return caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation()
                .getHeardFromClaimantNotAttend().getListClaim().getDisplayedValue();
        }
        return null;
    }

    protected String getHeardDefendantNotAttend(GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getAssistedOrderRepresentation())
            && caseData.getAssistedOrderRepresentation().getRepresentationType().equals(CLAIMANT_AND_DEFENDANT)
            && caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation().getDefendantRepresentation()
            .equals(DefendantRepresentationType.DEFENDANT_NOT_ATTENDING)) {
            return caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation().getHeardFromDefendantNotAttend()
                .getListDef().getDisplayedValue();
        }
        return null;
    }

    protected String getHeardDefendantTwoNotAttend(GeneralApplicationCaseData caseData) {
        if (caseData.getIsMultiParty().equals(YesOrNo.YES)
            && nonNull(caseData.getAssistedOrderRepresentation())
            && caseData.getAssistedOrderRepresentation().getRepresentationType().equals(CLAIMANT_AND_DEFENDANT)
            && caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation()
            .getDefendantTwoRepresentation().equals(DefendantRepresentationType.DEFENDANT_NOT_ATTENDING)) {
            return caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation().getHeardFromDefendantTwoNotAttend()
                .getListDefTwo().getDisplayedValue();
        }
        return null;
    }

    protected String getClaimantRepresentation(GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getAssistedOrderRepresentation())
            && caseData.getAssistedOrderRepresentation().getRepresentationType().equals(CLAIMANT_AND_DEFENDANT)) {
            return caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation().getClaimantRepresentation().getDisplayedValue();
        }
        return null;
    }

    protected String getDefendantRepresentation(GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getAssistedOrderRepresentation())
            && caseData.getAssistedOrderRepresentation().getRepresentationType().equals(CLAIMANT_AND_DEFENDANT)) {
            return caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation().getDefendantRepresentation().getDisplayedValue();
        }
        return null;
    }

    protected String getDefendantTwoRepresentation(GeneralApplicationCaseData caseData) {
        if (caseData.getIsMultiParty().equals(YesOrNo.YES)
            && nonNull(caseData.getAssistedOrderRepresentation())
            && caseData.getAssistedOrderRepresentation().getRepresentationType().equals(CLAIMANT_AND_DEFENDANT)) {
            return caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation().getDefendantTwoRepresentation().getDisplayedValue();
        }
        return null;
    }

    protected String getJudgeHeardFromRepresentation(GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getAssistedOrderRepresentation())) {
            return caseData.getAssistedOrderRepresentation().getRepresentationType().getDisplayedValue();
        }
        return null;
    }

    protected boolean checkJudgeHeardFromToggle(GeneralApplicationCaseData caseData) {
        return (nonNull(caseData.getAssistedOrderJudgeHeardFrom())
            && nonNull(caseData.getAssistedOrderJudgeHeardFrom().get(0))
            && caseData.getAssistedOrderJudgeHeardFrom().get(0).equals(FinalOrderShowToggle.SHOW));
    }

    protected boolean checkRecitalsToggle(GeneralApplicationCaseData caseData) {

        return (nonNull(caseData.getAssistedOrderRecitals())
            && nonNull(caseData.getAssistedOrderRecitals().get(0))
            && caseData.getAssistedOrderRecitals().get(0).equals(FinalOrderShowToggle.SHOW));
    }

    protected String getOrderMadeOnText(GeneralApplicationCaseData caseData) {
        if (caseData.getOrderMadeOnOption().equals(OrderMadeOnTypes.COURTS_INITIATIVE)) {
            return caseData.getOrderMadeOnOwnInitiative().getDetailText();
        } else if (caseData.getOrderMadeOnOption().equals(OrderMadeOnTypes.WITHOUT_NOTICE)) {
            return caseData.getOrderMadeOnWithOutNotice().getDetailText();
        } else {
            return "";
        }
    }

    protected String getReasonText(GeneralApplicationCaseData caseData) {
        if (isNull(caseData.getAssistedOrderGiveReasonsYesNo())
            || caseData.getAssistedOrderGiveReasonsYesNo().equals(YesOrNo.NO)
            || isNull(caseData.getAssistedOrderGiveReasonsDetails().getReasonsText())) {
            return null;
        } else {
            return (caseData.getAssistedOrderGiveReasonsDetails().getReasonsText());
        }
    }

    protected String getFileName(DocmosisTemplates template) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FILE_TIMESTAMP_FORMAT);
        return String.format(template.getDocumentTitle(),
                LocalDateTime.now().format(formatter));
    }

    protected String getCaseNumberFormatted(GeneralApplicationCaseData caseData) {
        String[] parts = caseData.getCcdCaseReference().toString().split("(?<=\\G.{4})");
        return String.join("-", parts);
    }

    protected DocmosisTemplates getTemplate(FlowFlag userType) {
        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            return POST_JUDGE_ASSISTED_ORDER_FORM_LIP;
        }
        return ASSISTED_ORDER_FORM;
    }
}
