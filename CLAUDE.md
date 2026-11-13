# CLAUDE.md

Instruções para agentes (Claude Code e similares) que trabalham neste
repositório. Leia antes de compilar, testar ou publicar qualquer coisa.

## Contexto

- Fork de `JamesHuckle/OpenWhisper` (remote `upstream`). O `origin` é
  `brayan-mesquita/OpenWhisper`, **repositório público**.
- O foco deste fork é o **app Android** (`apps/android`, Kotlin).
  O app de Windows (`apps/desktop`) e o worker Python (`apps/worker`) vêm do
  upstream e **não são publicados por este fork**: o atualizador do desktop
  ainda aponta para o repositório original.
- Ambiente de trabalho: **Linux**. Os scripts de `scripts/` são PowerShell para
  Windows (veja "Scripts e hooks" abaixo).
- Idioma: README e conversas em **português do Brasil**. Código, comentários e
  mensagens de commit podem ser em inglês.

## Ambiente local (Linux)

Ferramentas ficam em `.tools/` (ignorado pelo git), sem root:

| Ferramenta | Caminho |
|---|---|
| JDK 17 (Temurin) | `.tools/jdk-17` |
| Android SDK (platform 36, build-tools 36.0.0, platform-tools) | `.tools/android-sdk` |

Variáveis para qualquer comando Gradle, `apksigner` ou `adb`:

```bash
export JAVA_HOME=$PWD/.tools/jdk-17
export ANDROID_HOME=$PWD/.tools/android-sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH
BT=$ANDROID_HOME/build-tools/36.0.0
```

Se `.tools/` não existir, reinstale: JDK 17 Linux x64 pela API da Adoptium,
command-line tools do Android, depois
`sdkmanager --sdk_root=.tools/android-sdk "platform-tools" "platforms;android-36" "build-tools;36.0.0"`.

## Compilar e testar

```bash
cd apps/android
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug   # debug
./gradlew --no-daemon testDebugUnitTest lintRelease assembleRelease # release sem assinatura
```

- Debug: `app/build/outputs/apk/debug/app-debug.apk`, pacote
  `com.openwhisper.android.debug`, começa em **modo demonstração** (texto fixo,
  sem microfone nem API). Pode ficar instalado ao lado do app oficial.
- Release sem assinatura: `app/build/outputs/apk/release/app-release-unsigned.apk`.
- Testes instrumentados (`connectedDebugAndroidTest`) exigem emulador ou
  aparelho conectado.
- O lint falha com `MissingTranslation` se uma string nova não tiver tradução em
  `res/values-pt/strings.xml`. Textos que não se traduzem levam
  `translatable="false"` em `res/values/strings.xml`.
- `./gradlew clean` apaga `app/build/outputs/apk/public/` (os APKs assinados).

## Regras de release (não quebre)

1. **Sempre a mesma chave de assinatura.** Keystore:
   `~/.openwhisper/android/openwhisper-release.jks`, alias `openwhisper`,
   certificado `CN=OpenWhisper, O=OpenWhisper`, SHA-256
   `459995a67ef1eee67305c13a3537c9d47b353bba2e5844a3421bb908766fad71`.
   Outra chave = os usuários não conseguem atualizar (teriam que desinstalar e
   perder a chave da OpenAI salva).
2. **`versionCode` sempre maior** que o da release anterior, e `versionName`
   novo, em `apps/android/app/build.gradle.kts`.
3. **Nunca mude o `applicationId`** (`com.openwhisper.android`).
4. **A senha da keystore nunca passa pelo agente.** Quem assina é o usuário,
   num terminal próprio (o `keytool`/`apksigner` pedem senha interativa).
5. Publicar (push na `main`, release no GitHub) é público e dispara aviso de
   atualização em todos os aparelhos: **só com autorização explícita do
   usuário**.

## Passo a passo de uma release Android

1. Subir a versão em `apps/android/app/build.gradle.kts` (`versionCode` +1,
   `versionName` novo).
2. Compilar e testar: `testDebugUnitTest lintRelease assembleRelease`.
3. Commit e `git push origin main` (com autorização).
4. Pedir ao usuário para assinar, num terminal próprio:
   `~/.openwhisper/android/sign-release.sh`.
   O script cria a keystore só se ela não existir, faz `zipalign`, assina e
   gera em `apps/android/app/build/outputs/apk/public/`:
   `OpenWhisper-Android-vX.Y.Z.apk`, `OpenWhisper-Android.apk` (cópia com nome
   fixo) e `OpenWhisper-Android-vX.Y.Z.apk.sha256`.
5. Verificar a assinatura **antes** de publicar:
   ```bash
   cd apps/android/app/build/outputs/apk/public
   $BT/apksigner verify --print-certs OpenWhisper-Android-vX.Y.Z.apk   # SHA-256 da regra 1
   $BT/aapt2 dump badging OpenWhisper-Android-vX.Y.Z.apk | head -1     # versionCode/versionName
   sha256sum -c OpenWhisper-Android-vX.Y.Z.apk.sha256
   ```
6. Criar `OpenWhisper-Android-update.json` (é o que o app lê para saber se há
   atualização):
   ```json
   {
     "versionCode": 31,
     "versionName": "0.3.1",
     "apkUrl": "https://github.com/brayan-mesquita/OpenWhisper/releases/download/v0.3.1/OpenWhisper-Android-v0.3.1.apk",
     "sha256": "<sha256 do APK versionado, minúsculo>",
     "releaseNotes": "Resumo curto em português."
   }
   ```
   `apkUrl` precisa ser `https://`. O app recusa a atualização se o hash, o
   pacote, a versão ou a assinatura não baterem.
7. Publicar com o `gh` (o `--target` exige o **SHA completo**, não o curto):
   ```bash
   gh release create vX.Y.Z --repo brayan-mesquita/OpenWhisper \
     --target "$(git rev-parse HEAD)" --title "OpenWhisper Android vX.Y.Z" \
     --notes-file <notas.md> --latest \
     OpenWhisper-Android-vX.Y.Z.apk OpenWhisper-Android.apk \
     OpenWhisper-Android-vX.Y.Z.apk.sha256 OpenWhisper-Android-update.json
   ```
   Não envie o `.idsig`. Remova da pasta os arquivos de versões antigas antes
   de publicar, para não subir o arquivo errado.
8. Conferir pelos links públicos "latest" (é o que o app e o README usam):
   ```bash
   B=https://github.com/brayan-mesquita/OpenWhisper/releases/latest/download
   curl -fsSL $B/OpenWhisper-Android-update.json      # versão nova?
   curl -fsSL $B/OpenWhisper-Android.apk | sha256sum  # mesmo hash do update.json?
   ```
9. Atualizar a seção "Novidades deste fork" do `README.md`.

## Scripts e hooks

Tudo em `scripts/` é PowerShell para **Windows** e não roda neste Linux sem
adaptação. Resumo:

| Script | O que faz |
|---|---|
| `bootstrap-android.ps1` | Instala JDK/SDK/emulador em `.tools` (Windows) |
| `android.ps1`, `test-android.ps1` | Chamam o Gradle / rodam testes |
| `setup-android-signing.ps1` | Cria a keystore e grava senhas no `.env` |
| `build-android-release.ps1` | Build + zipalign + assinatura (lê senhas do `.env`) |
| `release-android.ps1` | Publica a release Android e copia assets do Windows da release anterior |
| `pre-push-release.ps1` | Release coordenada web + Windows + Android |
| `bump-release-version.ps1` | Sobe a versão do desktop e do Android juntos |

**Hooks do git (`scripts/hooks/`)** ficam **desligados**. Se alguém rodar
`git config core.hooksPath scripts/hooks`:
- `pre-commit` sobe a versão a cada commit;
- `pre-push` na `main` compila tudo, cria a tag com `--force`, publica release
  "latest" no GitHub e atualiza o app Windows local.

Não ative os hooks neste ambiente e não rode `pre-push-release.ps1`: eles
quebram no Linux e, no Windows, publicam uma release a cada push. A release
deste fork é feita pelo passo a passo acima.

## Teste no aparelho (adb)

- O celular de testes é um Samsung (Teclado Samsung). Confirme com
  `adb devices -l` e veja a versão instalada com
  `adb shell dumpsys package com.openwhisper.android | grep versionName`.
- Para testar uma correção sem publicar: instale o **debug** (modo
  demonstração), peça ao usuário para ativar o serviço de acessibilidade do
  debug e desativar o oficial (permissão sensível: quem ativa é o usuário).
- Localizar o microfone (janela de acessibilidade):
  `adb shell dumpsys window windows`, procurar `ACCESSIBILITY_OVERLAY` e o
  `mFrame`. Tocar com `adb shell input tap X Y`.
- Para inspecionar um campo, use `uiautomator dump` e filtre **só o campo de
  texto** (ex.: `resource-id=com.whatsapp:id/entry`). Não leia nem exiba as
  mensagens das conversas, não envie nada e limpe o campo depois do teste.
- Ao terminar, desinstale o debug (`adb uninstall com.openwhisper.android.debug`)
  se o usuário pedir, e lembre-o de reativar o serviço do app oficial.

## Segredos (repositório público)

- Nunca commite `.env`, `*.jks`, `*.keystore`, `*.key` (já estão no
  `.gitignore`), nem chaves da OpenAI, senhas ou tokens.
- A keystore fica fora do repositório (`~/.openwhisper/android/`).
- Antes de um push grande, verifique o histórico:
  ```bash
  git log --all -p --no-color | grep -nE "sk-(proj-)?[A-Za-z0-9_-]{20,}|-----BEGIN [A-Z ]*PRIVATE KEY|ghp_[A-Za-z0-9]{30,}|AKIA[0-9A-Z]{16}"
  ```
  Ocorrências conhecidas e inofensivas: o exemplo `...` do `.env.example` e
  `sk-instrumentation-secret` num teste.

## Armadilhas conhecidas

- **Texto de exemplo em campos vazios:** apps como o WhatsApp informam
  `"Mensagem"` como conteúdo do campo pela acessibilidade, sem marcar como hint.
  Por isso a inserção usa primeiro o canal do teclado (`InputConnection`,
  Android 13+) e só depois a acessibilidade (`RoutingEditorSessionPort`).
  Não inverta essa ordem.
- **Atualizador:** os dados de atualização nunca vêm de extras de `Intent`
  (a `MainActivity` é exportada). O app sempre baixa o feed fixo e confere
  pacote, versão e assinatura do APK (`UpdatePackageVerifier`).
- **Prompt da OpenAI:** precisa estar no idioma do áudio. Para português o
  dicionário vai como `Vocabulário: ...`; no `gpt-transcribe` ele vai em
  `keywords[]`. O `whisper-1` aceita prompt curto (limite de 224 tokens).
