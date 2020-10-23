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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.READY;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BusinessProcessService.class})
class BusinessProcessServiceTest {

    @Autowired
    private BusinessProcessService service;

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"FINISHED"})
    void shouldNotUpdateBusinessProcess_whenBusinessProcessStatusIsNotFinishedNorNull(BusinessProcessStatus status) {
        BusinessProcess businessProcess = buildBusinessProcessWithStatus(status);
        CaseData caseData = CaseData.builder()
            .businessProcess(businessProcess)
            .build();

        CaseData caseDataUpdated = service.updateBusinessProcess(caseData, CREATE_CLAIM);

        assertThat(caseDataUpdated.getBusinessProcess()).isEqualTo(businessProcess);
    }

    @ParameterizedTest
    @ArgumentsSource(GetBusinessProcessArguments.class)
    void shouldNotAddErrorAndUpdateBusinessProcess_whenBusinessProcessStatusFinishedOrNull(BusinessProcess
                                                                                               businessProcess) {
        CaseData caseData = CaseData.builder().businessProcess(businessProcess).build();

        CaseData caseDataUpdated = service.updateBusinessProcess(caseData, CREATE_CLAIM);

        assertThat(caseDataUpdated.getBusinessProcess()).extracting("status").isEqualTo(READY);
        assertThat(caseDataUpdated.getBusinessProcess()).extracting("camundaEvent").isEqualTo(CREATE_CLAIM.name());
        assertThat(caseDataUpdated.getBusinessProcess()).extracting("activityId").isNull();
        assertThat(caseDataUpdated.getBusinessProcess()).extracting("processInstanceId").isNull();

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
