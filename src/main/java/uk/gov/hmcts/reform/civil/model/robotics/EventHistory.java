package uk.gov.hmcts.reform.civil.model.robotics;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.minidev.json.annotate.JsonIgnore;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class EventHistory {

    private List<Event> miscellaneous;
    private List<Event> acknowledgementOfServiceReceived;
    private List<Event> consentExtensionFilingDefence;
    private List<Event> defenceFiled;
    private List<Event> defenceAndCounterClaim;
    private List<Event> receiptOfPartAdmission;
    private List<Event> receiptOfAdmission;
    private List<Event> replyToDefence;
    private List<Event> directionsQuestionnaireFiled;
    private List<Event> breathingSpaceEntered;
    private List<Event> breathingSpaceLifted;
    private List<Event> breathingSpaceMentalHealthEntered;
    private List<Event> breathingSpaceMentalHealthLifted;
    private List<Event> interlocutoryJudgment;
    private List<Event> defaultJudgment;
    private List<Event> setAsideJudgment;
    private List<Event> statesPaid;
    private List<Event> judgmentByAdmission;
    private List<Event> generalFormOfApplication;
    private List<Event> defenceStruckOut;
    private List<Event> certificateOfSatisfactionOrCancellation;

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
