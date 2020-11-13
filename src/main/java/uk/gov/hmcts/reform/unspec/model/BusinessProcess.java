package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.READY;

@Data
@Builder
public class BusinessProcess {

    private String processInstanceId;
    private BusinessProcessStatus status;
    private String activityId;
    private String camundaEvent;

    public static BusinessProcess ready(CaseEvent caseEvent) {
        return BusinessProcess.builder().status(READY).camundaEvent(caseEvent.name()).build();
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

        return this;
    }
}
