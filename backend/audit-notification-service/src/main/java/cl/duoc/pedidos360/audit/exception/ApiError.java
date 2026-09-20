package cl.duoc.pedidos360.audit.exception;

import java.time.Instant;

public record ApiError(Instant timestamp, int status, String error, String message, String path) {
}
