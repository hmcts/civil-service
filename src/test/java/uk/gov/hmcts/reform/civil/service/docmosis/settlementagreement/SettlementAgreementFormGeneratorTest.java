package uk.gov.hmcts.reform.civil.service.docmosis.settlementagreement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.settlementagreement.SettlementAgreementForm;
import uk.gov.hmcts.reform.civil.model.docmosis.settlementagreement.SettlementAgreementFormMapper;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLEMENT_AGREEMENT_PDF;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SettlementAgreementFormGenerator.class,
    JacksonAutoConfiguration.class
})
public class SettlementAgreementFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String REFERENCE_NUMBER = "000MC014";
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private SettlementAgreementFormMapper settlementAgreementFormMapper;
    @Autowired
    private SettlementAgreementFormGenerator generator;

    @Test
    void shouldGenerateSettlementAgreementDoc_whenValidDataIsProvided() {
        String fileName = String.format(
                SETTLEMENT_AGREEMENT_PDF.getDocumentTitle(), REFERENCE_NUMBER);

        CaseDocument caseDocument = CaseDocumentBuilder.builder()
                .documentName(fileName)
                .documentType(SETTLEMENT_AGREEMENT)
                .build();

        when(documentGeneratorService.generateDocmosisDocument(any(SettlementAgreementForm.class), eq(SETTLEMENT_AGREEMENT_PDF)))
                .thenReturn(new DocmosisDocument(SETTLEMENT_AGREEMENT_PDF.getDocumentTitle(), bytes));

        when(documentManagementService
                .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SETTLEMENT_AGREEMENT)))
                .thenReturn(caseDocument);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .legacyCaseReference(REFERENCE_NUMBER)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .build();

        when(settlementAgreementFormMapper.buildFormData(caseData)).thenReturn(SettlementAgreementForm.builder().build());

        CaseDocument actual = generator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SETTLEMENT_AGREEMENT));
        assertThat(actual).isEqualTo(caseDocument);
    }
}
