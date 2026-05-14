package top.wjr.serenoj.common.exception;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.wjr.serenoj.common.result.CommonResult;

import java.io.IOException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return CommonResult.errorResponse(msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return CommonResult.errorResponse(msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<Void> handleIllegalArgument(IllegalArgumentException e) {
        return CommonResult.errorResponse(e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public CommonResult<Void> handleIOException(IOException e) {
        return CommonResult.errorResponse("文件处理失败：" + e.getMessage());
    }
}
