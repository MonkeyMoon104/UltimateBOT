# Release API, publish e GitHub Pages

Questa e l'unica guida da seguire per:

- aggiornare `mcbot-api` e `mcbot-sdk`
- pubblicare una nuova versione API/SDK
- aggiornare le Javadocs su GitHub Pages
- evitare il problema della pagina docs che mostra ancora la versione vecchia

Non mantenere procedure duplicate in altri file: se cambia il flow, aggiorna solo questo file.

## Dove si cambia la versione

La versione generale del progetto si cambia solo in `gradle.properties`:

```properties
mcbot.version=X.Y.Z
```

Gradle usa quella property per tutti i moduli, per il `plugin.yml` generato dentro il jar e per i titoli delle Javadocs API/SDK.

Non mettere versioni hardcoded in `plugin.yml`, nei `build.gradle.kts` dei moduli o in altri file di build.

## Regola principale

La release API e la pubblicazione docs sono due cose diverse:

- `mcbot.version=X.Y.Z` decide la versione reale generata da Gradle.
- Il tag `vX.Y.Z` deve combaciare con `mcbot.version` e pubblica l'API tramite GitHub Actions.
- Il submodule `docs` pubblica le Javadocs su GitHub Pages.

Le Javadocs vanno rigenerate dopo aver cambiato `mcbot.version`, cosi il titolo mostra subito la versione corretta.

## Release completa

Esempio: rilascio `1.1.0`.

### 1. Aggiorna la versione

Modifica solo `gradle.properties`:

```properties
mcbot.version=1.1.0
```

### 2. Verifica stato e build

```powershell
git status --short
.\gradlew.bat :plugin:shadowJar
```

La build deve passare prima di creare tag e docs.

### 3. Commit e push del codice

```powershell
git add .
git commit -m "feat(api): add extended bot controls"
git push origin mcbot
```

Usa un messaggio coerente con il contenuto reale della release.

### 4. Rigenera le Javadocs

Usa sempre `:mcbot-api:clean` e `:mcbot-sdk:clean` per evitare Javadoc `UP-TO-DATE` con titolo vecchio:

```powershell
.\gradlew.bat :mcbot-api:clean :mcbot-sdk:clean publishAllDocs
```

Controlla il titolo:

```powershell
Select-String -Path docs\mcbot\index.html -Pattern "mcbot-api"
Select-String -Path docs\mcbot-sdk\index.html -Pattern "mcbot-sdk"
```

Deve mostrare:

```text
mcbot-api 1.1.0 API
mcbot-sdk 1.1.0 API
```

### 5. Commit e push delle docs

`docs` e un submodule separato, quindi va committato dentro `docs`:

```powershell
git -C docs status --short
git -C docs add mcbot mcbot-sdk
git -C docs commit -m "docs(api): regenerate javadocs for 1.1.0"
git -C docs push origin master
```

### 6. Commit e push del puntatore `docs`

Dopo il push del submodule, il repo principale vede `docs` come modificato:

```powershell
git status --short
git add docs
git commit -m "docs(api): point to 1.1.0 javadocs"
git push origin mcbot
```

### 7. Crea e pusha il tag finale

Il tag deve essere creato sul commit finale, dopo il puntatore `docs`:

```powershell
git tag v1.1.0
git push origin v1.1.0
```

Il push del tag `v*` avvia `.github/workflows/release.yml`, che pubblica l'API tramite GitHub Actions.

## Aggiornare solo le docs

Usa questa procedura solo se vuoi correggere/rigenerare GitHub Pages senza cambiare API.

```powershell
.\gradlew.bat :mcbot-api:clean :mcbot-sdk:clean publishAllDocs
Select-String -Path docs\mcbot\index.html -Pattern "mcbot-api"
Select-String -Path docs\mcbot-sdk\index.html -Pattern "mcbot-sdk"

git -C docs add mcbot mcbot-sdk
git -C docs commit -m "docs(api): regenerate javadocs for X.Y.Z"
git -C docs push origin master

git add docs
git commit -m "docs(api): point to X.Y.Z javadocs"
git push origin mcbot
```

## Comandi rapidi

```powershell
# prima modifica mcbot.version in gradle.properties
.\gradlew.bat :plugin:shadowJar
git add .
git commit -m "feat(api): <descrizione>"
git push origin mcbot

.\gradlew.bat :mcbot-api:clean :mcbot-sdk:clean publishAllDocs
git -C docs add mcbot mcbot-sdk
git -C docs commit -m "docs(api): regenerate javadocs for X.Y.Z"
git -C docs push origin master

git add docs
git commit -m "docs(api): point to X.Y.Z javadocs"
git push origin mcbot
git tag vX.Y.Z
git push origin vX.Y.Z
```

## Controlli finali

```powershell
git status --short
git tag --points-at HEAD
git ls-remote --tags origin vX.Y.Z
Select-String -Path docs\mcbot\index.html -Pattern "mcbot-api"
Select-String -Path docs\mcbot-sdk\index.html -Pattern "mcbot-sdk"
```

Su GitHub Pages potrebbe servire qualche minuto prima che la nuova versione sia visibile. Se localmente il file mostra la versione corretta ma online no, aspetta e forza refresh del browser.
