package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestForReconsiderationDeadlineParamsBuilderTest {

    private RequestForReconsiderationDeadlineParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new RequestForReconsiderationDeadlineParamsBuilder();
    }

    @Test
    void shouldAddRequestForReconsiderationDeadlineWhenPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        LocalDateTime deadline = LocalDateTime.parse("2023-10-01T12:00:00");

        when(caseData.getRequestForReconsiderationDeadline()).thenReturn(deadline);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("requestForReconsiderationDeadline", deadline);
        assertThat(params).containsEntry("requestForReconsiderationDeadlineEn", "1 October 2023");
        assertThat(params).containsEntry("requestForReconsiderationDeadlineCy", "1 Hydref 2023");
    }

    @Test
    void shouldNotAddRequestForReconsiderationDeadlineWhenNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        when(caseData.getRequestForReconsiderationDeadline()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
