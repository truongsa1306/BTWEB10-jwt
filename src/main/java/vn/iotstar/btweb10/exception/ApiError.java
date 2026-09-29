package vn.iotstar.btweb10.exception;

import java.time.Instant;

/** Single error format used by GlobalExceptionHandler AND the security entry point / denied handler. */
public record ApiError(Instant timestamp, int status, String error, String message, String path) {
}
