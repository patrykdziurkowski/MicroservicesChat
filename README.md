# MicroservicesChat

## Required variables
### To run the project locally
Define in a `.env` file located in project's root:
* `MSSQL_SA_PASSWORD` - used in Docker Compose to set the password of the database's admin account.
* `JWT_SECRET` - a 256-bit secret key used to generate JWT keys.
* `REGISTRY_PATH` - (optional) the url of your image registry. To store images locally do not set this variable or set it to `localhost`.
* `DOCKER_SOCKET_PATH` - (optional) used in docker-compose file. Set to `//./pipe/docker_engine` if developing on Windows.

### To run the project remotely on Azure Container Instances
Additionally, GitHub Actions Secrets defines the following:
* `MSSQL_SA_PASSWORD` - used in Docker Compose to set the password of the database's admin account.
* `JWT_SECRET` - a 256-bit secret key used to generate JWT keys.
* `REGISTRY_PATH` - the address of your container registry (i.e. `example.azurecr.io`). Must not end with a `/` or contain protocol elements such as `http://` etc.
* `REGISTRY_USERNAME` - your Azure Container Registry username.
* `REGISTRY_PASSWORD` - your Azure Container Registry password.
* `DNS_NAME_LABEL` - a unique identifier for your website's DNS.
* `AZURE_LOCATION` - the location of your Azure Container Registry, for example `polandcentral`.
* `AZURE_STORAGE_ACCOUNT` - the name of the storage account used to create the required file shares.
* `AZURE_STORAGE_KEY` - the key to the storage account used to create the required file shares.

## Setup guide
### Option 1: Run the docker compose locally
1. Clone the repository
2. Set the required environment variables in a .env file in the project's root.
3. Run `docker compose up --build`
### Option 2: Run the docker compose using Azure Container Instances (ACI) and Azure Container Registry (ACR).
1. Log in to Azure.
2. Create a resource group.
3. Create a container registry.
4. Log in to the ACR.
5. Get your ACR credentials.
6. Setup the file shares on Azure using the guide found [here](https://learn.microsoft.com/en-us/azure/container-instances/container-instances-container-group-automatic-ssl).
6. Fork the repository.
7. Fill out GitHub Secrets.
8. Push a commit to main to trigger the deployment workflow.