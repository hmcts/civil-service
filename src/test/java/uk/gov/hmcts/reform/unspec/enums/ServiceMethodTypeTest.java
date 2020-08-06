package uk.gov.hmcts.reform.unspec.enums;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ServiceMethodTypeTest {

    @Nested
    class ExpectedDays {

        @ParameterizedTest
        @EnumSource(value = ServiceMethodType.class, names = {"POST", "DOCUMENT_EXCHANGE", "FAX", "EMAIL", "OTHER"})
        void shouldReturnExpectedDays_whenTimeIsBefore4pm(ServiceMethodType serviceMethod) {
            LocalDate dateTime = LocalDate.of(2000, 1, 1);

            assertThat(serviceMethod.getDeemedDateOfService(dateTime.atStartOfDay()))
                .isEqualTo(dateTime.plusDays(serviceMethod.getDays()));
        }

        @ParameterizedTest
        @EnumSource(value = ServiceMethodType.class, names = {"POST", "DOCUMENT_EXCHANGE", "FAX", "EMAIL", "OTHER"})
        void shouldReturnExpectedDays_whenTimeIs4am(ServiceMethodType serviceMethod) {
            LocalDate dateTime = LocalDate.of(2000, 1, 1);

            assertThat(serviceMethod.getDeemedDateOfService(dateTime.atTime(4, 0)))
                .isEqualTo(dateTime.plusDays(serviceMethod.getDays()));
        }
    }

    @Nested
    class PlusOneDay {

        @ParameterizedTest
        @EnumSource(value = ServiceMethodType.class, names = {"EMAIL", "FAX"})
        void shouldReturnPlusOneDays_whenTimeIsAfter4pm(ServiceMethodType serviceMethod) {
            LocalDate dateTime = LocalDate.of(2000, 1, 1);

            assertThat(serviceMethod.getDeemedDateOfService(dateTime.atTime(16, 1)))
                .isEqualTo(dateTime.plusDays(1));
        }

        @ParameterizedTest
        @EnumSource(value = ServiceMethodType.class, names = {"EMAIL", "FAX"})
        void shouldReturnPlusOneDays_whenTimeIs4pm(ServiceMethodType serviceMethod) {
            LocalDate dateTime = LocalDate.of(2000, 1, 1);

            assertThat(serviceMethod.getDeemedDateOfService(dateTime.atTime(16, 0)))
                .isEqualTo(dateTime.plusDays(1));
        }
    }

    @Nested
    class PlusTwoDays {

        @ParameterizedTest
        @EnumSource(value = ServiceMethodType.class, names = {"POST", "DOCUMENT_EXCHANGE", "OTHER"})
        void shouldReturnPlusTwoDays_whenTimeIsAfter4pm(ServiceMethodType serviceMethod) {
            LocalDate dateTime = LocalDate.of(2000, 1, 1);

            assertThat(serviceMethod.getDeemedDateOfService(dateTime.atTime(16, 1)))
                .isEqualTo(dateTime.plusDays(2));
        }

        @ParameterizedTest
        @EnumSource(value = ServiceMethodType.class, names = {"POST", "DOCUMENT_EXCHANGE", "OTHER"})
        void shouldReturnPlusTwoDays_whenTimeIs4pm(ServiceMethodType serviceMethod) {
            LocalDate dateTime = LocalDate.of(2000, 1, 1);

            assertThat(serviceMethod.getDeemedDateOfService(dateTime.atTime(16, 1)))
                .isEqualTo(dateTime.plusDays(2));
        }
    }
}
