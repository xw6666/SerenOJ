#!/bin/bash
# MySQL setup for SerenOJ
mysql -u root <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';
FLUSH PRIVILEGES;
EOF
echo "MySQL password set."

mysql -u root -proot < /tmp/serenoj.sql
echo "Database imported."

mysql -u root -proot -e "USE serenoj; SHOW TABLES;"
