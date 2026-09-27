# Release API, publish e GitHub Pages

Questa e l'unica guida da seguire per:

- aggiornare `common`, `api` e `sdk`
- pubblicare una nuova versione API/SDK
- aggiornare le Javadocs su GitHub Pages
- evitare il problema della pagina docs che mostra ancora la versione vecchia

Non mantenere procedure duplicate in altri file: se cambia il flow, aggiorna solo questo file.

## Dove si cambia la versione

La versione generale del progetto si cambia solo in `gradle.properties`:

```properties
ultimatebot.version=X.Y.Z
```

Gradle usa quella property per tutti i moduli, per il `plugin.yml` generato dentro il jar e per i titoli delle Javadocs API/SDK.

Non mettere versioni hardcoded in `plugin.yml`, nei `build.gradle.kts` dei moduli o in altri file di build.

## Regola principale

La release API e la pubblicazione docs sono due cose diverse:

- `ultimatebot.version=X.Y.Z` decide la versione reale generata da Gradle.
- La pubblicazione su MonkeyRepo (Reposilite) si fa in locale con Gradle: `.\gradlew.bat publish`.
- Il tag `vX.Y.Z` e solo un marker git (opzionale) e deve combaciare con `ultimatebot.version`.
- Il submodule `docs` pubblica le Javadocs su GitHub Pages.

Publish locale (stesso sistema di KTPlus):

```powershell
# in ~/.gradle/gradle.properties
# monkeyrepo.user=...
# monkeyrepo.secret=...

.\gradlew.bat publish
# oppure solo alcuni moduli, es. :api:publish :sdk:publish :addons:metrics:publish
```

Repository Maven pubblico per i consumatori:

```text
https://repo.monkeymoon104.it/release
```

Le Javadocs vanno rigenerate dopo aver cambiato `ultimatebot.version`, cosi il titolo mostra subito la versione corretta.

## Release completa

Esempio: rilascio `1.1.0`.

### 1. Aggiorna la versione

Modifica solo `gradle.properties`:

```properties
ultimatebot.version=1.1.0
```

### 2. Verifica stato e build

```powershell
git status --short
.\gradlew.bat :dist:shadowJar
```

La build deve passare prima di creare tag e docs.

### 3. Commit e push del codice

```powershell
git add .
git commit -m "feat(api): add extended bot controls"
git push origin ultimatebot
```

Usa un messaggio coerente con il contenuto reale della release.

### 4. Rigenera le Javadocs

Usa sempre `:api:clean` e `:sdk:clean` per evitare Javadoc `UP-TO-DATE` con titolo vecchio:

```powershell
.\gradlew.bat :api:clean :sdk:clean publishAllDocs
```

Controlla il titolo:

```powershell
Select-String -Path docs\ultimatebot\index.html -Pattern "api"
Select-String -Path docs\sdk\index.html -Pattern "sdk"
```

Deve mostrare:

```text
api 1.1.0 API
sdk 1.1.0 API
```

### 5. Commit e push delle docs

`docs` e un submodule separato, quindi va committato dentro `docs`:

```powershell
git -C docs status --short
git -C docs add ultimatebot sdk
git -C docs commit -m "docs(api): regenerate javadocs for 1.1.0"
git -C docs push origin master
```

### 6. Commit e push del puntatore `docs`

Dopo il push del submodule, il repo principale vede `docs` come modificato:

```powershell
git status --short
git add docs
git commit -m "docs(api): point to 1.1.0 javadocs"
git push origin ultimatebot
```

### 7. Pubblica su MonkeyRepo

Con le credenziali in `~/.gradle/gradle.properties` (`monkeyrepo.user` / `monkeyrepo.secret`):

```powershell
.\gradlew.bat publish
```

### 8. Tag git (opzionale)

```powershell
git tag v1.1.0
git push origin v1.1.0
```

Il tag non avvia alcun workflow: e solo un riferimento alla release.

## Aggiornare solo le docs

Usa questa procedura solo se vuoi correggere/rigenerare GitHub Pages senza cambiare API.

```powershell
.\gradlew.bat :api:clean :sdk:clean publishAllDocs
Select-String -Path docs\ultimatebot\index.html -Pattern "api"
Select-String -Path docs\sdk\index.html -Pattern "sdk"

git -C docs add ultimatebot sdk
git -C docs commit -m "docs(api): regenerate javadocs for X.Y.Z"
git -C docs push origin master

git add docs
git commit -m "docs(api): point to X.Y.Z javadocs"
git push origin ultimatebot
```

## Comandi rapidi

```powershell
# prima modifica ultimatebot.version in gradle.properties
.\gradlew.bat :dist:shadowJar
git add .
git commit -m "feat(api): <descrizione>"
git push origin ultimatebot

.\gradlew.bat :api:clean :sdk:clean publishAllDocs
git -C docs add ultimatebot sdk
git -C docs commit -m "docs(api): regenerate javadocs for X.Y.Z"
git -C docs push origin master

git add docs
git commit -m "docs(api): point to X.Y.Z javadocs"
git push origin ultimatebot

.\gradlew.bat publish
git tag vX.Y.Z
git push origin vX.Y.Z
```

## Controlli finali

```powershell
git status --short
git tag --points-at HEAD
git ls-remote --tags origin vX.Y.Z
Select-String -Path docs\ultimatebot\index.html -Pattern "api"
Select-String -Path docs\sdk\index.html -Pattern "sdk"
```

Su GitHub Pages potrebbe servire qualche minuto prima che la nuova versione sia visibile. Se localmente il file mostra la versione corretta ma online no, aspetta e forza refresh del browser.
