package uk.gov.hmcts.reform.civil;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.UserRole;

@Component
public class Config implements CCDConfig<CaseData, CaseState, UserRole> {

    @Override
    public void configure(ConfigBuilder<CaseData, CaseState, UserRole> builder) {
        builder.caseType("CIVIL", "Civil", "Civil");
        builder.jurisdiction("CIVIL", "Civil", "Civil Jurisdiction");
        builder.setCallbackHost(System.getenv().getOrDefault("API_URL", "http://localhost:3333"));
        // builder.shutterService(UserRole.SOLICITOR);
        // builder.shutterService(UserRole.SOLICITOR);
    }

}
