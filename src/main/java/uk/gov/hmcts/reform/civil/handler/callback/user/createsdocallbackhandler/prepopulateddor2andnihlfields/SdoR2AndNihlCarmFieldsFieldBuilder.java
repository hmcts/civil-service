package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SdoR2AndNihlCarmFieldsFieldBuilder implements SdoCaseFieldBuilder {

    private final List<IncludeInOrderToggle> includeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2SmallClaimsMediationSectionToggle(includeInOrderToggle);
        updatedData.sdoR2SmallClaimsMediationSectionStatement(SdoR2SmallClaimsMediation.builder()
                .input(SdoR2UiConstantSmallClaim.CARM_MEDIATION_TEXT)
                .build());
    }
}
