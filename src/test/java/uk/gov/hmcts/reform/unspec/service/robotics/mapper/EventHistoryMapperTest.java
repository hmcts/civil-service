package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.robotics.Event;
import uk.gov.hmcts.reform.unspec.model.robotics.EventDetails;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    EventHistoryMapper.class
})
class EventHistoryMapperTest {

    @Autowired
    EventHistoryMapper mapper;

    @Test
    void shouldPrepareMiscellaneousEvent_whenClaimWithUnrepresentedDefendant() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
            .respondent1Represented(YesOrNo.NO)
            .build();
        Event expectedEvent = Event.builder()
            .eventSequence(1)
            .eventCode("999")
            .dateReceived(caseData.getClaimSubmittedDateTime().toLocalDate().format(ISO_DATE))
            .eventDetails(EventDetails.builder()
                              .miscText("RPA Reason: Unrepresented defendant.")
                              .build())
            .build();

        var events = mapper.buildEvents(caseData);

        assertThat(events).isNotNull();
        assertThat(events)
            .extracting("miscellaneous")
            .asList()
            .hasSize(1);
        assertThat(events)
            .extracting("miscellaneous")
            .asList()
            .containsExactly(expectedEvent);
    }
}
