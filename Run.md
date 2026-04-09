# RUN.md

## Java Version
Java 21 (Microsoft OpenJDK 21.0.10)

## Database Setup
The database is auto-created by JPA (`spring.jpa.hibernate.ddl-auto=update`).
MariaDB is used as a drop-in replacement for MySQL.
The database `campusstore` is created automatically on first run.

## Database Credentials
Set in `src/main/resources/application.properties`:
- URL: `jdbc:mysql://localhost:3306/campusstore`
- Username: `root`
- Password: `root`

## Starting the Application
1. Start MariaDB: `sudo service mariadb start`
2. Run the app: `mvn spring-boot:run`
3. Open: `http://localhost:8080/catalog`

## Admin Login
- Email: `admin@example.com`
- Password: `Admin@1234`
- The admin account is created automatically at startup by `DataSeeder.java`

## Customer Login
Register a new account at `/register` or use:
- Email: `ember@example.com`
- Password: (set during registration)

## Seed Data
`DataSeeder.java` automatically seeds 1 admin user, 3 categories,
and 8 active products on first startup. No manual SQL required.

## Security Note
CSRF protection is disabled. All access control is enforced server-side
via session attributes and a HandlerInterceptor in `WebConfig.java`.