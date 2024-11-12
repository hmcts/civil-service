package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.HearingTimeFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HearingTimeFieldBuilderTest {

    @InjectMocks
    private HearingTimeFieldBuilder hearingTimeFieldBuilder;

    @Test
    void shouldSetHearingTime() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        hearingTimeFieldBuilder.build(updatedData);

        CaseData result = updatedData.build();
        DisposalHearingHearingTime hearingTime = result.getDisposalHearingHearingTime();
        assertThat(hearingTime).isNotNull();
        assertThat(hearingTime.getInput())
                .isEqualTo("This claim will be listed for final disposal before a judge on the first available date after");
        assertThat(hearingTime.getDateTo()).isEqualTo(LocalDate.now().plusWeeks(16));
    }
}