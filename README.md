# OpenWhisper

Ditado por voz com a inteligência artificial da OpenAI, direto no teclado que
você já usa. Você fala, o OpenWhisper transcreve e o texto aparece no campo em
que você estava digitando: WhatsApp, e-mail, navegador, notas, qualquer app.

Este repositório é um fork do [OpenWhisper original](https://github.com/JamesHuckle/OpenWhisper)
com foco no **app para Android**, adaptado para quem fala **português do Brasil**.

**[⬇️ Baixar o OpenWhisper para Android](https://github.com/brayan-mesquita/OpenWhisper/releases/latest/download/OpenWhisper-Android.apk)**

---

## Como funciona no celular

1. Você toca num campo de texto e o teclado abre normalmente.
2. Um pequeno botão de microfone do OpenWhisper aparece ao lado do teclado.
3. Toque uma vez para começar a falar e outra vez para terminar.
4. Em poucos segundos o texto transcrito é inserido onde estava o cursor.
   Uma cópia também vai para a área de transferência, por segurança.

O microfone some sozinho quando o teclado fecha, quando o campo não aceita
texto e em **campos de senha ou marcados como sensíveis**.

## Compatível com o Teclado Samsung

O OpenWhisper **não substitui nem modifica o seu teclado**. Você continua usando
o Teclado Samsung (ou o Gboard, ou outro), com seus atalhos, emojis e correção
automática. O OpenWhisper só acrescenta o botão de microfone por cima, ao lado
da tecla **A**.

Isso é possível porque o app usa o recurso de **acessibilidade** do Android,
que você ativa uma única vez. Não existe outra forma oficial de um app colocar
um botão dentro do teclado de outra empresa.

Testado num **Samsung Galaxy S23 Ultra** (Android 17). Em outros modelos Samsung
deve funcionar igual, mas vale observar o posicionamento do botão ao girar a
tela ou usar janelas divididas.

## Instalação

1. **Baixe o APK** pelo link acima, direto no celular.
2. **Abra o arquivo** pelo app *Meus Arquivos*. Se o Android pedir, permita
   instalar apps desta fonte.
   - Nos Samsung, o **Bloqueador automático** pode impedir a instalação.
     Desative em *Configurações > Segurança e privacidade > Bloqueador
     automático*, instale e depois ative de novo.
3. **Abra o OpenWhisper** e conceda a permissão do **microfone** e das
   **notificações**.
4. Toque em **Ativar o microfone de acessibilidade**, escolha
   **OpenWhisper dictation** e ative. O Android mostra um aviso sobre o acesso
   que o app terá; leia e confirme se estiver de acordo.
5. Cole a sua **chave da API da OpenAI** e toque em **Salvar com segurança**.
   Você cria a chave em [platform.openai.com](https://platform.openai.com/api-keys);
   o uso é cobrado pela OpenAI na sua conta.
6. Desligue a **Transcrição de demonstração** para usar o microfone de verdade.

### Atualizações automáticas

Depois da primeira instalação, você não precisa baixar mais nada. O app verifica
novas versões ao ser aberto e uma vez por dia, mostra uma notificação e, com um
toque, baixa e instala a atualização. O Android sempre pede a sua confirmação.
Suas configurações e a chave da OpenAI são mantidas.

## Configurações

### Idioma falado

O padrão é **Português (Brasil)**. Fixar o idioma reduz erros de transcrição.
Termos técnicos em inglês continuam sendo reconhecidos. Também há as opções
Automático, Inglês e Espanhol.

### Modelo da OpenAI

Você escolhe entre mais precisão ou menor custo:

| Modelo | Indicado para | Custo aproximado |
|---|---|---|
| **GPT Transcribe** (padrão) | Melhor precisão e melhor uso do dicionário | US$ 0,0045 por minuto |
| **GPT-4o mini Transcribe** | Economizar | ~US$ 0,003 por minuto |
| GPT-4o Transcribe | Modelo antigo, descontinuado pela OpenAI | ~US$ 0,006 por minuto |
| Whisper | Modelo antigo, descontinuado pela OpenAI | US$ 0,006 por minuto |

Os preços são referências da OpenAI e podem mudar; o valor oficial está no site
da OpenAI.

### Dicionário pessoal

Coloque, um por linha, as palavras que a transcrição costuma errar: nomes de
pessoas e empresas, siglas e termos técnicos, inclusive em inglês. Por exemplo:

```
Git
GitHub
deploy
MCP
```

Esses termos são enviados junto com cada transcrição para orientar o
reconhecimento. Cabem até 100 termos.

### Arquivos de áudio

Também dá para transcrever um arquivo de áudio salvo no celular. O app guarda
as 10 gravações mais recentes, para que nada se perca se a internet cair no
meio da transcrição.

## Privacidade e segurança

- **Sua chave da OpenAI fica criptografada** no celular, protegida pelo
  armazenamento seguro do Android, e não entra em backups.
- **O áudio vai direto do seu celular para a OpenAI.** Não há servidor
  intermediário.
- **O texto transcrito não é registrado** nos logs do sistema.
- **Campos de senha e sensíveis são ignorados**: o microfone não aparece neles.
- **Atualizações são verificadas antes de instalar**: o app só aceita um APK do
  mesmo app, de versão mais nova e assinado com a mesma chave.

O que o app **não** protege: um celular com root ou comprometido está fora da
proteção do Android.

## Novidades deste fork

### 0.3.1
- Corrigido: em campos vazios, o texto de exemplo (como **"Mensagem"** no
  WhatsApp) aparecia antes da transcrição. Agora o texto é inserido pelo mesmo
  canal que o teclado usa, que informa o conteúdo real do campo.

### 0.3.0
- Escolha do **modelo da OpenAI** nas configurações.
- **Dicionário pessoal** para termos que a transcrição costuma errar.
- **Idioma falado**, com português do Brasil como padrão.
- App **traduzido para português**.
- Segurança do atualizador reforçada: outros apps não conseguem mais forjar uma
  atualização, e o APK baixado é conferido antes da instalação.

## Problemas comuns

| Problema | O que fazer |
|---|---|
| O microfone não aparece | Confira se *OpenWhisper dictation* está ativo em *Configurações > Acessibilidade > Apps instalados*. Alguns Samsung desativam o serviço depois de uma atualização do sistema. |
| O microfone some depois de um tempo | Em *Configurações > Aplicativos > OpenWhisper > Bateria*, escolha **Sem restrições**. |
| Aparece uma frase em inglês fixa | A **Transcrição de demonstração** está ligada. Desligue nas configurações. |
| Erro de transcrição | Verifique a internet e se a chave da OpenAI está salva e tem crédito. A transcrição fica na área de transferência quando a inserção falha. |
| Não instala a atualização | Permita que o OpenWhisper instale apps em *Configurações > Aplicativos > OpenWhisper > Instalar apps desconhecidos*. |

## Versão para computador (Windows)

O projeto original também tem um app de ditado para Windows. Ele continua
recebendo atualizações pelo [repositório original](https://github.com/JamesHuckle/OpenWhisper/releases);
este fork não publica a versão para Windows.

---

## Para desenvolvedores

O app Android fica em [`apps/android`](apps/android) (Kotlin, Android 9 ou
mais novo). Para compilar no Linux é preciso o JDK 17 e o Android SDK
(plataforma 36):

```bash
cd apps/android
./gradlew testDebugUnitTest assembleDebug
```

Detalhes de arquitetura, testes e publicação estão em
[`docs/android.md`](docs/android.md), e o checklist de testes de aceitação em
[`docs/android-acceptance.md`](docs/android-acceptance.md).

Outras partes do repositório, herdadas do projeto original:

- `apps/desktop`: app para Windows (Tauri)
- `apps/worker`: serviço em Python usado pelo app para Windows
- `apps/web`: site do projeto
- `docs/architecture.md`: arquitetura geral

> **Atenção:** os scripts de release em `scripts/` e os hooks do git em
> `scripts/hooks/` foram feitos para Windows e publicam uma release a cada push
> na `main`. Eles ficam desligados até alguém rodar
> `git config core.hooksPath scripts/hooks`.
