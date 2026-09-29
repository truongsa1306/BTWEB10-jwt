package vn.iotstar.btweb10.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Maps every error to {@link ApiError}. Framework MVC errors (404, 405, 400 ...) keep their
 * own status via ResponseEntityExceptionHandler but are rendered in the same format.
 * Status codes follow the table in the JWT document (401 for invalid/expired JWT).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "The username or password is incorrect", req);
    }

    @ExceptionHandler({UsernameNotFoundException.class, InsufficientAuthenticationException.class})
    public ResponseEntity<ApiError> handleAuthentication(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication is required", req);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ApiError> handleAccountStatus(AccountStatusException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "The account is locked or disabled", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "You are not authorized to access this resource", req);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiError> handleSignature(SignatureException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "The JWT signature is invalid", req);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiError> handleExpired(ExpiredJwtException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "The JWT token has expired", req);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ApiError> handleMalformed(MalformedJwtException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "The JWT token is malformed", req);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handleJwt(JwtException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "The JWT token is invalid", req);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT, "The request conflicts with existing data", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown internal server error", req);
    }

    /** Re-render standard Spring MVC errors (validation, 404, 405, unreadable body...) as ApiError. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        String message;
        if (ex instanceof MethodArgumentNotValidException manve) {
            message = manve.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        } else if (body instanceof ProblemDetail pd && pd.getDetail() != null) {
            message = pd.getDetail();
        } else {
            message = ex.getMessage();
        }
        String path = request instanceof ServletWebRequest swr ? swr.getRequest().getRequestURI() : "";
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String reason = status != null ? status.getReasonPhrase() : "Error";
        ApiError error = new ApiError(Instant.now(), statusCode.value(), reason, message, path);
        return ResponseEntity.status(statusCode).headers(headers).body(error);
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message,
                        req.getRequestURI()));
    }
}
