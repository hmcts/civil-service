package uk.gov.hmcts.reform.civil.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.wa.WaMapper;

import java.util.Base64;

@Slf4j
public class WaMapperUtils {

    private WaMapperUtils() {
        //no op
    }

    public static WaMapper getWaMapper(String clientContext) {
        if (clientContext != null) {
            log.info("clientContext is present");
            byte[] decodedBytes = Base64.getDecoder().decode(clientContext);
            String decodedString = new String(decodedBytes);
            try {
                return new ObjectMapper().readValue(decodedString, WaMapper.class);
            } catch (Exception ex) {
                log.error("Exception while parsing the Client-Context {}", ex.getMessage());
            }
        }
        return null;
    }
}
