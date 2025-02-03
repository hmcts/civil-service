package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.acceptRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTimeNotBeingTakenOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTimeProcessedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseContainsLiP;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedPastHearingFeeDue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDetailsNotifiedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceNotProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isOneVOneResponseFlagSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimDetailsNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.rejectRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.responseDeadlinePassed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineBySystem;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawn;

class FlowPredicateTest {

    @Nested
    class PaymentSuccessful {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertTrue(paymentSuccessful.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedStateWithoutPaymentSuccessfulDate() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessfulWithoutPaymentSuccessDate().build();
            assertTrue(paymentSuccessful.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            assertFalse(paymentSuccessful.test(caseData));
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

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDetailsNotifiedTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            assertTrue(claimDetailsNotifiedTimeExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimDetailsNotifiedTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now())
                .build();
            assertFalse(claimDetailsNotifiedTimeExtension.test(caseData));
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

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimAcknowledgedRespondent1Extension1v2() {
            CaseData caseData =
                CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent2(Party.builder().partyName("Respondent2").build())
                    .respondent2SameLegalRepresentative(NO)
                    .build();
            assertTrue(notificationAcknowledged.and(respondentTimeExtension).test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimAcknowledgedRespondent2Extension1v2() {
            CaseData caseData =
                CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .respondent2(Party.builder().partyName("Respondent2").build())
                    .respondent2SameLegalRepresentative(NO)
                    .build();
            assertTrue(notificationAcknowledged.and(respondentTimeExtension).test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimDetailsNotified1v2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent2(Party.builder().partyName("Respondent2").build())
                .respondent2SameLegalRepresentative(NO)
                .build();
            assertFalse(notificationAcknowledged.and(respondentTimeExtension).test(caseData));
        }
    }

    @Nested
    class RespondentFullDefence {

        @Nested
        class TransitionFromClaimDetailsNotified {

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetailsTimeExtension() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetailsTimeExtension()
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(allResponsesReceived);
                assertTrue(predicate.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterBothNotifyClaimDetailsTimeExtension() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateTwoRespondentsFullDefenceAfterNotifyClaimDetailsTimeExtension()
                    .respondent2SameLegalRepresentative(NO)
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(allResponsesReceived);
                assertTrue(predicate.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterClaimNotifiedSpec() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .respondent1ResponseDate(LocalDateTime.now())
                    .caseAccessCategory(SPEC_CLAIM)
                    .build();
                assertTrue(fullDefenceSpec.test(caseData));
            }
        }

        @Nested
        class TransitionClaimDetailsNotifiedTimeExtension {

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetailsTimeExtension() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetailsTimeExtension()
                    .build();
                Predicate<CaseData> predicate = respondentTimeExtension.and(allResponsesReceived);
                assertTrue(predicate.test(caseData));
            }
        }

        @Nested
        class TransitionNotificationAcknowledged {

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterAcknowledgementTimeExtension() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterAcknowledgementTimeExtension()
                    .build();
                Predicate<CaseData> predicate = notificationAcknowledged.and(respondentTimeExtension).and(
                    allResponsesReceived);
                assertTrue(predicate.test(caseData));
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
                void shouldReturnFalse_whenPredicateDivergentRespondGoOfflineAndBothFullDefence2v1() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
                        .build();

                    assertFalse(divergentRespondGoOffline.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondWithDQGoOfflineBothNotFullDefence2v1() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(COUNTER_CLAIM)
                        .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                        .build();

                    assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondWithDQGoOfflineAndOneFullDefence2v1() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondGoOfflineAndBothFullDefence1v2SameSolicitor() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent2ClaimResponseType(FULL_DEFENCE)
                        .respondent2(Party.builder().partyName("Respondent2").build())
                        .respondent2SameLegalRepresentative(YES)
                        .addApplicant2(null)
                        .build();

                    assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondDQGoOfflineBothNotFullDefence1v2SameSolicitor() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(COUNTER_CLAIM)
                        .respondent2ClaimResponseType(PART_ADMISSION)
                        .respondent2(Party.builder().partyName("Respondent2").build())
                        .respondent2SameLegalRepresentative(YES)
                        .addApplicant2(null)
                        .build();

                    assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondWithDQGoOfflineAndOneFullDefence1v2SameSolicitor() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent2ClaimResponseType(FULL_ADMISSION)
                        .respondent2(Party.builder().partyName("Respondent2").build())
                        .respondent2SameLegalRepresentative(YES)
                        .addApplicant2(null)
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondGoOfflineAndBothFullDefence1v2DifferentRep() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent2ClaimResponseType(FULL_DEFENCE)
                        .respondent2(Party.builder().partyName("Respondent2").build())
                        .respondent2SameLegalRepresentative(NO)
                        .addApplicant2(null)
                        .build();

                    assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondWithDQGoOfflineBothNotFullDefence1v2DifferentRep() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(COUNTER_CLAIM)
                        .respondent2ClaimResponseType(PART_ADMISSION)
                        .respondent2(Party.builder().partyName("Respondent2").build())
                        .respondent2SameLegalRepresentative(NO)
                        .addApplicant2(null)
                        .build();

                    assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondWithDQGoOfflineAndOneFullDefence1v2DifferentRep() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                        .respondent1ClaimResponseType(FULL_DEFENCE)
                        .respondent2ClaimResponseType(FULL_ADMISSION)
                        .respondent2(Party.builder().partyName("Respondent2").build())
                        .respondent2SameLegalRepresentative(NO)
                        .respondent2ResponseDate(LocalDateTime.now().minusDays(1))
                        .addApplicant2(null)
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

            }

            @Nested
            class OneVTwoWithTwoReps {

                @BeforeEach
                void setup() {
                    caseDataBuilder = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .setClaimTypeToSpecClaim();
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
                void shouldReturnTrue_whenDefendant1RespondedWithFullAdmissionAndDefendant2RespondedWithCounterClaim() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim()
                        .build();

                    assertTrue(divergentRespondGoOffline.test(caseData));
                }

                @Test
                void awaitingResponsesFullDefenceReceivedRespondent1ShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .build();

                    assertTrue(awaitingResponsesFullDefenceReceived.test(caseData));
                }

                @Test
                void awaitingResponsesFullDefenceReceivedRespondent2ShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                        .build();

                    assertTrue(awaitingResponsesFullDefenceReceived.test(caseData));
                }

                @Test
                void awaitingResponsesNonFullDefenceRespondent1ReceivedShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondent1CounterClaimAfterNotifyDetails()
                        .build();

                    assertTrue(awaitingResponsesNonFullDefenceReceived.test(caseData));
                }

                @Test
                void awaitingResponsesNonFullDefenceRespondent2ReceivedShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateRespondent2CounterClaimAfterNotifyDetails()
                        .build();

                    assertTrue(awaitingResponsesNonFullDefenceReceived.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenPredicateDivergentRespondGoOfflineBothNotFullDefence1v2_2() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondent1v2AdmitAll_AdmitPart().build().toBuilder()
                        .respondent1ResponseDate(LocalDateTime.now())
                        .respondent2ResponseDate(LocalDateTime.now().plusHours(1))
                        .build();

                    assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondGoOffline_default() {
                    CaseData caseData = caseDataBuilder.build();
                    assertFalse(divergentRespondGoOfflineSpec.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenPredicateDivergentRespondWithDQAndGoOffline_default() {
                    CaseData caseData = CaseData.builder().build();
                    assertFalse(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                }

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
                    .atStateRespondent1CounterClaimAfterNotifyDetails()
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
                    .applicant1ProceedWithClaimMultiParty2v1(YES)
                    .applicant2ProceedWithClaimMultiParty2v1(NO)
                    .build();
                assertTrue(fullDefenceProceed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefence2v1SecondApplicantProceed() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoApplicants()
                    .applicant1ProceedWithClaimMultiParty2v1(NO)
                    .applicant2ProceedWithClaimMultiParty2v1(YES)
                    .build();
                assertTrue(fullDefenceProceed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2OneLR_ProceedVsRespondent1() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimOneDefendantSolicitor()
                    .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES)
                    .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO)
                    .build();
                assertTrue(fullDefenceProceed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2OneLR_ProceedVsRespondent2() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimOneDefendantSolicitor()
                    .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(NO)
                    .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YES)
                    .build();
                assertTrue(fullDefenceProceed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2TwoLR_ProceedVsRespondent1() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES)
                    .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO)
                    .build();
                assertTrue(fullDefenceProceed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2DiffSol_ProceedVsRespondent2() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(NO)
                    .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YES)
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
                    .applicant1ProceedWithClaim(NO)
                    .build();
                assertTrue(fullDefenceNotProceed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefence2v1BothApplicantsNotProceed() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoApplicants()
                    .applicant1ProceedWithClaimMultiParty2v1(NO)
                    .applicant2ProceedWithClaimMultiParty2v1(NO)
                    .build();
                assertTrue(fullDefenceNotProceed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2SameSol_ApplicantNotProceedAgainstBothDefendants() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimOneDefendantSolicitor()
                    .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(NO)
                    .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO)
                    .build();
                assertTrue(fullDefenceNotProceed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2DiffSol_ApplicantNotProceedAgainstBothDefendants() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(NO)
                    .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO)
                    .build();
                assertTrue(fullDefenceNotProceed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefence1v1AndApplicantNotProceedSpec() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .setClaimTypeToSpecClaim()
                    .applicant1ProceedWithClaim(NO)
                    .build();
                assertTrue(fullDefenceNotProceed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateFullDefence2v1BothApplicantsNotProceedSpec() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .setClaimTypeToSpecClaim()
                    .multiPartyClaimTwoApplicants()
                    .applicant1ProceedWithClaimSpec2v1(NO)
                    .build();
                assertTrue(fullDefenceNotProceed.test(caseData));
            }
        }

        @Nested
        class NoDefendantResponse {
            @Test
            void shouldReturnFalse_whenResponseDeadlineHasNotPassedAndNoDefendantResponse() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .respondent1ClaimResponseIntentionType(null)
                    .respondent1ResponseDeadline(LocalDateTime.now().plusDays(1))
                    .build();

                assertFalse(responseDeadlinePassed.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenResponseDeadlineHasPassedAndNoDefendantResponse() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .respondent1ClaimResponseIntentionType(null)
                    .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
                    .build();

                assertTrue(responseDeadlinePassed.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenResponseDeadlineHasNotPassedAndDefendantResponse() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .respondent1ResponseDate(LocalDateTime.now().minusDays(2))
                    .respondent1ResponseDeadline(LocalDateTime.now().plusDays(1))
                    .build();

                assertFalse(responseDeadlinePassed.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenResponseDeadlineHasPassedAndDefendantResponse() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .respondent1ResponseDate(LocalDateTime.now().minusDays(2))
                    .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
                    .build();

                assertFalse(responseDeadlinePassed.test(caseData));
            }
        }

        @Nested
        class ClaimTakenOfflineByStaff {

            @Test
            void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimNotified() {
                CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimNotified().build();
                assertTrue(takenOfflineByStaffAfterClaimNotified.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimDetailsNotified1v1() {
                CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotified().build();
                assertTrue(takenOfflineByStaffAfterClaimDetailsNotified.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimDetailsNotified1v2SameSolicitor() {
                CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotified()
                    .respondent2(Party.builder().partyName("Respondent 2").build())
                    .respondent2SameLegalRepresentative(YES)
                    .build();
                assertTrue(takenOfflineByStaffAfterClaimDetailsNotified.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataAtNotTakenOfflineAfterClaimDetailsNotified1v2SameSolicitor() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                    .build();
                assertFalse(takenOfflineByStaffAfterClaimDetailsNotified.test(caseData));
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

            @Test
            void shouldReturnTrue_whenTakenOfflineBySystem() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateTakenOfflinePastApplicantResponseDeadline()
                    .build();

                assertTrue(takenOfflineBySystem.test(caseData));
            }

        }

        @Nested
        class ClaimDismissed {

            @Test
            void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterClaimDetailsNotified() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDismissed()
                    .respondent1ResponseDate(null)
                    .respondent2ResponseDate(null)
                    .takenOfflineByStaffDate(null)
                    .build();
                assertTrue(caseDismissedAfterDetailNotified.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateClaimDismissedAfterClaimDetailsNotifiedExt() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDismissed()
                    .respondent1ResponseDate(null)
                    .respondent2ResponseDate(null)
                    .takenOfflineByStaffDate(LocalDateTime.now())
                    .build();
                assertFalse(caseDismissedAfterDetailNotified.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateApplicantRespondToDefence() {
                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                    .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                    .respondent1ResponseDate(null)
                    .respondent2ResponseDate(null)
                    .build();
                assertFalse(caseDismissedAfterDetailNotified.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateApplicantRespondToDefence_ext() {
                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                    .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                    .respondent1ResponseDate(LocalDateTime.now())
                    .respondent2ResponseDate(LocalDateTime.now())
                    .build();
                assertFalse(caseDismissedAfterDetailNotified.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateApplicantRespondToDefence_ext1() {
                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                    .claimDismissedDeadline(LocalDateTime.now().minusDays(2))
                    .respondent1ResponseDate(LocalDateTime.now())
                    .respondent2ResponseDate(LocalDateTime.now())
                    .build();
                assertFalse(caseDismissedAfterDetailNotified.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDismissedPastHearingFeeDue() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build();
                assertTrue(caseDismissedPastHearingFeeDue.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDismissedBeforeHearingFeeDue() {
                CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build();
                assertFalse(caseDismissedPastHearingFeeDue.test(caseData));
            }
        }

        @Nested
        class ApplicantOutOfTime {

            @Test
            void shouldReturnTrue_whenCaseDataIsAtStatePastApplicantResponseDeadline() {
                CaseData caseData = CaseDataBuilder.builder().atStatePastApplicantResponseDeadline().build();
                assertTrue(applicantOutOfTimeNotBeingTakenOffline.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataIsAtStateApplicantRespondToDefenceAndProceed() {
                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
                assertFalse(applicantOutOfTimeNotBeingTakenOffline.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenCaseDataPastApplicantResponseDeadlineButHasApplicantResponseDate() {
                CaseData caseData = CaseDataBuilder.builder().atStatePastApplicantResponseDeadline()
                    .applicant1ResponseDate(LocalDateTime.now().minusDays(1))
                    .build();
                assertFalse(applicantOutOfTimeNotBeingTakenOffline.test(caseData));
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

        @Nested
        class SpecOneVOneScenarios {

            CaseDataBuilder caseDataBuilder;

            @BeforeEach
            void setup() {
                caseDataBuilder = CaseDataBuilder.builder()
                    .setClaimTypeToSpecClaim();
            }

            @Test
            void shouldReturnTrue_whenDefendantResponseFullDefence() {
                CaseData caseData = caseDataBuilder.build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .respondent1ResponseDate(LocalDateTime.now())
                    .build();
                assertTrue(fullDefenceSpec.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenNoDefendantResponseFullDefence() {
                CaseData caseData = caseDataBuilder.build().toBuilder().build();
                assertFalse(fullDefenceSpec.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenAccessCategoryisNotSpecFullDefence() {
                CaseData caseData = caseDataBuilder.setClaimTypeToUnspecClaim().build().toBuilder().build();
                assertFalse(fullDefenceSpec.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenAccessCategoryisSpecFullDefence2v1SingleResponse() {
                CaseData caseData = caseDataBuilder
                    .addApplicant2()
                    .defendantSingleResponseToBothClaimants(YES)
                    .build()
                    .toBuilder()
                    .build();
                assertFalse(fullDefenceSpec.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenDefendantResponseFullAdmission() {
                CaseData caseData = caseDataBuilder.build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                    .respondent1ResponseDate(LocalDateTime.now())
                    .build();
                assertTrue(fullAdmissionSpec.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenNoDefendantResponseFullAdmission() {
                CaseData caseData = caseDataBuilder.build().toBuilder().build();
                assertFalse(fullAdmissionSpec.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenDefendantResponsePartAdmission() {
                CaseData caseData = caseDataBuilder.build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                    .respondent1ResponseDate(LocalDateTime.now())
                    .build();
                assertTrue(partAdmissionSpec.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenNoDefendantResponsePartAdmission() {
                CaseData caseData = caseDataBuilder.build().toBuilder().build();
                assertFalse(partAdmissionSpec.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenDefendantResponseCounterClaim() {
                CaseData caseData = caseDataBuilder.build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .respondent1ResponseDate(LocalDateTime.now())
                    .build();
                assertTrue(counterClaimSpec.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenNoDefendantResponseCounterClaim() {
                CaseData caseData = caseDataBuilder.build().toBuilder().build();
                assertFalse(counterClaimSpec.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenClaimTypeIsSpec_Claim() {
                CaseData caseData = caseDataBuilder.build().toBuilder().build();
                assertTrue(specClaim.test(caseData));
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
                        caseDataBuilder = CaseDataBuilder.builder()
                            .multiPartyClaimTwoApplicants()
                            .setClaimTypeToSpecClaim();
                    }

                    @Test
                    void shouldReturnTrue_whenPredicateDivergentRespondGoOfflineBothNotFullDefence2v1() {
                        CaseData caseData = caseDataBuilder
                            .atStateBothClaimantv1BothNotFullDefence_PartAdmissionX2().build().toBuilder()
                            .build();

                        assertTrue(divergentRespondGoOfflineSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnFalse_whenBothDefendentsRespondFullDefence2v1() {
                        CaseData caseData = caseDataBuilder
                            .atStateRespondent2v1FullDefence().build().toBuilder()
                            .build();

                        assertFalse(divergentRespondGoOfflineSpec.test(caseData));
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

                    @Test
                    void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2DiffSol_Proceed() {
                        CaseData caseData = CaseDataBuilder.builder()
                            .atStateApplicantRespondToDefenceAndProceed()
                            .setClaimTypeToSpecClaim()
                            .multiPartyClaimTwoDefendantSolicitors()
                            .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(NO)
                            .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YES)
                            .build();
                        assertTrue(fullDefenceProceed.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenCaseDataAtStateFullDefence1v2SameSol_or1v1_Proceed() {
                        CaseData caseData = CaseDataBuilder.builder()
                            .atStateApplicantRespondToDefenceAndProceed()
                            .setClaimTypeToSpecClaim()
                            .multiPartyClaimTwoDefendantSolicitors()
                            .applicant1ProceedWithClaim(YES)
                            .build();
                        assertTrue(fullDefenceProceed.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenCaseDataAtStateFullDefenceTwo_v_one_Proceed() {
                        CaseData caseData = CaseDataBuilder.builder()
                            .atStateApplicantRespondToDefenceAndProceed()
                            .setClaimTypeToSpecClaim()
                            .multiPartyClaimTwoApplicants()
                            .applicant1ProceedWithClaimSpec2v1(YES)
                            .build();
                        assertTrue(fullDefenceProceed.test(caseData));
                    }
                }

                @Nested
                class OneVTwoWithTwoReps {

                    @BeforeEach
                    void setup() {
                        caseDataBuilder = CaseDataBuilder.builder()
                            .multiPartyClaimTwoDefendantSolicitors()
                            .setClaimTypeToSpecClaim();
                    }

                    @Test
                    void shouldReturnTrue_whenPredicateDivergentRespondGoOfflineBothNotFullDefence1v2_1() {
                        CaseData caseData = caseDataBuilder
                            .atStateRespondent1v2AdmitAll_AdmitPart().build().toBuilder()
                            .respondent2ResponseDate(LocalDateTime.now())
                            .respondent1ResponseDate(LocalDateTime.now().plusHours(1))
                            .build();

                        assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenPredicateDivergentRespondGoOfflineBothNotFullDefence1v2_2() {
                        CaseData caseData = caseDataBuilder
                            .atStateRespondent1v2AdmitAll_AdmitPart().build().toBuilder()
                            .respondent1ResponseDate(LocalDateTime.now())
                            .respondent2ResponseDate(LocalDateTime.now().plusHours(1))
                            .build();

                        assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenPredicateDivergentRespondGoOfflineBothNotFullDefence1v2_3() {
                        LocalDateTime localDateTime = LocalDateTime.now();
                        CaseData caseData = caseDataBuilder
                            .atStateRespondent1v2AdmitAll_AdmitPart().build().toBuilder()
                            .respondent2ResponseDate(localDateTime)
                            .respondent1ResponseDate(localDateTime)
                            .build();

                        assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
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
                    void shouldGenerateDQAndGoOffline_whenNeitherDefendantRespondedWithFullDefence() {
                        CaseData caseData = caseDataBuilder
                            .atStateRespondent1v2FullAdmission().build().toBuilder()
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
                }

                @Nested
                class OneVTwoWithOneRep {

                    @BeforeEach
                    void setup() {
                        caseDataBuilder = CaseDataBuilder.builder()
                            .multiPartyClaimOneDefendantSolicitor()
                            .setClaimTypeToSpecClaim();
                    }

                    @Test
                    void shouldReturnTrue_whenDefendantsBothRespondedAndResponsesDivergent() {
                        CaseData caseData = caseDataBuilder
                            .atStateRespondent1v2AdmitAll_AdmitPart().build().toBuilder()
                            .respondentResponseIsSame(NO)
                            .build();

                        assertTrue(divergentRespondGoOfflineSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenDefendantsOnlyFirstRespondsFullDefenceAndResponsesDivergent() {
                        CaseData caseData = caseDataBuilder
                            .atStateRespondent1v2FullDefence_AdmitPart().build().toBuilder()
                            .respondentResponseIsSame(NO)
                            .build();

                        assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenDefendantsOnlySecondRespondsFullDefenceAndResponsesDivergent() {
                        CaseData caseData = caseDataBuilder
                            .atStateRespondent1v2AdmintPart_FullDefence().build().toBuilder()
                            .respondentResponseIsSame(NO)
                            .build();

                        assertTrue(divergentRespondWithDQAndGoOfflineSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenDefendantsBothRespondedFullDefenceAndResponsesTheSame() {
                        CaseData caseData = caseDataBuilder.build().toBuilder()
                            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                            .respondent1ResponseDate(LocalDateTime.now())
                            .respondentResponseIsSame(YES)
                            .build();

                        assertTrue(fullDefenceSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenDefendantsBothRespondedFullDefenceAndResponsesNotTheSame() {
                        CaseData caseData = caseDataBuilder.build().toBuilder()
                            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                            .respondent1ResponseDate(LocalDateTime.now())
                            .respondentResponseIsSame(NO)
                            .build();

                        assertTrue(fullDefenceSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenDefendantsBothRespondedFullAdmissionAndResponsesTheSame() {
                        CaseData caseData = caseDataBuilder.build().toBuilder()
                            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                            .respondent1ResponseDate(LocalDateTime.now())
                            .respondentResponseIsSame(YES)
                            .build();

                        assertTrue(fullAdmissionSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenDefendantsBothRespondedFullAdmissionAndResponsesNotTheSame() {
                        CaseData caseData = caseDataBuilder.build().toBuilder()
                            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                            .respondent1ResponseDate(LocalDateTime.now())
                            .respondentResponseIsSame(NO)
                            .build();

                        assertTrue(fullAdmissionSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenDefendantsBothRespondedPartAdmissionAndResponsesTheSame() {
                        CaseData caseData = caseDataBuilder.build().toBuilder()
                            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                            .respondent1ResponseDate(LocalDateTime.now())
                            .respondentResponseIsSame(YES)
                            .build();

                        assertTrue(partAdmissionSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnTrue_whenDefendantsBothRespondedFullCounterClaimAndResponsesNotTheSame() {
                        CaseData caseData = caseDataBuilder.build().toBuilder()
                            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                            .respondent1ResponseDate(LocalDateTime.now())
                            .respondentResponseIsSame(NO)
                            .build();

                        assertTrue(counterClaimSpec.test(caseData));
                    }

                    @Test
                    void shouldReturnFalse_whenOnlyOneResponse() {
                        CaseData caseData = caseDataBuilder.build().toBuilder()
                            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                            .respondent1ResponseDate(LocalDateTime.now())
                            .respondentResponseIsSame(NO)
                            .build();

                        assertFalse(fullDefenceSpec.test(caseData));
                    }
                }

                @Nested
                class TwoApplicants {

                    @BeforeEach
                    void setup() {
                        caseDataBuilder = CaseDataBuilder.builder()
                            .multiPartyClaimTwoApplicants()
                            .setClaimTypeToSpecClaim();
                    }

                    @Test
                    void shouldReturnTrue_whenResponsesToBothApplicants() {
                        CaseData caseData = caseDataBuilder.build().toBuilder()
                            .caseAccessCategory(SPEC_CLAIM)
                            .respondent1ResponseDate(LocalDateTime.now())
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

        @Nested
        class SDOTakenOffline {

            @Test
            void shouldReturnTrue_whenTakenOfflineAsSdoNotDrawn() {
                CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
                    .build();
                assertTrue(takenOfflineSDONotDrawn.test(caseData));
                assertFalse(takenOfflineAfterSDO.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenTakenOfflineAfterSdoDrawn() {
                CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
                    .build();
                assertFalse(takenOfflineSDONotDrawn.test(caseData));
                assertTrue(takenOfflineAfterSDO.test(caseData));
            }
        }

        @Nested
        class RepaymentScenarios {
            CaseDataBuilder caseDataBuilder;

            @BeforeEach
            void setup() {
                caseDataBuilder = CaseDataBuilder.builder()
                    .setClaimTypeToSpecClaim();
            }

            @Test
            void shouldReturnRejectTrueAcceptFalse_whenAcceptFullAdmitPaymentPlanSpecNo() {
                CaseData caseData = caseDataBuilder.build().toBuilder().applicant1AcceptFullAdmitPaymentPlanSpec(NO)
                    .build();
                assertTrue(rejectRepaymentPlan.test(caseData));
                assertFalse(acceptRepaymentPlan.test(caseData));
            }

            @Test
            void shouldReturnRejectTrueAcceptFalse_whenAcceptPartAdmitPaymentPlanSpecNo() {
                CaseData caseData = caseDataBuilder.build().toBuilder().applicant1AcceptPartAdmitPaymentPlanSpec(NO)
                    .build();
                assertTrue(rejectRepaymentPlan.test(caseData));
                assertFalse(acceptRepaymentPlan.test(caseData));
            }

            @Test
            void shouldReturnRejectFalseAcceptTrue_whenAcceptFullAdmitPaymentPlanSpecYes() {
                CaseData caseData = caseDataBuilder.build().toBuilder()
                    .applicant1AcceptFullAdmitPaymentPlanSpec(YES).build();
                assertFalse(rejectRepaymentPlan.test(caseData));
                assertTrue(acceptRepaymentPlan.test(caseData));
            }

            @Test
            void shouldReturnRejectFalseAcceptTrue_whenAcceptPartAdmitPaymentPlanSpecYes() {
                CaseData caseData = caseDataBuilder.build().toBuilder().applicant1AcceptPartAdmitPaymentPlanSpec(YES)
                    .build();
                assertFalse(rejectRepaymentPlan.test(caseData));
                assertTrue(acceptRepaymentPlan.test(caseData));
            }
        }

        @Nested
        class ContactDetails {

            CaseDataBuilder caseDataBuilder;

            @BeforeEach
            void setup() {
                caseDataBuilder = CaseDataBuilder.builder()
                    .atStateClaimIssued();
            }
        }

        @Nested
        class OneVOneResponseFlag {

            @Test
            void shouldReturnFalse_whenShowOneVOneResponseFlagExist() {
                CaseData caseData = CaseData.builder().build();

                assertFalse(isOneVOneResponseFlagSpec.test(caseData));
            }

            @Test
            void shouldReturnTrue_whenShowOneVOneResponseFlagNotExist() {
                CaseData caseData = CaseData.builder()
                    .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_FULL_DEFENCE).build();

                assertTrue(isOneVOneResponseFlagSpec.test(caseData));
            }
        }

        @Test
        public void isInHearingReadiness_whenHearingNoticeSubmitted() {
            CaseData caseData = CaseData.builder()
                .hearingReferenceNumber("11111")
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .build();

            assertTrue(isInHearingReadiness.test(caseData));
        }

        @Test
        public void isNotInHearingReadiness_whenHearingNoticeSubmitted() {
            CaseData caseData = CaseData.builder()
                .hearingReferenceNumber("11111")
                .listingOrRelisting(ListingOrRelisting.RELISTING)
                .build();

            assertFalse(isInHearingReadiness.test(caseData));
        }

        @Test
        void caseContainsLiP() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();

            assertFalse(caseContainsLiP.test(caseData));
        }
    }
}
