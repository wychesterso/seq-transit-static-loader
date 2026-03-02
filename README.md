# SEQ Transit – Static Loader

A scheduled Spring Boot application that ingests GTFS static data from Translink (South East Queensland) and loads it into a Neon-hosted PostgreSQL database.

This service runs daily at 3:00 AM (UTC+10) via GitHub Actions and ensures the backend database reflects the latest published transit schedule data.

It can also be triggered manually using the **workflow_dispatch** option.

---

## Overview

<img width="820" height="180" alt="D1" src="https://github.com/user-attachments/assets/46ba8477-1f67-4d82-b10f-0f12c1e07483" />

Within the SEQ Transit pipeline, `seq-transit-static-loader` is responsible for:

- Downloading GTFS static data from Translink
- Extracting the GTFS ZIP archive
- Truncating existing schedule tables prior to load
- Loading each GTFS file into PostgreSQL using bulk `COPY` operations
- Ensuring the database contains data on services for the upcoming day

Companion Projects:
- [API Server](https://github.com/wychesterso/seq-transit-server)
- [Mobile App](https://github.com/wychesterso/seq-transit-app)

---

## Data Source

Static GTFS data is sourced from: **[https://gtfsrt.api.translink.com.au/GTFS/SEQ_GTFS.zip](https://gtfsrt.api.translink.com.au/GTFS/SEQ_GTFS.zip)**

---

## Tech Stack

- Java 17
- Spring Boot 3.5.9
- Gradle
- PostgreSQL (org.postgresql:postgresql)
- Liquibase 4.33.0
- GitHub Actions (cron scheduling)

---

## Database Schema

Database schema management is handled by **Liquibase**.

On an empty database, Liquibase:

- Creates tables matching the GTFS specification
- Initializes indexes to assist with performance

This ensures the ingestion pipeline can assume a fully prepared schema before performing bulk loads.

---

## Environment Variables

Required configuration:

```bash
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
```
