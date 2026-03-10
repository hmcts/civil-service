package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration
@Import(JacksonConfiguration.class)
public class TestJacksonAutoConfiguration {

    @Bean
    public ObjectMapper objectMapper(ObjectProvider<List<Module>> modulesProvider) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        modulesProvider.getIfAvailable(List::of).forEach(mapper::registerModule);
        mapper.addMixIn(uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder.class,
                        JacksonConfiguration.GAApproveConsentOrderMixin.class);
        mapper.addMixIn(uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation.class,
                        JacksonConfiguration.FastTrackAllocationMixin.class);
        mapper.addMixIn(uk.gov.hmcts.reform.civil.model.common.DynamicList.class,
                        JacksonConfiguration.DynamicListMixin.class);
        mapper.addMixIn(uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial.class,
                        JacksonConfiguration.SdoR2TrialMixin.class);
        mapper.addMixIn(uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing.class,
                        JacksonConfiguration.SdoR2SmallClaimsHearingMixin.class);
        mapper.addMixIn(uk.gov.hmcts.reform.civil.model.CaseDataCaseProgression.class,
                        CaseDataCaseProgressionMixin.class);
        mapper.addMixIn(uk.gov.hmcts.reform.civil.model.dq.RequestedCourt.class,
                        RequestedCourtMixin.class);
        mapper.addMixIn(uk.gov.hmcts.reform.civil.model.CaseData.class, CaseDataNullFieldMixin.class);
        mapper.configOverride(uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        mapper.configOverride(uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private interface CaseDataCaseProgressionMixin {
        @JsonInclude(JsonInclude.Include.ALWAYS)
        String getNotificationText();

        @JsonInclude(JsonInclude.Include.ALWAYS)
        java.time.LocalDate getHearingDate();

        @JsonInclude(JsonInclude.Include.ALWAYS)
        java.time.LocalDate getHearingDueDate();
    }

    private interface CaseDataNullFieldMixin {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object getGeneralApplications();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        uk.gov.hmcts.reform.civil.enums.YesOrNo getIsRespondent1();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        uk.gov.hmcts.reform.civil.model.common.DynamicList getSelectLitigationFriend();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object getServedDocumentFiles();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object getRespondent1Copy();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object getRespondent2Copy();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object getSdoOrderDocument();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object getNextDeadline();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object getApplicant1ResponseDeadline();

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        Object getSystemGeneratedCaseDocuments();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object getRegistrationTypeRespondentTwo();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object getRegistrationTypeRespondentOne();

        @JsonInclude(JsonInclude.Include.ALWAYS)
        Object getRespondent1GeneratedResponseDocument();

        @JsonInclude(JsonInclude.Include.ALWAYS)
        Object getRespondent2GeneratedResponseDocument();
    }

    private interface RequestedCourtMixin {
        @JsonInclude(JsonInclude.Include.ALWAYS)
        String getResponseCourtCode();
    }
}
