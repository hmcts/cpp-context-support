# CLAUDE.md — cpp-context-support

Context-specific notes for the **Java 25 / WildFly 40 / Jakarta EE 11** upgrade (25.104.x). See the
workspace-root `CLAUDE.md` for platform-wide guidance. This is a deliberately **simple** context — the
guinea-pig for validating the Java-25 upgrade + environments before the gnarlier contexts.

## Branches / versions
- Java-25 integration branch: **`team/25.104.x`** (created from `main`; auto-releases on merge). Work on a
  `dev/support-…` branch and PR into it.
- Parent → `service-parent-pom:25.104.0-M9`; project version `25.104.0-M1-SNAPSHOT`.

## Jakarta / Java 25 mechanics
- `javax.*` → `jakarta.*` across all modules (json, inject, ws.rs, persistence, enterprise).
- `javax:javaee-api` → `jakarta.platform:jakarta.jakartaee-api`; `javax.json:javax.json-api` →
  `jakarta.json:jakarta.json-api`; `javax.xml.bind:jaxb-api` → `jakarta.xml.bind:jakarta.xml.bind-api`.
- The RAML `messaging-adapter-generator-plugin` / `messaging-client-generator-plugin` blocks need the
  `jakarta.xml.bind:jakarta.xml.bind-api` (`${jakarta.xml.bind-api.raml.version}`) override in their
  `<dependencies>` (in `support-event/pom.xml` and `support-command/support-command-api/pom.xml`).
- `persistence.xml` → Jakarta 3.0 namespace (`https://jakarta.ee/xml/ns/persistence`, `version="3.0"`).

## ⚠️ DeltaSpike Data → plain JPA (production code)
`FeedbackRepository` was a DeltaSpike Data `@Repository` interface (`EntityRepository<Feedback, UUID>` with
derived queries). DeltaSpike is EOL / unavailable under Jakarta EE 11 / CDI 4, and the 25.104.x parent no
longer manages the DeltaSpike / OpenEJB / H2 versions — so the old code fails at POM-parse
(`'dependencies.dependency.version' … is missing`).

Rewritten to the standard Java-25 pattern (mirrors `cpp-context-listing` / `cpp-context-applications-courtorders`):
- **Repository** → concrete `@ApplicationScoped` class with `@PersistenceContext(unitName = "support") EntityManager`.
  Each derived query becomes explicit JPQL (`findBycaseId`, `findByDateReceivedBetween`,
  `findByCaseIdAndDateReceivedBetween`); `save()` → `entityManager.merge()` (idempotent under event-listener
  replay); `findBy(id)` → `entityManager.find(...)` (kept returning `Feedback`, not `Optional`, to match callers).
- Callers (`SupportEventListener`, `SupportQueryView`/`FeedbackService`) inject `FeedbackRepository` **by type**,
  so the interface→class change is transparent.
- **`viewstore-persistence/pom.xml`** — removed `persistence-deltaspike`, all `deltaspike-*`, `openejb-*`,
  `junit-vintage-engine`, `test-utils-persistence`, `test-utils-logging-log4j`; removed the root-pom `h2`
  downgrade. Test deps are now `test-utils-hibernate` + `com.h2database:h2` + `junit-jupiter-api` + `hamcrest`.
- **Repository test** — `FeedbackRepositoryTest` is a **real-JPA test against in-memory H2** at build time
  (Surefire), via `HibernateTestEntityManagerProvider` + a test `persistence.xml`
  (`support-test-persistence-unit`, H2, `hbm2ddl create`, rolled back per test). It exercises the actual JPQL
  (not mocked) — same pattern as businessprocesses/defence/etc. `@RegisterExtension` provider +
  `injectEntityManagerInto(repository)`. The real-Postgres path is additionally covered end-to-end by
  `SendFeedbackIT`/`SearchByCaseIdIT`.

## Pipeline
`azure-pipelines.yaml` is on the **wildfly40** track: `ref: 'wildfly40'`, agent `ubuntu-j25` (NOT
`-postgres`), `aksDeployBranch: 'wildfly40'`.

## Build / test
- Full build: `mm` (the whole 25.104.x chain is released, so no enforcer skips needed).
- ITs: `./runIntegrationTests.sh` (no Elasticsearch/Camunda — just WildFly 40 + Postgres + Artemis).
  Validated: build + unit + coverage green; ITs `SendFeedbackIT` 2/2, `SearchByCaseIdIT` 1/1; healthchecks 3/3.
