package uk.gov.hmcts.reform.unspec.service.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAuthContent {

    String userToken;
    String userId;
}
