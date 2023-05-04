package uk.gov.hmcts.reform.civil.postcode;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class PostcodeLookupService {

    private static final Logger LOG = LoggerFactory.getLogger(PostcodeLookupService.class);
    private static final List<Pattern> POSTCODE_FORMATS = List.of(
        Pattern.compile("[a-zA-Z]{1,2}\\d[a-zA-Z] \\d[a-zA-Z]{2}"),
        Pattern.compile("[a-zA-Z]{1,2}\\d \\d[a-zA-Z]{2}"),
        Pattern.compile("[a-zA-Z]{1,2}\\d{2} \\d[a-zA-Z]{2}")
    );

    private final RestTemplate restTemplate;
    private final PostcodeLookupConfiguration configuration;

    public PostcodeLookupService(RestTemplate restTemplate, PostcodeLookupConfiguration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    /**
     * Validates that postcode belongs to UK.
     *
     * @param postcode the postcode, not null
     * @return true if the country of the postcode is england, wales, scotland or north ireland
     */
    public boolean validatePostCodeUk(String postcode) {
        if (StringUtils.isBlank(postcode)) {
            return false;
        }
        if (postcodeNorthernIreland(postcode)) {
            return true;
        }
        String countryName = Optional.ofNullable(fetchCountryFromPostCode(postcode.toUpperCase(Locale.UK)))
            .map(s -> s.toUpperCase(Locale.UK))
            .orElse(null);
        return (CountriesAllowed.ENGLAND.name().equals(countryName)
            || CountriesAllowed.WALES.name().equals(countryName)
            || CountriesAllowed.SCOTLAND.name().equals(countryName));
    }

    /*
    Tried with api.os.uk but Northern Ireland postcodes returned no results, so I'm checking just format and
    BT beginning until I have a better way
     */
    private boolean postcodeNorthernIreland(String postcode) {
        return postcode.toUpperCase().startsWith("BT")
            && POSTCODE_FORMATS.stream().anyMatch(f -> f.matcher(postcode).matches());
    }

    public boolean validatePostCodeForDefendant(String postcode) {
        String countryName = fetchCountryFromPostCode(postcode.toUpperCase(Locale.UK));
        return (countryName != null
            && (CountriesAllowed.ENGLAND.name().equals(countryName.toUpperCase(Locale.UK))
            || CountriesAllowed.WALES.name().equals(countryName.toUpperCase(Locale.UK))));
    }

    private String fetchCountryFromPostCode(String postcode) {
        String countryName = null;
        String postcodeFromApilookup = null;
        HttpEntity<String> response = null;
        try {

            Map<String, String> params = new HashMap<>();
            String postcodeQueryParam = StringUtils.deleteWhitespace(postcode) + "+localtype=postcode";
            params.put("query", postcodeQueryParam);
            params.put("maxresults", "1");
            String url = configuration.getUrl();
            String key = configuration.getAccessKey();
            params.put("key", key);
            if (url == null) {
                throw new RuntimeException("Postcode URL is null");
            }
            if (key == null || key.equals("")) {
                throw new RuntimeException("Postcode API Key is null");
            }
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                new HttpEntity(headers),
                String.class
            );

            HttpStatus responseStatus = ((ResponseEntity) response).getStatusCode();

            if (responseStatus.value() == org.apache.http.HttpStatus.SC_OK) {
                JSONObject jsonObj = new JSONObject(response.getBody());

                if (jsonObj.has("results")) {
                    JSONObject gazeteerEntry = new JSONObject(new JSONObject(((JSONArray) jsonObj
                        .get("results")).get(0).toString()).get("GAZETTEER_ENTRY").toString());
                    postcodeFromApilookup = StringUtils.deleteWhitespace(gazeteerEntry.get("NAME1").toString());
                    if (postcodeFromApilookup.equals(StringUtils.deleteWhitespace(postcode))) {
                        countryName = gazeteerEntry.get("COUNTRY").toString();
                    }
                }
            } else if (responseStatus.value() == org.apache.http.HttpStatus.SC_NOT_FOUND) {
                LOG.info("Postcode " + postcode + " not found");
            } else {
                LOG.info("Postcode lookup failed with status ", responseStatus.value());
            }

        } catch (Exception e) {
            LOG.error("Postcode Lookup Failed - ", e.getMessage());
            throw new RuntimeException(e);
        }
        return countryName;
    }
}
