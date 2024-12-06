package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.buildRespondentResponseText;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartAdmissionBuilder extends BaseEventBuilder {

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(PART_ADMISSION);
    }

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildRespondentPartAdmission(builder, caseData);
    }

    private void buildRespondentPartAdmission(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText;
        List<Event> directionsQuestionnaireFiledEvents = new ArrayList<>();
        if (defendant1ResponseExists.test(caseData)) {
            final Party respondent1 = caseData.getRespondent1();
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
            LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();

            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && Objects.nonNull(caseData.getSpecDefenceAdmittedRequired())
                && caseData.getSpecDefenceAdmittedRequired().equals(YES)) {
                builder.statesPaid(buildDefenceFiledEvent(
                    builder,
                    respondent1ResponseDate,
                    RESPONDENT_ID,
                    true
                ));
            } else {
                builder.receiptOfPartAdmission(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID(RESPONDENT_ID)
                        .build()
                );
            }

            buildRespondentResponseText(builder, caseData, miscText, respondent1ResponseDate);

            directionsQuestionnaireFiledEvents.add(
                buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                    respondent1ResponseDate,
                    RESPONDENT_ID,
                    respondent1DQ,
                    respondent1,
                    true
                ));
            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                final Party respondent2 = caseData.getRespondent2();
                final Respondent1DQ respondent2DQ = caseData.getRespondent1DQ();
                LocalDateTime respondent2ResponseDate = null != caseData.getRespondent2ResponseDate()
                    ? caseData.getRespondent2ResponseDate() : caseData.getRespondent1ResponseDate();
                miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
                builder.receiptOfPartAdmission(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                        .dateReceived(respondent2ResponseDate)
                        .litigiousPartyID(RESPONDENT2_ID)
                        .build()
                );
                buildRespondentResponseText(builder, caseData, miscText, respondent2ResponseDate);
                directionsQuestionnaireFiledEvents.add(
                    buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                        respondent2ResponseDate,
                        RESPONDENT2_ID,
                        respondent2DQ,
                        respondent2,
                        true
                    ));
            }
        }
        if (defendant2ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
            Party respondent2 = caseData.getRespondent2();
            Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
            LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();
            builder.receiptOfPartAdmission(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID(RESPONDENT2_ID)
                    .build()
            );

            buildRespondentResponseText(builder, caseData, miscText, respondent2ResponseDate);

            directionsQuestionnaireFiledEvents.add(
                buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                    respondent2ResponseDate,
                    RESPONDENT2_ID,
                    respondent2DQ,
                    respondent2,
                    false
                ));
        }
        builder.clearDirectionsQuestionnaireFiled().directionsQuestionnaireFiled(directionsQuestionnaireFiledEvents);
    }
}
