package uk.gov.hmcts.reform.civil.service.docmosis.dq;

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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Experts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureReport;
import uk.gov.hmcts.reform.civil.model.dq.FurtherInformation;
import uk.gov.hmcts.reform.civil.model.dq.FutureApplications;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181_2V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181_MULTIPARTY_SAME_SOL;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DirectionsQuestionnaireGenerator.class,
    JacksonAutoConfiguration.class,
    StateFlowEngine.class,
    CaseDetailsConverter.class
})
class DirectionsQuestionnaireGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String FILE_NAME_DEFENDANT = format(N181.getDocumentTitle(), "defendant", REFERENCE_NUMBER);
    private static final String FILE_NAME_CLAIMANT = format(N181.getDocumentTitle(), "claimant", REFERENCE_NUMBER);

    private static final CaseDocument CASE_DOCUMENT_DEFENDANT =
        CaseDocumentBuilder.builder()
            .documentName(FILE_NAME_DEFENDANT)
            .documentType(DIRECTIONS_QUESTIONNAIRE)
            .build();
    private static final CaseDocument CASE_DOCUMENT_CLAIMANT =
        CaseDocumentBuilder.builder()
            .documentName(FILE_NAME_CLAIMANT)
            .documentType(DIRECTIONS_QUESTIONNAIRE)
            .build();

    private final Representative defendant1Representative =
        Representative.builder()
            .organisationName("test org")
            .build();

    private final Representative defendant2Representative =
        Representative.builder()
            .organisationName("test org 2")
            .build();

    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private DirectionsQuestionnaireGenerator generator;

    @Nested
    class RespondentOne {

        @BeforeEach
        void setup() {
            when(representativeService.getRespondent1Representative(any())).thenReturn(defendant1Representative);
        }

        @Test
        void shouldGenerateRespondentOneCertificateOfService_whenStateFlowIsFullDefence() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Test
        void shouldGenerateClaimantCertificateOfService_whenStateFlowIsRespondToDefenceAndProceed() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Test
        void shouldGenerateDQ_when2v1ScenarioWithFullDefence() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181_2V1)))
                .thenReturn(new DocmosisDocument(N181_2V1.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);
            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                eq(N181_2V1));
        }

        @Test
        void shouldGenerateDQ_when1v2SameSolicitorScenarioWithFullDefence() {
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(N181_MULTIPARTY_SAME_SOL)))
                .thenReturn(new DocmosisDocument(N181_MULTIPARTY_SAME_SOL.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(YES)
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                                                                      eq(N181_MULTIPARTY_SAME_SOL));
        }

        @Test
        void specGenerate() {
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.DEFENDANT_RESPONSE_SPEC.getDocumentTitle(), bytes));

            String expectedTitle = format(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC.getDocumentTitle(),
                   "defendant", REFERENCE_NUMBER);
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build().toBuilder()
                .superClaimType(SuperClaimType.SPEC_CLAIM)
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                                                                      eq(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC));
        }

        @Nested
        class GetTemplateData {

            @Test
            void whenCaseStateIsRespondedToClaim_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getRespondent1DQ(), caseData);
            }

            @Test
            void whenCaseStateIsFullDefence_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build()
                    .toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant1DQ(), caseData);
            }

            @Test
            void whenMultiparty2v1_shouldGetDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoApplicants()
                    .build()
                    .toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .applicant2LitigationFriend(LitigationFriend.builder().fullName("applicantTwo LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect2v1(templateData, caseData.getRespondent1DQ(), caseData);
            }

            @Test
            void whenNoRequestedCourt_build() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQRequestedCourt(null)
                                       .responseClaimCourtLocationRequired(null)
                                       .respondToCourtLocation(null)
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                assertThat(templateData.getRequestedCourt()).isNotNull()
                    .returns(NO, RequestedCourt::getRequestHearingAtSpecificCourt);
            }

            @Test
            void whenNoExperts_build() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQExperts(null)
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                assertThat(templateData.getRequestedCourt()).isNotNull()
                    .returns(NO, RequestedCourt::getRequestHearingAtSpecificCourt);
            }

            @Test
            void whenExperts_includeDetails() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                uk.gov.hmcts.reform.civil.model.dq.Expert expert1 =
                    uk.gov.hmcts.reform.civil.model.dq.Expert.builder()
                    .name("Expert 1")
                    .fieldOfExpertise("expertise 1")
                    .whyRequired("Explanation")
                    .estimatedCost(BigDecimal.valueOf(10000))
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQExperts(uk.gov.hmcts.reform.civil.model.dq.Experts.builder()
                                                                 .expertRequired(YES)
                                                                 .expertReportsSent(ExpertReportsSent.NOT_OBTAINED)
                                                                 .jointExpertSuitable(YES)
                                                                 .details(ElementUtils.wrapElements(expert1))
                                                                 .build())
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                Expert extracted = templateData.getExperts().getDetails().get(0);
                assertThat(extracted.getName()).isEqualTo(expert1.getName());
                assertThat(extracted.getFieldOfExpertise()).isEqualTo(expert1.getFieldOfExpertise());
                assertThat(extracted.getWhyRequired()).isEqualTo(expert1.getWhyRequired());
                assertThat(extracted.getFormattedCost()).isEqualTo("£100.00");
            }

            @Test
            void whenDisclosureReport_include() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                String disclosureOrderNumber = "123";
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQDisclosureReport(DisclosureReport.builder()
                                                                          .disclosureFormFiledAndServed(YES)
                                                                          .disclosureProposalAgreed(YES)
                                                                          .draftOrderNumber(disclosureOrderNumber)
                                                                          .build())
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                DisclosureReport extracted = templateData.getDisclosureReport();
                assertThat(extracted.getDraftOrderNumber()).isEqualTo(disclosureOrderNumber);
                assertThat(extracted.getDisclosureProposalAgreed()).isEqualTo(YES);
                assertThat(extracted.getDisclosureFormFiledAndServed()).isEqualTo(YES);
            }

            @Test
            void whenFurtherInformation_include() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();

                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQFutureApplications(
                                           FutureApplications.builder()
                                               .intentionToMakeFutureApplications(YES)
                                               .whatWillFutureApplicationsBeMadeFor("Reason for future apps")
                                               .build()
                                       )
                                       .respondent1DQFurtherInformation(FurtherInformation.builder()
                                                                            .otherInformationForJudge("other info")
                                                                            .build())
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                FurtherInformation extracted = templateData.getFurtherInformation();
                assertThat(extracted.getFutureApplications()).isEqualTo(YES);
                assertThat(extracted.getIntentionToMakeFutureApplications()).isEqualTo(YES);
                assertThat(extracted.getOtherInformationForJudge()).isEqualTo("other info");
            }

            @Test
            public void whenNoWelsh_build() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQLanguage(null)
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                assertThat(templateData.getWelshLanguageRequirements()).isNotNull();
            }

            @Test
            void whenSmallClaimAndNoWitness() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQExperts(null)
                                       .respondent1DQWitnesses(null)
                                       .respondent1DQHearing(uk.gov.hmcts.reform.civil.model.dq.Hearing.builder()
                                                                 .hearingLength(null)
                                                                 .build())
                                       .build())
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                assertThat(templateData.getRequestedCourt()).isNotNull()
                    .returns(NO, RequestedCourt::getRequestHearingAtSpecificCourt);
                assertThat(templateData.getWitnessesIncludingDefendants())
                    .isEqualTo(0);
            }

            @Test
            void whenSmallClaimAndWitnesses() {
                int witnessesIncludingDefendant = 2;
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQExperts(null)
                                       .respondent1DQWitnesses(null)
                                       .respondent1DQHearing(null)
                                       .build())
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .responseClaimWitnesses(Integer.toString(witnessesIncludingDefendant))
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                assertThat(templateData.getRequestedCourt()).isNotNull()
                    .returns(NO, RequestedCourt::getRequestHearingAtSpecificCourt);
                assertThat(templateData.getWitnessesIncludingDefendants())
                    .isEqualTo(witnessesIncludingDefendant);
            }

            private void assertThatDqFieldsAreCorrect2v1(DirectionsQuestionnaireForm templateData, DQ dq,
                                                         CaseData caseData) {
                assertEquals(templateData.getApplicant2(), getApplicant2(caseData));
                assertThatDqFieldsAreCorrect(templateData, dq, caseData);
            }

            private void assertThatDqFieldsAreCorrect(DirectionsQuestionnaireForm templateData,
                                                      DQ dq, CaseData caseData) {
                Assertions.assertAll(
                    "DQ data should be as expected",
                    () -> assertEquals(
                        templateData.getFileDirectionsQuestionnaire(),
                        dq.getFileDirectionQuestionnaire()
                    ),
                    () -> assertEquals(
                        templateData.getDisclosureOfElectronicDocuments(),
                        dq.getDisclosureOfElectronicDocuments()
                    ),
                    () -> assertEquals(
                        templateData.getDisclosureOfNonElectronicDocuments(),
                        dq.getDisclosureOfNonElectronicDocuments()
                    ),
                    () -> assertEquals(templateData.getRespondents(), getRespondents(caseData)),
                    () -> assertEquals(templateData.getApplicant(), getApplicant(caseData)),
                    () -> assertEquals(templateData.getExperts(), getExperts(dq)),
                    () -> assertEquals(templateData.getWitnesses(), getWitnesses(dq)),
                    () -> assertEquals(templateData.getHearing(), getHearing(dq)),
                    () -> assertEquals(templateData.getHearingSupport(), getHearingSupport(dq)),
                    () -> assertEquals(templateData.getWelshLanguageRequirements(), getWelshLanguageRequirements(dq)),
                    () -> assertEquals(templateData.getStatementOfTruth(), dq.getStatementOfTruth())
                );
            }

            private Party getApplicant(CaseData caseData) {
                var applicant = caseData.getApplicant1();
                return Party.builder()
                    .name(applicant.getPartyName())
                    .primaryAddress(applicant.getPrimaryAddress())
                    .litigationFriendName("applicant LF")
                    .build();
            }

            private Party getApplicant2(CaseData caseData) {
                var applicant = caseData.getApplicant2();
                return Party.builder()
                    .name(applicant.getPartyName())
                    .primaryAddress(applicant.getPrimaryAddress())
                    .litigationFriendName("applicantTwo LF")
                    .build();
            }

            private List<Party> getRespondents(CaseData caseData) {
                var respondent = caseData.getRespondent1();
                return List.of(Party.builder()
                                   .name(respondent.getPartyName())
                                   .primaryAddress(respondent.getPrimaryAddress())
                                   .representative(defendant1Representative)
                                   .litigationFriendName("respondent LF")
                                   .build());
            }

            private Experts getExperts(DQ dq) {
                var experts = dq.getExperts();
                return Experts.builder()
                    .expertRequired(experts.getExpertRequired())
                    .expertReportsSent(
                        ofNullable(experts.getExpertReportsSent())
                            .map(ExpertReportsSent::getDisplayedValue)
                            .orElse(""))
                    .jointExpertSuitable(experts.getJointExpertSuitable())
                    .details(getExpertsDetails(dq))
                    .build();
            }

            private List<Expert> getExpertsDetails(DQ dq) {
                return unwrapElements(dq.getExperts().getDetails())
                    .stream()
                    .map(expert -> Expert.builder()
                        .name(expert.getName())
                        .fieldOfExpertise(expert.getFieldOfExpertise())
                        .whyRequired(expert.getWhyRequired())
                        .formattedCost(NumberFormat.getCurrencyInstance(Locale.UK)
                                           .format(MonetaryConversions.penniesToPounds(expert.getEstimatedCost())))
                        .build())
                    .collect(toList());
            }

            private Witnesses getWitnesses(DQ dq) {
                var witnesses = dq.getWitnesses();
                return Witnesses.builder()
                    .witnessesToAppear(witnesses.getWitnessesToAppear())
                    .details(unwrapElements(witnesses.getDetails()))
                    .build();
            }

            private Hearing getHearing(DQ dq) {
                var hearing = dq.getHearing();
                return Hearing.builder()
                    .hearingLength(getHearingLength(dq))
                    .unavailableDatesRequired(hearing.getUnavailableDatesRequired())
                    .unavailableDates(unwrapElements(hearing.getUnavailableDates()))
                    .build();
            }

            private String getHearingLength(DQ dq) {
                var hearing = dq.getHearing();
                switch (hearing.getHearingLength()) {
                    case LESS_THAN_DAY:
                        return hearing.getHearingLengthHours() + " hours";
                    case ONE_DAY:
                        return "One day";
                    default:
                        return hearing.getHearingLengthDays() + " days";
                }
            }

            private String getHearingSupport(DQ dq) {
                var stringBuilder = new StringBuilder();
                ofNullable(dq.getHearingSupport())
                    .map(HearingSupport::getRequirements)
                    .orElse(List.of())
                    .forEach(requirement -> {
                        var hearingSupport = dq.getHearingSupport();
                        stringBuilder.append(requirement.getDisplayedValue());
                        switch (requirement) {
                            case SIGN_INTERPRETER:
                                stringBuilder.append(" - ").append(hearingSupport.getSignLanguageRequired());
                                break;
                            case LANGUAGE_INTERPRETER:
                                stringBuilder.append(" - ").append(hearingSupport.getLanguageToBeInterpreted());
                                break;
                            case OTHER_SUPPORT:
                                stringBuilder.append(" - ").append(hearingSupport.getOtherSupport());
                                break;
                            default:
                                break;
                        }
                        stringBuilder.append("\n");
                    });
                return stringBuilder.toString().trim();
            }

            private WelshLanguageRequirements getWelshLanguageRequirements(DQ dq) {
                var welshLanguageRequirements = dq.getWelshLanguageRequirements();
                return WelshLanguageRequirements.builder()
                    .evidence(ofNullable(
                        welshLanguageRequirements.getEvidence()).map(Language::getDisplayedValue).orElse(""))
                    .court(ofNullable(
                        welshLanguageRequirements.getCourt()).map(Language::getDisplayedValue).orElse(""))
                    .documents(ofNullable(
                        welshLanguageRequirements.getDocuments()).map(Language::getDisplayedValue).orElse(""))
                    .build();
            }
        }
    }

    @Nested
    class RespondentTwo {

        @BeforeEach
        void setup() {
            when(representativeService.getRespondent1Representative(any())).thenReturn(defendant1Representative);
            when(representativeService.getRespondent2Representative(any())).thenReturn(defendant2Representative);
        }

        @Test
        void shouldGenerateRespondentTwoCertificateOfService_whenStateFlowIsFullDefenceForBoth() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

            verify(representativeService).getRespondent2Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Test
        void shouldGenerateClaimantCertificateOfService_whenStateFlowIsRespondToDefenceAndProceed() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Nested
        class GetTemplateData {

            @Test
            void whenRespondent2Response_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                assertThatDqFieldsAreCorrect(templateData, caseData.getRespondent2DQ(), caseData);
            }

            @Test
            void whenRespondent2LaterResponse_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent1ResponseDate(null)
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                assertThatDqFieldsAreCorrect(templateData, caseData.getRespondent2DQ(), caseData);
            }

            @Test
            void whenRespondent2SameLegalRepAndRespondentResponseSame_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent1ResponseDate(null)
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YES)
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

                assertEquals(templateData.getRespondents(), getRespondents(caseData));
            }

            @Test
            void when1v2SolRespondsTo2ndDefendantWithDivergentResponse_shouldGetRespondentDQData() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .build();
                CaseDocument caseDocument = generator.generateDQFor1v2SingleSolDiffResponse(caseData, BEARER_TOKEN,
                                                                                            "TWO");

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                                                                          eq(N181));
            }

            @Test
            void when1v2SolRespondsTo1stDefendantWithDivergentResponse_shouldGetRespondentDQData() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .build();
                CaseDocument caseDocument = generator.generateDQFor1v2SingleSolDiffResponse(caseData, BEARER_TOKEN,
                                                                                            "ONE");

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                    eq(N181));
            }

            private void assertThatDqFieldsAreCorrect(DirectionsQuestionnaireForm templateData, DQ dq,
                                                      CaseData caseData) {
                Assertions.assertAll(
                    "DQ data should be as expected",
                    () -> assertEquals(
                        templateData.getFileDirectionsQuestionnaire(),
                        dq.getFileDirectionQuestionnaire()
                    ),
                    () -> assertEquals(
                        templateData.getDisclosureOfElectronicDocuments(),
                        dq.getDisclosureOfElectronicDocuments()
                    ),
                    () -> assertEquals(
                        templateData.getDisclosureOfNonElectronicDocuments(),
                        dq.getDisclosureOfNonElectronicDocuments()
                    ),
                    () -> assertEquals(templateData.getRespondents(), getRespondent(caseData)),
                    () -> assertEquals(templateData.getApplicant(), getApplicant(caseData)),
                    () -> assertEquals(templateData.getExperts(), getExperts(dq)),
                    () -> assertEquals(templateData.getWitnesses(), getWitnesses(dq)),
                    () -> assertEquals(templateData.getHearing(), getHearing(dq)),
                    () -> assertEquals(templateData.getHearingSupport(), getHearingSupport(dq)),
                    () -> assertEquals(templateData.getWelshLanguageRequirements(), getWelshLanguageRequirements(dq)),
                    () -> assertEquals(templateData.getStatementOfTruth(), dq.getStatementOfTruth())
                );
            }

            private Party getApplicant(CaseData caseData) {
                var applicant = caseData.getApplicant1();
                return Party.builder()
                    .name(applicant.getPartyName())
                    .primaryAddress(applicant.getPrimaryAddress())
                    .litigationFriendName("applicant LF")
                    .build();
            }

            private List<Party> getRespondent(CaseData caseData) {
                var respondent = caseData.getRespondent2();
                return List.of(Party.builder()
                                   .name(respondent.getPartyName())
                                   .primaryAddress(respondent.getPrimaryAddress())
                                   .representative(defendant2Representative)
                                   .litigationFriendName("respondent LF")
                                   .build());
            }

            private List<Party> getRespondents(CaseData caseData) {
                var respondent1 = caseData.getRespondent1();
                var respondent2 = caseData.getRespondent2();
                return List.of(Party.builder()
                                   .name(respondent1.getPartyName())
                                   .primaryAddress(respondent1.getPrimaryAddress())
                                   .representative(defendant1Representative)
                                   .litigationFriendName("respondent LF")
                                   .build(),
                               Party.builder()
                                   .name(respondent2.getPartyName())
                                   .primaryAddress(respondent2.getPrimaryAddress())
                                   .representative(defendant2Representative)
                                   .litigationFriendName("respondent LF")
                                   .build());
            }

            private Experts getExperts(DQ dq) {
                var experts = dq.getExperts();
                return Experts.builder()
                    .expertRequired(experts.getExpertRequired())
                    .expertReportsSent(
                        ofNullable(experts.getExpertReportsSent())
                            .map(ExpertReportsSent::getDisplayedValue)
                            .orElse(""))
                    .jointExpertSuitable(experts.getJointExpertSuitable())
                    .details(getExpertsDetails(dq))
                    .build();
            }

            private List<Expert> getExpertsDetails(DQ dq) {
                return unwrapElements(dq.getExperts().getDetails())
                    .stream()
                    .map(expert -> Expert.builder()
                        .name(expert.getName())
                        .fieldOfExpertise(expert.getFieldOfExpertise())
                        .whyRequired(expert.getWhyRequired())
                        .formattedCost(NumberFormat.getCurrencyInstance(Locale.UK)
                                           .format(MonetaryConversions.penniesToPounds(expert.getEstimatedCost())))
                        .build())
                    .collect(toList());
            }

            private Witnesses getWitnesses(DQ dq) {
                var witnesses = dq.getWitnesses();
                return Witnesses.builder()
                    .witnessesToAppear(witnesses.getWitnessesToAppear())
                    .details(unwrapElements(witnesses.getDetails()))
                    .build();
            }

            private Hearing getHearing(DQ dq) {
                var hearing = dq.getHearing();
                return Hearing.builder()
                    .hearingLength(getHearingLength(dq))
                    .unavailableDatesRequired(hearing.getUnavailableDatesRequired())
                    .unavailableDates(unwrapElements(hearing.getUnavailableDates()))
                    .build();
            }

            private String getHearingLength(DQ dq) {
                var hearing = dq.getHearing();
                switch (hearing.getHearingLength()) {
                    case LESS_THAN_DAY:
                        return hearing.getHearingLengthHours() + " hours";
                    case ONE_DAY:
                        return "One day";
                    default:
                        return hearing.getHearingLengthDays() + " days";
                }
            }

            private String getHearingSupport(DQ dq) {
                var stringBuilder = new StringBuilder();
                ofNullable(dq.getHearingSupport())
                    .map(HearingSupport::getRequirements)
                    .orElse(List.of())
                    .forEach(requirement -> {
                        var hearingSupport = dq.getHearingSupport();
                        stringBuilder.append(requirement.getDisplayedValue());
                        switch (requirement) {
                            case SIGN_INTERPRETER:
                                stringBuilder.append(" - ").append(hearingSupport.getSignLanguageRequired());
                                break;
                            case LANGUAGE_INTERPRETER:
                                stringBuilder.append(" - ").append(hearingSupport.getLanguageToBeInterpreted());
                                break;
                            case OTHER_SUPPORT:
                                stringBuilder.append(" - ").append(hearingSupport.getOtherSupport());
                                break;
                            default:
                                break;
                        }
                        stringBuilder.append("\n");
                    });
                return stringBuilder.toString().trim();
            }

            private WelshLanguageRequirements getWelshLanguageRequirements(DQ dq) {
                var welshLanguageRequirements = dq.getWelshLanguageRequirements();
                return WelshLanguageRequirements.builder()
                    .evidence(ofNullable(
                        welshLanguageRequirements.getEvidence()).map(Language::getDisplayedValue).orElse(""))
                    .court(ofNullable(
                        welshLanguageRequirements.getCourt()).map(Language::getDisplayedValue).orElse(""))
                    .documents(ofNullable(
                        welshLanguageRequirements.getDocuments()).map(Language::getDisplayedValue).orElse(""))
                    .build();
            }
        }
    }
}
