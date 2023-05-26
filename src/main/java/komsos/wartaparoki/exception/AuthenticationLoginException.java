package komsos.wartaparoki.exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import komsos.wartaparoki.helper.ResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationLoginException implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setHeader("error", authException.getMessage());
        ResponseDto<Boolean> error = new ResponseDto<>();
        List<String> errorMessage = new ArrayList<>();
        errorMessage.add("Username dan password tidak valid");
        error.setErrorMessage(errorMessage);
        error.setStatus(false);
        error.setPayload(false);
        error.setMessage("Gagal Login");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }

}
