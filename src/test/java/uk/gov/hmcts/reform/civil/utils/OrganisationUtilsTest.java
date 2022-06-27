package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class OrganisationUtilsTest {

    @Nested
    class GetCaaEmails {
        List<String> mockCaaEmails;
        List<ProfessionalUsersResponse> caaUsers;
        List<ProfessionalUsersResponse> otherUsers;

        @BeforeEach
        void setup() {
            var mockCaaRoles = Arrays.asList("pui-caa");
            mockCaaEmails = Arrays.asList(
                "org-1-caa-1@example.com",
                "org-1-caa-2@example.com",
                "org-1-caa-3@example.com"
            );
            var mockOtherRoles = Arrays.asList("other");
            var mockNonCaaEmails = Arrays.asList(
                "org-1-other-1@example.com",
                "org-1-other-2@example.com"
            );

            caaUsers = mockCaaEmails.stream()
                .map(email -> ProfessionalUsersResponse.builder().email(email).roles(mockCaaRoles).build())
                .collect(Collectors.toList());
            otherUsers =
                mockNonCaaEmails.stream()
                    .map(email -> ProfessionalUsersResponse.builder().email(email).roles(mockOtherRoles).build())
                    .collect(Collectors.toList());
        }

        @Test
        void shouldReturnExpectedCaaEmails() {
            var mockUsers = new ArrayList<ProfessionalUsersResponse>();
            mockUsers.addAll(caaUsers);
            mockUsers.addAll(otherUsers);
            var professionalUsers =
                ProfessionalUsersEntityResponse.builder().users(mockUsers).build();

            assertThat(OrganisationUtils.getCaaEmails(Optional.of(professionalUsers))).isEqualTo(mockCaaEmails);
        }

        @Test
        void shouldReturnEmptyList() {
            var professionalUsers =
                ProfessionalUsersEntityResponse.builder().users(otherUsers).build();

            assertThat(OrganisationUtils.getCaaEmails(Optional.of(professionalUsers)).isEmpty());
        }
    }
}
