package br.com.fiap.rootfix;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class SegurancaTest extends ApiTestBase {
    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void tokenInvalidoRetorna401() throws Exception {
        mvc.perform(get("/usuarios/me").header("Authorization", "Bearer token.invalido.qualquer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void moradorNaoCriaComunidade403() throws Exception {
        String morador = moradorEm(PERTO_LAT, PERTO_LON);
        mvc.perform(post("/comunidades").header("Authorization", bearer(morador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"latitude\":-23.5,\"longitude\":-46.6}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void gestorNaoEntraEmComunidadeComoMembro403() throws Exception {
        Gestor gestor = gestorComComunidadeEm(BELA_VISTA_LAT, BELA_VISTA_LON);
        mvc.perform(post("/comunidades/{id}/membros", gestor.comunidadeId()).header("Authorization", bearer(gestor.token())))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ninguem@rootfix.com\",\"senha\":\"errada123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos."));
    }

    @Test
    void cadastroInvalidoRetorna400ComCampos() throws Exception {
        mvc.perform(post("/auth/registro/morador").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"nao-e-email\",\"senha\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").isNotEmpty());
    }
}
