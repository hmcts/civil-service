package org.springframework.boot.actuate.health;

// TODO(DTSCCI-3888): Remove this shim once all external dependencies are upgraded to the
// Boot 4 actuator health API and no longer reference the legacy org.springframework.boot.actuate.health.Health type.
public final class Health {

    private final Status status;
    private final Throwable error;

    private Health(Status status, Throwable error) {
        this.status = status;
        this.error = error;
    }

    public Status getStatus() {
        return status;
    }

    public Throwable getError() {
        return error;
    }

    public static Builder down(Exception exception) {
        return new Builder(Status.DOWN).withException(exception);
    }

    public static final class Builder {

        private final Status status;
        private Throwable error;

        public Builder(Status status) {
            this.status = status;
        }

        public Builder withException(Throwable exception) {
            this.error = exception;
            return this;
        }

        public Health build() {
            return new Health(status, error);
        }
    }
}
