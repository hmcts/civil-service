package uk.gov.hmcts.reform.unspec.service.docmosis.cos;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.enums.ServiceLocationType;
import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServiceLocation;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.docmosis.cos.CertificateOfServiceForm;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.enums.ServedDocuments.OTHER;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.CERTIFICATE_OF_SERVICE;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N215;
import static uk.gov.hmcts.reform.unspec.utils.DocmosisTemplateDataUtils.fetchApplicantName;
import static uk.gov.hmcts.reform.unspec.utils.DocmosisTemplateDataUtils.fetchRespondentName;
import static uk.gov.hmcts.reform.unspec.utils.DocmosisTemplateDataUtils.fetchSolicitorReferences;
import static uk.gov.hmcts.reform.unspec.utils.DocmosisTemplateDataUtils.toCaseName;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    CertificateOfServiceGenerator.class,
    JacksonAutoConfiguration.class
})
class CertificateOfServiceGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000LR001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = format(N215.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(CERTIFICATE_OF_SERVICE)
        .build();

    @MockBean
    private DocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @Autowired
    private CertificateOfServiceGenerator generator;

    @Test
    void shouldGenerateCertificateOfService_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), eq(N215)))
            .thenReturn(new DocmosisDocument(N215.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, CERTIFICATE_OF_SERVICE)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, CERTIFICATE_OF_SERVICE));
        verify(documentGeneratorService)
            .generateDocmosisDocument(any(CertificateOfServiceForm.class), eq(N215));
    }

    @Nested
    class PrepareServedLocation {

        @ParameterizedTest
        @EnumSource(value = ServiceLocationType.class, names = {"BUSINESS", "RESIDENCE"})
        void shouldPrepareLocation_whenLocationTypeIsNotOther(ServiceLocationType type) {
            ServiceLocation serviceLocation = ServiceLocation
                .builder()
                .location(type)
                .build();

            String location = generator.prepareServedLocation(serviceLocation);

            assertEquals(type.getLabel(), location);
        }

        @Test
        void shouldPrepareLocation_whenLocationTypeIsOther() {
            ServiceLocation serviceLocation = ServiceLocation
                .builder()
                .location(ServiceLocationType.OTHER)
                .build();

            String location = generator.prepareServedLocation(serviceLocation);

            assertEquals(ServiceLocationType.OTHER.getLabel() + " - " + serviceLocation.getOther(), location);
        }

        @Test
        void shouldReturnNull_whenLocationIsNull() {
            assertThat(generator.prepareServedLocation(null)).isNull();
        }
    }

    @Nested
    class PrepareDocumentList {

        @ParameterizedTest
        @EnumSource(
            value = ServedDocuments.class,
            names = {"OTHER"},
            mode = EnumSource.Mode.EXCLUDE

        )
        void shouldPrepareDocument_whenServedDocumentIsNotOthers(ServedDocuments servedDocument) {
            List<ServedDocuments> servedDocuments = List.of(servedDocument);

            String documentList = generator.prepareDocumentList(servedDocuments, "");

            assertEquals(servedDocument.getLabel(), documentList);
        }

        @Test
        void shouldPrepareDocument_whenServedDocumentsIncludeOthers() {
            List<ServedDocuments> servedDocuments = List.of(ServedDocuments.CLAIM_FORM, ServedDocuments.OTHER);

            String documentList = generator.prepareDocumentList(servedDocuments, "Some other");

            assertEquals(ServedDocuments.CLAIM_FORM.getLabel() + "\n" + "Other - Some other", documentList);
        }
    }

    @Nested
    class GetTemplateData {

        @Test
        void whenCaseIsAtStateServiceConfirmed_shouldGetCertificateOfServiceData() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();

            var templateData = generator.getTemplateData(caseData);

            assertThatFieldsAreCorrect(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect(CertificateOfServiceForm templateData, CaseData caseData) {
            Assertions.assertAll(
                "CertificateOfService data should be as expected",
                () -> assertEquals(templateData.getCaseName(), toCaseName.apply(caseData)),
                () -> assertEquals(templateData.getReferenceNumber(), caseData.getLegacyCaseReference()),
                () -> assertEquals(
                    templateData.getSolicitorReferences(),
                    fetchSolicitorReferences(caseData.getSolicitorReferences())
                ),
                () -> assertEquals(templateData.getDateServed(), caseData.getServiceDateToRespondentSolicitor1()),
                () -> assertEquals(
                    templateData.getDeemedDateOfService(),
                    caseData.getDeemedServiceDateToRespondentSolicitor1()
                ),
                () -> assertEquals(templateData.getApplicantName(), fetchApplicantName(caseData)),
                () -> assertEquals(templateData.getRespondentName(), fetchRespondentName(caseData)),
                () -> assertEquals(
                    templateData.getServiceMethod(),
                    caseData.getServiceMethodToRespondentSolicitor1().getType().getLabel()
                ),
                () -> assertEquals(
                    templateData.getOnWhomServed(),
                    caseData.getServiceNamedPersonToRespondentSolicitor1()
                ),
                () -> assertEquals(
                    templateData.getServedLocation(),
                    prepareServedLocation(caseData.getServiceLocationToRespondentSolicitor1())
                ),
                () -> assertEquals(
                    templateData.getDocumentsServed(),
                    prepareDocumentList(caseData.getServedDocuments(), caseData.getServedDocumentsOther())
                ),
                () -> assertEquals(
                    templateData.getStatementOfTruth(),
                    caseData.getApplicantSolicitor1ClaimStatementOfTruth()
                ),
                () -> assertEquals(templateData.getApplicantRepresentative(), getRepresentative()),
                () -> assertEquals(templateData.getRespondentRepresentative(), getRepresentative())
            );
        }

        private Representative getRepresentative() {
            return Representative.builder()
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
                .build();
        }

        private String prepareServedLocation(ServiceLocation serviceLocation) {
            if (serviceLocation == null) {
                return null;
            }
            if (serviceLocation.getLocation() == ServiceLocationType.OTHER) {
                return ServiceLocationType.OTHER.getLabel() + " - " + serviceLocation.getOther();
            }
            return serviceLocation.getLocation().getLabel();
        }

        private String prepareDocumentList(List<ServedDocuments> servedDocuments, String otherServedDocuments) {
            String withoutOther = servedDocuments.stream()
                .filter(doc -> doc != OTHER)
                .map(ServedDocuments::getLabel)
                .collect(Collectors.joining("\n"));

            return servedDocuments.contains(OTHER) ? withoutOther + "\nOther - " + otherServedDocuments : withoutOther;
        }
    }
}
