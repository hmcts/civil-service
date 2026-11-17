package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.postcode.PostcodeLookupService;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

/** Prepares postcodes for Caseman without changing the source case addresses. */
@Component
@RequiredArgsConstructor
public class RoboticsPostcodeMapper {

    private final PostcodeLookupService postcodeLookupService;

    public void mapPostcodes(List<LitigiousParty> parties, List<Solicitor> solicitors) {
        Map<String, Boolean> englandOrWales = new HashMap<>();
        if (parties != null) {
            parties.forEach(party -> mapAddress(party.getAddresses(), englandOrWales));
        }
        if (solicitors != null) {
            solicitors.forEach(solicitor -> mapAddress(solicitor.getAddresses(), englandOrWales));
        }
    }

    private void mapAddress(RoboticsAddresses addresses, Map<String, Boolean> englandOrWales) {
        if (addresses == null || addresses.getContactAddress() == null) {
            return;
        }
        RoboticsAddress address = addresses.getContactAddress();
        String postcode = address.getPostCode();
        if (isBlank(postcode)) {
            return;
        }
        String lookupPostcode = postcode.replaceAll("\\s", "").toUpperCase(Locale.UK);
        if (Boolean.FALSE.equals(
            englandOrWales.computeIfAbsent(lookupPostcode, postcodeLookupService::validatePostCodeForDefendant))) {
            String line3 = address.getAddressLine3();
            address.setAddressLine3(isBlank(line3) ? postcode : line3 + " - " + postcode);
            address.setPostCode(null);
        }
    }
}
