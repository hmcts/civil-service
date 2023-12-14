package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmcAddress {

    private String line1;
    private String line2;
    private String line3;
    private String city;
    private String county;
    private String postcode;
}
