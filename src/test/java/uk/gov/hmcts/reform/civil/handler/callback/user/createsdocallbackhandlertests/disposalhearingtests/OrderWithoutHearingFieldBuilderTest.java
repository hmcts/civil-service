package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.OrderWithoutHearingFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderWithoutHearingFieldBuilderTest {

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @InjectMocks
    private OrderWithoutHearingFieldBuilder orderWithoutHearingFieldBuilder;

    @Test
    void shouldSetOrderWithoutHearing() {
        LocalDate expectedDate = LocalDate.now().plusDays(7);
        when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5)).thenReturn(expectedDate);

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        orderWithoutHearingFieldBuilder.build(updatedData);

        CaseData result = updatedData.build();
        DisposalOrderWithoutHearing orderWithoutHearing = result.getDisposalOrderWithoutHearing();
        assertThat(orderWithoutHearing).isNotNull();
        assertThat(orderWithoutHearing.getInput())
                .isEqualTo(String.format(
                        "This order has been made without hearing. Each party has the right "
                                + "to apply to have this Order set aside or varied. Any such application must be "
                                + "received by the Court (together with the appropriate fee) by 4pm on %s.",
                        expectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
                ));
    }
}