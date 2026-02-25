package uk.gov.hmcts.reform.civil.prd.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class DxAddress {

    private String dxExchange;
    private String dxNumber;
}
