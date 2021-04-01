package uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prd.model.DxAddress;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.SolicitorOrganisationDetails;

import java.util.List;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.unspec.model.Address.fromContactInformation;

@Data
@Builder(toBuilder = true)
public class Representative {

    private final String organisationName;
    private final String phoneNumber;
    private final String dxAddress;
    private final String emailAddress;
    private final Address serviceAddress;

    public static Representative fromSolicitorOrganisationDetails(
        SolicitorOrganisationDetails solicitorOrganisationDetails) {
        return Representative.builder()
            .dxAddress(solicitorOrganisationDetails.getDx())
            .organisationName(solicitorOrganisationDetails.getOrganisationName())
            .phoneNumber(solicitorOrganisationDetails.getPhoneNumber())
            .emailAddress(solicitorOrganisationDetails.getEmail())
            .serviceAddress(solicitorOrganisationDetails.getAddress())
            .build();
    }

    public static Representative fromOrganisation(Organisation organisation) {
        var contactInformation = organisation.getContactInformation().get(0);
        return Representative.builder()
            .organisationName(organisation.getName())
            .dxAddress(ofNullable(contactInformation.getDxAddress())
                           .filter(not(List::isEmpty))
                           .map(dxAddressList -> dxAddressList.get(0))
                           .map(DxAddress::getDxNumber)
                           .orElse(""))
            .serviceAddress(fromContactInformation(contactInformation))
            .build();
    }
}
