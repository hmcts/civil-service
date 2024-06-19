package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FeesApiRequest {

    private String channel;
    private String event;
    private String jurisdiction;
    private String jurisdiction2;
    private String service;
    private String keyword;
    private BigDecimal amount;
}
