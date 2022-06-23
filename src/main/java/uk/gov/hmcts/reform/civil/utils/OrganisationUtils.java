package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.prd.model.ProfessionalUsersEntityResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class OrganisationUtils {

    private static final String CASEWORKER_CAA = "pui-caa";

    public static List<String> getCaaEmails(Optional<ProfessionalUsersEntityResponse> orgUsersResponse) {
        if (orgUsersResponse.isPresent()) {
            return orgUsersResponse
                .get().getUsers().stream()
                .filter(user -> user.getRoles().contains(CASEWORKER_CAA))
                .map(user -> user.getEmail()).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
