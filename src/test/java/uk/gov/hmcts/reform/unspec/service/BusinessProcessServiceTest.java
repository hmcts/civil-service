package uk.gov.hmcts.reform.unspec.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.READY;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BusinessProcessService.class, JacksonAutoConfiguration.class})
class BusinessProcessServiceTest {

    @Autowired
    private BusinessProcessService service;

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"FINISHED"})
    void shouldAddErrorAndNotUpdateBusinessProcess_whenBusinessProcessStatusIsNotFinishedNorNull(BusinessProcessStatus
                                                                                          status) {
        BusinessProcess businessProcess = buildBusinessProcessWithStatus(status);
        Map<String, Object> data = new HashMap<>(Map.of("businessProcess", businessProcess));

        List<String> errors = service.updateBusinessProcess(data, CREATE_CLAIM);

        assertThat(errors).containsOnly("Business Process Error");
        assertThat(data).isEqualTo(Map.of("businessProcess", businessProcess));
    }

    @ParameterizedTest
    @ArgumentsSource(GetBusinessProcessArguments.class)
    void shouldNotAddErrorAndUpdateBusinessProcess_whenBusinessProcessStatusFinishedOrNull(BusinessProcess
                                                                                               businessProcess) {
        Map<String, Object> data = new HashMap<>();
        data.put("businessProcess", businessProcess);

        List<String> errors = service.updateBusinessProcess(data, CREATE_CLAIM);

        assertThat(errors).isEmpty();
        assertThat(data.get("businessProcess")).extracting("status").isEqualTo(READY);
        assertThat(data.get("businessProcess")).extracting("camundaEvent").isEqualTo(CREATE_CLAIM.name());
        assertThat(data.get("businessProcess")).extracting("activityId").isNull();
        assertThat(data.get("businessProcess")).extracting("processInstanceId").isNull();
    }

    static class GetBusinessProcessArguments implements ArgumentsProvider {

        @Override
        @SneakyThrows
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(buildBusinessProcessWithStatus(FINISHED)),
                Arguments.of(buildBusinessProcessWithStatus(null)),
                Arguments.of((BusinessProcess) null)
            );
        }
    }

    private static BusinessProcess buildBusinessProcessWithStatus(BusinessProcessStatus status) {
        return BusinessProcess.builder()
            .camundaEvent("someCamundaEvent")
            .activityId("someActivityId")
            .processInstanceId("someProcessInstanceId")
            .status(status)
            .build();
    }
}
