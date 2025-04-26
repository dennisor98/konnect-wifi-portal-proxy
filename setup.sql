CREATE USER IF NOT EXISTS 'clusteradmin'@'%' IDENTIFIED BY 'password';
CREATE USER IF NOT EXISTS 'clusteradmin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON *.* TO 'clusteradmin'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'clusteradmin'@'localhost' WITH GRANT OPTION;

