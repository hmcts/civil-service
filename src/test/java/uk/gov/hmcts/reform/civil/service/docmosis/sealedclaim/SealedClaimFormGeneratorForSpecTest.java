package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimFormForSpec;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_1V2_DIFFERENT_SOL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_1V2_SAME_SOL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_2V1;

@ExtendWith(SpringExtension.class)
public class SealedClaimFormGeneratorForSpecTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = format(N1.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(SEALED_CLAIM)
        .build();

    private final Representative representative1 = Representative.builder().organisationName("test org").build();
    private final Representative representative2 = Representative.builder().organisationName("test org2").build();

    @InjectMocks
    private SealedClaimFormGeneratorForSpec sealedClaimFormGenerator;

    @Mock
    private RepresentativeService representativeService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private InterestCalculator interestCalculator;
    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @BeforeEach
    void setup() {
        when(representativeService.getRespondent1Representative(any())).thenReturn(representative1);
        when(representativeService.getRespondent2Representative(any())).thenReturn(representative2);
        when(representativeService.getApplicantRepresentative(any())).thenReturn(getRepresentative());
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.ZERO);
    }

    private Representative getRepresentative() {
        return Representative.builder()
            .organisationName("MiguelSpooner")
            .dxAddress("DX 751Newport")
            .organisationName("DBE Law")
            .emailAddress("jim.smith@slatergordon.com")
            .serviceAddress(Address.builder()
                                .addressLine1("AdmiralHouse")
                                .addressLine2("Queensway")
                                .postTown("Newport")
                                .postCode("NP204AG")
                                .build())
            .build();
    }

    @Test
    void generateSealedClaimForm1v1() {
        CaseData.CaseDataBuilder caseBuilder = getBaseCaseDataBuilder();
        CaseData caseData = caseBuilder
            .build();

        when(deadlinesCalculator.calculateFirstWorkingDay(caseData.getIssueDate().plusDays(14)))
            .thenReturn(caseData.getIssueDate().plusDays(17));
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2)))
            .thenReturn(new DocmosisDocument(N2.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(any(SealedClaimFormForSpec.class), eq(N2));
    }

    @Test
    void generateSealedClaimForm1v2SameSolicitor() {
        CaseData.CaseDataBuilder caseBuilder = getBaseCaseDataBuilder();
        CaseData caseData = caseBuilder
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .partyName("name")
                             .build())
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .build();

        when(deadlinesCalculator.calculateFirstWorkingDay(caseData.getIssueDate().plusDays(14)))
            .thenReturn(caseData.getIssueDate().plusDays(17));
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2_1V2_SAME_SOL)))
            .thenReturn(new DocmosisDocument(N2_1V2_SAME_SOL.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService, times(2)).getRespondent1Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(SealedClaimFormForSpec.class),
            eq(N2_1V2_SAME_SOL)
        );
    }

    @Test
    void generateSealedClaimForm1v2DifferentSolicitor() {
        CaseData.CaseDataBuilder caseBuilder = getBaseCaseDataBuilder();
        CaseData caseData = caseBuilder
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .partyName("name")
                             .build())
            .build();

        when(deadlinesCalculator.calculateFirstWorkingDay(caseData.getIssueDate().plusDays(14)))
            .thenReturn(caseData.getIssueDate().plusDays(17));
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2_1V2_DIFFERENT_SOL)))
            .thenReturn(new DocmosisDocument(N2_1V2_DIFFERENT_SOL.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(representativeService).getRespondent2Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(SealedClaimFormForSpec.class),
            eq(N2_1V2_DIFFERENT_SOL)
        );
    }

    @Test
    void generateSealedClaimForm2v1() {
        CaseData.CaseDataBuilder caseBuilder = getBaseCaseDataBuilder();
        CaseData caseData = caseBuilder
            .applicant2(Party.builder()
                            .type(Party.Type.COMPANY)
                            .partyName("name")
                            .build())
            .build();

        when(deadlinesCalculator.calculateFirstWorkingDay(caseData.getIssueDate().plusDays(14)))
            .thenReturn(caseData.getIssueDate().plusDays(17));
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2_2V1)))
            .thenReturn(new DocmosisDocument(N2_2V1.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(any(SealedClaimFormForSpec.class), eq(N2_2V1));
    }

    private CaseData.CaseDataBuilder getBaseCaseDataBuilder() {
        return CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .totalClaimAmount(BigDecimal.valueOf(850_00))
            .claimFee(Fee.builder()
                          .calculatedAmountInPence(BigDecimal.valueOf(70_00))
                          .build());
    }
}
