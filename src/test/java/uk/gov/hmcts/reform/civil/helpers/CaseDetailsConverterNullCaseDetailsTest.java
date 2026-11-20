package uk.gov.hmcts.reform.civil.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseDetailsConverterNullCaseDetailsTest {

    private final CaseDetailsConverter converter =
        new CaseDetailsConverter(new ObjectMapper().registerModule(new JavaTimeModule()));

    @Test
    void toCaseDataShouldThrowNullPointerExceptionWhenCaseDetailsIsNull() {
        assertThatThrownBy(() -> converter.toCaseData((CaseDetails) null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("caseDetails must not be null");
    }

    @Test
    void toBaseCaseDataShouldThrowNullPointerExceptionWhenCaseDetailsIsNull() {
        assertThatThrownBy(() -> converter.toBaseCaseData(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toCaseDataShouldStillThrowNullPointerExceptionWhenDataIsNull() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();

        assertThatThrownBy(() -> converter.toCaseData(caseDetails))
            .isInstanceOf(NullPointerException.class);
    }
}
