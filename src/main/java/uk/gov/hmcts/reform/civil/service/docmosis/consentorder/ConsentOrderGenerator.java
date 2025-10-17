package uk.gov.hmcts.reform.civil.service.docmosis.consentorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.ConsentOrderForm;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;

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
    private final GaCaseDataEnricher gaCaseDataEnricher;
    private final ObjectMapper objectMapper;

    @Override
    public ConsentOrderForm getTemplateData(CaseData caseData, String authorisation) {

        var caseLocation = caseData.getCaseManagementLocation();

        ConsentOrderForm.ConsentOrderFormBuilder consentOrderFormBuilder =
            ConsentOrderForm.builder()
                .claimNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
                .isMultiParty(caseData.getIsMultiParty())
                .claimant1Name(caseData.getClaimant1PartyName())
                .claimant2Name(caseData.getClaimant2PartyName() != null ? caseData.getClaimant2PartyName() : null)
                .defendant1Name(caseData.getDefendant1PartyName())
                .defendant2Name(caseData.getDefendant2PartyName() != null ? caseData.getDefendant2PartyName() : null)
                .orderDate(LocalDate.now())
                .courtName(docmosisService.getCaseManagementLocationVenueName(caseData, authorisation).getExternalShortName())
                .siteName(caseLocation != null ? caseLocation.getSiteName() : null)
                .address(caseLocation != null ? caseLocation.getAddress() : null)
                .postcode(caseLocation != null ? caseLocation.getPostcode() : null)
                .consentOrder(caseData.getApproveConsentOrder()
                                  .getConsentOrderDescription());

        return consentOrderFormBuilder.build();
    }

    public ConsentOrderForm getTemplateData(GeneralApplicationCaseData gaCaseData, String authorisation) {
        return getTemplateData(asCaseData(gaCaseData), authorisation);
    }

    protected String getDateFormatted(LocalDate date) {
        if (isNull(date)) {
            return null;
        }
        return DateFormatHelper.formatLocalDate(date, " d MMMM yyyy");
    }

    public CaseDocument generate(CaseData caseData, String authorisation) {
        ConsentOrderForm templateData = getTemplateData(caseData, authorisation);

        DocmosisTemplates docmosisTemplate = getDocmosisTemplate();

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );

        log.info("Generate consent order for caseId: {}", caseData.getCcdCaseReference());

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(docmosisTemplate), docmosisDocument.getBytes(),
                    DocumentType.CONSENT_ORDER)
        );
    }

    public CaseDocument generate(GeneralApplicationCaseData gaCaseData, String authorisation) {
        return generate(asCaseData(gaCaseData), authorisation);
    }

    private CaseData asCaseData(GeneralApplicationCaseData gaCaseData) {
        ObjectMapper mapperWithJavaTime = objectMapper.copy().registerModule(new JavaTimeModule());
        CaseData converted = mapperWithJavaTime.convertValue(gaCaseData, CaseData.class);
        return gaCaseDataEnricher.enrich(converted, gaCaseData);
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(docmosisTemplate.getDocumentTitle(), LocalDateTime.now().format(formatter));
    }

    private DocmosisTemplates getDocmosisTemplate() {
        return CONSENT_ORDER_FORM;
    }

}
