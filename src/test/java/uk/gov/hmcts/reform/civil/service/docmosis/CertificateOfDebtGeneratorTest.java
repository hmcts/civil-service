package uk.gov.hmcts.reform.civil.service.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.cosc.CertificateOfDebtForm;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.cosc.CertificateOfDebtGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.CERTIFICATE_OF_DEBT_PAYMENT;

@ExtendWith(MockitoExtension.class)
class CertificateOfDebtGeneratorTest {

    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private CertificateOfDebtGenerator certificateOfDebtGenerator;
    @Mock
    private LocationReferenceDataService locationRefDataService;
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private static final String REFERENCE_NUMBER = "000MC015";

    private static final String fileName = String.format(
        CERTIFICATE_OF_DEBT_PAYMENT.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument COSC_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(DocumentType.CERTIFICATE_OF_DEBT_PAYMENT)
        .build();

    @BeforeEach
    void setup() {
        certificateOfDebtGenerator = new CertificateOfDebtGenerator(documentManagementService, documentGeneratorService, locationRefDataService);
    }
    
    @Test
    void shouldGenerateCoscDocument() {

        //Given
        when(documentGeneratorService.generateDocmosisDocument(any(CertificateOfDebtForm.class), eq(CERTIFICATE_OF_DEBT_PAYMENT)))
            .thenReturn(new DocmosisDocument(CERTIFICATE_OF_DEBT_PAYMENT.getDocumentTitle(), LETTER_CONTENT));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, LETTER_CONTENT, DocumentType.CERTIFICATE_OF_DEBT_PAYMENT)))
            .thenReturn(COSC_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31DaysForCosc().toBuilder()
            .build();

        CaseDocument caseDoc = certificateOfDebtGenerator.generateDoc(caseData, BEARER_TOKEN);
        assertThat(caseDoc).isNotNull();

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, LETTER_CONTENT, DocumentType.CERTIFICATE_OF_DEBT_PAYMENT));
    }

    @Test
    void shouldGenerateCoscDocumentWhenJudgmentCancelled() {

        //Given
        when(documentGeneratorService.generateDocmosisDocument(any(CertificateOfDebtForm.class), eq(CERTIFICATE_OF_DEBT_PAYMENT)))
            .thenReturn(new DocmosisDocument(CERTIFICATE_OF_DEBT_PAYMENT.getDocumentTitle(), LETTER_CONTENT));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, LETTER_CONTENT, DocumentType.CERTIFICATE_OF_DEBT_PAYMENT)))
            .thenReturn(COSC_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseWithMarkJudgementPaidWithin31DaysForCosc().toBuilder()
            .build();

        CaseDocument caseDoc = certificateOfDebtGenerator.generateDoc(caseData, BEARER_TOKEN);
        assertThat(caseDoc).isNotNull();

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, LETTER_CONTENT, DocumentType.CERTIFICATE_OF_DEBT_PAYMENT));
    }
}
