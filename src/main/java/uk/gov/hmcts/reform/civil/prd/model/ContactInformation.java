package uk.gov.hmcts.reform.civil.prd.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ContactInformation {

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String country;
    private String county;
    private List<DxAddress> dxAddress;
    private String postCode;
    private String townCity;
}
