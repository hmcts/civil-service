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
        DQExtraDetailsLip extraDetailsLip = DQExtraDetailsLip
            .builder()
            .respondent1DQLiPExpert(
                ExpertLiP
                    .builder()
                    .details(
                        wrapElements(List.of(
                            ExpertReportLiP.builder()
                                .expertName("Name")
                                .reportDate(LocalDate.now())
                                .build()))
                    )
                    .build())
            .build();
        //When
        List<ExpertReportLiP> result = extraDetailsLip.getReportExpertDetails();
        //Then
        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenReportExpertDetailsDoNotExist() {
        //Given
        DQExtraDetailsLip extraDetailsLip = DQExtraDetailsLip.builder().build();
        //When
        List<ExpertReportLiP> result = extraDetailsLip.getReportExpertDetails();
        //Then
        assertThat(result).isEmpty();
    }
}
