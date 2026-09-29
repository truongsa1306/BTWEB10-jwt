package vn.iotstar.btweb10.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import vn.iotstar.btweb10.exception.ErrorResponseWriter;

import java.io.IOException;

/** Unauthenticated request to a protected resource -> 401 (Spring default would be 403). */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED, "Authentication is required");
    }
}
