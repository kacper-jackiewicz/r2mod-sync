package dev.kj.r2mod_sync.exception;

public class NotValidR2Z extends RuntimeException {
    public NotValidR2Z(Exception e) {
        super(e);
    }

    public NotValidR2Z() {
    }

    public NotValidR2Z(String message) {
        super(message);
    }
}

