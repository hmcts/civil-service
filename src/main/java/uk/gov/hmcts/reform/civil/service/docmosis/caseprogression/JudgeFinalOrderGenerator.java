package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders.ClaimantAttendsOrRepresentedTextBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders.DefendantAttendsOrRepresentedTextBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.AppealInitiativeGroup;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.AttendeesRepresentationGroup;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.CaseInfoGroup;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.CostsDetailsGroup;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.HearingDetailsGroup;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.JudgeCourtDetailsGroup;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.OrderDetailsGroup;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.enums.finalorders.AppealList.OTHER;
import static uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList.CIRCUIT_COURT;
import static uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList.GRANTED;
import static uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList.REFUSED;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.ASSISTED_ORDER_PDF;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FREE_FORM_ORDER_PDF;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeFinalOrderGenerator implements TemplateDataGenerator<JudgeFinalOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final UserService userService;
    private final LocationReferenceDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;
    private final DocumentHearingLocationHelper documentHearingLocationHelper;
    private LocationRefData caseManagementLocationDetails;
    private DefendantAttendsOrRepresentedTextBuilder defendantAttendsOrRepresentedTextBuilder;
    private ClaimantAttendsOrRepresentedTextBuilder claimantAttendsOrRepresentedTextBuilder;

    private AppealInitiativeGroup appealInitiativeGroup;
    private AttendeesRepresentationGroup attendeesRepresentationGroup;
    private CaseInfoGroup caseInfoGroup;
    private CostsDetailsGroup costsDetailsGroup;
    private HearingDetailsGroup hearingDetailsGroup;
    private JudgeCourtDetailsGroup judgeCourtDetailsGroup;
    private OrderDetailsGroup orderDetailsGroup;

    private static final String DATE_FORMAT = "dd/MM/yyyy";

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
        return format(docmosisTemplate.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    }

    private JudgeFinalOrderForm getFinalOrderType(CaseData caseData, String authorisation) {
        return caseData.getFinalOrderSelection().equals(FREE_FORM_ORDER) ? getFreeFormOrder(
            caseData,
            authorisation
        ) : getAssistedOrder(caseData, authorisation);
    }

    private JudgeFinalOrderForm getFreeFormOrder(CaseData caseData, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        caseManagementLocationDetails = documentHearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);

        var freeFormOrderBuilder = JudgeFinalOrderForm.builder();

        caseInfoGroup.populateCaseInfo(freeFormOrderBuilder, caseData);
        judgeCourtDetailsGroup.populateJudgeCourtDetails(freeFormOrderBuilder, userDetails,
                                                         caseManagementLocationDetails, getHearingLocationText(caseData));
        orderDetailsGroup.populateOrderDetails(freeFormOrderBuilder, caseData);

        return freeFormOrderBuilder.build();
    }

    private JudgeFinalOrderForm getAssistedOrder(CaseData caseData, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        caseManagementLocationDetails = documentHearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);

        var assistedFormOrderBuilder = JudgeFinalOrderForm.builder();

        caseInfoGroup.populateCaseInfo(assistedFormOrderBuilder, caseData);
        judgeCourtDetailsGroup.populateJudgeCourtDetails(assistedFormOrderBuilder, userDetails,
                                                         caseManagementLocationDetails, getHearingLocationText(caseData));
        orderDetailsGroup.populateAssistedOrderDetails(assistedFormOrderBuilder, caseData);
        attendeesRepresentationGroup.populateAttendeesDetails(assistedFormOrderBuilder, caseData);
        hearingDetailsGroup.populateHearingDetails(assistedFormOrderBuilder, caseData, caseManagementLocationDetails);
        costsDetailsGroup.populateCostsDetails(assistedFormOrderBuilder, caseData);
        appealInitiativeGroup.populateAppealDetails(assistedFormOrderBuilder, caseData);
        appealInitiativeGroup.populateInitiativeOrWithoutNoticeDetails(assistedFormOrderBuilder, caseData);

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
        FinalOrderAppeal appealComplex = caseData.getFinalOrderAppealComplex();

        if (appealComplex != null
            && appealComplex.getApplicationList() == GRANTED) {
            if (appealComplex.getAppealGrantedDropdown().getCircuitOrHighCourtList().equals(
                CIRCUIT_COURT)) {
                return appealComplex.getAppealGrantedDropdown().getAppealChoiceSecondDropdownA().getAppealGrantedRefusedDate();
            } else {
                return appealComplex.getAppealGrantedDropdown().getAppealChoiceSecondDropdownB().getAppealGrantedRefusedDate();
            }
        }
        if (appealComplex != null
            && appealComplex.getApplicationList() == REFUSED) {
            if (appealComplex.getAppealRefusedDropdown().getCircuitOrHighCourtListRefuse().equals(CIRCUIT_COURT)) {
                return appealComplex.getAppealRefusedDropdown().getAppealChoiceSecondDropdownA().getAppealGrantedRefusedDate();
            } else {
                return appealComplex.getAppealRefusedDropdown().getAppealChoiceSecondDropdownB().getAppealGrantedRefusedDate();
            }
        }
        return null;
    }


    public String circuitOrHighCourt(CaseData caseData) {
        FinalOrderAppeal appealComplex = caseData.getFinalOrderAppealComplex();
        if (appealComplex != null) {
            if (appealComplex.getApplicationList() == GRANTED
                && appealComplex.getAppealGrantedDropdown().getCircuitOrHighCourtList().equals(CIRCUIT_COURT)) {
                return "a";
            }
            if (appealComplex.getApplicationList() == REFUSED
                && appealComplex.getAppealRefusedDropdown().getCircuitOrHighCourtListRefuse().equals(CIRCUIT_COURT)) {
                return "a";
            }
        }
        return "b";
    }

    public String populateInterimPaymentText(CaseData caseData) {
        return format(
            "An interim payment of £%s on account of costs shall be paid by 4pm on ",
            MonetaryConversions.penniesToPounds(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentThirdDropdownAmount()));
    }

    public String populateSummarilyAssessedText(CaseData caseData) {
        if (caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList().equals(CostEnums.CLAIMANT)) {
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
        return false;
    }

    public String getAppealFor(CaseData caseData) {
        if (caseData.getFinalOrderAppealComplex() != null && caseData.getFinalOrderAppealComplex().getList() != null) {
            if (caseData.getFinalOrderAppealComplex().getList().name().equals(OTHER.name())) {
                return caseData.getFinalOrderAppealComplex().getOtherText();
            } else {
                return caseData.getFinalOrderAppealComplex().getList().name().toLowerCase() + "'s";
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
        if (caseData.getFinalOrderFurtherHearingComplex() != null && caseData.getFinalOrderFurtherHearingComplex().getLengthList() != null) {
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
                    return getOtherLength(caseData);
                default:
                    return "";
            }
        }
        return "";
    }

    private String getOtherLength(CaseData caseData) {
        StringBuilder otherLength = new StringBuilder();
        if (Objects.nonNull(caseData.getFinalOrderFurtherHearingComplex().getLengthListOther())) {
            String otherDay = caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherDays();
            String otherHour = caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherHours();
            String otherMinute = caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherMinutes();
            otherLength.append(Objects.nonNull(otherDay) ? (otherDay + " days ") : "")
                    .append(Objects.nonNull(otherHour) ? (otherHour + " hours ") : "")
                    .append(Objects.nonNull(otherMinute) ? (otherMinute + " minutes") : "");
        }
        return otherLength.toString();
    }

    public String orderMadeDateBuilder(CaseData caseData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
        if (caseData.getFinalOrderDateHeardComplex() != null) {
            if (caseData.getFinalOrderDateHeardComplex().getSingleDateSelection() != null) {
                LocalDate date1 = caseData.getFinalOrderDateHeardComplex().getSingleDateSelection().getSingleDate();
                return format("on %s", date1.format(formatter));

            }
            if (caseData.getFinalOrderDateHeardComplex().getDateRangeSelection() != null) {
                LocalDate date1 = caseData.getFinalOrderDateHeardComplex().getDateRangeSelection().getDateRangeFrom();
                LocalDate date2 = caseData.getFinalOrderDateHeardComplex().getDateRangeSelection().getDateRangeTo();
                return format("between %s and %s", date1.format(formatter), date2.format(formatter));
            }
            if (caseData.getFinalOrderDateHeardComplex().getBespokeRangeSelection() != null) {
                return format(
                    "on %s",
                    caseData.getFinalOrderDateHeardComplex().getBespokeRangeSelection().getBespokeRangeTextArea()
                );
            }
        }
        return null;
    }

    public String generateClaimantAttendsOrRepresentedText(CaseData caseData, Boolean isClaimant2) {
        return claimantAttendsOrRepresentedTextBuilder.claimantBuilder(caseData, isClaimant2);
    }

    public String generateDefendantAttendsOrRepresentedText(CaseData caseData, Boolean isDefendant2) {
        return defendantAttendsOrRepresentedTextBuilder.defendantBuilder(caseData, isDefendant2);
    }

    private String getHearingLocationText(CaseData caseData) {
        return caseData.getHearingLocationText() != null ? caseData.getHearingLocationText()
            : LocationRefDataService.getDisplayEntry(caseManagementLocationDetails);
    }

}
