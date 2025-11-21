package uk.gov.hmcts.reform.civil.ga.service.docmosis.consentorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.ConsentOrderForm;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.CONSENT_ORDER_FORM;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentOrderGenerator implements TemplateDataGenerator<ConsentOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocmosisService docmosisService;

    @Override
    public ConsentOrderForm getTemplateData(GeneralApplicationCaseData caseData, String authorisation) {

        ConsentOrderForm.ConsentOrderFormBuilder consentOrderFormBuilder =
            ConsentOrderForm.builder()
                .claimNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
                .isMultiParty(caseData.getIsMultiParty())
                .claimant1Name(caseData.getClaimant1PartyName())
                .claimant2Name(caseData.getClaimant2PartyName() != null ? caseData.getClaimant2PartyName() : null)
                .defendant1Name(caseData.getDefendant1PartyName())
                .defendant2Name(caseData.getDefendant2PartyName() != null ? caseData.getDefendant2PartyName() : null)
                .orderDate(LocalDate.now())
                .courtName(docmosisService.getCaseManagementLocationVenueName(
                    caseData,
                    authorisation
                ).getExternalShortName())
                .siteName(caseData.getCaseManagementLocation().getSiteName())
                .address(caseData.getCaseManagementLocation().getAddress())
                .postcode(caseData.getCaseManagementLocation().getPostcode())
                .consentOrder(caseData.getApproveConsentOrder()
                                  .getConsentOrderDescription());

        return consentOrderFormBuilder.build();
    }

    protected String getDateFormatted(LocalDate date) {
        if (isNull(date)) {
            return null;
        }
        return DateFormatHelper.formatLocalDate(date, " d MMMM yyyy");
    }

    public CaseDocument generate(GeneralApplicationCaseData caseData, String authorisation) {
        ConsentOrderForm templateData = getTemplateData(caseData, authorisation);

        DocmosisTemplates docmosisTemplate = getDocmosisTemplate();

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );

        log.info("Generate consent order for caseId: {}", caseData.getCcdCaseReference());

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(docmosisTemplate), docmosisDocument.getBytes(),
                DocumentType.CONSENT_ORDER
            )
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(docmosisTemplate.getDocumentTitle(), LocalDateTime.now().format(formatter));
    }

    private DocmosisTemplates getDocmosisTemplate() {
        return CONSENT_ORDER_FORM;
    }

}
