package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.ValidateFieldsNihl;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SdoR2FastTrackValidator implements GenerateSdoOrderValidator {

    private final ValidateFieldsNihl validateFieldsNihl;
    private final FeatureToggleService featureToggleService;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (isSdoR2EnabledForNihlFastTrack(caseData)) {
            log.debug("Validating Nihl Fast Track fields");
            List<String> errorsNihl = validateFieldsNihl.validateFieldsNihl(caseData);
            if (!errorsNihl.isEmpty()) {
                log.warn("Nihl Fast Track validation errors: {}", errorsNihl);
                errors.addAll(errorsNihl);
            }
        }
    }

    private boolean isSdoR2EnabledForNihlFastTrack(CaseData caseData) {
        boolean enabled = featureToggleService.isSdoR2Enabled() && SdoHelper.isNihlFastTrack(caseData);
        log.debug("SdoR2 enabled for Nihl Fast Track: {}", enabled);
        return enabled;
    }
}
