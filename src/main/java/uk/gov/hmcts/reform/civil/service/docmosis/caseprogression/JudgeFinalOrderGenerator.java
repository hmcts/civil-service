package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.JudgeFinalOrderFormPopulator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection.FREE_FORM_ORDER;
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
    private final DocumentHearingLocationHelper documentHearingLocationHelper;
    private LocationRefData caseManagementLocationDetails;
    private final JudgeFinalOrderFormPopulator judgeFinalOrderFormPopulator;

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    public CaseDocument generate(CaseData caseData, String authorisation) {
        JudgeFinalOrderForm templateData = getFinalOrderType(caseData, authorisation);
        DocmosisTemplates docmosisTemplate = caseData.getFinalOrderSelection().equals(FREE_FORM_ORDER)
            ? FREE_FORM_ORDER_PDF
            : ASSISTED_ORDER_PDF;

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

        return judgeFinalOrderFormPopulator.populateFreeFormOrder(caseData, caseManagementLocationDetails, userDetails);
    }

    private JudgeFinalOrderForm getAssistedOrder(CaseData caseData, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        caseManagementLocationDetails = documentHearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);

        return judgeFinalOrderFormPopulator.populateFinalOrderForm(caseData, caseManagementLocationDetails, userDetails);
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

}
