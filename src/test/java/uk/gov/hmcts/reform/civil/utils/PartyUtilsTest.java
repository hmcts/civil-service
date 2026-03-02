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
            Party party = new Party();
            party.setType(null);
            assertThrows(NullPointerException.class, () -> PartyUtils.getPartyNameBasedOnType(party));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsIndividual() {
            Party individual = new Party();
            individual.setIndividualTitle("Mr");
            individual.setIndividualFirstName("Jacob");
            individual.setIndividualLastName("Martin");
            individual.setType(Party.Type.INDIVIDUAL);

            assertEquals("Mr Jacob Martin", PartyUtils.getPartyNameBasedOnType(individual));
        }

        @Test
        void shouldProvideNameWithoutTitle_whenPartyTypeIsIndividual() {
            Party individual = new Party();
            individual.setIndividualTitle("Mr");
            individual.setIndividualFirstName("Jacob");
            individual.setIndividualLastName("Martin");
            individual.setType(Party.Type.INDIVIDUAL);

            assertEquals("Jacob Martin", PartyUtils.getPartyNameBasedOnType(individual, true));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsIndividualWithoutTitle() {
            Party individual = new Party();
            individual.setIndividualFirstName("Jacob");
            individual.setIndividualLastName("Martin");
            individual.setType(Party.Type.INDIVIDUAL);

            assertEquals("Jacob Martin", PartyUtils.getPartyNameBasedOnType(individual));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsCompany() {
            Party individual = new Party();
            individual.setCompanyName("XYZ Company House");
            individual.setType(Party.Type.COMPANY);

            assertEquals("XYZ Company House", PartyUtils.getPartyNameBasedOnType(individual));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsOrganisation() {
            Party organisation = new Party();
            organisation.setOrganisationName("ABC Solutions");
            organisation.setType(Party.Type.ORGANISATION);

            assertEquals("ABC Solutions", PartyUtils.getPartyNameBasedOnType(organisation));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsSoleTrader() {
            Party soleTrader = new Party();
            soleTrader.setSoleTraderTitle("Mr");
            soleTrader.setSoleTraderFirstName("Jacob");
            soleTrader.setSoleTraderLastName("Martin");
            soleTrader.setType(Party.Type.SOLE_TRADER);

            assertEquals("Mr Jacob Martin", PartyUtils.getPartyNameBasedOnType(soleTrader));
        }

        @Test
        void shouldProvideNameWithoutTitle_whenPartyTypeIsSoleTrader() {
            Party soleTrader = new Party();
            soleTrader.setSoleTraderTitle("Mr");
            soleTrader.setSoleTraderFirstName("Jacob");
            soleTrader.setSoleTraderLastName("Martin");
            soleTrader.setType(Party.Type.SOLE_TRADER);

            assertEquals("Jacob Martin", PartyUtils.getPartyNameBasedOnType(soleTrader, true));
        }
    }

    @Nested
    class LitigiousPartyName {

        @Test
        void shouldThrowNullPointer_whenPartyTypeIsNull() {
            Party party = new Party();
            party.setType(null);
            LitigationFriend litigationFriend = new LitigationFriend();
            assertThrows(NullPointerException.class, () -> PartyUtils.getLitigiousPartyName(party, litigationFriend));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsIndividualAndNoLitigationFriend() {
            Party individual = new Party();
            individual.setIndividualTitle("Mr");
            individual.setIndividualFirstName("Jacob");
            individual.setIndividualLastName("Martin");
            individual.setType(Party.Type.INDIVIDUAL);

            assertEquals("Mr Jacob Martin", PartyUtils.getLitigiousPartyName(individual, null));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsIndividualAndLitigationFriend() {
            Party individual = new Party();
            individual.setIndividualTitle("Mr");
            individual.setIndividualFirstName("Jacob");
            individual.setIndividualLastName("Martin");
            individual.setType(Party.Type.INDIVIDUAL);
            LitigationFriend litigationFriend = new LitigationFriend();
            litigationFriend.setFirstName("Litigious");
            litigationFriend.setLastName("Friend");
            assertEquals(
                "Mr Jacob Martin L/F Litigious Friend",
                PartyUtils.getLitigiousPartyName(individual, litigationFriend)
            );
        }

        @Test
        void shouldProvideName_whenPartyTypeIsCompany() {
            Party individual = new Party();
            individual.setCompanyName("XYZ Company House");
            individual.setType(Party.Type.COMPANY);

            assertEquals("XYZ Company House", PartyUtils.getLitigiousPartyName(individual, null));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsOrganisation() {
            Party organisation = new Party();
            organisation.setOrganisationName("ABC Solutions");
            organisation.setType(Party.Type.ORGANISATION);

            assertEquals("ABC Solutions", PartyUtils.getLitigiousPartyName(organisation, null));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsSoleTrader() {
            Party soleTrader = new Party();
            soleTrader.setSoleTraderTitle("Mr");
            soleTrader.setSoleTraderFirstName("Jacob");
            soleTrader.setSoleTraderLastName("Martin");
            soleTrader.setType(Party.Type.SOLE_TRADER);

            assertEquals("Mr Jacob Martin", PartyUtils.getLitigiousPartyName(soleTrader, null));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsSoleTraderAndTradingAs() {
            Party soleTrader = new Party();
            soleTrader.setSoleTraderTitle("Mr");
            soleTrader.setSoleTraderFirstName("Jacob");
            soleTrader.setSoleTraderLastName("Martin");
            soleTrader.setSoleTraderTradingAs("Trading Co");
            soleTrader.setType(Party.Type.SOLE_TRADER);

            assertEquals(
                "Mr Jacob Martin T/A Trading Co",
                PartyUtils.getLitigiousPartyName(soleTrader, null)
            );
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
            CaseData caseData = CaseDataBuilder.builder().build();
            SolicitorReferences solicitorReferences = new SolicitorReferences();
            solicitorReferences.setApplicantSolicitor1Reference("App One");
            caseData.setSolicitorReferences(solicitorReferences);
            caseData.setRespondentSolicitor2Reference("Def Two");

            String partyReferences = PartyUtils.buildPartiesReferences(caseData);

            assertEquals("Claimant reference: App One\nDefendant 2 reference: Def Two", partyReferences);
        }

        @Test
        void shouldReturnReferences_when1v2DiffSolicitorCase() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setRespondentSolicitor2Reference("defendant sol 2");

            String partyReferences = PartyUtils.buildPartiesReferences(caseData);

            assertEquals(
                """
                Claimant reference: 12345
                Defendant 1 reference: 6789
                Defendant 2 reference: defendant sol 2""",
                partyReferences
            );
        }

        @Test
        void shouldReturnRespondentReference2_when1v2DiffSolicitorCase() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            SolicitorReferences solicitorReferences = new SolicitorReferences();
            solicitorReferences.setRespondentSolicitor2Reference("defendant sol 2");
            caseData.setSolicitorReferences(solicitorReferences);

            String respondentReference = PartyUtils.buildRespondentReference(caseData, true);

            assertEquals(
                "defendant sol 2",
                respondentReference
            );
        }

        @Test
        void shouldReturnRespondentReference1_when1v2DiffSolicitorCase() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            SolicitorReferences solicitorReferences = new SolicitorReferences();
            solicitorReferences.setRespondentSolicitor1Reference("defendant sol 1");
            caseData.setSolicitorReferences(solicitorReferences);

            String respondentReference = PartyUtils.buildRespondentReference(caseData, false);

            assertEquals(
                "defendant sol 1",
                respondentReference
            );
        }

        @Test
        void shouldReturnApplicantReference1_when1v2DiffSolicitorCase() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            SolicitorReferences solicitorReferences = new SolicitorReferences();
            solicitorReferences.setApplicantSolicitor1Reference("applicant sol");
            caseData.setSolicitorReferences(solicitorReferences);

            String respondentReference = PartyUtils.buildClaimantReferenceOnly(caseData);

            assertEquals(
                "applicant sol",
                respondentReference
            );
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
            assertEquals(
                FULL_DEFENCE,
                PartyUtils.getResponseTypeForRespondent(
                    caseData,
                    caseData.getRespondent1()
                )
            );

            CaseData partAdmissionCaseData = CaseDataBuilder.builder().atStateRespondentPartAdmission().build();

            assertEquals(
                PART_ADMISSION,
                PartyUtils.getResponseTypeForRespondent(
                    partAdmissionCaseData,
                    partAdmissionCaseData.getRespondent1()
                )
            );
        }

        @Test
        void shouldReturnCorrectResponseTypeFor1v2Cases() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .build();

            assertEquals(
                FULL_DEFENCE,
                PartyUtils.getResponseTypeForRespondent(
                    caseData,
                    caseData.getRespondent1()
                )
            );

            assertEquals(
                FULL_DEFENCE,
                PartyUtils.getResponseTypeForRespondent(
                    caseData,
                    caseData.getRespondent2()
                )
            );

            CaseData partAdmissionCaseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullAdmission()
                .respondent2Responds(PART_ADMISSION)
                .build();

            assertEquals(
                FULL_ADMISSION,
                PartyUtils.getResponseTypeForRespondent(
                    partAdmissionCaseData,
                    partAdmissionCaseData.getRespondent1()
                )
            );

            assertEquals(
                PART_ADMISSION,
                PartyUtils.getResponseTypeForRespondent(
                    partAdmissionCaseData,
                    partAdmissionCaseData.getRespondent2()
                )
            );
        }

        @Test
        void shouldReturnCorrectResponseTypeFor1v1CasesSpec() {
            CaseData fullDefenceCaseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build();

            assertEquals(
                RespondentResponseTypeSpec.FULL_DEFENCE,
                PartyUtils.getResponseTypeForRespondentSpec(
                    fullDefenceCaseData,
                    fullDefenceCaseData.getRespondent1()
                )
            );

            CaseData partAdmissionCaseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();

            assertEquals(
                RespondentResponseTypeSpec.PART_ADMISSION,
                PartyUtils.getResponseTypeForRespondentSpec(
                    partAdmissionCaseData,
                    partAdmissionCaseData.getRespondent1()
                )
            );

            CaseData fullAdmissionCaseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();

            assertEquals(
                RespondentResponseTypeSpec.FULL_ADMISSION,
                PartyUtils.getResponseTypeForRespondentSpec(
                    fullAdmissionCaseData,
                    fullAdmissionCaseData.getRespondent1()
                )
            );

            CaseData counterClaimCaseData = CaseDataBuilder.builder().atStateRespondentCounterClaimSpec().build();

            assertEquals(
                RespondentResponseTypeSpec.COUNTER_CLAIM,
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

            assertEquals(
                RespondentResponseTypeSpec.FULL_ADMISSION,
                PartyUtils.getResponseTypeForRespondentSpec(
                    divergentResponseCaseData,
                    divergentResponseCaseData.getRespondent1()
                )
            );

            assertEquals(
                RespondentResponseTypeSpec.PART_ADMISSION,
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

            assertEquals(
                RespondentResponseTypeSpec.COUNTER_CLAIM,
                PartyUtils.getResponseTypeForRespondentSpec(
                    counterClaimCaseData,
                    counterClaimCaseData.getRespondent1()
                )
            );

            assertEquals(
                RespondentResponseTypeSpec.COUNTER_CLAIM,
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

            PartyData expectedData = new PartyData()
                .setRole(RESPONDENT_ONE)
                .setDetails(caseData.getRespondent1())
                .setTimeExtensionDate(datetime)
                .setSolicitorAgreedDeadlineExtension(date);

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

            PartyData expectedData = new PartyData()
                .setRole(RESPONDENT_TWO)
                .setDetails(caseData.getRespondent2())
                .setTimeExtensionDate(datetime)
                .setSolicitorAgreedDeadlineExtension(date);

            PartyData actualData = PartyUtils.respondent2Data(caseData);

            assertEquals(expectedData, actualData);
        }
    }

    @Nested
    class AppendWithNewPartyId {

        @Test
        void shouldAddPartyId_toGivenParty() {
            Party party = new Party();
            party.setPartyName("mock party");

            Party actual = PartyUtils.appendWithNewPartyId(party);

            assertNotNull(actual.getPartyID());
        }

        @Test
        void shouldNotAppendPartyId_whenPartyIdExists() {
            Party expected = new Party();
            expected.setPartyID(UUID.randomUUID().toString());
            expected.setPartyName("mock party");

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
            LitigationFriend litigationFriend = new LitigationFriend();
            litigationFriend.setFirstName("mock party");

            LitigationFriend actual = PartyUtils.appendWithNewPartyId(litigationFriend);

            assertNotNull(actual.getPartyID());
        }

        @Test
        void shouldNotAppendPartyId_whenLitigationFriendPartyIdExists() {
            LitigationFriend expected = new LitigationFriend();
            expected.setPartyID(UUID.randomUUID().toString());
            expected.setFirstName("litfriend");

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
            PartyFlagStructure partyFlagStructure = new PartyFlagStructure();
            partyFlagStructure.setFirstName("mock party");

            PartyFlagStructure actual = PartyUtils.appendWithNewPartyId(partyFlagStructure);

            assertNotNull(actual.getPartyID());
        }

        @Test
        void shouldNotAppendPartyId_whenPartyFlagStructurePartyIdExists() {
            PartyFlagStructure expected = new PartyFlagStructure();
            expected.setPartyID(UUID.randomUUID().toString());
            expected.setFirstName("structure");

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
            PartyFlagStructure partyFlagStructure = new PartyFlagStructure();
            partyFlagStructure.setFirstName("structure");
            List<Element<PartyFlagStructure>> partyFlagStructures = wrapElements(List.of(partyFlagStructure));

            var actual = PartyUtils.appendWithNewPartyIds(partyFlagStructures);

            assertEquals(partyFlagStructures.size(), actual.size());
            assertNotNull(actual.get(0).getValue().getPartyID());
        }

        @Test
        void shouldNotAppendParty_whenPartyIdExists() {
            PartyFlagStructure partyFlagStructure = new PartyFlagStructure();
            partyFlagStructure.setFirstName("structure");
            partyFlagStructure.setPartyID("some id");
            List<Element<PartyFlagStructure>> expected = wrapElements(List.of(partyFlagStructure));

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
            Party applicant1 = new Party();
            applicant1.setPartyName("mock party 1");
            Party applicant2 = new Party();
            applicant2.setPartyName("mock party 3");
            Party respondent1 = new Party();
            respondent1.setPartyName("mock party 4");
            Party respondent2 = new Party();
            respondent2.setPartyName("mock party 5");
            LitigationFriend applicant1LitigationFriend = new LitigationFriend();
            applicant1LitigationFriend.setFirstName("mock litfriend 1");
            LitigationFriend respondent1LitigationFriend = new LitigationFriend();
            respondent1LitigationFriend.setFirstName("mock litfriend 2");
            LitigationFriend respondent2LitigationFriend = new LitigationFriend();
            respondent2LitigationFriend.setFirstName("mock litfriend 3");

            CaseData caseData = CaseDataBuilder.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .build();
            caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
            caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
            caseData.setRespondent2LitigationFriend(respondent2LitigationFriend);
            PartyUtils.populateWithPartyIds(caseData);

            assertNotNull(caseData.getApplicant1().getPartyID());
            assertNotNull(caseData.getApplicant2().getPartyID());
            assertNotNull(caseData.getRespondent1().getPartyID());
            assertNotNull(caseData.getRespondent2().getPartyID());
            assertNotNull(caseData.getApplicant1LitigationFriend().getPartyID());
            assertNotNull(caseData.getRespondent1LitigationFriend().getPartyID());
            assertNotNull(caseData.getRespondent2LitigationFriend().getPartyID());
        }

        @Test
        void shouldNotPopulateAlreadyNullPartyFields_withinCaseDataBuilder() {
            Party applicant1 = new Party();
            applicant1.setPartyName("mock party 1");
            Party respondent1 = new Party();
            respondent1.setPartyName("mock party 4");
            LitigationFriend applicant1LitigationFriend = new LitigationFriend();
            applicant1LitigationFriend.setFirstName("mock litfriend 1");
            LitigationFriend respondent1LitigationFriend = new LitigationFriend();
            respondent1LitigationFriend.setFirstName("mock litfriend 2");

            CaseData caseData = CaseDataBuilder.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .build();
            caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
            caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
            PartyUtils.populateWithPartyIds(caseData);

            CaseData actual = caseData;

            assertNull(actual.getApplicant2());
            assertNull(actual.getRespondent2());
            assertNull(actual.getRespondent2LitigationFriend());
        }
    }

    @Nested
    class PopulateDQPartyIds {

        @Test
        void shouldDQPopulatePartyIds_withinGivenCaseDataBuilder() {
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQWitnesses(buildWitnesses("app1witness"));
            applicant1DQ.setApplicant1DQExperts(buildExperts("app1expert"));
            applicant1DQ.setApplicant1RespondToClaimExperts(buildExpertDetails("app1expertdetails"));
            Applicant2DQ applicant2DQ = new Applicant2DQ();
            applicant2DQ.setApplicant2DQWitnesses(buildWitnesses("app2witness"));
            applicant2DQ.setApplicant2DQExperts(buildExperts("app2expert"));
            applicant2DQ.setApplicant2RespondToClaimExperts(buildExpertDetails("app2expertdetails"));
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQWitnesses(buildWitnesses("res1witness"));
            respondent1DQ.setRespondent1DQExperts(buildExperts("res1expert"));
            respondent1DQ.setRespondToClaimExperts(buildExpertDetails("res1expertdetails"));
            Respondent2DQ respondent2DQ = new Respondent2DQ();
            respondent2DQ.setRespondent2DQWitnesses(buildWitnesses("res2witness"));
            respondent2DQ.setRespondent2DQExperts(buildExperts("res2expert"));
            respondent2DQ.setRespondToClaimExperts2(buildExpertDetails("res2expertdetails"));

            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(applicant1DQ)
                .applicant2DQ(applicant2DQ)
                .respondent1DQ(respondent1DQ)
                .respondent2DQ(respondent2DQ)
                .build();

            PartyUtils.populateDQPartyIds(caseData);

            var app1Witness = unwrapElements(caseData.getApplicant1DQ().getApplicant1DQWitnesses().getDetails()).get(0);
            assertNotNull(app1Witness.getPartyID());
            assertEquals(app1Witness.getFirstName(), "app1witness");

            var app2Witness = unwrapElements(caseData.getApplicant2DQ().getApplicant2DQWitnesses().getDetails()).get(0);
            assertNotNull(app2Witness.getPartyID());
            assertEquals(app2Witness.getFirstName(), "app2witness");

            var res1Witness = unwrapElements(caseData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails()).get(0);
            assertNotNull(res1Witness.getPartyID());
            assertEquals(res1Witness.getFirstName(), "res1witness");

            var res2Witness = unwrapElements(caseData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails()).get(0);
            assertNotNull(res2Witness.getPartyID());
            assertEquals(res2Witness.getFirstName(), "res2witness");

            var app1Expert = unwrapElements(caseData.getApplicant1DQ().getApplicant1DQExperts().getDetails()).get(0);
            assertNotNull(app1Expert.getPartyID());
            assertEquals(app1Expert.getFirstName(), "app1expert");

            var app1ExpertDetails = caseData.getApplicant1DQ().getApplicant1RespondToClaimExperts();
            assertNotNull(app1ExpertDetails.getPartyID());
            assertEquals(app1ExpertDetails.getFirstName(), "app1expertdetails");

            var app2Expert = unwrapElements(caseData.getApplicant2DQ().getApplicant2DQExperts().getDetails()).get(0);
            assertNotNull(app2Expert.getPartyID());
            assertEquals(app2Expert.getFirstName(), "app2expert");

            var app2ExpertDetails = caseData.getApplicant2DQ().getApplicant2RespondToClaimExperts();
            assertNotNull(app2ExpertDetails.getPartyID());
            assertEquals(app2ExpertDetails.getFirstName(), "app2expertdetails");

            var res1Expert = unwrapElements(caseData.getRespondent1DQ().getRespondent1DQExperts().getDetails()).get(0);
            assertNotNull(res1Expert.getPartyID());
            assertEquals(res1Expert.getFirstName(), "res1expert");

            var res1ExpertDetails = caseData.getRespondent1DQ().getRespondToClaimExperts();
            assertNotNull(res1ExpertDetails.getPartyID());
            assertEquals(res1ExpertDetails.getFirstName(), "res1expertdetails");

            var res2Expert = unwrapElements(caseData.getRespondent2DQ().getRespondent2DQExperts().getDetails()).get(0);
            assertNotNull(res2Expert.getPartyID());
            assertEquals(res2Expert.getFirstName(), "res2expert");

            var res2ExpertDetails = caseData.getRespondent2DQ().getRespondToClaimExperts2();
            assertNotNull(res2ExpertDetails.getPartyID());
            assertEquals(res2ExpertDetails.getFirstName(), "res2expertdetails");
        }

        @Test
        void shouldNotOverWritePartyIds_whenPartyIdsExist() {
            UUID app1WitnessElementId = UUID.randomUUID();
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQWitnesses(buildWitnesses("app1witness", "app1witnesspartyid", app1WitnessElementId));

            UUID app1ExpertElementId = UUID.randomUUID();
            applicant1DQ.setApplicant1DQExperts(buildExperts("app1expert", "app1expertpartyid", app1ExpertElementId));
            applicant1DQ.setApplicant1RespondToClaimExperts(buildExpertDetails("app1expertdetails", "app1expertdetailspartyid"));
            Applicant2DQ applicant2DQ = new Applicant2DQ();

            UUID app2WitnessElementId = UUID.randomUUID();
            applicant2DQ.setApplicant2DQWitnesses(buildWitnesses("app2witness", "app2witnesspartyid", app2WitnessElementId));

            UUID app2ExpertElementId = UUID.randomUUID();
            applicant2DQ.setApplicant2DQExperts(buildExperts("app2expert", "app2expertpartyid", app2ExpertElementId));
            applicant2DQ.setApplicant2RespondToClaimExperts(buildExpertDetails("app2expertdetails", "app2expertdetailspartyid"));
            Respondent1DQ respondent1DQ = new Respondent1DQ();

            UUID res1WitnessElementId = UUID.randomUUID();
            respondent1DQ.setRespondent1DQWitnesses(buildWitnesses("res1witness", "res1witnesspartyid", res1WitnessElementId));

            UUID res1ExpertElementId = UUID.randomUUID();
            respondent1DQ.setRespondent1DQExperts(buildExperts("res1expert", "res1expertpartyid", res1ExpertElementId));
            respondent1DQ.setRespondToClaimExperts(buildExpertDetails("res1expertdetails", "res1expertdetailspartyid"));
            Respondent2DQ respondent2DQ = new Respondent2DQ();

            UUID res2WitnessElementId = UUID.randomUUID();
            respondent2DQ.setRespondent2DQWitnesses(buildWitnesses("res2witness", "res2witnesspartyid", res2WitnessElementId));

            UUID res2ExpertElementId = UUID.randomUUID();
            respondent2DQ.setRespondent2DQExperts(buildExperts("res2expert", "res2expertpartyid", res2ExpertElementId));
            respondent2DQ.setRespondToClaimExperts2(buildExpertDetails("res2expertdetails", "res2expertdetailspartyid"));

            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(applicant1DQ)
                .applicant2DQ(applicant2DQ)
                .respondent1DQ(respondent1DQ)
                .respondent2DQ(respondent2DQ)
                .build();

            PartyUtils.populateDQPartyIds(caseData);

            var app1Witness = caseData.getApplicant1DQ().getApplicant1DQWitnesses().getDetails().get(0);
            assertEquals(app1Witness.getId(), app1WitnessElementId);
            assertEquals(app1Witness.getValue().getPartyID(), "app1witnesspartyid");

            var app2Witness = caseData.getApplicant2DQ().getApplicant2DQWitnesses().getDetails().get(0);
            assertEquals(app2Witness.getId(), app2WitnessElementId);
            assertEquals(app2Witness.getValue().getPartyID(), "app2witnesspartyid");

            var res1Witness = caseData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails().get(0);
            assertEquals(res1Witness.getId(), res1WitnessElementId);
            assertEquals(res1Witness.getValue().getPartyID(), "res1witnesspartyid");

            var res2Witness = caseData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails().get(0);
            assertEquals(res2Witness.getId(), res2WitnessElementId);
            assertEquals(res2Witness.getValue().getPartyID(), "res2witnesspartyid");

            var app1Expert = caseData.getApplicant1DQ().getApplicant1DQExperts().getDetails().get(0);
            assertEquals(app1Expert.getId(), app1ExpertElementId);
            assertEquals(app1Expert.getValue().getPartyID(), "app1expertpartyid");

            var app1ExpertDetails = caseData.getApplicant1DQ().getApplicant1RespondToClaimExperts();
            assertEquals(app1ExpertDetails.getPartyID(), "app1expertdetailspartyid");

            var app2Expert = caseData.getApplicant2DQ().getApplicant2DQExperts().getDetails().get(0);
            assertEquals(app2Expert.getId(), app2ExpertElementId);
            assertEquals(app2Expert.getValue().getPartyID(), "app2expertpartyid");

            var app2ExpertDetails = caseData.getApplicant2DQ().getApplicant2RespondToClaimExperts();
            assertEquals(app2ExpertDetails.getPartyID(), "app2expertdetailspartyid");

            var res1Expert = caseData.getRespondent1DQ().getRespondent1DQExperts().getDetails().get(0);
            assertEquals(res1Expert.getId(), res1ExpertElementId);
            assertEquals(res1Expert.getValue().getPartyID(), "res1expertpartyid");

            var res1ExpertDetails = caseData.getRespondent1DQ().getRespondToClaimExperts();
            assertEquals(res1ExpertDetails.getPartyID(), "res1expertdetailspartyid");

            var res2Expert = caseData.getRespondent2DQ().getRespondent2DQExperts().getDetails().get(0);
            assertEquals(res2Expert.getId(), res2ExpertElementId);
            assertEquals(res2Expert.getValue().getPartyID(), "res2expertpartyid");

            var res2ExpertDetails = caseData.getRespondent2DQ().getRespondToClaimExperts2();
            assertEquals(res2ExpertDetails.getPartyID(), "res2expertdetailspartyid");
        }

        @Test
        void shouldReturnNullWitnessExpertFields_whenCaseDataBuilderHasNullWitnessExpertFields() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(new Applicant1DQ())
                .applicant2DQ(new Applicant2DQ())
                .respondent1DQ(new Respondent1DQ())
                .respondent2DQ(new Respondent2DQ())
                .build();

            PartyUtils.populateDQPartyIds(caseData);

            assertNull(caseData.getApplicant1DQ().getApplicant1DQWitnesses());
            assertNull(caseData.getApplicant2DQ().getApplicant2DQWitnesses());
            assertNull(caseData.getRespondent1DQ().getRespondent1DQWitnesses());
            assertNull(caseData.getRespondent2DQ().getRespondent2DQWitnesses());
            assertNull(caseData.getApplicant1DQ().getApplicant1DQExperts());
            assertNull(caseData.getApplicant1DQ().getApplicant1RespondToClaimExperts());
            assertNull(caseData.getApplicant2DQ().getApplicant2DQExperts());
            assertNull(caseData.getApplicant2DQ().getApplicant2RespondToClaimExperts());
            assertNull(caseData.getRespondent1DQ().getRespondent1DQExperts());
            assertNull(caseData.getRespondent1DQ().getRespondToClaimExperts());
            assertNull(caseData.getRespondent2DQ().getRespondent2DQExperts());
            assertNull(caseData.getRespondent2DQ().getRespondToClaimExperts2());
        }

        @Test
        void shouldReturnNullDQFields_whenCaseDataBuilderHasNullDQFields() {
            CaseData caseData = CaseDataBuilder.builder().build();

            PartyUtils.populateDQPartyIds(caseData);

            assertNull(caseData.getApplicant1DQ());
            assertNull(caseData.getApplicant2DQ());
            assertNull(caseData.getRespondent1DQ());
            assertNull(caseData.getRespondent2DQ());
        }

        private Witnesses buildWitnesses(String firstName, String partyId, UUID elementId) {
            Witness witness = new Witness();
            witness.setPartyID(partyId);
            witness.setFirstName(firstName);
            Element<Witness> witnessElement = Element.<Witness>builder().id(elementId).value(witness).build();
            Witnesses witnesses = new Witnesses();
            witnesses.setDetails(List.of(witnessElement));
            return witnesses;
        }

        private Witnesses buildWitnesses(String firstName) {
            return buildWitnesses(firstName, null, UUID.randomUUID());
        }

        private Experts buildExperts(String firstName, String partyId, UUID elementId) {
            Expert expert = new Expert();
            expert.setPartyID(partyId);
            expert.setFirstName(firstName);
            Element<Expert> expertElement = Element.<Expert>builder().id(elementId).value(expert).build();
            Experts experts = new Experts();
            experts.setDetails(List.of(expertElement));
            return experts;
        }

        private Experts buildExperts(String firstName) {
            return buildExperts(firstName, null, null);
        }

        private ExpertDetails buildExpertDetails(String firstName, String partyId) {
            ExpertDetails expertDetails = new ExpertDetails();
            expertDetails.setPartyID(partyId);
            expertDetails.setFirstName(firstName);
            return expertDetails;
        }

        private ExpertDetails buildExpertDetails(String firstName) {
            return buildExpertDetails(firstName, null);
        }
    }

    @Nested
    class PopulatePartyIndividualPartyIds {

        @Test
        void shouldopulateIndividualsPartyIds_withinGivenCaseDataBuilder() {
            PartyFlagStructure app1LR = new PartyFlagStructure();
            app1LR.setFirstName("app1");
            app1LR.setLastName("lrindividual");
            PartyFlagStructure res1LR = new PartyFlagStructure();
            res1LR.setFirstName("res1");
            res1LR.setLastName("lrindividual");
            PartyFlagStructure res2LR = new PartyFlagStructure();
            res2LR.setFirstName("res2");
            res2LR.setLastName("lrindividual");
            PartyFlagStructure app1Org = new PartyFlagStructure();
            app1Org.setFirstName("app1");
            app1Org.setLastName("orgindividual");
            PartyFlagStructure app2Org = new PartyFlagStructure();
            app2Org.setFirstName("app2");
            app2Org.setLastName("orgindividual");
            PartyFlagStructure res1Org = new PartyFlagStructure();
            res1Org.setFirstName("res1");
            res1Org.setLastName("orgindividual");
            PartyFlagStructure res2Org = new PartyFlagStructure();
            res2Org.setFirstName("res2");
            res2Org.setLastName("orgindividual");

            CaseData actual = CaseDataBuilder.builder().build();
            actual.setApplicant1LRIndividuals(wrapElements(List.of(app1LR)));
            actual.setRespondent1LRIndividuals(wrapElements(List.of(res1LR)));
            actual.setRespondent2LRIndividuals(wrapElements(List.of(res2LR)));
            actual.setApplicant1OrgIndividuals(wrapElements(List.of(app1Org)));
            actual.setApplicant2OrgIndividuals(wrapElements(List.of(app2Org)));
            actual.setRespondent1OrgIndividuals(wrapElements(List.of(res1Org)));
            actual.setRespondent2OrgIndividuals(wrapElements(List.of(res2Org)));
            PartyUtils.populatePartyIndividuals(actual);

            var app1LRIndividual = unwrapElements(actual.getApplicant1LRIndividuals()).get(0);
            assertNotNull(app1LRIndividual.getPartyID());
            assertEquals(app1LRIndividual.getFirstName(), "app1");
            assertEquals(app1LRIndividual.getLastName(), "lrindividual");

            var res1LRIndividual = unwrapElements(actual.getRespondent1LRIndividuals()).get(0);
            assertNotNull(res1LRIndividual.getPartyID());
            assertEquals(res1LRIndividual.getFirstName(), "res1");
            assertEquals(res1LRIndividual.getLastName(), "lrindividual");

            var res2LRIndividual = unwrapElements(actual.getRespondent2LRIndividuals()).get(0);
            assertNotNull(res2LRIndividual.getPartyID());
            assertEquals(res2LRIndividual.getFirstName(), "res2");
            assertEquals(res2LRIndividual.getLastName(), "lrindividual");

            var app1OrgIndividual = unwrapElements(actual.getApplicant1OrgIndividuals()).get(0);
            assertNotNull(app1OrgIndividual.getPartyID());
            assertEquals(app1OrgIndividual.getFirstName(), "app1");
            assertEquals(app1OrgIndividual.getLastName(), "orgindividual");

            var app2OrgIndividual = unwrapElements(actual.getApplicant2OrgIndividuals()).get(0);
            assertNotNull(app2OrgIndividual.getPartyID());
            assertEquals(app2OrgIndividual.getFirstName(), "app2");
            assertEquals(app2OrgIndividual.getLastName(), "orgindividual");

            var res1OrgIndividual = unwrapElements(actual.getRespondent1OrgIndividuals()).get(0);
            assertNotNull(res1OrgIndividual.getPartyID());
            assertEquals(res1OrgIndividual.getFirstName(), "res1");
            assertEquals(res1OrgIndividual.getLastName(), "orgindividual");

            var res2OrgIndividual = unwrapElements(actual.getRespondent2OrgIndividuals()).get(0);
            assertNotNull(res2OrgIndividual.getPartyID());
            assertEquals(res2OrgIndividual.getFirstName(), "res2");
            assertEquals(res2OrgIndividual.getLastName(), "orgindividual");
        }

        @Test
        void shouldopulateIndividualsPartyIds_mixedNullLRAndOrgFields() {
            PartyFlagStructure app1LR = new PartyFlagStructure();
            app1LR.setFirstName("app1");
            app1LR.setLastName("lrindividual");
            PartyFlagStructure res1Org = new PartyFlagStructure();
            res1Org.setFirstName("res1");
            res1Org.setLastName("orgindividual");

            CaseData actual = CaseDataBuilder.builder().build();
            actual.setApplicant1LRIndividuals(wrapElements(List.of(app1LR)));
            actual.setRespondent1OrgIndividuals(wrapElements(List.of(res1Org)));
            PartyUtils.populatePartyIndividuals(actual);

            var app1LRIndividual = unwrapElements(actual.getApplicant1LRIndividuals()).get(0);
            assertNotNull(app1LRIndividual.getPartyID());
            assertEquals(app1LRIndividual.getFirstName(), "app1");
            assertEquals(app1LRIndividual.getLastName(), "lrindividual");

            var res1OrgIndividual = unwrapElements(actual.getRespondent1OrgIndividuals()).get(0);
            assertNotNull(res1OrgIndividual.getPartyID());
            assertEquals(res1OrgIndividual.getFirstName(), "res1");
            assertEquals(res1OrgIndividual.getLastName(), "orgindividual");

            assertNull(actual.getRespondent1LRIndividuals());
            assertNull(actual.getRespondent2LRIndividuals());
            assertNull(actual.getApplicant1OrgIndividuals());
            assertNull(actual.getApplicant2OrgIndividuals());
            assertNull(actual.getRespondent2OrgIndividuals());
        }

        @Test
        void shouldNotOverwriteExistingPartyIds_withinGivenCaseDataBuilder() {
            PartyFlagStructure app1LR = new PartyFlagStructure();
            app1LR.setPartyID("app1-lr-ind-id");
            app1LR.setFirstName("app1");
            app1LR.setLastName("lrindividual");
            PartyFlagStructure res1LR = new PartyFlagStructure();
            res1LR.setPartyID("res1-lr-ind-id");
            res1LR.setFirstName("res1");
            res1LR.setLastName("lrindividual");
            PartyFlagStructure res2LR = new PartyFlagStructure();
            res2LR.setPartyID("res2-lr-ind-id");
            res2LR.setFirstName("res2");
            res2LR.setLastName("lrindividual");
            PartyFlagStructure app1Org = new PartyFlagStructure();
            app1Org.setPartyID("app1-org-ind-id");
            app1Org.setFirstName("app1");
            app1Org.setLastName("orgindividual");
            PartyFlagStructure app2Org = new PartyFlagStructure();
            app2Org.setPartyID("app2-org-ind-id");
            app2Org.setFirstName("app2");
            app2Org.setLastName("orgindividual");
            PartyFlagStructure res1Org = new PartyFlagStructure();
            res1Org.setPartyID("res1-org-ind-id");
            res1Org.setFirstName("res1");
            res1Org.setLastName("orgindividual");
            PartyFlagStructure res2Org = new PartyFlagStructure();
            res2Org.setPartyID("res2-org-ind-id");
            res2Org.setFirstName("res2");
            res2Org.setLastName("orgindividual");

            CaseData actual = CaseDataBuilder.builder().build();
            actual.setApplicant1LRIndividuals(wrapElements(List.of(app1LR)));
            actual.setRespondent1LRIndividuals(wrapElements(List.of(res1LR)));
            actual.setRespondent2LRIndividuals(wrapElements(List.of(res2LR)));
            actual.setApplicant1OrgIndividuals(wrapElements(List.of(app1Org)));
            actual.setApplicant2OrgIndividuals(wrapElements(List.of(app2Org)));
            actual.setRespondent1OrgIndividuals(wrapElements(List.of(res1Org)));
            actual.setRespondent2OrgIndividuals(wrapElements(List.of(res2Org)));
            PartyUtils.populatePartyIndividuals(actual);

            var app1LRIndividual = unwrapElements(actual.getApplicant1LRIndividuals()).get(0);
            assertEquals(app1LRIndividual.getPartyID(), "app1-lr-ind-id");
            assertEquals(app1LRIndividual.getFirstName(), "app1");
            assertEquals(app1LRIndividual.getLastName(), "lrindividual");

            var res1LRIndividual = unwrapElements(actual.getRespondent1LRIndividuals()).get(0);
            assertEquals(res1LRIndividual.getPartyID(), "res1-lr-ind-id");
            assertEquals(res1LRIndividual.getFirstName(), "res1");
            assertEquals(res1LRIndividual.getLastName(), "lrindividual");

            var res2LRIndividual = unwrapElements(actual.getRespondent2LRIndividuals()).get(0);
            assertEquals(res2LRIndividual.getPartyID(), "res2-lr-ind-id");
            assertEquals(res2LRIndividual.getFirstName(), "res2");
            assertEquals(res2LRIndividual.getLastName(), "lrindividual");

            var app1OrgIndividual = unwrapElements(actual.getApplicant1OrgIndividuals()).get(0);
            assertEquals(app1OrgIndividual.getPartyID(), "app1-org-ind-id");
            assertEquals(app1OrgIndividual.getFirstName(), "app1");
            assertEquals(app1OrgIndividual.getLastName(), "orgindividual");

            var app2OrgIndividual = unwrapElements(actual.getApplicant2OrgIndividuals()).get(0);
            assertEquals(app2OrgIndividual.getPartyID(), "app2-org-ind-id");
            assertEquals(app2OrgIndividual.getFirstName(), "app2");
            assertEquals(app2OrgIndividual.getLastName(), "orgindividual");

            var res1OrgIndividual = unwrapElements(actual.getRespondent1OrgIndividuals()).get(0);
            assertEquals(res1OrgIndividual.getPartyID(), "res1-org-ind-id");
            assertEquals(res1OrgIndividual.getFirstName(), "res1");
            assertEquals(res1OrgIndividual.getLastName(), "orgindividual");

            var res2OrgIndividual = unwrapElements(actual.getRespondent2OrgIndividuals()).get(0);
            assertEquals(res2OrgIndividual.getPartyID(), "res2-org-ind-id");
            assertEquals(res2OrgIndividual.getFirstName(), "res2");
            assertEquals(res2OrgIndividual.getLastName(), "orgindividual");
        }
    }

    @Nested
    class PopulateWitnessAndExpertsPartyIds {

        @Test
        void shouldPopulateWitnessAndExpertsPartyIds_withinGivenCaseDataBuilder() {
            PartyFlagStructure app1Expert = new PartyFlagStructure();
            app1Expert.setPartyID("app1-expert-id");
            app1Expert.setFirstName("app1");
            app1Expert.setLastName("expert");
            PartyFlagStructure res1Expert = new PartyFlagStructure();
            res1Expert.setPartyID("res1-expert-id");
            res1Expert.setFirstName("res1");
            res1Expert.setLastName("expert");
            PartyFlagStructure res2Expert = new PartyFlagStructure();
            res2Expert.setPartyID("res2-expert-id");
            res2Expert.setFirstName("res2");
            res2Expert.setLastName("expert");
            PartyFlagStructure app1Witness = new PartyFlagStructure();
            app1Witness.setPartyID("app1-witness-id");
            app1Witness.setFirstName("app1");
            app1Witness.setLastName("witness");
            PartyFlagStructure res1Witness = new PartyFlagStructure();
            res1Witness.setPartyID("res1-witness-id");
            res1Witness.setFirstName("res1");
            res1Witness.setLastName("witness");
            PartyFlagStructure res2Witness = new PartyFlagStructure();
            res2Witness.setPartyID("res2-witness-id");
            res2Witness.setFirstName("res2");
            res2Witness.setLastName("witness");

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicantExperts(wrapElements(List.of(app1Expert)));
            caseData.setRespondent1Experts(wrapElements(List.of(res1Expert)));
            caseData.setRespondent2Experts(wrapElements(List.of(res2Expert)));
            caseData.setApplicantWitnesses(wrapElements(List.of(app1Witness)));
            caseData.setRespondent1Witnesses(wrapElements(List.of(res1Witness)));
            caseData.setRespondent2Witnesses(wrapElements(List.of(res2Witness)));
            PartyUtils.populateWitnessAndExpertsPartyIds(caseData);

            CaseData actual = caseData;

            assertNotNull(actual.getApplicantExperts().get(0).getValue().getPartyID());
            assertEquals("app1-expert-id", actual.getApplicantExperts().get(0).getValue().getPartyID());

            assertNotNull(actual.getRespondent1Experts().get(0).getValue().getPartyID());
            assertEquals("res1-expert-id", actual.getRespondent1Experts().get(0).getValue().getPartyID());

            assertNotNull(actual.getRespondent2Experts().get(0).getValue().getPartyID());
            assertEquals("res2-expert-id", actual.getRespondent2Experts().get(0).getValue().getPartyID());

            assertNotNull(actual.getApplicantWitnesses().get(0).getValue().getPartyID());
            assertEquals("app1-witness-id", actual.getApplicantWitnesses().get(0).getValue().getPartyID());

            assertNotNull(actual.getRespondent1Witnesses().get(0).getValue().getPartyID());
            assertEquals("res1-witness-id", actual.getRespondent1Witnesses().get(0).getValue().getPartyID());

            assertNotNull(actual.getRespondent2Witnesses().get(0).getValue().getPartyID());
            assertEquals("res2-witness-id", actual.getRespondent2Witnesses().get(0).getValue().getPartyID());
        }

        @Test
        void shouldNotOverWriteExistingWitnessAndExpertsPartyIds() {
            PartyFlagStructure app1Expert = new PartyFlagStructure();
            app1Expert.setPartyID("existingAppExpertPartyId");
            app1Expert.setFirstName("app1");
            app1Expert.setLastName("expert");
            PartyFlagStructure res1Expert = new PartyFlagStructure();
            res1Expert.setPartyID("existingRes1ExpertPartyId");
            res1Expert.setFirstName("res1");
            res1Expert.setLastName("expert");
            PartyFlagStructure res2Expert = new PartyFlagStructure();
            res2Expert.setPartyID("existingRes2ExpertPartyId");
            res2Expert.setFirstName("res2");
            res2Expert.setLastName("expert");
            PartyFlagStructure app1Witness = new PartyFlagStructure();
            app1Witness.setPartyID("existingAppWitnessPartyId");
            app1Witness.setFirstName("app1");
            app1Witness.setLastName("witness");
            PartyFlagStructure res1Witness = new PartyFlagStructure();
            res1Witness.setPartyID("existingRes1WitnessPartyId");
            res1Witness.setFirstName("res1");
            res1Witness.setLastName("witness");
            PartyFlagStructure res2Witness = new PartyFlagStructure();
            res2Witness.setPartyID("existingRes2WitnessPartyId");
            res2Witness.setFirstName("res2");
            res2Witness.setLastName("witness");

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicantExperts(wrapElements(List.of(app1Expert)));
            caseData.setRespondent1Experts(wrapElements(List.of(res1Expert)));
            caseData.setRespondent2Experts(wrapElements(List.of(res2Expert)));
            caseData.setApplicantWitnesses(wrapElements(List.of(app1Witness)));
            caseData.setRespondent1Witnesses(wrapElements(List.of(res1Witness)));
            caseData.setRespondent2Witnesses(wrapElements(List.of(res2Witness)));
            PartyUtils.populateWitnessAndExpertsPartyIds(caseData);

            CaseData actual = caseData;

            assertEquals("existingAppExpertPartyId", actual.getApplicantExperts().get(0).getValue().getPartyID());
            assertEquals("existingRes1ExpertPartyId", actual.getRespondent1Experts().get(0).getValue().getPartyID());
            assertEquals("existingRes2ExpertPartyId", actual.getRespondent2Experts().get(0).getValue().getPartyID());
            assertEquals("existingAppWitnessPartyId", actual.getApplicantWitnesses().get(0).getValue().getPartyID());
            assertEquals("existingRes1WitnessPartyId", actual.getRespondent1Witnesses().get(0).getValue().getPartyID());
            assertEquals("existingRes2WitnessPartyId", actual.getRespondent2Witnesses().get(0).getValue().getPartyID());
        }

        @Test
        void shouldReturnNull_whenCaseDataBuilderHasNullWitnessAndExperts() {
            CaseData caseData = CaseDataBuilder.builder().build();

            PartyUtils.populateWitnessAndExpertsPartyIds(caseData);
            CaseData actual = caseData;

            assertNull(actual.getApplicantExperts());
            assertNull(actual.getRespondent1Experts());
            assertNull(actual.getRespondent2Experts());
            assertNull(actual.getApplicantWitnesses());
            assertNull(actual.getRespondent1Witnesses());
            assertNull(actual.getRespondent2Witnesses());
        }

    }
}
