# Aggiornare solo le Javadocs pubbliche

Usa questa procedura quando il codice API e il tag sono gia corretti e devi aggiornare solo GitHub Pages.

## Ordine corretto

1. Verifica che `HEAD` abbia la versione/tag giusto:

```powershell
git describe --tags --exact-match HEAD
```

Esempio atteso:

```text
v1.0.6
```

2. Rigenera le Javadocs forzando il rebuild del modulo API:

```powershell
.\gradlew.bat :mcbot-api:clean :mcbot-api:javadoc publishApiDocs
```

Non usare solo `publishApiDocs` se hai appena creato/spostato un tag: Gradle puo considerare la Javadoc `UP-TO-DATE` e lasciare il titolo vecchio.

3. Controlla che il titolo generato sia corretto:

```powershell
Select-String -Path docs\mcbot\index.html -Pattern "mcbot-api"
```

Deve mostrare la versione nuova, per esempio:

```text
mcbot-api 1.0.6 API
```

4. Entra nel submodule docs, committa e pusha:

```powershell
git -C docs status --short
git -C docs add mcbot
git -C docs commit -m "docs(api): regenerate javadocs for 1.0.6"
git -C docs push origin master
```

5. Torna al repo principale, committa il puntatore aggiornato del submodule e pusha:

```powershell
git status --short
git add docs
git commit -m "docs(api): point to 1.0.6 javadocs"
git push origin mcbot
```

## Nota cache GitHub Pages

Dopo il push GitHub Pages puo richiedere qualche minuto. Se il browser mostra ancora la versione vecchia, usa refresh forzato o una finestra privata.
