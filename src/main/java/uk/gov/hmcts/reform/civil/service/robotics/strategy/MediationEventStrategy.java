package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.prepareApplicantsDetails;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDirectionsQuestionnaireEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

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
            List<Event> dqEvents = applicantDetails.stream().map(applicantDetail -> buildApplicantDirectionsQuestionnaireEvent(builder, caseData, applicantDetail))
                .toList();
            builder.directionsQuestionnaireFiled(dqEvents);
        }

        builder.miscellaneous(buildMiscEvent(
            builder,
            sequenceGenerator,
            textFormatter.inMediation(),
            resolveApplicantResponseDate(caseData)
        ));
    }

    private Event buildApplicantDirectionsQuestionnaireEvent(EventHistory.EventHistoryBuilder builder,
                                                             CaseData caseData,
                                                             ClaimantResponseDetails claimantDetails) {
        String preferredCourtCode = RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode(caseData.getApplicant1DQ());
        return buildDirectionsQuestionnaireEvent(
            builder,
            sequenceGenerator,
            claimantDetails.getResponseDate(),
            claimantDetails.getLitigiousPartyID(),
            claimantDetails.getDq(),
            preferredCourtCode,
            RoboticsDirectionsQuestionnaireSupport.prepareEventDetailsText(
                claimantDetails.getDq(),
                preferredCourtCode
            )
        );
    }

    private LocalDateTime resolveApplicantResponseDate(CaseData caseData) {
        return timelineHelper.ensurePresentOrNow(caseData.getApplicant1ResponseDate());
    }
}
