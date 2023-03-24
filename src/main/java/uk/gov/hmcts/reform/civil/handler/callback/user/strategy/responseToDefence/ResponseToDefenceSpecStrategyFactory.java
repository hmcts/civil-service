package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.responseToDefence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;

@Component
@RequiredArgsConstructor
public class ResponseToDefenceSpecStrategyFactory {
    private final ResponseToDefenceSpecDefault responseToDefenceSpecDefault;
    private final ResponseToDefenceSpecV1 responseToDefenceSpecV1;
    private final ResponseToDefenceSpecV2 responseToDefenceSpecV2;

    public ResponseToDefenceSpecStrategy getResponseToDefeneceSpecStrategy(CallbackVersion version){
        switch (version){
            case V_1: return responseToDefenceSpecV1;
            case V_2: return responseToDefenceSpecV2;
            default: return responseToDefenceSpecDefault;
        }
    }
}
