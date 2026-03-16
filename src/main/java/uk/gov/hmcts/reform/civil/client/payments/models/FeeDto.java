package uk.gov.hmcts.reform.civil.client.payments.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeDto {
    private BigDecimal calculatedAmount;
    private String ccdCaseNumber;
    private String code;
    private String description;
    private Integer id;
    private String jurisdiction1;
    private String jurisdiction2;
    private String memoLine;
    private String naturalAccountCode;
    private BigDecimal netAmount;
    private String reference;
    private String version;
    private Integer volume;
}
