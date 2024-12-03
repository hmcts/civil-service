package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.MISC_TEXT_GRANTED_DJ;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.RPA_RECORD_JUDGMENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class MiscellaneousJudgmentEventBuilder {

    private final FeatureToggleService featureToggleService;

    public void buildMiscellaneousJudgmentEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                                Predicate<CaseData> grantedFlagPredicate, String miscTextRequested,
                                                String miscTextGranted, DynamicList details) {
        log.info("Building event: {} for case id: {} ", "MISCELLANEOUS_JUDGMENT", caseData.getCcdCaseReference());

        Boolean grantedFlag = grantedFlagPredicate.test(caseData);
        if (details != null) {
            if (miscTextGranted.equals(MISC_TEXT_GRANTED_DJ) && featureToggleService.isJOLiveFeedActive()
                    && caseData.getCcdState() == CaseState.All_FINAL_ORDERS_ISSUED) {
                miscTextGranted = RPA_RECORD_JUDGMENT;
            }

            builder.miscellaneous(
                Event.builder()
                    .eventSequence(EventHistoryUtil.prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(LocalDateTime.now())
                    .eventDetailsText(grantedFlag.booleanValue() ? miscTextRequested : miscTextGranted)
                    .eventDetails(EventDetails.builder()
                        .miscText(grantedFlag.booleanValue() ? miscTextRequested : miscTextGranted)
                        .build())
                    .build());
        }
    }
}


