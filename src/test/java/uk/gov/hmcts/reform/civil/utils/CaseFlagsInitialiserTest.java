package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.APPLICANT_SOLICITOR_EXPERT;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.APPLICANT_SOLICITOR_WITNESS;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_SOLICITOR_ONE_EXPERT;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_SOLICITOR_ONE_WITNESS;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_SOLICITOR_TWO_EXPERT;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_SOLICITOR_TWO_WITNESS;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID;

class CaseFlagsInitialiserTest {

    private CaseFlagsInitialiser caseFlagsInitialiser;

    private OrganisationService organisationService;

    @BeforeEach
    void setup() {
        organisationService = mock(OrganisationService.class);
        caseFlagsInitialiser = new CaseFlagsInitialiser(organisationService);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("Civil - Organisation 1").build()));
    }

    @Test
    void shouldInitialiseCaseFlagsForCreateClaimEvent() {
        var applicant1 = PartyBuilder.builder().individual().build();
        var applicant2 = PartyBuilder.builder().company().build();
        var respondent1 = PartyBuilder.builder().soleTrader().build();
        var respondent2 = PartyBuilder.builder().organisation().build();
        var applicant1LitFriend = LitigationFriend.builder().firstName("Jason").lastName("Wilson").build();
        var applicant2LitFriend = LitigationFriend.builder().firstName("Jenny").lastName("Carter").build();

        var expected = CaseData.builder()
            .applicant1(
                applicant1.toBuilder().flags(
                    new Flags()
                        .setPartyName("Mr. John Rambo")
                        .setRoleOnCase("Claimant 1")
                        .setDetails(List.of())).build())
            .applicant2(
                applicant2.toBuilder().flags(
                    new Flags()
                        .setPartyName("Company ltd")
                        .setRoleOnCase("Claimant 2")
                        .setDetails(List.of())).build())
            .applicant1LitigationFriend(
                applicant1LitFriend.toBuilder().flags(
                    new Flags()
                        .setPartyName("Jason Wilson")
                        .setRoleOnCase("Claimant 1 Litigation Friend")
                        .setDetails(List.of()))
                    .build()
                )
            .applicant2LitigationFriend(
                applicant2LitFriend.toBuilder().flags(
                        new Flags()
                            .setPartyName("Jenny Carter")
                            .setRoleOnCase("Claimant 2 Litigation Friend")
                            .setDetails(List.of()))
                    .build())
            .respondent1(
                respondent1.toBuilder().flags(
                    new Flags()
                        .setPartyName("Mr. Sole Trader")
                        .setRoleOnCase("Defendant 1")
                        .setDetails(List.of())).build())
            .respondent2(
                respondent2.toBuilder().flags(
                    new Flags()
                        .setPartyName("The Organisation")
                        .setRoleOnCase("Defendant 2")
                        .setDetails(List.of())).build())
            .build();

        var caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant1LitigationFriend(applicant1LitFriend)
            .applicant2(applicant2)
            .applicant2LitigationFriend(applicant2LitFriend)
            .respondent1(respondent1)
            .respondent2(respondent2).build();

        caseFlagsInitialiser.initialiseCaseFlags(CaseEvent.CREATE_CLAIM, caseData);

        assertEquals(expected, caseData);
    }

    @Test
    void shouldInitialiseCaseFlagsForAddLitigationFriendEvent() {
        var respondent1LitFriend = LitigationFriend.builder().firstName("Jason").lastName("Wilson").build();
        var respondent2LitFriend = LitigationFriend.builder().firstName("Jenny").lastName("Carter").build();

        var expected = CaseData.builder()
            .respondent1LitigationFriend(
                respondent1LitFriend.toBuilder().flags(
                        new Flags()
                            .setPartyName("Jason Wilson")
                            .setRoleOnCase("Defendant 1 Litigation Friend")
                            .setDetails(List.of()))
                    .build()
            )
            .respondent2LitigationFriend(
                respondent2LitFriend.toBuilder().flags(
                        new Flags()
                            .setPartyName("Jenny Carter")
                            .setRoleOnCase("Defendant 2 Litigation Friend")
                            .setDetails(List.of()))
                    .build())
            .build();

        var caseData = CaseData.builder()
            .respondent1LitigationFriend(respondent1LitFriend)
            .respondent2LitigationFriend(respondent2LitFriend).build();

        caseFlagsInitialiser.initialiseCaseFlags(CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND, caseData);

        assertEquals(expected, caseData);
    }

    @Test
    void shouldInitialiseCaseFlagsForManageContactInformationEvent() {
        CaseData caseData = CaseData.builder()
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                .organisationID("id")
                                                                .build())
                                              .build())
            .updateDetailsForm(UpdateDetailsForm.builder()
                                   .partyChosenId(CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID)
                                   .build())
            .build();

        caseFlagsInitialiser.initialiseCaseFlags(CaseEvent.MANAGE_CONTACT_INFORMATION, caseData);

        verify(organisationService).findOrganisationById("id");
    }

    @Test
    void shouldReinitialiseMissingCaseFlags() {
        Party applicant1 = PartyBuilder.builder().individual().build();
        Party applicant2 = PartyBuilder.builder().company().build();
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        LitigationFriend applicant1LitFriend = LitigationFriend.builder().firstName("Jason").lastName("Wilson").build();
        LitigationFriend applicant2LitFriend = LitigationFriend.builder().firstName("Jenny").lastName("Carter").build();
        Witness witness1 = Witness.builder().firstName("First").lastName("Name").build();
        Witness witness2 = Witness.builder().firstName("Second").lastName("witness").build();
        Witness witness3 = Witness.builder().firstName("Third").lastName("witnessy").build();
        Expert expert1 = Expert.builder().firstName("First").lastName("Name").build();
        Expert expert2 = Expert.builder().firstName("Second").lastName("expert").build();
        Expert expert3 = Expert.builder().firstName("Third").lastName("experto").build();

        CaseData expected = CaseData.builder()
            .applicant1(
                applicant1.toBuilder().flags(
                    new Flags()
                        .setPartyName("Mr. John Rambo")
                        .setRoleOnCase("Claimant 1")
                        .setDetails(List.of())).build())
            .applicant2(
                applicant2.toBuilder().flags(
                    new Flags()
                        .setPartyName("Company ltd")
                        .setRoleOnCase("Claimant 2")
                        .setDetails(List.of())).build())
            .applicant1LitigationFriend(
                applicant1LitFriend.toBuilder().flags(
                        new Flags()
                            .setPartyName("Jason Wilson")
                            .setRoleOnCase("Claimant 1 Litigation Friend")
                            .setDetails(List.of()))
                    .build()
            )
            .applicant2LitigationFriend(
                applicant2LitFriend.toBuilder().flags(
                        new Flags()
                            .setPartyName("Jenny Carter")
                            .setRoleOnCase("Claimant 2 Litigation Friend")
                            .setDetails(List.of()))
                    .build())
            .respondent1(
                respondent1.toBuilder().flags(
                    new Flags()
                        .setPartyName("Mr. Sole Trader")
                        .setRoleOnCase("Defendant 1")
                        .setDetails(List.of())).build())
            .applicantWitnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .setDetails(List.of()))
                    .build(),
                 PartyFlagStructure.builder()
                     .firstName("Second")
                     .lastName("witness")
                     .flags(new Flags()
                                .setPartyName("Second witness")
                                .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                                .setDetails(List.of()))
                     .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("witnessy")
                    .flags(new Flags()
                               .setPartyName("Third witnessy")
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .setDetails(List.of()))
                    .build())))
            .applicantExperts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of()))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(new Flags()
                               .setPartyName("Second expert")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of()))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("experto")
                    .flags(new Flags()
                               .setPartyName("Third experto")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of()))
                    .build())))
            .respondent1Witnesses(wrapElements(List.of(
                    PartyFlagStructure.builder()
                        .firstName("First")
                        .lastName("Name")
                        .flags(new Flags()
                                   .setPartyName("First Name")
                                   .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                                   .setDetails(List.of()))
                        .build(),
                    PartyFlagStructure.builder()
                        .firstName("Second")
                        .lastName("witness")
                        .flags(new Flags()
                                   .setPartyName("Second witness")
                                   .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                                   .setDetails(List.of()))
                        .build())))
            .respondent1Experts(wrapElements(List.of(
                    PartyFlagStructure.builder()
                        .firstName("First")
                        .lastName("Name")
                        .flags(new Flags()
                                   .setPartyName("First Name")
                                   .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                                   .setDetails(List.of()))
                        .build(),
                    PartyFlagStructure.builder()
                        .firstName("Second")
                        .lastName("expert")
                        .flags(new Flags()
                                   .setPartyName("Second expert")
                                   .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                                   .setDetails(List.of()))
                        .build())))
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant1LitigationFriend(applicant1LitFriend)
            .applicant2(applicant2)
            .addApplicant2(YES)
            .addRespondent2(YES)
            .applicant2LitigationFriend(applicant2LitFriend)
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQWitnesses(Witnesses
                                                         .builder()
                                                         .details(wrapElements(witness1, witness2))
                                                         .build())
                              .applicant1DQExperts(Experts
                                                       .builder()
                                                       .details(wrapElements(expert1, expert2))
                                                       .build())
                              .build())
            .applicant2DQ(Applicant2DQ.builder()
                              .applicant2DQWitnesses(Witnesses
                                                         .builder()
                                                         .details(wrapElements(witness3))
                                                         .build())
                              .applicant2DQExperts(Experts
                                                       .builder()
                                                       .details(wrapElements(expert3))
                                                       .build())
                              .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQExperts(Experts
                                                         .builder()
                                                         .details(wrapElements(expert1, expert2))
                                                         .build())
                               .respondent1DQWitnesses(Witnesses
                                                           .builder()
                                                           .details(wrapElements(witness1, witness2))
                                                           .build())
                               .build())
            .respondent1(respondent1).build();

        caseFlagsInitialiser.initialiseMissingCaseFlags(caseData);

        assertFlags(expected, caseData, false);
    }

    @Test
    void shouldNotReinitialiseCaseFlagsForRespondentDQ_whenRespondent1DQFlagsExist() {
        Party applicant1 = PartyBuilder.builder().individual().build();
        Party applicant2 = PartyBuilder.builder().company().build();
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        Party respondent2 = PartyBuilder.builder().organisation().build();
        LitigationFriend applicant1LitFriend = LitigationFriend.builder().firstName("Jason").lastName("Wilson").build();
        LitigationFriend applicant2LitFriend = LitigationFriend.builder().firstName("Jenny").lastName("Carter").build();
        Witness witness1 = Witness.builder().firstName("First").lastName("Name").build();
        Witness witness2 = Witness.builder().firstName("Second").lastName("witness").build();
        Witness witness3 = Witness.builder().firstName("Third").lastName("witnessy").build();
        Expert expert1 = Expert.builder().firstName("First").lastName("Name").build();
        Expert expert2 = Expert.builder().firstName("Second").lastName("expert").build();
        Expert expert3 = Expert.builder().firstName("Third").lastName("experto").build();

        CaseData expected = CaseData.builder()
            .applicant1(
                applicant1.toBuilder().flags(
                    new Flags()
                        .setPartyName("Mr. John Rambo")
                        .setRoleOnCase("Claimant 1")
                        .setDetails(List.of())).build())
            .applicant2(
                applicant2.toBuilder().flags(
                    new Flags()
                        .setPartyName("Company ltd")
                        .setRoleOnCase("Claimant 2")
                        .setDetails(List.of())).build())
            .applicant1LitigationFriend(
                applicant1LitFriend.toBuilder().flags(
                        new Flags()
                            .setPartyName("Jason Wilson")
                            .setRoleOnCase("Claimant 1 Litigation Friend")
                            .setDetails(List.of()))
                    .build()
            )
            .applicant2LitigationFriend(
                applicant2LitFriend.toBuilder().flags(
                        new Flags()
                            .setPartyName("Jenny Carter")
                            .setRoleOnCase("Claimant 2 Litigation Friend")
                            .setDetails(List.of()))
                    .build())
            .respondent1(
                respondent1.toBuilder().flags(
                    new Flags()
                        .setPartyName("Mr. Sole Trader")
                        .setRoleOnCase("Defendant 1")
                        .setDetails(List.of())).build())
            .respondent2(
                respondent2.toBuilder().flags(
                    new Flags()
                        .setPartyName("The Organisation")
                        .setRoleOnCase("Defendant 2")
                        .setDetails(List.of())).build())
            .applicantWitnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .setDetails(List.of()))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(new Flags()
                               .setPartyName("Second witness")
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .setDetails(List.of()))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("witnessy")
                    .flags(new Flags()
                               .setPartyName("Third witnessy")
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .setDetails(List.of()))
                    .build())))
            .applicantExperts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of()))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(new Flags()
                               .setPartyName("Second expert")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of()))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("experto")
                    .flags(new Flags()
                               .setPartyName("Third experto")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of()))
                    .build())))
            .respondent1Witnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS))
                    .build())))
            .respondent1Experts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT))
                    .build())))
            .respondent2Witnesses(wrapElements(List.of(PartyFlagStructure.builder()
                                                           .firstName("Third")
                                                           .lastName("witnessy")
                                                           .flags(new Flags()
                                                                      .setDetails(wrapElements(List.of(new FlagDetail()
                                                                                                        .setName("Flag name")
                                                                                                        .setFlagCode("123"))))
                                                                      .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS))
                                                           .build())))
            .respondent2Experts(wrapElements(List.of(PartyFlagStructure.builder()
                                                         .firstName("Third")
                                                         .lastName("experto")
                                                         .flags(new Flags()
                                                                    .setDetails(wrapElements(List.of(new FlagDetail()
                                                                                                      .setName("Flag name")
                                                                                                      .setFlagCode("123"))))
                                                                    .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)).build())))
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant1LitigationFriend(applicant1LitFriend)
            .applicant2(applicant2)
            .addApplicant2(YES)
            .applicant2LitigationFriend(applicant2LitFriend)
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQWitnesses(Witnesses
                                                         .builder()
                                                         .details(wrapElements(witness1, witness2))
                                                         .build())
                              .applicant1DQExperts(Experts
                                                       .builder()
                                                       .details(wrapElements(expert1, expert2))
                                                       .build())
                              .build())
            .applicant2DQ(Applicant2DQ.builder()
                              .applicant2DQWitnesses(Witnesses
                                                         .builder()
                                                         .details(wrapElements(witness3))
                                                         .build())
                              .applicant2DQExperts(Experts
                                                       .builder()
                                                       .details(wrapElements(expert3))
                                                       .build())
                              .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQExperts(Experts
                                                         .builder()
                                                         .details(wrapElements(expert1, expert2))
                                                         .build())
                               .respondent1DQWitnesses(Witnesses
                                                           .builder()
                                                           .details(wrapElements(witness1, witness2))
                                                           .build())
                               .build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQExperts(Experts.builder()
                                                         .details(wrapElements(expert3))
                                                         .build())
                               .respondent2DQWitnesses(Witnesses
                                                           .builder()
                                                           .details(wrapElements(witness3))
                                                           .build())
                               .build())
            .addRespondent2(YES)
            .respondent1Witnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123")))))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(new Flags()
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123")))))
                    .build())))
            .respondent1Experts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123")))))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(new Flags()
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123")))))
                    .build())))
            .respondent2Witnesses(wrapElements(List.of(PartyFlagStructure.builder()
                                                           .firstName("Third")
                                                           .lastName("witnessy")
                                                           .flags(new Flags()
                                                                      .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS)
                                                                      .setDetails(wrapElements(List.of(new FlagDetail()
                                                                                                        .setName("Flag name")
                                                                                                        .setFlagCode("123")))))
                                                           .build())))
            .respondent2Experts(wrapElements(List.of(PartyFlagStructure.builder()
                                                         .firstName("Third")
                                                         .lastName("experto")
                                                         .flags(new Flags()
                                                                    .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)
                                                                    .setDetails(wrapElements(List.of(new FlagDetail()
                                                                                                      .setName("Flag name")
                                                                                                      .setFlagCode("123"))))
                                                         ).build())))
            .respondent1(respondent1)
            .respondent2(respondent2).build();

        caseFlagsInitialiser.initialiseMissingCaseFlags(caseData);

        assertFlags(expected, caseData, true);
    }

    @Test
    void shouldNotReinitialiseCaseFlagsForApplicantDQ_whenApplicantDQFlagsExist() {
        Party applicant1 = PartyBuilder.builder().individual().build();
        Party applicant2 = PartyBuilder.builder().company().build();
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        Party respondent2 = PartyBuilder.builder().organisation().build();
        LitigationFriend applicant1LitFriend = LitigationFriend.builder().firstName("Jason").lastName("Wilson").build();
        LitigationFriend applicant2LitFriend = LitigationFriend.builder().firstName("Jenny").lastName("Carter").build();
        Witness witness1 = Witness.builder().firstName("First").lastName("Name").build();
        Witness witness2 = Witness.builder().firstName("Second").lastName("witness").build();
        Witness witness3 = Witness.builder().firstName("Third").lastName("witnessy").build();
        Expert expert1 = Expert.builder().firstName("First").lastName("Name").build();
        Expert expert2 = Expert.builder().firstName("Second").lastName("expert").build();
        Expert expert3 = Expert.builder().firstName("Third").lastName("experto").build();

        CaseData expected = CaseData.builder()
            .applicant1(
                applicant1.toBuilder().flags(
                    new Flags()
                        .setPartyName("Mr. John Rambo")
                        .setRoleOnCase("Claimant 1")
                        .setDetails(List.of())).build())
            .applicant2(
                applicant2.toBuilder().flags(
                    new Flags()
                        .setPartyName("Company ltd")
                        .setRoleOnCase("Claimant 2")
                        .setDetails(List.of())).build())
            .applicant1LitigationFriend(
                applicant1LitFriend.toBuilder().flags(
                        new Flags()
                            .setPartyName("Jason Wilson")
                            .setRoleOnCase("Claimant 1 Litigation Friend")
                            .setDetails(List.of()))
                    .build()
            )
            .applicant2LitigationFriend(
                applicant2LitFriend.toBuilder().flags(
                        new Flags()
                            .setPartyName("Jenny Carter")
                            .setRoleOnCase("Claimant 2 Litigation Friend")
                            .setDetails(List.of()))
                    .build())
            .respondent1(
                respondent1.toBuilder().flags(
                    new Flags()
                        .setPartyName("Mr. Sole Trader")
                        .setRoleOnCase("Defendant 1")
                        .setDetails(List.of())).build())
            .respondent2(
                respondent2.toBuilder().flags(
                    new Flags()
                        .setPartyName("The Organisation")
                        .setRoleOnCase("Defendant 2")
                        .setDetails(List.of())).build())
            .applicantWitnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    )
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    )
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("witnessy")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    )
                    .build())))
            .applicantExperts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    )
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    )
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("experto")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    )
                    .build())))
            .respondent1Witnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .setDetails(wrapElements(List.of())))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(new Flags()
                               .setPartyName("Second witness")
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .setDetails(wrapElements(List.of())))
                    .build())))
            .respondent1Experts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .setDetails(wrapElements(List.of())))
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(new Flags()
                               .setPartyName("Second expert")
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .setDetails(wrapElements(List.of()))
                    )
                    .build())))
            .respondent2Witnesses(wrapElements(List.of(PartyFlagStructure.builder()
                                                           .firstName("Third")
                                                           .lastName("witnessy")
                                                           .flags(new Flags()
                                                                      .setPartyName("Third witnessy")
                                                                      .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS)
                                                                      .setDetails(wrapElements(List.of()))
                                                           )
                                                           .build())))
            .respondent2Experts(wrapElements(List.of(PartyFlagStructure.builder()
                                                         .firstName("Third")
                                                         .lastName("experto")
                                                         .flags(new Flags()
                                                                    .setPartyName("Third experto")
                                                                    .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)
                                                                    .setDetails(wrapElements(List.of()))
                                                         ).build())))
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant1LitigationFriend(applicant1LitFriend)
            .applicant2(applicant2)
            .applicant2LitigationFriend(applicant2LitFriend)
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQWitnesses(Witnesses
                                                         .builder()
                                                         .details(wrapElements(witness1, witness2))
                                                         .build())
                              .applicant1DQExperts(Experts
                                                       .builder()
                                                       .details(wrapElements(expert1, expert2))
                                                       .build())
                              .build())
            .applicant2DQ(Applicant2DQ.builder()
                              .applicant2DQWitnesses(Witnesses
                                                         .builder()
                                                         .details(wrapElements(witness3))
                                                         .build())
                              .applicant2DQExperts(Experts
                                                       .builder()
                                                       .details(wrapElements(expert3))
                                                       .build())
                              .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQExperts(Experts
                                                         .builder()
                                                         .details(wrapElements(expert1, expert2))
                                                         .build())
                               .respondent1DQWitnesses(Witnesses
                                                           .builder()
                                                           .details(wrapElements(witness1, witness2))
                                                           .build())
                               .build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQExperts(Experts.builder()
                                                         .details(wrapElements(expert3))
                                                         .build())
                               .respondent2DQWitnesses(Witnesses
                                                           .builder()
                                                           .details(wrapElements(witness3))
                                                           .build())
                               .build())
            .respondent1(respondent1)
            .respondent2(respondent2)
            .applicantWitnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    )
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    )
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("witnessy")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    )
                    .build()
            )))
            .applicantExperts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    )
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    )
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("experto")
                    .flags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    )
                    .build()
            ))).build();

        caseFlagsInitialiser.initialiseMissingCaseFlags(caseData);

        assertFlags(expected, caseData, true);
    }

    private void assertFlags(CaseData expected, CaseData actual, boolean respondent2) {
        assertThat(actual.getApplicant1().getFlags()).isEqualTo(expected.getApplicant1().getFlags());
        assertThat(actual.getApplicant2().getFlags()).isEqualTo(expected.getApplicant2().getFlags());
        assertThat(actual.getRespondent1().getFlags()).isEqualTo(expected.getRespondent1().getFlags());
        assertThat(actual.getApplicant1LitigationFriend().getFlags()).isEqualTo(expected.getApplicant1LitigationFriend().getFlags());
        assertThat(actual.getApplicant2LitigationFriend().getFlags()).isEqualTo(expected.getApplicant2LitigationFriend().getFlags());
        assertThat(unwrapElements(actual.getApplicantWitnesses())).isEqualTo(unwrapElements(expected.getApplicantWitnesses()));
        assertThat(unwrapElements(actual.getApplicantExperts())).isEqualTo(unwrapElements(expected.getApplicantExperts()));
        assertThat(unwrapElements(actual.getRespondent1Witnesses())).isEqualTo(unwrapElements(expected.getRespondent1Witnesses()));
        assertThat(unwrapElements(actual.getRespondent1Experts())).isEqualTo(unwrapElements(expected.getRespondent1Experts()));
        if (respondent2) {
            assertThat(actual.getRespondent2().getFlags()).isEqualTo(expected.getRespondent2().getFlags());
            assertThat(unwrapElements(actual.getRespondent2Witnesses())).isEqualTo(unwrapElements(expected.getRespondent2Witnesses()));
            assertThat(unwrapElements(actual.getRespondent2Experts())).isEqualTo(unwrapElements(expected.getRespondent2Experts()));
        }
    }
}
