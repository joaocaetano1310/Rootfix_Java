package br.com.fiap.rootfix.exception;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.fiap.rootfix.dto.CampoErro;
import br.com.fiap.rootfix.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class TratadorGlobalDeErros {
    private static final Logger log = LoggerFactory.getLogger(TratadorGlobalDeErros.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResponse> regraDaApi(ApiException e, HttpServletRequest req) {
        return resposta(e.getStatus(), e.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> camposInvalidos(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<CampoErro> campos = new ArrayList<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.add(new CampoErro(f.getField(), f.getDefaultMessage())));
        e.getBindingResult().getGlobalErrors()
                .forEach(g -> campos.add(new CampoErro(g.getObjectName(), g.getDefaultMessage())));
        return resposta(HttpStatus.BAD_REQUEST, "Confira os campos informados.", req, campos);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponse> parametrosInvalidos(HandlerMethodValidationException e, HttpServletRequest req) {
        List<CampoErro> campos = new ArrayList<>();
        e.getParameterValidationResults().forEach(r -> r.getResolvableErrors().forEach(erro ->
                campos.add(new CampoErro(r.getMethodParameter().getParameterName(), erro.getDefaultMessage()))));
        return resposta(HttpStatus.BAD_REQUEST, "Confira os parâmetros informados.", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "O corpo da requisição não é um JSON válido.", req, List.of());
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class })
    public ResponseEntity<ErroResponse> parametroInvalido(Exception e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro ausente ou inválido na requisição.", req, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> semPermissao(AccessDeniedException e, HttpServletRequest req) {
        return resposta(HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta ação.", req, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> conflitoNoBanco(DataIntegrityViolationException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Os dados conflitam com um registro que já existe.", req, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Rota não encontrada.", req, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoInvalido(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado nesta rota.", req, List.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroResponse> arquivoGrande(MaxUploadSizeExceededException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONTENT_TOO_LARGE, "Arquivo grande demais. O limite é 5 MB.", req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInesperado(Exception e, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente de novo em instantes.", req, List.of());
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem, HttpServletRequest req,
            List<CampoErro> campos) {
        return ResponseEntity.status(status).body(ErroResponse.de(status, mensagem, req.getRequestURI(), campos));
    }
}
