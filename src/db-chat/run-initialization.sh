# Wait to be sure that SQL Server came up
sleep 15s
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $1 -Q "CREATE DATABASE chatdb" -No