package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(generate = false)
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class IdamUserDetails {

    private String email;
    private String id;
}
