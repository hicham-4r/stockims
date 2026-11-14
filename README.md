# StockIMS

A stock management web application built with Java and Spring Boot for the IMS program at ESITH. StockIMS brings article records, stock entries, and withdrawals together in a responsive, French-language interface.

## Features

- **Article catalogue:** create articles with a unique reference and a descriptive name.
- **Stock tracking:** calculate available quantities from recorded entries and withdrawals.
- **Stock movements:** register incoming and outgoing quantities, with the current date recorded automatically.
- **Validation:** reject missing fields, duplicate references, non-positive quantities, and withdrawals that exceed available stock.
- **Article deletion:** review an article and its current stock, then confirm permanent deletion.
- **Responsive interface:** shared navigation, styled forms, empty states, and feedback messages built with HTML and CSS.

Deleting an article also permanently deletes all its associated stock entries and withdrawals. To remove only a quantity, record a withdrawal instead.

## Technology

| Component | Technology |
| --- | --- |
| Language | Java 21 |
| Application framework | Spring Boot 3.2.4 |
| Web layer | Spring MVC |
| Templates | Thymeleaf |
| Interface | HTML, CSS, inline SVG icons |
| Persistence | Spring Data JPA and Hibernate |
| Application database | MySQL |
| Test database | H2, in MySQL compatibility mode |
| Validation | Jakarta Bean Validation |
| Boilerplate reduction | Lombok |
| Build | Maven Wrapper |
| Tests | JUnit 5, Spring Boot Test, MockMvc, AssertJ |

The interface requires no JavaScript framework, Node.js, external fonts, or CDN assets.

## Prerequisites

- A full **JDK 21** installation.
- A running MySQL server for normal application use.
- Git.
- IntelliJ IDEA or another Java IDE, if desired.

The repository includes Maven Wrapper scripts, so a separate Maven installation is not required. The first build needs internet access to download Maven and project dependencies.

## Getting started

The terminal examples below use **Windows PowerShell**.

### 1. Clone the repository

```powershell
git clone https://github.com/hicham-4r/stockims.git
```

```powershell
cd stockims
```

Check the Java version used by Maven:

```powershell
.\mvnw.cmd --version
```

The output should identify Java 21.

### 2. Create the database

Connect through MySQL Workbench, or open a MySQL terminal:

```powershell
mysql -u root -p
```

Run this SQL statement:

```sql
CREATE DATABASE IF NOT EXISTS stockims_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

The application uses Hibernate's `update` setting to create or update its tables when it starts.

### 3. Configure database access

The application configuration is in [application.properties](src/main/resources/application.properties).

| Setting | Default |
| --- | --- |
| Database URL | `jdbc:mysql://localhost:3306/stockims_db` |
| Database username | `root` |
| Database password | Read from the `DB_PASSWORD` environment variable |
| Schema handling | `spring.jpa.hibernate.ddl-auto=update` |
| HTTP port | `8080` |

To enter your credentials without writing the password in a command, use:

```powershell
$dbCredential = Get-Credential -UserName "root" -Message "Enter your local MySQL credentials"
```

```powershell
$env:SPRING_DATASOURCE_USERNAME = $dbCredential.UserName
```

```powershell
$env:DB_PASSWORD = $dbCredential.GetNetworkCredential().Password
```

These variables apply to the current terminal session and applications launched from it. Repeat the configuration when opening a new terminal.

For a different database host, port, or name, you can also override the URL:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/stockims_db"
```

Keep real database passwords out of source files and commits.

### 4. Start the application

Run from the project root, in the terminal where you configured the credentials:

```powershell
.\mvnw.cmd spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

Stop the application with **Ctrl+C**.

On macOS or Linux, use `sh mvnw` instead of `.\mvnw.cmd`, and set the database environment variables using your shell.

## Running in IntelliJ IDEA

1. Open the cloned project folder, or import `pom.xml` as a Maven project.
2. Set the **Project SDK** and the **Maven runner JRE** to JDK 21.
3. Allow IntelliJ to import the Maven dependencies.
4. Open `src/main/java/ma/esith/stockims/StockimsApplication.java`.
5. In **Run > Edit Configurations**, add `DB_PASSWORD` with your local MySQL password to the application's environment variables.
6. If needed, also configure `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_URL`.
7. Run `StockimsApplication` and open [http://localhost:8080](http://localhost:8080).

Environment variables entered in IntelliJ's terminal do not automatically apply to an application started with the IDE's Run button. Configure that run configuration separately, or start the application from the terminal.

If IntelliJ does not recognize Lombok-generated methods or constructors, check annotation processing under **Settings > Build, Execution, Deployment > Compiler > Annotation Processors**, then reload the Maven project.

## Using StockIMS

1. Open **Mes articles** to browse the catalogue.
2. Choose **Ajouter un article** and enter a unique reference and a designation.
3. Open **Voir le stock** on an article.
4. Enter a positive whole-number quantity and choose an entry or a withdrawal.
5. To delete an article, select its delete action, review the confirmation page, tick the confirmation checkbox, and submit.

References support up to **20 characters** and designations up to **80 characters**. Newly created articles have zero stock until an entry is recorded.

## Stock rules

Available stock is calculated as:

**Available stock = total incoming quantities − total outgoing quantities**

For example, an entry of 12 units followed by a withdrawal of 3 units leaves 9 units available.

- Quantities must be positive whole numbers.
- A withdrawal cannot exceed the available quantity.
- Stock operations lock the article within a transaction to serialize changes to the same article.
- Article deletion removes the related movements and article in one transaction.
- Opening the deletion confirmation page does not delete data; deletion requires a confirmed POST request.

## Build and tests

Run the test suite:

```powershell
.\mvnw.cmd test
```

Build the executable JAR and run verification:

```powershell
.\mvnw.cmd clean verify
```

Tests use a disposable H2 database configured in [src/test/resources/application.properties](src/test/resources/application.properties). They do not require a running MySQL server or the application's database password.

The suite covers application startup, template rendering, article creation, validation, stock entries and withdrawals, deletion confirmation, dependent movement cleanup, preservation of unrelated articles, and missing-article responses.

After a successful build, run the packaged application with the same database environment variables:

```powershell
java -jar .\target\stockims-0.0.1-SNAPSHOT.jar
```

## Project structure

| Path | Purpose |
| --- | --- |
| `pom.xml` | Dependencies and Maven build configuration |
| `src/main/java/ma/esith/stockims/model/` | Articles and stock movement entities |
| `src/main/java/ma/esith/stockims/repository/` | Database access and article locking |
| `src/main/java/ma/esith/stockims/service/` | Article and stock business logic |
| `src/main/java/ma/esith/stockims/web/` | Article and stock controllers and form objects |
| `src/main/resources/templates/` | Thymeleaf pages and shared layout fragments |
| `src/main/resources/static/css/stockims.css` | Shared responsive stylesheet |
| `src/main/resources/application.properties` | Application and MySQL configuration |
| `src/test/java/ma/esith/stockims/` | Automated tests |
| `src/test/resources/application.properties` | Isolated test database configuration |

## Main routes

| Method | Route | Purpose |
| --- | --- | --- |
| GET | `/` | Home page |
| GET | `/articles` | Article catalogue |
| GET | `/articles/nouveau` | New article form |
| POST | `/articles` | Create an article |
| GET | `/stock/{id}` | Article stock page |
| POST | `/stock/{id}/entrees` | Record a stock entry |
| POST | `/stock/{id}/sorties` | Record a withdrawal |
| GET | `/stock/{id}/supprimer` | Review deletion |
| POST | `/stock/{id}/supprimer` | Delete an article after confirmation |

## Updating your local project

Stop the running application, then run these commands from the repository folder:

```powershell
git switch main
```

```powershell
git pull --ff-only origin main
```

Restart the application to load the changes. If Git reports local modifications that would be overwritten, commit or stash your work before updating.

## Troubleshooting

| Problem | What to check |
| --- | --- |
| `DB_PASSWORD` cannot be resolved | Set the variable in the terminal or IntelliJ run configuration used to launch the application. |
| MySQL connection refused | Confirm that MySQL is running and the configured host and port are correct. |
| Access denied for the database user | Verify the username, password, and permissions on `stockims_db`. |
| Unknown database `stockims_db` | Create the database before starting the application. |
| Java release 21 is not supported | Check that Maven and IntelliJ use a full JDK 21 installation. |
| Port 8080 is already in use | Stop the other application, or pass `--server.port=8081` as an application argument. |
| A reference already exists | Choose a unique article reference. |
| A withdrawal is rejected | Use a positive whole number no greater than the available stock. |
<img width="1280" height="719" alt="image" src="https://github.com/user-attachments/assets/b780a81a-66c1-4eb4-a633-568d33ca357f" />
<img width="1280" height="677" alt="image" src="https://github.com/user-attachments/assets/7d9c2cc9-0a2e-43bf-88de-889ef8254ace" />
<img width="1280" height="673" alt="image" src="https://github.com/user-attachments/assets/452faa12-5f37-43c2-9192-50a149c14eeb" />



