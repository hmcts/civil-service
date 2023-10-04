package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_TWO;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.sampledata.PartyBuilder.DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class PartyUtilsTest {

    @Nested
    class PartyNameBasedOnType {
        @Test
        void shouldThrowNullPointer_whenPartyTypeIsNull() {
            Party party = Party.builder().type(null).build();
            assertThrows(NullPointerException.class, () -> PartyUtils.getPartyNameBasedOnType(party));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsIndividual() {
            Party individual = Party.builder()
                .individualTitle("Mr")
                .individualFirstName("Jacob")
                .individualLastName("Martin")
                .type(Party.Type.INDIVIDUAL).build();

            assertEquals("Mr Jacob Martin", PartyUtils.getPartyNameBasedOnType(individual));
        }

        @Test
        void shouldProvideNameWithoutTitle_whenPartyTypeIsIndividual() {
            Party individual = Party.builder()
                .individualTitle("Mr")
                .individualFirstName("Jacob")
                .individualLastName("Martin")
                .type(Party.Type.INDIVIDUAL).build();

            assertEquals("Jacob Martin", PartyUtils.getPartyNameBasedOnType(individual, true));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsIndividualWithoutTitle() {
            Party individual = Party.builder()
                .individualFirstName("Jacob")
                .individualLastName("Martin")
                .type(Party.Type.INDIVIDUAL).build();

            assertEquals("Jacob Martin", PartyUtils.getPartyNameBasedOnType(individual));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsCompany() {
            Party individual = Party.builder()
                .companyName("XYZ Company House")
                .type(Party.Type.COMPANY).build();

            assertEquals("XYZ Company House", PartyUtils.getPartyNameBasedOnType(individual));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsOrganisation() {
            Party organisation = Party.builder()
                .organisationName("ABC Solutions")
                .type(Party.Type.ORGANISATION).build();

            assertEquals("ABC Solutions", PartyUtils.getPartyNameBasedOnType(organisation));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsSoleTrader() {
            Party soleTrader = Party.builder()
                .soleTraderTitle("Mr")
                .soleTraderFirstName("Jacob")
                .soleTraderLastName("Martin")
                .type(Party.Type.SOLE_TRADER).build();

            assertEquals("Mr Jacob Martin", PartyUtils.getPartyNameBasedOnType(soleTrader));
        }

        @Test
        void shouldProvideNameWithoutTitle_whenPartyTypeIsSoleTrader() {
            Party soleTrader = Party.builder()
                .soleTraderTitle("Mr")
                .soleTraderFirstName("Jacob")
                .soleTraderLastName("Martin")
                .type(Party.Type.SOLE_TRADER).build();

            assertEquals("Jacob Martin", PartyUtils.getPartyNameBasedOnType(soleTrader, true));
        }
    }

    @Nested
    class LitigiousPartyName {

        @Test
        void shouldThrowNullPointer_whenPartyTypeIsNull() {
            Party party = Party.builder().type(null).build();
            LitigationFriend litigationFriend = LitigationFriend.builder().build();
            assertThrows(NullPointerException.class, () -> PartyUtils.getLitigiousPartyName(party, litigationFriend));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsIndividualAndNoLitigationFriend() {
            Party individual = Party.builder()
                .individualTitle("Mr")
                .individualFirstName("Jacob")
                .individualLastName("Martin")
                .type(Party.Type.INDIVIDUAL).build();

            assertEquals("Mr Jacob Martin", PartyUtils.getLitigiousPartyName(individual, null));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsIndividualAndLitigationFriend() {
            Party individual = Party.builder()
                .individualTitle("Mr")
                .individualFirstName("Jacob")
                .individualLastName("Martin")
                .type(Party.Type.INDIVIDUAL).build();
            LitigationFriend litigationFriend = LitigationFriend.builder()
                .firstName("Litigious")
                .lastName("Friend")
                .build();
            assertEquals("Mr Jacob Martin L/F Litigious Friend",
                         PartyUtils.getLitigiousPartyName(individual, litigationFriend));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsCompany() {
            Party individual = Party.builder()
                .companyName("XYZ Company House")
                .type(Party.Type.COMPANY).build();

            assertEquals("XYZ Company House", PartyUtils.getLitigiousPartyName(individual, null));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsOrganisation() {
            Party organisation = Party.builder()
                .organisationName("ABC Solutions")
                .type(Party.Type.ORGANISATION).build();

            assertEquals("ABC Solutions", PartyUtils.getLitigiousPartyName(organisation, null));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsSoleTrader() {
            Party soleTrader = Party.builder()
                .soleTraderTitle("Mr")
                .soleTraderFirstName("Jacob")
                .soleTraderLastName("Martin")
                .type(Party.Type.SOLE_TRADER).build();

            assertEquals("Mr Jacob Martin", PartyUtils.getLitigiousPartyName(soleTrader, null));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsSoleTraderAndTradingAs() {
            Party soleTrader = Party.builder()
                .soleTraderTitle("Mr")
                .soleTraderFirstName("Jacob")
                .soleTraderLastName("Martin")
                .soleTraderTradingAs("Trading Co")
                .type(Party.Type.SOLE_TRADER).build();

            assertEquals("Mr Jacob Martin T/A Trading Co",
                         PartyUtils.getLitigiousPartyName(soleTrader, null));
        }
    }

    @Nested
    class PartyDateOfBirth {

        @ParameterizedTest
        @EnumSource(value = Party.Type.class, names = {"SOLE_TRADER", "INDIVIDUAL"})
        void shouldReturnDateOfBirth_whenPartyTypeIsIndividualOrSoleTrader(Party.Type type) {
            Party party = PartyBuilder.builder().ofType(type).build();

            assertThat(PartyUtils.getDateOfBirth(party)).contains(DATE_OF_BIRTH);
        }

        @ParameterizedTest
        @EnumSource(value = Party.Type.class, mode = EnumSource.Mode.EXCLUDE, names = {"SOLE_TRADER", "INDIVIDUAL"})
        void shouldReturnEmpty_whenPartyTypeIsNotIndividualOrSoleTrader(Party.Type type) {
            Party party = PartyBuilder.builder().ofType(type).build();

            assertThat(PartyUtils.getDateOfBirth(party)).isEmpty();
        }
    }

    @Nested
    class PartyReferences {

        @Test
        void shouldReturnReferences_whenNoRefsAvailable() {
            CaseData caseData = CaseDataBuilder.builder().build();

            String partyReferences = PartyUtils.buildPartiesReferences(caseData);

            assertEquals("", partyReferences);
        }

        @Test
        void shouldReturnReferences_whenCaseHasOneDefendantSolicitorAndBothRefsAvailable() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

            String partyReferences = PartyUtils.buildPartiesReferences(caseData);

            assertEquals("Claimant reference: 12345\nDefendant reference: 6789", partyReferences);
        }

        @Test
        void shouldReturnReferences_when1v2DiffSolicitorAndOnlyClaimantRefAndSol2RefAvailable() {
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .solicitorReferences(SolicitorReferences.builder().applicantSolicitor1Reference("App One").build())
                .respondentSolicitor2Reference("Def Two")
                .build();

            String partyReferences = PartyUtils.buildPartiesReferences(caseData);

            assertEquals("Claimant reference: App One\nDefendant 2 reference: Def Two", partyReferences);
        }

        @Test
        void shouldReturnReferences_when1v2DiffSolicitorCase() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .respondentSolicitor2Reference("defendant sol 2")
                .build();

            String partyReferences = PartyUtils.buildPartiesReferences(caseData);

            assertEquals("Claimant reference: 12345\nDefendant 1 reference: 6789\nDefendant 2 reference: "
                             + "defendant sol 2",
                         partyReferences);
        }

        @Test
        void shouldReturnRespondentReference2_when1v2DiffSolicitorCase() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .solicitorReferences(SolicitorReferences.builder()
                                         .respondentSolicitor2Reference("defendant sol 2").build())
                .build();

            String respondentReference = PartyUtils.buildRespondentReference(caseData, true);

            assertEquals("defendant sol 2",
                         respondentReference);
        }

        @Test
        void shouldReturnRespondentReference1_when1v2DiffSolicitorCase() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .solicitorReferences(SolicitorReferences.builder()
                                         .respondentSolicitor1Reference("defendant sol 1").build())
                .build();

            String respondentReference = PartyUtils.buildRespondentReference(caseData, false);

            assertEquals("defendant sol 1",
                         respondentReference);
        }

        @Test
        void shouldReturnApplicantReference1_when1v2DiffSolicitorCase() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .solicitorReferences(SolicitorReferences.builder()
                                         .applicantSolicitor1Reference("applicant sol").build())
                .build();

            String respondentReference = PartyUtils.buildClaimantReferenceOnly(caseData);

            assertEquals("applicant sol",
                         respondentReference);
        }
    }

    @Nested
    class ClaimantReferences {

        @Test
        void shouldReturnEmptyReferences_whenNoRefsAvailable() {
            CaseData caseData = CaseDataBuilder.builder().build();

            String partyReferences = PartyUtils.buildClaimantReference(caseData);

            assertEquals("", partyReferences);
        }

        @Test
        void shouldReturnClaimaintReferences_whenCaseHasRefsAvailable() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

            String partyReferences = PartyUtils.buildClaimantReference(caseData);

            assertEquals("Claimant reference: 12345", partyReferences);
        }
    }

    @Nested
    class PartyResponseType {

        @Test
        void shouldReturnCorrectResponseTypeFor1v1Cases() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();

            assertEquals(PartyUtils.getResponseTypeForRespondent(
                caseData,
                caseData.getRespondent1()
            ), FULL_DEFENCE);

            CaseData partAdmissionCaseData = CaseDataBuilder.builder().atStateRespondentPartAdmission().build();

            assertEquals(PartyUtils.getResponseTypeForRespondent(
                partAdmissionCaseData,
                partAdmissionCaseData.getRespondent1()
            ), PART_ADMISSION);
        }

        @Test
        void shouldReturnCorrectResponseTypeFor1v2Cases() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .build();

            assertEquals(PartyUtils.getResponseTypeForRespondent(
                caseData,
                caseData.getRespondent1()
            ), FULL_DEFENCE);

            assertEquals(PartyUtils.getResponseTypeForRespondent(
                caseData,
                caseData.getRespondent2()
            ), FULL_DEFENCE);

            CaseData partAdmissionCaseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullAdmission()
                .respondent2Responds(PART_ADMISSION)
                .build();

            assertEquals(PartyUtils.getResponseTypeForRespondent(
                partAdmissionCaseData,
                partAdmissionCaseData.getRespondent1()
            ), FULL_ADMISSION);

            assertEquals(PartyUtils.getResponseTypeForRespondent(
                partAdmissionCaseData,
                partAdmissionCaseData.getRespondent2()
            ), PART_ADMISSION);
        }

        @Test
        void shouldReturnCorrectResponseTypeFor1v1CasesSpec() {
            CaseData fullDefenceCaseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build();

            assertEquals(RespondentResponseTypeSpec.FULL_DEFENCE,
                         PartyUtils.getResponseTypeForRespondentSpec(
                             fullDefenceCaseData,
                             fullDefenceCaseData.getRespondent1()
                         )
            );

            CaseData partAdmissionCaseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();

            assertEquals(RespondentResponseTypeSpec.PART_ADMISSION,
                         PartyUtils.getResponseTypeForRespondentSpec(
                             partAdmissionCaseData,
                             partAdmissionCaseData.getRespondent1()
                         )
            );

            CaseData fullAdmissionCaseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();

            assertEquals(RespondentResponseTypeSpec.FULL_ADMISSION,
                         PartyUtils.getResponseTypeForRespondentSpec(
                             fullAdmissionCaseData,
                             fullAdmissionCaseData.getRespondent1()
                         )
            );

            CaseData counterClaimCaseData = CaseDataBuilder.builder().atStateRespondentCounterClaimSpec().build();

            assertEquals(RespondentResponseTypeSpec.COUNTER_CLAIM,
                         PartyUtils.getResponseTypeForRespondentSpec(
                             counterClaimCaseData,
                             counterClaimCaseData.getRespondent1()
                         )
            );
        }

        @Test
        void shouldReturnCorrectResponseTypeFor1v2CasesSpec() {
            CaseData divergentResponseCaseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullAdmissionSpec()
                .respondent2RespondsSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .build();

            assertEquals(RespondentResponseTypeSpec.FULL_ADMISSION,
                         PartyUtils.getResponseTypeForRespondentSpec(
                             divergentResponseCaseData,
                             divergentResponseCaseData.getRespondent1()
                         )
            );

            assertEquals(RespondentResponseTypeSpec.PART_ADMISSION,
                         PartyUtils.getResponseTypeForRespondentSpec(
                             divergentResponseCaseData,
                             divergentResponseCaseData.getRespondent2()
                         )
            );

            CaseData counterClaimCaseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentCounterClaimSpec()
                .respondent2RespondsSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                .build();

            assertEquals(RespondentResponseTypeSpec.COUNTER_CLAIM,
                         PartyUtils.getResponseTypeForRespondentSpec(
                             counterClaimCaseData,
                             counterClaimCaseData.getRespondent1()
                         )
            );

            assertEquals(RespondentResponseTypeSpec.COUNTER_CLAIM,
                         PartyUtils.getResponseTypeForRespondentSpec(
                             counterClaimCaseData,
                             counterClaimCaseData.getRespondent2()
                         )
            );
        }
    }

    @Nested
    class RespondentData {

        @Test
        void shouldReturnExpectedData_forRespondent1() {
            LocalDateTime datetime = LocalDateTime.now();
            LocalDate date = LocalDate.now();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1TimeExtensionDate(datetime)
                .respondentSolicitor1AgreedDeadlineExtension(date)
                .build();

            PartyData expectedData = PartyData.builder()
                .role(RESPONDENT_ONE)
                .details(caseData.getRespondent1())
                .timeExtensionDate(datetime)
                .solicitorAgreedDeadlineExtension(date)
                .build();

            PartyData actualData = PartyUtils.respondent1Data(caseData);

            assertEquals(expectedData, actualData);
        }

        @Test
        void shouldReturnExpectedData_forRespondent2() {
            LocalDateTime datetime = LocalDateTime.now();
            LocalDate date = LocalDate.now();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2TimeExtensionDate(datetime)
                .respondentSolicitor2AgreedDeadlineExtension(date)
                .build();

            PartyData expectedData = PartyData.builder()
                .role(RESPONDENT_TWO)
                .details(caseData.getRespondent2())
                .timeExtensionDate(datetime)
                .solicitorAgreedDeadlineExtension(date)
                .build();

            PartyData actualData = PartyUtils.respondent2Data(caseData);

            assertEquals(expectedData, actualData);
        }
    }

    @Nested
    class AppendWithNewPartyId {

        @Test
        void shouldAddPartyId_toGivenParty() {
            Party party = Party.builder().partyName("mock party").build();

            Party actual = PartyUtils.appendWithNewPartyId(party);

            assertNotNull(actual.getPartyID());
        }

        @Test
        void shouldNotAppendPartyId_whenPartyIdExists() {
            Party expected = Party.builder()
                .partyID(UUID.randomUUID().toString())
                .partyName("mock party")
                .build();

            Party actual = PartyUtils.appendWithNewPartyId(expected);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnNull_whenGivenPartyIsNull() {
            Party party = null;

            Party actual = PartyUtils.appendWithNewPartyId(party);

            assertNull(actual);
        }

        @Test
        void shouldAddPartyId_toGivenLitigationFriend() {
            LitigationFriend litigationFriend = LitigationFriend.builder()
                .firstName("mock party").build();

            LitigationFriend actual = PartyUtils.appendWithNewPartyId(litigationFriend);

            assertNotNull(actual.getPartyID());
        }

        @Test
        void shouldNotAppendPartyId_whenLitigationFriendPartyIdExists() {
            LitigationFriend expected = LitigationFriend.builder()
                .partyID(UUID.randomUUID().toString())
                .firstName("litfriend")
                .build();

            LitigationFriend actual = PartyUtils.appendWithNewPartyId(expected);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnNull_whenGivenLitigationFriendIsNull() {
            LitigationFriend litigationFriend = null;

            LitigationFriend actual = PartyUtils.appendWithNewPartyId(litigationFriend);

            assertNull(actual);
        }

        @Test
        void shouldAddPartyId_toGivenPartyFlagStructure() {
            PartyFlagStructure partyFlagStructure = PartyFlagStructure.builder()
                .firstName("mock party").build();

            PartyFlagStructure actual = PartyUtils.appendWithNewPartyId(partyFlagStructure);

            assertNotNull(actual.getPartyID());
        }

        @Test
        void shouldNotAppendPartyId_whenPartyFlagStructurePartyIdExists() {
            PartyFlagStructure expected = PartyFlagStructure.builder()
                .partyID(UUID.randomUUID().toString())
                .firstName("structure")
                .build();

            PartyFlagStructure actual = PartyUtils.appendWithNewPartyId(expected);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnNull_whenGivenPartyFlagStructureIsNull() {
            PartyFlagStructure partyFlagStructure = null;

            PartyFlagStructure actual = PartyUtils.appendWithNewPartyId(partyFlagStructure);

            assertNull(actual);
        }
    }

    @Nested
    class AppendWithNewPartyIds {

        @Test
        void shouldAddPartyIds_toGivenListOfPartyFlagStructures() {
            List<Element<PartyFlagStructure>> partyFlagStructures = wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("structure").build()
            ));

            var actual = PartyUtils.appendWithNewPartyIds(partyFlagStructures);

            assertEquals(partyFlagStructures.size(), actual.size());
            assertNotNull(actual.get(0).getValue().getPartyID());
        }

        @Test
        void shouldNotAppendParty_whenPartyIdExists() {
            List<Element<PartyFlagStructure>> expected = wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("structure")
                    .partyID("some id")
                    .build()
            ));

            List<Element<PartyFlagStructure>> actual = PartyUtils.appendWithNewPartyIds(expected);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnNull_whenGivenListOfPartyFlagStructuresIsNull() {
            List<Element<PartyFlagStructure>> party = null;

            List<Element<PartyFlagStructure>> actual = PartyUtils.appendWithNewPartyIds(party);

            assertNull(actual);
        }
    }

    @Nested
    class PopulateWithPartyIds {

        @Test
        void shouldPopulatePartyIds_withinGivenCaseDataBuilder() {
            CaseData.CaseDataBuilder builder = CaseData.builder()
                .applicant1(Party.builder().partyName("mock party 1").build())
                .applicant2(Party.builder().partyName("mock party 3").build())
                .respondent1(Party.builder().partyName("mock party 4").build())
                .respondent2(Party.builder().partyName("mock party 5").build())
                .applicant1LitigationFriend(LitigationFriend.builder().firstName("mock litfriend 1").build())
                .respondent1LitigationFriend(LitigationFriend.builder().firstName("mock litfriend 2").build())
                .respondent2LitigationFriend(LitigationFriend.builder().firstName("mock litfriend 3").build());

            PartyUtils.populateWithPartyIds(builder);
            CaseData actual = builder.build();

            assertNotNull(actual.getApplicant1().getPartyID());
            assertNotNull(actual.getApplicant2().getPartyID());
            assertNotNull(actual.getRespondent1().getPartyID());
            assertNotNull(actual.getRespondent2().getPartyID());
            assertNotNull(actual.getApplicant1LitigationFriend().getPartyID());
            assertNotNull(actual.getRespondent1LitigationFriend().getPartyID());
            assertNotNull(actual.getRespondent2LitigationFriend().getPartyID());
        }

        @Test
        void shouldNotPopulateAlreadyNullPartyFields_withinCaseDataBuilder() {
            CaseData.CaseDataBuilder builder = CaseData.builder()
                .applicant1(Party.builder().partyName("mock party 1").build())
                .respondent1(Party.builder().partyName("mock party 4").build())
                .applicant1LitigationFriend(LitigationFriend.builder().firstName("mock litfriend 1").build())
                .respondent1LitigationFriend(LitigationFriend.builder().firstName("mock litfriend 2").build());

            PartyUtils.populateWithPartyIds(builder);
            CaseData actual = builder.build();

            assertNull(actual.getApplicant2());
            assertNull(actual.getRespondent2());
            assertNull(actual.getRespondent2LitigationFriend());
        }
    }

    @Nested
    class PopulateDQPartyIds {

        @Test
        void shouldDQPopulatePartyIds_withinGivenCaseDataBuilder() {
            CaseData.CaseDataBuilder builder = CaseData.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQWitnesses(buildWitnesses("app1witness"))
                                  .applicant1DQExperts(buildExperts("app1expert"))
                                  .applicant1RespondToClaimExperts(buildExpertDetails("app1expertdetails"))
                                  .build())
                .applicant2DQ(Applicant2DQ.builder()
                                  .applicant2DQWitnesses(buildWitnesses("app2witness"))
                                  .applicant2DQExperts(buildExperts("app2expert"))
                                  .applicant2RespondToClaimExperts(buildExpertDetails("app2expertdetails"))
                                  .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQWitnesses(buildWitnesses("res1witness"))
                                   .respondent1DQExperts(buildExperts("res1expert"))
                                   .respondToClaimExperts(buildExpertDetails("res1expertdetails"))
                                   .build())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQWitnesses(buildWitnesses("res2witness"))
                                   .respondent2DQExperts(buildExperts("res2expert"))
                                   .respondToClaimExperts2(buildExpertDetails("res2expertdetails"))
                                   .build());

            PartyUtils.populateDQPartyIds(builder);

            CaseData actual = builder.build();

            var app1Witness = unwrapElements(actual.getApplicant1DQ().getApplicant1DQWitnesses().getDetails()).get(0);
            assertNotNull(app1Witness.getPartyID());
            assertEquals(app1Witness.getFirstName(), "app1witness");

            var app2Witness = unwrapElements(actual.getApplicant2DQ().getApplicant2DQWitnesses().getDetails()).get(0);
            assertNotNull(app2Witness.getPartyID());
            assertEquals(app2Witness.getFirstName(), "app2witness");

            var res1Witness = unwrapElements(actual.getRespondent1DQ().getRespondent1DQWitnesses().getDetails()).get(0);
            assertNotNull(res1Witness.getPartyID());
            assertEquals(res1Witness.getFirstName(), "res1witness");

            var res2Witness = unwrapElements(actual.getRespondent2DQ().getRespondent2DQWitnesses().getDetails()).get(0);
            assertNotNull(res2Witness.getPartyID());
            assertEquals(res2Witness.getFirstName(), "res2witness");

            var app1Expert = unwrapElements(actual.getApplicant1DQ().getApplicant1DQExperts().getDetails()).get(0);
            assertNotNull(app1Expert.getPartyID());
            assertEquals(app1Expert.getFirstName(), "app1expert");

            var app1ExpertDetails = actual.getApplicant1DQ().getApplicant1RespondToClaimExperts();
            assertNotNull(app1ExpertDetails.getPartyID());
            assertEquals(app1ExpertDetails.getFirstName(), "app1expertdetails");

            var app2Expert = unwrapElements(actual.getApplicant2DQ().getApplicant2DQExperts().getDetails()).get(0);
            assertNotNull(app2Expert.getPartyID());
            assertEquals(app2Expert.getFirstName(), "app2expert");

            var app2ExpertDetails = actual.getApplicant2DQ().getApplicant2RespondToClaimExperts();
            assertNotNull(app2ExpertDetails.getPartyID());
            assertEquals(app2ExpertDetails.getFirstName(), "app2expertdetails");

            var res1Expert = unwrapElements(actual.getRespondent1DQ().getRespondent1DQExperts().getDetails()).get(0);
            assertNotNull(res1Expert.getPartyID());
            assertEquals(res1Expert.getFirstName(), "res1expert");

            var res1ExpertDetails = actual.getRespondent1DQ().getRespondToClaimExperts();
            assertNotNull(res1ExpertDetails.getPartyID());
            assertEquals(res1ExpertDetails.getFirstName(), "res1expertdetails");

            var res2Expert = unwrapElements(actual.getRespondent2DQ().getRespondent2DQExperts().getDetails()).get(0);
            assertNotNull(res2Expert.getPartyID());
            assertEquals(res2Expert.getFirstName(), "res2expert");

            var res2ExpertDetails = actual.getRespondent2DQ().getRespondToClaimExperts2();
            assertNotNull(res2ExpertDetails.getPartyID());
            assertEquals(res2ExpertDetails.getFirstName(), "res2expertdetails");
        }

        @Test
        void shouldNotOverWritePartyIds_whenPartyIdsExist() {
            CaseData.CaseDataBuilder builder = CaseData.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQWitnesses(buildWitnesses("app1witness", "app1witnesspartyid"))
                                  .applicant1DQExperts(buildExperts("app1expert", "app1expertpartyid"))
                                  .applicant1RespondToClaimExperts(buildExpertDetails("app1expertdetails", "app1expertdetailspartyid"))
                                  .build())
                .applicant2DQ(Applicant2DQ.builder()
                                  .applicant2DQWitnesses(buildWitnesses("app2witness", "app2witnesspartyid"))
                                  .applicant2DQExperts(buildExperts("app2expert", "app2expertpartyid"))
                                  .applicant2RespondToClaimExperts(buildExpertDetails("app2expertdetails", "app2expertdetailspartyid"))
                                  .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQWitnesses(buildWitnesses("res1witness", "res1witnesspartyid"))
                                   .respondent1DQExperts(buildExperts("res1expert", "res1expertpartyid"))
                                   .respondToClaimExperts(buildExpertDetails("res1expertdetails", "res1expertdetailspartyid"))
                                   .build())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQWitnesses(buildWitnesses("res2witness", "res2witnesspartyid"))
                                   .respondent2DQExperts(buildExperts("res2expert", "res2expertpartyid"))
                                   .respondToClaimExperts2(buildExpertDetails("res2expertdetails", "res2expertdetailspartyid"))
                                   .build());

            PartyUtils.populateDQPartyIds(builder);

            CaseData actual = builder.build();

            var app1Witness = unwrapElements(actual.getApplicant1DQ().getApplicant1DQWitnesses().getDetails()).get(0);
            assertEquals(app1Witness.getPartyID(), "app1witnesspartyid");

            var app2Witness = unwrapElements(actual.getApplicant2DQ().getApplicant2DQWitnesses().getDetails()).get(0);
            assertEquals(app2Witness.getPartyID(), "app2witnesspartyid");

            var res1Witness = unwrapElements(actual.getRespondent1DQ().getRespondent1DQWitnesses().getDetails()).get(0);
            assertEquals(res1Witness.getPartyID(), "res1witnesspartyid");

            var res2Witness = unwrapElements(actual.getRespondent2DQ().getRespondent2DQWitnesses().getDetails()).get(0);
            assertEquals(res2Witness.getPartyID(), "res2witnesspartyid");

            var app1Expert = unwrapElements(actual.getApplicant1DQ().getApplicant1DQExperts().getDetails()).get(0);
            assertEquals(app1Expert.getPartyID(), "app1expertpartyid");

            var app1ExpertDetails = actual.getApplicant1DQ().getApplicant1RespondToClaimExperts();
            assertEquals(app1ExpertDetails.getPartyID(), "app1expertdetailspartyid");

            var app2Expert = unwrapElements(actual.getApplicant2DQ().getApplicant2DQExperts().getDetails()).get(0);
            assertEquals(app2Expert.getPartyID(), "app2expertpartyid");

            var app2ExpertDetails = actual.getApplicant2DQ().getApplicant2RespondToClaimExperts();
            assertEquals(app2ExpertDetails.getPartyID(), "app2expertdetailspartyid");

            var res1Expert = unwrapElements(actual.getRespondent1DQ().getRespondent1DQExperts().getDetails()).get(0);
            assertEquals(res1Expert.getPartyID(), "res1expertpartyid");

            var res1ExpertDetails = actual.getRespondent1DQ().getRespondToClaimExperts();
            assertEquals(res1ExpertDetails.getPartyID(), "res1expertdetailspartyid");

            var res2Expert = unwrapElements(actual.getRespondent2DQ().getRespondent2DQExperts().getDetails()).get(0);
            assertEquals(res2Expert.getPartyID(), "res2expertpartyid");

            var res2ExpertDetails = actual.getRespondent2DQ().getRespondToClaimExperts2();
            assertEquals(res2ExpertDetails.getPartyID(), "res2expertdetailspartyid");
        }

        @Test
        void shouldReturnNullWitnessExpertFields_whenCaseDataBuilderHasNullWitnessExpertFields() {
            CaseData.CaseDataBuilder builder = CaseData.builder()
                .applicant1DQ(Applicant1DQ.builder().build())
                .applicant2DQ(Applicant2DQ.builder().build())
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent2DQ(Respondent2DQ.builder().build());

            PartyUtils.populateDQPartyIds(builder);
            CaseData actual = builder.build();

            assertNull(actual.getApplicant1DQ().getApplicant1DQWitnesses());
            assertNull(actual.getApplicant2DQ().getApplicant2DQWitnesses());
            assertNull(actual.getRespondent1DQ().getRespondent1DQWitnesses());
            assertNull(actual.getRespondent2DQ().getRespondent2DQWitnesses());
            assertNull(actual.getApplicant1DQ().getApplicant1DQExperts());
            assertNull(actual.getApplicant1DQ().getApplicant1RespondToClaimExperts());
            assertNull(actual.getApplicant2DQ().getApplicant2DQExperts());
            assertNull(actual.getApplicant2DQ().getApplicant2RespondToClaimExperts());
            assertNull(actual.getRespondent1DQ().getRespondent1DQExperts());
            assertNull(actual.getRespondent1DQ().getRespondToClaimExperts());
            assertNull(actual.getRespondent2DQ().getRespondent2DQExperts());
            assertNull(actual.getRespondent2DQ().getRespondToClaimExperts2());
        }

        @Test
        void shouldReturnNullDQFields_whenCaseDataBuilderHasNullDQFields() {
            CaseData.CaseDataBuilder builder = CaseData.builder();

            PartyUtils.populateDQPartyIds(builder);
            CaseData actual = builder.build();

            assertNull(actual.getApplicant1DQ());
            assertNull(actual.getApplicant2DQ());
            assertNull(actual.getRespondent1DQ());
            assertNull(actual.getRespondent2DQ());
        }

        private Witnesses buildWitnesses(String firstName, String partyId) {
            return Witnesses.builder().details(
                wrapElements(List.of(Witness.builder().partyID(partyId).firstName(firstName).build()))).build();
        }

        private Witnesses buildWitnesses(String firstName) {
            return buildWitnesses(firstName, null);
        }

        private Experts buildExperts(String firstName, String partyId) {
            return Experts.builder().details(
                wrapElements(List.of(Expert.builder().partyID(partyId).firstName(firstName).build()))).build();
        }

        private Experts buildExperts(String firstName) {
            return buildExperts(firstName, null);
        }

        private ExpertDetails buildExpertDetails(String firstName, String partyId) {
            return ExpertDetails.builder().partyID(partyId).firstName(firstName).build();
        }

        private ExpertDetails buildExpertDetails(String firstName) {
            return buildExpertDetails(firstName, null);
        }
    }
}
