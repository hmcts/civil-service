package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.prd.model.DxAddress;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.List;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.model.Address.fromContactInformation;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Representative {

    private String contactName;
    private String organisationName;
    private String phoneNumber;
    private String dxAddress;
    private String emailAddress;
    private Address serviceAddress;
    private String legalRepHeading;

    public static Representative fromSolicitorOrganisationDetails(
        SolicitorOrganisationDetails solicitorOrganisationDetails) {
        return new Representative()
            .setDxAddress(solicitorOrganisationDetails.getDx())
            .setOrganisationName(solicitorOrganisationDetails.getOrganisationName())
            .setPhoneNumber(solicitorOrganisationDetails.getPhoneNumber())
            .setEmailAddress(solicitorOrganisationDetails.getEmail())
            .setServiceAddress(solicitorOrganisationDetails.getAddress());
    }

    public static Representative fromOrganisation(Organisation organisation) {
        var contactInformation = organisation.getContactInformation().get(0);
        return new Representative()
            .setOrganisationName(organisation.getName())
            .setDxAddress(ofNullable(contactInformation.getDxAddress())
                              .filter(not(List::isEmpty))
                              .map(dxAddressList -> dxAddressList.get(0))
                              .map(DxAddress::getDxNumber)
                              .orElse(""))
            .setServiceAddress(fromContactInformation(contactInformation));
    }
}
