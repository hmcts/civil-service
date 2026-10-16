package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

/**
 * Runs the decentralised CCD runtime in this service: the ccd-persistence endpoints data-store calls, event
 * submission, and the JSON definition support {@link CivilJsonCcdConfig} uses.
 *
 * <p>The runtime's components sit outside the {@code uk.gov.hmcts.reform} packages this application scans. They are
 * named by class so they cannot clash with this service's beans; the runtime's auto-configurations still load as
 * auto-configurations.
 */
@Configuration
@ConditionalOnProperty(name = "civil.decentralised.enabled", havingValue = "true")
@ComponentScan(
    basePackages = "uk.gov.hmcts.ccd.sdk",
    nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
    }
)
public class DecentralisedRuntimeConfiguration {
}
