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
import uk.gov.hmcts.reform.civil.postcode.PostcodeLookupService;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.model.robotics.ToJsonString;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;

import java.util.List;
import java.util.Locale;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

/** Prepares postcodes for Caseman without changing the source case addresses. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoboticsPostcodeMapper {

    private static final Set<String> OUTSIDE_COUNTRIES = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2)
        .stream().filter(code -> !"GB".equals(code))
        .map(code -> Locale.of("", code))
        .flatMap(locale -> Stream.of(locale.getCountry(), locale.getISO3Country(), locale.getDisplayCountry(Locale.UK)))
        .map(name -> name.toUpperCase(Locale.UK)).collect(Collectors.toUnmodifiableSet());

    private static boolean isOutsideCountry(String country) {
        return OUTSIDE_COUNTRIES.contains(country) || "SCOTLAND".equals(country) || "NORTHERN IRELAND".equals(country);
    }

    private final PostcodeLookupService postcodeLookupService;
    private final Cache<String, Optional<String>> countries =
        Caffeine.newBuilder()
            .maximumSize(1000).expireAfterWrite(Duration.ofMinutes(5)).build();

    private final JsonSchemaValidationService schemaValidationService;

    public String prepareJson(ToJsonString payload)
        throws JsonProcessingException {
        if (payload instanceof RoboticsCaseData data) {
            mapPostcodes(data.getLitigiousParties(), data.getSolicitors());
        } else if (payload instanceof RoboticsCaseDataSpec data) {
            mapPostcodes(data.getLitigiousParties(), data.getSolicitors());
        }
        String json = payload.toJsonString();
        var errors = schemaValidationService.validate(json);
        if (errors != null && !errors.isEmpty()) {
            throw new JsonSchemaValidationException(
                "Invalid prepared RPA payload", errors);
        }
        return json;
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
        String country = java.util.Objects.toString(address.getCountry(), "").trim().toUpperCase(Locale.UK);
        if (!isOutsideCountry(country) && !Set.of("ENGLAND", "WALES", "CYMRU").contains(country)) {
            country = countries.get(lookupPostcode, postcodeLookupService::findCountryForPostcode)
                .map(name -> name.trim().toUpperCase(Locale.UK)).orElse("");
        }
        if (isOutsideCountry(country)) {
            String line3 = address.getAddressLine3();
            String combined = isBlank(line3) ? postcode : line3 + " - " + postcode;
            if (combined.length() <= 35) {
                address.setAddressLine3(combined);
                address.setPostCode(null);
            } else {
                // Keep the postcode intact in line 3 and move existing text into the remaining lines.
                String remaining = Stream.of(line3, address.getAddressLine4(), address.getAddressLine5())
                    .filter(value -> !isBlank(value)).collect(Collectors.joining(", "));
                if (postcode.length() <= 35 && remaining.length() <= 70) {
                    int split = Math.min(35, remaining.length());
                    address.setAddressLine3(postcode);
                    address.setAddressLine4(remaining.substring(0, split));
                    address.setAddressLine5(remaining.length() > split ? remaining.substring(split) : null);
                    address.setPostCode(null);
                } else {
                    log.warn("RPA postcode relocation exceeds address capacity; retaining the original address");
                }
            }
        }
    }
}
