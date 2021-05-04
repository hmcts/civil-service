package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationService {

    private final OrganisationApi organisationApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final PrdAdminUserConfiguration userConfig;

    public Optional<Organisation> findOrganisation(String authToken) {
        try {
            return ofNullable(organisationApi.findUserOrganisation(authToken, authTokenGenerator.generate()));

        } catch (FeignException.NotFound | FeignException.Forbidden ex) {
            log.error("User not registered in MO", ex);
            return Optional.empty();
        }
    }

    public Optional<Organisation> findOrganisationById(String id) {
        String authToken = idamClient.getAccessToken(userConfig.getUsername(), userConfig.getPassword());
        try {
            return ofNullable(organisationApi.findOrganisationById(authToken, authTokenGenerator.generate(), id));
        } catch (FeignException.NotFound ex) {
            log.error("Organisation not found", ex);
            return Optional.empty();
        }
    }
}
