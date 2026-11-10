package br.com.fiap.rootfix;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ApiTestBase {
    protected static final String BELA_VISTA_LAT = "-23.5640";
    protected static final String BELA_VISTA_LON = "-46.6527";
    protected static final String PERTO_LAT = "-23.5578";
    protected static final String PERTO_LON = "-46.6556";
    protected static final String MOOCA_LAT = "-23.5578";
    protected static final String MOOCA_LON = "-46.5999";

    protected record Gestor(String token, long comunidadeId) {
    }

    @Autowired
    protected MockMvc mvc;

    protected String emailNovo() {
        return "teste-" + UUID.randomUUID() + "@rootfix.com";
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected String moradorEm(String latitude, String longitude) throws Exception {
        String corpo = """
                {"nome":"Morador Teste","email":"%s","senha":"Senha@1234","endereco":"Rua Teste, 10",
                 "latitude":%s,"longitude":%s}
                """.formatted(emailNovo(), latitude, longitude);
        String resposta = mvc.perform(post("/auth/registro/morador").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    protected Gestor gestorComComunidadeEm(String latitude, String longitude) throws Exception {
        String corpo = """
                {"nome":"Gestor Teste","email":"%s","senha":"Senha@1234","organizacao":"Associação Teste",
                 "comunidade":{"nome":"Comunidade Teste","descricao":"Teste","latitude":%s,"longitude":%s}}
                """.formatted(emailNovo(), latitude, longitude);
        String resposta = mvc.perform(post("/auth/registro/gestor").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(resposta, "$.token");

        String comunidades = mvc.perform(get("/usuarios/me/comunidades").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(comunidades, "$[0].id");
        return new Gestor(token, id.longValue());
    }

    protected void entrar(String tokenMorador, long comunidadeId) throws Exception {
        mvc.perform(post("/comunidades/{id}/membros", comunidadeId).header("Authorization", bearer(tokenMorador)))
                .andExpect(status().isCreated());
    }

    protected long publicar(String token, long comunidadeId) throws Exception {
        String resposta = mvc.perform(multipart("/comunidades/{id}/problemas", comunidadeId)
                        .param("titulo", "Buraco na rua")
                        .param("descricao", "Buraco grande perto da esquina")
                        .param("latitude", BELA_VISTA_LAT)
                        .param("longitude", BELA_VISTA_LON)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(resposta, "$.id");
        return id.longValue();
    }
}
