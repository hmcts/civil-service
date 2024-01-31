package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import java.time.LocalDateTime;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@Data
@Builder(toBuilder = true)
public class BusinessProcess {

    private String processInstanceId;
    private BusinessProcessStatus status;
    private String activityId;
    private String camundaEvent;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime readyOn;

    public static BusinessProcess ready(CaseEvent caseEvent) {
        return BusinessProcess.builder().status(READY).camundaEvent(caseEvent.name()).readyOn(LocalDateTime.now()).build();
    }

    @JsonIgnore
    public boolean hasSameProcessInstanceId(String processInstanceId) {
        return this.getProcessInstanceId().equals(processInstanceId);
    }

    @JsonIgnore
    public BusinessProcessStatus getStatusOrDefault() {
        return ofNullable(this.getStatus()).orElse(READY);
    }

    @JsonIgnore
    public BusinessProcess start() {
        this.status = BusinessProcessStatus.STARTED;
        this.activityId = null;
        this.readyOn = null;
        return this;
    }

    @JsonIgnore
    public BusinessProcess updateProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    @JsonIgnore
    public BusinessProcess updateActivityId(String activityId) {
        this.activityId = activityId;
        return this;
    }

    @JsonIgnore
    public BusinessProcess reset() {
        this.activityId = null;
        this.processInstanceId = null;
        this.status = BusinessProcessStatus.FINISHED;
        this.readyOn = null;
        return this;
    }

    @JsonIgnore
    public boolean isFinished() {
        return this.status == BusinessProcessStatus.FINISHED;
    }
}
