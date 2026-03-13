package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAOrderWithoutNoticeGAspec;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_WRITTEN_REPRESENTATION_CONCURRENT_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.WRITTEN_REPRESENTATION_CONCURRENT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService.DATE_FORMATTER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    WrittenRepresentationConcurrentOrderGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class WrittenRepresentationConcurrentOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private ListGeneratorService listGeneratorService;
    @Autowired
    private WrittenRepresentationConcurrentOrderGenerator writtenRepresentationConcurrentOrderGenerator;
    @MockBean
    private DocmosisService docmosisService;

    @Test
    void shouldGenerateWrittenRepresentationConcurrentDocument() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationConcurrentApplication().build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                                                               eq(WRITTEN_REPRESENTATION_CONCURRENT)))
            .thenReturn(new DocmosisDocument(WRITTEN_REPRESENTATION_CONCURRENT.getDocumentTitle(), bytes));

        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(new LocationRefData().setEpimmsId("2").setVenueName("Reading"));
        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");

        writtenRepresentationConcurrentOrderGenerator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.WRITTEN_REPRESENTATION_CONCURRENT)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                  eq(WRITTEN_REPRESENTATION_CONCURRENT));
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .writtenRepresentationConcurrentApplication()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("8").build())
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                                                               eq(WRITTEN_REPRESENTATION_CONCURRENT)))
            .thenReturn(new DocmosisDocument(WRITTEN_REPRESENTATION_CONCURRENT.getDocumentTitle(), bytes));

        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");

        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
            .when(docmosisService).getCaseManagementLocationVenueName(any(), any());

        Exception exception =
            assertThrows(IllegalArgumentException.class, () -> writtenRepresentationConcurrentOrderGenerator
                .generate(caseData, BEARER_TOKEN));
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Nested
    class GetTemplateDatLip {

        @Test
        void shouldGenerateWrittenRepresentationConcurrentDocument() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationConcurrentApplication().build();

            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                                                                   eq(POST_JUDGE_WRITTEN_REPRESENTATION_CONCURRENT_LIP)))
                .thenReturn(new DocmosisDocument(POST_JUDGE_WRITTEN_REPRESENTATION_CONCURRENT_LIP.getDocumentTitle(), bytes));

            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setVenueName("Reading"));
            when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");

            writtenRepresentationConcurrentOrderGenerator.generate(GeneralApplicationCaseDataBuilder.builder().getCivilCaseData(),
                                                                   caseData,
                                                                   BEARER_TOKEN,
                                                                   FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);

            verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.WRITTEN_REPRESENTATION_CONCURRENT)
            );
            verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                      eq(POST_JUDGE_WRITTEN_REPRESENTATION_CONCURRENT_LIP));
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetWrittenRepresentationConcurrentData() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setVenueName("London"));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .parentClaimantIsApplicant(NO)
                .writtenRepresentationConcurrentApplication().build()
                .copy()
                .isMultiParty(YesOrNo.YES)
                .build();

            when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");

            var templateData = writtenRepresentationConcurrentOrderGenerator
                .getTemplateData(GeneralApplicationCaseDataBuilder.builder().getCivilCaseData(),
                                 caseData,
                                 "auth",
                                 FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

            assertThatFieldsAreCorrect_WrittenRepresentationConcurrent(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_WrittenRepresentationConcurrent(JudgeDecisionPdfDocument templateData,
                                                                                GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "Written Representation Concurrent Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals("London", templateData.getCourtName()),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiativeForWrittenRep(), caseData
                    .getOrderCourtOwnInitiativeForWrittenRep().getOrderCourtOwnInitiative() + " ".concat(
                    caseData.getOrderCourtOwnInitiativeForWrittenRep()
                        .getOrderCourtOwnInitiativeDate().format(DATE_FORMATTER))),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudgeRecitalText()),
                () -> assertEquals(templateData.getWrittenOrder(), caseData.getDirectionInRelationToHearingText()),
                () -> assertEquals("John Doe", templateData.getJudgeNameTitle()),
                () -> assertEquals(templateData.getAddress(), caseData.getCaseManagementLocation().getAddress()),
                () -> assertEquals(templateData.getSiteName(), caseData.getCaseManagementLocation().getSiteName()),
                () -> assertEquals(templateData.getPostcode(), caseData.getCaseManagementLocation().getPostcode()),
                () -> assertEquals("respondent1partyname", templateData.getPartyName()),
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
        void whenJudgeMakeDecision_ShouldGetWrittenRepresentationConcurrentData() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setVenueName("London"));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationConcurrentApplication().build()
                .copy()
                .isMultiParty(YesOrNo.YES)
                .build();

            when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");

            var templateData = writtenRepresentationConcurrentOrderGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_WrittenRepresentationConcurrent(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_WrittenRepresentationConcurrent(JudgeDecisionPdfDocument templateData,
                                                                                GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "Written Representation Concurrent Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals("London", templateData.getCourtName()),
                () -> assertEquals(templateData.getApplicationType(), getApplicationType(caseData)),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiativeForWrittenRep(), caseData
                    .getOrderCourtOwnInitiativeForWrittenRep().getOrderCourtOwnInitiative() + " ".concat(
                    caseData.getOrderCourtOwnInitiativeForWrittenRep()
                        .getOrderCourtOwnInitiativeDate().format(DATE_FORMATTER))),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudgeRecitalText()),
                () -> assertEquals(templateData.getWrittenOrder(), caseData.getDirectionInRelationToHearingText()),
                () -> assertEquals("John Doe", templateData.getJudgeNameTitle()),
                () -> assertEquals(templateData.getAddress(), caseData.getCaseManagementLocation().getAddress()),
                () -> assertEquals(templateData.getSiteName(), caseData.getCaseManagementLocation().getSiteName()),
                () -> assertEquals(templateData.getPostcode(), caseData.getCaseManagementLocation().getPostcode())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetWrittenRepresentationConcurrentData_Option2() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder
                .builder().writtenRepresentationConcurrentApplication().build()
                .copy()
                .isMultiParty(YES)
                .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                            .address("london court")
                                            .baseLocation("1")
                                            .postcode("BA 117").build())
                .build();
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_1)
                .orderWithoutNoticeForWrittenRep(
                    new GAOrderWithoutNoticeGAspec()
                        .setOrderWithoutNotice("abcd")
                        .setOrderWithoutNoticeDate(LocalDate.now())).build();
            GeneralApplicationCaseData updateDate = caseDataBuilder.build();
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setVenueName("Reading"));
            when(listGeneratorService.applicationType(updateDate)).thenReturn("Extend time");

            var templateData = writtenRepresentationConcurrentOrderGenerator
                .getTemplateData(null, updateDate, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_WrittenRepConcurrent_Option2(templateData, updateDate);
        }

        private void assertThatFieldsAreCorrect_WrittenRepConcurrent_Option2(JudgeDecisionPdfDocument templateData,
                                                                             GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "Written Representation Concurrent Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals("Reading", templateData.getCourtName()),
                () -> assertEquals(templateData.getApplicationType(), getApplicationType(caseData)),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiativeForWrittenRep(), caseData
                    .getOrderWithoutNoticeForWrittenRep().getOrderWithoutNotice() + " ".concat(
                    caseData.getOrderWithoutNoticeForWrittenRep()
                        .getOrderWithoutNoticeDate().format(DATE_FORMATTER))),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudgeRecitalText()),
                () -> assertEquals(templateData.getWrittenOrder(), caseData.getDirectionInRelationToHearingText()),
                () -> assertEquals("John Doe", templateData.getJudgeNameTitle())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetWrittenRepresentationConcurrentData_Option3() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .writtenRepresentationConcurrentApplication().build()
                .copy()
                .isMultiParty(YES)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("3").build())
                .build();

            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_3).build();
            GeneralApplicationCaseData updateDate = caseDataBuilder.build();

            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setVenueName("Manchester"));
            when(listGeneratorService.applicationType(updateDate)).thenReturn("Extend time");

            var templateData = writtenRepresentationConcurrentOrderGenerator
                .getTemplateData(null, updateDate, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_WrittenRepConcurrent_Option3(templateData, updateDate);
        }

        private void assertThatFieldsAreCorrect_WrittenRepConcurrent_Option3(JudgeDecisionPdfDocument templateData,
                                                                             GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "Written Representation Concurrent Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals("Manchester", templateData.getCourtName()),
                () -> assertEquals(templateData.getApplicationType(), getApplicationType(caseData)),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals(StringUtils.EMPTY, templateData.getJudicialByCourtsInitiativeForWrittenRep()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudgeRecitalText()),
                () -> assertEquals(templateData.getWrittenOrder(), caseData.getDirectionInRelationToHearingText()),
                () -> assertEquals("John Doe", templateData.getJudgeNameTitle())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetWrittenRepresentationConcurrentData_1V2() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationConcurrentApplication().build()
                .copy()
                .isMultiParty(NO)
                .defendant2PartyName(null)
                .claimant2PartyName(null).build();

            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_3).build();
            GeneralApplicationCaseData updateDate = caseDataBuilder.build();
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setVenueName("Reading"));
            when(listGeneratorService.applicationType(updateDate)).thenReturn("Extend time");

            var templateData = writtenRepresentationConcurrentOrderGenerator
                .getTemplateData(null, updateDate, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_WrittenRepConcurrent_1V2(templateData, updateDate);
        }

        private void assertThatFieldsAreCorrect_WrittenRepConcurrent_1V2(JudgeDecisionPdfDocument templateData,
                                                                         GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "Written Representation Concurrent Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertNull(templateData.getClaimant2Name()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertNull(templateData.getDefendant2Name()),
                () -> assertEquals(NO, templateData.getIsMultiParty()),
                () -> assertEquals(templateData.getApplicationType(), getApplicationType(caseData)),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals(StringUtils.EMPTY, templateData.getJudicialByCourtsInitiativeForWrittenRep()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudgeRecitalText()),
                () -> assertEquals(templateData.getWrittenOrder(), caseData.getDirectionInRelationToHearingText()),
                () -> assertEquals("John Doe", templateData.getJudgeNameTitle())
            );
        }

        private String getApplicationType(GeneralApplicationCaseData caseData) {
            List<GeneralApplicationTypes> types = caseData.getGeneralAppType().getTypes();
            return types.stream()
                .map(GeneralApplicationTypes::getDisplayedValue).collect(Collectors.joining(", "));
        }
    }
}
