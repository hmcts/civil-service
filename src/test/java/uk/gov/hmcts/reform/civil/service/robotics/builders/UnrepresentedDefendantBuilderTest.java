package uk.gov.hmcts.reform.civil.service.robotics.builders;

import org.assertj.core.api.InstanceOfAssertFactories;
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

@SpringBootTest(classes = {UnrepresentedDefendantBuilder.class})
class UnrepresentedDefendantBuilderTest {

    @Autowired
    private UnrepresentedDefendantBuilder unrepresentedDefendantBuilder;

    @MockBean
    private Time time;

    @Test
    public void buildEvent() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants().build();
        caseData = caseData.toBuilder()
            .respondent1Represented(NO)
            .respondent2(null).build();

        final Event expectedEvent = Event.builder()
            .eventSequence(1)
            .eventCode("999")
            .dateReceived(caseData.getSubmittedDate())
            .eventDetailsText("RPA Reason: Unrepresented defendant: Mr. Sole Trader")
            .eventDetails(EventDetails.builder()
                .miscText("RPA Reason: Unrepresented defendant: Mr. Sole Trader")
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        EventHistoryDTO eventHistoryDTO = EventHistoryDTO.builder().builder(builder).caseData(caseData).build();
        when(time.now()).thenReturn(ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime());
        unrepresentedDefendantBuilder.buildEvent(eventHistoryDTO);

        assertThat(builder.build()).isNotNull();
        assertThat(builder.build())
            .extracting("miscellaneous")
            .asInstanceOf(InstanceOfAssertFactories.list(Event.class))
            .containsExactly(expectedEvent);
    }
}