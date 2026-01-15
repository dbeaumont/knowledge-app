# Importer la CA locale (HTTPS Keycloak)

Ce guide explique comment importer la CA locale générée par `make ca-root` (fichier `.certs/ca.crt`) pour éviter les erreurs TLS avec `https://keycloak.local:8443`.

## macOS (Keychain)
1) Ouvrir **Keychain Access** (Trousseaux d'accès).
2) Sélectionner le trousseau **System**.
3) Importer `.certs/ca.crt`.
4) Double‑cliquer sur le certificat, section **Trust**, mettre **Always Trust**.

CLI (équivalent) :
```bash
sudo security add-trusted-cert -d -r trustRoot \
  -k /Library/Keychains/System.keychain .certs/ca.crt
```

## Windows (certlm)
PowerShell (admin) :
```powershell
certutil -addstore -f "Root" ".certs\ca.crt"
```

Alternative GUI :
1) `Win + R` → `certlm.msc`
2) **Trusted Root Certification Authorities** → **Certificates**
3) **Import…** et choisir `.certs\ca.crt`

## Linux
### Debian / Ubuntu
```bash
sudo cp .certs/ca.crt /usr/local/share/ca-certificates/ai-knowledge-root-ca.crt
sudo update-ca-certificates
```

### Fedora / RHEL
```bash
sudo cp .certs/ca.crt /etc/pki/ca-trust/source/anchors/ai-knowledge-root-ca.crt
sudo update-ca-trust
```

### Arch
```bash
sudo trust anchor --store .certs/ca.crt
```

## Firefox (toutes plateformes)
Firefox utilise son propre store. Deux options :
1) **Utiliser le store OS** : `about:config` → `security.enterprise_roots.enabled = true`
2) **Importer manuellement** : `about:preferences#privacy` → Certificats → **Voir les certificats** → **Import**

## Vérification rapide
```bash
curl -v https://keycloak.local:8443/realms/knowledge/.well-known/openid-configuration
```
