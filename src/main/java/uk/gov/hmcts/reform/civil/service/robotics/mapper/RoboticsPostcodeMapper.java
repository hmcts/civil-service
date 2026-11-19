package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.postcode.CountriesAllowed;
import uk.gov.hmcts.reform.civil.postcode.PostcodeLookupService;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.model.robotics.ToJsonString;

import java.util.List;
import java.util.Locale;
import java.time.Duration;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

/** Prepares postcodes for Caseman without changing the source case addresses. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoboticsPostcodeMapper {

    private static final Pattern OUTSIDE_POSTCODE = Pattern.compile(
        "(?:BT|JE|GY|IM|AB|DD|EH|FK|G|HS|IV|KA|KW|KY|ML|PA|PH|ZE)\\d{1,2}\\d[A-Z]{2}");

    // District country coverage checked against Postcodes.io outcodes, November 2026.
    private static final Pattern SCOTTISH_DISTRICT = Pattern.compile("(?:DG(?:[1-9]|1[0-4])|TD(?:[1-46-8]|1[0134]))\\d[A-Z]{2}");
    private static final Pattern BORDER_POSTCODE = Pattern.compile("(?:DG16|TD(?:5|9|12|15))\\d[A-Z]{2}");

    private static final Set<String> OUTSIDE_COUNTRIES = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2)
        .stream().filter(code -> !"GB".equals(code))
        .map(code -> Locale.of("", code))
        .map(locale -> locale.getDisplayCountry(Locale.UK))
        .map(name -> name.toUpperCase(Locale.UK)).collect(Collectors.toUnmodifiableSet());

    private static final Set<String> DOMESTIC_COUNTRIES = Set.of(CountriesAllowed.ENGLAND.name(), CountriesAllowed.WALES.name(), "CYMRU");
    private static final Set<String> OUTSIDE_NAMES = Set.of(CountriesAllowed.SCOTLAND.name(), "NORTHERN IRELAND",
                                                            "USA", "EIRE", "ÉIRE", "REPUBLIC OF IRELAND");

    private static boolean isOutsideCountry(String country) {
        return OUTSIDE_COUNTRIES.contains(country) || OUTSIDE_NAMES.contains(country);
    }

    private final PostcodeLookupService postcodeLookupService;
    private final Cache<String, String> countries =
        Caffeine.newBuilder()
            .maximumSize(1000).expireAfterWrite(Duration.ofMinutes(5)).build();

    public String prepareJson(ToJsonString payload)
        throws JsonProcessingException {
        return switch (payload) {
            case RoboticsCaseData data -> {
                mapPostcodes(data.getLitigiousParties(), data.getSolicitors());
                yield data.toJsonString();
            }
            case RoboticsCaseDataSpec data -> {
                mapPostcodes(data.getLitigiousParties(), data.getSolicitors());
                yield data.toJsonString();
            }
            case null, default -> throw new IllegalArgumentException("Expected a specified or unspecified RPA payload");
        };
    }

    public void mapPostcodes(List<LitigiousParty> parties, List<Solicitor> solicitors) {
        if (parties != null) {
            parties.forEach(party -> mapAddress(party.getAddresses()));
        }
        if (solicitors != null) {
            solicitors.forEach(solicitor -> mapAddress(solicitor.getAddresses()));
        }
    }

    private void mapAddress(RoboticsAddresses addresses) {
        if (addresses == null || addresses.getContactAddress() == null) {
            return;
        }
        RoboticsAddress address = addresses.getContactAddress();
        String postcode = address.getPostCode();
        if (isBlank(postcode)) {
            return;
        }
        String lookupPostcode = postcode.replaceAll("\\s", "").toUpperCase(Locale.UK);
        String country = Objects.toString(address.getCountry(), "").trim().toUpperCase(Locale.UK);
        if (!isOutsideCountry(country) && !DOMESTIC_COUNTRIES.contains(country)) {
            if (OUTSIDE_POSTCODE.matcher(lookupPostcode).matches() || SCOTTISH_DISTRICT.matcher(lookupPostcode).matches()) {
                relocatePostcode(address, postcode);
                return;
            }
            if (!BORDER_POSTCODE.matcher(lookupPostcode).matches()) {
                return;
            }
            country = Objects.toString(countries.get(lookupPostcode, key -> postcodeLookupService
                .findCountryForPostcode(key).map(name -> name.trim().toUpperCase(Locale.UK)).orElse(null)), "");
        }
        if (isOutsideCountry(country)) {
            relocatePostcode(address, postcode);
        }
    }

    private void relocatePostcode(RoboticsAddress address, String postcode) {
        String line3 = address.getAddressLine3();
        String combined = isBlank(line3) ? postcode : line3 + " - " + postcode;
        if (combined.length() <= AddressLinesMapper.LINE_LIMIT) {
            address.setAddressLine3(combined);
            address.setPostCode(null);
            return;
        }

        if (postcode.length() <= AddressLinesMapper.LINE_LIMIT && isBlank(address.getAddressLine5())) {
            if (isBlank(address.getAddressLine4())) {
                address.setAddressLine4(postcode);
            } else {
                address.setAddressLine5(postcode);
            }
        } else if (postcode.length() <= AddressLinesMapper.LINE_LIMIT && isBlank(address.getAddressLine4())
            && address.getAddressLine5().length() <= AddressLinesMapper.LINE_LIMIT) {
            address.setAddressLine4(address.getAddressLine5());
            address.setAddressLine5(postcode);
        } else if (address.getAddressLine5() != null && (address.getAddressLine5() + " - " + postcode).length() <= AddressLinesMapper.LINE_LIMIT) {
            address.setAddressLine5(address.getAddressLine5() + " - " + postcode);
        } else {
            log.warn("RPA postcode relocation exceeds address capacity; retaining the original address");
            return;
        }
        address.setPostCode(null);
    }
}
