package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.LitigantInPersonForm;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.LITIGANT_IN_PERSON_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.LIP_CLAIM_FORM;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    LitigantInPersonFormGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class LitigantInPersonFormGeneratorTest {

    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final String fileName = String.format(LIP_CLAIM_FORM.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(LITIGANT_IN_PERSON_CLAIM_FORM)
        .build();

    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private LitigantInPersonFormGenerator litigantInPersonFormGenerator;

    @Test
    void shouldGenerateClaimForm_whenInvolked() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(LIP_CLAIM_FORM)))
            .thenReturn(new DocmosisDocument(LIP_CLAIM_FORM.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes,
                                                                            LITIGANT_IN_PERSON_CLAIM_FORM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePaymentSuccessful()
            .build();

        CaseDocument caseDocument = litigantInPersonFormGenerator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes,
                                                                               LITIGANT_IN_PERSON_CLAIM_FORM));
        verify(documentGeneratorService).generateDocmosisDocument(any(LitigantInPersonForm.class), eq(LIP_CLAIM_FORM));
    }
}
