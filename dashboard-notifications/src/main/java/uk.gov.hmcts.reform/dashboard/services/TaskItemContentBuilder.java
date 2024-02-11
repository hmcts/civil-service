package uk.gov.hmcts.reform.dashboard.services;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskItemContentBuilder {

    public String buildTaskItemContent(StringSubstitutor stringSubstitutor,
                                        String category, String content, String title) {
        Optional<String> updatedContent = Optional.ofNullable(content).map(stringSubstitutor::replace);
        StringBuilder taskItem = new StringBuilder(category);
        taskItem.append(title);
        updatedContent.ifPresent(taskItem::append);
        return taskItem.toString();
    }
}
