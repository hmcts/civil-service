package uk.gov.hmcts.reform.civil.service.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.client.EvidenceManagementApiClient;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleRequestMapper;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleCreationService {

    private final CoreCaseDataService coreCaseDataService;
    private final BundleRequestMapper bundleRequestMapper;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final EvidenceManagementApiClient evidenceManagementApiClient;
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;
    private final CaseDetailsConverter caseDetailsConverter;
    @Value("${bundle.config}")
    private String bundleConfig;
    @Value("${bundle.formConfig}")
    private String bundleFormConfig;

    public BundleCreateResponse createBundle(BundleCreationTriggerEvent event) {
        log.info("Handling create bundle request with event for case id {}", event.getCaseId());
        return createBundle(event.getCaseId());
    }

    public BundleCreateResponse createBundle(Long caseId) {
        log.info("Handling create bundle request for case id {}", caseId);
        CaseDetails caseDetails = coreCaseDataService.getCase(caseId);
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(
            caseDetailsConverter.toCaseData(caseDetails),
            bundleConfig,
            caseDetails.getJurisdiction(),
            caseDetails.getCaseTypeId());
        return createNewBundleRequest(getUserAccessToken(), serviceAuthTokenGenerator.generate(), bundleCreateRequest);
    }

    public BundleCreateResponse createBundle(Long caseId, List<DocumentMetaData> documentMetaDataList, String sealedFormName) {
        log.info("Handling create bundle request for claim forms for case id {} with formConfig {}", caseId, bundleFormConfig);
        CaseDetails caseDetails = coreCaseDataService.getCase(caseId);
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(
            caseId,
            documentMetaDataList,
            bundleFormConfig,
            sealedFormName,
            caseDetails.getJurisdiction(),
            caseDetails.getCaseTypeId()
        );
        return createNewBundleRequest(getUserAccessToken(), serviceAuthTokenGenerator.generate(), bundleCreateRequest);
    }

    private String getUserAccessToken() {
        return userService.getAccessToken(
            userConfig.getUserName(),
            userConfig.getPassword()
        );
    }

    private BundleCreateResponse createNewBundleRequest(String authorization, String serviceAuthorization,
                                                        BundleCreateRequest bundleCreateRequest) {
        return evidenceManagementApiClient.createNewBundle(authorization, serviceAuthorization, bundleCreateRequest);
    }
}
