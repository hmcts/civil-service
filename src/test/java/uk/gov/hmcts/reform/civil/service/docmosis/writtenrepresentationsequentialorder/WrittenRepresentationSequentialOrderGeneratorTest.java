package uk.gov.hmcts.reform.civil.service.docmosis.writtenrepresentationsequentialorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
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
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgedecisionpdfdocument.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.model.genapplication.GAOrderCourtOwnInitiativeGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAOrderWithoutNoticeGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.ListGeneratorService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;

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
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_WRITTEN_REPRESENTATION_SEQUENTIAL_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.WRITTEN_REPRESENTATION_SEQUENTIAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService.DATE_FORMATTER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    WrittenRepresentationSequentialOrderGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class WrittenRepresentationSequentialOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final ObjectMapper GA_OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());

    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private ListGeneratorService listGeneratorService;
    @MockBean
    private GaCaseDataEnricher gaCaseDataEnricher;

    @Autowired
    private WrittenRepresentationSequentialOrderGenerator writtenRepresentationSequentialOrderGenerator;
    @MockBean
    private DocmosisService docmosisService;

    @BeforeEach
    void setup() {
        when(gaCaseDataEnricher.enrich(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldGenerateWrittenRepresentationSequentialDocument() {
        CaseData caseData = gaWrittenRepresentationSequentialData().toBuilder()
            .judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_1)
            .orderCourtOwnInitiativeForWrittenRep(GAOrderCourtOwnInitiativeGAspec.builder()
                .orderCourtOwnInitiative("initiative text")
                .orderCourtOwnInitiativeDate(LocalDate.now())
                .build())
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                eq(WRITTEN_REPRESENTATION_SEQUENTIAL)))
                .thenReturn(new DocmosisDocument(WRITTEN_REPRESENTATION_SEQUENTIAL.getDocumentTitle(), bytes));

        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");

        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());

        writtenRepresentationSequentialOrderGenerator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                eq(WRITTEN_REPRESENTATION_SEQUENTIAL));
    }

    @Test
    void shouldGenerateSequentialOrderFromGaCaseData() {
        CaseData caseData = gaWrittenRepresentationSequentialData();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                eq(WRITTEN_REPRESENTATION_SEQUENTIAL)))
                .thenReturn(new DocmosisDocument(WRITTEN_REPRESENTATION_SEQUENTIAL.getDocumentTitle(), bytes));
        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());

        GeneralApplicationCaseData gaCaseData = GA_OBJECT_MAPPER.convertValue(caseData, GeneralApplicationCaseData.class);
        writtenRepresentationSequentialOrderGenerator.generate(gaCaseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL)
        );
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        CaseData caseData = gaWrittenRepresentationSequentialData().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("8").build())
                .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                eq(WRITTEN_REPRESENTATION_SEQUENTIAL)))
                .thenReturn(new DocmosisDocument(WRITTEN_REPRESENTATION_SEQUENTIAL.getDocumentTitle(), bytes));

        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");

        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
                .when(docmosisService).getCaseManagementLocationVenueName(any(), any());

        Exception exception =
                assertThrows(IllegalArgumentException.class, ()
                        -> writtenRepresentationSequentialOrderGenerator.generate(caseData, BEARER_TOKEN));
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Nested
    class GetTemplateDataLip {

        @Test
        void shouldGenerateWrittenRepresentationSequentialDocument() {
            CaseData caseData = gaWrittenRepresentationSequentialData();

            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                    eq(POST_JUDGE_WRITTEN_REPRESENTATION_SEQUENTIAL_LIP)))
                    .thenReturn(new DocmosisDocument(POST_JUDGE_WRITTEN_REPRESENTATION_SEQUENTIAL_LIP.getDocumentTitle(), bytes));

            when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");

            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                    .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());

            writtenRepresentationSequentialOrderGenerator.generate(CaseDataBuilder.builder().getCivilCaseData(),
                    caseData,
                    BEARER_TOKEN,
                    FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);

            verify(documentManagementService).uploadDocument(
                    BEARER_TOKEN,
                    new PDF(any(), any(), DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL)
            );
            verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                    eq(POST_JUDGE_WRITTEN_REPRESENTATION_SEQUENTIAL_LIP));
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetWrittenRepresentationSequentialData() {
            CaseData caseData = CaseDataBuilder.builder()
                    .parentClaimantIsApplicant(YES)
                    .writtenRepresentationSequentialApplication().build()
                    .toBuilder().build();

            when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");

            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                    .thenReturn(LocationRefData.builder().epimmsId("2").venueName("Reading").build());

            var templateData = writtenRepresentationSequentialOrderGenerator
                    .getTemplateData(CaseDataBuilder.builder().getCivilCaseData(),
                            caseData,
                            "auth", FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);

            assertThatFieldsAreCorrect_WrittenRepresentationSequential(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_WrittenRepresentationSequential(JudgeDecisionPdfDocument templateData,
                                                                                CaseData caseData) {
            Assertions.assertAll(
                    "Written Representation Sequential Document data should be as expected",
                    () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                    () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                    () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                    () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                    () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                    () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                    () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                    () -> assertEquals(templateData.getCourtName(), "Reading"),
                    () -> assertEquals(templateData.getJudicialByCourtsInitiativeForWrittenRep(), caseData
                            .getOrderCourtOwnInitiativeForWrittenRep().getOrderCourtOwnInitiative() + " ".concat(
                            caseData.getOrderCourtOwnInitiativeForWrittenRep()
                                    .getOrderCourtOwnInitiativeDate().format(DATE_FORMATTER))),
                    () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudgeRecitalText()),
                    () -> assertEquals(templateData.getWrittenOrder(), caseData.getDirectionInRelationToHearingText()),
                    () -> assertEquals(templateData.getJudgeNameTitle(), "John Doe"),
                    () -> assertEquals(templateData.getAddress(), caseData.getCaseManagementLocation().getAddress()),
                    () -> assertEquals(templateData.getSiteName(), caseData.getCaseManagementLocation().getSiteName()),
                    () -> assertEquals(templateData.getPostcode(), caseData.getCaseManagementLocation().getPostcode()),
                    () -> assertEquals("applicant1 partyname", templateData.getPartyName()),
                    () -> assertEquals("address1", templateData.getPartyAddressAddressLine1()),
                    () -> assertEquals("address2", templateData.getPartyAddressAddressLine2()),
                    () -> assertEquals("address3", templateData.getPartyAddressAddressLine3()),
                    () -> assertEquals("posttown", templateData.getPartyAddressPostTown()),
                    () -> assertEquals("postcode", templateData.getPartyAddressPostCode()));
        }
    }

    @Nested
    class GetTemplateData {

        @Test
        void whenJudgeMakeDecision_ShouldGetWrittenRepresentationSequentialData() {
            CaseData caseData = gaWrittenRepresentationSequentialData()
                    .toBuilder().build();

            when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");

            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                    .thenReturn(LocationRefData.builder().epimmsId("2").venueName("Reading").build());

            var templateData = writtenRepresentationSequentialOrderGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_WrittenRepresentationSequential(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_WrittenRepresentationSequential(JudgeDecisionPdfDocument templateData,
                                                                                CaseData caseData) {
            Assertions.assertAll(
                    "Written Representation Sequential Document data should be as expected",
                    () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                    () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                    () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                    () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                    () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                    () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                    () -> assertEquals(templateData.getApplicationType(), getApplicationType(caseData)),
                    () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                    () -> assertEquals(templateData.getCourtName(), "Reading"),
                    () -> assertEquals(templateData.getJudicialByCourtsInitiativeForWrittenRep(), caseData
                            .getOrderCourtOwnInitiativeForWrittenRep().getOrderCourtOwnInitiative() + " ".concat(
                            caseData.getOrderCourtOwnInitiativeForWrittenRep()
                                    .getOrderCourtOwnInitiativeDate().format(DATE_FORMATTER))),
                    () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudgeRecitalText()),
                    () -> assertEquals(templateData.getWrittenOrder(), caseData.getDirectionInRelationToHearingText()),
                    () -> assertEquals(templateData.getJudgeNameTitle(), "John Doe"),
                    () -> assertEquals(templateData.getAddress(), caseData.getCaseManagementLocation().getAddress()),
                    () -> assertEquals(templateData.getSiteName(), caseData.getCaseManagementLocation().getSiteName()),
                    () -> assertEquals(templateData.getPostcode(), caseData.getCaseManagementLocation().getPostcode())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetWrittenRepresentationSequentialData_Option2() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                    .thenReturn(LocationRefData.builder().epimmsId("2").venueName("Manchester").build());
            CaseData caseData = CaseDataBuilder.builder().writtenRepresentationSequentialApplication()
                    .build()
                    .toBuilder()
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("3").build())
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("3").build())
                    .build();
            CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
            caseDataBuilder.judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_2)
                    .orderWithoutNoticeForWrittenRep(
                            GAOrderWithoutNoticeGAspec.builder().orderWithoutNotice("abcde")
                                    .orderWithoutNoticeDate(LocalDate.now()).build())
                    .orderCourtOwnInitiativeForWrittenRep(
                            GAOrderCourtOwnInitiativeGAspec.builder().build()).build();

            CaseData updateData = caseDataBuilder.build();

            when(listGeneratorService.applicationType(updateData)).thenReturn("Extend time");

            var templateData = writtenRepresentationSequentialOrderGenerator
                    .getTemplateData(null, updateData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_WrittenRepSequential_Option2(templateData, updateData);
        }

        private void assertThatFieldsAreCorrect_WrittenRepSequential_Option2(JudgeDecisionPdfDocument templateData,
                                                                             CaseData caseData) {
            Assertions.assertAll(
                    "Written Representation Sequential Document data should be as expected",
                    () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                    () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                    () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                    () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                    () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                    () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                    () -> assertEquals(templateData.getApplicationType(), getApplicationType(caseData)),
                    () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                    () -> assertEquals(templateData.getCourtName(), "Manchester"),
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
        void whenJudgeMakeDecision_ShouldGetWrittenRepresentationSequentialData_Option3_1v1() {
            CaseData caseData = gaWrittenRepresentationSequentialData()
                    .toBuilder().isMultiParty(YesOrNo.YES)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("2").build())
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("2").build())
                    .build();
            CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
            caseDataBuilder.judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_3)
                    .orderCourtOwnInitiativeForWrittenRep(
                            GAOrderCourtOwnInitiativeGAspec.builder().build()).build();

            CaseData updateData = caseDataBuilder.build();

            when(listGeneratorService.applicationType(updateData)).thenReturn("Extend time");

            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                    .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());

            var templateData = writtenRepresentationSequentialOrderGenerator
                    .getTemplateData(null, updateData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_WrittenRepSequential_Option3(templateData, updateData);
        }

        private void assertThatFieldsAreCorrect_WrittenRepSequential_Option3(JudgeDecisionPdfDocument templateData,
                                                                             CaseData caseData) {
            Assertions.assertAll(
                    "Written Representation Sequential Document data should be as expected",
                    () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                    () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                    () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                    () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                    () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                    () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                    () -> assertEquals(YES, templateData.getIsMultiParty()),
                    () -> assertEquals(templateData.getApplicationType(), getApplicationType(caseData)),
                    () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                    () -> assertEquals(StringUtils.EMPTY, templateData.getJudicialByCourtsInitiativeForWrittenRep()),
                    () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudgeRecitalText()),
                    () -> assertEquals(templateData.getCourtName(), "London"),
                    () -> assertEquals(templateData.getWrittenOrder(), caseData.getDirectionInRelationToHearingText()),
                    () -> assertEquals("John Doe", templateData.getJudgeNameTitle())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetWrittenRepresentationSequentialData_Option3_1V1() {
            CaseData caseData = gaWrittenRepresentationSequentialData()
                    .toBuilder()
                    .defendant2PartyName(null)
                    .claimant2PartyName(null)
                    .isMultiParty(YesOrNo.NO).build();
            CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
            caseDataBuilder.judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_3)
                    .orderCourtOwnInitiativeForWrittenRep(
                            GAOrderCourtOwnInitiativeGAspec.builder().build()).build();

            CaseData updateData = caseDataBuilder.build();

            when(listGeneratorService.applicationType(updateData)).thenReturn("Extend time");
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                    .thenReturn(LocationRefData.builder().epimmsId("2").venueName("Reading").build());

            var templateData = writtenRepresentationSequentialOrderGenerator
                    .getTemplateData(null, updateData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_WrittenRepSequential_Option3_1V1(templateData, updateData);
        }

        private void assertThatFieldsAreCorrect_WrittenRepSequential_Option3_1V1(JudgeDecisionPdfDocument templateData,
                                                                                 CaseData caseData) {
            Assertions.assertAll(
                    "Written Representation Sequential Document data should be as expected",
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

        private String getApplicationType(CaseData caseData) {
            List<GeneralApplicationTypes> types = caseData.getGeneralAppType().getTypes();
            return types.stream()
                    .map(GeneralApplicationTypes::getDisplayedValue).collect(Collectors.joining(", "));
        }
    }

    private CaseData gaWrittenRepresentationSequentialData() {
        LocalDateTime now = LocalDateTime.now();
        Fee fee = Fee.builder()
            .calculatedAmountInPence(BigDecimal.valueOf(10800))
            .code("FEE0443")
            .version("1")
            .build();
        GAPbaDetails pbaDetails = GAPbaDetails.builder()
            .fee(fee)
            .serviceReqReference(CaseDataBuilder.CUSTOMER_REFERENCE)
            .build();

        GeneralApplicationCaseDataBuilder gaBuilder = GeneralApplicationCaseDataBuilder.builder()
            .withCcdCaseReference(CaseDataBuilder.CASE_ID)
            .withGeneralAppParentCaseReference(CaseDataBuilder.PARENT_CASE_ID)
            .withApplicantPartyName("Test Applicant Name")
            .withClaimant1PartyName("Test Claimant1 Name")
            .withDefendant1PartyName("Test Defendant1 Name")
            .withGeneralAppType(GAApplicationType.builder().types(List.of(GeneralApplicationTypes.EXTEND_TIME)).build())
            .withGeneralAppPBADetails(pbaDetails)
            .withGeneralAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .withJudicialDecision(GAJudicialDecision.builder().decision(GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS).build())
            .withGeneralAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .withGaCaseManagementLocation(GACaseLocation.builder()
                .siteName("testing")
                .address("london court")
                .baseLocation("1")
                .postcode("BA 117")
                .build());

        GeneralApplicationCaseData gaCaseData = gaBuilder.build();
        CaseData converted = GA_OBJECT_MAPPER.convertValue(gaCaseData, CaseData.class);
        CaseData enriched = gaCaseDataEnricher.enrich(converted, gaCaseData);

        return enriched.toBuilder()
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference(CaseDataBuilder.PARENT_CASE_ID).build())
            .ccdCaseReference(CaseDataBuilder.CASE_ID)
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .judgeTitle("John Doe")
            .caseManagementLocation(CaseLocationCivil.builder()
                .siteName("testing")
                .address("london court")
                .baseLocation("1")
                .postcode("BA 117")
                .build())
            .judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_1)
            .orderCourtOwnInitiativeForWrittenRep(GAOrderCourtOwnInitiativeGAspec.builder()
                .orderCourtOwnInitiative("abcd")
                .orderCourtOwnInitiativeDate(LocalDate.now())
                .build())
            .createdDate(now)
            .judgeRecitalText("Test Judge's recital")
            .directionInRelationToHearingText("Test written order")
            .judicialDecisionMakeAnOrderForWrittenRepresentations(GAJudicialWrittenRepresentations.builder()
                .writtenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
                .writtenSequentailRepresentationsBy(LocalDate.now())
                .sequentialApplicantMustRespondWithin(LocalDate.now().plusDays(5))
                .build())
            .build();
    }
}
