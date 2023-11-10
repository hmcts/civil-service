package uk.gov.hmcts.reform.civil.service.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.client.BundleApiClient;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleRequestMapper;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleCreationService {

    private final CoreCaseDataService coreCaseDataService;
    private final BundleRequestMapper bundleRequestMapper;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final BundleApiClient bundleApiClient;
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;

    @Value("${bundle.config}")
    private String bundleConfig;
    private final CaseDetailsConverter caseDetailsConverter;

    public BundleCreateResponse createBundle(BundleCreationTriggerEvent event) {
        CaseDetails caseDetails = coreCaseDataService.getCase(event.getCaseId());
        return createNewBundleRequest(getAccessToken(), serviceAuthTokenGenerator.generate(),
                            bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseDetailsConverter.toCaseData(caseDetails),
                                bundleConfig,
                                caseDetails.getJurisdiction(), caseDetails.getCaseTypeId(), caseDetails.getId()));
    }

    private String getAccessToken() {
        return userService.getAccessToken(
            userConfig.getUserName(),
            userConfig.getPassword()
        );
    }

    private BundleCreateResponse createNewBundleRequest(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) {

        return bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
    }
}
