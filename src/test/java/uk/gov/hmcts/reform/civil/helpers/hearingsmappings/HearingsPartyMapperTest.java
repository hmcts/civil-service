package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.hearingvalues.IndividualDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.OrganisationDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PartyDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.RelatedPartiesModel;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.IND;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.ORG;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingsPartyMapper.buildPartyObjectForHearingPayload;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.ORGANISATION;

@ExtendWith(SpringExtension.class)
public class HearingsPartyMapperTest {

    private static final String CLAIMANT_ROLE = "CLAI";
    private static final String DEFENDANT_ROLE = "DEFE";
    private static final String LITIGATION_FRIEND_ROLE = "LIFR";
    private static final String LEGAL_REP_ROLE = "LGRP";
    private static final String EXPERT_ROLE = "EXPR";
    private static final String WITNESS_ROLE = "WITN";

    private static final String APPLICANT_ORG_ID = "QWERTY A";
    private static final String RESPONDENT_ONE_ORG_ID = "QWERTY R";
    private static final String RESPONDENT_TWO_ORG_ID = "QWERTY R2";

    private static final String APPLICANT_COMPANY_NAME = "Applicant Company";
    private static final String RESPONDENT_ONE_ORG_NAME = "Respondent 1 Organisation";

    private static final String APPLICANT_LR_ORG_NAME = "Applicant LR Org name";
    private static final String RESPONDENT_ONE_LR_ORG_NAME = "Respondent 1 LR Org name";
    private static final String RESPONDENT_TWO_LR_ORG_NAME = "Respondent LR 2 Org name";

    @Mock
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        when(organisationService.findOrganisationById(APPLICANT_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(APPLICANT_LR_ORG_NAME)
                                        .build()));
        when(organisationService.findOrganisationById(RESPONDENT_ONE_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT_ONE_LR_ORG_NAME)
                                        .build()));
        when(organisationService.findOrganisationById(RESPONDENT_TWO_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT_TWO_LR_ORG_NAME)
                                        .build()));
    }

    @Test
    void shouldBuildIndividualDetails_whenClaimantIsIndividualRespondentSoleTrader() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .build();

        PartyDetailsModel applicantPartyDetails = buildExpectedIndividualPartyDetails(
            "John",
            "Rambo",
            "Mr. John Rambo",
            CLAIMANT_ROLE,
            "rambo@email.com",
            "0123456789"
        );

        PartyDetailsModel applicantSolicitorParty = buildExpectedOrganisationPartyObject(
            APPLICANT_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            APPLICANT_ORG_ID
        );

        PartyDetailsModel respondentPartyDetails = buildExpectedIndividualPartyDetails(
            "Sole",
            "Trader",
            "Mr. Sole Trader",
            DEFENDANT_ROLE,
            "sole.trader@email.com",
            "0123456789"
        );

        PartyDetailsModel respondentSolicitorParty = buildExpectedOrganisationPartyObject(
            RESPONDENT_ONE_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            RESPONDENT_ONE_ORG_ID
        );

        List<PartyDetailsModel> expected = new ArrayList<>();
        expected.add(applicantPartyDetails);
        expected.add(applicantSolicitorParty);
        expected.add(respondentPartyDetails);
        expected.add(respondentSolicitorParty);

        List<PartyDetailsModel> actualPartyDetailsModel = buildPartyObjectForHearingPayload(
            caseData,
            organisationService
        );
        assertThat(actualPartyDetailsModel).isEqualTo(expected);
    }

    @Test
    void shouldBuildOrganisationDetails_whenClaimantIsCompanyRespondentOrganisation() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .build()
            .toBuilder()
            .applicant1(Party.builder()
                            .companyName(APPLICANT_COMPANY_NAME)
                            .type(COMPANY)
                            .build())
            .respondent1(Party.builder()
                             .organisationName(RESPONDENT_ONE_ORG_NAME)
                             .type(ORGANISATION)
                             .build())
            .build();

        PartyDetailsModel applicantPartyDetails = buildExpectedOrganisationPartyObject(
            APPLICANT_COMPANY_NAME,
            CLAIMANT_ROLE,
            null
        );

        PartyDetailsModel applicantSolicitorParty = buildExpectedOrganisationPartyObject(
            APPLICANT_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            APPLICANT_ORG_ID
        );

        PartyDetailsModel respondentPartyDetails = buildExpectedOrganisationPartyObject(
            RESPONDENT_ONE_ORG_NAME,
            DEFENDANT_ROLE,
            null
        );

        PartyDetailsModel respondentSolicitorParty = buildExpectedOrganisationPartyObject(
            RESPONDENT_ONE_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            RESPONDENT_ONE_ORG_ID
        );

        List<PartyDetailsModel> expected = new ArrayList<>();
        expected.add(applicantPartyDetails);
        expected.add(applicantSolicitorParty);
        expected.add(respondentPartyDetails);
        expected.add(respondentSolicitorParty);

        List<PartyDetailsModel> actualPartyDetailsModel = buildPartyObjectForHearingPayload(
            caseData,
            organisationService
        );
        assertThat(actualPartyDetailsModel).isEqualTo(expected);
    }

    @Test
    void shouldBuildPartyDetails_whenClaimantResponds1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .addRespondent1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed()
            .addApplicant1ExpertsAndWitnesses()
            .addRespondent1ExpertsAndWitnesses()
            .build();

        PartyDetailsModel applicantPartyDetails = buildExpectedIndividualPartyDetails(
            "John",
            "Rambo",
            "Mr. John Rambo",
            CLAIMANT_ROLE,
            "rambo@email.com",
            "0123456789"
        );

        PartyDetailsModel applicantSolicitorParty = buildExpectedOrganisationPartyObject(
            APPLICANT_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            APPLICANT_ORG_ID
        );

        PartyDetailsModel applicantExpert = buildExpectedIndividualPartyDetails(
            "Applicant",
            "Expert",
            "Applicant Expert",
            EXPERT_ROLE,
            null,
            null
        );

        PartyDetailsModel applicantWitness = buildExpectedIndividualPartyDetails(
            "Applicant",
            "Witness",
            "Applicant Witness",
            WITNESS_ROLE,
            null,
            null
        );

        PartyDetailsModel applicantLitFriend = buildExpectedIndividualPartyDetails(
            "Applicant",
            "Litigation Friend",
            "Applicant Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        PartyDetailsModel respondentPartyDetails = buildExpectedIndividualPartyDetails(
            "Sole",
            "Trader",
            "Mr. Sole Trader",
            DEFENDANT_ROLE,
            "sole.trader@email.com",
            "0123456789"
        );

        PartyDetailsModel respondentSolicitorParty = buildExpectedOrganisationPartyObject(
            RESPONDENT_ONE_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            RESPONDENT_ONE_ORG_ID
        );

        PartyDetailsModel respondent1Expert = buildExpectedIndividualPartyDetails(
            "Respondent",
            "Expert",
            "Respondent Expert",
            EXPERT_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent1Witness = buildExpectedIndividualPartyDetails(
            "Respondent",
            "Witness",
            "Respondent Witness",
            WITNESS_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent1LitFriend = buildExpectedIndividualPartyDetails(
            "Litigation",
            "Friend",
            "Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        List<PartyDetailsModel> expected = new ArrayList<>();
        expected.add(applicantPartyDetails);
        expected.add(applicantSolicitorParty);
        expected.add(applicantExpert);
        expected.add(applicantWitness);
        expected.add(applicantLitFriend);
        expected.add(respondentPartyDetails);
        expected.add(respondentSolicitorParty);
        expected.add(respondent1Expert);
        expected.add(respondent1Witness);
        expected.add(respondent1LitFriend);

        List<PartyDetailsModel> actualPartyDetailsModel = buildPartyObjectForHearingPayload(
            caseData,
            organisationService
        );
        assertThat(actualPartyDetailsModel).isEqualTo(expected);
    }

    @Test
    void shouldBuildPartyDetails_whenClaimantResponds2v1App1NotProceedApp2Proceeds() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .addApplicant1LitigationFriend()
            .addApplicant2LitigationFriend()
            .addRespondent1LitigationFriend()
            .addApplicant2ExpertsAndWitnesses()
            .addRespondent1ExpertsAndWitnesses()
            .build();

        PartyDetailsModel applicantPartyDetails = buildExpectedIndividualPartyDetails(
            "John",
            "Rambo",
            "Mr. John Rambo",
            CLAIMANT_ROLE,
            "rambo@email.com",
            "0123456789"
        );

        PartyDetailsModel applicantSolicitorParty = buildExpectedOrganisationPartyObject(
            APPLICANT_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            APPLICANT_ORG_ID
        );

        PartyDetailsModel applicant2PartyDetails = buildExpectedIndividualPartyDetails(
            "Jason",
            "Rambo",
            "Mr. Jason Rambo",
            CLAIMANT_ROLE,
            "rambo@email.com",
            "0123456789"
        );

        PartyDetailsModel applicantExpert = buildExpectedIndividualPartyDetails(
            "Applicant Two",
            "Expert",
            "Applicant Two Expert",
            EXPERT_ROLE,
            null,
            null
        );

        PartyDetailsModel applicantWitness = buildExpectedIndividualPartyDetails(
            "Applicant Two",
            "Witness",
            "Applicant Two Witness",
            WITNESS_ROLE,
            null,
            null
        );

        PartyDetailsModel applicantLitFriend = buildExpectedIndividualPartyDetails(
            "Applicant",
            "Litigation Friend",
            "Applicant Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        PartyDetailsModel applicant2LitFriend = buildExpectedIndividualPartyDetails(
            "Applicant Two",
            "Litigation Friend",
            "Applicant Two Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        PartyDetailsModel respondentPartyDetails = buildExpectedIndividualPartyDetails(
            "Sole",
            "Trader",
            "Mr. Sole Trader",
            DEFENDANT_ROLE,
            "sole.trader@email.com",
            "0123456789"
        );

        PartyDetailsModel respondentSolicitorParty = buildExpectedOrganisationPartyObject(
            RESPONDENT_ONE_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            RESPONDENT_ONE_ORG_ID
        );

        PartyDetailsModel respondent1Expert = buildExpectedIndividualPartyDetails(
            "Respondent",
            "Expert",
            "Respondent Expert",
            EXPERT_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent1Witness = buildExpectedIndividualPartyDetails(
            "Respondent",
            "Witness",
            "Respondent Witness",
            WITNESS_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent1LitFriend = buildExpectedIndividualPartyDetails(
            "Litigation",
            "Friend",
            "Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        List<PartyDetailsModel> expected = new ArrayList<>();
        expected.add(applicantPartyDetails);
        expected.add(applicantSolicitorParty);
        expected.add(applicantLitFriend);
        expected.add(applicant2PartyDetails);
        expected.add(applicantExpert);
        expected.add(applicantWitness);
        expected.add(applicant2LitFriend);
        expected.add(respondentPartyDetails);
        expected.add(respondentSolicitorParty);
        expected.add(respondent1Expert);
        expected.add(respondent1Witness);
        expected.add(respondent1LitFriend);

        List<PartyDetailsModel> actualPartyDetailsModel = buildPartyObjectForHearingPayload(
            caseData,
            organisationService
        );
        assertThat(actualPartyDetailsModel).isEqualTo(expected);
    }

    @Test
    void shouldBuildPartyDetails_whenClaimantResponds1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateApplicantRespondToDefenceAndProceed(ONE_V_TWO_TWO_LEGAL_REP)
            .addApplicant1LitigationFriend()
            .addRespondent1LitigationFriend()
            .addRespondent2LitigationFriend()
            .addApplicant1ExpertsAndWitnesses()
            .addRespondent1ExpertsAndWitnesses()
            .addRespondent2ExpertsAndWitnesses()
            .build();

        PartyDetailsModel applicantPartyDetails = buildExpectedIndividualPartyDetails(
            "John",
            "Rambo",
            "Mr. John Rambo",
            CLAIMANT_ROLE,
            "rambo@email.com",
            "0123456789"
        );

        PartyDetailsModel applicantSolicitorParty = buildExpectedOrganisationPartyObject(
            APPLICANT_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            APPLICANT_ORG_ID
        );

        PartyDetailsModel applicantExpert = buildExpectedIndividualPartyDetails(
            "Applicant",
            "Expert",
            "Applicant Expert",
            EXPERT_ROLE,
            null,
            null
        );

        PartyDetailsModel applicantWitness = buildExpectedIndividualPartyDetails(
            "Applicant",
            "Witness",
            "Applicant Witness",
            WITNESS_ROLE,
            null,
            null
        );

        PartyDetailsModel applicantLitFriend = buildExpectedIndividualPartyDetails(
            "Applicant",
            "Litigation Friend",
            "Applicant Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        PartyDetailsModel respondentPartyDetails = buildExpectedIndividualPartyDetails(
            "Sole",
            "Trader",
            "Mr. Sole Trader",
            DEFENDANT_ROLE,
            "sole.trader@email.com",
            "0123456789"
        );

        PartyDetailsModel respondentSolicitorParty = buildExpectedOrganisationPartyObject(
            RESPONDENT_ONE_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            RESPONDENT_ONE_ORG_ID
        );

        PartyDetailsModel respondent1Expert = buildExpectedIndividualPartyDetails(
            "Respondent",
            "Expert",
            "Respondent Expert",
            EXPERT_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent1Witness = buildExpectedIndividualPartyDetails(
            "Respondent",
            "Witness",
            "Respondent Witness",
            WITNESS_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent1LitFriend = buildExpectedIndividualPartyDetails(
            "Litigation",
            "Friend",
            "Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent2PartyDetails = buildExpectedIndividualPartyDetails(
            "John",
            "Rambo",
            "Mr. John Rambo",
            DEFENDANT_ROLE,
            "rambo@email.com",
            "0123456789"
        );

        PartyDetailsModel respondent2SolicitorParty = buildExpectedOrganisationPartyObject(
            RESPONDENT_TWO_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            RESPONDENT_TWO_ORG_ID
        );

        PartyDetailsModel respondent2Expert = buildExpectedIndividualPartyDetails(
            "Respondent Two",
            "Expert",
            "Respondent Two Expert",
            EXPERT_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent2Witness = buildExpectedIndividualPartyDetails(
            "Respondent Two",
            "Witness",
            "Respondent Two Witness",
            WITNESS_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent2LitFriend = buildExpectedIndividualPartyDetails(
            "Litigation",
            "Friend",
            "Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        List<PartyDetailsModel> expected = new ArrayList<>();
        expected.add(applicantPartyDetails);
        expected.add(applicantSolicitorParty);
        expected.add(applicantExpert);
        expected.add(applicantWitness);
        expected.add(applicantLitFriend);
        expected.add(respondentPartyDetails);
        expected.add(respondentSolicitorParty);
        expected.add(respondent1Expert);
        expected.add(respondent1Witness);
        expected.add(respondent1LitFriend);
        expected.add(respondent2PartyDetails);
        expected.add(respondent2SolicitorParty);
        expected.add(respondent2Expert);
        expected.add(respondent2Witness);
        expected.add(respondent2LitFriend);

        List<PartyDetailsModel> actualPartyDetailsModel = buildPartyObjectForHearingPayload(
            caseData,
            organisationService
        );
        assertThat(actualPartyDetailsModel).isEqualTo(expected);
    }

    @Test
    void shouldBuildPartyDetails_whenClaimantResponds1v2SS() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateRespondentFullDefence_1v2_Resp1CounterClaimAndResp2FullDefence()
            .addApplicant1LitigationFriend()
            .addRespondent1LitigationFriend()
            .addRespondent2LitigationFriend()
            .addRespondent2ExpertsAndWitnesses()
            .build();

        PartyDetailsModel applicantPartyDetails = buildExpectedIndividualPartyDetails(
            "John",
            "Rambo",
            "Mr. John Rambo",
            CLAIMANT_ROLE,
            "rambo@email.com",
            "0123456789"
        );

        PartyDetailsModel applicantSolicitorParty = buildExpectedOrganisationPartyObject(
            APPLICANT_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            APPLICANT_ORG_ID
        );

        PartyDetailsModel applicantLitFriend = buildExpectedIndividualPartyDetails(
            "Applicant",
            "Litigation Friend",
            "Applicant Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        PartyDetailsModel respondentPartyDetails = buildExpectedIndividualPartyDetails(
            "Sole",
            "Trader",
            "Mr. Sole Trader",
            DEFENDANT_ROLE,
            "sole.trader@email.com",
            "0123456789"
        );

        PartyDetailsModel respondentSolicitorParty = buildExpectedOrganisationPartyObject(
            RESPONDENT_ONE_LR_ORG_NAME,
            LEGAL_REP_ROLE,
            RESPONDENT_ONE_ORG_ID
        );

        PartyDetailsModel respondent1LitFriend = buildExpectedIndividualPartyDetails(
            "Litigation",
            "Friend",
            "Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent2PartyDetails = buildExpectedIndividualPartyDetails(
            "John",
            "Rambo",
            "Mr. John Rambo",
            DEFENDANT_ROLE,
            "rambo@email.com",
            "0123456789"
        );

        PartyDetailsModel respondent2Expert = buildExpectedIndividualPartyDetails(
            "Respondent Two",
            "Expert",
            "Respondent Two Expert",
            EXPERT_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent2Witness = buildExpectedIndividualPartyDetails(
            "Respondent Two",
            "Witness",
            "Respondent Two Witness",
            WITNESS_ROLE,
            null,
            null
        );

        PartyDetailsModel respondent2LitFriend = buildExpectedIndividualPartyDetails(
            "Litigation",
            "Friend",
            "Litigation Friend",
            LITIGATION_FRIEND_ROLE,
            null,
            null
        );

        List<PartyDetailsModel> expected = new ArrayList<>();
        expected.add(applicantPartyDetails);
        expected.add(applicantSolicitorParty);
        expected.add(applicantLitFriend);
        expected.add(respondentPartyDetails);
        expected.add(respondentSolicitorParty);
        expected.add(respondent1LitFriend);
        expected.add(respondent2PartyDetails);
        expected.add(respondent2Expert);
        expected.add(respondent2Witness);
        expected.add(respondent2LitFriend);

        List<PartyDetailsModel> actualPartyDetailsModel = buildPartyObjectForHearingPayload(
            caseData,
            organisationService
        );
        assertThat(actualPartyDetailsModel).isEqualTo(expected);
    }

    private PartyDetailsModel buildExpectedIndividualPartyDetails(String firstName, String lastName,
                                                                  String partyName, String partyRole,
                                                                  String email, String phone) {
        List<String> hearingChannelEmail = email == null ? emptyList() : List.of(email);
        List<String> hearingChannelPhone = phone == null ? emptyList() : List.of(phone);
        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName(firstName)
            .lastName(lastName)
            .interpreterLanguage(null)
            .reasonableAdjustments(emptyList())
            .vulnerableFlag(false)
            .vulnerabilityDetails(null)
            .hearingChannelEmail(hearingChannelEmail)
            .hearingChannelPhone(hearingChannelPhone)
            .relatedParties(List.of(RelatedPartiesModel.builder().build()))
            .custodyStatus(null)
            .build();

        return PartyDetailsModel.builder()
            .partyID("")
            .partyType(IND)
            .partyName(partyName)
            .partyRole(partyRole)
            .individualDetails(individualDetails)
            .organisationDetails(null)
            .unavailabilityDOW(null)
            .unavailabilityRange(null)
            .hearingSubChannel(null)
            .build();
    }

    private PartyDetailsModel buildExpectedOrganisationPartyObject(String name,
                                                                 String partyRole,
                                                                 String cftOrganisationID) {
        OrganisationDetailsModel organisationDetails = OrganisationDetailsModel.builder()
            .name(name)
            .organisationType(null)
            .cftOrganisationID(cftOrganisationID)
            .build();

        return PartyDetailsModel.builder()
            .partyID("")
            .partyType(ORG)
            .partyName(name)
            .partyRole(partyRole)
            .individualDetails(null)
            .organisationDetails(organisationDetails)
            .unavailabilityDOW(null)
            .unavailabilityRange(null)
            .hearingSubChannel(null)
            .build();
    }
}
