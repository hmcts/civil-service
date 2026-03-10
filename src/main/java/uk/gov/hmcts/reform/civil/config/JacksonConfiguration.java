package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Configuration
public class JacksonConfiguration {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
        .ofPattern(DATE_TIME_FORMAT, Locale.UK);

    @Bean
    public Module jsonDateTimeFormatModule() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(new LocalDateSerializer(DATE_FORMATTER));
        module.addSerializer(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        return module;
    }

    @Bean
    public Module civilSerializationModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(GAApproveConsentOrder.class, new GAApproveConsentOrderSerializer());
        module.addSerializer(FastTrackAllocation.class, new FastTrackAllocationSerializer());
        module.addSerializer(DynamicList.class, new DynamicListSerializer());
        module.addDeserializer(DynamicList.class, new DynamicListDeserializer());
        return module;
    }

    @Bean
    public ObjectMapper objectMapper(ObjectProvider<List<Module>> modulesProvider) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        modulesProvider.getIfAvailable(List::of).forEach(mapper::registerModule);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.addMixIn(GAApproveConsentOrder.class, GAApproveConsentOrderMixin.class);
        mapper.addMixIn(FastTrackAllocation.class, FastTrackAllocationMixin.class);
        mapper.addMixIn(DynamicList.class, DynamicListMixin.class);
        mapper.addMixIn(SdoR2Trial.class, SdoR2TrialMixin.class);
        mapper.addMixIn(SdoR2SmallClaimsHearing.class, SdoR2SmallClaimsHearingMixin.class);
        mapper.configOverride(Collection.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        mapper.configOverride(List.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        mapper.configOverride(GAApproveConsentOrder.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        mapper.configOverride(FastTrackAllocation.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public static final class GAApproveConsentOrderSerializer extends JsonSerializer<GAApproveConsentOrder> {

        @Override
        public boolean isEmpty(SerializerProvider provider, GAApproveConsentOrder value) {
            return value == null
                || (value.getConsentOrderDescription() == null
                && value.getConsentOrderDateToEnd() == null
                && value.getShowConsentOrderDate() == null
                && value.getIsOrderProcessedByStayScheduler() == null);
        }

        @Override
        public void serialize(GAApproveConsentOrder value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            if (isEmpty(serializers, value)) {
                gen.writeNull();
                return;
            }
            gen.writeStartObject();
            if (value.getConsentOrderDescription() != null) {
                gen.writeStringField("consentOrderDescription", value.getConsentOrderDescription());
            }
            if (value.getConsentOrderDateToEnd() != null) {
                serializers.defaultSerializeField("consentOrderDateToEnd", value.getConsentOrderDateToEnd(), gen);
            }
            if (value.getShowConsentOrderDate() != null) {
                serializers.defaultSerializeField("showConsentOrderDate", value.getShowConsentOrderDate(), gen);
            }
            if (value.getIsOrderProcessedByStayScheduler() != null) {
                serializers.defaultSerializeField("isOrderProcessedByStayScheduler", value.getIsOrderProcessedByStayScheduler(), gen);
            }
            gen.writeEndObject();
        }
    }

    @JsonSerialize(using = GAApproveConsentOrderSerializer.class)
    public interface GAApproveConsentOrderMixin {
        // Marker mixin for serializer binding.
    }

    @JsonSerialize(using = FastTrackAllocationSerializer.class)
    public interface FastTrackAllocationMixin {
        // Marker mixin for serializer binding.
    }

    @JsonSerialize(using = DynamicListSerializer.class)
    @JsonDeserialize(using = DynamicListDeserializer.class)
    public interface DynamicListMixin {
        // Marker mixin for serializer/deserializer binding.
    }

    public abstract static class SdoR2TrialMixin {
        @JsonDeserialize(using = DynamicListEmptyValueAsNullDeserializer.class)
        public abstract void setAltHearingCourtLocationList(DynamicList altHearingCourtLocationList);
    }

    public abstract static class SdoR2SmallClaimsHearingMixin {
        @JsonDeserialize(using = DynamicListEmptyValueAsNullDeserializer.class)
        public abstract void setAltHearingCourtLocationList(DynamicList altHearingCourtLocationList);
    }

    public static final class DynamicListDeserializer extends JsonDeserializer<DynamicList> {

        @Override
        public DynamicList deserialize(JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt)
            throws IOException {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            JsonNode node = mapper.readTree(p);
            if (node == null || node.isNull()) {
                return null;
            }

            DynamicListElement value = null;
            JsonNode valueNode = node.get("value");
            if (valueNode != null && !valueNode.isNull()) {
                value = mapper.treeToValue(valueNode, DynamicListElement.class);
            }

            List<DynamicListElement> listItems = null;
            JsonNode listItemsNode = node.get("list_items");
            if (listItemsNode != null && !listItemsNode.isNull() && !(listItemsNode.isArray() && listItemsNode.isEmpty())) {
                listItems = mapper.convertValue(listItemsNode, new TypeReference<>() { });
            }

            return new DynamicList(value, listItems);
        }
    }

    public static final class DynamicListEmptyValueAsNullDeserializer extends JsonDeserializer<DynamicList> {

        @Override
        public DynamicList deserialize(JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt)
            throws IOException {
            DynamicList dynamicList = new DynamicListDeserializer().deserialize(p, ctxt);
            if (dynamicList == null) {
                return null;
            }
            DynamicListElement value = dynamicList.getValue();
            if (value != null
                && value.getCode() == null
                && value.getLabel() == null
                && dynamicList.getListItems() != null
                && !dynamicList.getListItems().isEmpty()) {
                dynamicList.setValue(null);
            }
            return dynamicList;
        }
    }

    public static final class DynamicListSerializer extends JsonSerializer<DynamicList> {

        @Override
        public boolean isEmpty(SerializerProvider provider, DynamicList value) {
            return value == null
                || (value.getValue() == null && (value.getListItems() == null || value.getListItems().isEmpty()));
        }

        @Override
        public void serialize(DynamicList value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            if (isEmpty(serializers, value)) {
                gen.writeNull();
                return;
            }
            gen.writeStartObject();
            if (value.getValue() != null) {
                gen.writeFieldName("value");
                gen.writeStartObject();
                if (value.getValue().getCode() != null) {
                    gen.writeStringField("code", value.getValue().getCode());
                }
                if (value.getValue().getLabel() != null) {
                    gen.writeStringField("label", value.getValue().getLabel());
                }
                gen.writeEndObject();
            }
            if (value.getListItems() != null && !value.getListItems().isEmpty()) {
                serializers.defaultSerializeField("list_items", value.getListItems(), gen);
            }
            gen.writeEndObject();
        }
    }

    public static final class FastTrackAllocationSerializer extends JsonSerializer<FastTrackAllocation> {

        @Override
        public boolean isEmpty(SerializerProvider provider, FastTrackAllocation value) {
            return value == null
                || (value.getAssignComplexityBand() == null
                && value.getBand() == null
                && value.getReasons() == null);
        }

        @Override
        public void serialize(FastTrackAllocation value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            if (isEmpty(serializers, value)) {
                gen.writeNull();
                return;
            }
            gen.writeStartObject();
            if (value.getAssignComplexityBand() != null) {
                serializers.defaultSerializeField("assignComplexityBand", value.getAssignComplexityBand(), gen);
            }
            if (value.getBand() != null) {
                serializers.defaultSerializeField("band", value.getBand(), gen);
            }
            if (value.getReasons() != null) {
                gen.writeStringField("reasons", value.getReasons());
            }
            gen.writeEndObject();
        }
    }
}
