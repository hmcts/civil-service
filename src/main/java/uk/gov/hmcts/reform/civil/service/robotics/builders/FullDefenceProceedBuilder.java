package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;
import uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.CLAIMANTS_PROCEED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.CLAIMANT_PROCEEDS;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.PROCEED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.RPA_REASON_MULTITRACK_UNSPEC_GOING_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.isStayClaim;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventDetailsText;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;

@Slf4j
@Component
@RequiredArgsConstructor
public class FullDefenceProceedBuilder extends BaseEventBuilder {

    private final LocationRefDataUtil locationRefDataUtil;

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(FULL_DEFENCE_PROCEED);
    }

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        String authToken = eventHistoryDTO.getAuthToken();
        buildFullDefenceProceed(builder, caseData, authToken);
    }

    private void buildFullDefenceProceed(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        List<ClaimantResponseDetails> applicantDetails = prepareApplicantsDetails(caseData);
        final List<String> miscEventText = prepMultipartyProceedMiscText(caseData);

        CaseCategory claimType = caseData.getCaseAccessCategory();
        String preferredCourtCode = claimType == SPEC_CLAIM ? getPreferredCourtCode(caseData.getApplicant1DQ()) : locationRefDataUtil.getPreferredCourtData(
            caseData, authToken, true);
        List<Event> dqEvents = applicantDetails.stream()
            .map(applicant -> prepareDqEvent(builder, applicant, preferredCourtCode))
            .toList();
        builder.directionsQuestionnaireFiled(dqEvents);

        YesOrNo applicant1MediationRequired = getMediationStatus(caseData.getApplicant1ClaimMediationSpecRequired());

        YesOrNo applicant2MediationRequired = getMediationStatus(caseData.getApplicantMPClaimMediationSpecRequired());

        String track = caseData.getResponseClaimTrack();

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_ONE:
                handleOneVOne(builder, caseData, claimType, track, applicant1MediationRequired, miscEventText);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                handleOneVTwoOneLegalRep(builder, caseData, claimType, track, applicant1MediationRequired, miscEventText);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                handleOneVTwoTwoLegalRep(builder, caseData, claimType, track, applicant1MediationRequired, miscEventText);
                break;
            case TWO_V_ONE:
                handleTwoVOne(builder, caseData, claimType, track, applicant1MediationRequired, applicant2MediationRequired, miscEventText);
                break;
            default:
        }
    }

    private Event prepareDqEvent(EventHistory.EventHistoryBuilder builder, ClaimantResponseDetails applicantDetail, String preferredCourtCode) {
        return Event.builder()
            .eventSequence(EventHistoryUtil.prepareEventSequence(builder.build()))
            .eventCode(DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
            .dateReceived(applicantDetail.getResponseDate())
            .litigiousPartyID(applicantDetail.getLitigiousPartyID())
            .eventDetails(EventDetails.builder()
                .stayClaim(isStayClaim(applicantDetail.getDq()))
                .preferredCourtCode(preferredCourtCode)
                .preferredCourtName("")
                .build())
            .eventDetailsText(prepareEventDetailsText(applicantDetail.getDq(), preferredCourtCode))
            .build();
    }

    private YesOrNo getMediationStatus(SmallClaimMedicalLRspec mediationSpec) {
        return (mediationSpec == null || mediationSpec.getHasAgreedFreeMediation() == null) ? NO : mediationSpec.getHasAgreedFreeMediation();
    }

    private void handleOneVOne(EventHistory.EventHistoryBuilder builder, CaseData caseData, CaseCategory claimType, String track,
                               YesOrNo applicant1MediationRequired, List<String> miscEventText) {
        YesOrNo respondent1MediationRequired = caseData.getResponseClaimMediationSpecRequired();

        if (isMediationRequired(applicant1MediationRequired, respondent1MediationRequired, claimType, track)) {
            handleMultipartyResponses(builder, caseData, miscEventText, new ArrayList<>());
        } else {
            handleMultipartyResponses(builder, caseData, new ArrayList<>(), List.of(CLAIMANT_PROCEEDS));
        }
    }

    private void handleOneVTwoOneLegalRep(EventHistory.EventHistoryBuilder builder, CaseData caseData, CaseCategory claimType, String track,
                                          YesOrNo applicant1MediationRequired, List<String> miscEventText) {
        YesOrNo proceedRespondent1 =
            caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2();
        YesOrNo proceedRespondent2 =
            caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2();
        YesOrNo respondent1MediationRequired = caseData.getResponseClaimMediationSpecRequired();

        if (NO.equals(proceedRespondent1) || NO.equals(proceedRespondent2)
            || (isMediationRequired(applicant1MediationRequired, respondent1MediationRequired, claimType, track))) {
            handleMultipartyResponses(builder, caseData, miscEventText, new ArrayList<>());
        } else {
            handleMultipartyResponses(builder, caseData, new ArrayList<>(), List.of(CLAIMANT_PROCEEDS));
        }
    }

    private void handleOneVTwoTwoLegalRep(EventHistory.EventHistoryBuilder builder, CaseData caseData, CaseCategory claimType,
                                          String track, YesOrNo applicant1MediationRequired, List<String> miscEventText) {
        YesOrNo proceedRespondent1 =
            caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2();
        YesOrNo proceedRespondent2 =
            caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2();
        YesOrNo respondent1MediationRequired = caseData.getResponseClaimMediationSpecRequired();
        YesOrNo respondent2MediationRequired = caseData.getResponseClaimMediationSpec2Required();

        if (NO.equals(proceedRespondent1) || NO.equals(proceedRespondent2)
            || (respondent2MediationRequired == YesOrNo.YES
            && isMediationRequired(applicant1MediationRequired, respondent1MediationRequired, claimType, track))
        ) {
            handleMultipartyResponses(builder, caseData, miscEventText, new ArrayList<>());
        } else {
            handleMultipartyResponses(builder, caseData, new ArrayList<>(), List.of(CLAIMANT_PROCEEDS));
        }
    }

    private void handleTwoVOne(EventHistory.EventHistoryBuilder builder, CaseData caseData, CaseCategory claimType, String track,
                               YesOrNo applicant1MediationRequired, YesOrNo applicant2MediationRequired, List<String> miscEventText) {
        YesOrNo applicant1Proceeds = caseData.getApplicant1ProceedWithClaimMultiParty2v1();
        YesOrNo applicant2Proceeds = caseData.getApplicant2ProceedWithClaimMultiParty2v1();
        YesOrNo respondent1MediationRequired = caseData.getResponseClaimMediationSpecRequired();

        if (NO.equals(applicant1Proceeds) || NO.equals(applicant2Proceeds)
            || (isMediationRequired(applicant1MediationRequired, respondent1MediationRequired, claimType, track)
            && applicant2MediationRequired == YesOrNo.YES)) {
            handleMultipartyResponses(builder, caseData, miscEventText, new ArrayList<>());
        } else {
            handleMultipartyResponses(builder, caseData, new ArrayList<>(), List.of(CLAIMANTS_PROCEED));
        }
    }

    private boolean isMediationRequired(YesOrNo applicantMediation, YesOrNo respondentMediation, CaseCategory claimType, String track) {
        return claimType == SPEC_CLAIM
            && AllocatedTrack.SMALL_CLAIM.name().equals(track)
            && applicantMediation == YesOrNo.YES
            && respondentMediation == YesOrNo.YES;
    }

    private void handleMultipartyResponses(EventHistory.EventHistoryBuilder builder, CaseData caseData, List<String> miscEventText, List<String> applicantProceedsText) {
        List<Event> miscText = prepareMiscEventList(builder, caseData, miscEventText);
        builder.miscellaneous(miscText);
        if (!applicantProceedsText.isEmpty()) {
            List<Event> applicantProceedMiscText = prepareMiscEventList(builder, caseData, applicantProceedsText);
            builder.miscellaneous(applicantProceedMiscText);
            buildTakenOfflineMultitrackUnspec(builder, caseData);
        }
    }

    private void buildTakenOfflineMultitrackUnspec(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            builder.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .eventDetailsText(RPA_REASON_MULTITRACK_UNSPEC_GOING_OFFLINE)
                    .eventDetails(EventDetails.builder()
                        .miscText(RPA_REASON_MULTITRACK_UNSPEC_GOING_OFFLINE)
                        .build())
                    .build());
        }
    }

    private List<String> prepMultipartyProceedMiscText(CaseData caseData) {
        List<String> eventDetailsText = new ArrayList<>();
        String currentTime = time.now().toLocalDate().toString();

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP:
                multipartyHandleOneVTwo(caseData, eventDetailsText, currentTime);
                break;
            case TWO_V_ONE:
                multipartyHandleTwoVOne(caseData, eventDetailsText, currentTime);
                break;
            case ONE_V_ONE:
            default: {
                eventDetailsText.add(EventHistoryUtil.RPA_REASON_CLAIMANT_PROCEEDS1);
            }
        }
        return eventDetailsText;
    }

    private static void multipartyHandleOneVTwo(CaseData caseData, List<String> eventDetailsText, String currentTime) {
        eventDetailsText.add(String.format(
            EventHistoryUtil.RPA_REASON_1_OF_2_S_CLAIMANT_HAS_PROVIDED_INTENTION_S_AGAINST_DEFENDANT_S,
            currentTime,
            YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                ? PROCEED
                : NOT_PROCEED,
            caseData.getRespondent1().getPartyName()
        ));
        eventDetailsText.add(String.format(
            EventHistoryUtil.RPA_REASON_2_OF_2_S_CLAIMANT_HAS_PROVIDED_INTENTION_S_AGAINST_DEFENDANT_S,
            currentTime,
            YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2())
                ? PROCEED
                : NOT_PROCEED,
            caseData.getRespondent2().getPartyName()
        ));
    }

    private static void multipartyHandleTwoVOne(CaseData caseData, List<String> eventDetailsText, String currentTime) {
        YesOrNo app1Proceeds;
        YesOrNo app2Proceeds;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            app1Proceeds = caseData.getApplicant1ProceedWithClaimSpec2v1();
            app2Proceeds = app1Proceeds;
        } else {
            app1Proceeds = caseData.getApplicant1ProceedWithClaimMultiParty2v1();
            app2Proceeds = caseData.getApplicant2ProceedWithClaimMultiParty2v1();
        }
        eventDetailsText.add(String.format(
            EventHistoryUtil.RPA_REASON_1_OF_2_S_CLAIMANT_S_HAS_PROVIDED_INTENTION_S,
            currentTime,
            caseData.getApplicant1().getPartyName(),
            YES.equals(app1Proceeds)
                ? PROCEED
                : NOT_PROCEED
        ));
        eventDetailsText.add(String.format(
            EventHistoryUtil.RPA_REASON_2_OF_2_S_CLAIMANT_S_HAS_PROVIDED_INTENTION_S,
            currentTime,
            caseData.getApplicant2().getPartyName(),
            YES.equals(app2Proceeds)
                ? PROCEED
                : NOT_PROCEED
        ));
    }
}
