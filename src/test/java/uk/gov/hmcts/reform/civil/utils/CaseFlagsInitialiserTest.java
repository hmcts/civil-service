package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

}
