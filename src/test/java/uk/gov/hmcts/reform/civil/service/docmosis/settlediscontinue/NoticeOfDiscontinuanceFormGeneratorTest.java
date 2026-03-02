package uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PermissionGranted;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.settleanddiscontinue.NoticeOfDiscontinuanceForm;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.NOTICE_OF_DISCONTINUANCE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.NOTICE_OF_DISCONTINUANCE_BILINGUAL_PDF;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.NOTICE_OF_DISCONTINUANCE_PDF;

@ExtendWith(MockitoExtension.class)
class NoticeOfDiscontinuanceFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String REFERENCE_NUMBER = "000MC015";
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private NoticeOfDiscontinuanceFormGenerator formGenerator;

    @Test
    void shouldGenerateRespondent1NoticeOfDiscontinuanceDoc_whenValidDataIsProvided() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        String fileName = String.format(
                NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), REFERENCE_NUMBER);

        CaseDocument caseDocument = CaseDocumentBuilder.builder()
                .documentName(fileName)
                .documentType(NOTICE_OF_DISCONTINUANCE)
                .build();

        when(documentGeneratorService.generateDocmosisDocument(any(NoticeOfDiscontinuanceForm.class), eq(NOTICE_OF_DISCONTINUANCE_PDF)))
                .thenReturn(new DocmosisDocument(NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), bytes));

        when(documentManagementService
                .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE)))
                .thenReturn(caseDocument);

        CaseData caseData = getCaseData();

        CaseDocument caseDoc = formGenerator.generateDocs(caseData,
                                                          caseData.getRespondent1().getPartyName(),
                                                          caseData.getRespondent1().getPrimaryAddress(),
                                                          "party_type",
                                                          BEARER_TOKEN, false);
        assertThat(caseDoc).isNotNull();

        verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE));
    }

    @Test
    void shouldGenerateWelshDiscontinuanceDoc_whenValidDataIsProvided() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        String fileName = String.format(
            NOTICE_OF_DISCONTINUANCE_BILINGUAL_PDF.getDocumentTitle(), REFERENCE_NUMBER);

        CaseDocument caseDocument = CaseDocumentBuilder.builder()
                .documentName(fileName)
                .documentType(NOTICE_OF_DISCONTINUANCE)
                .build();

        when(documentGeneratorService.generateDocmosisDocument(any(NoticeOfDiscontinuanceForm.class), eq(NOTICE_OF_DISCONTINUANCE_BILINGUAL_PDF)))
                .thenReturn(new DocmosisDocument(NOTICE_OF_DISCONTINUANCE_BILINGUAL_PDF.getDocumentTitle(), bytes));

        when(documentManagementService
                .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE)))
                .thenReturn(caseDocument);

        CaseData caseData = getCaseData().toBuilder()
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQLanguage(new WelshLanguageRequirements()
                                                             .setCourt(Language.WELSH)))
            .build();

        CaseDocument caseDoc = formGenerator.generateDocs(caseData,
                                                          caseData.getRespondent1().getPartyName(),
                                                          caseData.getRespondent1().getPrimaryAddress(),
                                                          "party_type",
                                                          BEARER_TOKEN, false);
        assertThat(caseDoc).isNotNull();

        verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE));
    }

    @Test
    void shouldGenerateApplicant1NoticeOfDiscontinuanceDoc_whenValidDataIsProvided() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        String fileName = String.format(
                NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), REFERENCE_NUMBER);

        CaseDocument caseDocument = CaseDocumentBuilder.builder()
                .documentName(fileName)
                .documentType(NOTICE_OF_DISCONTINUANCE)
                .build();

        when(documentGeneratorService.generateDocmosisDocument(any(NoticeOfDiscontinuanceForm.class), eq(NOTICE_OF_DISCONTINUANCE_PDF)))
                .thenReturn(new DocmosisDocument(NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), bytes));

        when(documentManagementService
                .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE)))
                .thenReturn(caseDocument);

        CaseData caseData = getCaseData();

        CaseDocument caseDoc = formGenerator.generateDocs(caseData,
                                                          caseData.getApplicant1().getPartyName(),
                                                          caseData.getApplicant1().getPrimaryAddress(),
                                                          "party_type",
                                                          BEARER_TOKEN, false);
        assertThat(caseDoc).isNotNull();

        verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE));
    }

    @Test
    void shouldGenerateRespondent2NoticeOfDiscontinuanceDoc_whenValidDataIsProvided() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        String fileName = String.format(
                NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), REFERENCE_NUMBER);

        CaseDocument caseDocument = CaseDocumentBuilder.builder()
                .documentName(fileName)
                .documentType(NOTICE_OF_DISCONTINUANCE)
                .build();

        when(documentGeneratorService.generateDocmosisDocument(any(NoticeOfDiscontinuanceForm.class), eq(NOTICE_OF_DISCONTINUANCE_PDF)))
                .thenReturn(new DocmosisDocument(NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), bytes));

        when(documentManagementService
                .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE)))
                .thenReturn(caseDocument);

        CaseData caseData = getCaseData();

        CaseDocument caseDoc = formGenerator.generateDocs(caseData,
                                                          caseData.getRespondent2().getPartyName(),
                                                          caseData.getRespondent2().getPrimaryAddress(),
                                                          "party_type",
                                                          BEARER_TOKEN, false);
        assertThat(caseDoc).isNotNull();

        verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE));
    }

    @Test
    void shouldIncludePartyTypeInFilename_whenValidDataIsProvided() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        String fileName = String.format(
            NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), "party_type_" + REFERENCE_NUMBER);

        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(NOTICE_OF_DISCONTINUANCE)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(NoticeOfDiscontinuanceForm.class), eq(NOTICE_OF_DISCONTINUANCE_PDF)))
            .thenReturn(new DocmosisDocument(NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE)))
            .thenReturn(caseDocument);

        CaseData caseData = getCaseData();

        CaseDocument caseDoc = formGenerator.generateDocs(caseData,
                                                          caseData.getRespondent1().getPartyName(),
                                                          caseData.getRespondent1().getPrimaryAddress(),
                                                          "party_type",
                                                          BEARER_TOKEN, false);
        assertThat(caseDoc).isNotNull();

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE));
    }

    @Test
    void shouldIncludeQMPublicInformation_whenValidWithQMPublicIsOnAndWelsh() {
        when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        String fileName = String.format(
            NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), "party_type_" + REFERENCE_NUMBER);

        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(NOTICE_OF_DISCONTINUANCE)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(NoticeOfDiscontinuanceForm.class), eq(NOTICE_OF_DISCONTINUANCE_PDF)))
            .thenReturn(new DocmosisDocument(NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE)))
            .thenReturn(caseDocument);

        CaseData caseData = getCaseData();

        CaseDocument caseDoc = formGenerator.generateDocs(caseData,
                                                          caseData.getRespondent1().getPartyName(),
                                                          caseData.getRespondent1().getPrimaryAddress(),
                                                          "party_type",
                                                          BEARER_TOKEN, false);
        assertThat(caseDoc).isNotNull();

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE));
    }

    @Test
    void shouldIncludeQMPublicInformation_whenWhenIsNotLipAndWelshIsOff() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
        String fileName = String.format(
            NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), REFERENCE_NUMBER);

        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(NOTICE_OF_DISCONTINUANCE)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(NoticeOfDiscontinuanceForm.class), eq(NOTICE_OF_DISCONTINUANCE_PDF)))
            .thenReturn(new DocmosisDocument(NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE)))
            .thenReturn(caseDocument);

        CaseData caseData = getCaseData();

        CaseDocument caseDoc = formGenerator.generateDocs(caseData,
                                                          caseData.getRespondent2().getPartyName(),
                                                          caseData.getRespondent2().getPrimaryAddress(),
                                                          "party_type",
                                                          BEARER_TOKEN, false);
        assertThat(caseDoc).isNotNull();

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE));
    }

    @Test
    void shouldIncludeQMPublicInformation_whenWhenIsLipAndWelshIsOff() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
        String fileName = String.format(
            NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), REFERENCE_NUMBER);

        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(NOTICE_OF_DISCONTINUANCE)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(NoticeOfDiscontinuanceForm.class), eq(NOTICE_OF_DISCONTINUANCE_PDF)))
            .thenReturn(new DocmosisDocument(NOTICE_OF_DISCONTINUANCE_PDF.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE)))
            .thenReturn(caseDocument);

        CaseData caseData = getCaseData();

        CaseDocument caseDoc = formGenerator.generateDocs(caseData,
                                                          caseData.getRespondent2().getPartyName(),
                                                          caseData.getRespondent2().getPrimaryAddress(),
                                                          "party_type",
                                                          BEARER_TOKEN, true);
        assertThat(caseDoc).isNotNull();

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, NOTICE_OF_DISCONTINUANCE));
    }

    private CaseData getCaseData() {
        return CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                .legacyCaseReference(REFERENCE_NUMBER)
                .respondent1(PartyBuilder.builder().individual().build().toBuilder().individualFirstName("John").individualLastName("Doe").build())
                .respondent2(PartyBuilder.builder().individual().build().toBuilder().individualFirstName("Lily").individualLastName("Potter").build())
                .applicant1(PartyBuilder.builder().individual().build().toBuilder().individualFirstName("James").individualLastName("White").build())
                .applicant2(PartyBuilder.builder().individual().build().toBuilder().individualFirstName("Jan").individualLastName("Black").build())
                .claimantWhoIsDiscontinuing(DynamicList.builder()
                        .value(DynamicListElement.builder()
                                .label("Both")
                                .build())
                        .build())
                .claimantsConsentToDiscontinuance(YesOrNo.YES)
                .courtPermissionNeeded(SettleDiscontinueYesOrNoList.YES)
                .isPermissionGranted(SettleDiscontinueYesOrNoList.YES)
                .permissionGrantedComplex(new PermissionGranted()
                        .setPermissionGrantedJudge("Judge Name")
                        .setPermissionGrantedDate(LocalDate.parse("2022-02-01")))
                .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                .partDiscontinuanceDetails("partial part")
                .build();
    }
}
