package komsos.wartaparoki.exception;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import komsos.wartaparoki.helper.ResponseDto;

@RestControllerAdvice
public class ExceptionResolver {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto<String> handleNoHandlerFound(NoHandlerFoundException e, WebRequest request) {
        String url = ((ServletWebRequest) request).getRequest().getRequestURI().toString();
        ResponseDto<String> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add("page not found - URL:" + url);
        response.setMessage("Halaman Tidak ditemukan");
        return response;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ResponseDto<String> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        ResponseDto<String> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add("File to large !");
        response.setMessage("Failed Upload file");
        return response;
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto<Boolean> noSuchElementException(NoSuchElementException exc) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add(exc.getMessage());
        response.setMessage("Data tidak ditemukan");
        return response;
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto<Boolean> nullPointerException(NullPointerException exc) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add("Data tidak ditemukan di basis data");
        response.getErrorMessage().add(exc.getLocalizedMessage());
        response.setMessage("Gagal mengambil data");
        return response;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto<Boolean> illegalArgumentException(IllegalArgumentException iae) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add(iae.getLocalizedMessage());
        response.setMessage("Gagal mengambil data");
        return response;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto<Boolean> notFoundException(NotFoundException nfe) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add(nfe.getLocalizedMessage());
        response.setMessage("Data Tidak Ditemukan.");
        return response;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto<Boolean> resouceNotFoundException(ResourceNotFoundException nfe) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.setPayload(false);
        response.getErrorMessage().add(nfe.getLocalizedMessage());
        response.setMessage(nfe.getCustomMessage());
        return response;
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<Boolean> internalAuthenticationServiceException(InternalAuthenticationServiceException iase) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add(iase.getLocalizedMessage());
        response.setMessage("Login Gagal.");
        return response;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<Boolean> constraintViolationException(ConstraintViolationException cv) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        for (ConstraintViolation<?> iterable_element : cv.getConstraintViolations()) {
            response.getErrorMessage().add(iterable_element.getMessage());
        }
        response.setMessage("Gagal menyimpan data");
        response.setPayload(false);
        return response;
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<Boolean> duplicateResourceException(DuplicateResourceException dre) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add(dre.getLocalizedMessage());
        response.setMessage(dre.getCustomMessage());
        response.setPayload(false);
        return response;
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<Boolean> dataIntegrityViolationException(DataIntegrityViolationException dre) {
        String msg = dre.getMessage();
        if (dre.getCause().getCause() instanceof SQLException) {
            SQLException e = (SQLException) dre.getCause().getCause();
            msg = e.getLocalizedMessage();
        }
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add(msg);
        response.setMessage(dre.getMessage());
        response.setPayload(false);
        return response;
    }

    @ExceptionHandler(CustomIllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<Boolean> customIllegalArgumentException(CustomIllegalArgumentException ciae) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add(ciae.getLocalizedMessage());
        response.setMessage(ciae.getCustomMessage());
        response.setPayload(false);
        return response;
    }

    @ExceptionHandler(UserIsLockedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseDto<Boolean> userIsLockedException(UserIsLockedException uile) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add(uile.getLocalizedMessage());
        response.setMessage(uile.getCustomMessage());
        response.setPayload(false);
        return response;
    }

    @ExceptionHandler(CustomUnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseDto<Boolean> customUnauthorizedException(CustomUnauthorizedException cue) {
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setStatus(false);
        response.getErrorMessage().add(cue.getLocalizedMessage());
        response.setMessage(cue.getCustomMessage());
        response.setPayload(false);
        return response;
    }
}
