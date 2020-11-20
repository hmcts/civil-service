package uk.gov.hmcts.reform.unspec.service.docmosis.cos;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.enums.ServiceLocationType;
import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServiceLocation;
import uk.gov.hmcts.reform.unspec.model.SolicitorReferences;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.docmosis.cos.CertificateOfServiceForm;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.DocumentType;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.unspec.utils.CaseNameUtils;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.unspec.enums.ServedDocuments.OTHER;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N215;

@Service
@RequiredArgsConstructor
public class CertificateOfServiceGenerator implements TemplateDataGenerator<CertificateOfServiceForm> {

    private static final Representative TEMP_REPRESENTATIVE = Representative.builder()
        .contactName("MiguelSpooner")
        .dxAddress("DX 751Newport")
        .organisationName("DBE Law")
        .phoneNumber("0800 206 1592")
        .emailAddress("jim.smith@slatergordon.com")
        .serviceAddress(Address.builder()
                            .addressLine1("AdmiralHouse")
                            .addressLine2("Queensway")
                            .postTown("Newport")
                            .postCode("NP204AG")
                            .build())
        .build(); //TODO Rep details need to be picked from reference data

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        CertificateOfServiceForm templateData = getTemplateData(caseData);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, N215);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData), docmosisDocument.getBytes(), DocumentType.CERTIFICATE_OF_SERVICE)
        );
    }

    private String getFileName(CaseData caseData) {
        return String.format(N215.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public CertificateOfServiceForm getTemplateData(CaseData caseData) {
        return CertificateOfServiceForm.builder()
            .caseName(CaseNameUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(prepareSolicitorReferences(caseData.getSolicitorReferences()))
            .dateServed(caseData.getServiceDateToRespondentSolicitor1())
            .deemedDateOfService(caseData.getDeemedServiceDateToRespondentSolicitor1())
            .applicantName(CaseNameUtils.fetchApplicantName(caseData))
            .respondentName(CaseNameUtils.fetchRespondentName(caseData))
            .respondentRepresentative(TEMP_REPRESENTATIVE)
            .serviceMethod(caseData.getServiceMethodToRespondentSolicitor1().getType().getLabel())
            .onWhomServed(caseData.getServiceNamedPersonToRespondentSolicitor1())
            .servedLocation(prepareServedLocation(caseData.getServiceLocationToRespondentSolicitor1()))
            .documentsServed(prepareDocumentList(caseData.getServedDocuments(), caseData.getServedDocumentsOther()))
            .statementOfTruth(caseData.getApplicantSolicitor1ClaimStatementOfTruth())
            .applicantRepresentative(TEMP_REPRESENTATIVE)
            .build();
    }

    public SolicitorReferences prepareSolicitorReferences(SolicitorReferences solicitorReferences) {
        return SolicitorReferences
            .builder()
            .applicantSolicitor1Reference(
                ofNullable(solicitorReferences)
                    .map(SolicitorReferences::getApplicantSolicitor1Reference)
                    .orElse("Not Provided"))
            .respondentSolicitor1Reference(
                ofNullable(solicitorReferences)
                    .map(SolicitorReferences::getRespondentSolicitor1Reference)
                    .orElse("Not Provided"))
            .build();
    }

    public String prepareServedLocation(ServiceLocation serviceLocation) {
        if (serviceLocation == null) {
            return null;
        }
        if (serviceLocation.getLocation() == ServiceLocationType.OTHER) {
            return ServiceLocationType.OTHER.getLabel() + " - " + serviceLocation.getOther();
        }
        return serviceLocation.getLocation().getLabel();
    }

    public String prepareDocumentList(List<ServedDocuments> servedDocuments, String otherServedDocuments) {
        String withoutOther = servedDocuments.stream()
            .filter(doc -> doc != OTHER)
            .map(ServedDocuments::getLabel)
            .collect(Collectors.joining("\n"));

        return servedDocuments.contains(OTHER) ? withoutOther + "\nOther - " + otherServedDocuments : withoutOther;
    }
}
