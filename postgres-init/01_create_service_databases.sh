#!/usr/bin/env bash
set -Eeuo pipefail

if [[ -z "${POSTGRES_DATABASES:-}" ]]; then
  echo "POSTGRES_DATABASES is empty; no service databases to create."
  exit 0
fi

IFS=',' read -ra databases <<< "${POSTGRES_DATABASES}"

for database in "${databases[@]}"; do
  if [[ -z "${database}" ]]; then
    continue
  fi

  echo "Ensuring PostgreSQL database '${database}' exists."

  psql \
    --set=ON_ERROR_STOP=1 \
    --set=database_name="${database}" \
    --set=database_owner="${POSTGRES_USER}" \
    --username "${POSTGRES_USER}" \
    --dbname "${POSTGRES_DB}" <<-'SQL'
SELECT format('CREATE DATABASE %I OWNER %I', :'database_name', :'database_owner')
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = :'database_name'
)
\gexec
SQL
done
