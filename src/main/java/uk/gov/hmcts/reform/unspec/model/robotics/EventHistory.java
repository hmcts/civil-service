package uk.gov.hmcts.reform.unspec.model.robotics;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class EventHistory {

    @Singular("miscellaneous")
    private List<Event> miscellaneous;
    @Builder.Default
    private List<Event> acknowledgementOfServiceReceived = List.of(Event.builder().build());
    @Builder.Default
    private List<Event> consentExtensionFilingDefence = List.of(Event.builder().build());
    @Builder.Default
    private List<Event> defenceFiled = List.of(Event.builder().build());
    @Builder.Default
    private List<Event> defenceAndCounterClaim = List.of(Event.builder().build());
    @Builder.Default
    private List<Event> receiptOfPartAdmission = List.of(Event.builder().build());
    @Builder.Default
    private List<Event> receiptOfAdmission = List.of(Event.builder().build());
    @Builder.Default
    private List<Event> replyToDefence = List.of(Event.builder().build());
    @Singular("directionsQuestionnaire")
    private List<Event> directionsQuestionnaireFiled;
}
