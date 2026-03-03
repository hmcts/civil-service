package org.springframework.boot.actuate.health;

public interface HealthIndicator {
    Health health();
}
