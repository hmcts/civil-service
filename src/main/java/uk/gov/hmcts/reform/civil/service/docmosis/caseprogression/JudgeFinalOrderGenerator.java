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
    private final DefendantAttendsOrRepresentedTextBuilder defendantAttendsOrRepresentedTextBuilder;
    private final ClaimantAttendsOrRepresentedTextBuilder claimantAttendsOrRepresentedTextBuilder;

    private final AppealInitiativeGroup appealInitiativeGroup;
    private final AttendeesRepresentationGroup attendeesRepresentationGroup;
    private final CaseInfoGroup caseInfoGroup;
    private final CostsDetailsGroup costsDetailsGroup;
    private final HearingDetailsGroup hearingDetailsGroup;
    private final JudgeCourtDetailsGroup judgeCourtDetailsGroup;
    private final OrderDetailsGroup orderDetailsGroup;

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

        freeFormOrderBuilder = caseInfoGroup.populateCaseInfo(freeFormOrderBuilder, caseData);
        freeFormOrderBuilder = judgeCourtDetailsGroup.populateJudgeCourtDetails(freeFormOrderBuilder, userDetails,
                                                         caseManagementLocationDetails, getHearingLocationText(caseData));
        freeFormOrderBuilder = orderDetailsGroup.populateOrderDetails(freeFormOrderBuilder, caseData);

        return freeFormOrderBuilder.build();
    }

    private JudgeFinalOrderForm getAssistedOrder(CaseData caseData, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        caseManagementLocationDetails = documentHearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);

        var assistedFormOrderBuilder = JudgeFinalOrderForm.builder();

        assistedFormOrderBuilder = caseInfoGroup.populateCaseInfo(assistedFormOrderBuilder, caseData);
        assistedFormOrderBuilder = judgeCourtDetailsGroup.populateJudgeCourtDetails(assistedFormOrderBuilder, userDetails,
                                                         caseManagementLocationDetails, getHearingLocationText(caseData));
        assistedFormOrderBuilder = orderDetailsGroup.populateAssistedOrderDetails(assistedFormOrderBuilder, caseData);
        assistedFormOrderBuilder = attendeesRepresentationGroup.populateAttendeesDetails(assistedFormOrderBuilder, caseData);
        assistedFormOrderBuilder = hearingDetailsGroup.populateHearingDetails(assistedFormOrderBuilder, caseData, caseManagementLocationDetails);
        assistedFormOrderBuilder = costsDetailsGroup.populateCostsDetails(assistedFormOrderBuilder, caseData);
        assistedFormOrderBuilder = appealInitiativeGroup.populateAppealDetails(assistedFormOrderBuilder, caseData);
        assistedFormOrderBuilder = appealInitiativeGroup.populateInitiativeOrWithoutNoticeDetails(assistedFormOrderBuilder, caseData);

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
