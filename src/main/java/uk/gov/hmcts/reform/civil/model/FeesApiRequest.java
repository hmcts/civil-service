package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FeesApiRequest {

    private String channel;
    private String event;
    private String jurisdiction;
    private String jurisdiction2;
    private String service;
    private String keyword;
    private BigDecimal amount;
}
