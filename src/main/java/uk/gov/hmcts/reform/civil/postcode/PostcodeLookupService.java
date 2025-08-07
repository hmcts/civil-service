package uk.gov.hmcts.reform.civil.postcode;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
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
 * @author gergelykiss
 * @version 1.0
 */
@Service
public class PostcodeLookupService {

    private final RestTemplate restTemplate;
    private final PostcodeLookupConfiguration configuration;

    public PostcodeLookupService(RestTemplate restTemplate, PostcodeLookupConfiguration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    public boolean validatePostCodeForDefendant(String postcode) {
        String countryName = fetchCountryFromPostCode(postcode.toUpperCase(Locale.UK));
        return (countryName != null && (CountriesAllowed.ENGLAND.name().equals(countryName.toUpperCase(Locale.UK)) || CountriesAllowed.WALES.name().equals(
            countryName.toUpperCase(Locale.UK))));
    }

    private String fetchCountryFromPostCode(String postcode) {
        try {
            String url = configuration.getUrl();
            String key = configuration.getAccessKey();

            if (url == null) {
                throw new IllegalArgumentException("Postcode url cannot be blank or empty");
            }
            if (StringUtils.isBlank(key)) {
                throw new IllegalStateException("Postcode API key is not configured");
            }

            Map<String, String> params = new HashMap<>();
            String postcodeQueryParam = StringUtils.deleteWhitespace(postcode) + "+localtype=postcode";
            params.put("query", postcodeQueryParam);
            params.put("maxresults", "1");
            params.put("key", key);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );

            HttpStatus responseStatus = response.getStatusCode();

            if (responseStatus.value() == org.apache.http.HttpStatus.SC_OK) {
                JSONObject jsonObj = new JSONObject(response.getBody());

                if (jsonObj.has("results")) {
                    JSONArray results = jsonObj.getJSONArray("results");
                    if (!results.isEmpty()) {
                        JSONObject gazeteerEntry = new JSONObject(results.getJSONObject(0).get("GAZETTEER_ENTRY").toString());
                        String postcodeFromApiLookup = StringUtils.deleteWhitespace(gazeteerEntry.get("NAME1").toString());
                        if (postcodeFromApiLookup.equals(StringUtils.deleteWhitespace(postcode))) {
                            return gazeteerEntry.get("COUNTRY").toString();
                        }
                    }
                }
            }
            // For NOT_FOUND or any other status, simply return null without logging
            return null;

        } catch (Exception e) {
            throw new PostcodeLookupException("Postcode lookup service failed for: " + postcode, e);
        }
    }
}
