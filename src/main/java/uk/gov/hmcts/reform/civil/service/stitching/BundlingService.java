package uk.gov.hmcts.reform.civil.service.stitching;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.cmc.client.BundleApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.BundleCreateRequestMapper;
import uk.gov.hmcts.reform.civil.model.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
public class BundlingService {

    @Autowired
    private BundleApiClient bundleApiClient;

    @Autowired
    private BundleCreateRequestMapper bundleCreateRequestMapper;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${bundle.english.config}")
    private String bundleEnglishConfig;

    @Value("${bundle.welsh.config}")
    private String bundleWelshConfig;

    public BundleCreateResponse createBundleServiceRequest(CaseData caseData, String eventId,
                                                           String authorization) throws Exception {
        return createBundle(authorization, authTokenGenerator.generate(),
            bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(
                caseData, eventId,
                getBundleConfig(null != caseData.getLanguagePreferenceWelsh()
                                    ? caseData.getLanguagePreferenceWelsh() : YesOrNo.NO)));
    }

    private BundleCreateResponse createBundle(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) throws Exception {
        return bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
    }

    private String getBundleConfig(YesOrNo welshPreference) {
        if (YesOrNo.YES.equals(welshPreference)) {
            return bundleWelshConfig;
        }
        return bundleEnglishConfig;
    }

}
