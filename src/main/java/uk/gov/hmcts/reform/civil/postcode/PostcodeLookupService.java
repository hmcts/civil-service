package uk.gov.hmcts.reform.civil.postcode;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class PostcodeLookupService {

    private static final String RESULTS = "results";

    private final RestTemplate restTemplate;
    private final PostcodeLookupConfiguration configuration;

    public PostcodeLookupService(RestTemplate restTemplate, PostcodeLookupConfiguration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    public boolean validatePostCodeForDefendant(String postcode) {
        if (StringUtils.isBlank(postcode)) {
            return false;
        }
        String countryName = fetchCountryFromPostCode(postcode.toUpperCase(Locale.UK));
        return (countryName != null
            && (CountriesAllowed.ENGLAND.name().equals(countryName.toUpperCase(Locale.UK))
            || CountriesAllowed.WALES.name().equals(countryName.toUpperCase(Locale.UK))));
    }

    private String fetchCountryFromPostCode(String postcode) {
        try {

            Map<String, String> params = new HashMap<>();
            String compactPostcode = StringUtils.deleteWhitespace(postcode);
            String postcodeQueryParam = compactPostcode;
            if (compactPostcode.length() > 3) {
                int inwardCodeStart = compactPostcode.length() - 3;
                postcodeQueryParam = compactPostcode.substring(0, inwardCodeStart)
                    + " " + compactPostcode.substring(inwardCodeStart);
            }
            params.put("query", postcodeQueryParam);
            params.put("maxresults", "1");
            params.put("fq", "LOCAL_TYPE:Postcode");
            String url = configuration.getUrl();
            String key = configuration.getAccessKey();
            params.put("key", key);
            if (url == null) {
                throw new RuntimeException("Postcode URL is null");
            }
            if (key == null || key.equals("")) {
                throw new RuntimeException("Postcode API Key is null");
            }
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            ResponseEntity<String> response = restTemplate.exchange(
                builder.build().encode().toUri(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );

            HttpStatusCode responseStatus = response.getStatusCode();

            if (responseStatus.value() == org.apache.http.HttpStatus.SC_OK) {
                return countryForMatchingPostcode(new JSONObject(response.getBody()), compactPostcode);
            } else if (responseStatus.value() == org.apache.http.HttpStatus.SC_NOT_FOUND) {
                postcode = postcode.replaceAll("[\n\r]", "_");
                log.info("Postcode {} not found", postcode);
            } else {
                log.info("Postcode lookup failed with status {}", responseStatus.value());
            }

        } catch (HttpClientErrorException.NotFound _) {
            log.info("Postcode {} not found", StringUtils.deleteWhitespace(postcode));
        } catch (Exception e) {
            // HTTP exception messages can include the API key in the request URL.
            log.error("Postcode lookup failed ({})", e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
        return null;
    }

    private String countryForMatchingPostcode(JSONObject response, String compactPostcode) {
        if (!response.has(RESULTS)) {
            return null;
        }
        JSONArray results = response.getJSONArray(RESULTS);
        if (results.length() == 0) {
            return null;
        }
        JSONObject entry = results.getJSONObject(0).getJSONObject("GAZETTEER_ENTRY");
        if (compactPostcode.equals(StringUtils.deleteWhitespace(entry.getString("NAME1")))) {
            return entry.getString("COUNTRY");
        }
        return null;
    }
}
