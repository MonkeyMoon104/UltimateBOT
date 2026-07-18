# Release API, publish e GitHub Pages

Questa e l'unica guida da seguire per:

- aggiornare `mcbot-api`
- pubblicare una nuova versione API
- aggiornare le Javadocs su GitHub Pages
- evitare il problema della pagina docs che mostra ancora la versione vecchia

Non mantenere procedure duplicate in altri file: se cambia il flow, aggiorna solo questo file.

## Come viene calcolata la versione

La versione viene risolta dal `build.gradle` root in questo ordine:

1. Variabile ambiente `RELEASE_VERSION`, usata dalla CI.
2. Tag Git esatto sul commit corrente, per esempio `v1.0.6`.
3. Ultimo tag disponibile nel repository.
4. Fallback `1.0.0` solo se non ci sono tag.

Per questo motivo le Javadocs devono essere rigenerate quando il commit si trova gia sul tag corretto, altrimenti possono rimanere con la versione precedente.

## Regola principale

La release API e la pubblicazione docs sono due cose diverse:

- Il tag `vX.Y.Z` pubblica l'API tramite GitHub Actions.
- Il submodule `docs` pubblica le Javadocs su GitHub Pages.

Il tag deve esistere prima di rigenerare le Javadocs, oppure il titolo puo restare alla versione precedente.

## Release completa

Esempio: rilascio `1.0.6`.

### 1. Verifica stato e build

```powershell
git status --short
.\gradlew.bat :plugin:shadowJar
```

La build deve passare prima di creare il tag.

### 2. Commit e push del codice

```powershell
git add .
git commit -m "feat(api): add extended bot controls"
git push origin mcbot
```

Usa un messaggio coerente con il contenuto reale della release.

### 3. Crea e pusha il tag

```powershell
git tag v1.0.6
git push origin v1.0.6
```

Il push del tag `v*` avvia `.github/workflows/release.yml`.

Quel workflow richiama:

```text
MonkeyMoon104/build-infra/.github/workflows/publish-api.yml@main
```

Quindi il publish API online parte da GitHub Actions, non da un comando manuale locale.

### 4. Rigenera le Javadocs dopo il tag

Questo passaggio deve essere fatto dopo il tag. Usa sempre `:mcbot-api:clean` per evitare Javadoc `UP-TO-DATE` con titolo vecchio:

```powershell
.\gradlew.bat :mcbot-api:clean :mcbot-api:javadoc publishApiDocs
```

Controlla il titolo:

```powershell
Select-String -Path docs\mcbot\index.html -Pattern "mcbot-api"
```

Deve mostrare:

```text
mcbot-api 1.0.6 API
```

### 5. Commit e push delle docs

`docs` e un submodule separato, quindi va committato dentro `docs`:

```powershell
git -C docs status --short
git -C docs add mcbot
git -C docs commit -m "docs(api): regenerate javadocs for 1.0.6"
git -C docs push origin master
```

### 6. Commit e push del puntatore `docs`

Dopo il push del submodule, il repo principale vede `docs` come modificato:

```powershell
git status --short
git add docs
git commit -m "docs(api): point to 1.0.6 javadocs"
git push origin mcbot
```

### 7. Allinea il tag al commit finale

Se dopo il tag hai creato il commit che aggiorna il puntatore `docs`, sposta il tag sul commit finale:

```powershell
git tag -f v1.0.6
git push --force origin v1.0.6
```

Questo mantiene tag release, codice e puntatore docs sullo stesso commit.

## Aggiornare solo le docs

Usa questa procedura solo se l'API e gia rilasciata e vuoi correggere/rigenerare GitHub Pages.

```powershell
git describe --tags --exact-match HEAD
.\gradlew.bat :mcbot-api:clean :mcbot-api:javadoc publishApiDocs
Select-String -Path docs\mcbot\index.html -Pattern "mcbot-api"

git -C docs add mcbot
git -C docs commit -m "docs(api): regenerate javadocs for X.Y.Z"
git -C docs push origin master

git add docs
git commit -m "docs(api): point to X.Y.Z javadocs"
git push origin mcbot
```

Se il commit finale deve essere incluso nella release corrente, sposta anche il tag:

```powershell
git tag -f vX.Y.Z
git push --force origin vX.Y.Z
```

## Comandi rapidi

```powershell
.\gradlew.bat :plugin:shadowJar
git add .
git commit -m "feat(api): <descrizione>"
git push origin mcbot
git tag vX.Y.Z
git push origin vX.Y.Z

.\gradlew.bat :mcbot-api:clean :mcbot-api:javadoc publishApiDocs
git -C docs add mcbot
git -C docs commit -m "docs(api): regenerate javadocs for X.Y.Z"
git -C docs push origin master

git add docs
git commit -m "docs(api): point to X.Y.Z javadocs"
git push origin mcbot
git tag -f vX.Y.Z
git push --force origin vX.Y.Z
```

## Controlli finali

```powershell
git status --short
git tag --points-at HEAD
git ls-remote --tags origin vX.Y.Z
Select-String -Path docs\mcbot\index.html -Pattern "mcbot-api"
```

Su GitHub Pages potrebbe servire qualche minuto prima che la nuova versione sia visibile. Se localmente il file mostra la versione corretta ma online no, aspetta e forza refresh del browser.
