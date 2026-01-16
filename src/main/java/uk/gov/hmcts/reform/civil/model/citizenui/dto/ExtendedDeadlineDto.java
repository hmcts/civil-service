package uk.gov.hmcts.reform.civil.model.citizenui.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ExtendedDeadlineDto {

    private LocalDate responseDate;
    private Integer plusDays;
}
