package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
public class GANotificationDeadline {

    private final String generalAppDeadlineNotification;

    @JsonCreator
    GANotificationDeadline(@JsonProperty("generalAppDeadlineNotification")
                               String generalAppDeadlineNotification) {
        this.generalAppDeadlineNotification = generalAppDeadlineNotification;
    }
}
