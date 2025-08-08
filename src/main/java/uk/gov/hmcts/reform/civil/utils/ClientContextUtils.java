package uk.gov.hmcts.reform.civil.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.wa.ClientContextWrapper;

import java.util.Base64;

@Slf4j
@NoArgsConstructor
public class ClientContextUtils {

    public static String encodeClientContext(ClientContextWrapper clientContext, ObjectMapper mapper) {
        try {
            String jsonString = mapper.writeValueAsString(clientContext);
            byte[] encodedBytes = Base64.getEncoder().encode(jsonString.getBytes());
            String encodedString = new String(encodedBytes);
            log.info("encoded string wa mapper {}", encodedString);
            return encodedString;

        } catch (Exception ex) {
            log.error("Exception while serializing the WaMapper object: {}", ex.getMessage());
            return null;
        }
    }

}
