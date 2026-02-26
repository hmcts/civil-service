package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.Party.Type.ORGANISATION;
import static uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER;
import static uk.gov.hmcts.reform.civil.model.common.DynamicListElement.dynamicElementFromCode;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicantOptions2v1;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant2Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendantOptions1v2SameSolicitor;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.appendUserAndType;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapExpertsToUpdatePartyDetailsForm;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapFormDataToIndividualsData;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapPartyFieldsToPartyFormData;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQExperts;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQWitnesses;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapWitnessesToUpdatePartyDetailsForm;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.prepareLRIndividuals;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.prepareOrgIndividuals;

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
            .applicant1Represented(YES)
            .atStateApplicantRespondToDefenceAndProceed()
            .addApplicant1ExpertsAndWitnesses().build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .applicant1Represented(YES)
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicant1Options(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedApplicant1Options(true, false, false));

        List<DynamicListElement> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addApplicant1Options(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedApplicant1Options(false, false, false));
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
            .applicant1Represented(YES)
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicant1Options(true, true, false));
    }

    @Test
    void shouldAddCorrectOptions_forClaimant1OrganisationAsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .applicant1Represented(YES)
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(Party.builder()
                            .organisationName("Test Inc")
                            .type(ORGANISATION)
                            .build())
            .build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicant1OrgOptions(true, true, false));
    }

    @Test
    void shouldAddCorrectOptions_forClaimant1LipAsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
                .addApplicant1LitigationFriend()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1Represented(NO)
                .applicant1(Party.builder()
                        .organisationName("Test Inc")
                        .type(ORGANISATION)
                        .build())
                .build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicant1OrgOptions(true, true, true));
    }

    @Test
    void shouldAddCorrectOptions_forClaimant1CompanyAsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .applicant1Represented(YES)
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(Party.builder()
                            .companyName("Test Inc")
                            .type(COMPANY)
                            .build())
            .build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicant1OrgOptions(true, true, false));
    }

    @Test
    void shouldAddCorrectOptions_forClaimant1LipCompanyAsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
                .addApplicant1LitigationFriend()
                .applicant1Represented(NO)
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1(Party.builder()
                        .companyName("Test Inc")
                        .type(COMPANY)
                        .build())
                .build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicant1OrgOptions(true, true, true));
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
    void shouldAddCorrectOptions_forClaimants2v1AsAdmin_secondClaimantAsCompany() {
        CaseData caseData = CaseDataBuilder.builder()
                .addApplicant1LitigationFriend()
                .addApplicant2LitigationFriend()
                .multiPartyClaimTwoApplicants()
                .applicant2(PartyBuilder.builder().company().build())
                .atStateApplicantRespondToDefenceAndProceed().build();

        List<DynamicListElement> options = new ArrayList<>();
        addApplicantOptions2v1(options, caseData, true);

        List<DynamicListElement> expected = new ArrayList<>();
        expected.add(dynamicElementFromCode("CLAIMANT_1", "CLAIMANT 1: Mr. John Rambo"));
        expected.add(dynamicElementFromCode("CLAIMANT_1_LITIGATION_FRIEND", "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend"));
        expected.add(dynamicElementFromCode("CLAIMANT_2", "CLAIMANT 2: Company ltd"));
        expected.add(dynamicElementFromCode("CLAIMANT_2_ORGANISATION_INDIVIDUALS", "CLAIMANT 2: Individuals attending for the organisation"));
        expected.add(dynamicElementFromCode("CLAIMANT_1_LR_INDIVIDUALS", "CLAIMANTS: Individuals attending for the legal representative"));
        expected.add(dynamicElementFromCode("CLAIMANT_1_WITNESSES", "CLAIMANTS: Witnesses"));
        expected.add(dynamicElementFromCode("CLAIMANT_1_EXPERTS", "CLAIMANTS: Experts"));

        assertThat(options).isEqualTo(expected);
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
        Expert expert1 = new Expert().setFirstName("First").setLastName("Name").setPartyID("id").setEventAdded("event");
        Expert expert2 = new Expert().setFirstName("Second").setLastName("expert").setFieldOfExpertise("field")
            .setPhoneNumber("1").setEmailAddress("email").setPartyID("id2");
        Experts experts = new Experts().setDetails(wrapElements(expert1, expert2));
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
        Witness witness1 = new Witness().setFirstName("First").setLastName("Name").setPartyID("id").setEventAdded("event");
        Witness witness2 = new Witness().setFirstName("Second").setLastName("expert").setReasonForWitness("reason")
            .setPhoneNumber("1").setEmailAddress("email").setPartyID("id2");
        Witnesses witnesses = new Witnesses().setDetails(wrapElements(witness1, witness2));
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

        Expert expert1 = new Expert().setFirstName("First").setLastName("Name").setPartyID("id").setEventAdded("event")
            .setDateAdded(date).setEstimatedCost(BigDecimal.valueOf(10000));
        Expert expert2 = new Expert().setFirstName("Second").setLastName("expert").setFieldOfExpertise("field")
            .setEventAdded("event").setDateAdded(date).setPhoneNumber("1").setEmailAddress("email").setPartyID("id2");

        @Test
        void shouldEditExperts() {
            Expert expectedExpert1 = new Expert().setFirstName("Lewis").setLastName("John").setPartyID("id")
                .setEventAdded("event").setDateAdded(date).setEstimatedCost(BigDecimal.valueOf(10000));
            Expert expectedExpert2 = new Expert().setFirstName("Second").setLastName("expert").setFieldOfExpertise("field")
                .setEventAdded("event").setDateAdded(date).setPhoneNumber("1").setEmailAddress("expertemail").setPartyID("id2");
            Experts experts = new Experts().setDetails(wrapElements(expert1, expert2));

            assertThat(mapUpdatePartyDetailsFormToDQExperts(experts, wrapElements(party, party2)))
                .isEqualTo(wrapElements(expectedExpert1, expectedExpert2));
        }

        @Test
        void shouldAddExperts() {
            Expert expectedExpert1 = new Expert().setFirstName("Lewis").setLastName("John")
                .setEventAdded("Manage Contact Information Event").setDateAdded(LocalDate.now())
                .setPartyID(PARTY_ID);
            Expert expectedExpert2 = new Expert().setFirstName("Second").setLastName("expert").setFieldOfExpertise("field")
                .setEventAdded("Manage Contact Information Event").setDateAdded(LocalDate.now()).setPhoneNumber("1")
                .setEmailAddress("expertemail")
                .setPartyID(PARTY_ID);

            assertThat(mapUpdatePartyDetailsFormToDQExperts(null, wrapElements(party, party2)))
                .isEqualTo(wrapElements(expectedExpert1, expectedExpert2));
        }

        @Test
        void shouldAddExpertsWithExistingExperts() {
            Expert expectedExpert1 = new Expert().setFirstName("Lewis").setLastName("John").setPartyID("id")
                .setEventAdded("event").setDateAdded(date).setEstimatedCost(BigDecimal.valueOf(10000));
            Expert expectedExpert2 = new Expert().setFirstName("Second").setLastName("expert").setFieldOfExpertise("field")
                .setEventAdded("Manage Contact Information Event").setDateAdded(LocalDate.now()).setPhoneNumber("1")
                .setEmailAddress("expertemail")
                .setPartyID(PARTY_ID);

            Experts experts = new Experts().setDetails(wrapElements(expert1));

            assertThat(mapUpdatePartyDetailsFormToDQExperts(experts, wrapElements(party, party2)))
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

        Witness witness1 = new Witness().setFirstName("First").setLastName("Name").setPartyID("id").setEventAdded("event")
            .setDateAdded(date).setReasonForWitness("reason");
        Witness witness2 = new Witness().setFirstName("Second").setLastName("expert").setEventAdded("event")
            .setDateAdded(date).setPhoneNumber("1").setEmailAddress("email").setPartyID("id2");

        Witnesses witnesses = new Witnesses().setDetails(wrapElements(witness1, witness2));

        @Test
        void shouldEditWitnesses() {
            Witness expectedWitness1 = new Witness().setFirstName("Lewis").setLastName("John")
                .setEventAdded("event").setDateAdded(date).setReasonForWitness("reason").setPartyID("id");

            Witness expectedWitness2 = new Witness().setFirstName("Second").setLastName("witness")
                .setEventAdded("event").setDateAdded(date).setPhoneNumber("1").setEmailAddress("witnessemail")
                .setPartyID("id2");

            assertThat(mapUpdatePartyDetailsFormToDQWitnesses(witnesses, wrapElements(party, party2)))
                .isEqualTo(wrapElements(expectedWitness1, expectedWitness2));
        }

        @Test
        void shouldAddWitnesses() {
            Witness expectedWitness1 = new Witness().setFirstName("Lewis").setLastName("John")
                .setEventAdded("Manage Contact Information Event").setDateAdded(LocalDate.now())
                .setPartyID(PARTY_ID);
            Witness expectedWitness2 = new Witness().setFirstName("Second").setLastName("witness")
                .setEventAdded("Manage Contact Information Event").setDateAdded(LocalDate.now()).setPhoneNumber("1")
                .setEmailAddress("witnessemail")
                .setPartyID(PARTY_ID);

            assertThat(mapUpdatePartyDetailsFormToDQWitnesses(null, wrapElements(party, party2)))
                .isEqualTo(wrapElements(expectedWitness1, expectedWitness2));
        }

        @Test
        void shouldRemoveWitnesses() {
            assertThat(mapUpdatePartyDetailsFormToDQWitnesses(witnesses, null))
                .isEmpty();
        }

        @Test
        void shouldAddWitnessesWithExistingWitnesses() {
            Witness expectedWitness1 = new Witness().setFirstName("Lewis").setLastName("John").setPartyID("id")
                .setReasonForWitness("reason").setEventAdded("event").setDateAdded(date);
            Witness expectedWitness2 = new Witness().setFirstName("Second").setLastName("witness")
                .setEventAdded("Manage Contact Information Event").setDateAdded(LocalDate.now()).setPhoneNumber("1")
                .setEmailAddress("witnessemail")
                .setPartyID(PARTY_ID);

            Witnesses witnesses = new Witnesses().setDetails(wrapElements(witness1));

            assertThat(mapUpdatePartyDetailsFormToDQWitnesses(witnesses, wrapElements(party, party2)))
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

    @Nested
    class MapPartyFieldsToPartyFormData {

        @Test
        void shouldReturnExpectedPartyDetailsFormData_whenDataIsDefined() {
            var partyData = createParty("claimant-1-lr-individual");

            List<Element<UpdatePartyDetailsForm>> actual = mapPartyFieldsToPartyFormData(wrapElements(partyData));

            assertThat(unwrapElements(actual)).isEqualTo(List.of(
                UpdatePartyDetailsForm.builder()
                    .firstName(partyData.getFirstName())
                    .lastName(partyData.getLastName())
                    .phoneNumber(partyData.getPhone())
                    .emailAddress(partyData.getEmail())
                    .build()));
        }

        @Test
        void shouldReturnEmptyList_whenDataIsNull() {
            List<Element<UpdatePartyDetailsForm>> actual = mapPartyFieldsToPartyFormData(null);
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class MapFormDataToIndividualsData {
        @Test
        void shouldReturnExpectedPartyDetailsList_forGivenData_whenExistingListIsDefined() {
            PartyFlagStructure existing = createParty("claimant-1-lr-individual");

            UpdatePartyDetailsForm updated = UpdatePartyDetailsForm.builder()
                .firstName("NewClaimantName")
                .lastName(existing.getLastName())
                .phoneNumber(existing.getPhone())
                .emailAddress(existing.getEmail())
                .build();

            UUID uuid = UUID.randomUUID();
            List<Element<PartyFlagStructure>> existingList = List.of(Element.<PartyFlagStructure>builder().id(uuid).value(existing).build());
            List<Element<UpdatePartyDetailsForm>>  updatedList = List.of(Element.<UpdatePartyDetailsForm>builder().id(uuid).value(updated).build());

            List<Element<PartyFlagStructure>> actual = mapFormDataToIndividualsData(existingList, updatedList);

            List<PartyFlagStructure>  expected = List.of(existing.copy().setFirstName("NewClaimantName"));
            assertThat(unwrapElements(actual)).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedPartyDetailsList_forGivenData_whenExistingListIsEmpty() {

            UpdatePartyDetailsForm updated = UpdatePartyDetailsForm.builder()
                .firstName("NewClaimantName")
                .lastName("LastName")
                .phoneNumber("09876565432")
                .emailAddress("some-email@example.com")
                .build();

            UUID uuid = UUID.randomUUID();

            List<Element<PartyFlagStructure>> existingList = List.of();
            List<Element<UpdatePartyDetailsForm>>  updatedList = List.of(Element.<UpdatePartyDetailsForm>builder().id(uuid).value(updated).build());

            List<Element<PartyFlagStructure>> actual = mapFormDataToIndividualsData(existingList, updatedList);

            List<PartyFlagStructure> expected = List.of(
                new PartyFlagStructure()
                    .setFirstName(updated.getFirstName())
                    .setLastName(updated.getLastName())
                    .setPhone(updated.getPhoneNumber())
                    .setEmail(updated.getEmailAddress())
                    
            );

            assertThat(unwrapElements(actual)).isEqualTo(expected);
        }
    }

    @Nested
    class PrepareOrgIndividuals {

        @Test
        void shouldPopulateExpectedOrgIndividuals_applicant1OrgIndividuals() {
            PartyFlagStructure partyDetails1 = createParty("claimant-1-org-individual-1");
            PartyFlagStructure partyDetails2 = createParty("claimant-1-org-individual-2");

            CaseData caseData = CaseData.builder()
                .applicant1OrgIndividuals(wrapElements(partyDetails1, partyDetails2))
                .applicant2OrgIndividuals(wrapElements(createParty("claimant-2-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareOrgIndividuals(
                CLAIMANT_ONE_ORG_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEqualTo(
                wrapElements(
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails1.getFirstName())
                        .lastName(partyDetails1.getLastName())
                        .emailAddress(partyDetails1.getEmail())
                        .phoneNumber(partyDetails1.getPhone())
                        .build(),
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails2.getFirstName())
                        .lastName(partyDetails2.getLastName())
                        .emailAddress(partyDetails2.getEmail())
                        .phoneNumber(partyDetails2.getPhone())
                        .build()
                )
            );
        }

        @Test
        void shouldReturnEmptyList_applicant1OrgIndividualsNull() {
            CaseData caseData = CaseData.builder()
                .applicant2OrgIndividuals(wrapElements(createParty("claimant-2-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareOrgIndividuals(
                CLAIMANT_ONE_ORG_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEmpty();
        }

        @Test
        void shouldPopulateExpectedOrgIndividuals_applicant2OrgIndividuals() {
            PartyFlagStructure partyDetails1 = createParty("claimant-2-org-individual-1");
            PartyFlagStructure partyDetails2 = createParty("claimant-2-org-individual-2");

            CaseData caseData = CaseData.builder()
                .applicant2OrgIndividuals(wrapElements(partyDetails1, partyDetails2))
                .applicant1OrgIndividuals(wrapElements(createParty("claimant-1-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareOrgIndividuals(
                CLAIMANT_TWO_ORG_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEqualTo(
                wrapElements(
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails1.getFirstName())
                        .lastName(partyDetails1.getLastName())
                        .emailAddress(partyDetails1.getEmail())
                        .phoneNumber(partyDetails1.getPhone())
                        .build(),
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails2.getFirstName())
                        .lastName(partyDetails2.getLastName())
                        .emailAddress(partyDetails2.getEmail())
                        .phoneNumber(partyDetails2.getPhone())
                        .build()
                )
            );
        }

        @Test
        void shouldReturnEmptyList_applicant2OrgIndividualsNull() {
            CaseData caseData = CaseData.builder()
                .applicant1OrgIndividuals(wrapElements(createParty("claimant-1-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareOrgIndividuals(
                CLAIMANT_TWO_ORG_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEmpty();
        }

        @Test
        void shouldPopulateExpectedOrgIndividuals_respondent1OrgIndividuals() {
            PartyFlagStructure partyDetails1 = createParty("defendant-1-org-individual-1");
            PartyFlagStructure partyDetails2 = createParty("defendant-1-org-individual-2");

            CaseData caseData = CaseData.builder()
                .respondent1OrgIndividuals(wrapElements(partyDetails1, partyDetails2))
                .respondent2OrgIndividuals(wrapElements(createParty("defendant-1-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareOrgIndividuals(
                DEFENDANT_ONE_ORG_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEqualTo(
                wrapElements(
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails1.getFirstName())
                        .lastName(partyDetails1.getLastName())
                        .emailAddress(partyDetails1.getEmail())
                        .phoneNumber(partyDetails1.getPhone())
                        .build(),
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails2.getFirstName())
                        .lastName(partyDetails2.getLastName())
                        .emailAddress(partyDetails2.getEmail())
                        .phoneNumber(partyDetails2.getPhone())
                        .build()
                )
            );
        }

        @Test
        void shouldReturnEmprtList_respondent1OrgIndividualsNull() {
            CaseData caseData = CaseData.builder()
                .respondent2OrgIndividuals(wrapElements(createParty("defendant-2-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareOrgIndividuals(
                DEFENDANT_ONE_ORG_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEmpty();
        }

        @Test
        void shouldPopulateExpectedOrgIndividuals_respondent2OrgIndividuals() {
            PartyFlagStructure partyDetails1 = createParty("defendant-2-org-individual-1");
            PartyFlagStructure partyDetails2 = createParty("defendant-2-org-individual-2");

            CaseData caseData = CaseData.builder()
                .respondent2OrgIndividuals(wrapElements(partyDetails1, partyDetails2))
                .respondent1OrgIndividuals(wrapElements(createParty("defendant-1-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareOrgIndividuals(
                DEFENDANT_TWO_ORG_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEqualTo(
                wrapElements(
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails1.getFirstName())
                        .lastName(partyDetails1.getLastName())
                        .emailAddress(partyDetails1.getEmail())
                        .phoneNumber(partyDetails1.getPhone())
                        .build(),
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails2.getFirstName())
                        .lastName(partyDetails2.getLastName())
                        .emailAddress(partyDetails2.getEmail())
                        .phoneNumber(partyDetails2.getPhone())
                        .build()
                )
            );
        }

        @Test
        void shouldReturnEmprtList_respondent2OrgIndividualsNull() {
            CaseData caseData = CaseData.builder()
                .respondent1OrgIndividuals(wrapElements(createParty("defendant-1-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareOrgIndividuals(
                DEFENDANT_TWO_ORG_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class PrepareLRIndividuals {

        @Test
        void shouldPopulateExpectedLRIndividuals_applicant1LRIndividuals() {
            PartyFlagStructure partyDetails1 = createParty("claimant-1-lr-individual-1");
            PartyFlagStructure partyDetails2 = createParty("claimant-1-lr-individual-2");

            CaseData caseData = CaseData.builder()
                .applicant1LRIndividuals(wrapElements(partyDetails1, partyDetails2))
                .respondent1LRIndividuals(wrapElements(createParty("defendant-1-lr-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareLRIndividuals(
                CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEqualTo(
                wrapElements(
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails1.getFirstName())
                        .lastName(partyDetails1.getLastName())
                        .emailAddress(partyDetails1.getEmail())
                        .phoneNumber(partyDetails1.getPhone())
                        .build(),
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails2.getFirstName())
                        .lastName(partyDetails2.getLastName())
                        .emailAddress(partyDetails2.getEmail())
                        .phoneNumber(partyDetails2.getPhone())
                        .build()
                )
            );
        }

        @Test
        void shouldReturnEmptyList_applicant1LRIndividualsNull() {
            CaseData caseData = CaseData.builder()
                .respondent1LRIndividuals(wrapElements(createParty("defendant-1-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareLRIndividuals(
                CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEmpty();
        }

        @Test
        void shouldPopulateExpectedLRIndividuals_respondent1LRIndividuals() {
            PartyFlagStructure partyDetails1 = createParty("defendant-1-lr-individual-1");
            PartyFlagStructure partyDetails2 = createParty("defendant-1-lr-individual-2");

            CaseData caseData = CaseData.builder()
                .respondent1LRIndividuals(wrapElements(partyDetails1, partyDetails2))
                .respondent2LRIndividuals(wrapElements(createParty("defendant-2-lr-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareLRIndividuals(
                DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEqualTo(
                wrapElements(
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails1.getFirstName())
                        .lastName(partyDetails1.getLastName())
                        .emailAddress(partyDetails1.getEmail())
                        .phoneNumber(partyDetails1.getPhone())
                        .build(),
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails2.getFirstName())
                        .lastName(partyDetails2.getLastName())
                        .emailAddress(partyDetails2.getEmail())
                        .phoneNumber(partyDetails2.getPhone())
                        .build()
                )
            );
        }

        @Test
        void shouldReturnEmptyList_respondent1LRIndividualsNull() {
            CaseData caseData = CaseData.builder()
                .respondent2LRIndividuals(wrapElements(createParty("defendant-2-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareLRIndividuals(
                DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEmpty();
        }

        @Test
        void shouldPopulateExpectedLRIndividuals_respondent2LRIndividuals() {
            PartyFlagStructure partyDetails1 = createParty("defendant-2-lr-individual-1");
            PartyFlagStructure partyDetails2 = createParty("defendant-2-lr-individual-2");

            CaseData caseData = CaseData.builder()
                .respondent2LRIndividuals(wrapElements(partyDetails1, partyDetails2))
                .respondent1LRIndividuals(wrapElements(createParty("defendant-1-lr-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareLRIndividuals(
                DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEqualTo(
                wrapElements(
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails1.getFirstName())
                        .lastName(partyDetails1.getLastName())
                        .emailAddress(partyDetails1.getEmail())
                        .phoneNumber(partyDetails1.getPhone())
                        .build(),
                    UpdatePartyDetailsForm.builder()
                        .firstName(partyDetails2.getFirstName())
                        .lastName(partyDetails2.getLastName())
                        .emailAddress(partyDetails2.getEmail())
                        .phoneNumber(partyDetails2.getPhone())
                        .build()
                )
            );
        }

        @Test
        void shouldReturnEmptyList_respondent2LRIndividualsNull() {
            CaseData caseData = CaseData.builder()
                .respondent1LRIndividuals(wrapElements(createParty("defendant-1-org-individual")))
                .build();

            List<Element<UpdatePartyDetailsForm>> actual = prepareLRIndividuals(
                DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID,
                caseData
            );

            assertThat(actual).isEmpty();
        }
    }

    private List<DynamicListElement> expectedApplicant1Options(boolean withExpertsAndWitnesses, boolean isAdmin, boolean isLip) {
        List<DynamicListElement> list = new ArrayList<>();
        list.add(dynamicElementFromCode("CLAIMANT_1", "CLAIMANT 1: Mr. John Rambo"));
        list.add(dynamicElementFromCode("CLAIMANT_1_LITIGATION_FRIEND", "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend"));
        if (!isLip) {
            list.add(dynamicElementFromCode("CLAIMANT_1_LR_INDIVIDUALS", "CLAIMANT 1: Individuals attending for the legal representative"));
        }
        if (withExpertsAndWitnesses || isAdmin) {
            list.add(dynamicElementFromCode("CLAIMANT_1_WITNESSES", "CLAIMANT 1: Witnesses"));
            list.add(dynamicElementFromCode("CLAIMANT_1_EXPERTS", "CLAIMANT 1: Experts"));
        }
        return list;
    }

    private List<DynamicListElement> expectedApplicant1OrgOptions(boolean withExpertsAndWitnesses, boolean isAdmin, boolean isLip) {
        List<DynamicListElement> list = new ArrayList<>();
        list.add(dynamicElementFromCode("CLAIMANT_1", "CLAIMANT 1: Test Inc"));
        list.add(dynamicElementFromCode("CLAIMANT_1_ORGANISATION_INDIVIDUALS", "CLAIMANT 1: Individuals attending for the organisation"));
        if (!isLip) {
            list.add(dynamicElementFromCode("CLAIMANT_1_LR_INDIVIDUALS", "CLAIMANT 1: Individuals attending for the legal representative"));
        }
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

    private PartyFlagStructure createParty(String prefix) {
        return new PartyFlagStructure()
            .setPartyID(prefix + "-id")
            .setFirstName(prefix + "-firstname")
            .setLastName(prefix + "-lastname")
            .setEmail(prefix + "-individual@example.com")
            .setPhone(prefix + "-07867654543")
            .setFlags(new Flags()
                       .setRoleOnCase(prefix + "-role"))
            ;
    }
}
