package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTimeProcessedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceivedSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceivedSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.bothDefSameLegalRep;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledgedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedPastHearingFeeDue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.certificateOfServiceEnabled;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDetailsNotifiedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedBothRespondentUnrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedBothUnregisteredSolicitors;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneRespondentRepresentative;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneUnrepresentedDefendantOnly;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOnlyOneRespondentRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent1Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent2Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRegisteredRespondentRepresentatives;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRespondentRepresentativesOneUnregistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.contactDetailsChange;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isOneVOneResponseFlagSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.multipartyCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.oneVsOneCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimDetailsNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentFailed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pendingClaimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pinInPostEnabledAndLiP;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.rejectRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterNotSuitableForSdo;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimIssue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimantResponseBeforeSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterDefendantResponse;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffBeforeClaimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffBeforeMediationUnsuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineBySystem;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawn;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawnAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawnAfterNotificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension;

class FlowPredicateTest {

    @Nested
    class ClaimSubmittedOneRespondentRepresentative {

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
            assertFalse(certificateOfServiceEnabled.test(caseData));
        }

        @Test
        void shouldReturnFalse_cos_whenCaseDataAtClaimSubmittedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            assertFalse(certificateOfServiceEnabled.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedOneRespondentRepresentativeState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedOneRespondentRepresentative().build();
            assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
            assertFalse(certificateOfServiceEnabled.test(caseData));
        }

        @Test
        void shouldReturnFalse_cos_whenCaseDataAtClaimSubmittedOneRespondentRepresentativeState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedOneRespondentRepresentative().build();
            assertFalse(certificateOfServiceEnabled.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build();
            assertFalse(claimSubmittedOneRespondentRepresentative.test(caseData));
            assertFalse(certificateOfServiceEnabled.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(claimSubmittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseTakenOfflineBeforeIssue() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .takenOfflineByStaff()
                .build();
            assertTrue(takenOfflineByStaffBeforeClaimIssued.test(caseData));
        }
    }

    @Nested
    class ClaimSubmittedTwoRespondentRepresentatives {

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
                .respondent2Represented(YES)
                .respondent2OrgRegistered(YES)
                .build();
            assertTrue(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
            assertFalse(claimSubmittedBothUnregisteredSolicitors.test(caseData));
            assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesUnregisteredState() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered()
                .build();
            assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
            assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
            assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
            assertTrue(claimSubmittedBothUnregisteredSolicitors.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesSameSolicitorNullUnregistered() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered()
                .respondent2SameLegalRepresentative(null)
                .build();
            assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
            assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
            assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
            assertTrue(claimSubmittedBothUnregisteredSolicitors.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
            assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
            assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRepresentativesStateRespOneUnreg() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
                .respondent2Represented(YES)
                .respondent2OrgRegistered(YES)
                .respondent1OrgRegistered(NO)
                .respondent2SameLegalRepresentative(NO)
                .build();
            assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
            assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
            assertTrue(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRepresentativesStateRespTwoUnreg() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
                .respondent2Represented(YES)
                .respondent2OrgRegistered(NO)
                .respondent1OrgRegistered(YES)
                .respondent2SameLegalRepresentative(NO)
                .build();
            assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
            assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
            assertFalse(claimSubmittedOnlyOneRespondentRepresented.test(caseData));
            assertTrue(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedBothRepresentativesUnrepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
                .respondent1Represented(NO)
                .respondent2Represented(NO)
                .build();
            assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
            assertFalse(claimSubmittedOnlyOneRespondentRepresented.test(caseData));
            assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
            assertTrue(claimSubmittedBothRespondentUnrepresented.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedFirstRepresentativeUnrepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
                .respondent1Represented(YES)
                .respondent2Represented(NO)
                .build();
            assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
            assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
            assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
            assertTrue(claimSubmittedOnlyOneRespondentRepresented.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedSecondRepresentativeUnrepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives()
                .respondent1Represented(NO)
                .respondent2Represented(YES)
                .build();
            assertFalse(claimSubmittedTwoRegisteredRespondentRepresentatives.test(caseData));
            assertFalse(claimSubmittedBothRespondentUnrepresented.test(caseData));
            assertFalse(claimSubmittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
            assertTrue(claimSubmittedOnlyOneRespondentRepresented.test(caseData));
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

        // 1v1 Case / 1v2 Same Solicitor (Field is null)
        @Test
        void shouldBeClaimNotified_whenSolicitorOptions_isNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v1()
                .build();

            assertTrue(claimNotified.test(caseData));
        }

        //1v2 - Notify Both Sol
        @Test
        void shouldBeClaimNotified_when1v2DifferentSolicitor_andNotifySolicitorOptions_isBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyBothSolicitors()
                .build();

            assertTrue(claimNotified.test(caseData));
            assertFalse(takenOfflineAfterClaimNotified.test(caseData));
        }

        //1v2 - Notify One Sol
        @Test
        void shouldHandOffline_when1v2DifferentSolicitor_andNotifySolicitorOptions_isOneSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();

            assertFalse(claimNotified.test(caseData));
            assertTrue(takenOfflineAfterClaimNotified.test(caseData));
        }

        @Test
        void shouldHandOffline_when1v2DifferentSolicitor_andNotifySolicitorOptions_isBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();

            assertFalse(claimNotified.test(caseData));
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
    class RespondentUnrepresented {

        @Test
        void shouldReturnTrue_whenRespondent1NotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
            assertTrue(respondent1NotRepresented.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenRespondent1NotRepresentedPinToPostLR() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssuedUnrepresentedDefendant()
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .respondentCaseRole("Solicitor")
                                                   .build())
                .build();
            assertTrue(pinInPostEnabledAndLiP.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseNotificationState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            assertFalse(respondent1NotRepresented.test(caseData));
        }

        @Test
        void shouldResolve_whenOnlyOneUnrepresentedDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1UnrepresentedDefendant()
                .defendant1LIPAtClaimIssued(YES).build();

            assertTrue(certificateOfServiceEnabled.test(caseData));
            assertTrue(claimSubmittedOneUnrepresentedDefendantOnly.test(caseData));
            assertTrue(claimSubmittedRespondent1Unrepresented.test(caseData));
        }

        @Test
        void shouldResolve_whenFirstDefendantUnrepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendant1()
                .defendant1LIPAtClaimIssued(YES).build();

            assertTrue(certificateOfServiceEnabled.test(caseData));
            assertFalse(claimSubmittedOneUnrepresentedDefendantOnly.test(caseData));
            assertTrue(claimSubmittedRespondent1Unrepresented.test(caseData));
            assertFalse(claimSubmittedRespondent2Unrepresented.test(caseData));
        }

        @Test
        void shouldResolve_whenSecondDefendantUnrepresented() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssuedUnrepresentedDefendant2()
                .defendant2LIPAtClaimIssued(YES).build();

            assertTrue(certificateOfServiceEnabled.test(caseData));
            assertFalse(claimSubmittedOneUnrepresentedDefendantOnly.test(caseData));
            assertFalse(claimSubmittedRespondent1Unrepresented.test(caseData));
            assertTrue(claimSubmittedRespondent2Unrepresented.test(caseData));
        }

        @Test
        void shouldResolve_whenBothDefendantsUnrepresented() {
            CaseData caseData = CaseDataBuilder.builder()
                .defendant1LIPAtClaimIssued(YES)
                .defendant2LIPAtClaimIssued(YES)
                .atStateClaimIssuedUnrepresentedDefendants().build();

            assertTrue(certificateOfServiceEnabled.test(caseData));
            assertFalse(claimSubmittedOneUnrepresentedDefendantOnly.test(caseData));
            assertTrue(claimSubmittedRespondent1Unrepresented.test(caseData));
            assertTrue(claimSubmittedRespondent2Unrepresented.test(caseData));
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
        void shouldReturnTrue_whenCaseDataAtIssuedStateClaimIssuedPayment() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedPaymentFailed().build();
            assertTrue(paymentFailed.test(caseData));
        }

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
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotificationAcknowledgement() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build();
                Predicate<CaseData> predicate =
                    fullDefence.and(not(notificationAcknowledged.or(respondentTimeExtension)));
                assertFalse(predicate.test(caseData));
            }

            @Test
            void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotificationAcknowledgement1v2() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateTwoRespondentsFullDefenceAfterNotificationAcknowledgement().build().toBuilder()
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
                Predicate<CaseData> predicate = notificationAcknowledged.and(respondentTimeExtension).and(
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
                void shouldReturnTrue_whenBothDefendantsRespondedWithFullAdmission() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse()
                        .build();

                    assertTrue(fullAdmission.test(caseData));
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

                @Test
                void awaitingRespondent1ResponsesFullDefenceReceivedShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .build();

                    assertTrue(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
                }

                @Test
                void awaitingFirstDefendantResponsesNonFullDefenceReceivedShouldReturnTrue() {
                    CaseData caseData = caseDataBuilder
                        .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                        .build();

                    assertTrue(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
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
                        .respondentResponseIsSame(YES)
                        .build();

                    assertTrue(fullDefence.test(caseData));
                }

                @Test
                void shouldReturnTrue_whenDefendantsBothRespondedAndResponsesTheSameButMarkedDifferent() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .respondent2Responds(FULL_DEFENCE)
                        .respondentResponseIsSame(NO)
                        .build();

                    assertTrue(fullDefence.test(caseData));
                }

                @Test
                void shouldReturnFalse_whenDefendantsBothRespondedAndResponsesNotTheSame() {
                    CaseData caseData = caseDataBuilder
                        .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                        .respondent2Responds(PART_ADMISSION)
                        .respondentResponseIsSame(NO)
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

        @Test
        void shouldReturnTrue_whenTakenOfflineBeforeClaimantResponseAfterDefendantResponse1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .takenOfflineByStaff().build();
            assertTrue(takenOfflineByStaffAfterDefendantResponse.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineBeforeClaimantResponseAfterDefendantResponse1v2DS() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondentFullDefence()
                .respondent2Responds(FULL_DEFENCE)
                .takenOfflineByStaff().build();
            assertTrue(takenOfflineByStaffAfterDefendantResponse.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineBeforeClaimantResponseAfterDefendantResponse1v2SS() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateBothRespondentsSameResponse(FULL_DEFENCE)
                .takenOfflineByStaff().build();
            assertTrue(takenOfflineByStaffAfterDefendantResponse.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineBeforeClaimantResponseAfterDefendantResponse2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .atStateRespondentFullDefence()
                .takenOfflineByStaff().build();
            assertTrue(takenOfflineByStaffAfterDefendantResponse.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAfterClaimantResponseAfterDefendantResponse() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .takenOfflineByStaff().build();
            assertFalse(takenOfflineByStaffAfterDefendantResponse.test(caseData));
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
    class ClaimTakenOfflineByStaff {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimIssue() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();

            assertTrue(takenOfflineByStaffAfterClaimIssue.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimIssueSpec() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaff()
                .setClaimNotificationDate()
                .setClaimTypeToSpecClaim().build();

            assertTrue(takenOfflineByStaffAfterClaimIssue.test(caseData));
        }

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
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterNotificationAcknowledged1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledged()
                .respondent2(Party.builder().partyName("Respondent 2").build())
                .respondent2SameLegalRepresentative(YES)
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(1))
                .build();
            assertTrue(takenOfflineByStaffAfterNotificationAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateTakenOfflineAfterNotificationAcknowledged1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent1TimeExtension()
                .respondent2(Party.builder().partyName("Respondent 2").build())
                .respondent2SameLegalRepresentative(YES)
                .build();
            assertFalse(takenOfflineByStaffAfterNotificationAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterNotificationAcknowledgedExtension1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension().build();
            assertTrue(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterNotificationAcknowledgedExtension1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension()
                .respondent2(Party.builder().partyName("Respondent 2").build())
                .respondent2SameLegalRepresentative(YES)
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
                .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
                .build();
            assertTrue(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateTakenOfflineAfterNotificationAcknowledgedExtension1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent1TimeExtension()
                .respondent2(Party.builder().partyName("Respondent 2").build())
                .respondent2SameLegalRepresentative(YES)
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
                .build();
            assertFalse(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension.test(caseData));
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

        @Test
        void shouldReturnTrue_whenTakenOfflineByStaffAferClaimantResponseBeforeSdo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .takenOfflineByStaff()
                .build();

            assertTrue(takenOfflineByStaffAfterClaimantResponseBeforeSDO.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineByStaffAferClaimantResponseAfterSdo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .takenOfflineByStaff()
                .build().toBuilder()
                .drawDirectionsOrderRequired(YES).build();

            assertFalse(takenOfflineByStaffAfterClaimantResponseBeforeSDO.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineByStaffMediationUnsuccessful() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateMediationUnsuccessful(MultiPartyScenario.ONE_V_ONE)
                .takenOfflineByStaff()
                .build();

            assertFalse(takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineByStaffInMediation() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateMediationUnsuccessful(MultiPartyScenario.ONE_V_ONE)
                .takenOfflineByStaff()
                .build().toBuilder()
                .mediation(Mediation.builder().build())
                .build();

            assertTrue(takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
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
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledged_1v2DS() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged_1v2_BothDefendants()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterClaimAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledged_1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
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
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledgedExtensionRep1_1v2DS() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedTimeExtensionRespondent1_1v2DS()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledgedExtensionRep2_1v2DS() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedTimeExtensionRespondent2_1v2DS()
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

        @Test
        void shouldReturnTrue_whenPredicateOneVsOneCase() {
            CaseData caseData = caseDataBuilder.build().toBuilder().build();
            assertTrue(oneVsOneCase.test(caseData));
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

                @Test
                void shouldReturnTrue_whenPredicateMultipartyCase() {
                    CaseData caseData = caseDataBuilder.build().toBuilder().build();
                    assertTrue(multipartyCase.test(caseData));
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

                @Test
                void shouldReturnTrue_whenPredicateMultipartyCase() {
                    CaseData caseData = caseDataBuilder.build().toBuilder().build();
                    assertTrue(multipartyCase.test(caseData));
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

                @Test
                void shouldReturnTrue_whenPredicateMultipartyCase() {
                    CaseData caseData = caseDataBuilder.build().toBuilder().build();
                    assertTrue(multipartyCase.test(caseData));
                }
            }
        }
    }

    @Test
    public void testDisjoint() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .addRespondent2(NO)
            .build();

        Assertions.assertTrue(pendingClaimIssued.test(caseData));

        caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .addRespondent2(YES)
            .respondent2Represented(YES)
            .respondent2OrgRegistered(YES)
            .build();

        Assertions.assertTrue(pendingClaimIssued.test(caseData));
    }

    @Test
    public void when1v2ssIssued_thenPendingClaimIssued() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .respondent2(Party.builder().build())
            .respondent2Represented(YES)
            .respondent2SameLegalRepresentative(YES)
            .build();

        Assertions.assertTrue(pendingClaimIssued.test(caseData));
        Assertions.assertFalse(
            ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .and(bothDefSameLegalRep.negate()).test(caseData));
    }

    @Test
    public void when1v2dsIssued_thenPendingClaimIssued() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .respondent2(Party.builder().build())
            .respondent2Represented(YES)
            .respondent2SameLegalRepresentative(NO)
            .respondent2OrgRegistered(YES)
            .build();

        Assertions.assertTrue(pendingClaimIssued.test(caseData));
        Assertions.assertFalse(
            ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .and(bothDefSameLegalRep.negate()).test(caseData));
    }

    @Test
    public void whenXv1Issued_thenPendingClaimIssued() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .build();

        Assertions.assertTrue(pendingClaimIssued.test(caseData));
        Assertions.assertFalse(
            ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .and(bothDefSameLegalRep.negate()).test(caseData));
    }

    @Nested
    class AllAgreedToMediation {

        @Test
        public void whenUnspec_false() {
            CaseData caseData = CaseData.builder().build();
            Assertions.assertFalse(FlowPredicate.allAgreedToLrMediationSpec.test(caseData));
        }

        @Test
        public void whenNotSmall_false() {
            CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .build();
            Assertions.assertFalse(FlowPredicate.allAgreedToLrMediationSpec.test(caseData));
        }

        @Test
        public void when1v1() {
            CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .build();

            Map<YesOrNo[], Boolean> defClaim = Map.of(
                new YesOrNo[]{null, null, NO}, false,
                new YesOrNo[]{NO, NO, NO}, false,
                new YesOrNo[]{NO, YES, NO}, false,
                new YesOrNo[]{YES, NO, NO}, false,
                new YesOrNo[]{YES, NO, YES}, false,
                new YesOrNo[]{YES, YES, YES}, true
            );

            defClaim.forEach((whoAgrees, expected) -> {
                CaseData cd = caseData.toBuilder()
                    .responseClaimMediationSpecRequired(whoAgrees[0])
                    .applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                              .hasAgreedFreeMediation(whoAgrees[1])
                                                              .build())
                    .build();
                Assertions.assertEquals(expected, FlowPredicate.allAgreedToLrMediationSpec.test(cd));
            });
        }

        @Test
        public void when1v2ss() {
            CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .respondent2(Party.builder().build())
                .respondent2SameLegalRepresentative(YES)
                .build();

            Map<YesOrNo[], Boolean> defClaim = Map.of(
                new YesOrNo[]{null, null, NO}, false,
                new YesOrNo[]{NO, NO, NO}, false,
                new YesOrNo[]{NO, YES, NO}, false,
                new YesOrNo[]{YES, NO, NO}, false,
                new YesOrNo[]{YES, NO, YES}, false,
                new YesOrNo[]{YES, YES, YES}, true
            );

            defClaim.forEach((whoAgrees, expected) -> {
                CaseData cd = caseData.toBuilder()
                    .responseClaimMediationSpecRequired(whoAgrees[0])
                    .applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                              .hasAgreedFreeMediation(whoAgrees[1])
                                                              .build())
                    .build();
                Assertions.assertEquals(expected, FlowPredicate.allAgreedToLrMediationSpec.test(cd));
            });
        }

        @Test
        public void when1v2ds() {
            CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .respondent2(Party.builder().build())
                .respondent2SameLegalRepresentative(NO)
                .build();

            Map<YesOrNo[], Boolean> defClaim = Map.of(
                new YesOrNo[]{null, null, null, NO}, false,
                new YesOrNo[]{NO, NO, NO, NO}, false,
                new YesOrNo[]{NO, NO, YES, NO}, false,
                new YesOrNo[]{NO, YES, NO, NO}, false,
                new YesOrNo[]{NO, YES, YES, NO}, false,
                new YesOrNo[]{YES, NO, NO, NO}, false,
                new YesOrNo[]{YES, NO, YES, NO}, false,
                new YesOrNo[]{YES, YES, NO, NO}, false,
                new YesOrNo[]{YES, NO, NO, YES}, false,
                new YesOrNo[]{YES, YES, YES, YES}, true
            );

            defClaim.forEach((whoAgrees, expected) -> {
                CaseData cd = caseData.toBuilder()
                    .responseClaimMediationSpecRequired(whoAgrees[0])
                    .responseClaimMediationSpec2Required(whoAgrees[1])
                    .applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                              .hasAgreedFreeMediation(whoAgrees[2])
                                                              .build())
                    .build();
                Assertions.assertEquals(expected, FlowPredicate.allAgreedToLrMediationSpec.test(cd));
            });
        }

        @Test
        public void when2v1() {
            CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .build();

            Map<YesOrNo[], Boolean> defClaim = Map.of(
                new YesOrNo[]{null, null, null, NO}, false,
                new YesOrNo[]{NO, NO, NO, NO}, false,
                new YesOrNo[]{NO, NO, YES, NO}, false,
                new YesOrNo[]{NO, YES, NO, NO}, false,
                new YesOrNo[]{NO, YES, YES, NO}, false,
                new YesOrNo[]{YES, NO, NO, NO}, false,
                new YesOrNo[]{YES, NO, YES, NO}, false,
                new YesOrNo[]{YES, YES, NO, NO}, false,
                new YesOrNo[]{YES, NO, NO, YES}, false,
                new YesOrNo[]{YES, YES, YES, YES}, true
            );

            defClaim.forEach((whoAgrees, expected) -> {
                CaseData cd = caseData.toBuilder()
                    .responseClaimMediationSpecRequired(whoAgrees[0])
                    .applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                              .hasAgreedFreeMediation(whoAgrees[1])
                                                              .build())
                    .applicantMPClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                               .hasAgreedFreeMediation(whoAgrees[2])
                                                               .build())
                    .build();
                Assertions.assertEquals(expected, FlowPredicate.allAgreedToLrMediationSpec.test(cd));
            });
        }

        @Test
        void shouldReturnFalse_whenClaimantAgreedToLipMediation() {
            //Given
            CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .caseDataLiP(CaseDataLiP.builder()
                                 .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                                              .hasAgreedFreeMediation(MediationDecision.Yes)
                                                                              .build())
                                 .build())
                .build();
            //When
            boolean result = FlowPredicate.allAgreedToLrMediationSpec.test(caseData);
            //Then
            assertFalse(result);
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
        void shouldReturnTrue_whenTakenOfflineAsSdoNotDrawnAfterClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(MultiPartyScenario.ONE_V_ONE, true)
                .build();
            assertTrue(takenOfflineSDONotDrawnAfterClaimDetailsNotified.test(caseData));
            assertFalse(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterClaimDetailsNotifiedReasonInputMissing() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(MultiPartyScenario.ONE_V_ONE, false)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterClaimDetailsNotified.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterClaimDetailsNotified.test(caseData));
            assertTrue(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineAsSdoNotDrawnAfterClaimDetailsNotified1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, true)
                .build();
            assertTrue(takenOfflineSDONotDrawnAfterClaimDetailsNotified.test(caseData));
            assertFalse(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterClaimDetailsNotified1v2ReasonInputMissing() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, false)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterClaimDetailsNotified.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterClaimDetailsNotified1v2() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterClaimDetailsNotified.test(caseData));
            assertTrue(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineAsSdoNotDrawnAfterClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension(true)
                .build();
            assertTrue(takenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension.test(caseData));
            assertFalse(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterClaimDetailsNotifiedExtensionReasonInputMissing() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension(false)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension.test(caseData));
            assertTrue(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(MultiPartyScenario.ONE_V_ONE, true)
                .build();
            assertTrue(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
            assertFalse(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledgedReasonInputMissing() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(MultiPartyScenario.ONE_V_ONE, false)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
            assertTrue(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledged1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, true)
                .build();
            assertTrue(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
            assertFalse(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledged1v2ReasonInputMissing() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, false)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledged1v2() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
            assertTrue(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledgedTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_ONE, true)
                .build();
            assertTrue(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
            assertFalse(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledgedTimeExtensionReasonInputMissing() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_ONE, false)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledgedTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
            assertTrue(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledgedTimeExtension1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, true)
                .build();
            assertTrue(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
            assertFalse(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledgedTimeExtension1v2ReasonInputMissing() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, false)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledgedTimeExtension1v2() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                .build();
            assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
            assertTrue(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenTakenOfflineAfterSdoDrawn() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
                .build();
            assertFalse(takenOfflineSDONotDrawn.test(caseData));
            assertTrue(takenOfflineAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineByStaffAfterSdoDrawn() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterSDO(MultiPartyScenario.ONE_V_ONE)
                .build();
            assertFalse(takenOfflineSDONotDrawn.test(caseData));
            assertFalse(takenOfflineAfterSDO.test(caseData));
            assertTrue(takenOfflineByStaffAfterSDO.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenTakenOfflineByStaffAfterNotSuitableSdo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
                .takenOfflineByStaff()
                .build();
            assertFalse(takenOfflineSDONotDrawn.test(caseData));
            assertFalse(takenOfflineAfterSDO.test(caseData));
            assertTrue(takenOfflineAfterNotSuitableForSdo.test(caseData));
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

        @Test
        void shouldReturnTrue_whenContactDetailsChangedAlready() {
            CaseData caseData = caseDataBuilder.atSpecAoSApplicantCorrespondenceAddressRequired(NO).build();

            assertTrue(contactDetailsChange.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenContactDetailsNotYetChanged() {
            CaseData caseData = caseDataBuilder.atSpecAoSApplicantCorrespondenceAddressRequired(YES).build();

            assertFalse(contactDetailsChange.test(caseData));
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
}
