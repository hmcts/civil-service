package uk.gov.hmcts.reform.civil.service.robotics.builders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@SpringBootTest(classes = {TakenOfflineDueToDependantNocBuilder.class})
class TakenOfflineDueToDependantNocBuilderTest {

    @Autowired
    private TakenOfflineDueToDependantNocBuilder takenOfflineDueToDependantNocBuilder;

    @MockBean
    private Time time;

    @Test
    public void buildEvent() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants().build();
        caseData = caseData.toBuilder()
            .respondent1Represented(NO)
            .build();

        Event expectedEvent = Event.builder()
            .eventSequence(1)
            .eventCode("999")
            .dateReceived(caseData.getTakenOfflineDate())
            .eventDetailsText("RPA Reason : Notice of Change filed.")
            .eventDetails(EventDetails.builder()
                .miscText("RPA Reason : Notice of Change filed.")
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        EventHistoryDTO eventHistoryDTO = EventHistoryDTO.builder().builder(builder).caseData(caseData).build();
        takenOfflineDueToDependantNocBuilder.buildEvent(eventHistoryDTO);

        assertThat(builder.build()).isNotNull();
        assertThat(builder.build())
            .extracting("miscellaneous")
            .asList()
            .containsExactly(expectedEvent);
    }
}