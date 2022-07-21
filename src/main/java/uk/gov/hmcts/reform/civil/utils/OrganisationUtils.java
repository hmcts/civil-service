package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.prd.model.ProfessionalUsersEntityResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrganisationUtils {

    private static final String PUI_CAA = "pui-caa";

    private OrganisationUtils() {
        //NO-OP
    }

    public static List<String> getCaaEmails(Optional<ProfessionalUsersEntityResponse> orgUsersResponse) {
        if (orgUsersResponse.isPresent()) {
            return orgUsersResponse
                .get().getUsers().stream()
                .filter(user -> user.getRoles().contains(PUI_CAA))
                .map(user -> user.getEmail())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static List<String> getCaaEmails(Optional<ProfessionalUsersEntityResponse> orgUsersResponse, int maxEmails) {
        var caaEmails = getCaaEmails(orgUsersResponse);
        return caaEmails.size() > maxEmails ? caaEmails.subList(0, maxEmails) : caaEmails;
    }
}
