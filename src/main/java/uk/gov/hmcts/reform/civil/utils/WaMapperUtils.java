package uk.gov.hmcts.reform.civil.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.civil.constants.WorkAllocationConstants;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sendandreply.MessageWaTaskDetails;
import uk.gov.hmcts.reform.civil.model.wa.AdditionalProperties;
import uk.gov.hmcts.reform.civil.model.wa.ClientContext;
import uk.gov.hmcts.reform.civil.model.wa.TaskData;
import uk.gov.hmcts.reform.civil.model.wa.UserTask;
import uk.gov.hmcts.reform.civil.model.wa.WaMapper;

import java.util.Base64;

@Slf4j
public class WaMapperUtils {

    private WaMapperUtils() {
        //no op
    }

    public static WaMapper getWaMapper(String clientContext) {
        if (clientContext != null) {
            log.info("clientContext is present");
            byte[] decodedBytes = Base64.getDecoder().decode(clientContext);
            String decodedString = new String(decodedBytes);
            try {
                return new ObjectMapper().readValue(decodedString, WaMapper.class);
            } catch (Exception ex) {
                log.error("Exception while parsing the Client-Context {}", ex.getMessage());
            }
        }
        return null;
    }

    public static MultiValueMap<String, String> createClientContext(WaMapper waMapper,
                                                                    CaseData caseData) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        MessageWaTaskDetails messageWaTaskDetails = caseData.getMessageWaTaskDetails();
        if (waMapper != null && messageWaTaskDetails != null) {
            ClientContext clientContext = waMapper.getClientContext() != null
                ? waMapper.getClientContext() : ClientContext.builder().build();
            UserTask userTask = clientContext.getUserTask() != null
                ? clientContext.getUserTask() : UserTask.builder().build();
            TaskData taskData = userTask.getTaskData() != null ? userTask.getTaskData() : TaskData.builder().build();
            waMapper = waMapper.toBuilder()
                .clientContext(clientContext.toBuilder()
                                   .userTask(userTask.toBuilder()
                                                 .taskData(taskData.toBuilder()
                                                               .id(messageWaTaskDetails.getTaskId())
                                                               .additionalProperties(AdditionalProperties.builder()
                                                                                         .messageId(
                                                                                             messageWaTaskDetails.getMessageID())
                                                                                         .build())
                                                               .build())
                                                 .completeTask(true)
                                                 .build())
                                   .build())
                .build();
            log.info("updated wa mapper " + waMapper);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(waMapper);
                byte[] encodedBytes = Base64.getEncoder().encode(jsonString.getBytes());
                String encodedString = new String(encodedBytes);
                log.info("encoded string wa mapper " + encodedString);
                multiValueMap.add(WorkAllocationConstants.CLIENT_CONTEXT_HEADER_PARAMETER, encodedString);

            } catch (Exception ex) {
                log.error("Exception while serializing the WaMapper object: {}", ex.getMessage());
            }
        }
        return multiValueMap;
    }
}
