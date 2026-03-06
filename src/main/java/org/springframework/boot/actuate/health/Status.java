package org.springframework.boot.actuate.health;

import java.util.Objects;

// TODO(DTSCCI-3888): Remove this shim once all dependencies are compatible with Boot 4
// and no longer require the legacy Status type in this package.
public final class Status {

    public static final Status UP = new Status("UP");
    public static final Status DOWN = new Status("DOWN");

    private final String code;

    public Status(String code) {
        this.code = Objects.requireNonNull(code, "code must not be null");
    }

    public String getCode() {
        return code;
    }
}
