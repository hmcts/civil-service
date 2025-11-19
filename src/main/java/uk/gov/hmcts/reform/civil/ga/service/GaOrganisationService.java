package uk.gov.hmcts.reform.civil.ga.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.civil.ga.client.GaOrganisationApi;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class GaOrganisationService {

    private final GaOrganisationApi organisationApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final PrdAdminUserConfiguration userConfig;

    public Optional<Organisation> findOrganisationByUserId(String userId) {
        try {
            String authToken = userService.getAccessToken(userConfig.getUsername(), userConfig.getPassword());
            return ofNullable(organisationApi.findOrganisationByUserId(
                authToken,
                authTokenGenerator.generate(),
                userId
            ));
        } catch (FeignException.NotFound ex) {
            log.error("Organisation not found", ex);
            return Optional.empty();
        }
    }
}
