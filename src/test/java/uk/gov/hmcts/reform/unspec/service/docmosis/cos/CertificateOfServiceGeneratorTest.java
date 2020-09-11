package uk.gov.hmcts.reform.unspec.service.docmosis.cos;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServiceLocation;
import uk.gov.hmcts.reform.unspec.model.SolicitorReferences;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;

import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.CERTIFICATE_OF_SERVICE;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N215;

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
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGenerateCertificateOfService_whenValidDataIsProvided() {

        when(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), eq(N215)))
            .thenReturn(new DocmosisDocument(N215.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(eq(BEARER_TOKEN), eq(new PDF(fileName, bytes, CERTIFICATE_OF_SERVICE))))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(documentManagementService)
            .uploadDocument(eq(BEARER_TOKEN), eq(new PDF(fileName, bytes, CERTIFICATE_OF_SERVICE)));
        verify(documentGeneratorService)
            .generateDocmosisDocument(any(DocmosisData.class), eq(N215));
    }

    @Nested
    class PrepareSolicitorReferences {

        @Test
        void shouldPopulateNotProvided_whenSolicitorReferencesMissing() {
            SolicitorReferences solicitorReferences = SolicitorReferences.builder().build();
            SolicitorReferences result = generator.prepareSolicitorReferences(solicitorReferences);
            assertAll(
                "SolicitorReferences not provided",
                () -> assertEquals("Not Provided", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor1Reference())
            );
        }

        @Test
        void shouldPopulateProvidedValues_whenSolicitorReferencesAvailable() {
            SolicitorReferences solicitorReferences = SolicitorReferences
                .builder()
                .applicantSolicitor1Reference("Claimant ref")
                .respondentSolicitor1Reference("Defendant ref")
                .build();

            SolicitorReferences result = generator.prepareSolicitorReferences(solicitorReferences);
            assertAll(
                "SolicitorReferences provided",
                () -> assertEquals("Claimant ref", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Defendant ref", result.getRespondentSolicitor1Reference())
            );
        }

        @Test
        void shouldPopulateNotProvided_whenOneReferencesNotAvailable() {
            SolicitorReferences solicitorReferences = SolicitorReferences
                .builder()
                .applicantSolicitor1Reference("Claimant ref")
                .build();

            SolicitorReferences result = generator.prepareSolicitorReferences(solicitorReferences);

            assertAll(
                "SolicitorReferences one is provided",
                () -> assertEquals("Claimant ref", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor1Reference())
            );
        }
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
}
