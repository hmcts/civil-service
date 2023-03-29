package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
            LitigationFriend litigationFriend = LitigationFriend.builder().fullName("Mr Litigious Friend").build();
            assertEquals("Mr Jacob Martin L/F Mr Litigious Friend",
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
                    .firstName("structure").build()
            ));

            List<Element<PartyFlagStructure>> actual = PartyUtils.appendWithNewPartyIds(expected);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnNull_whenGiventoGivenListOfPartyFlagStructuresIsNull() {
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
                .respondent2LitigationFriend(LitigationFriend.builder().firstName("mock litfriend 3").build())
                .applicantExperts(wrapElements(List.of(PartyFlagStructure.builder().firstName("expert 1").build())))
                .respondent1Experts(wrapElements(List.of(PartyFlagStructure.builder().firstName("expert 2").build())))
                .respondent2Experts(wrapElements(List.of(PartyFlagStructure.builder().firstName("expert 3").build())))
                .applicantWitnesses(wrapElements(List.of(PartyFlagStructure.builder().firstName("witness 1").build())))
                .respondent1Witnesses(wrapElements(List.of(PartyFlagStructure.builder().firstName("witness 2").build())))
                .respondent2Witnesses(wrapElements(List.of(PartyFlagStructure.builder().firstName("witness 3").build())));

            PartyUtils.populateWithPartyIds(builder);
            CaseData actual = builder.build();

            assertNotNull(actual.getApplicant1().getPartyID());
            assertNotNull(actual.getApplicant2().getPartyID());
            assertNotNull(actual.getRespondent1().getPartyID());
            assertNotNull(actual.getRespondent2().getPartyID());
            assertNotNull(actual.getApplicant1LitigationFriend().getPartyID());
            assertNotNull(actual.getRespondent1LitigationFriend().getPartyID());
            assertNotNull(actual.getRespondent2LitigationFriend().getPartyID());
            assertNotNull(actual.getApplicantExperts().get(0).getValue().getPartyID());
            assertNotNull(actual.getRespondent1Experts().get(0).getValue().getPartyID());
            assertNotNull(actual.getRespondent2Experts().get(0).getValue().getPartyID());
            assertNotNull(actual.getApplicantWitnesses().get(0).getValue().getPartyID());
            assertNotNull(actual.getRespondent1Witnesses().get(0).getValue().getPartyID());
            assertNotNull(actual.getRespondent2Witnesses().get(0).getValue().getPartyID());
        }

        @Test
        void shouldNotPopulateAlreadyNullPartyFields_withinCaseDataBuilder() {
            CaseData.CaseDataBuilder builder = CaseData.builder()
                .applicant1(Party.builder().partyName("mock party 1").build())
                .respondent1(Party.builder().partyName("mock party 4").build())
                .applicant1LitigationFriend(LitigationFriend.builder().firstName("mock litfriend 1").build())
                .respondent1LitigationFriend(LitigationFriend.builder().firstName("mock litfriend 2").build())
                .applicantExperts(wrapElements(List.of(PartyFlagStructure.builder().firstName("expert 1").build())))
                .respondent1Experts(wrapElements(List.of(PartyFlagStructure.builder().firstName("expert 2").build())))
                .applicantWitnesses(wrapElements(List.of(PartyFlagStructure.builder().firstName("witness 1").build())))
                .respondent1Witnesses(wrapElements(List.of(PartyFlagStructure.builder().firstName("witness 2").build())));

            PartyUtils.populateWithPartyIds(builder);
            CaseData actual = builder.build();

            assertNull(actual.getApplicant2());
            assertNull(actual.getRespondent2());
            assertNull(actual.getRespondent2LitigationFriend());
            assertNull(actual.getRespondent2Experts());
            assertNull(actual.getRespondent2Witnesses());
        }
    }
}
