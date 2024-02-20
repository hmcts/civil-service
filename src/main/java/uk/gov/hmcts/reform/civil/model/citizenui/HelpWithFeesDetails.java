package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class HelpWithFeesDetails {

    private String noRemissionDetails;
    private NoRemissionDetailsSummary noRemissionDetailsSummary;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal remissionAmount;
}
