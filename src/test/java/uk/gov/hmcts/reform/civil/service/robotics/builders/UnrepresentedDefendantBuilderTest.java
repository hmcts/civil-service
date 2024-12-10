package uk.gov.hmcts.reform.civil.service.robotics.builders;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class UnrepresentedDefendantBuilderTest {

    @InjectMocks
    private UnrepresentedDefendantBuilder unrepresentedDefendantBuilder;

    @MockBean
    private Time time;

    @Test
    void buildEvent() {
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
        unrepresentedDefendantBuilder.buildEvent(eventHistoryDTO);

        assertThat(builder.build()).isNotNull();
        assertThat(builder.build())
            .extracting("miscellaneous")
            .asInstanceOf(InstanceOfAssertFactories.list(Event.class))
            .containsExactly(expectedEvent);
    }
}