package uk.gov.hmcts.reform.prd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalUsersEntityResponse {

    private String organisationIdentifier;
    private List<ProfessionalUsersResponse> users;
}
