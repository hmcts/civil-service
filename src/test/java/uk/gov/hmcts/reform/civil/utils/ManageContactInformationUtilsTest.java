package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.Party.Type.ORGANISATION;
import static uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER;
import static uk.gov.hmcts.reform.civil.model.common.DynamicListElement.dynamicElementFromCode;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicantOptions2v1;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant2Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendantOptions1v2SameSolicitor;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.appendUserAndType;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapExpertsToUpdatePartyDetailsForm;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQExperts;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQWitnesses;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapWitnessesToUpdatePartyDetailsForm;

@SuppressWarnings("unchecked")
class ManageContactInformationUtilsTest {

    private static final String PARTY_ID = "party-id";
    private static MockedStatic partyIdMock;

    @BeforeAll
    static void setupSuite() {
        partyIdMock = mockStatic(PartyUtils.class, Mockito.CALLS_REAL_METHODS);
        partyIdMock.when(PartyUtils::createPartyId).thenReturn(PARTY_ID);
    }

    @AfterAll
    static void tearDown() {
        partyIdMock.reset();
        partyIdMock.close();
    }

    @Test
    void shouldAddCorrectOptions_forClaimant1AsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed()
            .addApplicant1ExpertsAndWitnesses().build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicant1Options(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedApplicant1Options(true, false));

        List<DynamicListElement> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addApplicant1Options(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedApplicant1Options(false, false));
    }

    @Test
    void shouldAddCorrectOptions_forClaimants2v1AsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .addApplicant2LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed()
            .multiPartyClaimTwoApplicants()
            .applicant2DQ()
            .addApplicant2ExpertsAndWitnesses()
            .addApplicant1ExpertsAndWitnesses().build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .addApplicant1LitigationFriend()
            .addApplicant2LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicantOptions2v1(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedApplicants2v1Options(true, false));

        List<DynamicListElement> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addApplicantOptions2v1(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedApplicants2v1Options(false, false));
    }

    @Test
    void shouldAddCorrectOptions_forClaimant1AsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicant1Options(true, true));
    }

    @Test
    void shouldAddCorrectOptions_forClaimant1OrganisationAsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(Party.builder()
                            .organisationName("Test Inc")
                            .type(ORGANISATION)
                            .build())
            .build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicant1OrgOptions(true, true));
    }

    @Test
    void shouldAddCorrectOptions_forClaimant1CompanyAsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(Party.builder()
                            .companyName("Test Inc")
                            .type(COMPANY)
                            .build())
            .build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicant1OrgOptions(true, true));
    }

    @Test
    void shouldAddCorrectOptions_forClaimants2v1AsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .addApplicant2LitigationFriend()
            .multiPartyClaimTwoApplicants()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicantOptions2v1(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicants2v1Options(true, true));
    }

    @Test
    void shouldAddCorrectOptions_forDefendant1AsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .addRespondent1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed()
            .addRespondent1ExpertsAndWitnesses()
            .build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .addRespondent1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addDefendant1Options(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedDefendant1Options(true, false));

        List<DynamicListElement> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addDefendant1Options(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedDefendant1Options(false, false));
    }

    @Test
    void shouldAddCorrectOptions_forDefendant1AsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addRespondent1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addDefendant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedDefendant1Options(true, true));
    }

    @Test
    void shouldAddCorrectOptions_forDefendant2AsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .addRespondent2(YES)
            .respondent2Represented(YES)
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateApplicantRespondToDefenceAndProceed(ONE_V_TWO_TWO_LEGAL_REP)
            .addRespondent2LitigationFriend()
            .addRespondent2ExpertsAndWitnesses()
            .build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .addRespondent2LitigationFriend()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent2Represented(YES)
            .addRespondent2LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addDefendant2Options(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedDefendant2Options(true, false));

        List<DynamicListElement> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addDefendant2Options(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedDefendant2Options(false, false));
    }

    @Test
    void shouldAddCorrectOptions_forDefendant2AsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addRespondent2(YES)
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent2Represented(YES)
            .addRespondent2LitigationFriend()
            .respondent2Responds(FULL_DEFENCE).build();

        List<DynamicListElement> options = new ArrayList<>();
        addDefendant2Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedDefendant2Options(true, true));
    }

    @Test
    void shouldAddCorrectOptions_forDefendants1v2SameSolicitorAsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateRespondentFullDefence_1v2_Resp1CounterClaimAndResp2FullDefence()
            .addRespondent1LitigationFriend()
            .addRespondent2LitigationFriend()
            .addRespondent2ExpertsAndWitnesses()
            .addRespondent1ExpertsAndWitnesses()
            .build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .addRespondent1LitigationFriend()
            .addRespondent2LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addDefendantOptions1v2SameSolicitor(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedDefendants1v2SameSolicitorOptions(true, false));

        List<DynamicListElement> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addDefendantOptions1v2SameSolicitor(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedDefendants1v2SameSolicitorOptions(false, false));
    }

    @Test
    void shouldAddCorrectOptions_forDefendant1v2SameSolicitorAsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addRespondent2(YES)
            .multiPartyClaimOneDefendantSolicitor()
            .addRespondent2LitigationFriend()
            .addRespondent1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addDefendantOptions1v2SameSolicitor(options, caseData, true);

        assertThat(options).isEqualTo(expectedDefendants1v2SameSolicitorOptions(true, true));
    }

    @Test
    void shouldMapExpertsToUpdatePartyDetailsForm() {
        Expert expert1 = Expert.builder().firstName("First").lastName("Name").partyID("id").eventAdded("event").build();
        Expert expert2 = Expert.builder().firstName("Second").lastName("expert").fieldOfExpertise("field")
            .phoneNumber("1").emailAddress("email").partyID("id2").build();
        Experts experts = Experts.builder().details(wrapElements(expert1, expert2)).build();
        UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name")
            .partyId("id").build();
        UpdatePartyDetailsForm party2 = UpdatePartyDetailsForm.builder().firstName("Second").lastName("expert")
            .fieldOfExpertise("field").phoneNumber("1").emailAddress("email").partyId("id2").build();

        assertThat(mapExpertsToUpdatePartyDetailsForm(experts)).isEqualTo(wrapElements(party, party2));
    }

    @Test
    void shouldMapExpertsToUpdatePartyDetailsForm_ifEmpty() {
        assertThat(mapExpertsToUpdatePartyDetailsForm(null)).isEqualTo(new ArrayList<>());
    }

    @Test
    void shouldMapWitnessesToUpdatePartyDetailsForm() {
        Witness witness1 = Witness.builder().firstName("First").lastName("Name").partyID("id").eventAdded("event").build();
        Witness witness2 = Witness.builder().firstName("Second").lastName("expert").reasonForWitness("reason")
            .phoneNumber("1").emailAddress("email").partyID("id2").build();
        Witnesses witnesses = Witnesses.builder().details(wrapElements(witness1, witness2)).build();
        UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name")
            .partyId("id").build();
        UpdatePartyDetailsForm party2 = UpdatePartyDetailsForm.builder().firstName("Second").lastName("expert")
            .phoneNumber("1").emailAddress("email").partyId("id2").build();

        assertThat(mapWitnessesToUpdatePartyDetailsForm(witnesses)).isEqualTo(wrapElements(party, party2));
    }

    @Test
    void shouldMapWitnessesToUpdatePartyDetailsForm_ifEmpty() {
        assertThat(mapWitnessesToUpdatePartyDetailsForm(null)).isEqualTo(new ArrayList<>());
    }

    @Nested
    class MapToDQExperts {
        UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("Lewis").lastName("John")
            .partyId("id").build();
        UpdatePartyDetailsForm party2 = UpdatePartyDetailsForm.builder().firstName("Second").lastName("expert")
            .fieldOfExpertise("field").phoneNumber("1").emailAddress("expertemail").partyId("id2").build();

        LocalDate date = LocalDate.of(2020, 3, 20);

        Expert expert1 = Expert.builder().firstName("First").lastName("Name").partyID("id").eventAdded("event")
            .dateAdded(date).estimatedCost(BigDecimal.valueOf(10000)).build();
        Expert expert2 = Expert.builder().firstName("Second").lastName("expert").fieldOfExpertise("field")
            .eventAdded("event").dateAdded(date).phoneNumber("1").emailAddress("email").partyID("id2").build();

        @Test
        void shouldEditExperts() {
            Expert expectedExpert1 = Expert.builder().firstName("Lewis").lastName("John").partyID("id")
                .eventAdded("event").dateAdded(date).estimatedCost(BigDecimal.valueOf(10000)).build();
            Expert expectedExpert2 = Expert.builder().firstName("Second").lastName("expert").fieldOfExpertise("field")
                .eventAdded("event").dateAdded(date).phoneNumber("1").emailAddress("expertemail").partyID("id2").build();

            assertThat(mapUpdatePartyDetailsFormToDQExperts(wrapElements(expert1, expert2), wrapElements(party, party2)))
                .isEqualTo(wrapElements(expectedExpert1, expectedExpert2));
        }

        @Test
        void shouldAddExperts() {
            Expert expectedExpert1 = Expert.builder().firstName("Lewis").lastName("John")
                .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now())
                .partyID(PARTY_ID)
                .build();
            Expert expectedExpert2 = Expert.builder().firstName("Second").lastName("expert").fieldOfExpertise("field")
                .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now()).phoneNumber("1")
                .emailAddress("expertemail")
                .partyID(PARTY_ID)
                .build();

            assertThat(mapUpdatePartyDetailsFormToDQExperts(null, wrapElements(party, party2)))
                .isEqualTo(wrapElements(expectedExpert1, expectedExpert2));
        }

        @Test
        void shouldAddExpertsWithExistingExperts() {
            Expert expectedExpert1 = Expert.builder().firstName("Lewis").lastName("John").partyID("id")
                .eventAdded("event").dateAdded(date).estimatedCost(BigDecimal.valueOf(10000)).build();
            Expert expectedExpert2 = Expert.builder().firstName("Second").lastName("expert").fieldOfExpertise("field")
                .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now()).phoneNumber("1")
                .emailAddress("expertemail")
                .partyID(PARTY_ID)
                .build();

            assertThat(mapUpdatePartyDetailsFormToDQExperts(wrapElements(expert1), wrapElements(party, party2)))
                .isEqualTo(wrapElements(expectedExpert1, expectedExpert2));
        }
    }

    @Nested
    class MapToDQWitnesses {
        UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("Lewis").lastName("John")
            .partyId("id").build();
        UpdatePartyDetailsForm party2 = UpdatePartyDetailsForm.builder().firstName("Second").lastName("witness")
            .phoneNumber("1").emailAddress("witnessemail").partyId("id2").build();

        LocalDate date = LocalDate.of(2020, 3, 20);

        Witness witness1 = Witness.builder().firstName("First").lastName("Name").partyID("id").eventAdded("event")
            .dateAdded(date).reasonForWitness("reason").build();
        Witness witness2 = Witness.builder().firstName("Second").lastName("expert").eventAdded("event")
            .dateAdded(date).phoneNumber("1").emailAddress("email").partyID("id2").build();

        @Test
        void shouldEditWitnesses() {
            Witness expectedWitness1 = Witness.builder().firstName("Lewis").lastName("John")
                .eventAdded("event").dateAdded(date).reasonForWitness("reason").partyID("id").build();

            Witness expectedWitness2 = Witness.builder().firstName("Second").lastName("witness")
                .eventAdded("event").dateAdded(date).phoneNumber("1").emailAddress("witnessemail")
                .partyID("id2").build();

            assertThat(mapUpdatePartyDetailsFormToDQWitnesses(wrapElements(witness1, witness2), wrapElements(party, party2)))
                .isEqualTo(wrapElements(expectedWitness1, expectedWitness2));
        }

        @Test
        void shouldAddWitnesses() {
            Witness expectedWitness1 = Witness.builder().firstName("Lewis").lastName("John")
                .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now())
                .partyID(PARTY_ID).build();
            Witness expectedWitness2 = Witness.builder().firstName("Second").lastName("witness")
                .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now()).phoneNumber("1")
                .emailAddress("witnessemail")
                .partyID(PARTY_ID).build();

            assertThat(mapUpdatePartyDetailsFormToDQWitnesses(null, wrapElements(party, party2)))
                .isEqualTo(wrapElements(expectedWitness1, expectedWitness2));
        }

        @Test
        void shouldRemoveWitnesses() {
            assertThat(mapUpdatePartyDetailsFormToDQWitnesses(wrapElements(witness1, witness2), null))
                .isEmpty();
        }

        @Test
        void shouldAddWitnessesWithExistingWitnesses() {
            Witness expectedWitness1 = Witness.builder().firstName("Lewis").lastName("John").partyID("id")
                .reasonForWitness("reason").eventAdded("event").dateAdded(date).build();
            Witness expectedWitness2 = Witness.builder().firstName("Second").lastName("witness")
                .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now()).phoneNumber("1")
                .emailAddress("witnessemail")
                .partyID(PARTY_ID)
                .build();

            assertThat(mapUpdatePartyDetailsFormToDQWitnesses(wrapElements(witness1), wrapElements(party, party2)))
                .isEqualTo(wrapElements(expectedWitness1, expectedWitness2));
        }
    }

    @Nested
    class AppendCorrectUserAndType {
        @Test
        void shouldHaveCorrectID_ClaimantOneAdminIndividual() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1(Party.builder().type(INDIVIDUAL).build()).build();

            String result = appendUserAndType(CLAIMANT_ONE_ID, caseData, true);

            assertThat(result).isEqualTo("CLAIMANT_1_ADMIN_INDIVIDUAL");
        }

        @Test
        void shouldHaveCorrectID_ClaimantTwoAdminSoleTrader() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant2(Party.builder().type(SOLE_TRADER).build()).build();

            String result = appendUserAndType(CLAIMANT_TWO_ID, caseData, true);

            assertThat(result).isEqualTo("CLAIMANT_2_ADMIN_SOLE_TRADER");
        }

        @Test
        void shouldHaveCorrectID_DefendantOneAdminOrganisation() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(Party.builder().type(ORGANISATION).build()).build();

            String result = appendUserAndType(DEFENDANT_ONE_ID, caseData, true);

            assertThat(result).isEqualTo("DEFENDANT_1_ADMIN_ORGANISATION");
        }

        @Test
        void shouldHaveCorrectID_DefendantTwoAdminCompany() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent2(Party.builder().type(COMPANY).build()).build();

            String result = appendUserAndType(DEFENDANT_TWO_ID, caseData, true);

            assertThat(result).isEqualTo("DEFENDANT_2_ADMIN_COMPANY");
        }

        @Test
        void shouldHaveCorrectID_DefendantTwoLegalRepIndividual() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent2(Party.builder().type(INDIVIDUAL).build()).build();

            String result = appendUserAndType(DEFENDANT_TWO_ID, caseData, false);

            assertThat(result).isEqualTo("DEFENDANT_2_LR_INDIVIDUAL");
        }

        @Test
        void shouldHaveCorrectID_Applicant1LitigationFriendAdmin() {
            CaseData caseData = CaseDataBuilder.builder().build();

            String result = appendUserAndType(CLAIMANT_ONE_LITIGATION_FRIEND_ID, caseData, false);

            assertThat(result).isEqualTo("CLAIMANT_1_LITIGATION_FRIEND_LR");
        }

        @Test
        void shouldHaveCorrectID_Defendant2LitigationFriendAdmin() {
            CaseData caseData = CaseDataBuilder.builder().build();

            String result = appendUserAndType(DEFENDANT_TWO_LITIGATION_FRIEND_ID, caseData, true);

            assertThat(result).isEqualTo("DEFENDANT_2_LITIGATION_FRIEND_ADMIN");
        }
    }

    private List<DynamicListElement> expectedApplicant1Options(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<DynamicListElement> list = new ArrayList<>();
        list.add(dynamicElementFromCode("CLAIMANT_1", "CLAIMANT 1: Mr. John Rambo"));
        list.add(dynamicElementFromCode("CLAIMANT_1_LITIGATION_FRIEND", "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend"));
        list.add(dynamicElementFromCode("CLAIMANT_1_LR_INDIVIDUALS", "CLAIMANT 1: Individuals attending for the legal representative"));
        if (withExpertsAndWitnesses || isAdmin) {
            list.add(dynamicElementFromCode("CLAIMANT_1_WITNESSES", "CLAIMANT 1: Witnesses"));
            list.add(dynamicElementFromCode("CLAIMANT_1_EXPERTS", "CLAIMANT 1: Experts"));
        }
        return list;
    }

    private List<DynamicListElement> expectedApplicant1OrgOptions(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<DynamicListElement> list = new ArrayList<>();
        list.add(dynamicElementFromCode("CLAIMANT_1", "CLAIMANT 1: Test Inc"));
        list.add(dynamicElementFromCode("CLAIMANT_1_ORGANISATION_INDIVIDUALS", "CLAIMANT 1: Individuals attending for the organisation"));
        list.add(dynamicElementFromCode("CLAIMANT_1_LR_INDIVIDUALS", "CLAIMANT 1: Individuals attending for the legal representative"));
        if (withExpertsAndWitnesses || isAdmin) {
            list.add(dynamicElementFromCode("CLAIMANT_1_WITNESSES", "CLAIMANT 1: Witnesses"));
            list.add(dynamicElementFromCode("CLAIMANT_1_EXPERTS", "CLAIMANT 1: Experts"));
        }
        return list;
    }

    private List<DynamicListElement> expectedApplicants2v1Options(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<DynamicListElement> list = new ArrayList<>();
        list.add(dynamicElementFromCode("CLAIMANT_1", "CLAIMANT 1: Mr. John Rambo"));
        list.add(dynamicElementFromCode("CLAIMANT_1_LITIGATION_FRIEND", "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend"));
        list.add(dynamicElementFromCode("CLAIMANT_2", "CLAIMANT 2: Mr. Jason Rambo"));
        list.add(dynamicElementFromCode("CLAIMANT_2_LITIGATION_FRIEND", "CLAIMANT 2: Litigation Friend: Applicant Two Litigation Friend"));
        list.add(dynamicElementFromCode("CLAIMANT_1_LR_INDIVIDUALS", "CLAIMANTS: Individuals attending for the legal representative"));
        if (withExpertsAndWitnesses || isAdmin) {
            list.add(dynamicElementFromCode("CLAIMANT_1_WITNESSES", "CLAIMANTS: Witnesses"));
            list.add(dynamicElementFromCode("CLAIMANT_1_EXPERTS", "CLAIMANTS: Experts"));
        }
        return list;
    }

    private List<DynamicListElement> expectedDefendant1Options(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<DynamicListElement> list = new ArrayList<>();
        list.add(dynamicElementFromCode("DEFENDANT_1", "DEFENDANT 1: Mr. Sole Trader"));
        list.add(dynamicElementFromCode("DEFENDANT_1_LITIGATION_FRIEND", "DEFENDANT 1: Litigation Friend: Litigation Friend"));
        list.add(dynamicElementFromCode("DEFENDANT_1_LR_INDIVIDUALS", "DEFENDANT 1: Individuals attending for the legal representative"));
        if (withExpertsAndWitnesses || isAdmin) {
            list.add(dynamicElementFromCode("DEFENDANT_1_WITNESSES", "DEFENDANT 1: Witnesses"));
            list.add(dynamicElementFromCode("DEFENDANT_1_EXPERTS", "DEFENDANT 1: Experts"));
        }
        return list;
    }

    private List<DynamicListElement> expectedDefendant2Options(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<DynamicListElement> list = new ArrayList<>();
        list.add(dynamicElementFromCode("DEFENDANT_2", "DEFENDANT 2: Mr. John Rambo"));
        list.add(dynamicElementFromCode("DEFENDANT_2_LITIGATION_FRIEND", "DEFENDANT 2: Litigation Friend: Litigation Friend"));
        list.add(dynamicElementFromCode("DEFENDANT_2_LR_INDIVIDUALS", "DEFENDANT 2: Individuals attending for the legal representative"));
        if (withExpertsAndWitnesses || isAdmin) {
            list.add(dynamicElementFromCode("DEFENDANT_2_WITNESSES", "DEFENDANT 2: Witnesses"));
            list.add(dynamicElementFromCode("DEFENDANT_2_EXPERTS", "DEFENDANT 2: Experts"));
        }
        return list;
    }

    private List<DynamicListElement> expectedDefendants1v2SameSolicitorOptions(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<DynamicListElement> list = new ArrayList<>();
        list.add(dynamicElementFromCode("DEFENDANT_1", "DEFENDANT 1: Mr. Sole Trader"));
        list.add(dynamicElementFromCode("DEFENDANT_1_LITIGATION_FRIEND", "DEFENDANT 1: Litigation Friend: Litigation Friend"));
        list.add(dynamicElementFromCode("DEFENDANT_2", "DEFENDANT 2: Mr. John Rambo"));
        list.add(dynamicElementFromCode("DEFENDANT_2_LITIGATION_FRIEND", "DEFENDANT 2: Litigation Friend: Litigation Friend"));
        list.add(dynamicElementFromCode("DEFENDANT_1_LR_INDIVIDUALS", "DEFENDANTS: Individuals attending for the legal representative"));
        if (withExpertsAndWitnesses || isAdmin) {
            list.add(dynamicElementFromCode("DEFENDANT_1_WITNESSES", "DEFENDANTS: Witnesses"));
            list.add(dynamicElementFromCode("DEFENDANT_1_EXPERTS", "DEFENDANTS: Experts"));
        }
        return list;
    }
}
