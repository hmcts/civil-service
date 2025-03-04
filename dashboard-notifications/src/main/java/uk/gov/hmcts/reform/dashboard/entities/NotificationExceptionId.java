package uk.gov.hmcts.reform.dashboard.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Embeddable
public class NotificationExceptionId implements Serializable {

    @Column(name = "reference")
    private String reference;

    @Column(name = "task_id")
    private String taskId;

}
