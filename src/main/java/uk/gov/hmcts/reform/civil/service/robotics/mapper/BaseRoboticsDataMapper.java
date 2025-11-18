package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.DxAddress;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseRoboticsDataMapper {

    protected final RoboticsAddressMapper addressMapper;

    protected Consumer<Organisation> buildOrganisation(
        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder, Address providedServiceAddress
    ) {
        return organisation -> {
            List<ContactInformation> contactInformation = organisation.getContactInformation();
            solicitorBuilder
                .name(organisation.getName())
                .addresses(fromProvidedAddress(contactInformation, providedServiceAddress))
                .contactDX(getContactDX(contactInformation));
        };
    }

    protected String getContactDX(List<ContactInformation> contactInformation) {
        if (isEmpty(contactInformation)) {
            return null;
        }
        List<DxAddress> dxAddresses = contactInformation.get(0).getDxAddress();
        return isEmpty(dxAddresses) ? null : dxAddresses.get(0).getDxNumber();
    }

    protected RoboticsAddresses fromProvidedAddress(List<ContactInformation> contactInformation, Address provided) {
        return Optional.ofNullable(provided)
            .map(address -> addressMapper.toRoboticsAddresses(provided))
            .orElse(addressMapper.toRoboticsAddresses(contactInformation));
    }

    protected Optional<String> getOrganisationId(uk.gov.hmcts.reform.civil.model.OrganisationPolicy respondent1OrganisationPolicy) {
        return ofNullable(respondent1OrganisationPolicy)
            .map(uk.gov.hmcts.reform.civil.model.OrganisationPolicy::getOrganisation)
            .map(uk.gov.hmcts.reform.civil.model.Organisation::getOrganisationID);
    }

    protected Consumer<SolicitorOrganisationDetails> buildOrganisationDetails(
        Solicitor.SolicitorBuilder solicitorBuilder
    ) {
        return organisationDetails ->
            solicitorBuilder
                .name(organisationDetails.getOrganisationName())
                .contactTelephoneNumber(organisationDetails.getPhoneNumber())
                .contactFaxNumber(organisationDetails.getFax())
                .contactDX(organisationDetails.getDx())
                .contactEmailAddress(organisationDetails.getEmail())
                .addresses(ofNullable(organisationDetails.getAddress())
                               .map(addressMapper::toRoboticsAddresses)
                               .orElse(null)
                );
    }
}
