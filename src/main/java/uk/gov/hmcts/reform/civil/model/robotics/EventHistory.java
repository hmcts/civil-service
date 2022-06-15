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
    @Singular("acknowledgementOfServiceReceived")
    private List<Event> acknowledgementOfServiceReceived;
    @Singular("consentExtensionFilingDefence")
    private List<Event> consentExtensionFilingDefence;
    @Singular("defenceFiled")
    private List<Event> defenceFiled;
    @Singular("defenceAndCounterClaim")
    private List<Event> defenceAndCounterClaim;
    @Singular("receiptOfPartAdmission")
    private List<Event> receiptOfPartAdmission;
    @Singular("receiptOfAdmission")
    private List<Event> receiptOfAdmission;
    @Singular("replyDefence")
    private List<Event> replyToDefence;
    @Singular("directionsQuestionnaire")
    private List<Event> directionsQuestionnaireFiled;
    @Singular("breathingSpaceEntered")
    private List<Event> breathingSpaceEntered;
    @Singular("breathingSpaceLifted")
    private List<Event> breathingSpaceLifted;
    @Singular("breathingSpaceMentalHealthEntered")
    private List<Event> breathingSpaceMentalHealthEntered;
    @Singular("breathingSpaceMentalHealthLifted")
    private List<Event> breathingSpaceMentalHealthLifted;
    @Singular("statesPaid")
    private List<Event> statesPaid;

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
            directionsQuestionnaireFiled,
            breathingSpaceEntered,
            breathingSpaceLifted,
            breathingSpaceMentalHealthEntered,
            breathingSpaceMentalHealthLifted,
            statesPaid
        );
        return eventsList.stream()
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
