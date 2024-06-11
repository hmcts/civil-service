package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGMENT_BY_DETERMINATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_DETERMINATION_DEFENDANT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JudgmentByDeterminationDocGenerator.class,
    JacksonAutoConfiguration.class
})
public class JudgmentByDeterminationDocGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final CaseDocument CASE_DOCUMENT_CLAIMANT = CaseDocumentBuilder.builder()
        .documentName("Judgment_by_determination_claimant.pdf")
        .documentType(JUDGMENT_BY_DETERMINATION_CLAIMANT)
        .build();
    private static final CaseDocument CASE_DOCUMENT_DEFENDANT = CaseDocumentBuilder.builder()
        .documentName("Judgment_by_determination_defendant.pdf")
        .documentType(DocumentType.JUDGMENT_BY_DETERMINATION_DEFENDANT)
        .build();
    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @Autowired
    private JudgmentByDeterminationDocGenerator generator;

    @MockBean
    private OrganisationService organisationService;

    @Test
    void shouldDefaultJudgmentFormGeneratorOneFormClaimant() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT)))
            .thenReturn(new DocmosisDocument(DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_claimant.pdf", bytes,
                                                       JUDGMENT_BY_DETERMINATION_CLAIMANT)))
            .thenReturn(CASE_DOCUMENT_CLAIMANT);

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment()
            .toBuilder().applicant1(PartyBuilder.builder().soleTrader().build())
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .respondent2(PartyBuilder.builder().soleTrader().build())
            .build();
        List<CaseDocument> caseDocuments = generator.generateDocs(caseData, BEARER_TOKEN, GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT.name());

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_claimant.pdf", bytes, JUDGMENT_BY_DETERMINATION_CLAIMANT));

    }

    @Test
    void shouldDefaultJudgmentFormGeneratorDefendant() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                                                               eq(JUDGMENT_BY_DETERMINATION_DEFENDANT)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_DETERMINATION_DEFENDANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_defendant.pdf", bytes,
                                                       DocumentType.JUDGMENT_BY_DETERMINATION_DEFENDANT)))
            .thenReturn(CASE_DOCUMENT_DEFENDANT);

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment()
            .toBuilder().applicant1(PartyBuilder.builder().soleTrader().build())
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .respondent2(PartyBuilder.builder().soleTrader().build())
            .build();

        List<CaseDocument> caseDocuments = generator.generateDocs(caseData, BEARER_TOKEN,
                                                                  GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT.name());

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_defendant.pdf", bytes,
                                                  DocumentType.JUDGMENT_BY_DETERMINATION_DEFENDANT));

    }
}
