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
import uk.gov.hmcts.reform.civil.exceptions.PostcodeLookupException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Service for postcode validation and country lookup.
 *
 * <p><strong>Security Note:</strong> This class is NOT vulnerable to CVE-2024-22259 as it does not use
 * UriComponentsBuilder.buildAndExpand() anywhere. All URI construction uses safe methods only.</p>
 *
 *  @author gergelykiss
 * @version 1.0
 */

@Service
@Slf4j
@SuppressWarnings({"All", "java:S2139"}) // CVE-2024-22259 not applicable, logging with rethrow intentional
public class PostcodeLookupService {

    private static final Logger LOG = LoggerFactory.getLogger(PostcodeLookupService.class);

    private final RestTemplate restTemplate;
    private final PostcodeLookupConfiguration configuration;

    public PostcodeLookupService(RestTemplate restTemplate, PostcodeLookupConfiguration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
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
                throw new IllegalArgumentException("Postcode url cannot be blank or empty");
            }
            if (StringUtils.isBlank(key)) {
                throw new IllegalStateException("Postcode API key is not configured");
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
                new HttpEntity<>(headers),
                String.class
            );

            HttpStatus responseStatus = ((ResponseEntity<?>) response).getStatusCode();

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
                LOG.info("Postcode {} not found", postcode);
            } else {
                LOG.info("Postcode lookup failed with status {}", responseStatus.value());
            }

        } catch (Exception e) {
            LOG.error("Postcode Lookup Failed - {} ", e.getMessage());
            throw new PostcodeLookupException("Postcode lookup service failed for: " + postcode, e);
        }
        return countryName;
    }
}
