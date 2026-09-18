# Expense Flow

An expense reimbursement app where AI does the first pass of review. An
employee submits an expense with a receipt photo; a pipeline of AI agents
reads the receipt, checks it against policy, screens it for fraud signals,
and checks it against the department budget, then a final agent decides
**APPROVE**, **REJECT**, or **REVIEW**. Anything sent to `REVIEW` waits for a
human reviewer to make the final call.

## How it works

1. **Submit** — the employee uploads a receipt image/PDF (via a presigned S3
   URL) and enters the amount and description. This kicks off
   `ProcessExpenseFlow`, which persists the `Expense` and emits an
   `AnalysisRequestedEvent`.
2. **AI analysis** — `AnalyzeExpenseFlow` reacts to that event and runs a
   sequence of LangChain4j agentic AI agents (`guru.quarkus.expense.ai`):
   - `ReceiptAgent` reads the receipt image (vision) and extracts merchant,
     date, total, items, etc.
   - `PolicyAgent`, `FraudAgent`, and `BudgetAgent` run in parallel against
     that extracted data (`BudgetAgent` calls a `getCurrentBudget` tool
     backed by the `budgets` table).
   - `DecisionAgent` combines all of the above into a final
     `ExpenseDecision` (`APPROVE` / `REJECT` / `REVIEW`), which is recorded
     on the expense.
3. **Human review** — if the decision is `REVIEW`, the workflow suspends and
   waits for a `ReviewerDecisionEvent`. The Reviewer page lists expenses
   awaiting review; the reviewer approves or rejects (with an optional
   explanation), which publishes that event asynchronously and the workflow
   records the final decision.

Every decision — the AI's and any human reviewer's — is kept as a JSON
history on the `Expense` entity, so you can see the full reasoning trail.

## Stack

- **Quarkus** (Java 21) as the application framework
- **LangChain4j Agentic** (`quarkus-langchain4j-agentic`) for the multi-agent
  AI pipeline, backed by **OpenAI**
- **Serverless Workflow** (`quarkus-flow`, CNCF Serverless Workflow spec) for
  event-driven orchestration of both the submission and analysis flows
- **Hibernate ORM with Panache** + **MySQL** for persistence (decisions are
  stored as native `json` columns)
- **Amazon S3** for receipt storage, via presigned URLs
- A single-page vanilla HTML/CSS/JS frontend (`src/main/resources/META-INF/resources/index.html`)
  with three views: Submit, All Expenses, and Reviewer

## Prerequisites

- Java 21 and Maven (or just use the included `./mvnw`)
- An OpenAI API key
- AWS credentials with access to an S3 bucket (the bucket name and region are
  hardcoded in `application.properties`; adjust `expense.s3.bucket-name` and
  `quarkus.s3.aws.region` if needed)
- A MySQL database — in dev mode Quarkus Dev Services will provision one for
  you automatically if you don't configure one explicitly

Set these environment variables before running:

```shell script
export OPENAI_API_KEY=...
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
```

## Running the application in dev mode

```shell script
./mvnw quarkus:dev
```

The app is served at <http://localhost:8080>, and the Quarkus Dev UI is
available at <http://localhost:8080/q/dev/>.

> **Note:** dev mode uses `drop-and-create` schema management, so the
> database (including seeded budget data from `import.sql`) is reset on
> every restart.

## API

All endpoints are under `/api`:

| Method  | Path                       | Purpose                                           |
|---------|----------------------------|----------------------------------------------------|
| `POST`  | `/api/receipts`            | Get a presigned S3 URL to upload a receipt         |
| `POST`  | `/api/expenses`             | Submit an expense, kicking off the AI analysis     |
| `GET`   | `/api/expenses`             | List all expenses                                  |
| `GET`   | `/api/expenses/{id}/image`  | Get a presigned URL to view an expense's receipt   |
| `PATCH` | `/api/expenses/{id}`        | Record a human reviewer's decision                 |

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it's not an _über-jar_ as the dependencies are copied into the
`target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/expense-flow-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.
