package uk.gov.hmcts.reform.unspec.service.docmosis.aos;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.docmosis.aos.AcknowledgementOfServiceForm;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Respondent;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.DocumentType;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.unspec.utils.DocmosisTemplateDataUtils;

import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N9;

@Service
@RequiredArgsConstructor
public class AcknowledgementOfServiceGenerator implements TemplateDataGenerator<AcknowledgementOfServiceForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        AcknowledgementOfServiceForm templateData = getTemplateData(caseData);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, N9);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData), docmosisDocument.getBytes(), DocumentType.ACKNOWLEDGEMENT_OF_SERVICE)
        );
    }

    private String getFileName(CaseData caseData) {
        return String.format(N9.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public AcknowledgementOfServiceForm getTemplateData(CaseData caseData) {
        return AcknowledgementOfServiceForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData.getSolicitorReferences()))
            .claimIssuedDate(caseData.getClaimIssuedDate())
            .responseDeadline(caseData.getRespondentSolicitor1ResponseDeadline().toLocalDate())
            .respondent(prepareRespondent(caseData.getRespondent1()))
            .build();
    }

    private Respondent prepareRespondent(Party respondent) {
        return Respondent.builder()
            .name(respondent.getPartyName())
            .primaryAddress(respondent.getPrimaryAddress())
            .build();
    }
}
