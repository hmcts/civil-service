package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentSDOOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_SDO_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_TRIAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_R2_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_R2_TRIAL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DefaultJudgmentOrderFormGenerator.class,
    JacksonAutoConfiguration.class
})
public class DefaultJudgmentOrderFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static String fileNameDisposal = null;
    private static String FILE_NAME_DISPOSAL_HNL = null;
    private static String fileNameTrial = null;
    private static final CaseDocument CASE_DOCUMENT_DISPOSAL = CaseDocumentBuilder.builder()
        .documentName(fileNameDisposal)
        .documentType(DEFAULT_JUDGMENT_SDO_ORDER)
        .build();
    private static final CaseDocument CASE_DOCUMENT_TRIAL = CaseDocumentBuilder.builder()
        .documentName(fileNameTrial)
        .documentType(DEFAULT_JUDGMENT_SDO_ORDER)
        .build();
    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private DocumentHearingLocationHelper documentHearingLocationHelper;

    @MockBean
    private IdamClient idamClient;

    @Autowired
    private DefaultJudgmentOrderFormGenerator generator;

    @BeforeEach
    void setUp() {
        fileNameDisposal = LocalDate.now() + "_Judge Dredd" + ".pdf";
        fileNameTrial = LocalDate.now() + "_Judge Dredd" + ".pdf";
        FILE_NAME_DISPOSAL_HNL = LocalDate.now() + "_Judge Dredd" + ".pdf";

        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .forename("Judge")
                                                                    .surname("Dredd")
                                                                    .roles(Collections.emptyList()).build());
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateSdoTrialDj()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenValidDataIsProvidedAndTelephoneHearing() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOTelephoneHearing()
            .atStateSdoTrialDj()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenValidDataIsProvidedAndVidoeHearing() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOVideoHearing()
            .atStateSdoTrialDj()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgementDisposalFormGenerator_HnlFieldsWhenToggled() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                                                               eq(DJ_SDO_DISPOSAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_DISPOSAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DISPOSAL_HNL, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_DISPOSAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedDisposalHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedDisposalSDOVideoCall()
            .atStateClaimIssuedDisposalHearingInPersonDJ()
            .atStateDisposalHearingOrderMadeWithoutHearing()
            .build();
        LocationRefData locationRefData = LocationRefData.builder().build();
        Mockito.when(documentHearingLocationHelper.getHearingLocation(
            nullable(String.class), eq(caseData), eq(BEARER_TOKEN)
        )).thenReturn(locationRefData);
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DISPOSAL_HNL, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject arg) ->
                arg instanceof DefaultJudgmentSDOOrderForm
                    && locationRefData.equals(((DefaultJudgmentSDOOrderForm) arg).getHearingLocation())
            ),
            any(DocmosisTemplates.class)
        );
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenNoticeOfChangeEnabled() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateSdoTrialDj()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenSdoR2Enabled() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_R2_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_R2_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateSdoTrialDj()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgmentDisposalOrderFormGenerator_whenSdoR2Enabled() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_R2_DISPOSAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_R2_DISPOSAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_DISPOSAL);
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedDisposalHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedDisposalSDOVideoCall()
            .atStateClaimIssuedDisposalHearingInPersonDJ()
            .atStateDisposalHearingOrderMadeWithoutHearing()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Nested
    class GetDisposalHearingBundleTypeText {
        @Test
        void shouldReturnText_whenAllThreeTypesSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.DOCUMENTS,
                DisposalHearingBundleType.ELECTRONIC,
                DisposalHearingBundleType.SUMMARY
            );

            DisposalHearingBundleDJ disposalHearingBundle = DisposalHearingBundleDJ.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundleDJ(disposalHearingBundle)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / an electronic bundle of digital documents"
                + " / a case summary containing no more than 500 words";

            assertThat(DefaultJudgmentOrderFormGenerator.fillTypeBundleInfo(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenDocumentsAndElectronicTypesSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.DOCUMENTS,
                DisposalHearingBundleType.ELECTRONIC
            );

            DisposalHearingBundleDJ disposalHearingBundle = DisposalHearingBundleDJ.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundleDJ(disposalHearingBundle)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / an electronic bundle of digital documents";

            assertThat(DefaultJudgmentOrderFormGenerator.fillTypeBundleInfo(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenDocumentsAndSummaryTypesSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.DOCUMENTS,
                DisposalHearingBundleType.SUMMARY
            );

            DisposalHearingBundleDJ disposalHearingBundle = DisposalHearingBundleDJ.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundleDJ(disposalHearingBundle)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / a case summary containing no more than 500 words";

            assertThat(DefaultJudgmentOrderFormGenerator.fillTypeBundleInfo(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenElectronicAndSummaryTypesSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.ELECTRONIC,
                DisposalHearingBundleType.SUMMARY
            );

            DisposalHearingBundleDJ disposalHearingBundle = DisposalHearingBundleDJ.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundleDJ(disposalHearingBundle)
                .build();

            String expectedText = "an electronic bundle of digital documents"
                + " / a case summary containing no more than 500 words";

            assertThat(DefaultJudgmentOrderFormGenerator.fillTypeBundleInfo(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenOnlyDocumentsTypeSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.DOCUMENTS
            );

            DisposalHearingBundleDJ disposalHearingBundle = DisposalHearingBundleDJ.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundleDJ(disposalHearingBundle)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered";

            assertThat(DefaultJudgmentOrderFormGenerator.fillTypeBundleInfo(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenOnlyElectronicTypeSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.ELECTRONIC
            );

            DisposalHearingBundleDJ disposalHearingBundle = DisposalHearingBundleDJ.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundleDJ(disposalHearingBundle)
                .build();

            String expectedText = "an electronic bundle of digital documents";

            assertThat(DefaultJudgmentOrderFormGenerator.fillTypeBundleInfo(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenOnlySummaryTypeSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.SUMMARY
            );

            DisposalHearingBundleDJ disposalHearingBundle = DisposalHearingBundleDJ.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundleDJ(disposalHearingBundle)
                .build();

            String expectedText = "a case summary containing no more than 500 words";

            assertThat(DefaultJudgmentOrderFormGenerator.fillTypeBundleInfo(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnEmptyString_whenNoTypesSelected() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(DefaultJudgmentOrderFormGenerator.fillTypeBundleInfo(caseData)).isEqualTo("");
        }
    }

}
