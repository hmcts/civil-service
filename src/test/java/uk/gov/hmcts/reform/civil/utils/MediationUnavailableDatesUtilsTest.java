package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.MediationUnavailableDatesUtils.checkUnavailable;

class MediationUnavailableDatesUtilsTest {

    private List<String> errors;

    private static final LocalDate now = LocalDate.of(2024, 1, 1);
    private static MockedStatic<LocalDate> localDateNowMock;

    @BeforeAll
    static void setupSuite() {
        localDateNowMock = mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
        localDateNowMock.when(LocalDate::now).thenReturn(now);
    }

    @AfterAll
    static void tearDown() {
        localDateNowMock.reset();
        localDateNowMock.close();
    }

    @BeforeEach
    void setUp() {
        errors = new ArrayList<>();
    }

    // -----------------------
    // checkUnavailable tests
    // -----------------------

    @Test
    void shouldAddToErrors_whenUnavailableDatesListIsEmpty() {
        checkUnavailable(errors, new ArrayList<>());
        assertThat(errors).containsExactly(
            "Please provide at least one valid Date from if you cannot attend hearing within next 3 months."
        );
    }

    @Test
    void shouldAddToErrors_whenSingleDatesListIsMoreThan3MonthsInTheFuture() {
        List<Element<UnavailableDate>> unavailableDates = wrapElements(UnavailableDate.builder()
                                                                           .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                                           .date(LocalDate.of(2024, 5, 1))
                                                                           .build());
        checkUnavailable(errors, unavailableDates);
        assertThat(errors).containsExactly("Unavailability Date must not be more than three months in the future.");
    }

    @Test
    void shouldAddToErrors_whenSingleDatesListIsInThePast() {
        List<Element<UnavailableDate>> unavailableDates = wrapElements(UnavailableDate.builder()
                                                                           .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                                           .date(LocalDate.of(2023, 12, 31))
                                                                           .build());
        checkUnavailable(errors, unavailableDates);
        assertThat(errors).containsExactly("Unavailability Date must not be before today.");
    }

    @Test
    void shouldAddToErrors_whenDateRangeFromIsInThePast() {
        List<Element<UnavailableDate>> unavailableDates = wrapElements(UnavailableDate.builder()
                                                                           .unavailableDateType(UnavailableDateType.DATE_RANGE)
                                                                           .fromDate(LocalDate.of(2023, 12, 31))
                                                                           .toDate(LocalDate.of(2024, 12, 31))
                                                                           .build());
        checkUnavailable(errors, unavailableDates);
        assertThat(errors).containsExactly("Unavailability Date From must not be before today.");
    }

    @Test
    void shouldAddToErrors_whenDateRangeToIsBeforeFrom() {
        List<Element<UnavailableDate>> unavailableDates = wrapElements(UnavailableDate.builder()
                                                                           .unavailableDateType(UnavailableDateType.DATE_RANGE)
                                                                           .fromDate(LocalDate.of(2024, 2, 20))
                                                                           .toDate(LocalDate.of(2023, 12, 31))
                                                                           .build());
        checkUnavailable(errors, unavailableDates);
        assertThat(errors).containsExactly(
            "Unavailability Date From cannot be after Unavailability Date To. Please enter valid range."
        );
    }

    @Test
    void shouldAddToErrors_whenDateRangeToIsMoreThan3MonthsInTheFuture() {
        List<Element<UnavailableDate>> unavailableDates = wrapElements(UnavailableDate.builder()
                                                                           .unavailableDateType(UnavailableDateType.DATE_RANGE)
                                                                           .fromDate(LocalDate.of(2024, 6, 2))
                                                                           .toDate(LocalDate.of(2024, 6, 3))
                                                                           .build());
        checkUnavailable(errors, unavailableDates);
        assertThat(errors).containsExactly(
            "Unavailability Date To must not be more than three months in the future."
        );
    }

    @Test
    void shouldNotAddToErrors_whenDateRangeAndSingleDateAreBothValid() {
        List<Element<UnavailableDate>> unavailableDates = wrapElements(
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.of(2024, 2, 2))
                .toDate(LocalDate.of(2024, 2, 5))
                .build(),
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.of(2024, 1, 31))
                .build()
        );
        checkUnavailable(errors, unavailableDates);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotAddError_whenSingleDateIsExactlyTodayOr3MonthsAhead() {
        List<Element<UnavailableDate>> dates = wrapElements(
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(now)
                .build(),
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(now.plusMonths(3))
                .build()
        );
        checkUnavailable(errors, dates);
        assertThat(errors).isEmpty();
    }

    // -----------------------
    // normalizeUnavailableDates tests
    // -----------------------

    @Test
    void shouldDoNothing_whenUnavailableDatesListIsNullOrEmptyInNormalize() {
        MediationUnavailableDatesUtils.normalizeUnavailableDates(null);
        MediationUnavailableDatesUtils.normalizeUnavailableDates(new ArrayList<>());
    }

    @Test
    void shouldSkipElement_whenValueIsNull() {
        List<Element<UnavailableDate>> list = new ArrayList<>();
        list.add(new Element<>()); // value is null
        MediationUnavailableDatesUtils.normalizeUnavailableDates(list);
        assertThat(list.get(0).getValue()).isNull();
    }

    @Test
    void shouldSkipElement_whenUnavailableDateTypeIsNull() {
        UnavailableDate date = UnavailableDate.builder().build(); // type is null
        List<Element<UnavailableDate>> list = wrapElements(date);
        MediationUnavailableDatesUtils.normalizeUnavailableDates(list);
        assertThat(date.getDate()).isNull();
        assertThat(date.getFromDate()).isNull();
        assertThat(date.getToDate()).isNull();
    }

    @Test
    void shouldClearFromAndTo_whenSingleDate() {
        UnavailableDate date = UnavailableDate.builder()
            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
            .fromDate(LocalDate.of(2024, 1, 1))
            .toDate(LocalDate.of(2024, 1, 2))
            .build();
        List<Element<UnavailableDate>> list = wrapElements(date);
        MediationUnavailableDatesUtils.normalizeUnavailableDates(list);
        assertThat(date.getFromDate()).isNull();
        assertThat(date.getToDate()).isNull();
    }

    @Test
    void shouldClearDate_whenDateRange() {
        UnavailableDate date = UnavailableDate.builder()
            .unavailableDateType(UnavailableDateType.DATE_RANGE)
            .date(LocalDate.of(2024, 1, 1))
            .build();
        List<Element<UnavailableDate>> list = wrapElements(date);
        MediationUnavailableDatesUtils.normalizeUnavailableDates(list);
        assertThat(date.getDate()).isNull();
    }
}
