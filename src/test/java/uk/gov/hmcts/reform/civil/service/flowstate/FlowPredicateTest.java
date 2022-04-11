package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTimeProcessedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceivedSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceivedSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledgedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneRespondentRepresentative;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRespondentRepresentatives;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefence;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceNotProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimDetailsNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentFailed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pendingClaimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimIssue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension;

class FlowPredicateTest {

    @Nested
    class ClaimSubmittedOneRespondentRepresentative {

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedOneRespondentRepresentativeState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedOneRespondentRepresentative().build();
            assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build();
            assertFalse(claimSubmittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(claimSubmittedOneRespondentRepresentative.test(caseData));
        }
    }

    @Nested
    class ClaimSubmittedTwoRespondentRepresentatives {

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build();
            assertTrue(claimSubmittedTwoRespondentRepresentatives.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(claimSubmittedTwoRespondentRepresentatives.test(caseData));
        }
    }

    @Nested
    class ClaimNotified {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
            assertTrue(claimNotified.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(claimNotified.test(caseData));
        }

        @Test // 1v1 Case / 1v2 Same Solicitor (Field is null)
        void shouldBeClaimNotified_whenSolicitorOptions_isNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v1()
                .build();

            assertTrue(claimNotified.test(caseData));
        }

        @Test //1v2 - Notify Both Sol
        void shouldBeClaimNotified_when1v2DifferentSolicitor_andNotifySolicitorOptions_isBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyBothSolicitors()
                .build();

            assertTrue(claimNotified.test(caseData));
        }

        @Test //1v2 - Notify One Sol
        void shouldHandOffline_when1v2DifferentSolicitor_andNotifySolicitorOptions_isOneSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();

            assertTrue(takenOfflineAfterClaimNotified.test(caseData));
        }
    }

    @Nested
    class ClaimDetailsNotified {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            assertTrue(claimDetailsNotified.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
            assertFalse(claimDetailsNotified.test(caseData));
        }

        @Test
        void shouldBeClaimDetailsNotified_when1v2DifferentSolicitor_andNotifySolicitor_isBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();

            assertTrue(claimDetailsNotified.test(caseData));
        }

        @Test
        void shouldHandOffline_when1v2DifferentSolicitor_andNotifyDetailsSolicitor_isOneSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();

            assertTrue(takenOfflineAfterClaimDetailsNotified.test(caseData));
            assertFalse(claimDetailsNotified.test(caseData));
        }

        @Test
        void shouldBeClaimNotified_whenSolicitorOptions_isNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();

            assertTrue(claimDetailsNotified.test(caseData));
            assertFalse(takenOfflineAfterClaimDetailsNotified.test(caseData));
        }
    }

    @Nested
    class Respondent1NotRepresented {

        @Test
        void shouldReturnTrue_whenRespondentNotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
            assertTrue(respondent1NotRepresented.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseNotificationState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            assertFalse(respondent1NotRepresented.test(caseData));
        }
    }

    @Nested
    class Respondent1NotRegistered {

        @Test
        void shouldReturnTrue_whenRespondentNotRegistered() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnregisteredDefendant().build();
            assertTrue(respondent1OrgNotRegistered.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseNotificationState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            assertFalse(respondent1OrgNotRegistered.test(caseData));
        }
    }

    @Nested
    class Respondent2OrgNotRegistered {
        @Test
        void shouldReturnTrue_whenStateClaimSubmitted1v2Respondent2OrgNotRegistered() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2Respondent2OrgNotRegistered()
                .build();
            assertTrue(respondent2OrgNotRegistered.test(caseData));
        }
    }

    @Nested
    class PaymentFailed {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentFailed().build();
            assertTrue(paymentFailed.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            assertFalse(paymentFailed.test(caseData));
        }
    }

    @Nested
    class PaymentSuccessful {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertTrue(paymentSuccessful.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            assertFalse(paymentSuccessful.test(caseData));
        }
    }

    @Nested
    class PendingClaimIssued {

        @Test
        void shouldReturnTrue_whenCaseDataIsAtPendingClaimIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            assertTrue(pendingClaimIssued.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertFalse(pendingClaimIssued.test(caseData));
        }
    }

    @Nested
    class ClaimIssued {

        @Test
        void shouldReturnTrue_whenCaseDataIsAtClaimIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            assertTrue(claimIssued.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertFalse(claimIssued.test(caseData));
        }
    }

    @Nested
    class NotificationAcknowledged {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimRespondentOneAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            assertTrue(notificationAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimRespondentTwoAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent2Only()
                .build();
            assertTrue(notificationAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimBothAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent2()
                .build();
            assertTrue(notificationAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            assertFalse(notificationAcknowledged.test(caseData));
        }
    }

    @Nested
    class NotificationAcknowledgedTimeExtension {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimAcknowledged() {
            CaseData caseData =
                CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension().build();
            assertTrue(notificationAcknowledged.and(respondentTimeExtension).test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            assertFalse(notificationAcknowledged.and(respondentTimeExtension).test(caseData));
        }
    }

    @Nested
    class RespondentFullDefence {

        @Test
        void shouldReturnTrue_whenDefendantResponse() {
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseType(FULL_DEFENCE)
                .respondent1ResponseDate(LocalDateTime.now())
                .build();
            assertTrue(fullDefence.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenNoDefendantResponse() {
            CaseData caseData = CaseData.builder().build();
            assertFalse(fullDefence.test(caseData));
        }

        @Nested
        class TransitionFromClaimDetailsNotified {

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetails() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                    .build();
                Predicate<CaseData> predicate =
                    fullDefence.and(not(notificationAcknowledged.or(respondentTimeExtension)));
                assertTrue(predicate.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetailsTimeExtension() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetailsTimeExtension()
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(allResponsesReceived);
                assertFalse(predicate.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotificationAcknowledgement() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build();
                Predicate<CaseData> predicate =
                    fullDefence.and(not(notificationAcknowledged.or(respondentTimeExtension)));
                assertFalse(predicate.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterClaimNotifiedSpec() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .respondent1ResponseDate(LocalDateTime.now())
                    .superClaimType(SuperClaimType.SPEC_CLAIM)
                    .build();
                assertTrue(fullDefenceSpec.test(caseData));
            }
        }

        @Nested
        class TransitionClaimDetailsNotifiedTimeExtension {

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetailsTimeExtension() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetailsTimeExtension()
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(allResponsesReceived);
                assertFalse(predicate.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetails() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(not(notificationAcknowledged)).and(
                    fullDefence);
                assertFalse(predicate.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterAcknowledgementTimeExtension() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterAcknowledgementTimeExtension()
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(not(notificationAcknowledged)).and(
                    fullDefence);
                assertFalse(predicate.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotificationAcknowledgement() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(not(notificationAcknowledged)).and(
                    fullDefence);
                assertFalse(predicate.test(caseData));
            }
        }

        @Nested
        class TransitionNotificationAcknowledged {

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetailsTimeExtension() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetailsTimeExtension()
                    .build();
                Predicate<CaseData> predicate = notificationAcknowledged.and(not(respondentTimeExtension)).and(
                    fullDefence);
                assertFalse(predicate.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetails() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                    .build();
                Predicate<CaseData> predicate = notificationAcknowledged.and(not(respondentTimeExtension)).and(
                    fullDefence);
                assertFalse(predicate.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterAcknowledgementTimeExtension() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterAcknowledgementTimeExtension()
                    .build();
                Predicate<CaseData> predicate = notificationAcknowledged.and(not(respondentTimeExtension)).and(
                    allResponsesReceived);
                assertTrue(predicate.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterNotificationAcknowledgement() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build();
                Predicate<CaseData> predicate = notificationAcknowledged.and(not(respondentTimeExtension)).and(
                    fullDefence);
                assertTrue(predicate.test(caseData));
            }
        }

        @Nested
        class TransitionNotificationAcknowledgedTimeExtension {

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetailsTimeExtension() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetailsTimeExtension()
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(notificationAcknowledged).and(fullDefence);
                assertFalse(predicate.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetails() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(notificationAcknowledged).and(fullDefence);
                assertFalse(predicate.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotificationAcknowledgement() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(notificationAcknowledged).and(fullDefence);
                assertFalse(predicate.test(caseData));
            }
        }

        @Nested
        class MultiParty {
            CaseDataBuilder caseDataBuilder;

            @Nested
            class TwoVOne {

                @BeforeEach
                void setup() {
                    caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoApplicants();
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondGoOfflineBothNotFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(COUNTER_CLAIM)
                        .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                        .build();

                    assertTrue(divergentRespondGoOffline.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondGoOfflineAndOneFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                        .build();

                    assertFalse(divergentRespondGoOffline.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondGoOfflineAndBothFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                        .build();

                    assertFalse(divergentRespondGoOffline.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondWithDQGoOfflineBothNotFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(COUNTER_CLAIM)
                        .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                        .build();

                    assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondWithDQGoOfflineAndOneFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondWithDQGoOfflineAndBothFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                        .build();

                    assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateFullDefenceBothNotFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(COUNTER_CLAIM)
                        .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                        .build();

                    assertFalse(fullDefence.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateFullDefenceAndOneFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                        .build();

                    assertFalse(fullDefence.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateFullDefenceAndBothFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                        .build();

                    assertTrue(fullDefence.test(caseData));
                }

            }

            @Nested
            class OneVTwoWithTwoReps {

                @BeforeEach
                void setup() {
                    caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors();
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothResponded() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .respondent2Responds(FULL_DEFENCE)
                        .build();

                    assertTrue(fullDefence.test(caseData));
                }

                @Test
                void shouldGoOffline_whenDivergentRespondAndFirstResponseWithFullDefense() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse()
                        .respondent2Responds(PART_ADMISSION)
                        .build();

                    assertTrue(divergentRespondGoOffline.test(caseData));
                }

                @Test
                void shouldGenerateDQAndGoOffline_whenDivergentAndSecondDefendantRespondedWithFullDefence() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentPartAdmission()
                        .respondent2Responds(FULL_DEFENCE)
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendant1RespondedWithFullAdmissionAndDefendant2RespondedWithCounterClaim() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim()
                        .build();

                    assertTrue(divergentRespondGoOffline.test(caseData));
                }

                @Test
                void awaitingResponsesFullDefenceReceivedShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .build();

                    assertTrue(awaitingResponsesFullDefenceReceived.test(caseData));
                }

                @Test
                void awaitingResponsesFullDefenceReceivedShouldReturnFalse() {
                    CaseData caseData = caseDataBuilder
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentCounterClaimAfterNotifyDetails()
                        .build();

                    assertFalse(awaitingResponsesFullDefenceReceived.test(caseData));
                }

                @Test
                void awaitingResponsesNonFullDefenceReceivedShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentCounterClaimAfterNotifyDetails()
                        .build();

                    assertTrue(awaitingResponsesNonFullDefenceReceived.test(caseData));
                }

                @Test
                void awaitingResponsesNonFullDefenceReceivedShouldReturnFalse() {
                    CaseData caseData = caseDataBuilder
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .build();

                    assertFalse(awaitingResponsesNonFullDefenceReceived.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenBothDefendantsRespondedWithFullAdmission() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse()
                        .build();

                    assertTrue(fullAdmission.test(caseData));
                }
            }

            @Nested
            class OneVTwoWithOneRep {

                @BeforeEach
                void setup() {
                    caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimOneDefendantSolicitor();
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothRespondedAndResponsesTheSame() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .respondent2Responds(FULL_DEFENCE)
                        .respondentResponseIsSame(YesOrNo.YES)
                        .build();

                    assertTrue(fullDefence.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothRespondedAndResponsesTheSameButMarkedDifferent() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .respondent2Responds(FULL_DEFENCE)
                        .respondentResponseIsSame(YesOrNo.NO)
                        .build();

                    assertTrue(fullDefence.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenDefendantsBothRespondedAndResponsesNotTheSame() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .respondent2Responds(PART_ADMISSION)
                        .respondentResponseIsSame(YesOrNo.NO)
                        .build();

                    assertFalse(fullDefence.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenOnlyOneResponse() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .build();

                    assertFalse(fullDefence.test(caseData));
                }
            }

            @Nested
            class TwoApplicants {

                @BeforeEach
                void setup() {
                    caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoApplicants();
                }

                @Test
                void shouldReturnTrue_whenResponsesToBothApplicants() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                        .build();

                    assertTrue(fullDefence.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenDifferentResponses() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                        .build();

                    assertFalse(fullDefence.test(caseData));
                }
            }
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClosed() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDiscontinued().build();
            assertFalse(fullDefence.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();
            assertFalse(fullDefence.test(caseData));
        }
    }

    @Nested
    class RespondentFullAdmission {

        @Test
        void shouldReturnTrue_whenDefendantResponse() {
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseType(RespondentResponseType.FULL_ADMISSION)
                .respondent1ResponseDate(LocalDateTime.now())
                .build();
            assertTrue(fullAdmission.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenNoDefendantResponse() {
            CaseData caseData = CaseData.builder().build();
            assertFalse(fullAdmission.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullAdmissionAfterNotifyClaimDetails() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullAdmissionAfterNotifyDetails()
                .build();
            Predicate<CaseData> predicate =
                fullAdmission.and(not(notificationAcknowledged.or(respondentTimeExtension)));
            assertTrue(predicate.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateFullAdmissionAfterAcknowledgementTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullAdmissionAfterAcknowledgementTimeExtension()
                .build();
            Predicate<CaseData> predicate =
                fullAdmission.and(not(notificationAcknowledged.or(respondentTimeExtension)));
            assertFalse(predicate.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateFullAdmissionAfterNotificationAcknowledgement() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullAdmissionAfterNotificationAcknowledged()
                .build();
            Predicate<CaseData> predicate =
                fullAdmission.and(not(notificationAcknowledged.or(respondentTimeExtension)));
            assertFalse(predicate.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();
            assertFalse(fullAdmission.test(caseData));
        }
    }

    @Nested
    class RespondentPartAdmission {

        @Test
        void shouldReturnTrue_whenDefendantResponse() {
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseType(PART_ADMISSION)
                .respondent1ResponseDate(LocalDateTime.now())
                .build();
            assertTrue(partAdmission.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenNoDefendantResponse() {
            CaseData caseData = CaseData.builder().build();
            assertFalse(partAdmission.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStatePartAdmissionAfterNotifyClaimDetails() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentPartAdmissionAfterNotifyDetails()
                .build();
            Predicate<CaseData> predicate =
                partAdmission.and(not(notificationAcknowledged.or(respondentTimeExtension)));
            assertTrue(predicate.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStatePartAdmissionAfterAcknowledgementTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentPartAdmissionAfterAcknowledgementTimeExtension()
                .build();
            Predicate<CaseData> predicate =
                partAdmission.and(not(notificationAcknowledged.or(respondentTimeExtension)));
            assertFalse(predicate.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateFullAdmissionAfterNotificationAcknowledgement() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentPartAdmissionAfterNotificationAcknowledgement()
                .build();
            Predicate<CaseData> predicate =
                partAdmission.and(not(notificationAcknowledged.or(respondentTimeExtension)));
            assertFalse(predicate.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();
            assertFalse(partAdmission.test(caseData));
        }
    }

    @Nested
    class RespondentCounterClaim {

        @Test
        void shouldReturnTrue_whenDefendantResponse() {
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseType(RespondentResponseType.COUNTER_CLAIM)
                .respondent1ResponseDate(LocalDateTime.now())
                .build();
            assertTrue(counterClaim.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenNoDefendantResponse() {
            CaseData caseData = CaseData.builder().build();
            assertFalse(counterClaim.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateCounterClaimAfterNotifyClaimDetails() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentCounterClaimAfterNotifyDetails()
                .build();
            Predicate<CaseData> predicate =
                counterClaim.and(not(notificationAcknowledged.or(respondentTimeExtension)));
            assertTrue(predicate.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCounterClaimAfterAcknowledgementTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentCounterClaimAfterAcknowledgementTimeExtension()
                .build();
            Predicate<CaseData> predicate =
                counterClaim.and(not(notificationAcknowledged.or(respondentTimeExtension)));
            assertFalse(predicate.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCounterClaimAfterNotificationAcknowledgement() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentCounterClaim()
                .build();
            Predicate<CaseData> predicate =
                counterClaim.and(not(notificationAcknowledged.or(respondentTimeExtension)));
            assertFalse(predicate.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();
            assertFalse(counterClaim.test(caseData));
        }
    }

    @Nested
    class ApplicantRespondToDefence {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            assertTrue(fullDefenceProceed.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence2v1FirstApplicantProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .applicant1ProceedWithClaimMultiParty2v1(YesOrNo.YES)
                .applicant2ProceedWithClaimMultiParty2v1(YesOrNo.NO)
                .build();
            assertTrue(fullDefenceProceed.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence2v1SecondApplicantProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .applicant1ProceedWithClaimMultiParty2v1(YesOrNo.NO)
                .applicant2ProceedWithClaimMultiParty2v1(YesOrNo.YES)
                .build();
            assertTrue(fullDefenceProceed.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2OneLR_ProceedVsRespondent1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimOneDefendantSolicitor()
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.NO)
                .build();
            assertTrue(fullDefenceProceed.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2OneLR_ProceedVsRespondent2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimOneDefendantSolicitor()
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.NO)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.YES)
                .build();
            assertTrue(fullDefenceProceed.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2TwoLR_ProceedVsRespondent1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoDefendantSolicitors()
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.NO)
                .build();
            assertTrue(fullDefenceProceed.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2DiffSol_ProceedVsRespondent2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoDefendantSolicitors()
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.NO)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.YES)
                .build();
            assertTrue(fullDefenceProceed.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .build();
            assertFalse(fullDefenceProceed.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence1v1AndApplicantNotProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1ProceedWithClaim(YesOrNo.NO)
                .build();
            assertTrue(fullDefenceNotProceed.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence2v1BothApplicantsNotProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .applicant1ProceedWithClaimMultiParty2v1(YesOrNo.NO)
                .applicant2ProceedWithClaimMultiParty2v1(YesOrNo.NO)
                .build();
            assertTrue(fullDefenceNotProceed.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2SameSol_ApplicantNotProceedAgainstBothDefendants() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimOneDefendantSolicitor()
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.NO)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.NO)
                .build();
            assertTrue(fullDefenceNotProceed.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2DiffSol_ApplicantNotProceedAgainstBothDefendants() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoDefendantSolicitors()
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.NO)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.NO)
                .build();
            assertTrue(fullDefenceNotProceed.test(caseData));
        }
    }

    @Nested
    class ClaimTakenOfflineByStaff {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimIssue() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();

            assertTrue(takenOfflineByStaffAfterClaimIssue.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimNotified().build();
            assertTrue(takenOfflineByStaffAfterClaimNotified.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotified().build();
            assertTrue(takenOfflineByStaffAfterClaimDetailsNotified.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension()
                .build();
            assertTrue(takenOfflineByStaffAfterClaimDetailsNotifiedExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterNotificationAcknowledged()
                .build();
            assertTrue(takenOfflineByStaffAfterNotificationAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterNotificationAcknowledgedExtnesion() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension().build();
            assertTrue(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterDefendantResponse() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterDefendantResponse().build();
            assertTrue(takenOfflineByStaff.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataNotAtStateProceedsOffline() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            assertFalse(takenOfflineByStaff.test(caseData));
        }
    }

    @Nested
    class ClaimDismissed {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissed().build();
            assertTrue(caseDismissedAfterDetailNotified.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterClaimNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterDetailNotifiedExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterClaimNotifiedExtensionAndDef2Response() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotifiedTimeExtension_Defendent2()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterDetailNotifiedExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledged_1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledged_1v2DS() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged_1v2_BothDefendants()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterClaimAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledgedExtension_1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledgedExtension_1v2DS() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension_1v2DS()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateApplicantRespondToDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertFalse(caseDismissedAfterDetailNotified.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimAcknowledge() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            assertFalse(caseDismissedAfterClaimAcknowledged.test(caseData));
        }
    }

    @Nested
    class ApplicantOutOfTime {

        @Test
        void shouldReturnTrue_whenCaseDataIsAtStatePastApplicantResponseDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStatePastApplicantResponseDeadline().build();
            assertTrue(applicantOutOfTime.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtStateApplicantRespondToDefenceAndProceed() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            assertFalse(applicantOutOfTime.test(caseData));
        }
    }

    @Nested
    class ApplicantOutOfTimeProcessedByCamunda {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflinePastApplicantResponseDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflinePastApplicantResponseDeadline().build();
            assertTrue(applicantOutOfTimeProcessedByCamunda.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateApplicantRespondToDefenceAndProceed() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            assertFalse(applicantOutOfTimeProcessedByCamunda.test(caseData));
        }
    }

    @Nested
    class FailToNotifyClaim {

        @Test
        void shouldReturnTrue_whenCaseDataClaimDismissedPastClaimNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimNotificationDeadline().build();
            assertTrue(pastClaimNotificationDeadline.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateAwaitingCaseNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            assertFalse(pastClaimNotificationDeadline.test(caseData));
        }
    }

    @Nested
    class PastClaimDetailsNotificationDeadline {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedPastClaimDetailsNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimPastClaimDetailsNotificationDeadline()
                .build();
            assertTrue(pastClaimDetailsNotificationDeadline.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
            assertFalse(pastClaimDetailsNotificationDeadline.test(caseData));
        }
    }

    @Nested
    class DismissedByCamunda {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedPastClaimDetailsNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDismissedPastClaimDetailsNotificationDeadline()
                .build();
            assertTrue(claimDismissedByCamunda.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimPastClaimDetailsNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimPastClaimDetailsNotificationDeadline().build();
            assertFalse(claimDismissedByCamunda.test(caseData));
        }
    }

    @Test
    void shouldReturnTrue_whenStateClaimSubmitted1v2Respondent2OrgNotRegistered() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmitted1v2Respondent2OrgNotRegistered()
            .build();
        assertTrue(respondent2OrgNotRegistered.test(caseData));
    }

    @Nested
    class SpecOneVOneScenarios {

        @Test
        void shouldReturnTrue_whenDefendantResponseFullDefence() {
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .respondent1ResponseDate(LocalDateTime.now())
                .build();
            assertTrue(fullDefenceSpec.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenNoDefendantResponseFullDefence() {
            CaseData caseData = CaseData.builder().build();
            assertFalse(fullDefenceSpec.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenDefendantResponseFullAdmission() {
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .respondent1ResponseDate(LocalDateTime.now())
                .build();
            assertTrue(fullAdmissionSpec.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenNoDefendantResponseFullAdmission() {
            CaseData caseData = CaseData.builder().build();
            assertFalse(fullAdmissionSpec.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenDefendantResponsePartAdmission() {
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .respondent1ResponseDate(LocalDateTime.now())
                .build();
            assertTrue(partAdmissionSpec.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenNoDefendantResponsePartAdmission() {
            CaseData caseData = CaseData.builder().build();
            assertFalse(partAdmissionSpec.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenDefendantResponseCounterClaim() {
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                .respondent1ResponseDate(LocalDateTime.now())
                .build();
            assertTrue(counterClaimSpec.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenNoDefendantResponseCounterClaim() {
            CaseData caseData = CaseData.builder().build();
            assertFalse(counterClaimSpec.test(caseData));
        }
    }

    @Nested
    class RespondentFullDefenceSpec {

        @Nested
        class MultiParty {
            CaseDataBuilder caseDataBuilder;

            @Nested
            class TwoVOne {

                @BeforeEach
                void setup() {
                    caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoApplicants();
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondGoOfflineBothNotFullDefence2v1() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent2v1BothNotFullDefence_PartAdmissionX2().build().toBuilder()
                        .build();

                    assertTrue(divergentRespondGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondWithDQGoOfflineAndOnlyFirstDefendantFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent2v1FirstFullDefence_SecondPartAdmission().build().toBuilder()
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondWithDQGoOfflineAndOnlySecondDefendantFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent2v1SecondFullDefence_FirstPartAdmission().build().toBuilder()
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondWithDQGoOfflineBothNotFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent2v1BothNotFullDefence_PartAdmissionX2().build().toBuilder()
                        .build();

                    assertFalse(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondWithDQGoOfflineAndBothFullDefence() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .build();

                    assertFalse(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateFullDefenceBothNotFullDefence() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ResponseDate(LocalDateTime.now())
                        .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                        .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                        .build();

                    assertFalse(fullDefenceSpec.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateFullDefenceAndOneFullDefence() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ResponseDate(LocalDateTime.now())
                        .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                        .build();

                    assertFalse(fullDefenceSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateFullDefenceAndBothFullDefence() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ResponseDate(LocalDateTime.now())
                        .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .build();

                    assertTrue(fullDefenceSpec.test(caseData));
                }

            }

            @Nested
            class OneVTwoWithTwoReps {

                @BeforeEach
                void setup() {
                    caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors();
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondGoOfflineBothNotFullDefence1v2_1() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent1v2AdmitAll_AdmitPart().build().toBuilder()
                        .respondent2ResponseDate(LocalDateTime.now())
                        .respondent1ResponseDate(LocalDateTime.now().plusHours(1))
                        .build();

                    assertTrue(divergentRespondGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondGoOfflineBothNotFullDefence1v2_2() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent1v2AdmitAll_AdmitPart().build().toBuilder()
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondent2ResponseDate(LocalDateTime.now().plusHours(1))
                        .build();

                    assertTrue(divergentRespondGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondGoOfflineBothNotFullDefence1v2_3() {
                    LocalDateTime localDateTime = LocalDateTime.now();
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent1v2AdmitAll_AdmitPart().build().toBuilder()
                        .respondent2ResponseDate(localDateTime)
                        .respondent1ResponseDate(localDateTime)
                        .build();

                    assertTrue(divergentRespondGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondGoOffline_default() {
                    CaseData caseData = CaseData.builder().build();
                    assertFalse(divergentRespondGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldGenerateDQAndGoOffline_whenDivergentAndFirstefendantRespondedWithFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent1v2FullDefence_AdmitPart().build().toBuilder()
                        .respondent2ResponseDate(LocalDateTime.now())
                        .respondent1ResponseDate(LocalDateTime.now().plusHours(1))
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldGenerateDQAndGoOffline_whenDivergentAndSecondDefendantRespondedWithFullDefence() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent1v2AdmintPart_FullDefence().build().toBuilder()
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondent2ResponseDate(LocalDateTime.now().plusHours(1))
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondWithDQAndGoOffline_default() {
                    CaseData caseData = CaseData.builder().build();
                    assertFalse(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothResponded() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .build();

                    assertTrue(fullDefenceSpec.test(caseData));
                }

                @Test
                void awaitingRespondent2ResponsesFullDefenceReceivedShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .build();

                    assertTrue(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
                }

                @Test
                void awaitingRespondent1ResponsesFullDefenceReceivedShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .build();

                    assertTrue(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
                }

                @Test
                void awaitingResponsesFullDefenceReceivedShouldReturnFalse() {
                    CaseData caseData = caseDataBuilder
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .build();

                    assertFalse(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
                }

                @Test
                void awaitingSecondDefendantResponsesNonFullDefenceReceivedShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                        .build();

                    assertTrue(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
                }

                @Test
                void awaitingFirstDefendantResponsesNonFullDefenceReceivedShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                        .build();

                    assertTrue(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
                }

                @Test
                void awaitingResponsesNonFullDefenceReceivedShouldReturnFalse() {
                    CaseData caseData = caseDataBuilder
                        .build();

                    assertFalse(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
                }
            }

            @Nested
            class OneVTwoWithOneRep {

                @BeforeEach
                void setup() {
                    caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimOneDefendantSolicitor();
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothRespondedAndResponsesDivergent() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent1v2AdmitAll_AdmitPart().build().toBuilder()
                        .respondentResponseIsSame(YesOrNo.NO)
                        .build();

                    assertTrue(divergentRespondGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsOnlyFirstRespondsFullDefenceAndResponsesDivergent() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent1v2FullDefence_AdmitPart().build().toBuilder()
                        .respondentResponseIsSame(YesOrNo.NO)
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsOnlySecondRespondsFullDefenceAndResponsesDivergent() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent1v2AdmintPart_FullDefence().build().toBuilder()
                        .respondentResponseIsSame(YesOrNo.NO)
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothRespondedFullDefenceAndResponsesTheSame() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondentResponseIsSame(YesOrNo.YES)
                        .build();

                    assertTrue(fullDefenceSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothRespondedFullDefenceAndResponsesNotTheSame() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondentResponseIsSame(YesOrNo.NO)
                        .build();

                    assertTrue(fullDefenceSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothRespondedFullAdmissionAndResponsesTheSame() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondentResponseIsSame(YesOrNo.YES)
                        .build();

                    assertTrue(fullAdmissionSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothRespondedFullAdmissionAndResponsesNotTheSame() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                        .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondentResponseIsSame(YesOrNo.NO)
                        .build();

                    assertTrue(fullAdmissionSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothRespondedPartAdmissionAndResponsesTheSame() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondentResponseIsSame(YesOrNo.YES)
                        .build();

                    assertTrue(partAdmissionSpec.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothRespondedFullCounterClaimAndResponsesNotTheSame() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                        .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondentResponseIsSame(YesOrNo.NO)
                        .build();

                    assertTrue(counterClaimSpec.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenOnlyOneResponse() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondentResponseIsSame(YesOrNo.NO)
                        .build();

                    assertFalse(fullDefenceSpec.test(caseData));
                }

                @Test
                void awaitingResponsesFullDefenceReceivedShouldHitDefault() {
                    CaseData caseData = caseDataBuilder
                        .build();

                    assertFalse(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
                }

                @Test
                void awaitingResponsesNonFullDefenceReceivedShouldHitDefault() {
                    CaseData caseData = caseDataBuilder
                        .build();

                    assertFalse(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
                }
            }

            @Nested
            class TwoApplicants {

                @BeforeEach
                void setup() {
                    caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoApplicants();
                }

                @Test
                void shouldReturnTrue_whenResponsesToBothApplicants() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .build();

                    assertTrue(fullDefenceSpec.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenDifferentResponses() {
                    CaseData caseData = caseDataBuilder.build().toBuilder()
                        .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                        .build();

                    assertFalse(fullDefenceSpec.test(caseData));
                }
            }
        }
    }
}
