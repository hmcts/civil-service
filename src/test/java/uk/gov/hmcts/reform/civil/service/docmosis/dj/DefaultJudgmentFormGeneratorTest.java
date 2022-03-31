package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.DEFAULT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N11;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DefaultJudgmentFormGenerator.class,
    JacksonAutoConfiguration.class
})
public class DefaultJudgmentFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = String.format(N121.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(DEFAULT_JUDGMENT)
        .build();
    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @Autowired
    private DefaultJudgmentFormGenerator generator;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private FeesService feesService;

    @MockBean
    private InterestCalculator interestCalculator;

    @Test
    void shouldDefaultJudgmentFormGeneratorOneForm_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121)))
            .thenReturn(new DocmosisDocument(N11.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM.name());

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT));

    }

    @Test
    void shouldDefaultJudgmentFormGeneratorTwoForms_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121)))
            .thenReturn(new DocmosisDocument(N11.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .totalClaimAmount(new BigDecimal(2000))
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM.name());

        assertThat(caseDocuments.size()).isEqualTo(2);
    }

}
