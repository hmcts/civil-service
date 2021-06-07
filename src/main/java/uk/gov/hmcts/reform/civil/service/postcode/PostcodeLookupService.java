package uk.gov.hmcts.reform.civil.service.postcode;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import uk.gov.hmcts.reform.civil.config.PostcodeLookupConfiguration;
import uk.gov.hmcts.reform.civil.enums.CountriesAllowed;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class PostcodeLookupService {

    private static final Logger LOG = LoggerFactory.getLogger(PostcodeLookupService.class);

    private final RestTemplate restTemplate;
    private final PostcodeLookupConfiguration configuration;

    public PostcodeLookupService(RestTemplate restTemplate, PostcodeLookupConfiguration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    public boolean validatePostCodeForDefendant(String postcode) {
        String countryName = fetchCountryFromPostCode(postcode);
        if (countryName != null
            && (CountriesAllowed.ENGLAND.name().equals(countryName.toUpperCase())
            || CountriesAllowed.WALES.name().equals(countryName.toUpperCase()))) {
            return true;
        }
        return false;
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
                JSONObject jsonObj = new JSONObject(response.getBody().toString());

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
        }
        return countryName;
    }
}
