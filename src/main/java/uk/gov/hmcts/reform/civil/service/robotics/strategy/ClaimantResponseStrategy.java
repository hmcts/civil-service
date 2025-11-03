package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.isStayClaim;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.prepareApplicantsDetails;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.prepareEventDetailsText;

@Component
@Order(45)
@RequiredArgsConstructor
public class ClaimantResponseStrategy implements EventHistoryStrategy {

    private static final Set<FlowState.Main> PROCEED_STATES = EnumSet.of(
        FlowState.Main.FULL_DEFENCE_PROCEED,
        FlowState.Main.FULL_DEFENCE_NOT_PROCEED
    );
    private static final String PROCEED_INTENTION = "proceed";
    private static final String NOT_PROCEED_INTENTION = "not proceed";
    private static final String CLAIMANTS_PROCEED_TEXT = "Claimants proceed.";

    private final IStateFlowEngine stateFlowEngine;
    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsTimelineHelper timelineHelper;
    private final LocationRefDataUtil locationRefDataUtil;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null || caseData.getApplicant1ResponseDate() == null) {
            return false;
        }
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return PROCEED_STATES.stream().anyMatch(state -> hasState(stateFlow, state));
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        boolean hasProceedState = hasState(stateFlow, FlowState.Main.FULL_DEFENCE_PROCEED);
        boolean hasNotProceedState = hasState(stateFlow, FlowState.Main.FULL_DEFENCE_NOT_PROCEED);

        if (hasProceedState) {
            addProceedEvents(builder, caseData, authToken);
        }
        if (hasNotProceedState) {
            addNotProceedEvent(builder, caseData);
        }
    }

    private void addProceedEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        List<ClaimantResponseDetails> claimantDetails = prepareApplicantsDetails(caseData);
        if (!claimantDetails.isEmpty()) {
            addDirectionsQuestionnaireEvents(builder, caseData, authToken, claimantDetails);
        }

        List<String> multipartyTexts = prepareMultipartyTexts(caseData);
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        CaseCategory claimType = caseData.getCaseAccessCategory();
        String track = caseData.getResponseClaimTrack();

        YesOrNo applicant1Mediation = resolveApplicant1Mediation(caseData);
        YesOrNo applicant2Mediation = resolveApplicant2Mediation(caseData);
        YesOrNo respondent1Mediation = resolveRespondent1Mediation(caseData);
        YesOrNo respondent2Mediation = resolveRespondent2Mediation(caseData);

        switch (scenario) {
            case ONE_V_ONE:
                boolean useMultipartyTexts = isSmallClaimMediation(claimType, track, respondent1Mediation, applicant1Mediation);
                handleProceedScenario(builder, caseData, useMultipartyTexts, multipartyTexts,
                                      List.of(textFormatter.claimantProceeds()));
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                YesOrNo proceedResp1 = caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2();
                YesOrNo proceedResp2 = caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2();
                boolean useMultiSameSolicitor = isAnyNo(proceedResp1, proceedResp2)
                    || isSmallClaimMediation(claimType, track, respondent1Mediation, applicant1Mediation);
                handleProceedScenario(builder, caseData, useMultiSameSolicitor, multipartyTexts,
                                      List.of(textFormatter.claimantProceeds()));
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                YesOrNo proceedRespOne = caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2();
                YesOrNo proceedRespTwo = caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2();
                boolean useMultiTwoSolicitors = isAnyNo(proceedRespOne, proceedRespTwo)
                    || isSmallClaimMediation(claimType, track, respondent1Mediation, applicant1Mediation, respondent2Mediation);
                handleProceedScenario(builder, caseData, useMultiTwoSolicitors, multipartyTexts,
                                      List.of(textFormatter.claimantProceeds()));
                break;
            case TWO_V_ONE:
                YesOrNo applicant1Proceeds = caseData.getApplicant1ProceedWithClaimMultiParty2v1();
                YesOrNo applicant2Proceeds = caseData.getApplicant2ProceedWithClaimMultiParty2v1();
                boolean useMultiTwoApplicants = isAnyNo(applicant1Proceeds, applicant2Proceeds)
                    || isSmallClaimMediation(claimType, track, respondent1Mediation, applicant1Mediation, applicant2Mediation);
                handleProceedScenario(builder, caseData, useMultiTwoApplicants, multipartyTexts,
                                      List.of(CLAIMANTS_PROCEED_TEXT));
                break;
            default:
                handleProceedScenario(builder, caseData, false, multipartyTexts,
                                      List.of(textFormatter.claimantProceeds()));
        }
    }

    private void handleProceedScenario(EventHistory.EventHistoryBuilder builder,
                                       CaseData caseData,
                                       boolean useMultipartyTexts,
                                       List<String> multipartyTexts,
                                       List<String> proceedTexts) {
        List<String> textsToApply = useMultipartyTexts ? multipartyTexts : proceedTexts;
        builder.miscellaneous(prepareMiscEvents(builder, caseData, textsToApply));
        if (!useMultipartyTexts) {
            addTakenOfflineForMultitrack(builder, caseData);
        }
    }

    private void addDirectionsQuestionnaireEvents(EventHistory.EventHistoryBuilder builder,
                                                  CaseData caseData,
                                                  String authToken,
                                                  List<ClaimantResponseDetails> claimantDetails) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            String preferredCourt = getPreferredCourtCode(caseData.getApplicant1DQ());
            List<Event> dqEvents = claimantDetails.stream()
                .map(detail -> Event.builder()
                    .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                    .eventCode(EventType.DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
                    .dateReceived(detail.getResponseDate())
                    .litigiousPartyID(detail.getLitigiousPartyID())
                    .eventDetails(EventDetails.builder()
                        .stayClaim(isStayClaim(detail.getDq()))
                        .preferredCourtCode(preferredCourt)
                        .preferredCourtName("")
                        .build())
                    .eventDetailsText(prepareEventDetailsText(detail.getDq(), preferredCourt))
                    .build())
                .toList();
            builder.directionsQuestionnaireFiled(dqEvents);
            return;
        }

        String preferredCourt = locationRefDataUtil.getPreferredCourtData(caseData, authToken, true);
        List<Event> dqEvents = claimantDetails.stream()
            .map(detail -> Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(EventType.DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
                .dateReceived(detail.getResponseDate())
                .litigiousPartyID(detail.getLitigiousPartyID())
                .eventDetails(EventDetails.builder()
                    .stayClaim(isStayClaim(detail.getDq()))
                    .preferredCourtCode(preferredCourt)
                    .preferredCourtName("")
                    .build())
                .eventDetailsText(prepareEventDetailsText(detail.getDq(), preferredCourt))
                .build())
            .toList();
        builder.directionsQuestionnaireFiled(dqEvents);
    }

    private void addNotProceedEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String message = getMultiPartyScenario(caseData).equals(TWO_V_ONE)
            ? textFormatter.claimantsIntendNotToProceed()
            : textFormatter.claimantIntendsNotToProceed();

        builder.miscellaneous(Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(timelineHelper.ensurePresentOrNow(caseData.getApplicant1ResponseDate()))
            .eventDetailsText(message)
            .eventDetails(EventDetails.builder().miscText(message).build())
            .build());
    }

    private List<String> prepareMultipartyTexts(CaseData caseData) {
        List<String> texts = new ArrayList<>();
        String dateStamp = timelineHelper.now().toLocalDate().toString();

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP -> {
                texts.add(textFormatter.formatRpa(
                    "[1 of 2 - %s] Claimant has provided intention: %s against defendant: %s",
                    dateStamp,
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        ? PROCEED_INTENTION
                        : NOT_PROCEED_INTENTION,
                    caseData.getRespondent1().getPartyName()
                ));
                texts.add(textFormatter.formatRpa(
                    "[2 of 2 - %s] Claimant has provided intention: %s against defendant: %s",
                    dateStamp,
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2())
                        ? PROCEED_INTENTION
                        : NOT_PROCEED_INTENTION,
                    caseData.getRespondent2().getPartyName()
                ));
            }
            case TWO_V_ONE -> {
                YesOrNo app1Proceeds = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                    ? caseData.getApplicant1ProceedWithClaimSpec2v1()
                    : caseData.getApplicant1ProceedWithClaimMultiParty2v1();
                YesOrNo app2Proceeds = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                    ? caseData.getApplicant1ProceedWithClaimSpec2v1()
                    : caseData.getApplicant2ProceedWithClaimMultiParty2v1();

                texts.add(textFormatter.formatRpa(
                    "[1 of 2 - %s] Claimant: %s has provided intention: %s",
                    dateStamp,
                    caseData.getApplicant1().getPartyName(),
                    YES.equals(app1Proceeds) ? PROCEED_INTENTION : NOT_PROCEED_INTENTION
                ));
                texts.add(textFormatter.formatRpa(
                    "[2 of 2 - %s] Claimant: %s has provided intention: %s",
                    dateStamp,
                    caseData.getApplicant2().getPartyName(),
                    YES.equals(app2Proceeds) ? PROCEED_INTENTION : NOT_PROCEED_INTENTION
                ));
            }
            default -> texts.add(textFormatter.claimantProceeds());
        }

        return texts;
    }

    private List<Event> prepareMiscEvents(EventHistory.EventHistoryBuilder builder,
                                          CaseData caseData,
                                          List<String> texts) {
        LocalDateTime defaultDate = timelineHelper.ensurePresentOrNow(caseData.getApplicant1ResponseDate());
        return texts.stream()
            .map(text -> Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(EventType.MISCELLANEOUS.getCode())
                .dateReceived(defaultDate)
                .eventDetailsText(text)
                .eventDetails(EventDetails.builder().miscText(text).build())
                .build())
            .toList();
    }

    private void addTakenOfflineForMultitrack(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            String message = textFormatter.multitrackUnspecOffline();
            builder.miscellaneous(Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(EventType.MISCELLANEOUS.getCode())
                .dateReceived(timelineHelper.ensurePresentOrNow(caseData.getApplicant1ResponseDate()))
                .eventDetailsText(message)
                .eventDetails(EventDetails.builder().miscText(message).build())
                .build());
        }
    }

    private boolean isSmallClaimMediation(CaseCategory claimType,
                                          String track,
                                          YesOrNo respondent1,
                                          YesOrNo applicant1) {
        return claimType == SPEC_CLAIM
            && AllocatedTrack.SMALL_CLAIM.name().equals(track)
            && respondent1 == YES
            && applicant1 == YES;
    }

    private boolean isSmallClaimMediation(CaseCategory claimType,
                                          String track,
                                          YesOrNo respondent1,
                                          YesOrNo applicant1,
                                          YesOrNo additionalParty) {
        return isSmallClaimMediation(claimType, track, respondent1, applicant1)
            && additionalParty == YES;
    }

    private YesOrNo resolveApplicant1Mediation(CaseData caseData) {
        return caseData.getApplicant1ClaimMediationSpecRequired() != null
            && caseData.getApplicant1ClaimMediationSpecRequired().getHasAgreedFreeMediation() != null
            ? caseData.getApplicant1ClaimMediationSpecRequired().getHasAgreedFreeMediation()
            : NO;
    }

    private YesOrNo resolveApplicant2Mediation(CaseData caseData) {
        return caseData.getApplicantMPClaimMediationSpecRequired() != null
            && caseData.getApplicantMPClaimMediationSpecRequired().getHasAgreedFreeMediation() != null
            ? caseData.getApplicantMPClaimMediationSpecRequired().getHasAgreedFreeMediation()
            : NO;
    }

    private YesOrNo resolveRespondent1Mediation(CaseData caseData) {
        return caseData.getResponseClaimMediationSpecRequired() != null
            ? caseData.getResponseClaimMediationSpecRequired()
            : NO;
    }

    private YesOrNo resolveRespondent2Mediation(CaseData caseData) {
        return caseData.getResponseClaimMediationSpec2Required() != null
            ? caseData.getResponseClaimMediationSpec2Required()
            : NO;
    }

    private boolean isAnyNo(YesOrNo first, YesOrNo second) {
        return NO.equals(first) || NO.equals(second);
    }

    private boolean hasState(StateFlow stateFlow, FlowState.Main state) {
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(state.fullName()::equals);
    }
}
