# Release API e pubblicazione docs

Questa guida spiega l'ordine corretto per rilasciare una nuova versione di `mcbot-api`, pubblicarla tramite GitHub Actions e aggiornare le Javadocs su GitHub Pages.

## Come viene calcolata la versione

La versione del progetto viene risolta nel `build.gradle` root in questo ordine:

1. Variabile ambiente `RELEASE_VERSION`, usata dalla CI.
2. Tag Git esatto sul commit corrente, per esempio `v1.0.6`.
3. Ultimo tag disponibile nel repository.
4. Fallback `1.0.0` solo se non ci sono tag.

Per questo motivo le Javadocs devono essere rigenerate quando il commit si trova gia sul tag corretto, altrimenti possono rimanere con la versione precedente.

## Release completa API

Esempio: rilascio `1.0.6`.

### 1. Verifica e build

```powershell
git status --short
.\gradlew.bat :plugin:shadowJar
```

La build deve passare prima di creare il tag.

### 2. Commit e push delle modifiche codice

```powershell
git add .
git commit -m "feat(api): add extended bot controls"
git push origin mcbot
```

Usa un messaggio coerente con il contenuto reale della release.

### 3. Crea e pusha il tag API

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

### 4. Rigenera le Javadocs con il tag gia presente

Dopo aver creato il tag, rigenera forzando il modulo API:

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

### 5. Commit e push nel repo docs

`docs` e un submodule separato, quindi va committato dentro `docs`:

```powershell
git -C docs status --short
git -C docs add mcbot
git -C docs commit -m "docs(api): regenerate javadocs for 1.0.6"
git -C docs push origin master
```

### 6. Commit e push del puntatore docs nel repo principale

Dopo il push del submodule, il repo principale vede `docs` come modificato:

```powershell
git status --short
git add docs
git commit -m "docs(api): point to 1.0.6 javadocs"
git push origin mcbot
```

### 7. Sposta il tag se hai aggiornato il puntatore docs dopo il tag

Se hai creato il tag prima del commit che aggiorna il puntatore `docs`, sposta il tag sul commit finale:

```powershell
git tag -f v1.0.6
git push --force origin v1.0.6
```

Questo mantiene tag release, codice e puntatore docs sullo stesso commit.

## Sequenza rapida consigliata

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

Su GitHub Pages potrebbe servire qualche minuto prima che la nuova versione sia visibile.
