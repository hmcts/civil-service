package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The General Application counterpart to {@link NotifierFactory}.
 *
 * <p>Kept separate rather than merged because the two hold different type arguments. Spring
 * resolves generics on array and collection injection, so each factory receives only the notifiers
 * parameterised for its own case data and neither needs an unchecked cast.</p>
 */
@Component
public class GaNotifierFactory {

    private final Map<String, Notifier<GeneralApplicationCaseData>> taskIdToNotifierMapping;

    @SafeVarargs
    public GaNotifierFactory(Notifier<GeneralApplicationCaseData>... notifiers) {
        this.taskIdToNotifierMapping = Arrays.stream(notifiers)
            .collect(Collectors.toMap(Notifier::getTaskId, notifier -> notifier));
    }

    public Notifier<GeneralApplicationCaseData> getNotifier(String taskId) {
        return taskIdToNotifierMapping.get(taskId);
    }
}
