package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCasemanLR;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.apache.commons.lang3.StringUtils.left;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_STRUCK_OUT;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.GENERAL_FORM_OF_APPLICATION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;

@Slf4j
@Component
@RequiredArgsConstructor
public class TakenOfflineByStaffBuilder extends BaseEventBuilder {

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(TAKEN_OFFLINE_BY_STAFF);
    }

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();

        buildTakenOfflineByStaff(builder, caseData);
        buildGeneralFormApplicationEventsStrikeOutOrder(builder, caseData);
    }

    private void buildTakenOfflineByStaff(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineByStaffDate())
                .eventDetailsText(prepareTakenOfflineEventDetails(caseData))
                .eventDetails(EventDetails.builder()
                    .miscText(prepareTakenOfflineEventDetails(caseData))
                    .build())
                .build());
    }

    public String prepareTakenOfflineEventDetails(CaseData caseData) {
        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return left(format(
                "RPA Reason: Manually moved offline for reason %s on date %s.",
                prepareTakenOfflineByStaffReason(caseData.getClaimProceedsInCaseman()),
                caseData.getClaimProceedsInCaseman().getDate().format(ISO_DATE)
            ), 250); // Max chars allowed by Caseman
        } else {
            return left(format(
                "RPA Reason: Manually moved offline for reason %s on date %s.",
                prepareTakenOfflineByStaffReasonSpec(caseData.getClaimProceedsInCasemanLR()),
                caseData.getClaimProceedsInCasemanLR().getDate().format(ISO_DATE)
            ), 250); // Max chars allowed by Caseman
        }
    }

    private String prepareTakenOfflineByStaffReason(ClaimProceedsInCaseman claimProceedsInCaseman) {
        if (claimProceedsInCaseman.getReason() == ReasonForProceedingOnPaper.OTHER) {
            return claimProceedsInCaseman.getOther();
        }
        return claimProceedsInCaseman.getReason().name();
    }

    private String prepareTakenOfflineByStaffReasonSpec(ClaimProceedsInCasemanLR claimProceedsInCasemanLR) {
        if (claimProceedsInCasemanLR.getReason() == ReasonForProceedingOnPaper.OTHER) {
            return claimProceedsInCasemanLR.getOther();
        }
        return claimProceedsInCasemanLR.getReason().name();
    }

    private void buildGeneralFormApplicationEventsStrikeOutOrder(EventHistory.EventHistoryBuilder builder,
                                                                 CaseData caseData) {
        if (caseData.getGeneralApplications() != null) {
            var generalApplications = caseData
                .getGeneralApplications()
                .stream()
                .filter(application -> application.getValue().getGeneralAppType().getTypes().contains(STRIKE_OUT)
                    && getGeneralApplicationDetailsJudgeDecisionWithStruckOutDefence(
                    application.getValue()
                        .getCaseLink()
                        .getCaseReference(),
                    caseData
                )
                    != null)
                .toList();

            if (!generalApplications.isEmpty()) {
                buildGeneralFormOfApplicationStrikeOut(builder, generalApplications);
                buildDefenceStruckOutJudgmentEvent(builder, generalApplications);
            }
        }

    }

    private void buildGeneralFormOfApplicationStrikeOut(EventHistory.EventHistoryBuilder builder,
                                                        List<Element<GeneralApplication>> generalApplicationsStrikeOut) {

        List<Event> generalApplicationsEvents = IntStream.range(0, generalApplicationsStrikeOut.size())
            .mapToObj(index -> {
                String miscText = "APPLICATION TO Strike Out";
                return Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(GENERAL_FORM_OF_APPLICATION.getCode())
                    .dateReceived(generalApplicationsStrikeOut
                        .get(index)
                        .getValue()
                        .getGeneralAppSubmittedDateGAspec())
                    .litigiousPartyID(generalApplicationsStrikeOut
                        .get(index)
                        .getValue()
                        .getLitigiousPartyID())
                    .eventDetailsText(miscText)
                    .eventDetails(EventDetails.builder()
                        .miscText(miscText)
                        .build())
                    .build();
            })
            .toList();

        builder.generalFormOfApplication(generalApplicationsEvents);
    }

    private void buildDefenceStruckOutJudgmentEvent(EventHistory.EventHistoryBuilder builder,
                                                    List<Element<GeneralApplication>> generalApplicationsStrikeOut) {

        List<Event> generalApplicationsEvents = IntStream.range(0, generalApplicationsStrikeOut.size())
            .mapToObj(index ->
                 Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(DEFENCE_STRUCK_OUT.getCode())
                    .dateReceived(generalApplicationsStrikeOut
                        .get(index)
                        .getValue()
                        .getGeneralAppSubmittedDateGAspec())
                    .litigiousPartyID(generalApplicationsStrikeOut
                        .get(index)
                        .getValue()
                        .getLitigiousPartyID())
                    .build())
            .toList();

        builder.defenceStruckOut(generalApplicationsEvents);
    }

    private Element<GeneralApplicationsDetails> getGeneralApplicationDetailsJudgeDecisionWithStruckOutDefence(
        String caseLinkId, CaseData caseData) {
        return caseData.getGaDetailsMasterCollection().stream()
            .filter(generalApplicationsDetailsElement ->
                generalApplicationsDetailsElement
                    .getValue()
                    .getCaseLink()
                    .getCaseReference()
                    .equals(caseLinkId)
                    && generalApplicationsDetailsElement.getValue().getCaseState()
                    .equals(PROCEEDS_IN_HERITAGE.getDisplayedValue()))
            .findFirst()
            .orElse(null);
    }
}
