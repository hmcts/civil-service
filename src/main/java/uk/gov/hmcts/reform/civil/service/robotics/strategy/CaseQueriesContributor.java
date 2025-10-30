package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.time.LocalDateTime;

import java.util.EnumSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper.QUERIES_ON_CASE;

@Component
@Order(80)
@RequiredArgsConstructor
public class CaseQueriesContributor implements EventHistoryContributor {

    private static final Set<CaseState> OFFLINE_STATES = EnumSet.of(
        CaseState.CASE_DISMISSED,
        CaseState.PROCEEDS_IN_HERITAGE_SYSTEM,
        CaseState.CASE_DISCONTINUED
    );

    private final FeatureToggleService featureToggleService;
    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsTimelineHelper timelineHelper;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && isCaseOffline(caseData)
            && hasActiveQueries(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        LocalDateTime dateReceived = caseData.getTakenOfflineDate() != null
            ? caseData.getTakenOfflineDate()
            : timelineHelper.now();

        builder.miscellaneous(
            Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(EventType.MISCELLANEOUS.getCode())
                .dateReceived(dateReceived)
                .eventDetailsText(QUERIES_ON_CASE)
                .eventDetails(EventDetails.builder().miscText(QUERIES_ON_CASE).build())
                .build()
        );
    }

    private boolean hasActiveQueries(CaseData caseData) {
        if (featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            return caseData.getQueries() != null;
        }
        return caseData.getQmApplicantSolicitorQueries() != null
            || caseData.getQmRespondentSolicitor1Queries() != null
            || caseData.getQmRespondentSolicitor2Queries() != null;
    }

    private boolean isCaseOffline(CaseData caseData) {
        if (caseData.getTakenOfflineDate() != null) {
            return true;
        }
        return caseData.getCcdState() != null && OFFLINE_STATES.contains(caseData.getCcdState());
    }
}
