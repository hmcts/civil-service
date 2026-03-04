package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.model.Fee2Dto;
import uk.gov.hmcts.reform.civil.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.civil.model.FeeVersionDto;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Configuration
// TODO(DTSCCI-3888): Remove this compatibility config once upstream HMCTS/shared clients are fully
// Jackson 3 native (constructors/properties aligned) and contract/integration tests pass without mixins.
public class JacksonToolsCompatibilityConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer caseAssignmentUserRoleCustomizer() {
        return builder -> {
            builder.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
            builder.configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            builder.addMixIn(CaseAssignmentUserRole.class, CaseAssignmentUserRoleMixin.class);
            builder.addMixIn(CaseDetails.class, CaseDetailsMixin.class);
            builder.addMixIn(FeeLookupResponseDto.class, FeeLookupResponseDtoMixin.class);
            builder.addMixIn(Fee2Dto.class, Fee2DtoMixin.class);
            builder.addMixIn(FeeVersionDto.class, FeeVersionDtoMixin.class);
        };
    }

    abstract static class CaseAssignmentUserRoleMixin {

        @JsonCreator
        CaseAssignmentUserRoleMixin(
            @JsonProperty("case_id") String caseDataId,
            @JsonProperty("user_id") String userId,
            @JsonProperty("case_role") String caseRole
        ) {
            // Mixin constructor only; no implementation required.
        }
    }

    abstract static class CaseDetailsMixin {

        @JsonCreator
        CaseDetailsMixin(
            @JsonProperty("id") Long id,
            @JsonProperty("jurisdiction") String jurisdiction,
            @JsonProperty("case_type_id") String caseTypeId,
            @JsonProperty("created_date") LocalDateTime createdDate,
            @JsonProperty("last_modified") LocalDateTime lastModified,
            @JsonProperty("state") String state,
            @JsonProperty("locked_by_user_id") Integer lockedBy,
            @JsonProperty("security_level") Integer securityLevel,
            @JsonProperty("case_data") Map<String, Object> data,
            @JsonProperty("security_classification") Classification securityClassification,
            @JsonProperty("callback_response_status") String callbackResponseStatus,
            @JsonProperty("version") Integer version
        ) {
            // Mixin constructor only; no implementation required.
        }
    }

    abstract static class FeeLookupResponseDtoMixin {

        @JsonProperty("fee_amount")
        BigDecimal feeAmount;
    }

    abstract static class Fee2DtoMixin {

        @JsonProperty("applicant_type")
        Object applicantType;
        @JsonProperty("channel_type")
        Object channelType;
        @JsonProperty("current_version")
        Object currentVersion;
        @JsonProperty("event_type")
        Object eventType;
        @JsonProperty("fee_type")
        String feeType;
        @JsonProperty("fee_versions")
        List<?> feeVersions;
        @JsonProperty("matching_version")
        Object matchingVersion;
        @JsonProperty("max_range")
        BigDecimal maxRange;
        @JsonProperty("min_range")
        BigDecimal minRange;
        @JsonProperty("range_unit")
        String rangeUnit;
        @JsonProperty("service_type")
        Object serviceType;
        @JsonProperty("unspecified_claim_amount")
        Boolean unspecifiedClaimAmount;
    }

    abstract static class FeeVersionDtoMixin {

        @JsonProperty("flat_amount")
        Object flatAmount;
        @JsonProperty("memo_line")
        String memoLine;
        @JsonProperty("natural_account_code")
        String naturalAccountCode;
        @JsonProperty("percentage_amount")
        Object percentageAmount;
        @JsonProperty("si_ref_id")
        String siRefId;
        @JsonProperty("statutory_instrument")
        String statutoryInstrument;
        @JsonProperty("valid_from")
        Object validFrom;
        @JsonProperty("valid_to")
        Object validTo;
    }
}
