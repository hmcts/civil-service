package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FixedCosts;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimFormForSpec;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_1V2_DIFFERENT_SOL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_1V2_DIFFERENT_SOL_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_1V2_SAME_SOL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_2V1;

@ExtendWith(SpringExtension.class)
class  SealedClaimFormGeneratorForSpecTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String FILE_NAME  = format(N1.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(FILE_NAME)
        .documentType(SEALED_CLAIM)
        .build();

    private final Representative representative1 = new Representative().setOrganisationName("test org");
    private final Representative representative2 = new Representative().setOrganisationName("test org2");

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
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(representativeService.getRespondent1Representative(any())).thenReturn(representative1);
        when(representativeService.getRespondent2Representative(any())).thenReturn(representative2);
        when(representativeService.getApplicantRepresentative(any())).thenReturn(getRepresentative());
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.ZERO);
        when(interestCalculator.getInterestPerDayBreakdown(any(CaseData.class))).thenReturn("Interest will accrue at the daily rate of £0.50 up to the date of claim issue");
    }

    private Representative getRepresentative() {
        Address serviceAddress = new Address();
        serviceAddress.setAddressLine1("AdmiralHouse");
        serviceAddress.setAddressLine2("Queensway");
        serviceAddress.setPostTown("Newport");
        serviceAddress.setPostCode("NP204AG");

        return new Representative()
            .setOrganisationName("MiguelSpooner")
            .setDxAddress("DX 751Newport")
            .setOrganisationName("DBE Law")
            .setEmailAddress("jim.smith@slatergordon.com")
            .setServiceAddress(serviceAddress);
    }

    @Test
    void generateSealedClaimForm1v1() {
        CaseData.CaseDataBuilder caseBuilder = getBaseCaseDataBuilder();
        CaseData caseData = caseBuilder
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2)))
            .thenReturn(new DocmosisDocument(N2.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM));
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
            .submittedDate(LocalDateTime.now().minusDays(30))
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2_1V2_SAME_SOL)))
            .thenReturn(new DocmosisDocument(N2_1V2_SAME_SOL.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull();

        verify(representativeService, times(2)).getRespondent1Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM));
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

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2_1V2_DIFFERENT_SOL)))
            .thenReturn(new DocmosisDocument(N2_1V2_DIFFERENT_SOL.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(representativeService).getRespondent2Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(SealedClaimFormForSpec.class),
            eq(N2_1V2_DIFFERENT_SOL)
        );
    }

    @Test
    void generateSealedClaimForm1v2Respondent1LIP() {
        CaseData.CaseDataBuilder caseBuilder = getBaseCaseDataBuilder();
        CaseData caseData = caseBuilder
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .partyName("name")
                             .build())
            .specRespondent1Represented(YesOrNo.NO)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2_1V2_DIFFERENT_SOL_LIP)))
            .thenReturn(new DocmosisDocument(N2_1V2_DIFFERENT_SOL_LIP.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(representativeService).getRespondent2Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(SealedClaimFormForSpec.class),
            eq(N2_1V2_DIFFERENT_SOL_LIP)
        );
    }

    @Test
    void generateSealedClaimForm1v2Respondent2LIP() {
        CaseData.CaseDataBuilder caseBuilder = getBaseCaseDataBuilder();
        CaseData caseData = caseBuilder
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .partyName("name")
                             .build())
            .specRespondent2Represented(YesOrNo.NO)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2_1V2_DIFFERENT_SOL_LIP)))
            .thenReturn(new DocmosisDocument(N2_1V2_DIFFERENT_SOL_LIP.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(representativeService).getRespondent2Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(SealedClaimFormForSpec.class),
            eq(N2_1V2_DIFFERENT_SOL_LIP)
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

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2_2V1)))
            .thenReturn(new DocmosisDocument(N2_2V1.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(any(SealedClaimFormForSpec.class), eq(N2_2V1));
    }

    private CaseData.CaseDataBuilder getBaseCaseDataBuilder() {
        return CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .totalClaimAmount(BigDecimal.valueOf(850_00))
            .claimFee(new Fee()
                          .setCalculatedAmountInPence(BigDecimal.valueOf(70_00))
                          );
    }

    private CaseData.CaseDataBuilder getCaseDataBuilderWithAllDetails() {
        List<TimelineOfEvents> timelines = new ArrayList<>();
        timelines.add(new TimelineOfEvents(
            new TimelineOfEventDetails(LocalDate.now(), "test timeline"),
            null
        ));

        return CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .totalClaimAmount(BigDecimal.valueOf(850_00))
            .claimFee(new Fee()
                          .setCalculatedAmountInPence(BigDecimal.valueOf(70_00))
                          )
            .timelineOfEvents(timelines)
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .sameRateInterestSelection(buildSameRateSelection(new BigDecimal(100), "test"))
            .interestFromSpecificDate(LocalDate.now())
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .fixedCosts(new FixedCosts()
                            .setClaimFixedCosts(YesOrNo.YES)
                            .setFixedCostAmount("2000")
                            )
            .breakDownInterestDescription("test breakdown desc");
    }

    @Test
    void generateSealedClaimForm1v1WithAllFields() {

        CaseData.CaseDataBuilder caseBuilder = getCaseDataBuilderWithAllDetails();
        CaseData caseData = caseBuilder
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2)))
            .thenReturn(new DocmosisDocument(N2.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(any(SealedClaimFormForSpec.class), eq(N2));
    }

    @Test
    void generateSealedClaimForm1v1_whenBulkClaimNoInterest() {
        when(featureToggleService.isBulkClaimEnabled()).thenReturn(true);
        CaseData.CaseDataBuilder<?, ?> caseBuilder = getBaseCaseDataBuilder();
        CaseData caseData = caseBuilder
            .specRespondent1Represented(YesOrNo.NO)
            .sdtRequestIdFromSdt("1234")
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2)))
            .thenReturn(new DocmosisDocument(N2.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);
        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(any(SealedClaimFormForSpec.class), eq(N2));
    }

    @Test
    void generateSealedClaimForm1v1_whenBulkClaimWithInterest() {
        when(featureToggleService.isBulkClaimEnabled()).thenReturn(true);
        CaseData.CaseDataBuilder<?, ?> caseBuilder = getBaseCaseDataBuilder();
        CaseData caseData = caseBuilder
            .specRespondent1Represented(YesOrNo.NO)
            .sdtRequestIdFromSdt("1234")
            .claimInterest(YesOrNo.YES)
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .sameRateInterestSelection(buildSameRateSelection(new BigDecimal(5), "Bulk Claim"))
            .interestFromSpecificDate(LocalDate.now().minusDays(10))
            .build();

        when(interestCalculator.calculateBulkInterest(any(CaseData.class))).thenReturn(BigDecimal.valueOf(5));
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N2)))
            .thenReturn(new DocmosisDocument(N2.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);
        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(any(SealedClaimFormForSpec.class), eq(N2));
    }

    private SameRateInterestSelection buildSameRateSelection(BigDecimal rate, String reason) {
        SameRateInterestSelection selection = new SameRateInterestSelection();
        selection.setDifferentRate(rate);
        selection.setDifferentRateReason(reason);
        return selection;
    }

}
