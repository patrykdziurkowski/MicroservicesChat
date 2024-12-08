# Wait to be sure that SQL Server came up
while ! /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $1 -Q "SELECT 1" -b -No; do
    echo "Waiting for chatdb database startup..."
    sleep 5
done
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $1 -Q "CREATE DATABASE chatdb" -No
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $1 -Q "CREATE DATABASE chatauth" -No