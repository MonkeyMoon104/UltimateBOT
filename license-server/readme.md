# Aggiornamento `license-server`

## 1. Ricompila il jar sul tuo PC

Nella root del progetto:

```powershell
.\gradlew :license-server:build
```

Il file giusto resta:

```text
license-server/build/libs/license-server-1.0.0.jar
```

## 2. Carica il nuovo jar sulla VPS

Da Windows PowerShell:

```powershell
scp .\license-server\build\libs\license-server-1.0.0.jar root@92.112.127.240:/root/license-server.jar
```

## 3. Sostituisci il jar sulla VPS

Entra in SSH e fai:

```bash
systemctl stop mcbot-license
cp /opt/mcbot-license/license-server.jar /opt/mcbot-license/license-server.jar.bak
mv /root/license-server.jar /opt/mcbot-license/license-server.jar
chown mcbot-license:mcbot-license /opt/mcbot-license/license-server.jar
```

## 4. Riavvia il servizio

```bash
systemctl start mcbot-license
systemctl status mcbot-license --no-pager
```

## 5. Controlla i log se serve

```bash
journalctl -u mcbot-license -n 100 --no-pager
```

## 6. Test rapido

```bash
curl -X POST https://license.monkeymoon104.it/api/v1/plugin/validate \
  -H "Content-Type: application/json" \
  -d '{
    "licenseKey":"TEST-TEST-TEST-TEST",
    "product":"minecraftbot",
    "pluginVersion":"1.0.0",
    "installationId":"update-test",
    "fingerprintHash":"abc123"
  }'
```
