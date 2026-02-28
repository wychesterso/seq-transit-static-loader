# SEQ Transit – Static Loader

A scheduled Spring Boot application that ingests GTFS static data from Translink (South East Queensland) and loads it into a Neon-hosted PostgreSQL database.

This service runs daily at 3:00 AM (UTC+10) via GitHub Actions and ensures the backend database reflects the latest published transit schedule data.

It can also be triggered manually using the **workflow_dispatch** option.

---

## Overview

<img width="821" height="180" alt="D1" src="https://github.com/user-attachments/assets/d957b1b4-b5b8-4177-a662-4cab885214ae" />

Within the SEQ Transit pipeline, `seq-transit-static-loader` is responsible for:

- Downloading GTFS static data from Translink
- Extracting the GTFS ZIP archive
- Truncating existing schedule tables prior to load
- Loading each GTFS file into PostgreSQL using bulk `COPY` operations
- Ensuring the database contains data on services for the upcoming day

---

## Tech Stack

- Java 17
- Spring Boot 3.5.9
- Gradle
- PostgreSQL (org.postgresql:postgresql)
- Liquibase 4.33.0
- GitHub Actions (cron scheduling)

---

## Environment Variables

Required configuration:

```bash
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
```

---

## Data Source

Static GTFS data is sourced from **[https://gtfsrt.api.translink.com.au/GTFS/SEQ_GTFS.zip](https://gtfsrt.api.translink.com.au/GTFS/SEQ_GTFS.zip)**

Only publicly available transit schedule data is consumed.

---

## Database Schema

Database schema management is handled by **Liquibase**.

On an empty database, Liquibase:

- Creates tables matching the GTFS specification
- Initializes indexes to assist with performance

This ensures the ingestion pipeline can assume a fully prepared schema before performing bulk loads.
