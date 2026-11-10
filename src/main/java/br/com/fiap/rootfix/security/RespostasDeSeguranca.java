package br.com.fiap.rootfix.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RespostasDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {
    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta,
            AuthenticationException excecao) throws IOException {
        String mensagem = requisicao.getHeader(HttpHeaders.AUTHORIZATION) == null
                ? "Faça login para acessar este recurso."
                : "Token inválido ou expirado. Faça login de novo.";
        escrever(resposta, requisicao, HttpStatus.UNAUTHORIZED, mensagem);
    }

    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta,
            AccessDeniedException excecao) throws IOException {
        escrever(resposta, requisicao, HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta ação.");
    }

    private void escrever(HttpServletResponse resposta, HttpServletRequest requisicao, HttpStatus status,
            String mensagem) throws IOException {
        resposta.setStatus(status.value());
        resposta.setCharacterEncoding("UTF-8");
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.getWriter().write("{\"status\":" + status.value()
                + ",\"erro\":\"" + status.getReasonPhrase()
                + "\",\"mensagem\":\"" + json(mensagem)
                + "\",\"caminho\":\"" + json(requisicao.getRequestURI())
                + "\",\"timestamp\":\"" + LocalDateTime.now()
                + "\",\"campos\":[]}");
    }

    private String json(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
