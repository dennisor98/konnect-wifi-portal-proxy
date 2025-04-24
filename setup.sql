CREATE USER 'clusteradmin'@'%' IDENTIFIED BY 'cladmin';
GRANT ALL PRIVILEGES ON *.* TO 'clusteradmin'@'%' WITH GRANT OPTION;
-- Optional: Only include reset master if replication is needed
RESET MASTER;
