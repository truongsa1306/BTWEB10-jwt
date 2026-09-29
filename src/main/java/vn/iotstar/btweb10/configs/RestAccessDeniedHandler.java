package vn.iotstar.btweb10.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import vn.iotstar.btweb10.exception.ErrorResponseWriter;

import java.io.IOException;

/** Authenticated but not allowed -> 403. */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ErrorResponseWriter.write(request, response, HttpStatus.FORBIDDEN,
                "You are not authorized to access this resource");
    }
}
