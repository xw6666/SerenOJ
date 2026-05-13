#!/bin/bash
# MySQL setup for SerenOJ
set -e

: "${DB_PASSWORD:?DB_PASSWORD is required}"

MYSQL_AUTH_ARGS=(-u root)
if [ -n "${CURRENT_DB_PASSWORD:-}" ]; then
  MYSQL_AUTH_ARGS+=("-p${CURRENT_DB_PASSWORD}")
fi

mysql "${MYSQL_AUTH_ARGS[@]}" <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${DB_PASSWORD}';
FLUSH PRIVILEGES;
EOF
echo "MySQL password set."

mysql -u root -p"${DB_PASSWORD}" < /tmp/serenoj.sql
echo "Database imported."

mysql -u root -p"${DB_PASSWORD}" -e "USE serenoj; SHOW TABLES;"
