package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.prepareApplicantsDetails;

@Component
@Order(30)
@RequiredArgsConstructor
public class MediationEventStrategy implements EventHistoryStrategy {

    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.hasDefendantAgreedToFreeMediation()
            && caseData.hasClaimantAgreedToFreeMediation();
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            List<ClaimantResponseDetails> applicantDetails = prepareApplicantsDetails(caseData);
            List<Event> dqEvents = IntStream.range(0, applicantDetails.size())
                .mapToObj(index -> buildDirectionsQuestionnaireEvent(builder, caseData, applicantDetails.get(index)))
                .toList();
            builder.directionsQuestionnaireFiled(dqEvents);
        }

        builder.miscellaneous(
            Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(resolveApplicantResponseDate(caseData))
                .eventDetailsText(textFormatter.inMediation())
                .eventDetails(EventDetails.builder()
                                  .miscText(textFormatter.inMediation())
                                  .build())
                .build()
        );
    }

    private Event buildDirectionsQuestionnaireEvent(EventHistory.EventHistoryBuilder builder,
                                                    CaseData caseData,
                                                    ClaimantResponseDetails claimantDetails) {
        String preferredCourtCode = RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode(caseData.getApplicant1DQ());
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
            .dateReceived(claimantDetails.getResponseDate())
            .litigiousPartyID(claimantDetails.getLitigiousPartyID())
            .eventDetails(EventDetails.builder()
                              .stayClaim(RoboticsDirectionsQuestionnaireSupport.isStayClaim(claimantDetails.getDq()))
                              .preferredCourtCode(preferredCourtCode)
                              .preferredCourtName("")
                              .build())
            .eventDetailsText(RoboticsDirectionsQuestionnaireSupport.prepareEventDetailsText(
                claimantDetails.getDq(),
                preferredCourtCode
            ))
            .build();
    }

    private LocalDateTime resolveApplicantResponseDate(CaseData caseData) {
        return timelineHelper.ensurePresentOrNow(caseData.getApplicant1ResponseDate());
    }
}
