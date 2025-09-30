package uk.gov.hmcts.reform.civil.service.docmosis.requestforinformation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgedecisionpdfdocument.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.requestmoreinformation.RequestForInformationGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import static java.time.LocalDate.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_REQUEST_FOR_INFORMATION_ORDER_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.REQUEST_FOR_INFORMATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;

@ExtendWith(MockitoExtension.class)
class RequestForInformationGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    @Mock
    private SecuredDocumentManagementService documentManagementService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @InjectMocks
    private RequestForInformationGenerator requestForInformationGenerator;
    @Mock
    private DocmosisService docmosisService;

    @Mock
    private GaForLipService gaForLipService;

    @Test
    void shouldGenerateRequestForInformationDocument() {
        CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication().build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(REQUEST_FOR_INFORMATION)))
            .thenReturn(new DocmosisDocument(REQUEST_FOR_INFORMATION.getDocumentTitle(), bytes));
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());

        requestForInformationGenerator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.REQUEST_FOR_INFORMATION)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                  eq(REQUEST_FOR_INFORMATION));
    }

    @Test
    void shouldGenerateSendAppToOtherPartyDocumentForLipClaimant() {
        when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY)))
            .thenReturn(new DocmosisDocument(REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY.getDocumentTitle(), bytes));
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());

        CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                 .judgeRecitalText("test")
                                                 .requestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                                                 .judgeRequestMoreInfoByDate(now()).build())
            .build();

        requestForInformationGenerator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.SEND_APP_TO_OTHER_PARTY)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                  eq(REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY));
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
            .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("8").build())
            .build();

        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
            .when(docmosisService).getCaseManagementLocationVenueName(any(), any());

        Exception exception =
            assertThrows(IllegalArgumentException.class, ()
                -> requestForInformationGenerator.generate(caseData, BEARER_TOKEN));
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Nested
    class GetTemplateDataLip {

        @Test
        void shouldGenerateRequestForInformationDocument() {
            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication().build();

            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(POST_JUDGE_REQUEST_FOR_INFORMATION_ORDER_LIP)))
                .thenReturn(new DocmosisDocument(POST_JUDGE_REQUEST_FOR_INFORMATION_ORDER_LIP.getDocumentTitle(), bytes));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());

            requestForInformationGenerator.generate(CaseDataBuilder.builder().getCivilCaseData(),
                                                    caseData, BEARER_TOKEN, FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);

            verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.REQUEST_FOR_INFORMATION)
            );
            verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                      eq(POST_JUDGE_REQUEST_FOR_INFORMATION_ORDER_LIP));
        }

        @Test
        void shouldGenerateSendAppToOtherPartyDocumentForLipClaimant() {
            when(documentGeneratorService
                     .generateDocmosisDocument(any(MappableObject.class), eq(POST_JUDGE_REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY_LIP)))
                .thenReturn(new DocmosisDocument(POST_JUDGE_REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY_LIP
                                                     .getDocumentTitle(), bytes));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());

            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
                .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                     .judgeRecitalText("test")
                                                     .requestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                                                     .judgeRequestMoreInfoByDate(now()).build())
                .build();

            requestForInformationGenerator.generate(CaseDataBuilder.builder().getCivilCaseData(),
                                                    caseData, BEARER_TOKEN,
                                                    FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

            verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.SEND_APP_TO_OTHER_PARTY)
            );
            verify(documentGeneratorService)
                .generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                          eq(POST_JUDGE_REQUEST_FOR_INFORMATION_SEND_TO_OTHER_PARTY_LIP));
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetRequestForInformationData() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());
            CaseData caseData = CaseDataBuilder.builder()
                .parentClaimantIsApplicant(YES)
                .requestForInformationApplication().build().toBuilder()
                .build();

            var templateData = requestForInformationGenerator.getTemplateData(CaseDataBuilder.builder().getCivilCaseData(),
                                                                              caseData,
                                                                              "auth",
                                                                              FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);

            assertThatFieldsAreCorrect_RequestForInformation(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_RequestForInformation(JudgeDecisionPdfDocument templateData,
                                                                      CaseData caseData) {
            Assertions.assertAll(
                "Request For Information Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRecitalText()),
                () -> assertEquals(templateData.getCourtName(), "London"),
                () -> assertEquals(templateData.getJudgeComments(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRequestMoreInfoText()),
                () -> assertEquals(templateData.getAddress(), caseData.getGaCaseManagementLocation().getAddress()),
                () -> assertEquals(templateData.getSiteName(), caseData.getGaCaseManagementLocation().getSiteName()),
                () -> assertEquals(templateData.getPostcode(), caseData.getGaCaseManagementLocation().getPostcode()),
                () -> assertEquals("applicant1 partyname", templateData.getPartyName()),
                () -> assertEquals("address1", templateData.getPartyAddressAddressLine1()),
                () -> assertEquals("address2", templateData.getPartyAddressAddressLine2()),
                () -> assertEquals("address3", templateData.getPartyAddressAddressLine3()),
                () -> assertEquals("posttown", templateData.getPartyAddressPostTown()),
                () -> assertEquals("postcode", templateData.getPartyAddressPostCode()));
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetRequestForInformationData_LIP_Send_to_other_party() {

            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("Manchester").build());
            CaseData caseData = CaseDataBuilder.builder()
                .parentClaimantIsApplicant(YES)
                .requestForInformationApplication().build().toBuilder()
                .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                     .judgeRecitalText("test")
                                                     .requestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                                                     .judgeRequestMoreInfoByDate(now()).build())
                .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("3").build())
                .build();

            var templateData =
                requestForInformationGenerator.getTemplateData(CaseDataBuilder.builder().getCivilCaseData(),
                                                               caseData,
                                                               "auth",
                                                               FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

            Assertions.assertAll(
                "Request For Information Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getCourtName(), "Manchester"),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRecitalText()),
                () -> assertEquals(templateData.getJudgeComments(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRequestMoreInfoText()),
                () -> assertEquals(templateData.getApplicationCreatedDate(), caseData.getCreatedDate().toLocalDate()),
                () -> assertEquals(templateData.getAdditionalApplicationFee(), "£275"),
                () -> assertEquals("respondent1 partyname", templateData.getPartyName()),
                () -> assertEquals("respondent1address1", templateData.getPartyAddressAddressLine1()),
                () -> assertEquals("respondent1address2", templateData.getPartyAddressAddressLine2()),
                () -> assertEquals("respondent1address3", templateData.getPartyAddressAddressLine3()),
                () -> assertEquals("respondent1posttown", templateData.getPartyAddressPostTown()),
                () -> assertEquals("respondent1postcode", templateData.getPartyAddressPostCode()));
        }

    }

    @Nested
    class GetTemplateData {

        @Test
        void whenJudgeMakeDecision_ShouldGetRequestForInformationData() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());
            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication().build().toBuilder()
                .build();

            var templateData = requestForInformationGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_RequestForInformation(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_RequestForInformation(JudgeDecisionPdfDocument templateData,
                                                                      CaseData caseData) {
            Assertions.assertAll(
                "Request For Information Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRecitalText()),
                () -> assertEquals(templateData.getCourtName(), "London"),
                () -> assertEquals(templateData.getJudgeComments(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRequestMoreInfoText()),
                () -> assertEquals(templateData.getAddress(), caseData.getGaCaseManagementLocation().getAddress()),
                () -> assertEquals(templateData.getSiteName(), caseData.getGaCaseManagementLocation().getSiteName()),
                () -> assertEquals(templateData.getPostcode(), caseData.getGaCaseManagementLocation().getPostcode())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetRequestForInformationData_1v1() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("Manchester").build());
            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication().build().toBuilder()
                .defendant2PartyName(null)
                .claimant2PartyName(null)
                .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("3").build())
                .isMultiParty(NO)
                .build();

            var templateData =
                requestForInformationGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_RequestForInformation_1v1(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_RequestForInformation_1v1(JudgeDecisionPdfDocument templateData,
                                                                      CaseData caseData) {
            Assertions.assertAll(
                "Request For Information Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertNull(templateData.getClaimant2Name()),
                () -> assertEquals(NO, templateData.getIsMultiParty()),
                () -> assertEquals(templateData.getCourtName(), "Manchester"),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertNull(templateData.getDefendant2Name()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRecitalText()),
                () -> assertEquals(templateData.getJudgeComments(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRequestMoreInfoText())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetRequestForInformationData_LIP_Send_to_other_party() {

            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("Manchester").build());
            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication().build().toBuilder()
                .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                     .judgeRecitalText("test")
                                                     .requestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                                                     .judgeRequestMoreInfoByDate(now()).build())
                .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("3").build())
                .build();

            var templateData =
                requestForInformationGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            Assertions.assertAll(
                "Request For Information Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getCourtName(), "Manchester"),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRecitalText()),
                () -> assertEquals(templateData.getJudgeComments(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRequestMoreInfoText()),
                () -> assertEquals(templateData.getApplicationCreatedDate(), caseData.getCreatedDate().toLocalDate()),
                () -> assertEquals(templateData.getAdditionalApplicationFee(), "£275")
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetRequestForInformationData_LIP_Send_to_other_WelshParty() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("Manchester").welshExternalShortName("Manceinion").build());
            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication().build().toBuilder()
                .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                     .judgeRecitalText("test")
                                                     .requestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                                                     .judgeRequestMoreInfoByDate(now()).build())
                .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("3").build())
                .applicantBilingualLanguagePreferenceGA(YES)
                .build();

            var templateData =
                requestForInformationGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            Assertions.assertAll(
                "Request For Information Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getCourtName(), "Manchester"),
                () -> assertEquals(templateData.getCourtNameCy(), "Manceinion"),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRecitalText()),
                () -> assertEquals(templateData.getJudgeComments(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRequestMoreInfoText()),
                () -> assertEquals(templateData.getApplicationCreatedDate(), caseData.getCreatedDate().toLocalDate()),
                () -> assertEquals(templateData.getApplicationCreatedDateCy(), formatDateInWelsh(caseData.getCreatedDate().toLocalDate())),
                () -> assertEquals(templateData.getAdditionalApplicationFee(), "£275")
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetRequestForInformationData_LIP_Send_to_other_WelshParty_EnglishCourtName() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("Manchester").welshExternalShortName(null).build());
            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication().build().toBuilder()
                .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                     .judgeRecitalText("test")
                                                     .requestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                                                     .judgeRequestMoreInfoByDate(now()).build())
                .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("3").build())
                .applicantBilingualLanguagePreferenceGA(YES)
                .build();

            var templateData =
                requestForInformationGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            Assertions.assertAll(
                "Request For Information Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getCourtName(), "Manchester"),
                () -> assertEquals(templateData.getCourtNameCy(), "Manchester"),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRecitalText()),
                () -> assertEquals(templateData.getJudgeComments(), caseData.getJudicialDecisionRequestMoreInfo()
                    .getJudgeRequestMoreInfoText()),
                () -> assertEquals(templateData.getApplicationCreatedDate(), caseData.getCreatedDate().toLocalDate()),
                () -> assertEquals(templateData.getApplicationCreatedDateCy(), formatDateInWelsh(caseData.getCreatedDate().toLocalDate())),
                () -> assertEquals(templateData.getAdditionalApplicationFee(), "£275")
            );
        }
    }
}
