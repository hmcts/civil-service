package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
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
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_WITNESSES_ID;

class CaseFlagUtilsTest {

    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        organisationService = mock(OrganisationService.class);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(new Organisation().setName("Civil - Organisation")));
    }

    @Nested
    class CreateFlags {

        @Test
        void shouldCreateFlags() {
            Flags expected = new Flags().setPartyName("partyName").setRoleOnCase("roleOnCase").setDetails(List.of());
            Flags actual = CaseFlagUtils.createFlags("partyName", "roleOnCase");
            assertEquals(expected, actual);
        }
    }

    @Nested
    class UpdateParty {

        @Test
        void shouldUpdatePartyWithFlagsMeta() {
            Party party = PartyBuilder.builder().individual().build();
            Flags flags = new Flags().setPartyName("Mr. John Rambo").setRoleOnCase("applicant").setDetails(List.of());
            Party expected = party.toBuilder().flags(flags).build();

            Party actual = CaseFlagUtils.updateParty("applicant", party);

            assertEquals(expected, actual);
        }

        @Test
        void shouldNotUpdatePartyFlagsIfFlagsExist() {
            Party existingParty = PartyBuilder.builder().individual().build()
                .toBuilder()
                .flags(new Flags().setPartyName("Mr. John Rambo").setRoleOnCase("applicant").setDetails(List.of())).build();

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
            LitigationFriend litFriend = new LitigationFriend().setFirstName("John").setLastName("Rambo");
            Flags flags = new Flags().setPartyName("John Rambo").setRoleOnCase("applicant").setDetails(List.of());
            LitigationFriend expected = litFriend.copy().setFlags(flags);

            LitigationFriend actual = CaseFlagUtils.updateLitFriend("applicant", litFriend);

            assertEquals(expected, actual);
        }

        @Test
        void shouldNotUpdateLitigationFriendFlagsIfFlagsExist() {
            LitigationFriend existingLitFriend = new LitigationFriend().setFirstName("John").setLastName("Rambo")
                .copy()
                .setFlags(new Flags().setPartyName("John Rambo").setRoleOnCase("applicant").setDetails(List.of()));

            LitigationFriend actual = CaseFlagUtils.updateLitFriend("updatedField", existingLitFriend);

            assertEquals(existingLitFriend, actual);
        }

        @Test
        void shouldReturnNullWhenLitigationFriendIsNull() {
            assertNull(CaseFlagUtils.updateLitFriend("applicant", null));
        }
    }

    @Nested
    class UpdateDQParties {
        @Test
        void shouldCreateFlagsStructureForRespondentExperts() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            Expert expert1 = new Expert().setPartyID("partyId1").setFirstName("First").setLastName("Name");
            Expert expert2 = new Expert().setPartyID("partyId2").setFirstName("Second").setLastName("expert");
            Expert expert3 = new Expert().setPartyID("partyId3").setFirstName("Third").setLastName("experto");

            CaseData updatedCaseData = caseData.toBuilder()
                .respondent1DQ(new Respondent1DQ()
                                   .setRespondent1DQExperts(new Experts()
                                                                .setDetails(wrapElements(expert1, expert2))))
                .respondent2DQ(new Respondent2DQ()
                                   .setRespondent2DQExperts(new Experts()
                                                                .setDetails(wrapElements(expert3))))
                .build();

            addRespondentDQPartiesFlagStructure(updatedCaseData);

            List<Element<PartyFlagStructure>> respondent1ExpertsWithFlags = updatedCaseData.getRespondent1Experts();
            List<Element<PartyFlagStructure>> respondent2ExpertsWithFlags = updatedCaseData.getRespondent2Experts();

            Flags expectedExpert1Flags = new Flags().setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                .setPartyName("First Name")
                .setDetails(List.of());

            Flags expectedExpert2Flags = new Flags().setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                .setPartyName("Second expert")
                .setDetails(List.of());

            Flags expectedExpert3Flags = new Flags().setRoleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)
                .setPartyName("Third experto")
                .setDetails(List.of());

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
        void shouldCreateFlagsStructureForRespondentWitness() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            Witness witness1 = new Witness().setPartyID("partyId1").setFirstName("First").setLastName("Name");
            Witness witness2 = new Witness().setPartyID("partyId2").setFirstName("Second").setLastName("witness");
            Witness witness3 = new Witness().setPartyID("partyId3").setFirstName("Third").setLastName("witnessy");

            CaseData updatedCaseData = caseData.toBuilder()
                .respondent1DQ(new Respondent1DQ()
                                   .setRespondent1DQWitnesses(new Witnesses()
                                                                  .setDetails(wrapElements(witness1, witness2))))
                .respondent2DQ(new Respondent2DQ()
                                   .setRespondent2DQWitnesses(new Witnesses()
                                                                  .setDetails(wrapElements(witness3))))
                .build();

            addRespondentDQPartiesFlagStructure(updatedCaseData);

            List<Element<PartyFlagStructure>> respondent1WitnessWithFlags = updatedCaseData.getRespondent1Witnesses();
            List<Element<PartyFlagStructure>> respondent2WitnessWithFlags = updatedCaseData.getRespondent2Witnesses();

            Flags expectedWitness1Flags = new Flags().setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                .setPartyName("First Name")
                .setDetails(List.of());

            Flags expectedWitness2Flags = new Flags().setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                .setPartyName("Second witness")
                .setDetails(List.of());

            Flags expectedWitness3Flags = new Flags().setRoleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS)
                .setPartyName("Third witnessy")
                .setDetails(List.of());

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
        void shouldCreateFlagsStructureForApplicantWitness() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                .multiPartyClaimTwoApplicants()
                .build();

            Witness witness1 = new Witness().setPartyID("partyId1").setFirstName("First").setLastName("Name");
            Witness witness2 = new Witness().setPartyID("partyId2").setFirstName("Second").setLastName("witness");
            Witness witness3 = new Witness().setPartyID("partyId3").setFirstName("Third").setLastName("witnessy");

            CaseData updatedCaseData = caseData.toBuilder()
                .applicant1DQ(new Applicant1DQ()
                                  .setApplicant1DQWitnesses(new Witnesses()
                                                                .setDetails(wrapElements(witness1, witness2))))
                .applicant2DQ(new Applicant2DQ()
                                  .setApplicant2DQWitnesses(new Witnesses()
                                                                .setDetails(wrapElements(witness3))))
                .build();

            addApplicantExpertAndWitnessFlagsStructure(updatedCaseData);

            List<Element<PartyFlagStructure>> applicantWitnesses = updatedCaseData.getApplicantWitnesses();

            Flags expectedWitness1Flags = new Flags().setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                .setPartyName("First Name")
                .setDetails(List.of());

            Flags expectedWitness2Flags = new Flags().setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                .setPartyName("Second witness")
                .setDetails(List.of());

            Flags expectedWitness3Flags = new Flags().setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                .setPartyName("Third witnessy")
                .setDetails(List.of());

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
        void shouldCreateFlagsStructureForApplicantExperts() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                .multiPartyClaimTwoApplicants()
                .build();

            Expert expert1 = new Expert().setPartyID("partyId1").setFirstName("First").setLastName("Name");
            Expert expert2 = new Expert().setPartyID("partyId2").setFirstName("Second").setLastName("expert");
            Expert expert3 = new Expert().setPartyID("partyId3").setFirstName("Third").setLastName("expert");

            CaseData updatedCaseData = caseData.toBuilder()
                .applicant1DQ(new Applicant1DQ()
                                  .setApplicant1DQExperts(new Experts()
                                                              .setDetails(wrapElements(expert1, expert2))))
                .applicant2DQ(new Applicant2DQ()
                                  .setApplicant2DQExperts(new Experts()
                                                              .setDetails(wrapElements(expert3))))
                .build();

            addApplicantExpertAndWitnessFlagsStructure(updatedCaseData);

            List<Element<PartyFlagStructure>> applicantExperts = updatedCaseData.getApplicantExperts();

            Flags expectedExpert1Flags = new Flags().setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                .setPartyName("First Name")
                .setDetails(List.of());

            Flags expectedExpert2Flags = new Flags().setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                .setPartyName("Second expert")
                .setDetails(List.of());

            Flags expectedExpert3Flags = new Flags().setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                .setPartyName("Third expert")
                .setDetails(List.of());

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
        void getAllCaseFlagsThatAreNotEmpty_1v1() {
            List<FlagDetail> expectedApplicant1Flags = flagDetails();
            List<FlagDetail> expectedApplicant1WitnessFlags = flagDetails();
            List<FlagDetail> expectedApplicant1ExpertFlags = flagDetails();
            List<FlagDetail> expectedApplicant1LitigationFriendFlags = flagDetails();
            List<FlagDetail> expectedApplicant1OrgIndividualFlags = flagDetails();
            List<FlagDetail> expectedApplicant1LRIndividualFlags = flagDetails();
            List<FlagDetail> expectedRespondent1Flags = flagDetails();
            List<FlagDetail> expectedRespondent1WitnessFlags = flagDetails();
            List<FlagDetail> expectedRespondent1ExpertFlags = flagDetails();
            List<FlagDetail> expectedRespondent1LitigationFriendFlags = flagDetails();
            List<FlagDetail> expectedRespondent1OrgIndividualFlags = flagDetails();
            List<FlagDetail> expectedRespondent1LRIndividualFlags = flagDetails();

            List<FlagDetail> expected = new ArrayList<>();
            expected.addAll(expectedApplicant1Flags);
            expected.addAll(expectedApplicant1WitnessFlags);
            expected.addAll(expectedApplicant1ExpertFlags);
            expected.addAll(expectedApplicant1LitigationFriendFlags);
            expected.addAll(expectedRespondent1Flags);
            expected.addAll(expectedRespondent1WitnessFlags);
            expected.addAll(expectedRespondent1ExpertFlags);
            expected.addAll(expectedRespondent1LitigationFriendFlags);
            expected.addAll(expectedApplicant1OrgIndividualFlags);
            expected.addAll(expectedRespondent1OrgIndividualFlags);
            expected.addAll(expectedApplicant1LRIndividualFlags);
            expected.addAll(expectedRespondent1LRIndividualFlags);

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
                .withApplicant1OrgIndividualFlags()
                .withApplicant1LRIndividualFlags()
                .withRespondent1Flags()
                .withRespondent1ExpertFlags()
                .withRespondent1WitnessFlags()
                .withRespondent1LitigationFriendFlags()
                .withRespondent1OrgIndividualFlags()
                .withRespondent1LRIndividualFlags()
                .build();

            assertThat(getAllCaseFlags(caseData)).isEqualTo(expected);
        }

        @Test
        void getAllCaseFlagsThatAreNotEmpty_2v1_App2Proceeds() {
            List<FlagDetail> expectedApplicant2Flags = flagDetails();
            List<FlagDetail> expectedApplicant2WitnessFlags = flagDetails();
            List<FlagDetail> expectedApplicant2ExpertFlags = flagDetails();
            List<FlagDetail> expectedApplicant2LitigationFriendFlags = flagDetails();
            List<FlagDetail> expectedApplicant2OrgIndividualFlags = flagDetails();
            List<FlagDetail> expectedRespondent1Flags = flagDetails();
            List<FlagDetail> expectedRespondent1WitnessFlags = flagDetails();
            List<FlagDetail> expectedRespondent1ExpertFlags = flagDetails();
            List<FlagDetail> expectedRespondent1LitigationFriendFlags = flagDetails();
            List<FlagDetail> expectedRespondent1OrgIndividualFlags = flagDetails();
            List<FlagDetail> expectedRespondent1LRIndividualFlags = flagDetails();
            List<FlagDetail> expectedApplicant1OrgIndividualFlags = flagDetails();
            List<FlagDetail> expectedApplicant1LRIndividualFlags = flagDetails();

            List<FlagDetail> expected = new ArrayList<>();
            expected.addAll(expectedApplicant2Flags); // add other parties
            expected.addAll(expectedApplicant2WitnessFlags);
            expected.addAll(expectedApplicant2ExpertFlags);
            expected.addAll(expectedApplicant2LitigationFriendFlags);
            expected.addAll(expectedApplicant1OrgIndividualFlags);
            expected.addAll(expectedApplicant2OrgIndividualFlags);
            expected.addAll(expectedRespondent1Flags);
            expected.addAll(expectedRespondent1WitnessFlags);
            expected.addAll(expectedRespondent1ExpertFlags);
            expected.addAll(expectedRespondent1LitigationFriendFlags);
            expected.addAll(expectedRespondent1OrgIndividualFlags);
            expected.addAll(expectedApplicant1LRIndividualFlags);
            expected.addAll(expectedRespondent1LRIndividualFlags);

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
                .withApplicant1OrgIndividualFlags()
                .withApplicant2OrgIndividualFlags()
                .withApplicant1LRIndividualFlags()
                .withRespondent1Flags()
                .withRespondent1ExpertFlags()
                .withRespondent1WitnessFlags()
                .withRespondent1LitigationFriendFlags()
                .withRespondent1OrgIndividualFlags()
                .withRespondent1LRIndividualFlags()
                .build();

            assertThat(getAllCaseFlags(caseData)).isEqualTo(expected);
        }

        @Test
        void getAllCaseFlagsThatAreNotEmpty_1v2DS() {
            List<FlagDetail> expectedRespondent1Flags = flagDetails();
            List<FlagDetail> expectedRespondent1WitnessFlags = flagDetails();
            List<FlagDetail> expectedRespondent1ExpertFlags = flagDetails();
            List<FlagDetail> expectedRespondent1LitigationFriendFlags = flagDetails();
            List<FlagDetail> expectedRespondent1OrgIndividualFlags = flagDetails();
            List<FlagDetail> expectedRespondent1LRIndividualFlags = flagDetails();
            List<FlagDetail> expectedRespondent2Flags = flagDetails();
            List<FlagDetail> expectedRespondent2ExpertFlags = flagDetails();
            List<FlagDetail> expectedRespondent2WitnessFlags = flagDetails();
            List<FlagDetail> expectedRespondent2LitigationFriendFlags = flagDetails();
            List<FlagDetail> expectedRespondent2OrgIndividualFlags = flagDetails();
            List<FlagDetail> expectedRespondent2LRIndividualFlags = flagDetails();

            List<FlagDetail> expected = new ArrayList<>();

            expected.addAll(expectedRespondent1Flags);
            expected.addAll(expectedRespondent1WitnessFlags);
            expected.addAll(expectedRespondent1ExpertFlags);
            expected.addAll(expectedRespondent1LitigationFriendFlags);
            expected.addAll(expectedRespondent1OrgIndividualFlags);
            expected.addAll(expectedRespondent2Flags);
            expected.addAll(expectedRespondent2ExpertFlags);
            expected.addAll(expectedRespondent2WitnessFlags);
            expected.addAll(expectedRespondent2LitigationFriendFlags);
            expected.addAll(expectedRespondent2OrgIndividualFlags);
            expected.addAll(expectedRespondent1LRIndividualFlags);
            expected.addAll(expectedRespondent2LRIndividualFlags);

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
                .withRespondent1OrgIndividualFlags()
                .withRespondent1LRIndividualFlags()
                .withRespondent2Flags()
                .withRespondent2ExpertFlags()
                .withRespondent2WitnessFlags()
                .withRespondent2LitigationFriendFlags()
                .withRespondent2OrgIndividualFlags()
                .withRespondent2LRIndividualFlags()
                .build();

            assertThat(getAllCaseFlags(caseData)).isEqualTo(expected);
        }

        @Test
        void filterCaseFlagsByActive() {
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
        void filterCaseFlagsByHearingRelevant() {
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
            FlagDetail details1 = new FlagDetail()
                .setName("Vulnerable user")
                .setFlagComment("comment")
                .setFlagCode("AB001")
                .setHearingRelevant(YES)
                .setStatus("Active")
                .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
                .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

            FlagDetail details2 = new FlagDetail()
                .setName("Flight risk")
                .setFlagComment("comment")
                .setFlagCode("SM001")
                .setHearingRelevant(YES)
                .setStatus("Active")
                .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
                .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

            FlagDetail details3 = new FlagDetail()
                .setName("Audio/Video evidence")
                .setFlagComment("comment")
                .setFlagCode("RA001")
                .setHearingRelevant(NO)
                .setStatus("Active")
                .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
                .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

            FlagDetail details4 = new FlagDetail()
                .setName("Other")
                .setFlagComment("comment")
                .setFlagCode("AB001")
                .setHearingRelevant(YES)
                .setStatus("Inactive")
                .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
                .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

            return List.of(details1, details2, details3, details4);
        }

        private List<FlagDetail> activeFlagDetails() {
            FlagDetail details1 = new FlagDetail()
                .setName("Vulnerable user")
                .setFlagComment("comment")
                .setFlagCode("AB001")
                .setHearingRelevant(YES)
                .setStatus("Active")
                .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
                .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

            FlagDetail details2 = new FlagDetail()
                .setName("Flight risk")
                .setFlagComment("comment")
                .setFlagCode("SM001")
                .setHearingRelevant(YES)
                .setStatus("Active")
                .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
                .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

            FlagDetail details3 = new FlagDetail()
                .setName("Audio/Video evidence")
                .setFlagComment("comment")
                .setFlagCode("RA001")
                .setHearingRelevant(NO)
                .setStatus("Active")
                .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
                .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

            return List.of(details1, details2, details3);

        }

        private List<FlagDetail> hearingRelevantFlagDetails() {
            FlagDetail details1 = new FlagDetail()
                .setName("Vulnerable user")
                .setFlagComment("comment")
                .setFlagCode("AB001")
                .setHearingRelevant(YES)
                .setStatus("Active")
                .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
                .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

            FlagDetail details2 = new FlagDetail()
                .setName("Flight risk")
                .setFlagComment("comment")
                .setFlagCode("SM001")
                .setHearingRelevant(YES)
                .setStatus("Active")
                .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
                .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

            FlagDetail details3 = new FlagDetail()
                .setName("Other")
                .setFlagComment("comment")
                .setFlagCode("AB001")
                .setHearingRelevant(YES)
                .setStatus("Inactive")
                .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
                .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

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

                caseData.setApplicant1(caseData.getApplicant1().toBuilder()
                                           .flags(new Flags()
                                                      .setPartyName("Mr. John Rambo")
                                                      .setRoleOnCase("applicant")
                                                      .setDetails(wrapElements(List.of(
                                                          new FlagDetail().setName("flag name")))))
                                           .individualFirstName("Johnny")
                                           .individualLastName("Rambo new").build());

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getApplicant1().getFlags();
                Flags expected = new Flags().setPartyName("Mr. Johnny Rambo new")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("applicant");

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldNotUpdateClaimant1FlagName_whenFlagIsMissing() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_ID).build())
                    .build();

                caseData.setApplicant1(caseData.getApplicant1().toBuilder()
                                           .individualFirstName("Johnny")
                                           .individualLastName("Rambo new").build());

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getApplicant1().getFlags();
                Flags expected = caseData.getApplicant1().getFlags();

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenClaimant2NameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateClaimIssued()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_TWO_ID).build())
                    .build();

                caseData.setApplicant2(caseData.getApplicant2().toBuilder()
                                           .flags(new Flags()
                                                      .setPartyName("Mr. Jason Rambo")
                                                      .setRoleOnCase("applicant")
                                                      .setDetails(wrapElements(List.of(
                                                          new FlagDetail().setName("flag name")))))
                                           .individualFirstName("JJ")
                                           .individualLastName("Rambo edited").build());

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getApplicant2().getFlags();
                Flags expected = new Flags().setPartyName("Mr. JJ Rambo edited")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("applicant");

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldNotUpdateClaimant2FlagName_whenFlagIsMissing() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateClaimIssued()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_TWO_ID).build())
                    .build();

                caseData.setApplicant2(caseData.getApplicant2().toBuilder()
                                           .individualFirstName("JJ")
                                           .individualLastName("Rambo edited").build());

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getApplicant2().getFlags();
                Flags expected = caseData.getApplicant2().getFlags();

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenRespondent1NameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_ID).build())
                    .build();

                caseData.setRespondent1(caseData.getRespondent1().toBuilder()
                                            .flags(new Flags()
                                                       .setPartyName("Mr. Sole Trader")
                                                       .setRoleOnCase("respondent")
                                                       .setDetails(wrapElements(List.of(
                                                           new FlagDetail().setName("flag name")))))
                                            .soleTraderFirstName("Solo")
                                            .soleTraderLastName("New trader").build());

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getRespondent1().getFlags();
                Flags expected = new Flags().setPartyName("Mr. Solo New trader")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("respondent");

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldNotUpdateDefendant1FlagName_whenFlagIsMissing() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_ID).build())
                    .build();

                caseData.setRespondent1(caseData.getRespondent1().toBuilder()
                                            .soleTraderFirstName("Solo")
                                            .soleTraderLastName("New trader").build());

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getRespondent1().getFlags();
                Flags expected = caseData.getRespondent1().getFlags();

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenRespondent2NameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_ID).build())
                    .build();

                caseData.setRespondent2(caseData.getRespondent2().toBuilder()
                                            .flags(new Flags()
                                                       .setPartyName("Mr. John Rambo")
                                                       .setRoleOnCase("respondent")
                                                       .setDetails(wrapElements(List.of(
                                                           new FlagDetail().setName("flag name")))))
                                            .individualTitle("Miss")
                                            .individualFirstName("Jenny")
                                            .individualLastName("Rombo").build());

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getRespondent2().getFlags();
                Flags expected = new Flags().setPartyName("Miss Jenny Rombo")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("respondent");

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldNotUpdateDefendant2FlagName_whenFlagIsMissing() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_ID).build())
                    .build();

                caseData.setRespondent2(caseData.getRespondent2().toBuilder()
                                            .individualTitle("Miss")
                                            .individualFirstName("Jenny")
                                            .individualLastName("Rombo").build());

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getRespondent2().getFlags();
                Flags expected = caseData.getRespondent2().getFlags();

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

                caseData.setApplicant1LitigationFriend(caseData.getApplicant1LitigationFriend().copy()
                                                           .setFlags(new Flags()
                                                                      .setPartyName("Mr. Applicant Litigation Friend")
                                                                      .setRoleOnCase("litigation friend")
                                                                      .setDetails(wrapElements(List.of(
                                                                          new FlagDetail().setName("flag name")))))
                                                           .setFullName(null)
                                                           .setFirstName("Johnny").setLastName("Rambo new"));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getApplicant1LitigationFriend().getFlags();
                Flags expected = new Flags().setPartyName("Johnny Rambo new")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("litigation friend");

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenClaimant2LitigationFriendNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addApplicant2LitigationFriend()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_TWO_LITIGATION_FRIEND_ID).build())
                    .build();

                caseData.setApplicant2LitigationFriend(caseData.getApplicant2LitigationFriend().copy()
                                                           .setFlags(new Flags()
                                                                      .setPartyName("Applicant Two Litigation Friend")
                                                                      .setRoleOnCase("litigation friend")
                                                                      .setDetails(wrapElements(List.of(
                                                                          new FlagDetail().setName("flag name")))))
                                                           .setFullName(null)
                                                           .setFirstName("Johnny").setLastName("Rambo new"));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getApplicant2LitigationFriend().getFlags();
                Flags expected = new Flags().setPartyName("Johnny Rambo new")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("litigation friend");

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldUpdateFlagName_whenRespondent1LitigationFriendNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addRespondent1LitigationFriend()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_LITIGATION_FRIEND_ID).build())
                    .build();

                caseData.setRespondent1LitigationFriend(caseData.getRespondent1LitigationFriend().copy()
                                                            .setFlags(new Flags()
                                                                       .setPartyName("Litigation Friend")
                                                                       .setRoleOnCase("litigation friend")
                                                                       .setDetails(wrapElements(List.of(
                                                                           new FlagDetail().setName("flag name")))))
                                                            .setFullName(null)
                                                            .setFirstName("Johnny").setLastName("Rambo new"));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getRespondent1LitigationFriend().getFlags();
                Flags expected = new Flags().setPartyName("Johnny Rambo new")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("litigation friend");

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

                caseData.setRespondent2LitigationFriend(caseData.getRespondent2LitigationFriend().copy()
                                                            .setFlags(new Flags()
                                                                       .setPartyName("Litigation Friend")
                                                                       .setRoleOnCase("litigation friend")
                                                                       .setDetails(wrapElements(List.of(
                                                                           new FlagDetail().setName("flag name")))))
                                                            .setFullName(null)
                                                            .setFirstName("Johnny").setLastName("Rambo new"));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                Flags actual = caseData.getRespondent2LitigationFriend().getFlags();
                Flags expected = new Flags().setPartyName("Johnny Rambo new")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("litigation friend");

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

                caseData.setApplicant1LRIndividuals(wrapElements(new PartyFlagStructure()
                                                                     .setFirstName("Legally").setLastName("Rep")
                                                                     ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getApplicant1LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Legally Rep")
                    .setDetails(List.of())
                    .setRoleOnCase("Civil - Organisation");

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

                caseData.setApplicant1LRIndividuals(wrapElements(lrIndividual.copy()
                                                                     .setFlags(new Flags()
                                                                                .setPartyName("Legal Rep")
                                                                                .setRoleOnCase("Civil - Organisation")
                                                                                .setDetails(wrapElements(List.of(
                                                                                    new FlagDetail().setName("flag name")))))
                                                                     .setFirstName("Legally").setLastName("Rep")
                                                                     ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getApplicant1LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Legally Rep")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Civil - Organisation");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("app-lr-ind-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent1LRIndividualAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID).build())
                    .build();

                caseData.setRespondent1LRIndividuals(wrapElements(new PartyFlagStructure()
                                                                      .setFirstName("Legally").setLastName("Rep")
                                                                      ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent1LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Legally Rep")
                    .setDetails(List.of())
                    .setRoleOnCase("Civil - Organisation");

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

                caseData.setRespondent1LRIndividuals(wrapElements(lrIndividual.copy()
                                                                      .setFlags(new Flags()
                                                                                 .setPartyName("Legal Rep")
                                                                                 .setRoleOnCase("Civil - Organisation")
                                                                                 .setDetails(wrapElements(List.of(
                                                                                     new FlagDetail().setName(
                                                                                         "flag name")))))
                                                                      .setFirstName("Legally").setLastName("Rep")
                                                                      ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent1LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Legally Rep")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Civil - Organisation");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("res-1-lr-ind-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent2LRIndividualAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID).build())
                    .build();

                caseData.setRespondent2LRIndividuals(wrapElements(new PartyFlagStructure()
                                                                      .setFirstName("Legally").setLastName("Rep")
                                                                      ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent2LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Legally Rep")
                    .setDetails(List.of())
                    .setRoleOnCase("Civil - Organisation");

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

                caseData.setRespondent2LRIndividuals(wrapElements(lrIndividual.copy()
                                                                      .setFlags(new Flags()
                                                                                 .setPartyName("Legal Rep")
                                                                                 .setRoleOnCase("Civil - Organisation")
                                                                                 .setDetails(wrapElements(List.of(
                                                                                     new FlagDetail().setName(
                                                                                         "flag name")))))
                                                                      .setFirstName("Legally").setLastName("Rep")
                                                                      ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent2LRIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Legally Rep")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Civil - Organisation");

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

                caseData.setApplicant1OrgIndividuals(wrapElements(new PartyFlagStructure()
                                                                      .setFirstName("Org").setLastName("Ind")
                                                                      ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getApplicant1OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Org Ind")
                    .setDetails(List.of())
                    .setRoleOnCase("Mr. John Rambo");

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

                caseData.setApplicant1OrgIndividuals(wrapElements(orgIndividual.copy()
                                                                      .setFlags(new Flags()
                                                                                 .setPartyName("Org Person")
                                                                                 .setRoleOnCase("Mr. John Rambo")
                                                                                 .setDetails(wrapElements(List.of(
                                                                                     new FlagDetail().setName(
                                                                                         "flag name")))))
                                                                      .setFirstName("Org").setLastName("Ind")
                                                                      ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getApplicant1OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Org Ind")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Mr. John Rambo");

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

                caseData.setApplicant2OrgIndividuals(wrapElements(new PartyFlagStructure()
                                                                      .setFirstName("Org").setLastName("Ind")
                                                                      ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getApplicant2OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Org Ind")
                    .setDetails(List.of())
                    .setRoleOnCase("Mr. Jason Rambo");

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

                caseData.setApplicant2OrgIndividuals(wrapElements(orgIndividual.copy()
                                                                      .setFlags(new Flags()
                                                                                 .setPartyName("Org Person")
                                                                                 .setRoleOnCase("Mr. Jason Rambo")
                                                                                 .setDetails(wrapElements(List.of(
                                                                                     new FlagDetail().setName(
                                                                                         "flag name")))))
                                                                      .setFirstName("Org").setLastName("Ind")
                                                                      ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getApplicant2OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Org Ind")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Mr. Jason Rambo");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("app-2-org-ind-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent1OrgIndividualAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_ORG_INDIVIDUALS_ID).build())
                    .build();

                caseData.setRespondent1OrgIndividuals(wrapElements(new PartyFlagStructure()
                                                                       .setFirstName("Org").setLastName("Ind")
                                                                       ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent1OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Org Ind")
                    .setDetails(List.of())
                    .setRoleOnCase("Mr. Sole Trader");

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

                caseData.setRespondent1OrgIndividuals(wrapElements(orgIndividual.copy()
                                                                       .setFlags(new Flags()
                                                                                  .setPartyName("Org Person")
                                                                                  .setRoleOnCase("Mr. Sole Trader")
                                                                                  .setDetails(wrapElements(List.of(
                                                                                      new FlagDetail().setName(
                                                                                          "flag name")))))
                                                                       .setFirstName("Org").setLastName("Ind")
                                                                       ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent1OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Org Ind")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Mr. Sole Trader");

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

                caseData.setRespondent2OrgIndividuals(wrapElements(new PartyFlagStructure()
                                                                       .setFirstName("Org").setLastName("Ind")
                                                                       ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent2OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Org Ind")
                    .setDetails(List.of())
                    .setRoleOnCase("Mr. John Rambo");

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

                caseData.setRespondent2OrgIndividuals(wrapElements(lrIndividual.copy()
                                                                       .setFlags(new Flags()
                                                                                  .setPartyName("Org Person")
                                                                                  .setRoleOnCase("Mr. John Rambo")
                                                                                  .setDetails(wrapElements(List.of(
                                                                                      new FlagDetail().setName(
                                                                                          "flag name")))))
                                                                       .setFirstName("Org").setLastName("Ind")
                                                                       ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent2OrgIndividuals()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Org Ind")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Mr. John Rambo");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("res-2-org-ind-party-id");
            }
        }

        @Nested
        class Experts {

            @Test
            void shouldCreateFlag_whenClaimantExpertsAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_EXPERTS_ID).build())
                    .build();

                caseData.setApplicantExperts(wrapElements(new PartyFlagStructure()
                                                              .setFirstName("Ex").setLastName("Pert")
                                                              ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getApplicantExperts()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Ex Pert")
                    .setDetails(List.of())
                    .setRoleOnCase("Claimant solicitor expert");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenClaimantExpertNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addApplicant1ExpertsAndWitnesses()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_EXPERTS_ID).build())
                    .build();

                PartyFlagStructure newParty = unwrapElements(caseData.getApplicantExperts()).get(0);

                caseData.setApplicantExperts(wrapElements(newParty.copy()
                                                              .setFlags(new Flags()
                                                                         .setPartyName("Exxxx Pert")
                                                                         .setRoleOnCase("Claimant solicitor expert")
                                                                         .setDetails(wrapElements(List.of(
                                                                             new FlagDetail().setName("flag name")))))
                                                              .setFirstName("Ex").setLastName("Pert")
                                                              ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getApplicantExperts()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Ex Pert")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Claimant solicitor expert");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("app-1-expert-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent1ExpertsAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_EXPERTS_ID).build())
                    .build();

                caseData.setRespondent1Experts(wrapElements(new PartyFlagStructure()
                                                                .setFirstName("Ex").setLastName("Pert")
                                                                ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent1Experts()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Ex Pert")
                    .setDetails(List.of())
                    .setRoleOnCase("Defendant solicitor 1 expert");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenRespondent1ExpertNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addRespondent1ExpertsAndWitnesses()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_EXPERTS_ID).build())
                    .build();

                PartyFlagStructure newParty = unwrapElements(caseData.getRespondent1Experts()).get(0);

                caseData.setRespondent1Experts(wrapElements(newParty.copy()
                                                                .setFlags(new Flags()
                                                                           .setPartyName("Exxxx Pert")
                                                                           .setRoleOnCase("Defendant solicitor 1 expert")
                                                                           .setDetails(wrapElements(List.of(
                                                                               new FlagDetail().setName("flag name")))))
                                                                .setFirstName("Ex").setLastName("Pert")
                                                                ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent1Experts()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Ex Pert")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Defendant solicitor 1 expert");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("res-1-expert-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent2ExpertsAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_EXPERTS_ID).build())
                    .build();

                caseData.setRespondent2Experts(wrapElements(new PartyFlagStructure()
                                                                .setFirstName("Ex").setLastName("Pert")
                                                                ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent2Experts()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Ex Pert")
                    .setDetails(List.of())
                    .setRoleOnCase("Defendant solicitor 2 expert");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenRespondent2ExpertNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateApplicantRespondToDefenceAndProceed(ONE_V_TWO_TWO_LEGAL_REP)
                    .addRespondent2ExpertsAndWitnesses()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_EXPERTS_ID).build())
                    .build();

                PartyFlagStructure newParty = unwrapElements(caseData.getRespondent2Experts()).get(0);

                caseData.setRespondent2Experts(wrapElements(newParty.copy()
                                                                .setFlags(new Flags()
                                                                           .setPartyName("Exxxx Pert")
                                                                           .setRoleOnCase("Defendant solicitor 2 expert")
                                                                           .setDetails(wrapElements(List.of(
                                                                               new FlagDetail().setName("flag name")))))
                                                                .setFirstName("Ex").setLastName("Pert")
                                                                ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent2Experts()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Ex Pert")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Defendant solicitor 2 expert");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("res-2-expert-party-id");
            }
        }

        @Nested
        class Witnesses {

            @Test
            void shouldCreateFlag_whenClaimantWitnessAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_WITNESSES_ID).build())
                    .build();

                caseData.setApplicantWitnesses(wrapElements(new PartyFlagStructure()
                                                                .setFirstName("Wit").setLastName("Ness")
                                                                ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getApplicantWitnesses()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Wit Ness")
                    .setDetails(List.of())
                    .setRoleOnCase("Claimant solicitor witness");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenClaimantWitnessNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addApplicant1ExpertsAndWitnesses()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(CLAIMANT_ONE_WITNESSES_ID).build())
                    .build();

                PartyFlagStructure newParty = unwrapElements(caseData.getApplicantWitnesses()).get(0);

                caseData.setApplicantWitnesses(wrapElements(newParty.copy()
                                                                .setFlags(new Flags()
                                                                           .setPartyName("Wittyness")
                                                                           .setRoleOnCase("Claimant solicitor witness")
                                                                           .setDetails(wrapElements(List.of(
                                                                               new FlagDetail().setName("flag name")))))
                                                                .setFirstName("Wit").setLastName("Ness")
                                                                ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getApplicantWitnesses()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Wit Ness")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Claimant solicitor witness");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("app-1-witness-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent1WitnessAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_WITNESSES_ID).build())
                    .build();

                caseData.setRespondent1Witnesses(wrapElements(new PartyFlagStructure()
                                                                  .setFirstName("Wit").setLastName("Ness")
                                                                  ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent1Witnesses()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Wit Ness")
                    .setDetails(List.of())
                    .setRoleOnCase("Defendant solicitor 1 witness");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenRespondent1WitnessNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addRespondent1ExpertsAndWitnesses()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_ONE_WITNESSES_ID).build())
                    .build();

                PartyFlagStructure newParty = unwrapElements(caseData.getRespondent1Witnesses()).get(0);

                caseData.setRespondent1Witnesses(wrapElements(newParty.copy()
                                                                  .setFlags(new Flags()
                                                                             .setPartyName("Wittyness")
                                                                             .setRoleOnCase(
                                                                                 "Defendant solicitor 1 witness")
                                                                             .setDetails(wrapElements(List.of(
                                                                                 new FlagDetail().setName("flag name")))))
                                                                  .setFirstName("Wit").setLastName("Ness")
                                                                  ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent1Witnesses()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Wit Ness")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Defendant solicitor 1 witness");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("res-1-witness-party-id");
            }

            @Test
            void shouldCreateFlag_whenRespondent2WitnessAdded() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_WITNESSES_ID).build())
                    .build();

                caseData.setRespondent2Witnesses(wrapElements(new PartyFlagStructure()
                                                                  .setFirstName("Wit").setLastName("Ness")
                                                                  ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent2Witnesses()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Wit Ness")
                    .setDetails(List.of())
                    .setRoleOnCase("Defendant solicitor 2 witness");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isNotNull();
            }

            @Test
            void shouldUpdateFlagName_whenRespondent2WitnessNameUpdated() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateApplicantRespondToDefenceAndProceed(ONE_V_TWO_TWO_LEGAL_REP)
                    .addRespondent2ExpertsAndWitnesses()
                    .updateDetailsForm(UpdateDetailsForm.builder().partyChosenId(DEFENDANT_TWO_WITNESSES_ID).build())
                    .build();

                PartyFlagStructure newParty = unwrapElements(caseData.getRespondent2Witnesses()).get(0);

                caseData.setRespondent2Witnesses(wrapElements(newParty.copy()
                                                                  .setFlags(new Flags()
                                                                             .setPartyName("Wittyness")
                                                                             .setRoleOnCase(
                                                                                 "Defendant solicitor 2 witness")
                                                                             .setDetails(wrapElements(List.of(
                                                                                 new FlagDetail().setName("flag name")))))
                                                                  .setFirstName("Wit").setLastName("Ness")
                                                                  ));

                CaseFlagUtils.createOrUpdateFlags(caseData, organisationService);

                PartyFlagStructure individual = unwrapElements(caseData.getRespondent2Witnesses()).get(0);
                Flags actualFlags = individual.getFlags();
                Flags expectedFlags = new Flags().setPartyName("Wit Ness")
                    .setDetails(wrapElements(List.of(
                        new FlagDetail().setName("flag name"))))
                    .setRoleOnCase("Defendant solicitor 2 witness");

                assertThat(actualFlags).isEqualTo(expectedFlags);
                assertThat(individual.getPartyID()).isEqualTo("res-2-witness-party-id");
            }
        }
    }
}
