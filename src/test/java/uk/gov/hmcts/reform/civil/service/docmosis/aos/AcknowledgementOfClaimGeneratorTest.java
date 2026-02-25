package uk.gov.hmcts.reform.civil.service.docmosis.aos;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.aos.AcknowledgementOfClaimForm;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.CONTEST_JURISDICTION;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.ACKNOWLEDGEMENT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N11;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N9_MULTIPARTY_SAME_SOL;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.fetchSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.toCaseName;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    AcknowledgementOfClaimGenerator.class,
    JacksonAutoConfiguration.class
})
class AcknowledgementOfClaimGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = format(N11.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(ACKNOWLEDGEMENT_OF_CLAIM)
        .build();
    private static final String fileName_1v2 = format(N9_MULTIPARTY_SAME_SOL.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT_1V2 = CaseDocumentBuilder.builder()
        .documentName(fileName_1v2)
        .documentType(ACKNOWLEDGEMENT_OF_CLAIM)
        .build();
    private LocalDateTime acknowledgementDate;

    private final Representative representative = new Representative().setOrganisationName("test org");

    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private RepresentativeService representativeService;

    @Autowired
    private AcknowledgementOfClaimGenerator generator;

    @BeforeEach
    void setup() {
        when(representativeService.getRespondent1Representative(any())).thenReturn(representative);
        when(representativeService.getRespondent2Representative(any())).thenReturn(representative);
    }

    @Test
    void shouldGenerateAcknowledgementOfClaim_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N11)))
            .thenReturn(new DocmosisDocument(N11.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();

        AcknowledgementOfClaimForm expectedDocmosisData = new AcknowledgementOfClaimForm(
            "[userImage:courtseal.PNG]",
            "Mr. John Rambo \nvs Mr. Sole Trader T/A Sole Trader co",
            LEGACY_CASE_REFERENCE,
            caseData.getSolicitorReferences(),
            caseData.getIssueDate(),
            caseData.getRespondent1ResponseDeadline().toLocalDate(),
            new ArrayList<>(List.of(
                new Party()
                    .setName(caseData.getRespondent1().getPartyName())
                    .setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress())
                    .setRepresentative(representative)
                    .setLitigationFriendName(
                        ofNullable(caseData.getRespondent1LitigationFriend())
                            .map(LitigationFriend::getFullName)
                            .orElse("")))),
            DocmosisTemplateDataUtils.fetchResponseIntentionsDocmosisTemplate(caseData)
        );

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM));
        verify(documentGeneratorService)
            .generateDocmosisDocument(expectedDocmosisData, N11);
    }

    @Test
    void shouldGenerateAcknowledgementOfClaim_when1V2SameSolicitorDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N9_MULTIPARTY_SAME_SOL)))
            .thenReturn(new DocmosisDocument(N9_MULTIPARTY_SAME_SOL.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_1v2, bytes, ACKNOWLEDGEMENT_OF_CLAIM)))
            .thenReturn(CASE_DOCUMENT_1V2);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(YES)
            .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
            .build();

        AcknowledgementOfClaimForm expectedDocmosisData = new AcknowledgementOfClaimForm(
            "[userImage:courtseal.PNG]",
            "Mr. John Rambo \nvs 1 Mr. Sole Trader T/A Sole Trader co & 2 Mr. John Rambo",
            LEGACY_CASE_REFERENCE,
            caseData.getSolicitorReferences(),
            caseData.getIssueDate(),
            caseData.getRespondent1ResponseDeadline().toLocalDate(),
            new ArrayList<>(List.of(
                new Party()
                    .setName(caseData.getRespondent1().getPartyName())
                    .setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress())
                    .setRepresentative(representative)
                    .setLitigationFriendName(
                        ofNullable(caseData.getRespondent1LitigationFriend())
                            .map(LitigationFriend::getFullName)
                            .orElse("")), new Party()
                    .setName(caseData.getRespondent2().getPartyName())
                    .setPrimaryAddress(caseData.getRespondent2().getPrimaryAddress())
                    .setRepresentative(representative)
                    .setLitigationFriendName(
                        ofNullable(caseData.getRespondent2LitigationFriend())
                            .map(LitigationFriend::getFullName)
                            .orElse(""))
                )),
            DocmosisTemplateDataUtils.fetchResponseIntentionsDocmosisTemplate(caseData)
        );

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_1V2);
        verify(documentGeneratorService)
            .generateDocmosisDocument(expectedDocmosisData, N9_MULTIPARTY_SAME_SOL);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_1v2, bytes, ACKNOWLEDGEMENT_OF_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(AcknowledgementOfClaimForm.class), eq(N9_MULTIPARTY_SAME_SOL));
    }

    @Test
    void shouldGenerateAcknowledgementOfClaim_when1V2DifferentSolicitor1DataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N11)))
            .thenReturn(new DocmosisDocument(N11.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(NO)
            .build();

        AcknowledgementOfClaimForm expectedDocmosisData = new AcknowledgementOfClaimForm(
            "[userImage:courtseal.PNG]",
            "Mr. John Rambo \nvs 1 Mr. Sole Trader T/A Sole Trader co & 2 Mr. John Rambo",
            LEGACY_CASE_REFERENCE,
            caseData.getSolicitorReferences(),
            caseData.getIssueDate(),
            caseData.getRespondent1ResponseDeadline().toLocalDate(),
            new ArrayList<>(List.of(
                new Party()
                    .setName(caseData.getRespondent1().getPartyName())
                    .setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress())
                    .setRepresentative(representative)
                    .setLitigationFriendName(
                        ofNullable(caseData.getRespondent1LitigationFriend())
                            .map(LitigationFriend::getFullName)
                            .orElse("")), new Party()
                    .setName(caseData.getRespondent2().getPartyName())
                    .setPrimaryAddress(caseData.getRespondent2().getPrimaryAddress())
                    .setRepresentative(representative)
                    .setLitigationFriendName(
                        ofNullable(caseData.getRespondent2LitigationFriend())
                            .map(LitigationFriend::getFullName)
                            .orElse(""))
            )),
            DocmosisTemplateDataUtils.fetchResponseIntentionsDocmosisTemplate(caseData)
        );

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);
        verify(documentGeneratorService)
            .generateDocmosisDocument(expectedDocmosisData, N11);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(AcknowledgementOfClaimForm.class), eq(N11));
    }

    @Test
    void shouldGenerateAcknowledgementOfClaim_when1V2DifferentSolicitor2DataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N11)))
            .thenReturn(new DocmosisDocument(N11.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent1AcknowledgeNotificationDate(null)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
            .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
            .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(NO)
            .respondentSolicitor2Reference("5678")
            .solicitorReferences(new SolicitorReferences()
                .setApplicantSolicitor1Reference("12345")
                .setRespondentSolicitor1Reference(null)
                .setRespondentSolicitor2Reference("5678"))

            .build();
        AcknowledgementOfClaimForm expectedDocmosisData = new AcknowledgementOfClaimForm(
            "[userImage:courtseal.PNG]",
            "Mr. John Rambo \nvs 1 Mr. Sole Trader T/A Sole Trader co & 2 Mr. John Rambo",
            LEGACY_CASE_REFERENCE,
            caseData.getSolicitorReferences(),
            caseData.getIssueDate(),
            caseData.getRespondent1ResponseDeadline().toLocalDate(),
            new ArrayList<>(List.of(
                new Party()
                    .setName(caseData.getRespondent2().getPartyName())
                    .setPrimaryAddress(caseData.getRespondent2().getPrimaryAddress())
                    .setRepresentative(representative)
                    .setLitigationFriendName(
                        ofNullable(caseData.getRespondent2LitigationFriend())
                            .map(LitigationFriend::getFullName)
                            .orElse(""))
            )),
            DocmosisTemplateDataUtils.fetchResponseIntentionsDocmosisTemplate(caseData)
        );

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);
        verify(documentGeneratorService)
            .generateDocmosisDocument(expectedDocmosisData, N11);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(AcknowledgementOfClaimForm.class), eq(N11));
    }

    @Test
    void shouldGenerateAcknowledgementOfClaim_when2v1DataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N11)))
            .thenReturn(new DocmosisDocument(N11.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .applicant2(PartyBuilder.builder().individual().build())
            .respondent1AcknowledgeNotificationDate(null)
            .addApplicant2(YES)
            .respondent1ClaimResponseIntentionTypeApplicant2(CONTEST_JURISDICTION)
            .respondentSolicitor2Reference("5678")
            .solicitorReferences(new SolicitorReferences()
                .setApplicantSolicitor1Reference("12345")
                .setRespondentSolicitor1Reference("1234"))

            .build();
        AcknowledgementOfClaimForm expectedDocmosisData = new AcknowledgementOfClaimForm(
            "[userImage:courtseal.PNG]",
            "1 Mr. John Rambo & 2 Mr. John Rambo \nvs Mr. Sole Trader T/A Sole Trader co",
            LEGACY_CASE_REFERENCE,
            caseData.getSolicitorReferences(),
            caseData.getIssueDate(),
            caseData.getRespondent1ResponseDeadline().toLocalDate(),
            new ArrayList<>(List.of(
                new Party()
                    .setName(caseData.getRespondent1().getPartyName())
                    .setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress())
                    .setRepresentative(representative)
                    .setLitigationFriendName(
                        ofNullable(caseData.getRespondent1LitigationFriend())
                            .map(LitigationFriend::getFullName)
                            .orElse(""))
            )),
            DocmosisTemplateDataUtils.fetchResponseIntentionsDocmosisTemplate(caseData)
        );

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);
        verify(documentGeneratorService)
            .generateDocmosisDocument(expectedDocmosisData, N11);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(AcknowledgementOfClaimForm.class), eq(N11));
    }

    @Test
    void shouldGenerateAcknowledgementOfClaim_when1V2DifferentSolicitor1AcknowledgesFirst() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N11)))
            .thenReturn(new DocmosisDocument(N11.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent1AcknowledgeNotificationDate(LocalDateTime.now())
            .respondent2AcknowledgeNotificationDate(LocalDateTime.now().plusDays(1))
            .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
            .respondent2ResponseDeadline(LocalDateTime.now())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(NO)
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(AcknowledgementOfClaimForm.class), eq(N11));
    }

    @Test
    void shouldGenerateAcknowledgementOfClaim_when1V2DifferentSolicitor2AcknowledgesFirst() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N11)))
            .thenReturn(new DocmosisDocument(N11.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent1AcknowledgeNotificationDate(LocalDateTime.now().plusDays(1))
            .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
            .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(NO)
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(AcknowledgementOfClaimForm.class), eq(N11));
    }

    @Nested
    class GetTemplateData {

        @Test
        void whenCaseIsAtClaimAcknowledge_shouldGetAcknowledgementOfClaimFormData() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1LitigationFriend(new LitigationFriend().setFullName("LF name"))
                .build();

            var templateData = generator.getTemplateData(caseData);

            verify(representativeService).getRespondent1Representative(caseData);
            assertThatFieldsAreCorrect(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect(AcknowledgementOfClaimForm templateData, CaseData caseData) {
            Assertions.assertAll(
                "AcknowledgementOfClaim data should be as expected",
                () -> assertEquals(templateData.getCaseName(), toCaseName.apply(caseData)),
                () -> assertEquals(templateData.getReferenceNumber(), caseData.getLegacyCaseReference()),
                () -> assertEquals(
                    templateData.getSolicitorReferences(),
                    fetchSolicitorReferences(caseData)
                ),
                () -> assertEquals(templateData.getIssueDate(), caseData.getIssueDate()),
                () -> assertEquals(
                    templateData.getResponseDeadline(),
                    caseData.getRespondent1ResponseDeadline().toLocalDate()
                ),
                () -> assertEquals(templateData.getRespondent(), new ArrayList<>(List.of(new Party()
                    .setName(caseData.getRespondent1().getPartyName())
                    .setRepresentative(representative)
                    .setLitigationFriendName(caseData.getRespondent1LitigationFriend().getFullName())
                    .setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress())))
                )
            );
        }
    }
}
