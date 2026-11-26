package uk.gov.hmcts.reform.civil.model.robotics;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.minidev.json.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class EventHistory {

    private List<Event> miscellaneous = new ArrayList<>();
    private List<Event> acknowledgementOfServiceReceived = new ArrayList<>();
    private List<Event> consentExtensionFilingDefence = new ArrayList<>();
    private List<Event> defenceFiled = new ArrayList<>();
    private List<Event> defenceAndCounterClaim = new ArrayList<>();
    private List<Event> receiptOfPartAdmission = new ArrayList<>();
    private List<Event> receiptOfAdmission = new ArrayList<>();
    private List<Event> replyToDefence = new ArrayList<>();
    private List<Event> directionsQuestionnaireFiled = new ArrayList<>();
    private List<Event> breathingSpaceEntered = new ArrayList<>();
    private List<Event> breathingSpaceLifted = new ArrayList<>();
    private List<Event> breathingSpaceMentalHealthEntered = new ArrayList<>();
    private List<Event> breathingSpaceMentalHealthLifted = new ArrayList<>();
    private List<Event> interlocutoryJudgment = new ArrayList<>();
    private List<Event> defaultJudgment = new ArrayList<>();
    private List<Event> setAsideJudgment = new ArrayList<>();
    private List<Event> statesPaid = new ArrayList<>();
    private List<Event> judgmentByAdmission = new ArrayList<>();
    private List<Event> generalFormOfApplication = new ArrayList<>();
    private List<Event> defenceStruckOut = new ArrayList<>();
    private List<Event> certificateOfSatisfactionOrCancellation = new ArrayList<>();

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
            interlocutoryJudgment,
            defaultJudgment,
            setAsideJudgment,
            statesPaid,
            judgmentByAdmission,
            generalFormOfApplication,
            defenceStruckOut,
            certificateOfSatisfactionOrCancellation
        );
        return eventsList.stream()
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .toList();
    }
}
