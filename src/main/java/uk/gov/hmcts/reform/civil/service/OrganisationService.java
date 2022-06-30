package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationService {

    private final OrganisationApi organisationApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final PrdAdminUserConfiguration userConfig;
    private final CustomScopeIdamTokenGeneratorService tokenGenerator;

    //WARNING! below function findOrganisation is being used by both damages and specified claims,
    // changes to this code may break one of the claim journeys, check with respective teams before changing it

    public Optional<Organisation> findOrganisation(String authToken) {
        try {
            return ofNullable(organisationApi.findUserOrganisation(authToken, authTokenGenerator.generate()));

        } catch (FeignException.NotFound | FeignException.Forbidden ex) {
            log.error("User not registered in MO", ex);
            return Optional.empty();
        }
    }

    public Optional<ProfessionalUsersEntityResponse> findUsersInOrganisation(String orgId) {
        String authToken = tokenGenerator.getAccessToken(userConfig.getUsername(), userConfig.getPassword());
        List<String> caaRoles = Arrays.asList("pui-caa");
        List<String> otherRoles = Arrays.asList("caseworker-civil");

        try {
            var realResponse =  ofNullable(organisationApi.findUsersByOrganisation(authToken, authTokenGenerator.generate(), orgId));
            var users = new ArrayList<ProfessionalUsersResponse>();

            switch(orgId) {
                // Organisation 2
                case "79ZRSOU":
                    // CAAS
                    users.addAll(createMockUsers("hmcts.civil+organisation.2.superuser@gmail.com", caaRoles, 175));
                    //OTHER USERS
                    users.addAll(createMockUsers("hmcts.civil+organisation.2.solicitor.1@gmail.com", otherRoles, 100));

                    return createMockResponse(users, realResponse);
                // Organisation 3
                case "H2156A0":
                    // CAAS
                    users.addAll(createMockUsers("hmcts.civil+organisation.3.superuser@gmail.com", caaRoles, 175));
                    //OTHER USERS
                    users.addAll(createMockUsers("hmcts.civil+organisation.3.solicitor.1@gmail.com", otherRoles, 100));

                    return createMockResponse(users, realResponse);
                default:
                    return realResponse;
            }
        } catch (FeignException.NotFound ex) {
            log.error("Organisation not found", ex);
            return Optional.empty();
        }
    }

    private Optional<ProfessionalUsersEntityResponse> createMockResponse(List<ProfessionalUsersResponse> mockUsers, Optional<ProfessionalUsersEntityResponse> realResponse) {
        var realEntity = realResponse.isPresent() ? realResponse.get() : null;
        return ofNullable(
            ProfessionalUsersEntityResponse.builder()
                .organisationIdentifier(realEntity.getOrganisationIdentifier())
                .users(mockUsers)
                .build()
        );
    }

    private List<ProfessionalUsersResponse> createMockUsers(String email, List<String> roles, int amount) {
        var mockUsersList = new ArrayList<ProfessionalUsersResponse>();
        for(int i = 0; i<amount; i++) {
            var number = i + 1;
            mockUsersList.add(ProfessionalUsersResponse.builder()
                .userIdentifier("uid")
                .email(email)
                .roles(roles)
                .firstName("First " + number)
                .lastName("Last " + number)
                .idamStatus("ACTIVE").idamStatusCode("200")
                .idamMessage("11 OK").build()
            );
        }
        return mockUsersList;
    }

    //WARNING! below function findOrganisationById is being used by both damages and specified claims,
    // changes to this code may break one of the claim journeys, check with respective teams before changing it
    public Optional<Organisation> findOrganisationById(String id) {
        String authToken = userService.getAccessToken(userConfig.getUsername(), userConfig.getPassword());
        try {
            return ofNullable(organisationApi.findOrganisationById(authToken, authTokenGenerator.generate(), id));
        } catch (FeignException.NotFound ex) {
            log.error("Organisation not found", ex);
            return Optional.empty();
        }
    }
}
