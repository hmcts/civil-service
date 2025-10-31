package uk.gov.hmcts.reform.civil.service.robotics.support;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.DxAddress;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@Component
@RequiredArgsConstructor
public class RoboticsCaseDataSupport {

    private final RoboticsAddressMapper addressMapper;
    private final RoboticsPartyLookup partyLookup;

    public LitigiousParty buildLitigiousParty(
        Party party,
        LitigationFriend litigationFriend,
        String type,
        String id,
        String solicitorId,
        String solicitorOrganisationId,
        LocalDate dateOfService
    ) {
        return LitigiousParty.builder()
            .id(id)
            .solicitorID(solicitorId)
            .type(type)
            .name(PartyUtils.getLitigiousPartyName(party, litigationFriend))
            .dateOfBirth(PartyUtils.getDateOfBirth(party).map(d -> d.format(ISO_DATE)).orElse(null))
            .addresses(Optional.ofNullable(party.getPrimaryAddress())
                           .map(addressMapper::toRoboticsAddresses)
                           .orElse(null))
            .dateOfService(Optional.ofNullable(dateOfService).map(d -> d.format(ISO_DATE)).orElse(null))
            .solicitorOrganisationID(solicitorOrganisationId)
            .build();
    }

    public Solicitor buildSolicitor(SolicitorData solicitorData) {
        Solicitor.SolicitorBuilder<?, ?> builder = Solicitor.builder()
            .id(solicitorData.getId())
            .isPayee(solicitorData.isPayee())
            .organisationId(solicitorData.getOrganisationId())
            .contactEmailAddress(solicitorData.getContactEmailAddress())
            .reference(partyLookup.truncateReference(solicitorData.getReference()));

        Optional.ofNullable(solicitorData.getOrganisation())
            .ifPresent(org -> applyOrganisation(builder, org, solicitorData.getServiceAddress()));

        Optional.ofNullable(solicitorData.getOrganisationDetails())
            .ifPresent(details -> applyOrganisationDetails(builder, details));

        return builder.build();
    }

    public void applyOrganisationDetails(
        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder,
        SolicitorOrganisationDetails organisationDetails
    ) {
        if (organisationDetails == null) {
            return;
        }
        solicitorBuilder
            .name(organisationDetails.getOrganisationName())
            .contactTelephoneNumber(organisationDetails.getPhoneNumber())
            .contactFaxNumber(organisationDetails.getFax())
            .contactDX(organisationDetails.getDx())
            .contactEmailAddress(organisationDetails.getEmail())
            .addresses(Optional.ofNullable(organisationDetails.getAddress())
                           .map(addressMapper::toRoboticsAddresses)
                           .orElse(null));
    }

    public void applyOrganisation(
        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder,
        uk.gov.hmcts.reform.civil.prd.model.Organisation organisation,
        Address providedServiceAddress
    ) {
        if (organisation == null) {
            return;
        }
        List<ContactInformation> contactInformation = organisation.getContactInformation();
        solicitorBuilder
            .name(organisation.getName())
            .addresses(resolveOrganisationAddresses(contactInformation, providedServiceAddress))
            .contactDX(resolveDx(contactInformation));
    }

    public Optional<String> organisationId(OrganisationPolicy organisationPolicy) {
        return Optional.ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID);
    }

    public RoboticsAddresses resolveOrganisationAddresses(
        List<ContactInformation> contactInformation,
        Address providedServiceAddress
    ) {
        return Optional.ofNullable(providedServiceAddress)
            .map(addressMapper::toRoboticsAddresses)
            .orElse(addressMapper.toRoboticsAddresses(contactInformation));
    }

    public String resolveDx(List<ContactInformation> contactInformation) {
        if (contactInformation == null || contactInformation.isEmpty()) {
            return null;
        }
        List<DxAddress> dxAddresses = contactInformation.get(0).getDxAddress();
        return (dxAddresses == null || dxAddresses.isEmpty()) ? null : dxAddresses.get(0).getDxNumber();
    }

    public String resolveRespondentSolicitorId(YesOrNo represented, YesOrNo sameLegalRepresentative) {
        if (represented != YesOrNo.YES) {
            return null;
        }
        if (sameLegalRepresentative == YesOrNo.YES) {
            return RoboticsDataUtil.RESPONDENT_SOLICITOR_ID;
        }
        if (sameLegalRepresentative == YesOrNo.NO) {
            return RoboticsDataUtil.RESPONDENT2_SOLICITOR_ID;
        }
        return null;
    }

    @Getter
    @Builder
    public static class SolicitorData {
        private final String id;
        private final boolean isPayee;
        private final String organisationId;
        private final String contactEmailAddress;
        private final String reference;
        private final Address serviceAddress;
        private final uk.gov.hmcts.reform.civil.prd.model.Organisation organisation;
        private final SolicitorOrganisationDetails organisationDetails;
    }
}
