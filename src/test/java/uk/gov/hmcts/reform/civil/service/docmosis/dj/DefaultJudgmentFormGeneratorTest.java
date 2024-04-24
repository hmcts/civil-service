package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT2;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_DEFENDANT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DefaultJudgmentFormGenerator.class,
    JacksonAutoConfiguration.class
})
public class DefaultJudgmentFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = String.format(N121_SPEC.getDocumentTitle(), REFERENCE_NUMBER);
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
    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldDefaultJudgmentFormGeneratorOneForm_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC)))
            .thenReturn(new DocmosisDocument(N121_SPEC.getDocumentTitle(), bytes));

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
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT));

    }

    @Test
    void shouldDefaultJudgmentFormGeneratorTwoForms_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC)))
            .thenReturn(new DocmosisDocument(N121_SPEC.getDocumentTitle(), bytes));

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
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments.size()).isEqualTo(2);
    }

    @Test
    void shouldGenerateClaimantDocsNonDivergent_whenValidDataIsProvided() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_CLAIMANT)))
            .thenReturn(new DocmosisDocument(N121_SPEC_CLAIMANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_CLAIMANT1)))
            .thenReturn(CASE_DOCUMENT);
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_CLAIMANT2)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .totalClaimAmount(new BigDecimal(2000))
            .addApplicant2(YesOrNo.YES)
            .applicant2(PartyBuilder.builder().individual().build())
            .build();
        List<CaseDocument> caseDocuments = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                              GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name());

        assertThat(caseDocuments).hasSize(2);
    }

    @Test
    void shouldGenerateDefendantDocsNonDivergent_whenValidDataIsProvided() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_DEFENDANT)))
            .thenReturn(new DocmosisDocument(N121_SPEC_DEFENDANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_DEFENDANT1)))
            .thenReturn(CASE_DOCUMENT);
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_DEFENDANT2)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .totalClaimAmount(new BigDecimal(2000))
            .build();
        List<CaseDocument> caseDocuments = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                              GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name());

        assertThat(caseDocuments).hasSize(2);
    }
}
