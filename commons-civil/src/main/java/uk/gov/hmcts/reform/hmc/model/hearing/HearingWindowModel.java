package uk.gov.hmcts.reform.hmc.model.hearing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HearingWindowModel {

    private String dateRangeStart;
    private String dateRangeEnd;
    private String firstDateTimeMustBe;
}
