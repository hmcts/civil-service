package uk.gov.hmcts.reform.civil.model.robotics;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import net.minidev.json.annotate.JsonIgnore;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Singular("replyDefence")
    private List<Event> replyToDefence;
    @Singular("directionsQuestionnaire")
    private List<Event> directionsQuestionnaireFiled;

    @JsonIgnore
    public List<Event> flatEvents() {
        List<List<Event>> eventsList = Lists.newArrayList(
            miscellaneous,
            acknowledgementOfServiceReceived,
            consentExtensionFilingDefence,
            defenceFiled,
            defenceAndCounterClaim,
            receiptOfPartAdmission,
            receiptOfAdmission,
            replyToDefence,
            directionsQuestionnaireFiled
        );
        return eventsList.stream()
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
