package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class DQExtraDetailsLipTest {

    @Test
    void shouldReturnReportExpertDetails_whenItExists() {
        //Given
        DQExtraDetailsLip extraDetailsLip = new DQExtraDetailsLip()
            .setRespondent1DQLiPExpert(
                new ExpertLiP()
                    .setDetails(
                        wrapElements(List.of(
                            new ExpertReportLiP()
                                .setExpertName("Name")
                                .setReportDate(LocalDate.now())))
                    )
            );
        //When
        List<ExpertReportLiP> result = extraDetailsLip.getReportExpertDetails();
        //Then
        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenReportExpertDetailsDoNotExist() {
        //Given
        DQExtraDetailsLip extraDetailsLip = new DQExtraDetailsLip();
        //When
        List<ExpertReportLiP> result = extraDetailsLip.getReportExpertDetails();
        //Then
        assertThat(result).isEmpty();
    }
}
