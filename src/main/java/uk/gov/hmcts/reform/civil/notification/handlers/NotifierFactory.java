package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotifierFactory {

    private final Map<String, Notifier> taskIdToNotifierMapping;

    public NotifierFactory(Notifier... notifiers) {
        this.taskIdToNotifierMapping = Arrays.stream(notifiers)
            .collect(Collectors.toMap(Notifier::getTaskId, notifier -> notifier));
    }

    public Notifier getNotifier(String taskId) {
        return taskIdToNotifierMapping.get(taskId);
    }
}
