package uk.gov.hmcts.reform.civil.service.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class UserAuthContent {

    String userToken;
    String userId;
}
