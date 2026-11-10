package br.com.fiap.rootfix;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class RegrasDeNegocioTest extends ApiTestBase {
    private static final String EDICAO = "{\"titulo\":\"Buraco maior\",\"descricao\":\"Aumentou depois da chuva\"}";

    @Test
    void moradorAMaisDe3KmNaoEntraNaComunidade() throws Exception {
        Gestor gestor = gestorComComunidadeEm(BELA_VISTA_LAT, BELA_VISTA_LON);
        String moradorNaMooca = moradorEm(MOOCA_LAT, MOOCA_LON);

        mvc.perform(post("/comunidades/{id}/membros", gestor.comunidadeId()).header("Authorization", bearer(moradorNaMooca)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void moradorPertoEntraNaComunidade() throws Exception {
        Gestor gestor = gestorComComunidadeEm(BELA_VISTA_LAT, BELA_VISTA_LON);
        String morador = moradorEm(PERTO_LAT, PERTO_LON);

        mvc.perform(post("/comunidades/{id}/membros", gestor.comunidadeId()).header("Authorization", bearer(morador)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participo").value(true));
    }

    @Test
    void quemNaoEMembroNaoPublicaNaoApoiaNaoObserva() throws Exception {
        Gestor gestor = gestorComComunidadeEm(BELA_VISTA_LAT, BELA_VISTA_LON);
        String membro = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(membro, gestor.comunidadeId());
        long problema = publicar(membro, gestor.comunidadeId());

        String naoMembro = moradorEm(PERTO_LAT, PERTO_LON);

        mvc.perform(multipart("/comunidades/{id}/problemas", gestor.comunidadeId())
                        .param("titulo", "Lâmpada queimada")
                        .param("descricao", "Poste apagado")
                        .param("latitude", BELA_VISTA_LAT)
                        .param("longitude", BELA_VISTA_LON)
                        .header("Authorization", bearer(naoMembro)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/problemas/{id}/apoios", problema).header("Authorization", bearer(naoMembro)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/problemas/{id}/observacoes", problema).header("Authorization", bearer(naoMembro))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Também vi\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void umApoioPorUsuarioPorProblema() throws Exception {
        Gestor gestor = gestorComComunidadeEm(BELA_VISTA_LAT, BELA_VISTA_LON);
        String autor = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(autor, gestor.comunidadeId());
        long problema = publicar(autor, gestor.comunidadeId());

        String vizinho = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(vizinho, gestor.comunidadeId());

        mvc.perform(post("/problemas/{id}/apoios", problema).header("Authorization", bearer(vizinho)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApoios").value(1))
                .andExpect(jsonPath("$.apoiadoPorMim").value(true));

        mvc.perform(post("/problemas/{id}/apoios", problema).header("Authorization", bearer(vizinho)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApoios").value(0))
                .andExpect(jsonPath("$.apoiadoPorMim").value(false));

        mvc.perform(post("/problemas/{id}/apoios", problema).header("Authorization", bearer(vizinho)))
                .andExpect(jsonPath("$.totalApoios").value(1));
        mvc.perform(post("/problemas/{id}/apoios", problema).header("Authorization", bearer(autor)))
                .andExpect(jsonPath("$.totalApoios").value(2));
    }

    @Test
    void soOAutorEditaEExcluiESoAntesDaAdocao() throws Exception {
        Gestor gestor = gestorComComunidadeEm(BELA_VISTA_LAT, BELA_VISTA_LON);
        String autor = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(autor, gestor.comunidadeId());
        String vizinho = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(vizinho, gestor.comunidadeId());

        long problema = publicar(autor, gestor.comunidadeId());

        mvc.perform(put("/problemas/{id}", problema).header("Authorization", bearer(vizinho))
                        .contentType(MediaType.APPLICATION_JSON).content(EDICAO))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/problemas/{id}", problema).header("Authorization", bearer(vizinho)))
                .andExpect(status().isForbidden());

        mvc.perform(put("/problemas/{id}", problema).header("Authorization", bearer(autor))
                        .contentType(MediaType.APPLICATION_JSON).content(EDICAO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Buraco maior"));

        mvc.perform(post("/problemas/{id}/adotar", problema).header("Authorization", bearer(gestor.token())))
                .andExpect(status().isOk());

        mvc.perform(put("/problemas/{id}", problema).header("Authorization", bearer(autor))
                        .contentType(MediaType.APPLICATION_JSON).content(EDICAO))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(delete("/problemas/{id}", problema).header("Authorization", bearer(autor)))
                .andExpect(status().isUnprocessableContent());

        long outro = publicar(autor, gestor.comunidadeId());
        mvc.perform(delete("/problemas/{id}", outro).header("Authorization", bearer(autor)))
                .andExpect(status().isNoContent());
    }

    @Test
    void soOGestorDonoDaComunidadeAdota() throws Exception {
        Gestor dono = gestorComComunidadeEm(BELA_VISTA_LAT, BELA_VISTA_LON);
        Gestor outroGestor = gestorComComunidadeEm(BELA_VISTA_LAT, BELA_VISTA_LON);
        String autor = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(autor, dono.comunidadeId());
        long problema = publicar(autor, dono.comunidadeId());

        mvc.perform(post("/problemas/{id}/adotar", problema).header("Authorization", bearer(autor)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/problemas/{id}/adotar", problema).header("Authorization", bearer(outroGestor.token())))
                .andExpect(status().isForbidden());

        mvc.perform(post("/problemas/{id}/adotar", problema).header("Authorization", bearer(dono.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adotado").value(true))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        mvc.perform(post("/problemas/{id}/adotar", problema).header("Authorization", bearer(dono.token())))
                .andExpect(status().isConflict());
    }
}
