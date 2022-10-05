package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimFormForSpec;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.TimelineEventDetailsDocmosis;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SealedClaimFormForSpecTest {

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String eventAsJson = "{\"timelineDate\":\"2020-02-01\",\"timelineDescription\":\"asdf\"}";

    @Test
    public void canDeserializeAsFromFront() throws JsonProcessingException {
        TimelineOfEventDetails object = objectMapper.readerFor(TimelineOfEventDetails.class).readValue(eventAsJson);
        Assert.assertEquals(object.getTimelineDate(), LocalDate.of(2020, 2, 1));
    }

    @Test
    public void serializesAsExpectedByDocmosis() throws JsonProcessingException {
        SealedClaimFormForSpec form = SealedClaimFormForSpec.builder()
            .timeline(Collections.singletonList(
                new TimelineEventDetailsDocmosis(
                    objectMapper.readerFor(TimelineOfEventDetails.class).readValue(eventAsJson)
                )
            ))
            .build();
        @SuppressWarnings({"rawtypes", "unchecked"}) Object serialized = ((Map<String, Object>)
            ((List) form.toMap(objectMapper).get("timeline"))
                .get(0)).get("timelineDate");
        Assert.assertEquals("01-02-2020", serialized);
    }
}
