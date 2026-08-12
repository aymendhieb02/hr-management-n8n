# XTENSUS HR Management API

API Spring Boot 4 / Java 17 connectée à MySQL pour l'application XTENSUS HR Management.

## Configuration locale

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/rh_xtensus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Tunis&zeroDateTimeBehavior=CONVERT_TO_NULL"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="votre_mot_de_passe"
$env:JWT_SECRET="une_cle_secrete_robuste_de_32_caracteres_minimum"
```

Le fichier `.env.example` sert de documentation. Spring Boot ne le charge pas automatiquement.

## Démarrage

```powershell
.\mvnw.cmd spring-boot:run
```

L'API écoute par défaut sur `http://localhost:8081`.

## Tests

```powershell
.\mvnw.cmd test
```

## Migrations

Les migrations se trouvent dans `src/main/resources/db/migration`. Hibernate utilise `ddl-auto=validate` et ne modifie pas automatiquement la structure de la base.

Flyway est désactivé par défaut pour permettre l'utilisation d'un schéma MySQL déjà importé. Pour l'activer :

```powershell
$env:FLYWAY_ENABLED="true"
```

En production, fournissez les identifiants MySQL et le secret JWT via un gestionnaire de secrets et activez les cookies sécurisés avec `AUTH_COOKIE_SECURE=true` derrière HTTPS.
