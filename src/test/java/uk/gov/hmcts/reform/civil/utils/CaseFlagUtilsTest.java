package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.APPLICANT_SOLICITOR_EXPERT;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.APPLICANT_SOLICITOR_WITNESS;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_SOLICITOR_ONE_EXPERT;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_SOLICITOR_ONE_WITNESS;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_SOLICITOR_TWO_EXPERT;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_SOLICITOR_TWO_WITNESS;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.addApplicantExpertAndWitnessFlagsStructure;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.addRespondentDQPartiesFlagStructure;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.filter;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.getAllCaseFlags;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ORG_INDIVIDUALS_ID;

class CaseFlagUtilsTest {

    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        organisationService = mock(OrganisationService.class);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("Civil - Organisation").build()));
    }

    @Nested
    class CreateFlags {

        @Test
        void shouldCreateFlags() {
            Flags expected = Flags.builder().partyName("partyName").roleOnCase("roleOnCase").details(List.of()).build();
            Flags actual = CaseFlagUtils.createFlags("partyName", "roleOnCase");
            assertEquals(expected, actual);
        }
    }

    @Nested
    class UpdateParty {

        @Test
        void shouldUpdatePartyWithFlagsMeta() {
            Party party = PartyBuilder.builder().individual().build();
            Flags flags = Flags.builder().partyName("Mr. John Rambo").roleOnCase("applicant").details(List.of()).build();
            Party expected = party.toBuilder().flags(flags).build();

            Party actual = CaseFlagUtils.updateParty("applicant", party);

            assertEquals(expected, actual);
        }

        @Test
        void shouldNotUpdatePartyFlagsIfFlagsExist() {
            Party existingParty = PartyBuilder.builder().individual().build()
                .toBuilder()
                .flags(Flags.builder().partyName("Mr. John Rambo").roleOnCase("applicant").details(List.of()).build())
                .build();

            Party actual = CaseFlagUtils.updateParty("updatedField", existingParty);

            assertEquals(existingParty, actual);
        }

        @Test
        void shouldReturnNullWhenPartyIsNull() {
            Party actual = CaseFlagUtils.updateParty("applicant", null);
            assertNull(actual);
        }
    }

    @Nested
    class UpdateLitFriend {

        @Test
        void shouldUpdateLitigationFriendWithFlagsMeta() {
            LitigationFriend litFriend = LitigationFriend.builder().firstName("John").lastName("Rambo").build();
            Flags flags = Flags.builder().partyName("John Rambo").roleOnCase("applicant").details(List.of()).build();
            LitigationFriend expected = litFriend.toBuilder().flags(flags).build();

            LitigationFriend actual = CaseFlagUtils.updateLitFriend("applicant", litFriend);

            assertEquals(expected, actual);
        }

        @Test
        void shouldNotUpdateLitigationFriendFlagsIfFlagsExist() {
            LitigationFriend existingLitFriend = LitigationFriend.builder().firstName("John").lastName("Rambo").build()
                .toBuilder()
                .flags(Flags.builder().partyName("John Rambo").roleOnCase("applicant").details(List.of()).build())
                .build();

            LitigationFriend actual = CaseFlagUtils.updateLitFriend("updatedField", existingLitFriend);

            assertEquals(existingLitFriend, actual);
        }

        @Test
        void shouldReturnNullWhenLitigationFriendIsNull() {
            LitigationFriend actual = CaseFlagUtils.updateLitFriend("applicant", null);
            assertNull(actual);
        }
    }

    @Nested
    class UpdateDQParties {
        @Test
        public void shouldCreateFlagsStructureForRespondentExperts() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            Expert expert1 = Expert.builder().partyID("partyId1").firstName("First").lastName("Name").build();
            Expert expert2 = Expert.builder().partyID("partyId2").firstName("Second").lastName("expert").build();
            Expert expert3 = Expert.builder().partyID("partyId3").firstName("Third").lastName("experto").build();

            CaseData updatedCaseData = caseData.toBuilder()
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQExperts(Experts
                                                             .builder()
                                                             .details(wrapElements(expert1, expert2))
                                                             .build())
                                   .build())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQExperts(Experts.builder()
                                                             .details(wrapElements(expert3))
                                                             .build())
                                   .build())
                .build();

            CaseData.CaseDataBuilder<?, ?> caseDataBuilderToUpdateWithFlags = updatedCaseData.toBuilder();

            addRespondentDQPartiesFlagStructure(
                caseDataBuilderToUpdateWithFlags,
                updatedCaseData
            );

            CaseData caseDataWithFlags = caseDataBuilderToUpdateWithFlags.build();
            List<Element<PartyFlagStructure>> respondent1ExpertsWithFlags = caseDataWithFlags.getRespondent1Experts();
            List<Element<PartyFlagStructure>> respondent2ExpertsWithFlags = caseDataWithFlags.getRespondent2Experts();

            Flags expectedExpert1Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                .partyName("First Name")
                .details(List.of()).build();

            Flags expectedExpert2Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                .partyName("Second expert")
                .details(List.of()).build();

            Flags expectedExpert3Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)
                .partyName("Third experto")
                .details(List.of()).build();

            assertThat(respondent1ExpertsWithFlags).isNotNull();
            assertThat(respondent1ExpertsWithFlags).hasSize(2);

            assertThat(respondent1ExpertsWithFlags.get(0).getValue().getFlags()).isEqualTo(expectedExpert1Flags);
            assertThat(respondent1ExpertsWithFlags.get(0).getValue().getPartyID()).isEqualTo("partyId1");
            assertThat(respondent1ExpertsWithFlags.get(0).getValue().getFirstName()).isEqualTo("First");
            assertThat(respondent1ExpertsWithFlags.get(0).getValue().getLastName()).isEqualTo("Name");

            assertThat(respondent1ExpertsWithFlags.get(1).getValue().getFlags()).isEqualTo(expectedExpert2Flags);
            assertThat(respondent1ExpertsWithFlags.get(1).getValue().getPartyID()).isEqualTo("partyId2");
            assertThat(respondent1ExpertsWithFlags.get(1).getValue().getFirstName()).isEqualTo("Second");
            assertThat(respondent1ExpertsWithFlags.get(1).getValue().getLastName()).isEqualTo("expert");

            assertThat(respondent2ExpertsWithFlags).isNotNull();
            assertThat(respondent2ExpertsWithFlags).hasSize(1);

            assertThat(respondent2ExpertsWithFlags.get(0).getValue().getFlags()).isEqualTo(expectedExpert3Flags);
            assertThat(respondent2ExpertsWithFlags.get(0).getValue().getPartyID()).isEqualTo("partyId3");
            assertThat(respondent2ExpertsWithFlags.get(0).getValue().getFirstName()).isEqualTo("Third");
            assertThat(respondent2ExpertsWithFlags.get(0).getValue().getLastName()).isEqualTo("experto");
        }

        @Test
        public void shouldCreateFlagsStructureForRespondentWitness() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            Witness witness1 = Witness.builder().partyID("partyId1").firstName("First").lastName("Name").build();
            Witness witness2 = Witness.builder().partyID("partyId2").firstName("Second").lastName("witness").build();
            Witness witness3 = Witness.builder().partyID("partyId3").firstName("Third").lastName("witnessy").build();

            CaseData updatedCaseData = caseData.toBuilder()
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQWitnesses(Witnesses
                                                               .builder()
                                                               .details(wrapElements(witness1, witness2))
                                                               .build())
                                   .build())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQWitnesses(Witnesses
                                                               .builder()
                                                               .details(wrapElements(witness3))
                                                               .build())
                                   .build())
                .build();

            CaseData.CaseDataBuilder<?, ?> caseDataBuilderToUpdateWithFlags = updatedCaseData.toBuilder();

            addRespondentDQPartiesFlagStructure(
                caseDataBuilderToUpdateWithFlags,
                updatedCaseData
            );

            CaseData caseDataWithFlags = caseDataBuilderToUpdateWithFlags.build();
            List<Element<PartyFlagStructure>> respondent1WitnessWithFlags = caseDataWithFlags.getRespondent1Witnesses();
            List<Element<PartyFlagStructure>> respondent2WitnessWithFlags = caseDataWithFlags.getRespondent2Witnesses();

            Flags expectedWitness1Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                .partyName("First Name")
                .details(List.of()).build();

            Flags expectedWitness2Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                .partyName("Second witness")
                .details(List.of()).build();

            Flags expectedWitness3Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS)
                .partyName("Third witnessy")
                .details(List.of()).build();

            assertThat(respondent1WitnessWithFlags).isNotNull();
            assertThat(respondent1WitnessWithFlags).hasSize(2);

            assertThat(respondent1WitnessWithFlags.get(0).getValue().getFlags()).isEqualTo(expectedWitness1Flags);
            assertThat(respondent1WitnessWithFlags.get(0).getValue().getPartyID()).isEqualTo("partyId1");
            assertThat(respondent1WitnessWithFlags.get(0).getValue().getFirstName()).isEqualTo("First");
            assertThat(respondent1WitnessWithFlags.get(0).getValue().getLastName()).isEqualTo("Name");

            assertThat(respondent1WitnessWithFlags.get(1).getValue().getFlags()).isEqualTo(expectedWitness2Flags);
            assertThat(respondent1WitnessWithFlags.get(1).getValue().getPartyID()).isEqualTo("partyId2");
            assertThat(respondent1WitnessWithFlags.get(1).getValue().getFirstName()).isEqualTo("Second");
            assertThat(respondent1WitnessWithFlags.get(1).getValue().getLastName()).isEqualTo("witness");

            assertThat(respondent2WitnessWithFlags).isNotNull();
            assertThat(respondent2WitnessWithFlags).hasSize(1);

            assertThat(respondent2WitnessWithFlags.get(0).getValue().getFlags()).isEqualTo(expectedWitness3Flags);
            assertThat(respondent2WitnessWithFlags.get(0).getValue().getPartyID()).isEqualTo("partyId3");
            assertThat(respondent2WitnessWithFlags.get(0).getValue().getFirstName()).isEqualTo("Third");
            assertThat(respondent2WitnessWithFlags.get(0).getValue().getLastName()).isEqualTo("witnessy");
        }

        @Test
        public void shouldCreateFlagsStructureForApplicantWitness() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                .multiPartyClaimTwoApplicants()
                .build();

            Witness witness1 = Witness.builder().partyID("partyId1").firstName("First").lastName("Name").build();
            Witness witness2 = Witness.builder().partyID("partyId2").firstName("Second").lastName("witness").build();
            Witness witness3 = Witness.builder().partyID("partyId3").firstName("Third").lastName("witnessy").build();

            CaseData updatedCaseData = caseData.toBuilder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQWitnesses(Witnesses
                                                             .builder()
                                                             .details(wrapElements(witness1, witness2))
                                                             .build())
                                  .build())
                .applicant2DQ(Applicant2DQ.builder()
                                  .applicant2DQWitnesses(Witnesses
                                                             .builder()
                                                             .details(wrapElements(witness3))
                                                             .build())
                                  .build())
                .build();

            CaseData.CaseDataBuilder<?, ?> caseDataBuilderToUpdateWithFlags = updatedCaseData.toBuilder();

            addApplicantExpertAndWitnessFlagsStructure(
                caseDataBuilderToUpdateWithFlags,
                updatedCaseData
            );

            CaseData caseDataWithFlags = caseDataBuilderToUpdateWithFlags.build();
            List<Element<PartyFlagStructure>> applicantWitnesses = caseDataWithFlags.getApplicantWitnesses();

            Flags expectedWitness1Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                .partyName("First Name")
                .details(List.of()).build();

            Flags expectedWitness2Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                .partyName("Second witness")
                .details(List.of()).build();

            Flags expectedWitness3Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                .partyName("Third witnessy")
                .details(List.of()).build();

            assertThat(applicantWitnesses).isNotNull();
            assertThat(applicantWitnesses).hasSize(3);

            assertThat(applicantWitnesses.get(0).getValue().getFlags()).isEqualTo(expectedWitness1Flags);
            assertThat(applicantWitnesses.get(0).getValue().getPartyID()).isEqualTo("partyId1");
            assertThat(applicantWitnesses.get(0).getValue().getFirstName()).isEqualTo("First");
            assertThat(applicantWitnesses.get(0).getValue().getLastName()).isEqualTo("Name");

            assertThat(applicantWitnesses.get(1).getValue().getFlags()).isEqualTo(expectedWitness2Flags);
            assertThat(applicantWitnesses.get(1).getValue().getPartyID()).isEqualTo("partyId2");
            assertThat(applicantWitnesses.get(1).getValue().getFirstName()).isEqualTo("Second");
            assertThat(applicantWitnesses.get(1).getValue().getLastName()).isEqualTo("witness");

            assertThat(applicantWitnesses.get(2).getValue().getFlags()).isEqualTo(expectedWitness3Flags);
            assertThat(applicantWitnesses.get(2).getValue().getPartyID()).isEqualTo("partyId3");
            assertThat(applicantWitnesses.get(2).getValue().getFirstName()).isEqualTo("Third");
            assertThat(applicantWitnesses.get(2).getValue().getLastName()).isEqualTo("witnessy");
        }

        @Test
        public void shouldCreateFlagsStructureForApplicantExperts() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                .multiPartyClaimTwoApplicants()
                .build();

            Expert expert1 = Expert.builder().partyID("partyId1").firstName("First").lastName("Name").build();
            Expert expert2 = Expert.builder().partyID("partyId2").firstName("Second").lastName("expert").build();
            Expert expert3 = Expert.builder().partyID("partyId3").firstName("Third").lastName("expert").build();

            CaseData updatedCaseData = caseData.toBuilder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts
                                                           .builder()
                                                           .details(wrapElements(expert1, expert2))
                                                           .build())
                                  .build())
                .applicant2DQ(Applicant2DQ.builder()
                                  .applicant2DQExperts(Experts
                                                           .builder()
                                                           .details(wrapElements(expert3))
                                                           .build())
                                  .build())
                .build();

            CaseData.CaseDataBuilder<?, ?> caseDataBuilderToUpdateWithFlags = updatedCaseData.toBuilder();

            addApplicantExpertAndWitnessFlagsStructure(
                caseDataBuilderToUpdateWithFlags,
                updatedCaseData);

            CaseData caseDataWithFlags = caseDataBuilderToUpdateWithFlags.build();
            List<Element<PartyFlagStructure>> applicantExperts = caseDataWithFlags.getApplicantExperts();

            Flags expectedExpert1Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                .partyName("First Name")
                .details(List.of()).build();

            Flags expectedExpert2Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                .partyName("Second expert")
                .details(List.of()).build();

            Flags expectedExpert3Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                .partyName("Third expert")
                .details(List.of()).build();

            assertThat(applicantExperts).isNotNull();
            assertThat(applicantExperts).hasSize(3);

            assertThat(applicantExperts.get(0).getValue().getFlags()).isEqualTo(expectedExpert1Flags);
            assertThat(applicantExperts.get(0).getValue().getPartyID()).isEqualTo("partyId1");
            assertThat(applicantExperts.get(0).getValue().getFirstName()).isEqualTo("First");
            assertThat(applicantExperts.get(0).getValue().getLastName()).isEqualTo("Name");

            assertThat(applicantExperts.get(1).getValue().getFlags()).isEqualTo(expectedExpert2Flags);
            assertThat(applicantExperts.get(1).getValue().getPartyID()).isEqualTo("partyId2");
            assertThat(applicantExperts.get(1).getValue().getFirstName()).isEqualTo("Second");
            assertThat(applicantExperts.get(1).getValue().getLastName()).isEqualTo("expert");

            assertThat(applicantExperts.get(2).getValue().getFlags()).isEqualTo(expectedExpert3Flags);
            assertThat(applicantExperts.get(2).getValue().getPartyID()).isEqualTo("partyId3");
            assertThat(applicantExperts.get(2).getValue().getFirstName()).isEqualTo("Third");
            assertThat(applicantExperts.get(2).getValue().getLastName()).isEqualTo("expert");
        }

        @Test
        public void getAllCaseFlagsThatAreNotEmpty_1v1() {
            List<FlagDetail> expectedApplicant1Flags = flagDetails();
            List<FlagDetail> expectedApplicant1WitnessFlags = flagDetails();
            List<FlagDetail> expectedApplicant1ExpertFlags = flagDetails();
            List<FlagDetail> expectedApplicant1LitigationFriendFlags = flagDetails();
            List<FlagDetail> expectedRespondent1Flags = flagDetails();
            List<FlagDetail> expectedRespondent1WitnessFlags = flagDetails();
            List<FlagDetail> expectedRespondent1ExpertFlags = flagDetails();
            List<FlagDetail> expectedRespondent1LitigationFriendFlags = flagDetails();

            List<FlagDetail> expected = new ArrayList<>();
            expected.addAll(expectedApplicant1Flags);
            expected.addAll(expectedApplicant1WitnessFlags);
            expected.addAll(expectedApplicant1ExpertFlags);
            expected.addAll(expectedApplicant1LitigationFriendFlags);
            expected.addAll(expectedRespondent1Flags);
            expected.addAll(expectedRespondent1WitnessFlags);
            expected.addAll(expectedRespondent1ExpertFlags);
            expected.addAll(expectedRespondent1LitigationFriendFlags);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant1ExpertsAndWitnesses()
                .addApplicant1LitigationFriend()
                .addRespondent1ExpertsAndWitnesses()
                .addRespondent1LitigationFriend()
                .withApplicant1Flags()
                .withApplicant1WitnessFlags()
                .withApplicant1ExpertFlags()
                .withApplicant1LitigationFriendFlags()
                .withRespondent1Flags()
                .withRespondent1ExpertFlags()
                .withRespondent1WitnessFlags()
                .withRespondent1LitigationFriendFlags()
                .build();

            assertThat(getAllCaseFlags(caseData)).isEqualTo(expected);
        }

        @Test
        public void getAllCaseFlagsThatAreNotEmpty_2v1_App2Proceeds() {
            List<FlagDetail> expectedApplicant2Flags = flagDetails();
            List<FlagDetail> expectedApplicant2WitnessFlags = flagDetails();
            List<FlagDetail> expectedApplicant2ExpertFlags = flagDetails();
            List<FlagDetail> expectedApplicant2LitigationFriendFlags = flagDetails();
            List<FlagDetail> expectedRespondent1Flags = flagDetails();
            List<FlagDetail> expectedRespondent1WitnessFlags = flagDetails();
            List<FlagDetail> expectedRespondent1ExpertFlags = flagDetails();
            List<FlagDetail> expectedRespondent1LitigationFriendFlags = flagDetails();

            List<FlagDetail> expected = new ArrayList<>();
            expected.addAll(expectedApplicant2Flags); // add other parties
            expected.addAll(expectedApplicant2WitnessFlags);
            expected.addAll(expectedApplicant2ExpertFlags);
            expected.addAll(expectedApplicant2LitigationFriendFlags);
            expected.addAll(expectedRespondent1Flags);
            expected.addAll(expectedRespondent1WitnessFlags);
            expected.addAll(expectedRespondent1ExpertFlags);
            expected.addAll(expectedRespondent1LitigationFriendFlags);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicant2RespondToDefenceAndProceed_2v1()
                .multiPartyClaimTwoApplicants()
                .addApplicant2ExpertsAndWitnesses()
                .addApplicant2LitigationFriend()
                .addRespondent1ExpertsAndWitnesses()
                .addRespondent1LitigationFriend()
                .withApplicant2Flags()
                .withApplicant2WitnessFlags()
                .withApplicant2ExpertFlags()
                .withApplicant2LitigationFriendFlags()
                .withRespondent1Flags()
                .withRespondent1ExpertFlags()
                .withRespondent1WitnessFlags()
                .withRespondent1LitigationFriendFlags()
                .build();

            assertThat(getAllCaseFlags(caseData)).isEqualTo(expected);
        }

        @Test
        public void getAllCaseFlagsThatAreNotEmpty_1v2DS() {
            List<FlagDetail> expectedRespondent1Flags = flagDetails();
            List<FlagDetail> expectedRespondent1WitnessFlags = flagDetails();
            List<FlagDetail> expectedRespondent1ExpertFlags = flagDetails();
            List<FlagDetail> expectedRespondent1LitigationFriendFlags = flagDetails();
            List<FlagDetail> expectedRespondent2Flags = flagDetails();
            List<FlagDetail> expectedRespondent2ExpertFlags = flagDetails();
            List<FlagDetail> expectedRespondent2WitnessFlags = flagDetails();
            List<FlagDetail> expectedRespondent2LitigationFriendFlags = flagDetails();

            List<FlagDetail> expected = new ArrayList<>();

            expected.addAll(expectedRespondent1Flags);
            expected.addAll(expectedRespondent1WitnessFlags);
            expected.addAll(expectedRespondent1ExpertFlags);
            expected.addAll(expectedRespondent1LitigationFriendFlags);
            expected.addAll(expectedRespondent2Flags);
            expected.addAll(expectedRespondent2ExpertFlags);
            expected.addAll(expectedRespondent2WitnessFlags);
            expected.addAll(expectedRespondent2LitigationFriendFlags);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .addRespondent1ExpertsAndWitnesses()
                .addRespondent1LitigationFriend()
                .addRespondent2ExpertsAndWitnesses()
                .addRespondent2LitigationFriend()
                .withRespondent1Flags()
                .withRespondent1ExpertFlags()
                .withRespondent1WitnessFlags()
                .withRespondent1LitigationFriendFlags()
                .withRespondent2Flags()
                .withRespondent2ExpertFlags()
                .withRespondent2WitnessFlags()
                .withRespondent2LitigationFriendFlags()
                .build();

            assertThat(getAllCaseFlags(caseData)).isEqualTo(expected);
        }

        @Test
        public void filterCaseFlagsByActive() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .withApplicant1Flags()
                .withRespondent1Flags()
                .build();

            List<FlagDetail> expectedApplicant1Flags = activeFlagDetails();
            List<FlagDetail> expectedRespondent1Flags = activeFlagDetails();

            List<FlagDetail> expected = new ArrayList<>();
            expected.addAll(expectedApplicant1Flags);
            expected.addAll(expectedRespondent1Flags);

            assertThat(filter(getAllCaseFlags(caseData), CaseFlagPredicates.isActive())).isEqualTo(expected);
        }

        @Test
        public void filterCaseFlagsByHearingRelevant() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .withApplicant1Flags()
                .withRespondent1Flags()
                .build();

            List<FlagDetail> expectedApplicant1Flags = hearingRelevantFlagDetails();
            List<FlagDetail> expectedRespondent1Flags = hearingRelevantFlagDetails();

            List<FlagDetail> expected = new ArrayList<>();
            expected.addAll(expectedApplicant1Flags);
            expected.addAll(expectedRespondent1Flags);

            assertThat(filter(getAllCaseFlags(caseData), CaseFlagPredicates.isHearingRelevant())).isEqualTo(expected);
        }

        private List<FlagDetail> flagDetails() {
            FlagDetail details1 = FlagDetail.builder()
                .name("Vulnerable user")
                .flagComment("comment")
                .flagCode("AB001")
                .hearingRelevant(YES)
                .status("Active")
                .build();

            FlagDetail details2 = FlagDetail.builder()
                .name("Flight risk")
                .flagComment("comment")
                .flagCode("SM001")
                .hearingRelevant(YES)
                .status("Active")
                .build();

            FlagDetail details3 = FlagDetail.builder()
                .name("Audio/Video evidence")
                .flagComment("comment")
                .flagCode("RA001")
                .hearingRelevant(NO)
                .status("Active")
                .build();

            FlagDetail details4 = FlagDetail.builder()
                .name("Other")
                .flagComment("comment")
                .flagCode("AB001")
                .hearingRelevant(YES)
                .status("Inactive")
                .build();

            return List.of(details1, details2, details3, details4);
        }

        private List<FlagDetail> activeFlagDetails() {
            FlagDetail details1 = FlagDetail.builder()
                .name("Vulnerable user")
                .flagComment("comment")
                .flagCode("AB001")
                .hearingRelevant(YES)
                .status("Active")
                .build();

            FlagDetail details2 = FlagDetail.builder()
                .name("Flight risk")
                .flagComment("comment")
                .flagCode("SM001")
                .hearingRelevant(YES)
                .status("Active")
                .build();

            FlagDetail details3 = FlagDetail.builder()
                .name("Audio/Video evidence")
                .flagComment("comment")
                .flagCode("RA001")
                .hearingRelevant(NO)
                .status("Active")
                .build();

            return List.of(details1, details2, details3);

        }

        private List<FlagDetail> hearingRelevantFlagDetails() {
            FlagDetail details1 = FlagDetail.builder()
                .name("Vulnerable user")
                .flagComment("comment")
                .flagCode("AB001")
                .hearingRelevant(YES)
                .status("Active")
                .build();

            FlagDetail details2 = FlagDetail.builder()
                .name("Flight risk")
                .flagComment("comment")
                .flagCode("SM001")
                .hearingRelevant(YES)
                .status("Active")
                .build();

            FlagDetail details3 = FlagDetail.builder()
                .name("Other")
                .flagComment("comment")
                .flagCode("AB001")
                .hearingRelevant(YES)
                .status("Inactive")
                .build();

            return List.of(details1, details2, details3);
        }
    }

    @Nested
    class CreateAndUpdateFlagNamesAfterManageContactInformationEvent {

        @Nested
        class Parties {

            @Test
            void shouldUpdateFlagName_whenClaimant1NameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .applicant1(caseData.getApplicant1().toBuilder()
                                    .flags(Flags.builder()
                                               .partyName("Mr. John Rambo")
                                               .roleOnCase("applicant")
                                               .details(wrapElements(List.of(
                                                   FlagDetail.builder().name("flag name").build()))).build())
                                    .individualFirstName("Johnny")
                                    .individualLastName("Rambo new").build());

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                Flags actual = builder.build().getApplicant1().getFlags();
                Flags expected = Flags.builder().partyName("Mr. Johnny Rambo new")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("applicant").build();

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenClaimant2NameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateClaimIssued()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_TWO_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .applicant2(caseData.getApplicant2().toBuilder()
                                    .flags(Flags.builder()
                                               .partyName("Mr. Jason Rambo")
                                               .roleOnCase("applicant")
                                               .details(wrapElements(List.of(
                                                   FlagDetail.builder().name("flag name").build()))).build())
                                    .individualFirstName("JJ")
                                    .individualLastName("Rambo edited").build());

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                Flags actual = builder.build().getApplicant2().getFlags();
                Flags expected = Flags.builder().partyName("Mr. JJ Rambo edited")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("applicant").build();

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenRespondent1NameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent1(caseData.getRespondent1().toBuilder()
                                    .flags(Flags.builder()
                                               .partyName("Mr. Sole Trader")
                                               .roleOnCase("respondent")
                                               .details(wrapElements(List.of(
                                                   FlagDetail.builder().name("flag name").build()))).build())
                                    .soleTraderFirstName("Solo")
                                    .soleTraderLastName("New trader").build());

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                Flags actual = builder.build().getRespondent1().getFlags();
                Flags expected = Flags.builder().partyName("Mr. Solo New trader")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("respondent").build();

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenRespondent2NameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent2(caseData.getRespondent2().toBuilder()
                                     .flags(Flags.builder()
                                                .partyName("Mr. John Rambo")
                                                .roleOnCase("respondent")
                                                .details(wrapElements(List.of(
                                                    FlagDetail.builder().name("flag name").build()))).build())
                                     .individualTitle("Miss")
                                     .individualFirstName("Jenny")
                                     .individualLastName("Rombo").build());

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                Flags actual = builder.build().getRespondent2().getFlags();
                Flags expected = Flags.builder().partyName("Miss Jenny Rombo")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("respondent").build();

                assertThat(actual).isEqualTo(expected);
            }
        }

        @Nested
        class LitigationFriend {

            @Test
            void shouldUpdateFlagName_whenClaimant1LitigationFriendNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addApplicant1LitigationFriend()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_LITIGATION_FRIEND_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .applicant1LitigationFriend(caseData.getApplicant1LitigationFriend().toBuilder()
                                    .flags(Flags.builder()
                                               .partyName("Mr. Applicant Litigation Friend")
                                               .roleOnCase("litigation friend")
                                               .details(wrapElements(List.of(
                                                   FlagDetail.builder().name("flag name").build()))).build())
                                                    .fullName(null)
                                                    .firstName("Johnny").lastName("Rambo new")
                                                    .build());

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                Flags actual = builder.build().getApplicant1LitigationFriend().getFlags();
                Flags expected = Flags.builder().partyName("Johnny Rambo new")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("litigation friend").build();

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenClaimant2LitigationFriendNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addApplicant2LitigationFriend()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_TWO_LITIGATION_FRIEND_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .applicant2LitigationFriend(caseData.getApplicant2LitigationFriend().toBuilder()
                                                    .flags(Flags.builder()
                                                               .partyName("Applicant Two Litigation Friend")
                                                               .roleOnCase("litigation friend")
                                                               .details(wrapElements(List.of(
                                                                   FlagDetail.builder().name("flag name").build()))).build())
                                                    .fullName(null)
                                                    .firstName("Johnny").lastName("Rambo new")
                                                    .build());

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                Flags actual = builder.build().getApplicant2LitigationFriend().getFlags();
                Flags expected = Flags.builder().partyName("Johnny Rambo new")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("litigation friend").build();

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenRespondent1LitigationFriendNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addRespondent1LitigationFriend()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_LITIGATION_FRIEND_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent1LitigationFriend(caseData.getRespondent1LitigationFriend().toBuilder()
                                                    .flags(Flags.builder()
                                                               .partyName("Litigation Friend")
                                                               .roleOnCase("litigation friend")
                                                               .details(wrapElements(List.of(
                                                                   FlagDetail.builder().name("flag name").build()))).build())
                                                     .fullName(null)
                                                     .firstName("Johnny").lastName("Rambo new")
                                                     .build());

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                Flags actual = builder.build().getRespondent1LitigationFriend().getFlags();
                Flags expected = Flags.builder().partyName("Johnny Rambo new")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("litigation friend").build();

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenRespondent2LitigationFriendNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addRespondent2LitigationFriend()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_LITIGATION_FRIEND_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent2LitigationFriend(caseData.getRespondent2LitigationFriend().toBuilder()
                                                     .flags(Flags.builder()
                                                                .partyName("Litigation Friend")
                                                                .roleOnCase("litigation friend")
                                                                .details(wrapElements(List.of(
                                                                    FlagDetail.builder().name("flag name").build()))).build())
                                                     .fullName(null)
                                                     .firstName("Johnny").lastName("Rambo new")
                                                     .build());

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                Flags actual = builder.build().getRespondent2LitigationFriend().getFlags();
                Flags expected = Flags.builder().partyName("Johnny Rambo new")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("litigation friend").build();

                assertThat(actual).isEqualTo(expected);
            }
        }

        @Nested
        class LegalRepIndividuals {

            @Test
            void shouldCreateFlag_whenClaimantLRIndividualAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .applicant1LRIndividuals(wrapElements(PartyFlagStructure.builder()
                                                              .firstName("Legally").lastName("Rep")
                                                              .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getApplicant1LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Legally Rep")
                    .details(List.of())
                    .roleOnCase("Civil - Organisation").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenClaimantLRIndividualNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addApplicantLRIndividual("Legal", "Rep")
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID).build())
                    .build();

                PartyFlagStructure lrIndividual = unwrapElements(caseData.getApplicant1LRIndividuals()).get(0);

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .applicant1LRIndividuals(wrapElements(lrIndividual.toBuilder()
                                                    .flags(Flags.builder()
                                                               .partyName("Legal Rep")
                                                               .roleOnCase("Civil - Organisation")
                                                               .details(wrapElements(List.of(
                                                                   FlagDetail.builder().name("flag name").build()))).build())
                                                    .firstName("Legally").lastName("Rep")
                                                    .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getApplicant1LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Legally Rep")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("Civil - Organisation").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("app-lr-ind-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent1LRIndividualAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent1LRIndividuals(wrapElements(PartyFlagStructure.builder()
                                                              .firstName("Legally").lastName("Rep")
                                                              .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getRespondent1LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Legally Rep")
                    .details(List.of())
                    .roleOnCase("Civil - Organisation").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenRespondent1LRIndividualNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addRespondent1LRIndividual("Legal", "Rep")
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID).build())
                    .build();

                PartyFlagStructure lrIndividual = unwrapElements(caseData.getRespondent1LRIndividuals()).get(0);

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent1LRIndividuals(wrapElements(lrIndividual.toBuilder()
                                                              .flags(Flags.builder()
                                                                         .partyName("Legal Rep")
                                                                         .roleOnCase("Civil - Organisation")
                                                                         .details(wrapElements(List.of(
                                                                             FlagDetail.builder().name("flag name").build()))).build())
                                                              .firstName("Legally").lastName("Rep")
                                                              .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getRespondent1LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Legally Rep")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("Civil - Organisation").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("res-1-lr-ind-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent2LRIndividualAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent2LRIndividuals(wrapElements(PartyFlagStructure.builder()
                                                               .firstName("Legally").lastName("Rep")
                                                               .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getRespondent2LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Legally Rep")
                    .details(List.of())
                    .roleOnCase("Civil - Organisation").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenRespondent2LRIndividualNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addRespondent2LRIndividual("Legal", "Rep")
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID).build())
                    .build();

                PartyFlagStructure lrIndividual = unwrapElements(caseData.getRespondent2LRIndividuals()).get(0);

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent2LRIndividuals(wrapElements(lrIndividual.toBuilder()
                                                               .flags(Flags.builder()
                                                                          .partyName("Legal Rep")
                                                                          .roleOnCase("Civil - Organisation")
                                                                          .details(wrapElements(List.of(
                                                                              FlagDetail.builder().name("flag name").build()))).build())
                                                               .firstName("Legally").lastName("Rep")
                                                               .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getRespondent2LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Legally Rep")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("Civil - Organisation").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("res-2-lr-ind-party-id");
            }
        }

        @Nested
        class OrgIndividuals {

            @Test
            void shouldCreateFlag_whenClaimant1OrgIndividualAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_ORG_INDIVIDUALS_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .applicant1OrgIndividuals(wrapElements(PartyFlagStructure.builder()
                                                              .firstName("Org").lastName("Ind")
                                                              .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getApplicant1OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Org Ind")
                    .details(List.of())
                    .roleOnCase("Mr. John Rambo").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenClaimant1OrgIndividualNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addApplicant1OrgIndividual("Org", "Person")
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_ORG_INDIVIDUALS_ID).build())
                    .build();

                PartyFlagStructure orgIndividual = unwrapElements(caseData.getApplicant1OrgIndividuals()).get(0);

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .applicant1OrgIndividuals(wrapElements(orgIndividual.toBuilder()
                                                              .flags(Flags.builder()
                                                                         .partyName("Org Person")
                                                                         .roleOnCase("Mr. John Rambo")
                                                                         .details(wrapElements(List.of(
                                                                             FlagDetail.builder().name("flag name").build()))).build())
                                                              .firstName("Org").lastName("Ind")
                                                              .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getApplicant1OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Org Ind")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("Mr. John Rambo").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("app-1-org-ind-party-id");
            }

            @Test
            void shouldCreateFlag_whenClaimant2OrgIndividualAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .multiPartyClaimTwoApplicants()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_TWO_ORG_INDIVIDUALS_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .applicant2OrgIndividuals(wrapElements(PartyFlagStructure.builder()
                                                               .firstName("Org").lastName("Ind")
                                                               .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getApplicant2OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Org Ind")
                    .details(List.of())
                    .roleOnCase("Mr. Jason Rambo").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenClaimant2OrgIndividualNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .multiPartyClaimTwoApplicants()
                    .addApplicant2OrgIndividual("Org", "Person")
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_TWO_ORG_INDIVIDUALS_ID).build())
                    .build();

                PartyFlagStructure orgIndividual = unwrapElements(caseData.getApplicant2OrgIndividuals()).get(0);

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .applicant2OrgIndividuals(wrapElements(orgIndividual.toBuilder()
                                                               .flags(Flags.builder()
                                                                          .partyName("Org Person")
                                                                          .roleOnCase("Mr. Jason Rambo")
                                                                          .details(wrapElements(List.of(
                                                                              FlagDetail.builder().name("flag name").build()))).build())
                                                               .firstName("Org").lastName("Ind")
                                                               .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getApplicant2OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Org Ind")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("Mr. Jason Rambo").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("app-2-org-ind-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent1OrgIndividualAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_ORG_INDIVIDUALS_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent1OrgIndividuals(wrapElements(PartyFlagStructure.builder()
                                                               .firstName("Org").lastName("Ind")
                                                               .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getRespondent1OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Org Ind")
                    .details(List.of())
                    .roleOnCase("Mr. Sole Trader").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenRespondent1OrgIndividualNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addRespondent1OrgIndividual("Org", "Person")
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_ORG_INDIVIDUALS_ID).build())
                    .build();

                PartyFlagStructure orgIndividual = unwrapElements(caseData.getRespondent1OrgIndividuals()).get(0);

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent1OrgIndividuals(wrapElements(orgIndividual.toBuilder()
                                                               .flags(Flags.builder()
                                                                          .partyName("Org Person")
                                                                          .roleOnCase("Mr. Sole Trader")
                                                                          .details(wrapElements(List.of(
                                                                              FlagDetail.builder().name("flag name").build()))).build())
                                                               .firstName("Org").lastName("Ind")
                                                               .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getRespondent1OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Org Ind")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("Mr. Sole Trader").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("res-1-org-ind-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent2OrgIndividualAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_ORG_INDIVIDUALS_ID).build())
                    .build();

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent2OrgIndividuals(wrapElements(PartyFlagStructure.builder()
                                                                .firstName("Org").lastName("Ind")
                                                                .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getRespondent2OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Org Ind")
                    .details(List.of())
                    .roleOnCase("Mr. John Rambo").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenRespondent2OrgIndividualNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .addRespondent2OrgIndividual("Org", "Person")
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_ORG_INDIVIDUALS_ID).build())
                    .build();

                PartyFlagStructure lrIndividual = unwrapElements(caseData.getRespondent2OrgIndividuals()).get(0);

                CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                    .respondent2OrgIndividuals(wrapElements(lrIndividual.toBuilder()
                                                                .flags(Flags.builder()
                                                                           .partyName("Org Person")
                                                                           .roleOnCase("Mr. John Rambo")
                                                                           .details(wrapElements(List.of(
                                                                               FlagDetail.builder().name("flag name").build()))).build())
                                                                .firstName("Org").lastName("Ind")
                                                                .build()));

                CaseFlagUtils.createOrUpdateFlags(builder, builder.build(), organisationService);

                PartyFlagStructure individual = unwrapElements(builder.build().getRespondent2OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = Flags.builder().partyName("Org Ind")
                    .details(wrapElements(List.of(
                        FlagDetail.builder().name("flag name").build())))
                    .roleOnCase("Mr. John Rambo").build();

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("res-2-org-ind-party-id");
            }
        }
    }
}
