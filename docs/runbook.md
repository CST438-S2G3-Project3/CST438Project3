# Developer Runbook

Follow these steps to configure your local development environment and run the application.

## 1. Clone the Repository
Ensure you are on the default branch, which contains the latest tested version of the codebase.
\`\`\`bash
git clone https://github.com/CST438-S2G3-Project3/CST438Project3.git
cd CST438Project3
\`\`\`

## 2. Environment Configuration
We use environment variables to manage configuration. You must create a local `.env` file before starting the application.

1. Copy the example file:
   \`\`\`bash
   cp .env.example .env
   \`\`\`
2. Open `.env` and fill in the required values. *The `.env.example` file contains safe, test-only credentials that work out-of-the-box for the local database instance.*
3. **Security Warning:** The `.env` file is ignored by Git. Never commit real production secrets to the repository.

## 3. Run the Application
We use Docker Compose to orchestrate the services.

\`\`\`bash
# Build and start the containers in detached mode
docker compose up -d --build
\`\`\`

To view the application logs:
\`\`\`bash
docker compose logs -f
\`\`\`

## 4. Run the Tests
Once the application is running, you can execute the automated test suite to verify your setup.

**Backend Tests (Spring Boot):**
\`\`\`bash
cd api
./gradlew test
\`\`\`

**Frontend Tests (Expo):**
\`\`\`bash
cd app
npm test
\`\`\`

