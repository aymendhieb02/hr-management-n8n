# XTENSUS HR Management

Application web de gestion des ressources humaines et des congés. Le projet est composé d'une API Spring Boot/MySQL et d'une interface Angular responsive en français.

## Fonctionnalités

- Authentification JWT avec cookies HttpOnly, changement de mot de passe temporaire et mot de passe oublié par code email.
- Gestion des employés, photos, postes, types de contrat et rôles (`ADMIN`, `DG`, `DT`, `RH`, `EMPLOYE`).
- Un seul Directeur général (`DG`) et une seule Directrice technique (`DT`).
- Demandes de congé et autorisations d'absence avec brouillon, soumission, validation séquentielle et annulation.
- Congé maladie avec certificat médical (PDF/JPG/PNG).
- Soldes de congé, acquisition mensuelle configurable et historique des transactions.
- Régularisation de la consommation réelle par DG/DT.
- Jours fériés, calendriers personnel/équipe, disponibilité et rapports.
- Notifications internes, WebSocket et emails persistants via `email_outbox`.
- Interface d'administration des circuits de validation.

## Structure

```text
backend/hr-management-api/       API Spring Boot 3 / Java
frontend/hr-management-web/     Angular 21
database/mysql/                  Scripts SQL et sauvegardes
architecture/                    Documents techniques (non requis à l'exécution)
```

Les branches GitHub sont séparées : `frontend-hr-management-web`, `backend-hr-management-api` et `main`.

## Prérequis

- Java 17+, Maven Wrapper
- Node.js 20+ et npm
- MySQL ou MariaDB 10.6+
- Une base `rh_xtensus`

## Installation backend

```bash
cd backend/hr-management-api
copy .env.example .env
./mvnw spring-boot:run
```

Variables principales : `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `MEDICAL_CERTIFICATES_PATH`, `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`.

Le port API par défaut est `8081`. Flyway est désactivé par défaut dans certains environnements (`FLYWAY_ENABLED=false`) : exécuter les scripts de `src/main/resources/db/migration` dans l'ordre si nécessaire.

## Installation frontend

```bash
cd frontend/hr-management-web
npm install
npm start
```

L'interface est servie sur `http://localhost:4200` et utilise l'URL API définie dans `src/environments`.

## Modèle métier

Une demande est créée en `BROUILLON`, visible uniquement par son propriétaire. Lors de la confirmation, les contrôles de délai, jours ouvrables, jours fériés, chevauchement, solde et circuit actif sont appliqués. La demande passe alors à `EN_ATTENTE`.

Un circuit contient des étapes ordonnées. Seul le décideur de l'étape active peut approuver ou refuser. Une approbation intermédiaire active l'étape suivante; la dernière approbation débite le solde et informe l'employé. Un refus clôt immédiatement le circuit.

Une demi-journée vaut `0.5` jour et utilise un horaire matin `08:30–13:00` ou après-midi `14:00–18:00`.

## Rôles et accès

- `ADMIN` : configuration, employés, contrats, postes, circuits et supervision complète.
- `DG` / `DT` : pilotage d'équipe, décisions de workflow, employés de leur périmètre et régularisation.
- `RH` : gestion RH selon les gardes backend.
- `EMPLOYE` : profil, mot de passe, demandes personnelles, certificats, solde et notifications personnelles.

Les contrôles d'accès doivent toujours être vérifiés côté serveur; les gardes Angular ne sont qu'une aide d'interface.

## Configuration système

Les variables de la table `variables` pilotent notamment `solde_conge_par_mois`, `reinitialisation_solde`, `solde_negatif`, `solde_negatif_max` et les paramètres SMTP. L'action « Tester » de l'acquisition applique un test temporaire identifié `TEST_ACQUISITION:`; « Retourner » annule uniquement ce test et supprime ses lignes d'historique.

## Tests et qualité

```bash
cd backend/hr-management-api
./mvnw test
./mvnw -DskipTests package

cd frontend/hr-management-web
npm test
npx ng build hr-management-web --configuration production
```

Les avertissements de budget CSS Angular ne bloquent pas la compilation.

## Données et migrations

Ne jamais supprimer une table en production. Utiliser les migrations Flyway non destructives et sauvegarder la base avant toute correction SQL. Les dates doivent être de vraies dates MySQL (jamais `0000-00-00`). Les demandes existantes conservent leur workflow copié; modifier un circuit ne change que les demandes futures.

## Livraison Git

Les modifications frontend doivent être poussées uniquement vers `frontend-hr-management-web`, les modifications backend/migrations vers `backend-hr-management-api`, puis les versions validées peuvent être fusionnées dans `main`. Exclure `node_modules`, `target`, `dist`, `storage`, `mysql-data`, journaux locaux et fichiers d'environnement.

## Dépannage rapide

- Erreur SMTP : vérifier les variables SMTP; la décision métier reste enregistrée dans `email_outbox` pour nouvelle tentative.
- Erreur `Zero date value prohibited` : rechercher et corriger les dates `0000-00-00`.
- Demande absente d'un décideur : vérifier `conge_demande_workflows`, l'étape `EN_ATTENTE`, `etape_courante` et l'identité du décideur.
- Certificat absent : la demande doit être un congé maladie, et l'employé propriétaire est le seul à pouvoir téléverser.

## Contact technique

Avant toute modification importante, vérifier les migrations, les DTO backend, les services métier et les tests Angular correspondants. Conserver les libellés français et les règles d'autorisation existantes.
