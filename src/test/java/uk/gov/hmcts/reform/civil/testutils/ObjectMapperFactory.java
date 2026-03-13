package uk.gov.hmcts.reform.civil.testutils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.civil.config.JacksonConfiguration;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;

import java.util.Collection;
import java.util.List;

public class ObjectMapperFactory {

    private ObjectMapperFactory() {
        // utility class
    }

    public static ObjectMapper instance() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(new LocalDateSerializer(JacksonConfiguration.DATE_FORMATTER));
        module.addSerializer(new LocalDateTimeSerializer(JacksonConfiguration.DATE_TIME_FORMATTER));
        module.addSerializer(GAApproveConsentOrder.class, new JacksonConfiguration.GAApproveConsentOrderSerializer());
        module.addSerializer(FastTrackAllocation.class, new JacksonConfiguration.FastTrackAllocationSerializer());
        module.addSerializer(DynamicList.class, new JacksonConfiguration.DynamicListSerializer());
        module.addDeserializer(DynamicList.class, new JacksonConfiguration.DynamicListDeserializer());
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build()
            .registerModule(module)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.addMixIn(GAApproveConsentOrder.class, JacksonConfiguration.GAApproveConsentOrderMixin.class);
        mapper.addMixIn(FastTrackAllocation.class, JacksonConfiguration.FastTrackAllocationMixin.class);
        mapper.addMixIn(DynamicList.class, JacksonConfiguration.DynamicListMixin.class);
        mapper.addMixIn(uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial.class, JacksonConfiguration.SdoR2TrialMixin.class);
        mapper.addMixIn(uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing.class,
                        JacksonConfiguration.SdoR2SmallClaimsHearingMixin.class);
        mapper.configOverride(Collection.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        mapper.configOverride(List.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        mapper.configOverride(GAApproveConsentOrder.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        mapper.configOverride(FastTrackAllocation.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        return mapper;
    }
}
