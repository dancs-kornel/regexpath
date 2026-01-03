# RegExPath

A comprehensive web-based learning platform designed to make mastering regular expressions and XPath queries accessible, intuitive and engaging.

**Live Platform:** [www.regexpath.app](https://www.regexpath.app)

---
## Running Locally

The platform consists of three main components: a PostgreSQL database, a Spring Boot backend, and an Angular frontend

### Prerequisites
Before running the application locally, ensure you have the following installed:

| Component | Version | Installation |
|-----------|---------|--------------|
| **Java JDK** | 21 | macOS: `brew install openjdk@21`<br>Windows: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) |
| **Node.js** | 18.19+ or 20.9+ | macOS: `brew install node@20`<br>Windows: [nodejs.org](https://nodejs.org/) |
| **Angular CLI** | 19.2.0 | `npm install -g @angular/cli@19` |
| **PostgreSQL** | 14+ | macOS: `brew install postgresql@14`<br>Windows: [postgresql.org](https://www.postgresql.org/download/) |


**Verify installations:**
```bash
java -version
node -v
npm -v
ng version
```

### Database Setup

**1. Start PostgreSQL:**

*macOS:*
```bash
brew services start postgresql@14
```

*Windows (requires administrator privileges):*
```cmd
net start postgresql-x64-14
```

**2. Access PostgreSQL terminal:**

*macOS:*
```bash
sudo -u postgres psql
```

*Windows:*
```cmd
psql -U postgres
```

**3. Create database and user:**
```sql
CREATE DATABASE regex_xpath_platform;
CREATE USER regexpath_user WITH PASSWORD 'regexpath_dev_password';
GRANT ALL PRIVILEGES ON DATABASE regex_xpath_platform TO regexpath_user;

-- Connect to the database
\c regex_xpath_platform

-- Grant schema privileges (PostgreSQL 15+)
GRANT ALL ON SCHEMA public TO regexpath_user;

-- Exit
\q
```

> **Note:** Flyway migrations will automatically create all necessary tables on first backend startup.

### Startup Order

Start components in the following order:
1. PostgreSQL database
2. Backend (Spring Boot)
3. Frontend (Angular)

### Backend Startup

Navigate to the `server/` directory and run:

*macOS:*
```bash
cd server
./mvnw clean install
./mvnw spring-boot:run
```

*Windows:*
```cmd
cd server
mvnw clean install
mvnw spring-boot:run
```

The backend will be available at: **http://localhost:8080**

### Frontend Startup

Navigate to the `webapp/` directory and install dependencies (first time only):

```bash
cd webapp
npm install
```

Then start the development server:
```bash
npm start
# or: ng serve
```

The frontend will be available at: **http://localhost:4200**

---
## Key Features

### Interactive learning modules

**RegEx Sandbox**
- Real-time pattern matching with visual highlighting
- Token-based syntax breakdown showing how each element works
- Detailed explanations
- Automatic example generation based on input patterns
- Customizable test text input

**XPath Sandbox**
- Real-time node selection and highlighting
- Multiple view modes (editable source code, tree structure, rendered view)
- Support for complex XPath queries with detailed feedback

**Structured Lessons**
- Progressive curriculum building from fundamentals to advanced concepts
- Prerequisite tracking ensuring proper learning progression
- Interactive exercises embedded within lessons
- Adaptive difficulty system that adjusts to your performance

---

### Complete Teaching Platform

**Group Management**
- Create and manage student groups with unique invite codes

**Assignment System**
- Create custom exercise sets with multiple question types
- Generate exercises quicker with ready-to-go templates
- Distribute assignments to specific groups
- Set deadlines and track completion status

**Progress Tracking & Analytics**
- Hierarchial statistics from group level down to individual submissions
- Detailed performance metrics per student
- Export capabilities
  
