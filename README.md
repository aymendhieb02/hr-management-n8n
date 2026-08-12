# XTENSUS HR Management

Application web de gestion des ressources humaines et des congés pour XTENSUS.

Le dépôt principal contient deux applications indépendantes :

- `frontend/hr-management-web` : interface Angular 21 ;
- `backend/hr-management-api` : API REST Spring Boot 4 / Java 17 ;
- MySQL : stockage des employés, demandes, soldes, jours fériés, notifications et certificats médicaux.

## Fonctionnalités principales

- authentification sécurisée par JWT dans un cookie HttpOnly ;
- gestion des employés, postes et types de contrat ;
- demandes de congé et autorisations d'absence ;
- validation par rôle et suivi de l'historique ;
- calcul des jours ouvrés hors week-ends et jours fériés ;
- soldes de congé et transactions mensuelles ;
- certificats médicaux PDF, JPG et PNG ;
- notifications en temps réel avec WebSocket ;
- calendriers personnel et d'équipe ;
- tableaux de bord et rapports en français.

## Prérequis

- Java 17 ;
- Node.js 20 ou version compatible avec Angular 21 ;
- npm 10 ;
- MySQL 8 ;
- Git.

## Base de données MySQL

Créez une base nommée `rh_xtensus`, puis importez le schéma MySQL du projet ou activez Flyway si vous souhaitez appliquer les migrations automatiquement.

Configuration locale par défaut :

```text
Hôte : 127.0.0.1
Port : 3306
Base : rh_xtensus
Utilisateur : root
Mot de passe : vide
```

Les valeurs peuvent être remplacées avec des variables d'environnement :

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/rh_xtensus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Tunis&zeroDateTimeBehavior=CONVERT_TO_NULL"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="votre_mot_de_passe"
$env:JWT_SECRET="une_cle_secrete_robuste_de_32_caracteres_minimum"
$env:FLYWAY_ENABLED="false"
```

## Lancer le backend

```powershell
cd backend/hr-management-api
.\mvnw.cmd spring-boot:run
```

L'API démarre par défaut sur `http://localhost:8081`.

## Lancer le frontend

Dans un second terminal :

```powershell
cd frontend/hr-management-web
npm install
npm start
```

L'interface Angular est ensuite disponible sur `http://localhost:4200`.

## Tests

Backend :

```powershell
cd backend/hr-management-api
.\mvnw.cmd test
```

Frontend :

```powershell
cd frontend/hr-management-web
npm test -- --watch=false
```

Build de production du frontend :

```powershell
cd frontend/hr-management-web
npm run build
```

## Configuration utile

Les principales variables sont définies dans `backend/hr-management-api/src/main/resources/application.yml` :

| Variable | Valeur locale par défaut | Description |
| --- | --- | --- |
| `SERVER_PORT` | `8081` | Port de l'API |
| `DB_URL` | MySQL local `rh_xtensus` | URL JDBC |
| `DB_USERNAME` | `root` | Utilisateur MySQL |
| `DB_PASSWORD` | vide | Mot de passe MySQL |
| `JWT_SECRET` | valeur de développement | Secret JWT, à remplacer en production |
| `AUTH_COOKIE_SECURE` | `false` | À mettre à `true` derrière HTTPS |
| `MEDICAL_CERTIFICATES_PATH` | `./storage/medical-certificates` | Stockage des certificats |
| `FLYWAY_ENABLED` | `false` | Activation des migrations Flyway |

## Branches

- `main` : application complète ;
- `frontend-hr-management-web` : dossier frontend uniquement ;
- `backend-hr-management-api` : dossier backend uniquement.

## Sécurité

Ne versionnez jamais les mots de passe, fichiers `.env`, certificats médicaux ou secrets JWT. En production, utilisez HTTPS, activez `AUTH_COOKIE_SECURE=true` et fournissez les secrets par variables d'environnement ou via un gestionnaire de secrets.
