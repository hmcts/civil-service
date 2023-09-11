package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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

class CaseFlagsInitialiserTest {

    private CaseFlagsInitialiser caseFlagsInitialiser;

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        featureToggleService = mock(FeatureToggleService.class);
        caseFlagsInitialiser = new CaseFlagsInitialiser(featureToggleService);
        when(featureToggleService.isCaseFlagsEnabled()).thenReturn(true);
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
                    Flags.builder()
                        .partyName("Mr. John Rambo")
                        .roleOnCase("Applicant 1")
                        .details(List.of()).build()).build())
            .applicant2(
                applicant2.toBuilder().flags(
                    Flags.builder()
                        .partyName("Company ltd")
                        .roleOnCase("Applicant 2")
                        .details(List.of()).build()).build())
            .applicant1LitigationFriend(
                applicant1LitFriend.toBuilder().flags(
                    Flags.builder()
                        .partyName("Jason Wilson")
                        .roleOnCase("Applicant 1 Litigation Friend")
                        .details(List.of()).build())
                    .build()
                )
            .applicant2LitigationFriend(
                applicant2LitFriend.toBuilder().flags(
                        Flags.builder()
                            .partyName("Jenny Carter")
                            .roleOnCase("Applicant 2 Litigation Friend")
                            .details(List.of()).build())
                    .build())
            .respondent1(
                respondent1.toBuilder().flags(
                    Flags.builder()
                        .partyName("Mr. Sole Trader")
                        .roleOnCase("Respondent 1")
                        .details(List.of()).build()).build())
            .respondent2(
                respondent2.toBuilder().flags(
                    Flags.builder()
                        .partyName("The Organisation")
                        .roleOnCase("Respondent 2")
                        .details(List.of()).build()).build())
            .build();

        var actual = CaseData.builder()
            .applicant1(applicant1)
            .applicant1LitigationFriend(applicant1LitFriend)
            .applicant2(applicant2)
            .applicant2LitigationFriend(applicant2LitFriend)
            .respondent1(respondent1)
            .respondent2(respondent2);

        caseFlagsInitialiser.initialiseCaseFlags(CaseEvent.CREATE_CLAIM, actual);

        assertEquals(expected, actual.build());
    }

    @Test
    void shouldInitialiseCaseFlagsForAddLitigationFriendEvent() {
        var respondent1LitFriend = LitigationFriend.builder().firstName("Jason").lastName("Wilson").build();
        var respondent2LitFriend = LitigationFriend.builder().firstName("Jenny").lastName("Carter").build();

        var expected = CaseData.builder()
            .respondent1LitigationFriend(
                respondent1LitFriend.toBuilder().flags(
                        Flags.builder()
                            .partyName("Jason Wilson")
                            .roleOnCase("Respondent 1 Litigation Friend")
                            .details(List.of()).build())
                    .build()
            )
            .respondent2LitigationFriend(
                respondent2LitFriend.toBuilder().flags(
                        Flags.builder()
                            .partyName("Jenny Carter")
                            .roleOnCase("Respondent 2 Litigation Friend")
                            .details(List.of()).build())
                    .build())
            .build();

        var actual = CaseData.builder()
            .respondent1LitigationFriend(respondent1LitFriend)
            .respondent2LitigationFriend(respondent2LitFriend);

        caseFlagsInitialiser.initialiseCaseFlags(CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND, actual);

        assertEquals(expected, actual.build());
    }

    @Test
    void shouldNotInitialiseCaseFlagsWhenCaseFlagsToggleIsOff() {
        var applicant1 = PartyBuilder.builder().individual().build();
        var applicant2 = PartyBuilder.builder().company().build();
        var respondent1 = PartyBuilder.builder().soleTrader().build();
        var respondent2 = PartyBuilder.builder().organisation().build();
        var applicant1LitFriend = LitigationFriend.builder().firstName("Jason").lastName("Wilson").build();
        var applicant2LitFriend = LitigationFriend.builder().firstName("Jenny").lastName("Carter").build();

        var expected = CaseData.builder()
            .applicant1(applicant1)
            .applicant2(applicant2)
            .applicant1LitigationFriend(applicant1LitFriend)
            .applicant2LitigationFriend(applicant2LitFriend)
            .respondent1(respondent1)
            .respondent2(respondent2)
            .build();

        var actual = CaseData.builder()
            .applicant1(applicant1)
            .applicant1LitigationFriend(applicant1LitFriend)
            .applicant2(applicant2)
            .applicant2LitigationFriend(applicant2LitFriend)
            .respondent1(respondent1)
            .respondent2(respondent2);

        when(featureToggleService.isCaseFlagsEnabled()).thenReturn(false);

        caseFlagsInitialiser.initialiseCaseFlags(CaseEvent.CREATE_CLAIM, actual);

        assertEquals(expected, actual.build());
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
                    Flags.builder()
                        .partyName("Mr. John Rambo")
                        .roleOnCase("Applicant 1")
                        .details(List.of()).build()).build())
            .applicant2(
                applicant2.toBuilder().flags(
                    Flags.builder()
                        .partyName("Company ltd")
                        .roleOnCase("Applicant 2")
                        .details(List.of()).build()).build())
            .applicant1LitigationFriend(
                applicant1LitFriend.toBuilder().flags(
                        Flags.builder()
                            .partyName("Jason Wilson")
                            .roleOnCase("Applicant 1 Litigation Friend")
                            .details(List.of()).build())
                    .build()
            )
            .applicant2LitigationFriend(
                applicant2LitFriend.toBuilder().flags(
                        Flags.builder()
                            .partyName("Jenny Carter")
                            .roleOnCase("Applicant 2 Litigation Friend")
                            .details(List.of()).build())
                    .build())
            .respondent1(
                respondent1.toBuilder().flags(
                    Flags.builder()
                        .partyName("Mr. Sole Trader")
                        .roleOnCase("Respondent 1")
                        .details(List.of()).build()).build())
            .applicantWitnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .partyName("First Name")
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .details(List.of())
                               .build())
                    .build(),
                 PartyFlagStructure.builder()
                     .firstName("Second")
                     .lastName("witness")
                     .flags(Flags.builder()
                                .partyName("Second witness")
                                .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                                .details(List.of())
                                .build())
                     .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("witnessy")
                    .flags(Flags.builder()
                               .partyName("Third witnessy")
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .details(List.of())
                               .build())
                    .build())))
            .applicantExperts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .partyName("First Name")
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .details(List.of())
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(Flags.builder()
                               .partyName("Second expert")
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .details(List.of())
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("experto")
                    .flags(Flags.builder()
                               .partyName("Third experto")
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .details(List.of())
                               .build())
                    .build())))
            .respondent1Witnesses(wrapElements(List.of(
                    PartyFlagStructure.builder()
                        .firstName("First")
                        .lastName("Name")
                        .flags(Flags.builder()
                                   .partyName("First Name")
                                   .roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                                   .details(List.of())
                                   .build())
                        .build(),
                    PartyFlagStructure.builder()
                        .firstName("Second")
                        .lastName("witness")
                        .flags(Flags.builder()
                                   .partyName("Second witness")
                                   .roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                                   .details(List.of())
                                   .build())
                        .build())))
            .respondent1Experts(wrapElements(List.of(
                    PartyFlagStructure.builder()
                        .firstName("First")
                        .lastName("Name")
                        .flags(Flags.builder()
                                   .partyName("First Name")
                                   .roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                                   .details(List.of())
                                   .build())
                        .build(),
                    PartyFlagStructure.builder()
                        .firstName("Second")
                        .lastName("expert")
                        .flags(Flags.builder()
                                   .partyName("Second expert")
                                   .roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                                   .details(List.of())
                                   .build())
                        .build())))
            .build();

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
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
            .respondent1(respondent1);

        caseFlagsInitialiser.initialiseMissingCaseFlags(builder);
        CaseData actual = builder.build();

        assertFlags(expected, actual, false);
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
                    Flags.builder()
                        .partyName("Mr. John Rambo")
                        .roleOnCase("Applicant 1")
                        .details(List.of()).build()).build())
            .applicant2(
                applicant2.toBuilder().flags(
                    Flags.builder()
                        .partyName("Company ltd")
                        .roleOnCase("Applicant 2")
                        .details(List.of()).build()).build())
            .applicant1LitigationFriend(
                applicant1LitFriend.toBuilder().flags(
                        Flags.builder()
                            .partyName("Jason Wilson")
                            .roleOnCase("Applicant 1 Litigation Friend")
                            .details(List.of()).build())
                    .build()
            )
            .applicant2LitigationFriend(
                applicant2LitFriend.toBuilder().flags(
                        Flags.builder()
                            .partyName("Jenny Carter")
                            .roleOnCase("Applicant 2 Litigation Friend")
                            .details(List.of()).build())
                    .build())
            .respondent1(
                respondent1.toBuilder().flags(
                    Flags.builder()
                        .partyName("Mr. Sole Trader")
                        .roleOnCase("Respondent 1")
                        .details(List.of()).build()).build())
            .respondent2(
                respondent2.toBuilder().flags(
                    Flags.builder()
                        .partyName("The Organisation")
                        .roleOnCase("Respondent 2")
                        .details(List.of()).build()).build())
            .applicantWitnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .partyName("First Name")
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .details(List.of())
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(Flags.builder()
                               .partyName("Second witness")
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .details(List.of())
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("witnessy")
                    .flags(Flags.builder()
                               .partyName("Third witnessy")
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .details(List.of())
                               .build())
                    .build())))
            .applicantExperts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .partyName("First Name")
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .details(List.of())
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(Flags.builder()
                               .partyName("Second expert")
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .details(List.of())
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("experto")
                    .flags(Flags.builder()
                               .partyName("Third experto")
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .details(List.of())
                               .build())
                    .build())))
            .respondent1Witnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .build())
                    .build())))
            .respondent1Experts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .build())
                    .build())))
            .respondent2Witnesses(wrapElements(List.of(PartyFlagStructure.builder()
                                                           .firstName("Third")
                                                           .lastName("witnessy")
                                                           .flags(Flags.builder()
                                                                      .details(wrapElements(List.of(FlagDetail.builder()
                                                                                                        .name("Flag name")
                                                                                                        .flagCode("123")
                                                                                                        .build())))
                                                                      .roleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS)
                                                                      .build())
                                                           .build())))
            .respondent2Experts(wrapElements(List.of(PartyFlagStructure.builder()
                                                         .firstName("Third")
                                                         .lastName("experto")
                                                         .flags(Flags.builder()
                                                                    .details(wrapElements(List.of(FlagDetail.builder()
                                                                                                      .name("Flag name")
                                                                                                      .flagCode("123")
                                                                                                      .build())))
                                                                    .roleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)
                                                                    .build()).build())))
            .build();

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
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
                    .flags(Flags.builder()
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(Flags.builder()
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .build())
                    .build())))
            .respondent1Experts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(Flags.builder()
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .build())
                    .build())))
            .respondent2Witnesses(wrapElements(List.of(PartyFlagStructure.builder()
                                                           .firstName("Third")
                                                           .lastName("witnessy")
                                                           .flags(Flags.builder()
                                                                      .roleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS)
                                                                      .details(wrapElements(List.of(FlagDetail.builder()
                                                                                                        .name("Flag name")
                                                                                                        .flagCode("123")
                                                                                                        .build())))
                                                                      .build())
                                                           .build())))
            .respondent2Experts(wrapElements(List.of(PartyFlagStructure.builder()
                                                         .firstName("Third")
                                                         .lastName("experto")
                                                         .flags(Flags.builder()
                                                                    .roleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)
                                                                    .details(wrapElements(List.of(FlagDetail.builder()
                                                                                                      .name("Flag name")
                                                                                                      .flagCode("123")
                                                                                                      .build())))
                                                                    .build()).build())))
            .respondent1(respondent1)
            .respondent2(respondent2);

        caseFlagsInitialiser.initialiseMissingCaseFlags(builder);
        CaseData actual = builder.build();

        assertFlags(expected, actual, true);
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
                    Flags.builder()
                        .partyName("Mr. John Rambo")
                        .roleOnCase("Applicant 1")
                        .details(List.of()).build()).build())
            .applicant2(
                applicant2.toBuilder().flags(
                    Flags.builder()
                        .partyName("Company ltd")
                        .roleOnCase("Applicant 2")
                        .details(List.of()).build()).build())
            .applicant1LitigationFriend(
                applicant1LitFriend.toBuilder().flags(
                        Flags.builder()
                            .partyName("Jason Wilson")
                            .roleOnCase("Applicant 1 Litigation Friend")
                            .details(List.of()).build())
                    .build()
            )
            .applicant2LitigationFriend(
                applicant2LitFriend.toBuilder().flags(
                        Flags.builder()
                            .partyName("Jenny Carter")
                            .roleOnCase("Applicant 2 Litigation Friend")
                            .details(List.of()).build())
                    .build())
            .respondent1(
                respondent1.toBuilder().flags(
                    Flags.builder()
                        .partyName("Mr. Sole Trader")
                        .roleOnCase("Respondent 1")
                        .details(List.of()).build()).build())
            .respondent2(
                respondent2.toBuilder().flags(
                    Flags.builder()
                        .partyName("The Organisation")
                        .roleOnCase("Respondent 2")
                        .details(List.of()).build()).build())
            .applicantWitnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("witnessy")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .build())
                    .build())))
            .applicantExperts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("experto")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .build())
                    .build())))
            .respondent1Witnesses(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .partyName("First Name")
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .details(wrapElements(List.of()))
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(Flags.builder()
                               .partyName("Second witness")
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
                               .details(wrapElements(List.of()))
                               .build())
                    .build())))
            .respondent1Experts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .partyName("First Name")
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .details(wrapElements(List.of()))
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(Flags.builder()
                               .partyName("Second expert")
                               .roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
                               .details(wrapElements(List.of()))
                               .build())
                    .build())))
            .respondent2Witnesses(wrapElements(List.of(PartyFlagStructure.builder()
                                                           .firstName("Third")
                                                           .lastName("witnessy")
                                                           .flags(Flags.builder()
                                                                      .partyName("Third witnessy")
                                                                      .roleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS)
                                                                      .details(wrapElements(List.of()))
                                                                      .build())
                                                           .build())))
            .respondent2Experts(wrapElements(List.of(PartyFlagStructure.builder()
                                                         .firstName("Third")
                                                         .lastName("experto")
                                                         .flags(Flags.builder()
                                                                    .partyName("Third experto")
                                                                    .roleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)
                                                                    .details(wrapElements(List.of()))
                                                                    .build()).build())))
            .build();

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
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
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("witness")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("witnessy")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_WITNESS)
                               .build())
                    .build()
            )))
            .applicantExperts(wrapElements(List.of(
                PartyFlagStructure.builder()
                    .firstName("First")
                    .lastName("Name")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Second")
                    .lastName("expert")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .build())
                    .build(),
                PartyFlagStructure.builder()
                    .firstName("Third")
                    .lastName("experto")
                    .flags(Flags.builder()
                               .details(wrapElements(List.of(FlagDetail.builder()
                                                                 .name("Flag name")
                                                                 .flagCode("123")
                                                                 .build())))
                               .roleOnCase(APPLICANT_SOLICITOR_EXPERT)
                               .build())
                    .build()
            )));

        caseFlagsInitialiser.initialiseMissingCaseFlags(builder);
        CaseData actual = builder.build();

        assertFlags(expected, actual, true);
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
