# MicroservicesChat

## Required variables
Define in a `.env` file located in project's root:
* `MSSQL_SA_PASSWORD` - used in Docker Compose to set the password of the database's admin account.
* `REGISTRY_PATH` - set to `localhost` to be able to run the docker compose locally.

Additionally, GitHub Actions Secrets defines the following:
* `MSSQL_SA_PASSWORD` - used in Docker Compose to set the password of the database's admin account.
* `REGISTRY_PATH` - the address of your container registry (i.e. `example.azurecr.io`). Must not end with a `/` or contain protocol elements such as `http://` etc.
* `REGISTRY_USERNAME` - your Azure Container Registry username.
* `REGISTRY_PASSWORD` - your Azure Container Registry password.

## Setup guide
### Option 1: Run the docker compose locally
1. Clone the repository
2. Set the required environment variables in a .env file in the project's root.
3. Run `docker compose up --build`
### Option 2: Run the docker compose using Azure Container Instances (ACI) and Azure Container Registry (ACR).
1. Log in to Azure CLI
2. Create a resource group.
3. Create a container registry.
4. Log in to the ACR.
5. Get your ACR credentials.
6. Fill out GitHub Secrets using the ACR credentials.