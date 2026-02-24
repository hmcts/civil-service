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
            .thenReturn(Optional.of(new Organisation().setName("Civil - Organisation 1")));
    }

    @Test
    void shouldInitialiseCaseFlagsForCreateClaimEvent() {
        var applicant1 = PartyBuilder.builder().individual().build();
        var applicant2 = PartyBuilder.builder().company().build();
        var respondent1 = PartyBuilder.builder().soleTrader().build();
        var respondent2 = PartyBuilder.builder().organisation().build();
        var applicant1LitFriend = new LitigationFriend().setFirstName("Jason").setLastName("Wilson");
        var applicant2LitFriend = new LitigationFriend().setFirstName("Jenny").setLastName("Carter");

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
                applicant1LitFriend.copy().setFlags(
                    new Flags()
                        .setPartyName("Jason Wilson")
                        .setRoleOnCase("Claimant 1 Litigation Friend")
                        .setDetails(List.of()))
                    
                )
            .applicant2LitigationFriend(
                applicant2LitFriend.copy().setFlags(
                        new Flags()
                            .setPartyName("Jenny Carter")
                            .setRoleOnCase("Claimant 2 Litigation Friend")
                            .setDetails(List.of()))
                    )
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
        var respondent1LitFriend = new LitigationFriend().setFirstName("Jason").setLastName("Wilson");
        var respondent2LitFriend = new LitigationFriend().setFirstName("Jenny").setLastName("Carter");

        var expected = CaseData.builder()
            .respondent1LitigationFriend(
                respondent1LitFriend.copy().setFlags(
                        new Flags()
                            .setPartyName("Jason Wilson")
                            .setRoleOnCase("Defendant 1 Litigation Friend")
                            .setDetails(List.of()))
                    
            )
            .respondent2LitigationFriend(
                respondent2LitFriend.copy().setFlags(
                        new Flags()
                            .setPartyName("Jenny Carter")
                            .setRoleOnCase("Defendant 2 Litigation Friend")
                            .setDetails(List.of()))
                    )
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
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("id")))
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
        LitigationFriend applicant1LitFriend = new LitigationFriend().setFirstName("Jason").setLastName("Wilson");
        LitigationFriend applicant2LitFriend = new LitigationFriend().setFirstName("Jenny").setLastName("Carter");
        Witness witness1 = new Witness().setFirstName("First").setLastName("Name");
        Witness witness2 = new Witness().setFirstName("Second").setLastName("witness");
        Witness witness3 = new Witness().setFirstName("Third").setLastName("witnessy");
        Expert expert1 = new Expert().setFirstName("First").setLastName("Name");
        Expert expert2 = new Expert().setFirstName("Second").setLastName("expert");
        Expert expert3 = new Expert().setFirstName("Third").setLastName("experto");

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
                applicant1LitFriend.copy().setFlags(
                        new Flags()
                            .setPartyName("Jason Wilson")
                            .setRoleOnCase("Claimant 1 Litigation Friend")
                            .setDetails(List.of()))
                    
            )
            .applicant2LitigationFriend(
                applicant2LitFriend.copy().setFlags(
                        new Flags()
                            .setPartyName("Jenny Carter")
                            .setRoleOnCase("Claimant 2 Litigation Friend")
                            .setDetails(List.of()))
                    )
            .respondent1(
                respondent1.toBuilder().flags(
                    new Flags()
                        .setPartyName("Mr. Sole Trader")
                        .setRoleOnCase("Defendant 1")
                        .setDetails(List.of())).build())
            .applicantWitnesses(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .setDetails(List.of())),
                 new PartyFlagStructure()
                     .setFirstName("Second")
                     .setLastName("witness")
                     .setFlags(new Flags()
                                .setPartyName("Second witness")
                                .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                                .setDetails(List.of())),
                new PartyFlagStructure()
                    .setFirstName("Third")
                    .setLastName("witnessy")
                    .setFlags(new Flags()
                               .setPartyName("Third witnessy")
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .setDetails(List.of()))
                    )))
            .applicantExperts(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of())),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("expert")
                    .setFlags(new Flags()
                               .setPartyName("Second expert")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of())),
                new PartyFlagStructure()
                    .setFirstName("Third")
                    .setLastName("experto")
                    .setFlags(new Flags()
                               .setPartyName("Third experto")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of()))
                    )))
            .respondent1Witnesses(wrapElements(List.of(
                    new PartyFlagStructure()
                        .setFirstName("First")
                        .setLastName("Name")
                        .setFlags(new Flags()
                                   .setPartyName("First Name")
                                   .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                                   .setDetails(List.of())),
                    new PartyFlagStructure()
                        .setFirstName("Second")
                        .setLastName("witness")
                        .setFlags(new Flags()
                                   .setPartyName("Second witness")
                                   .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                                   .setDetails(List.of()))
                        )))
            .respondent1Experts(wrapElements(List.of(
                    new PartyFlagStructure()
                        .setFirstName("First")
                        .setLastName("Name")
                        .setFlags(new Flags()
                                   .setPartyName("First Name")
                                   .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                                   .setDetails(List.of())),
                    new PartyFlagStructure()
                        .setFirstName("Second")
                        .setLastName("expert")
                        .setFlags(new Flags()
                                   .setPartyName("Second expert")
                                   .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                                   .setDetails(List.of()))
                        )))
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant1LitigationFriend(applicant1LitFriend)
            .applicant2(applicant2)
            .addApplicant2(YES)
            .addRespondent2(YES)
            .applicant2LitigationFriend(applicant2LitFriend)
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQWitnesses(new Witnesses()
                                                            .setDetails(wrapElements(witness1, witness2)))
                              .setApplicant1DQExperts(new Experts()
                                                          .setDetails(wrapElements(expert1, expert2))))
            .applicant2DQ(new Applicant2DQ()
                              .setApplicant2DQWitnesses(new Witnesses()
                                                            .setDetails(wrapElements(witness3)))
                              .setApplicant2DQExperts(new Experts()
                                                          .setDetails(wrapElements(expert3))))
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQExperts(new Experts()
                                                            .setDetails(wrapElements(expert1, expert2)))
                               .setRespondent1DQWitnesses(new Witnesses()
                                                              .setDetails(wrapElements(witness1, witness2))))
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
        LitigationFriend applicant1LitFriend = new LitigationFriend().setFirstName("Jason").setLastName("Wilson");
        LitigationFriend applicant2LitFriend = new LitigationFriend().setFirstName("Jenny").setLastName("Carter");
        Witness witness1 = new Witness().setFirstName("First").setLastName("Name");
        Witness witness2 = new Witness().setFirstName("Second").setLastName("witness");
        Witness witness3 = new Witness().setFirstName("Third").setLastName("witnessy");
        Expert expert1 = new Expert().setFirstName("First").setLastName("Name");
        Expert expert2 = new Expert().setFirstName("Second").setLastName("expert");
        Expert expert3 = new Expert().setFirstName("Third").setLastName("experto");

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
                applicant1LitFriend.copy().setFlags(
                        new Flags()
                            .setPartyName("Jason Wilson")
                            .setRoleOnCase("Claimant 1 Litigation Friend")
                            .setDetails(List.of()))
                    
            )
            .applicant2LitigationFriend(
                applicant2LitFriend.copy().setFlags(
                        new Flags()
                            .setPartyName("Jenny Carter")
                            .setRoleOnCase("Claimant 2 Litigation Friend")
                            .setDetails(List.of()))
                    )
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
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .setDetails(List.of())),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("witness")
                    .setFlags(new Flags()
                               .setPartyName("Second witness")
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .setDetails(List.of())),
                new PartyFlagStructure()
                    .setFirstName("Third")
                    .setLastName("witnessy")
                    .setFlags(new Flags()
                               .setPartyName("Third witnessy")
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .setDetails(List.of()))
                    )))
            .applicantExperts(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of())),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("expert")
                    .setFlags(new Flags()
                               .setPartyName("Second expert")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of())),
                new PartyFlagStructure()
                    .setFirstName("Third")
                    .setLastName("experto")
                    .setFlags(new Flags()
                               .setPartyName("Third experto")
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .setDetails(List.of()))
                    )))
            .respondent1Witnesses(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("witness")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS))
                    )))
            .respondent1Experts(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("expert")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT))
                    )))
            .respondent2Witnesses(wrapElements(List.of(new PartyFlagStructure()
                                                           .setFirstName("Third")
                                                           .setLastName("witnessy")
                                                           .setFlags(new Flags()
                                                                      .setDetails(wrapElements(List.of(new FlagDetail()
                                                                                                        .setName("Flag name")
                                                                                                        .setFlagCode("123"))))
                                                                      .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS))
                                                           )))
            .respondent2Experts(wrapElements(List.of(new PartyFlagStructure()
                                                         .setFirstName("Third")
                                                         .setLastName("experto")
                                                         .setFlags(new Flags()
                                                                    .setDetails(wrapElements(List.of(new FlagDetail()
                                                                                                      .setName("Flag name")
                                                                                                      .setFlagCode("123"))))
                                                                    .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)))))
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant1LitigationFriend(applicant1LitFriend)
            .applicant2(applicant2)
            .addApplicant2(YES)
            .applicant2LitigationFriend(applicant2LitFriend)
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQWitnesses(new Witnesses()
                                                            .setDetails(wrapElements(witness1, witness2)))
                              .setApplicant1DQExperts(new Experts()
                                                          .setDetails(wrapElements(expert1, expert2))))
            .applicant2DQ(new Applicant2DQ()
                              .setApplicant2DQWitnesses(new Witnesses()
                                                            .setDetails(wrapElements(witness3)))
                              .setApplicant2DQExperts(new Experts()
                                                          .setDetails(wrapElements(expert3))))
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQExperts(new Experts()
                                                            .setDetails(wrapElements(expert1, expert2)))
                               .setRespondent1DQWitnesses(new Witnesses()
                                                              .setDetails(wrapElements(witness1, witness2))))
            .respondent2DQ(new Respondent2DQ()
                               .setRespondent2DQExperts(new Experts()
                                                            .setDetails(wrapElements(expert3)))
                               .setRespondent2DQWitnesses(new Witnesses()
                                                              .setDetails(wrapElements(witness3))))
            .addRespondent2(YES)
            .respondent1Witnesses(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("witness")
                    .setFlags(new Flags()
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123")))))
                    )))
            .respondent1Experts(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("expert")
                    .setFlags(new Flags()
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123")))))
                    )))
            .respondent2Witnesses(wrapElements(List.of(new PartyFlagStructure()
                                                           .setFirstName("Third")
                                                           .setLastName("witnessy")
                                                           .setFlags(new Flags()
                                                                      .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS)
                                                                      .setDetails(wrapElements(List.of(new FlagDetail()
                                                                                                        .setName("Flag name")
                                                                                                        .setFlagCode("123")))))
                                                           )))
            .respondent2Experts(wrapElements(List.of(new PartyFlagStructure()
                                                         .setFirstName("Third")
                                                         .setLastName("experto")
                                                         .setFlags(new Flags()
                                                                    .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)
                                                                    .setDetails(wrapElements(List.of(new FlagDetail()
                                                                                                      .setName("Flag name")
                                                                                                      .setFlagCode("123"))))
                                                         ))))
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
        LitigationFriend applicant1LitFriend = new LitigationFriend().setFirstName("Jason").setLastName("Wilson");
        LitigationFriend applicant2LitFriend = new LitigationFriend().setFirstName("Jenny").setLastName("Carter");
        Witness witness1 = new Witness().setFirstName("First").setLastName("Name");
        Witness witness2 = new Witness().setFirstName("Second").setLastName("witness");
        Witness witness3 = new Witness().setFirstName("Third").setLastName("witnessy");
        Expert expert1 = new Expert().setFirstName("First").setLastName("Name");
        Expert expert2 = new Expert().setFirstName("Second").setLastName("expert");
        Expert expert3 = new Expert().setFirstName("Third").setLastName("experto");

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
                applicant1LitFriend.copy().setFlags(
                        new Flags()
                            .setPartyName("Jason Wilson")
                            .setRoleOnCase("Claimant 1 Litigation Friend")
                            .setDetails(List.of()))
                    
            )
            .applicant2LitigationFriend(
                applicant2LitFriend.copy().setFlags(
                        new Flags()
                            .setPartyName("Jenny Carter")
                            .setRoleOnCase("Claimant 2 Litigation Friend")
                            .setDetails(List.of()))
                    )
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
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    ),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("witness")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    ),
                new PartyFlagStructure()
                    .setFirstName("Third")
                    .setLastName("witnessy")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    )
                    )))
            .applicantExperts(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    ),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("expert")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    ),
                new PartyFlagStructure()
                    .setFirstName("Third")
                    .setLastName("experto")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    )
                    )))
            .respondent1Witnesses(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .setDetails(wrapElements(List.of()))),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("witness")
                    .setFlags(new Flags()
                               .setPartyName("Second witness")
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .setDetails(wrapElements(List.of())))
                    )))
            .respondent1Experts(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setPartyName("First Name")
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .setDetails(wrapElements(List.of()))),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("expert")
                    .setFlags(new Flags()
                               .setPartyName("Second expert")
                               .setRoleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .setDetails(wrapElements(List.of()))
                    )
                    )))
            .respondent2Witnesses(wrapElements(List.of(new PartyFlagStructure()
                                                           .setFirstName("Third")
                                                           .setLastName("witnessy")
                                                           .setFlags(new Flags()
                                                                      .setPartyName("Third witnessy")
                                                                      .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS)
                                                                      .setDetails(wrapElements(List.of()))
                                                           )
                                                           )))
            .respondent2Experts(wrapElements(List.of(new PartyFlagStructure()
                                                         .setFirstName("Third")
                                                         .setLastName("experto")
                                                         .setFlags(new Flags()
                                                                    .setPartyName("Third experto")
                                                                    .setRoleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)
                                                                    .setDetails(wrapElements(List.of()))
                                                         ))))
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant1LitigationFriend(applicant1LitFriend)
            .applicant2(applicant2)
            .applicant2LitigationFriend(applicant2LitFriend)
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQWitnesses(new Witnesses()
                                                            .setDetails(wrapElements(witness1, witness2)))
                              .setApplicant1DQExperts(new Experts()
                                                          .setDetails(wrapElements(expert1, expert2))))
            .applicant2DQ(new Applicant2DQ()
                              .setApplicant2DQWitnesses(new Witnesses()
                                                            .setDetails(wrapElements(witness3)))
                              .setApplicant2DQExperts(new Experts()
                                                          .setDetails(wrapElements(expert3))))
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQExperts(new Experts()
                                                            .setDetails(wrapElements(expert1, expert2)))
                               .setRespondent1DQWitnesses(new Witnesses()
                                                              .setDetails(wrapElements(witness1, witness2))))
            .respondent2DQ(new Respondent2DQ()
                               .setRespondent2DQExperts(new Experts()
                                                            .setDetails(wrapElements(expert3)))
                               .setRespondent2DQWitnesses(new Witnesses()
                                                              .setDetails(wrapElements(witness3))))
            .respondent1(respondent1)
            .respondent2(respondent2)
            .applicantWitnesses(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    ),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("witness")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    ),
                new PartyFlagStructure()
                    .setFirstName("Third")
                    .setLastName("witnessy")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_WITNESS)
                    )
                    
            )))
            .applicantExperts(wrapElements(List.of(
                new PartyFlagStructure()
                    .setFirstName("First")
                    .setLastName("Name")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    ),
                new PartyFlagStructure()
                    .setFirstName("Second")
                    .setLastName("expert")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    ),
                new PartyFlagStructure()
                    .setFirstName("Third")
                    .setLastName("experto")
                    .setFlags(new Flags()
                               .setDetails(wrapElements(List.of(new FlagDetail()
                                                                 .setName("Flag name")
                                                                 .setFlagCode("123"))))
                               .setRoleOnCase(APPLICANT_SOLICITOR_EXPERT)
                    )
                    
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
