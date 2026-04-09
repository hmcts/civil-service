package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.CourtOfficerOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.time.LocalDate;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.config.JacksonConfiguration.DATE_FORMAT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.COURT_OFFICER_ORDER_PDF;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtOfficerOrderGenerator implements TemplateDataGenerator<CourtOfficerOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final LocationReferenceDataService locationRefDataService;
    private LocationRefData caseManagementLocationDetails;
    private final DocumentHearingLocationHelper documentHearingLocationHelper;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        CourtOfficerOrderForm templateData = getCourtOfficerData(caseData, authorisation);
        DocmosisTemplates docmosisTemplate = COURT_OFFICER_ORDER_PDF;

        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(docmosisTemplate),
                docmosisDocument.getBytes(),
                DocumentType.COURT_OFFICER_ORDER
            )
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        return format(docmosisTemplate.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    }

    private CourtOfficerOrderForm getCourtOfficerData(CaseData caseData, String authorisation) {
        caseManagementLocationDetails = documentHearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);

        return new CourtOfficerOrderForm(
            caseData.getCcdCaseReference().toString(),
            caseData.getApplicant1().getPartyName(),
            nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null,
            caseData.getRespondent1().getPartyName(),
            nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null,
            nonNull(caseData.getApplicant2()) ? "Claimant 1" : "Claimant",
            nonNull(caseData.getRespondent2()) ? "Defendant 1" : "Defendant",
            null,
            null,
            getHearingLocationText(caseData),
            caseManagementLocationDetails.getExternalShortName(),
            caseData.getCourtOfficerOrdered()
        );
    }

    private String getHearingLocationText(CaseData caseData) {
        return caseData.getHearingLocationText() != null ? caseData.getHearingLocationText()
            : LocationReferenceDataService.getDisplayEntry(caseManagementLocationDetails);
    }

}
