package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalTaskDTO {

    private String activityId;
    private String activityInstanceId;
    private String errorMessage;
    private String executionId;
    private String id;
    private Date lockExpirationTime;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processInstanceId;
    private String tenantId;
    private int retries;
    private Boolean suspended;
    private String workerId;
    private String topicName;
    private long priority;
}
