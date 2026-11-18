package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotifierFactory {

    private final Map<String, Notifier<CaseData>> taskIdToNotifierMapping;

    @SafeVarargs
    public NotifierFactory(Notifier<CaseData>... notifiers) {
        this.taskIdToNotifierMapping = Arrays.stream(notifiers)
            .collect(Collectors.toMap(Notifier::getTaskId, notifier -> notifier));
    }

    public Notifier<CaseData> getNotifier(String taskId) {
        return taskIdToNotifierMapping.get(taskId);
    }
}
