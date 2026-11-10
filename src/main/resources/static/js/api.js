const Api = (() => {
  'use strict';

  const CHAVE = 'rootfix.sessao';

  class ApiErro extends Error {
    constructor(status, mensagem, campos) {
      super(mensagem);
      this.status = status;
      this.campos = Array.isArray(campos) ? campos : [];
    }
  }

  function tokenExpirado(token) {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const dados = JSON.parse(atob(base64));
      return typeof dados.exp === 'number' && dados.exp * 1000 < Date.now();
    } catch (e) {
      return false;
    }
  }

  function lerSessao() {
    try {
      const s = JSON.parse(localStorage.getItem(CHAVE));
      if (s && s.token && !tokenExpirado(s.token)) return s;
    } catch (e) { }
    return null;
  }

  let sessao = lerSessao();

  function guardarSessao(nova) {
    sessao = nova;
    try {
      if (nova) localStorage.setItem(CHAVE, JSON.stringify(nova));
      else localStorage.removeItem(CHAVE);
    } catch (e) { }
  }

  function mensagemPadrao(status, caminho) {
    if (status === 401) return caminho.startsWith('/auth') ? 'E-mail ou senha inválidos.' : 'Sua sessão expirou. Entre de novo.';
    if (status === 403) return 'Você não tem permissão para isso, ou esta função ainda não foi publicada na API.';
    if (status === 404) return 'Não encontrado.';
    if (status === 413) return 'Arquivo grande demais. O limite é 5 MB.';
    if (status >= 500) return 'A API teve um erro interno (' + status + '). Veja o log no Eclipse.';
    return 'A API recusou a requisição (' + status + ').';
  }

  async function chamar(metodo, caminho, corpo, opcoes) {
    const headers = { Accept: 'application/json' };
    if (sessao && sessao.token) headers.Authorization = 'Bearer ' + sessao.token;

    let body;
    if (corpo instanceof FormData) {
      body = corpo;
    } else if (corpo !== undefined) {
      headers['Content-Type'] = 'application/json';
      body = JSON.stringify(corpo);
    }

    let resposta;
    try {
      resposta = await fetch(caminho, { method: metodo, headers, body });
    } catch (e) {
      throw new ApiErro(0, 'Não foi possível falar com a API. Confira se ela está rodando.');
    }

    if (resposta.status === 401 && sessao) {
      guardarSessao(null);
      window.dispatchEvent(new Event('rootfix:sessao-expirada'));
    }

    if (!resposta.ok) {
      let dados = null;
      try { dados = await resposta.json(); } catch (e) { }
      const mensagem = dados && (dados.mensagem || dados.message || dados.detail);
      throw new ApiErro(resposta.status, mensagem || mensagemPadrao(resposta.status, caminho), dados && dados.campos);
    }

    if (opcoes && opcoes.blob) return resposta.blob();
    if (resposta.status === 204) return null;
    const texto = await resposta.text();
    return texto ? JSON.parse(texto) : null;
  }

  function pagina(res) {
    const meta = (res && res.page) || res || {};
    return {
      itens: (res && res.content) || (Array.isArray(res) ? res : []),
      numero: meta.number || 0,
      totalPaginas: meta.totalPages || 1,
      total: meta.totalElements != null ? meta.totalElements : ((res && res.content) || []).length
    };
  }

  function qs(params) {
    const p = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') p.set(k, v); });
    const s = p.toString();
    return s ? '?' + s : '';
  }

  async function aposAutenticar(resposta, email, senha) {
    if (resposta && resposta.token) {
      guardarSessao({ token: resposta.token, usuario: resposta.usuario || {} });
      return resposta;
    }
    return entrar(email, senha);
  }

  async function entrar(email, senha) {
    const r = await chamar('POST', '/auth/login', { email, senha });
    guardarSessao({ token: r.token, usuario: r.usuario || {} });
    return r;
  }

  async function saude() {
    try {
      const r = await fetch('/actuator/health', { headers: { Accept: 'application/json' } });
      return await r.json();
    } catch (e) {
      return { status: 'FORA DO AR', components: {} };
    }
  }

  return {
    ApiErro,
    pagina,
    sessao: () => sessao,
    sair: () => guardarSessao(null),

    entrar,
    registrarMorador: async (dados) => aposAutenticar(await chamar('POST', '/auth/registro/morador', dados), dados.email, dados.senha),
    registrarGestor: async (dados) => aposAutenticar(await chamar('POST', '/auth/registro/gestor', dados), dados.email, dados.senha),

    saude,
    categorias: () => chamar('GET', '/categorias'),

    minhasComunidades: () => chamar('GET', '/usuarios/me/comunidades'),
    comunidadesProximas: (page, size) => chamar('GET', '/comunidades/proximas' + qs({ page, size })),
    comunidade: (id) => chamar('GET', '/comunidades/' + id),
    criarComunidade: (dados) => chamar('POST', '/comunidades', dados),
    entrarNaComunidade: (id) => chamar('POST', '/comunidades/' + id + '/membros'),
    sairDaComunidade: (id) => chamar('DELETE', '/comunidades/' + id + '/membros'),

    feed: (id, ordem, page, size) => chamar('GET', '/comunidades/' + id + '/problemas' + qs({ ordem, page, size })),
    supervisionados: (id, page, size) => chamar('GET', '/comunidades/' + id + '/supervisionados' + qs({ page, size })),
    publicarProblema: (id, formData) => chamar('POST', '/comunidades/' + id + '/problemas', formData),
    problema: (id) => chamar('GET', '/problemas/' + id),
    foto: (id) => chamar('GET', '/problemas/' + id + '/foto', undefined, { blob: true }),
    editarProblema: (id, dados) => chamar('PUT', '/problemas/' + id, dados),
    excluirProblema: (id) => chamar('DELETE', '/problemas/' + id),
    apoiar: (id) => chamar('POST', '/problemas/' + id + '/apoios'),
    retirarApoio: (id) => chamar('DELETE', '/problemas/' + id + '/apoios'),
    adotar: (id) => chamar('POST', '/problemas/' + id + '/adotar'),
    triagem: (page, size) => chamar('GET', '/triagem' + qs({ page, size })),
    reclassificar: (id) => chamar('POST', '/problemas/' + id + '/reclassificar'),

    observacoes: (id) => chamar('GET', '/problemas/' + id + '/observacoes'),
    observar: (id, texto) => chamar('POST', '/problemas/' + id + '/observacoes', { texto }),
    excluirObservacao: (id) => chamar('DELETE', '/observacoes/' + id),

    notificacoes: (page, size) => chamar('GET', '/notificacoes' + qs({ page, size })),
    marcarLida: (id) => chamar('PATCH', '/notificacoes/' + id + '/lida')
  };
})();
