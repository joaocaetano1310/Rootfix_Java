(() => {
  'use strict';

  const $ = (seletor, raiz = document) => raiz.querySelector(seletor);
  const $$ = (seletor, raiz = document) => Array.from(raiz.querySelectorAll(seletor));
  const app = () => $('#app');

  const ESC = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
  const esc = (v) => String(v == null ? '' : v).replace(/[&<>"']/g, (c) => ESC[c]);
  const num = (v) => Number(v) || 0;

  const PERFIL = { GESTOR: 'Gestor', MORADOR: 'Morador' };
  const GRAVIDADE = { BAIXA: 'Baixa', MEDIA: 'Média', ALTA: 'Alta', CRITICA: 'Crítica' };
  const STATUS = { PENDENTE: 'Pendente', EM_ANDAMENTO: 'Em andamento', RESOLVIDO: 'Resolvido', ARQUIVADO: 'Arquivado' };
  const TAMANHO_MAXIMO_FOTO = 5 * 1024 * 1024;
  const CONTAS_TESTE = { morador: 'morador@rootfix.com', gestor: 'gestor@rootfix.com', senha: 'Rootfix@2026' };

  const MARCA = '<svg class="marca-icone" viewBox="0 0 64 64" aria-hidden="true">'
    + '<rect width="64" height="64" rx="16" fill="currentColor" opacity=".14"/>'
    + '<path d="M32 11c-9.4 0-17 7.4-17 16.6C15 40 32 54 32 54s17-14 17-26.4C49 18.4 41.4 11 32 11z" fill="#f5b800"/>'
    + '<circle cx="32" cy="27.5" r="6.5" fill="#1d2433"/></svg>'
    + '<span>Root<b>Fix</b></span>';

  function usuario() {
    const s = Api.sessao();
    return (s && s.usuario) || {};
  }
  const souGestor = () => usuario().perfil === 'GESTOR';

  function primeiroNome(nome) {
    return String(nome || '').trim().split(/\s+/)[0] || 'vizinho';
  }

  function iniciais(nome) {
    const partes = String(nome || '?').trim().split(/\s+/);
    return ((partes[0] || '?')[0] + ((partes[1] || '')[0] || '')).toUpperCase();
  }

  function dataHora(v) {
    if (!v) return '';
    const d = new Date(v);
    if (isNaN(d)) return '';
    return d.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  function resumo(texto, max) {
    const t = String(texto || '');
    return t.length > max ? t.slice(0, max - 1) + '…' : t;
  }

  function km(valor) {
    const n = Number(valor);
    if (!isFinite(n)) return '';
    return n < 1 ? Math.round(n * 1000) + ' m daqui' : n.toFixed(1).replace('.', ',') + ' km daqui';
  }

  const vazio = (texto) => '<p class="vazio">' + texto + '</p>';
  const carregando = '<p class="vazio carregando">Carregando…</p>';

  let temporizadorAviso;
  function aviso(mensagem, tipo) {
    const t = $('#toast');
    t.textContent = mensagem;
    t.className = 'toast ' + (tipo || 'ok');
    t.hidden = false;
    clearTimeout(temporizadorAviso);
    temporizadorAviso = setTimeout(() => { t.hidden = true; }, 4500);
  }

  function erroHtml(e) {
    const campos = (e && e.campos) || [];
    const lista = campos.length
      ? '<ul>' + campos.map((c) => '<li><b>' + esc(c.campo) + ':</b> ' + esc(c.mensagem) + '</li>').join('') + '</ul>'
      : '';
    return '<div class="alerta perigo" role="alert">' + esc((e && e.message) || 'Algo deu errado.') + lista + '</div>';
  }

  function dadosDoForm(form) {
    return Object.fromEntries(new FormData(form).entries());
  }

  function aoEnviar(form, acao) {
    form.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      const botao = $('button[type=submit]', form);
      const caixa = $('.form-erro', form);
      if (caixa) caixa.innerHTML = '';
      if (botao) botao.disabled = true;
      try {
        await acao(dadosDoForm(form), form);
      } catch (e) {
        if (caixa) caixa.innerHTML = erroHtml(e);
        else aviso(e.message, 'erro');
      } finally {
        if (botao) botao.disabled = false;
      }
    });
  }

  function camposLocal(titulo, extra) {
    return '<fieldset class="local"><legend>' + titulo + '</legend>'
      + '<div class="grade-2">'
      + '<label>Latitude<input name="latitude" type="number" step="any" min="-90" max="90" required placeholder="-23.5505200"></label>'
      + '<label>Longitude<input name="longitude" type="number" step="any" min="-180" max="180" required placeholder="-46.6333090"></label>'
      + '</div>'
      + '<div class="linha-botoes"><button type="button" class="botao secundario pequeno" data-localizar>◎ Usar minha localização</button>'
      + (extra || '') + '</div>'
      + '</fieldset>';
  }

  function ligarLocalizacao(form) {
    const botao = $('[data-localizar]', form);
    if (!botao) return;
    const rotulo = botao.textContent;
    botao.addEventListener('click', () => {
      if (!navigator.geolocation) {
        aviso('Seu navegador não informa a localização. Preencha à mão.', 'erro');
        return;
      }
      botao.disabled = true;
      botao.textContent = 'Localizando…';
      navigator.geolocation.getCurrentPosition((pos) => {
        form.elements.latitude.value = pos.coords.latitude.toFixed(7);
        form.elements.longitude.value = pos.coords.longitude.toFixed(7);
        botao.disabled = false;
        botao.textContent = rotulo;
      }, () => {
        botao.disabled = false;
        botao.textContent = rotulo;
        aviso('Não deu para pegar sua localização. Preencha latitude e longitude à mão.', 'erro');
      }, { enableHighAccuracy: true, timeout: 10000 });
    });
  }

  function paginacao(alvo, p, carregar) {
    if (!alvo) return;
    if (p.totalPaginas <= 1) { alvo.innerHTML = ''; return; }
    alvo.innerHTML = '<button type="button" class="botao secundario pequeno" data-pag="ant" ' + (p.numero <= 0 ? 'disabled' : '') + '>← Anterior</button>'
      + '<span>Página ' + (p.numero + 1) + ' de ' + p.totalPaginas + '</span>'
      + '<button type="button" class="botao secundario pequeno" data-pag="prox" ' + (p.numero >= p.totalPaginas - 1 ? 'disabled' : '') + '>Próxima →</button>';
    $('[data-pag=ant]', alvo).onclick = () => carregar(p.numero - 1);
    $('[data-pag=prox]', alvo).onclick = () => carregar(p.numero + 1);
  }

  function seloGravidade(g) {
    if (!g) return '';
    return '<span class="etiqueta grav-' + esc(g) + '" title="Gravidade estimada pela IA">' + esc(GRAVIDADE[g] || g) + '</span>';
  }

  function seloSituacao(p) {
    if (!p.adotado) return '<span class="etiqueta aberta">Aguardando adoção</span>';
    return '<span class="etiqueta st-' + esc(p.status) + '">' + esc(STATUS[p.status] || p.status) + '</span>';
  }

  function seloCategoria(c) {
    return c ? '<span class="etiqueta neutra">' + esc(c.nome) + '</span>' : '';
  }

  function botaoApoio(p) {
    const marcado = !!p.apoiadoPorMim;
    return '<button type="button" class="apoio' + (marcado ? ' marcado' : '') + '" data-apoio="' + num(p.id) + '"'
      + ' aria-pressed="' + marcado + '" title="Também sofri com isso">'
      + '<span aria-hidden="true">▲</span> <b>' + num(p.totalApoios) + '</b>'
      + '<small>' + (marcado ? 'Você apoiou' : 'Apoiar') + '</small></button>';
  }

  function shell(conteudo) {
    const u = usuario();
    const hash = location.hash;
    const link = (href, icone, texto, extra) =>
      '<a href="' + href + '" class="' + (hash.startsWith(href) ? 'ativo' : '') + '"><span class="ico" aria-hidden="true">' + icone + '</span> <span>' + texto + '</span>' + (extra || '') + '</a>';

    app().innerHTML =
      '<div class="layout">'
      + '<aside class="lateral">'
      + '<a class="marca" href="#/inicio">' + MARCA + '</a>'
      + '<div class="rotulo-nav">' + (u.perfil === 'GESTOR' ? 'PAINEL DO GESTOR' : 'MEU BAIRRO') + '</div>'
      + '<nav aria-label="Menu principal">'
      + link('#/inicio', '⌂', 'Início')
      + link('#/comunidades', '◇', 'Comunidades')
      + (u.perfil === 'GESTOR' ? link('#/triagem', '✦', 'Triagem da IA') : '')
      + link('#/notificacoes', '◉', 'Notificações', '<b class="contador" data-contador hidden></b>')
      + link('#/status', '≋', 'Status da API')
      + '</nav>'
      + '<div class="faixa" aria-hidden="true"></div>'
      + '<div class="conta">'
      + '<div class="avatar" aria-hidden="true">' + esc(iniciais(u.nome)) + '</div>'
      + '<div><strong>' + esc(u.nome || 'Usuário') + '</strong><small>' + esc(PERFIL[u.perfil] || '') + (u.organizacao ? ' · ' + esc(u.organizacao) : '') + '</small></div>'
      + '<button type="button" class="sair" data-sair>Sair ↗</button>'
      + '</div>'
      + '</aside>'
      + '<main class="principal">' + conteudo
      + '<footer class="rodape">RootFix · zeladoria urbana por comunidade · Global Solution 2026/2 · FIAP</footer>'
      + '</main></div>';

    $('[data-sair]').onclick = () => { Api.sair(); location.hash = '#/entrar'; };
    atualizarContador();
  }

  function publico(conteudo) {
    app().innerHTML = '<div class="publico">'
      + '<header class="topo-publico"><a class="marca escura" href="#/entrar">' + MARCA + '</a>'
      + '<a class="botao secundario pequeno" href="#/entrar">Entrar</a></header>'
      + '<main class="principal">' + conteudo + '</main></div>';
  }

  async function atualizarContador() {
    try {
      const p = Api.pagina(await Api.notificacoes(0, 50));
      const n = p.itens.filter((x) => !x.lida).length;
      const el = $('[data-contador]');
      if (el) { el.textContent = n; el.hidden = n === 0; }
    } catch (e) { }
  }

  function cabecalho(sobrelinha, titulo, texto, acoes) {
    return '<header class="cabecalho"><div>'
      + '<div class="sobrelinha">' + sobrelinha + '</div>'
      + '<h1>' + titulo + '</h1>'
      + (texto ? '<p class="suave">' + texto + '</p>' : '')
      + '</div>' + (acoes ? '<div class="linha-botoes">' + acoes + '</div>' : '') + '</header>';
  }

  function telaAcesso(aba, formulario) {
    app().innerHTML =
      '<div class="acesso">'
      + '<section class="acesso-historia">'
      + '<a class="marca" href="#/entrar">' + MARCA + '</a>'
      + '<div>'
      + '<div class="sobrelinha clara">ZELADORIA URBANA POR COMUNIDADE</div>'
      + '<h1>Viu um problema na rua?<br><em>Seus vizinhos também.</em></h1>'
      + '<p>Publique o buraco, a lâmpada queimada ou o bueiro entupido. A vizinhança apoia, a IA estima a gravidade e a associação do bairro adota e acompanha até resolver.</p>'
      + '<div class="rua" aria-hidden="true"><i></i><span class="pino"></span></div>'
      + '<ol class="passos"><li><b>1</b>Publique com foto</li><li><b>2</b>Vizinhos apoiam</li><li><b>3</b>Gestor adota</li></ol>'
      + '</div>'
      + '<small>Global Solution 2026/2 · FIAP · grupo Rooted</small>'
      + '</section>'
      + '<main class="acesso-form">'
      + '<nav class="abas" aria-label="Acesso">'
      + '<a href="#/entrar" class="' + (aba === 'entrar' ? 'ativa' : '') + '">Entrar</a>'
      + '<a href="#/cadastro/morador" class="' + (aba === 'morador' ? 'ativa' : '') + '">Sou morador</a>'
      + '<a href="#/cadastro/gestor" class="' + (aba === 'gestor' ? 'ativa' : '') + '">Sou gestor</a>'
      + '</nav>'
      + formulario
      + '<p class="nota">A API está no ar? <a href="#/status">Ver status</a> · <a href="/swagger-ui.html" target="_blank" rel="noopener">Abrir Swagger ↗</a></p>'
      + '</main></div>';
  }

  function telaEntrar() {
    telaAcesso('entrar',
      '<div class="sobrelinha">BEM-VINDO DE VOLTA</div>'
      + '<h2>Entre na sua conta</h2>'
      + '<p class="suave">Acompanhe o que está acontecendo nas comunidades do seu bairro.</p>'
      + '<form id="form-entrar" class="formulario">'
      + '<div class="form-erro"></div>'
      + '<label>E-mail<input name="email" type="email" required autocomplete="username" placeholder="voce@email.com"></label>'
      + '<label>Senha<input name="senha" type="password" required autocomplete="current-password"></label>'
      + '<button type="submit" class="botao largo">Entrar →</button>'
      + '</form>'
      + '<p class="suave">Ainda não tem conta? <a href="#/cadastro/morador">Cadastre-se como morador</a> ou <a href="#/cadastro/gestor">como gestor</a>.</p>'
      + '<div class="contas-teste"><strong>Contas de teste</strong>'
      + '<p>Senha das duas: <code>' + esc(CONTAS_TESTE.senha) + '</code></p>'
      + '<div class="linha-botoes">'
      + '<button type="button" class="botao secundario pequeno" data-conta="morador">Entrar como morador</button>'
      + '<button type="button" class="botao secundario pequeno" data-conta="gestor">Entrar como gestor</button>'
      + '</div></div>');

    const form = $('#form-entrar');
    aoEnviar(form, async (d) => {
      await Api.entrar(d.email.trim(), d.senha);
      location.hash = '#/inicio';
    });
    $$('[data-conta]').forEach((b) => {
      b.onclick = () => {
        form.elements.email.value = CONTAS_TESTE[b.dataset.conta];
        form.elements.senha.value = CONTAS_TESTE.senha;
        form.requestSubmit();
      };
    });
  }

  function telaCadastro(tipo) {
    const gestor = tipo === 'gestor';
    const camposPessoa =
      '<label>Nome completo<input name="nome" required maxlength="120" autocomplete="name"></label>'
      + '<label>E-mail<input name="email" type="email" required maxlength="150" autocomplete="email"></label>'
      + '<label>Senha<input name="senha" type="password" required minlength="8" autocomplete="new-password"><small>Mínimo de 8 caracteres.</small></label>';

    const corpo = gestor
      ? camposPessoa
        + '<label>Organização<input name="organizacao" required maxlength="150" placeholder="Associação de Moradores da Vila X"></label>'
        + '<h3 class="subtitulo-form">Sua primeira comunidade</h3>'
        + '<label>Nome da comunidade<input name="nomeComunidade" required maxlength="120" placeholder="Vila X - Rua das Flores e arredores"></label>'
        + '<label>Descrição<textarea name="descricao" maxlength="500" rows="2" placeholder="Que área ela cobre?"></textarea></label>'
        + camposLocal('Centro da comunidade (moradores a até 3 km poderão entrar)')
      : camposPessoa
        + '<label>Endereço<input name="endereco" required maxlength="255" autocomplete="street-address" placeholder="Rua, número, bairro"></label>'
        + camposLocal('Coordenadas do endereço (usadas para achar comunidades a até 3 km)');

    telaAcesso(tipo,
      '<div class="sobrelinha">' + (gestor ? 'PARA ASSOCIAÇÕES E SECRETARIAS' : 'PARA QUEM MORA NO BAIRRO') + '</div>'
      + '<h2>' + (gestor ? 'Crie sua conta de gestor' : 'Crie sua conta de morador') + '</h2>'
      + '<p class="suave">' + (gestor
        ? 'Você cria comunidades e adota os problemas que vai acompanhar.'
        : 'Você entra nas comunidades perto de casa e publica o que precisa de conserto.') + '</p>'
      + '<form id="form-cadastro" class="formulario"><div class="form-erro"></div>' + corpo
      + '<button type="submit" class="botao largo">Criar conta →</button></form>');

    const form = $('#form-cadastro');
    ligarLocalizacao(form);
    aoEnviar(form, async (d) => {
      const base = { nome: d.nome.trim(), email: d.email.trim(), senha: d.senha };
      if (gestor) {
        await Api.registrarGestor(Object.assign(base, {
          organizacao: d.organizacao.trim(),
          comunidade: {
            nome: d.nomeComunidade.trim(),
            descricao: (d.descricao || '').trim(),
            latitude: parseFloat(d.latitude),
            longitude: parseFloat(d.longitude)
          }
        }));
      } else {
        await Api.registrarMorador(Object.assign(base, {
          endereco: d.endereco.trim(),
          latitude: parseFloat(d.latitude),
          longitude: parseFloat(d.longitude)
        }));
      }
      aviso('Conta criada. Bem-vindo ao RootFix!');
      location.hash = '#/inicio';
    });
  }

  function cartaoComunidade(c, acao) {
    const gestor = c.gestor || {};
    const meta = [];
    if (c.totalMembros != null) meta.push(num(c.totalMembros) + (num(c.totalMembros) === 1 ? ' membro' : ' membros'));
    if (c.distanciaKm != null) meta.push(km(c.distanciaKm));
    if (c.souGestor) meta.push('você é o gestor');
    return '<article class="cartao-comunidade">'
      + '<div class="selo-comunidade" aria-hidden="true">' + esc(iniciais(c.nome)) + '</div>'
      + '<div class="corpo">'
      + '<h3><a href="#/comunidades/' + num(c.id) + '">' + esc(c.nome) + '</a></h3>'
      + (c.descricao ? '<p>' + esc(resumo(c.descricao, 140)) + '</p>' : '')
      + '<p class="meta">Gestão: ' + esc(gestor.nome || '—') + (gestor.organizacao ? ' · ' + esc(gestor.organizacao) : '') + '</p>'
      + (meta.length ? '<p class="meta">' + meta.map(esc).join(' · ') + '</p>' : '')
      + '</div>'
      + (acao || '<a class="botao secundario pequeno" href="#/comunidades/' + num(c.id) + '">Abrir</a>')
      + '</article>';
  }

  async function telaInicio() {
    const u = usuario();
    const gestor = souGestor();
    shell(
      cabecalho(gestor ? 'PAINEL DO GESTOR' : 'SEU BAIRRO',
        'Olá, ' + esc(primeiroNome(u.nome)) + '.',
        gestor ? 'Veja o que os moradores estão relatando e decida o que adotar primeiro.'
               : 'Veja o que está acontecendo nas comunidades perto de você.',
        '<a class="botao" href="#/comunidades">' + (gestor ? '＋ Nova comunidade' : 'Encontrar comunidades') + '</a>')
      + '<section class="destaque">'
      + '<div><span class="pilula">COMO FUNCIONA</span>'
      + '<h2>Cada apoio conta.</h2>'
      + '<p>' + (gestor
        ? 'No seu feed, os problemas aparecem ordenados pela gravidade estimada pela IA e pelo número de apoios. Adote o que vai resolver e acompanhe o status no painel.'
        : 'Problemas com mais apoios sobem no feed e chegam primeiro ao gestor. Se você também sofre com algo que já foi publicado, apoie em vez de publicar de novo.') + '</p></div>'
      + '<div class="destaque-arte" aria-hidden="true"><i></i><i></i><i></i></div>'
      + '</section>'
      + '<section class="numeros">'
      + '<article><span>Minhas comunidades</span><strong data-n="comunidades">–</strong><small>' + (gestor ? 'Que você gerencia' : 'Em que você participa') + '</small></article>'
      + '<article><span>Avisos não lidos</span><strong data-n="avisos">–</strong><small><a href="#/notificacoes">Ver notificações →</a></small></article>'
      + '<article><span>Seu perfil</span><strong class="texto">' + esc(PERFIL[u.perfil] || '—') + '</strong><small>' + esc(u.organizacao || u.email || '') + '</small></article>'
      + '</section>'
      + '<section class="painel"><div class="titulo-secao"><h2>Minhas comunidades</h2><a href="#/comunidades">Ver todas →</a></div>'
      + '<div class="grade-cartoes" data-lista="minhas">' + carregando + '</div></section>');

    const [comunidades, notificacoes] = await Promise.allSettled([Api.minhasComunidades(), Api.notificacoes(0, 50)]);
    const alvo = $('[data-lista=minhas]');
    if (comunidades.status === 'fulfilled') {
      const lista = Array.isArray(comunidades.value) ? comunidades.value : Api.pagina(comunidades.value).itens;
      $('[data-n=comunidades]').textContent = lista.length;
      alvo.innerHTML = lista.length
        ? lista.map((c) => cartaoComunidade(c)).join('')
        : vazio(gestor ? 'Você ainda não tem comunidades. <a href="#/comunidades">Crie a primeira →</a>'
                       : 'Você ainda não participa de nenhuma comunidade. <a href="#/comunidades">Veja as que ficam perto de você →</a>');
    } else {
      alvo.innerHTML = erroHtml(comunidades.reason);
    }
    if (notificacoes.status === 'fulfilled') {
      $('[data-n=avisos]').textContent = Api.pagina(notificacoes.value).itens.filter((n) => !n.lida).length;
    }
  }

  async function telaComunidades() {
    const gestor = souGestor();
    shell(
      cabecalho('COMUNIDADES', gestor ? 'Suas comunidades' : 'Comunidades do seu bairro',
        gestor ? 'Crie comunidades para os bairros que a sua organização atende.'
               : 'Você pode participar de várias, desde que fiquem a até 3 km do seu endereço.')
      + '<section class="painel"><div class="titulo-secao"><h2>' + (gestor ? 'Que eu gerencio' : 'Em que eu participo') + '</h2></div>'
      + '<div class="grade-cartoes" data-lista="minhas">' + carregando + '</div></section>'
      + (gestor
        ? '<section class="painel"><div class="titulo-secao"><h2>Nova comunidade</h2></div>'
          + '<form id="form-comunidade" class="formulario estreito"><div class="form-erro"></div>'
          + '<label>Nome<input name="nome" required maxlength="120"></label>'
          + '<label>Descrição<textarea name="descricao" maxlength="500" rows="2"></textarea></label>'
          + camposLocal('Centro da comunidade')
          + '<button type="submit" class="botao">Criar comunidade</button></form></section>'
        : '<section class="painel"><div class="titulo-secao"><h2>Perto de você <small>até 3 km</small></h2></div>'
          + '<div class="grade-cartoes" data-lista="proximas">' + carregando + '</div>'
          + '<div class="paginacao" data-paginas="proximas"></div></section>'));

    try {
      const minhas = await Api.minhasComunidades();
      const lista = Array.isArray(minhas) ? minhas : Api.pagina(minhas).itens;
      $('[data-lista=minhas]').innerHTML = lista.length ? lista.map((c) => cartaoComunidade(c)).join('')
        : vazio(gestor ? 'Nenhuma comunidade ainda. Use o formulário abaixo.' : 'Você ainda não entrou em nenhuma.');
    } catch (e) {
      $('[data-lista=minhas]').innerHTML = erroHtml(e);
    }

    if (gestor) {
      const form = $('#form-comunidade');
      ligarLocalizacao(form);
      aoEnviar(form, async (d) => {
        const nova = await Api.criarComunidade({
          nome: d.nome.trim(),
          descricao: (d.descricao || '').trim(),
          latitude: parseFloat(d.latitude),
          longitude: parseFloat(d.longitude)
        });
        aviso('Comunidade criada.');
        location.hash = nova && nova.id ? '#/comunidades/' + num(nova.id) : '#/comunidades';
      });
    } else {
      carregarProximas(0);
    }
  }

  async function carregarProximas(numero) {
    const alvo = $('[data-lista=proximas]');
    alvo.innerHTML = carregando;
    try {
      const p = Api.pagina(await Api.comunidadesProximas(numero, 6));
      alvo.innerHTML = p.itens.length
        ? p.itens.map((c) => cartaoComunidade(c, c.participo
          ? '<a class="botao secundario pequeno" href="#/comunidades/' + num(c.id) + '">Abrir</a>'
          : '<button type="button" class="botao pequeno" data-entrar="' + num(c.id) + '">Entrar</button>')).join('')
        : vazio('Nenhuma comunidade a até 3 km do seu endereço por enquanto.');
      paginacao($('[data-paginas=proximas]'), p, carregarProximas);
      $$('[data-entrar]', alvo).forEach((b) => {
        b.onclick = async () => {
          b.disabled = true;
          try {
            await Api.entrarNaComunidade(b.dataset.entrar);
            aviso('Você entrou na comunidade.');
            location.hash = '#/comunidades/' + b.dataset.entrar;
          } catch (e) {
            aviso(e.message, 'erro');
            b.disabled = false;
          }
        };
      });
    } catch (e) {
      alvo.innerHTML = erroHtml(e);
    }
  }

  function cartaoProblema(p, podeAdotar) {
    const id = num(p.id);
    return '<article class="problema">'
      + '<a class="miniatura" href="#/problemas/' + id + '"' + (p.temFoto ? ' data-foto="' + id + '"' : '') + '>'
      + (p.temFoto ? '' : '<span>sem foto</span>') + '</a>'
      + '<div class="conteudo">'
      + '<div class="etiquetas">' + seloSituacao(p) + seloGravidade(p.gravidadeIa) + seloCategoria(p.categoria) + '</div>'
      + '<h3><a href="#/problemas/' + id + '">' + esc(p.titulo) + '</a></h3>'
      + '<p class="resumo">' + esc(resumo(p.descricao, 180)) + '</p>'
      + '<p class="meta">' + [
        'por ' + esc((p.autor && p.autor.nome) || '—'),
        esc(dataHora(p.criadoEm)),
        '<a href="#/problemas/' + id + '">' + num(p.totalObservacoes) + ' observaç' + (num(p.totalObservacoes) === 1 ? 'ão' : 'ões') + '</a>'
      ].filter(Boolean).join(' · ') + '</p>'
      + '</div>'
      + '<div class="acoes">' + botaoApoio(p)
      + (podeAdotar && !p.adotado ? '<button type="button" class="botao pequeno" data-adotar="' + id + '">Adotar</button>' : '')
      + '</div></article>';
  }

  function ligarAcoesProblema(raiz, depoisDeAdotar) {
    $$('[data-apoio]', raiz).forEach((b) => {
      b.onclick = async () => {
        const id = b.dataset.apoio;
        const marcado = b.getAttribute('aria-pressed') === 'true';
        b.disabled = true;
        try {
          const r = marcado ? await Api.retirarApoio(id) : await Api.apoiar(id);
          const total = r && r.totalApoios != null ? r.totalApoios : num($('b', b).textContent) + (marcado ? -1 : 1);
          const agora = r && r.apoiadoPorMim != null ? r.apoiadoPorMim : !marcado;
          b.outerHTML = botaoApoio({ id, totalApoios: total, apoiadoPorMim: agora });
          ligarAcoesProblema(raiz, depoisDeAdotar);
        } catch (e) {
          aviso(e.message, 'erro');
          b.disabled = false;
        }
      };
    });
    $$('[data-adotar]', raiz).forEach((b) => {
      b.onclick = async () => {
        if (!confirm('Adotar este problema? Ele passa a ser acompanhado no painel do gestor e o autor será avisado.')) return;
        b.disabled = true;
        try {
          await Api.adotar(b.dataset.adotar);
          aviso('Problema adotado. Status: Pendente.');
          if (depoisDeAdotar) depoisDeAdotar();
        } catch (e) {
          aviso(e.message, 'erro');
          b.disabled = false;
        }
      };
    });
  }

  function carregarFotos(raiz) {
    $$('[data-foto]', raiz).forEach(async (el) => {
      try {
        const blob = await Api.foto(el.dataset.foto);
        const img = document.createElement('img');
        img.alt = 'Foto do problema';
        img.src = URL.createObjectURL(blob);
        el.innerHTML = '';
        el.appendChild(img);
      } catch (e) {
        el.innerHTML = '<span>foto indisponível</span>';
      }
    });
  }

  async function telaComunidade(id) {
    const c = await Api.comunidade(id);
    const gestorDono = !!c.souGestor;
    const membro = !!c.participo || gestorDono;
    const gestor = c.gestor || {};

    shell(
      '<a class="voltar" href="#/comunidades">← Comunidades</a>'
      + cabecalho('COMUNIDADE', esc(c.nome),
        (c.descricao ? esc(c.descricao) + '<br>' : '')
        + '<span class="meta">Gestão: ' + esc(gestor.nome || '—') + (gestor.organizacao ? ' · ' + esc(gestor.organizacao) : '')
        + (c.totalMembros != null ? ' · ' + num(c.totalMembros) + ' membros' : '') + '</span>',
        (!gestorDono && c.participo ? '<button type="button" class="botao secundario" data-sair-comunidade>Sair da comunidade</button>' : '')
        + (!membro && !souGestor() ? '<button type="button" class="botao" data-entrar-comunidade>Entrar na comunidade</button>' : ''))
      + (membro ? '<div class="destaques" data-destaques></div>' : '')
      + '<nav class="abas-pagina" role="tablist">'
      + '<button type="button" role="tab" class="ativa" data-aba="feed">Feed</button>'
      + '<button type="button" role="tab" data-aba="comunicados">Comunicados</button>'
      + '<button type="button" role="tab" data-aba="supervisionados">Supervisionados</button>'
      + '</nav>'
      + '<section data-painel="feed">'
      + (membro ? formularioPublicar() : '<div class="alerta info">Entre na comunidade para publicar, apoiar e comentar.</div>')
      + '<div class="barra"><h2>Problemas relatados</h2><label class="rotulo-linha">Ordenar por '
      + '<select data-ordem>'
      + (gestorDono ? '<option value="gravidade">Gravidade (IA) e apoios</option>' : '')
      + '<option value="recentes">Mais recentes</option><option value="apoiados">Mais apoiados</option></select></label></div>'
      + '<div class="lista-problemas" data-lista="feed">' + carregando + '</div>'
      + '<div class="paginacao" data-paginas="feed"></div>'
      + '</section>'
      + '<section data-painel="comunicados" hidden>'
      + (gestorDono ? formularioComunicado() : '<p class="suave">Avisos oficiais do gestor da comunidade. Os destacados ficam no topo.</p>')
      + '<div class="lista-comunicados" data-lista="comunicados">' + carregando + '</div>'
      + '<div class="paginacao" data-paginas="comunicados"></div>'
      + '</section>'
      + '<section data-painel="supervisionados" hidden>'
      + '<p class="suave">Problemas que o gestor adotou. O status é atualizado pelo painel do gestor.</p>'
      + '<div class="lista-problemas" data-lista="supervisionados">' + carregando + '</div>'
      + '<div class="paginacao" data-paginas="supervisionados"></div>'
      + '</section>');

    let ordem = $('[data-ordem]').value;

    async function carregarFeed(numero) {
      const alvo = $('[data-lista=feed]');
      alvo.innerHTML = carregando;
      try {
        const p = Api.pagina(await Api.feed(id, ordem, numero, 10));
        alvo.innerHTML = p.itens.length ? p.itens.map((x) => cartaoProblema(x, gestorDono)).join('')
          : vazio('Nenhum problema publicado ainda.' + (membro ? ' Seja o primeiro.' : ''));
        paginacao($('[data-paginas=feed]'), p, carregarFeed);
        ligarAcoesProblema(alvo, () => carregarFeed(numero));
        carregarFotos(alvo);
      } catch (e) {
        alvo.innerHTML = erroHtml(e);
      }
    }

    async function carregarSupervisionados(numero) {
      const alvo = $('[data-lista=supervisionados]');
      alvo.innerHTML = carregando;
      try {
        const p = Api.pagina(await Api.supervisionados(id, numero, 10));
        alvo.innerHTML = p.itens.length ? p.itens.map((x) => cartaoProblema(x, false)).join('')
          : vazio('Nenhum problema adotado ainda.');
        paginacao($('[data-paginas=supervisionados]'), p, carregarSupervisionados);
        ligarAcoesProblema(alvo);
        carregarFotos(alvo);
      } catch (e) {
        alvo.innerHTML = erroHtml(e);
      }
    }

    async function carregarDestaques() {
      const alvo = $('[data-destaques]');
      if (!alvo) return;
      try {
        const p = Api.pagina(await Api.comunicados(id, 0, 3));
        const destacados = p.itens.filter((x) => x.destacado);
        alvo.innerHTML = destacados.map((x) => cartaoComunicado(x, false)).join('');
      } catch (e) {
        alvo.innerHTML = '';
      }
    }

    async function carregarComunicados(numero) {
      const alvo = $('[data-lista=comunicados]');
      alvo.innerHTML = carregando;
      try {
        const p = Api.pagina(await Api.comunicados(id, numero, 10));
        alvo.innerHTML = p.itens.length ? p.itens.map((x) => cartaoComunicado(x, gestorDono)).join('')
          : vazio(membro ? 'Nenhum comunicado publicado ainda.' : 'Entre na comunidade para ver os comunicados.');
        paginacao($('[data-paginas=comunicados]'), p, carregarComunicados);
        $$('[data-destacar]', alvo).forEach((b) => {
          b.onclick = async () => {
            const x = p.itens.find((i) => String(i.id) === b.dataset.destacar);
            try {
              await Api.editarComunicado(x.id, { titulo: x.titulo, texto: x.texto, destacado: !x.destacado });
              carregarComunicados(numero);
              carregarDestaques();
            } catch (e) { aviso(e.message, 'erro'); }
          };
        });
        $$('[data-excluir-comunicado]', alvo).forEach((b) => {
          b.onclick = async () => {
            if (!confirm('Excluir este comunicado?')) return;
            try { await Api.excluirComunicado(b.dataset.excluirComunicado); carregarComunicados(numero); carregarDestaques(); }
            catch (e) { aviso(e.message, 'erro'); }
          };
        });
      } catch (e) {
        alvo.innerHTML = membro ? erroHtml(e) : vazio('Entre na comunidade para ver os comunicados.');
      }
    }

    $$('[data-aba]').forEach((b) => {
      b.onclick = () => {
        $$('[data-aba]').forEach((x) => x.classList.toggle('ativa', x === b));
        $$('[data-painel]').forEach((s) => { s.hidden = s.dataset.painel !== b.dataset.aba; });
        if (b.dataset.aba === 'supervisionados') carregarSupervisionados(0);
        if (b.dataset.aba === 'comunicados') carregarComunicados(0);
      };
    });

    const formComunicado = $('#form-comunicado');
    if (formComunicado) {
      aoEnviar(formComunicado, async (d, f) => {
        await Api.publicarComunicado(id, { titulo: d.titulo.trim(), texto: d.texto.trim(), destacado: f.elements.destacado.checked });
        f.reset();
        const painel = f.closest('details');
        if (painel) painel.open = false;
        aviso('Comunicado publicado. Os membros vão receber o aviso.');
        carregarComunicados(0);
        carregarDestaques();
      });
    }
    $('[data-ordem]').onchange = (ev) => { ordem = ev.target.value; carregarFeed(0); };

    const sair = $('[data-sair-comunidade]');
    if (sair) {
      sair.onclick = async () => {
        if (!confirm('Sair desta comunidade? Você pode entrar de novo depois.')) return;
        try { await Api.sairDaComunidade(id); aviso('Você saiu da comunidade.'); location.hash = '#/comunidades'; }
        catch (e) { aviso(e.message, 'erro'); }
      };
    }
    const entrarBtn = $('[data-entrar-comunidade]');
    if (entrarBtn) {
      entrarBtn.onclick = async () => {
        try { await Api.entrarNaComunidade(id); aviso('Você entrou na comunidade.'); rotear(); }
        catch (e) { aviso(e.message, 'erro'); }
      };
    }

    const form = $('#form-publicar');
    if (form) {
      ligarLocalizacao(form);
      const centro = $('[data-centro]', form);
      if (centro) {
        centro.onclick = () => {
          form.elements.latitude.value = c.latitude != null ? c.latitude : '';
          form.elements.longitude.value = c.longitude != null ? c.longitude : '';
        };
      }
      aoEnviar(form, async (d, f) => {
        const arquivo = f.elements.foto.files[0];
        if (arquivo && arquivo.size > TAMANHO_MAXIMO_FOTO) {
          throw new Api.ApiErro(413, 'A foto tem mais de 5 MB. Escolha uma menor.');
        }
        const fd = new FormData();
        fd.append('titulo', d.titulo.trim());
        fd.append('descricao', d.descricao.trim());
        fd.append('latitude', d.latitude);
        fd.append('longitude', d.longitude);
        if (arquivo) fd.append('foto', arquivo);
        await Api.publicarProblema(id, fd);
        f.reset();
        const painel = f.closest('details');
        if (painel) painel.open = false;
        aviso('Problema publicado. A IA vai classificar em instantes.');
        carregarFeed(0);
      });
    }

    carregarDestaques();
    carregarFeed(0);
  }

  function cartaoComunicado(x, podeGerir) {
    return '<article class="comunicado' + (x.destacado ? ' destacado' : '') + '">'
      + '<div class="etiquetas">' + (x.destacado ? '<span class="etiqueta fixado">📌 Em destaque</span>' : '<span class="etiqueta neutra">Comunicado</span>') + '</div>'
      + '<h3>' + esc(x.titulo) + '</h3>'
      + '<p class="texto-livre">' + esc(x.texto) + '</p>'
      + '<p class="meta">' + esc((x.autor && x.autor.nome) || 'Gestor') + ' · ' + esc(dataHora(x.criadoEm)) + '</p>'
      + (podeGerir ? '<div class="acoes-comunicado">'
        + '<button type="button" class="botao secundario pequeno" data-destacar="' + num(x.id) + '">' + (x.destacado ? 'Tirar destaque' : 'Destacar') + '</button>'
        + '<button type="button" class="link-perigo" data-excluir-comunicado="' + num(x.id) + '">excluir</button></div>' : '')
      + '</article>';
  }

  function formularioComunicado() {
    return '<details class="publicar"><summary>＋ Publicar um comunicado</summary>'
      + '<form id="form-comunicado" class="formulario"><div class="form-erro"></div>'
      + '<label>Título<input name="titulo" required maxlength="120" placeholder="Falta de água na quinta-feira"></label>'
      + '<label>Texto<textarea name="texto" required maxlength="2000" rows="4" placeholder="O que os moradores precisam saber?"></textarea></label>'
      + '<label class="caixa"><input type="checkbox" name="destacado"> Destacar no topo da comunidade</label>'
      + '<button type="submit" class="botao">Publicar comunicado</button>'
      + '</form></details>';
  }

  function formularioPublicar() {
    return '<details class="publicar"><summary>＋ Publicar um problema</summary>'
      + '<form id="form-publicar" class="formulario"><div class="form-erro"></div>'
      + '<label>Título<input name="titulo" required maxlength="120" placeholder="Buraco grande na Rua das Flores, 120"></label>'
      + '<label>Descrição<textarea name="descricao" required maxlength="1000" rows="3" placeholder="O que está acontecendo, desde quando e qual o risco?"></textarea></label>'
      + '<label>Foto<input name="foto" type="file" accept="image/jpeg,image/png,image/webp"><small>JPG, PNG ou WEBP, até 5 MB. A IA avalia a gravidade pela foto: sem foto, o problema fica sem gravidade.</small></label>'
      + camposLocal('Onde fica o problema', '<button type="button" class="botao secundario pequeno" data-centro>Usar o centro da comunidade</button>')
      + '<button type="submit" class="botao">Publicar</button>'
      + '</form></details>';
  }

  async function telaProblema(id) {
    const p = await Api.problema(id);
    const editavel = !!p.souAutor && !p.adotado;
    const comunidade = p.comunidade || {};
    const lat = p.latitude, lng = p.longitude;
    const mapa = lat != null && lng != null
      ? 'https://www.openstreetmap.org/?mlat=' + encodeURIComponent(lat) + '&mlon=' + encodeURIComponent(lng) + '#map=18/' + encodeURIComponent(lat) + '/' + encodeURIComponent(lng)
      : null;

    shell(
      '<a class="voltar" href="#/comunidades/' + num(comunidade.id) + '">← ' + esc(comunidade.nome || 'Comunidade') + '</a>'
      + '<header class="cabecalho"><div>'
      + '<div class="etiquetas">' + seloSituacao(p) + seloGravidade(p.gravidadeIa) + seloCategoria(p.categoria) + '</div>'
      + '<h1>' + esc(p.titulo) + '</h1>'
      + '<p class="meta">' + ['por ' + esc((p.autor && p.autor.nome) || '—'), esc(dataHora(p.criadoEm))].filter(Boolean).join(' · ') + '</p>'
      + '</div></header>'
      + '<div class="colunas">'
      + '<div class="coluna-principal">'
      + '<figure class="foto-grande"' + (p.temFoto ? ' data-foto="' + num(p.id) + '"' : '') + '>' + (p.temFoto ? carregando : '<span>Sem foto</span>') + '</figure>'
      + '<section class="painel"><h2>Descrição</h2><p class="texto-livre" data-descricao>' + esc(p.descricao) + '</p>'
      + (editavel
        ? '<div class="linha-botoes"><button type="button" class="botao secundario pequeno" data-editar>Editar</button>'
          + '<button type="button" class="botao perigo pequeno" data-excluir>Excluir</button></div>'
          + '<form id="form-editar" class="formulario" hidden><div class="form-erro"></div>'
          + '<label>Título<input name="titulo" required maxlength="120" value="' + esc(p.titulo) + '"></label>'
          + '<label>Descrição<textarea name="descricao" required maxlength="1000" rows="4">' + esc(p.descricao) + '</textarea></label>'
          + '<div class="linha-botoes"><button type="submit" class="botao pequeno">Salvar</button>'
          + '<button type="button" class="botao secundario pequeno" data-cancelar>Cancelar</button></div></form>'
        : (p.souAutor ? '<p class="meta">Depois da adoção, o problema não pode mais ser editado nem excluído.</p>' : ''))
      + '</section>'
      + '<section class="painel"><h2>Observações</h2>'
      + '<div class="observacoes" data-lista="obs">' + carregando + '</div>'
      + '<form id="form-obs" class="formulario"><div class="form-erro"></div>'
      + '<label>Adicionar um detalhe<textarea name="texto" required maxlength="1000" rows="2" placeholder="Ex.: hoje um carro caiu no buraco; piora com chuva…"></textarea></label>'
      + '<button type="submit" class="botao pequeno">Enviar observação</button></form>'
      + '</section>'
      + '</div>'
      + '<aside class="coluna-lateral">'
      + '<section class="painel"><h2>Situação</h2>'
      + (p.adotado
        ? '<p>' + seloSituacao(p) + '</p><p class="meta">Adotado em ' + esc(dataHora(p.adotadoEm)) + '</p>'
          + (p.prioridade ? '<p class="meta">Prioridade definida pelo gestor: <b>' + esc(p.prioridade) + '</b></p>' : '')
        : '<p class="suave">Aguardando adoção pelo gestor da comunidade.</p>')
      + '<div class="linha-botoes" data-acoes>' + botaoApoio(p)
      + (p.souGestor && !p.adotado ? '<button type="button" class="botao" data-adotar="' + num(p.id) + '">Adotar problema</button>' : '')
      + '</div></section>'
      + '<section class="painel ia"><h2><span aria-hidden="true">✦</span> Análise da IA</h2>'
      + (p.gravidadeIa
        ? '<dl><dt>Categoria</dt><dd>' + esc((p.categoria && p.categoria.nome) || '—') + '</dd>'
          + '<dt>Gravidade</dt><dd>' + seloGravidade(p.gravidadeIa) + '</dd>'
          + '<dt>Justificativa</dt><dd>' + esc(p.justificativaIa || '—') + '</dd></dl>'
        : p.justificativaIa
          ? '<dl><dt>Gravidade</dt><dd><span class="etiqueta neutra">Não avaliada</span></dd>'
            + '<dt>Motivo</dt><dd>' + esc(p.justificativaIa) + '</dd></dl>'
          : '<p class="suave">A IA ainda não analisou este problema. A análise roda em segundo plano logo depois da publicação e avalia a gravidade pela foto.</p>')
      + '</section>'
      + '<section class="painel"><h2>Local</h2>'
      + (mapa ? '<p class="meta">' + esc(lat) + ', ' + esc(lng) + '</p><a href="' + esc(mapa) + '" target="_blank" rel="noopener">Ver no mapa ↗</a>' : '<p class="suave">Sem localização.</p>')
      + '</section>'
      + '</aside></div>');

    carregarFotos(app());
    ligarAcoesProblema($('[data-acoes]'), rotear);

    if (editavel) {
      const form = $('#form-editar');
      $('[data-editar]').onclick = () => { form.hidden = false; };
      $('[data-cancelar]').onclick = () => { form.hidden = true; };
      aoEnviar(form, async (d) => {
        await Api.editarProblema(id, { titulo: d.titulo.trim(), descricao: d.descricao.trim() });
        aviso('Problema atualizado.');
        rotear();
      });
      $('[data-excluir]').onclick = async () => {
        if (!confirm('Excluir este problema? Isso não pode ser desfeito.')) return;
        try {
          await Api.excluirProblema(id);
          aviso('Problema excluído.');
          location.hash = '#/comunidades/' + num(comunidade.id);
        } catch (e) { aviso(e.message, 'erro'); }
      };
    }

    async function carregarObservacoes() {
      const alvo = $('[data-lista=obs]');
      try {
        const res = await Api.observacoes(id);
        const lista = Array.isArray(res) ? res : Api.pagina(res).itens;
        alvo.innerHTML = lista.length ? lista.map((o) =>
          '<div class="observacao">'
          + '<div class="avatar pequeno" aria-hidden="true">' + esc(iniciais(o.autor && o.autor.nome)) + '</div>'
          + '<div><p class="meta"><b>' + esc((o.autor && o.autor.nome) || '—') + '</b> · ' + esc(dataHora(o.criadaEm)) + '</p>'
          + '<p class="texto-livre">' + esc(o.texto) + '</p></div>'
          + (o.souAutor ? '<button type="button" class="link-perigo" data-excluir-obs="' + num(o.id) + '">remover</button>' : '')
          + '</div>').join('')
          : vazio('Nenhuma observação ainda.');
        $$('[data-excluir-obs]', alvo).forEach((b) => {
          b.onclick = async () => {
            if (!confirm('Remover sua observação?')) return;
            try { await Api.excluirObservacao(b.dataset.excluirObs); carregarObservacoes(); }
            catch (e) { aviso(e.message, 'erro'); }
          };
        });
      } catch (e) {
        alvo.innerHTML = erroHtml(e);
      }
    }

    aoEnviar($('#form-obs'), async (d, f) => {
      await Api.observar(id, d.texto.trim());
      f.reset();
      carregarObservacoes();
    });

    carregarObservacoes();
  }

  async function telaNotificacoes() {
    shell(cabecalho('NOTIFICAÇÕES', 'Seus avisos',
      'Prioridade <b>alta</b>: atualizações do gestor, comunicados e comentários nos seus problemas. Prioridade <b>baixa</b>: problemas novos publicados pelos vizinhos.',
      '<button type="button" class="botao secundario" data-limpar-lidas>Apagar as lidas</button>')
      + '<section class="painel"><div class="lista-avisos" data-lista="avisos">' + carregando + '</div>'
      + '<div class="paginacao" data-paginas="avisos"></div></section>');
    $('[data-limpar-lidas]').onclick = async () => {
      if (!confirm('Apagar todas as notificações já lidas?')) return;
      try { await Api.limparLidas(); aviso('Notificações lidas apagadas.'); carregarAvisos(0); atualizarContador(); }
      catch (e) { aviso(e.message, 'erro'); }
    };
    carregarAvisos(0);
  }

  async function carregarAvisos(numero) {
    const alvo = $('[data-lista=avisos]');
    alvo.innerHTML = carregando;
    try {
      const p = Api.pagina(await Api.notificacoes(numero, 15));
      alvo.innerHTML = p.itens.length ? p.itens.map((n) =>
        '<div class="aviso-item' + (n.lida ? ' lido' : '') + '">'
        + '<span class="etiqueta prio-' + esc(n.prioridade) + '">' + (n.prioridade === 'ALTA' ? 'Alta' : 'Baixa') + '</span>'
        + '<div><p>' + esc(n.mensagem) + '</p><p class="meta">' + esc(dataHora(n.criadaEm))
        + (n.problemaId ? ' · <a href="#/problemas/' + num(n.problemaId) + '">ver problema →</a>' : '') + '</p></div>'
        + '<div class="acoes-aviso">'
        + (n.lida ? '<small class="meta">lida</small>' : '<button type="button" class="botao secundario pequeno" data-lida="' + num(n.id) + '">Marcar como lida</button>')
        + '<button type="button" class="link-perigo" data-apagar-aviso="' + num(n.id) + '">apagar</button></div>'
        + '</div>').join('')
        : vazio('Nenhum aviso por enquanto.');
      paginacao($('[data-paginas=avisos]'), p, carregarAvisos);
      $$('[data-apagar-aviso]', alvo).forEach((b) => {
        b.onclick = async () => {
          b.disabled = true;
          try { await Api.excluirNotificacao(b.dataset.apagarAviso); carregarAvisos(p.numero); atualizarContador(); }
          catch (e) { aviso(e.message, 'erro'); b.disabled = false; }
        };
      });
      $$('[data-lida]', alvo).forEach((b) => {
        b.onclick = async () => {
          b.disabled = true;
          try { await Api.marcarLida(b.dataset.lida); carregarAvisos(p.numero); }
          catch (e) { aviso(e.message, 'erro'); b.disabled = false; }
        };
      });
    } catch (e) {
      alvo.innerHTML = erroHtml(e);
    }
  }

  async function telaTriagem() {
    shell(cabecalho('TRIAGEM DA IA', 'O que adotar primeiro',
      'A IA lê a descrição e a foto de cada problema publicado e estima a gravidade. Aqui estão os problemas ainda não adotados das suas comunidades, do mais grave para o menos grave e, no empate, do mais apoiado.')
      + '<section class="numeros triagem-resumo" data-resumo>' + carregando + '</section>'
      + '<section class="painel"><div class="titulo-secao"><h2>Fila de triagem</h2><small class="meta">GET /triagem</small></div>'
      + '<div class="lista-triagem" data-lista="triagem">' + carregando + '</div>'
      + '<div class="paginacao" data-paginas="triagem"></div></section>');
    carregarTriagem(0);
  }

  async function carregarTriagem(numero) {
    const alvo = $('[data-lista=triagem]');
    alvo.innerHTML = carregando;
    try {
      const t = await Api.triagem(numero, 10);
      const bloco = (classe, titulo, valor, dica) => '<article class="resumo-grav ' + classe + '"><span>' + titulo + '</span>'
        + '<strong>' + num(valor) + '</strong><small>' + dica + '</small></article>';
      $('[data-resumo]').innerHTML =
        bloco('grav-CRITICA', 'Crítica', t.criticos, 'risco imediato')
        + bloco('grav-ALTA', 'Alta', t.altos, 'risco de acidente')
        + bloco('grav-MEDIA', 'Média', t.medios, 'atrapalha o dia a dia')
        + bloco('grav-BAIXA', 'Baixa', t.baixos, 'incômodo')
        + bloco('sem', 'Sem análise', t.semClassificacao, 'sem foto ou aguardando a IA');

      const p = Api.pagina(t.problemas);
      alvo.innerHTML = p.itens.length ? p.itens.map((x) => {
        const id = num(x.id);
        return '<article class="item-triagem">'
          + '<a class="miniatura" href="#/problemas/' + id + '"' + (x.temFoto ? ' data-foto="' + id + '"' : '') + '>' + (x.temFoto ? '' : '<span>sem foto</span>') + '</a>'
          + '<div class="conteudo">'
          + '<div class="etiquetas">' + (seloGravidade(x.gravidadeIa) || '<span class="etiqueta neutra">Sem análise</span>') + seloCategoria(x.categoria)
          + '<span class="etiqueta neutra">' + esc((x.comunidade && x.comunidade.nome) || '') + '</span></div>'
          + '<h3><a href="#/problemas/' + id + '">' + esc(x.titulo) + '</a></h3>'
          + (x.justificativaIa
            ? '<p class="justificativa"><span aria-hidden="true">✦</span> ' + esc(x.justificativaIa) + '</p>'
            : '<p class="meta">A IA ainda não analisou este problema.</p>')
          + '<p class="meta">' + [num(x.totalApoios) + (num(x.totalApoios) === 1 ? ' apoio' : ' apoios'), 'por ' + esc((x.autor && x.autor.nome) || '—'), esc(dataHora(x.criadoEm))].filter(Boolean).join(' · ') + '</p>'
          + '</div>'
          + '<div class="acoes">'
          + '<button type="button" class="botao pequeno" data-adotar="' + id + '">Adotar</button>'
          + '<button type="button" class="botao secundario pequeno" data-reclassificar="' + id + '">✦ Reclassificar com IA</button>'
          + '</div></article>';
      }).join('') : vazio('Nenhum problema esperando adoção nas suas comunidades.');

      paginacao($('[data-paginas=triagem]'), p, carregarTriagem);
      ligarAcoesProblema(alvo, () => carregarTriagem(numero));
      carregarFotos(alvo);
      $$('[data-reclassificar]', alvo).forEach((b) => {
        b.onclick = async () => {
          b.disabled = true;
          b.textContent = 'Analisando…';
          try {
            await Api.reclassificar(b.dataset.reclassificar);
            aviso('Pedido enviado à IA. Atualizando em alguns segundos…');
            setTimeout(() => { if (location.hash === '#/triagem') carregarTriagem(numero); }, 6000);
          } catch (e) {
            aviso(e.message, 'erro');
            b.disabled = false;
            b.textContent = '✦ Reclassificar com IA';
          }
        };
      });
    } catch (e) {
      alvo.innerHTML = erroHtml(e);
    }
  }

  async function telaStatus() {
    const conteudo = cabecalho('STATUS DA API', 'Como a API está agora',
      'Saúde dos componentes (banco, disco, fila), categorias carregadas do Oracle e a documentação Swagger.',
      '<a class="botao" href="/swagger-ui.html" target="_blank" rel="noopener">Abrir Swagger ↗</a>')
      + '<section class="numeros" data-saude>' + carregando + '</section>'
      + '<section class="painel"><div class="titulo-secao"><h2>Categorias de problema</h2><small class="meta">GET /categorias</small></div>'
      + '<div class="chips" data-lista="categorias">' + carregando + '</div></section>';

    if (Api.sessao()) shell(conteudo); else publico(conteudo);

    const saude = await Api.saude();
    const comps = saude.components || {};
    const ROTULOS = { db: 'Banco Oracle', diskSpace: 'Disco', rabbit: 'RabbitMQ', ping: 'Ping', livenessState: 'Liveness', readinessState: 'Readiness', ssl: 'SSL' };
    const bloco = (titulo, st, dica) => '<article class="saude ' + (st === 'UP' ? 'ok' : 'falha') + '"><span>' + esc(titulo) + '</span>'
      + '<strong>' + esc(st || '—') + '</strong><small>' + esc(dica) + '</small></article>';
    $('[data-saude]').innerHTML = bloco('API (geral)', saude.status, 'GET /actuator/health')
      + Object.keys(comps).map((k) => bloco(ROTULOS[k] || k, comps[k] && comps[k].status, k)).join('');

    try {
      const cats = await Api.categorias();
      $('[data-lista=categorias]').innerHTML = cats.length
        ? cats.map((c) => '<span class="chip">' + esc(c.nome) + '</span>').join('')
        : vazio('Nenhuma categoria. Rode o db/ddl.sql.');
    } catch (e) {
      $('[data-lista=categorias]').innerHTML = erroHtml(e);
    }
  }

  const ROTAS = [
    [/^#\/entrar$/, telaEntrar, 'visitante'],
    [/^#\/cadastro\/(morador|gestor)$/, telaCadastro, 'visitante'],
    [/^#\/status$/, telaStatus, 'livre'],
    [/^#\/inicio$/, telaInicio, 'logado'],
    [/^#\/comunidades$/, telaComunidades, 'logado'],
    [/^#\/comunidades\/(\d+)$/, telaComunidade, 'logado'],
    [/^#\/problemas\/(\d+)$/, telaProblema, 'logado'],
    [/^#\/triagem$/, telaTriagem, 'logado'],
    [/^#\/notificacoes$/, telaNotificacoes, 'logado']
  ];

  async function rotear() {
    const hash = location.hash || '#/inicio';
    const logado = !!Api.sessao();
    for (const [regex, tela, acesso] of ROTAS) {
      const m = hash.match(regex);
      if (!m) continue;
      if (acesso === 'logado' && !logado) { location.hash = '#/entrar'; return; }
      if (acesso === 'visitante' && logado) { location.hash = '#/inicio'; return; }
      try {
        await tela(...m.slice(1));
      } catch (e) {
        const corpo = '<a class="voltar" href="#/inicio">← Início</a>' + erroHtml(e);
        if (logado && Api.sessao()) shell(corpo); else publico(corpo);
      }
      window.scrollTo(0, 0);
      return;
    }
    location.hash = logado ? '#/inicio' : '#/entrar';
  }

  window.addEventListener('hashchange', rotear);
  window.addEventListener('rootfix:sessao-expirada', () => {
    aviso('Sua sessão expirou. Entre de novo.', 'erro');
    location.hash = '#/entrar';
  });

  rotear();
})();
