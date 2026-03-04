package org.springframework.boot.actuate.health;

// TODO(DTSCCI-3888): Remove this shim once all dependencies use the Boot 4 actuator contracts directly.
public interface HealthIndicator {

    Health health();
}
