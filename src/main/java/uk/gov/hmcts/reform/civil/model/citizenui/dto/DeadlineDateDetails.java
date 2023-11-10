package uk.gov.hmcts.reform.civil.model.citizenui.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineDateDetails {

    private LocalDate responseDate;
    private Integer plusDays;
}
