package br.com.fiap.rootfix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.JsonPath;

import br.com.fiap.rootfix.messaging.StatusAlteradoEvento;
import br.com.fiap.rootfix.service.NotificacaoService;

class NotificacoesTest extends ApiTestBase {
    @Autowired
    private NotificacaoService notificacaoService;

    private List<Map<String, Object>> notificacoes(String token) throws Exception {
        String resposta = mvc.perform(get("/notificacoes").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.content");
    }

    @Test
    void problemaNovoAvisaGestorComAltaEVizinhosComBaixa() throws Exception {
        Gestor gestor = gestorComComunidadeEm(BELA_VISTA_LAT, BELA_VISTA_LON);
        String autor = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(autor, gestor.comunidadeId());
        String vizinho = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(vizinho, gestor.comunidadeId());

        long problema = publicar(autor, gestor.comunidadeId());
        notificacaoService.problemaPublicado(problema);

        List<Map<String, Object>> doGestor = notificacoes(gestor.token());
        assertEquals(1, doGestor.size());
        assertEquals("ALTA", doGestor.get(0).get("prioridade"));

        List<Map<String, Object>> doVizinho = notificacoes(vizinho);
        assertEquals(1, doVizinho.size());
        assertEquals("BAIXA", doVizinho.get(0).get("prioridade"));

        assertEquals(0, notificacoes(autor).size());
    }

    @Test
    void mudancaDeStatusAvisaCadaMoradorUmaVezComAltaELevaORecadoDoGestor() throws Exception {
        Gestor gestor = gestorComComunidadeEm(BELA_VISTA_LAT, BELA_VISTA_LON);
        String autor = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(autor, gestor.comunidadeId());
        String vizinho = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(vizinho, gestor.comunidadeId());
        String membro = moradorEm(PERTO_LAT, PERTO_LON);
        entrar(membro, gestor.comunidadeId());

        long problema = publicar(autor, gestor.comunidadeId());
        mvc.perform(post("/problemas/{id}/apoios", problema).header("Authorization", bearer(vizinho)))
                .andExpect(status().isOk());
        mvc.perform(post("/problemas/{id}/observacoes", problema).header("Authorization", bearer(vizinho))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Também passo por aqui\"}"))
                .andExpect(status().isCreated());

        notificacaoService.statusAlterado(
                new StatusAlteradoEvento(problema, "PENDENTE", "EM_ANDAMENTO", "A equipe vai amanhã cedo"));

        for (String morador : List.of(autor, vizinho, membro)) {
            List<Map<String, Object>> avisos = notificacoes(morador);
            assertEquals(1, avisos.size());
            assertEquals("ALTA", avisos.get(0).get("prioridade"));
            assertEquals("O problema \"Buraco na rua\" mudou para Em andamento: A equipe vai amanhã cedo",
                    avisos.get(0).get("mensagem"));
        }
        assertEquals(0, notificacoes(gestor.token()).size());

        notificacaoService.statusAlterado(new StatusAlteradoEvento(problema, "EM_ANDAMENTO", "RESOLVIDO"));
        assertTrue(notificacoes(autor).stream()
                .anyMatch(n -> "O problema \"Buraco na rua\" mudou para Resolvido.".equals(n.get("mensagem"))));
    }
}
