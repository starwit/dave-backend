# Elasticsearch Removal Summary

## Overview
This document summarizes the refactoring performed to remove Elasticsearch from the dave-backend project and transition to using PostgreSQL database for all storage needs.

## Changes Made

### 1. Dependencies Removed
- **pom.xml**: Removed `spring-boot-starter-data-elasticsearch` dependency
- **pom.xml**: Removed Elasticsearch properties from Maven profiles (elasticsearch.host, elasticsearch.password)

### 2. Docker Configuration
- **stack/docker-compose.yml**: Removed `setup`, `elastic`, and `kibana` services
- **stack/docker-compose.yml**: Removed Elasticsearch environment variables from dave-backend service
- **stack/docker-compose.yml**: Removed elastic volumes (elastic-certs, elastic-data, elastic-backup, kibana-data)
- **stack/.env**: Removed Elasticsearch configuration (ELASTIC_PASSWORD, KIBANA_PASSWORD, STACK_VERSION, ELASTICSEARCH_CERT_FINGERPRINT)

### 3. Application Configuration
- **src/main/resources/application-docker.yml**: Removed Elasticsearch configuration (index.suffix, host, port, user, etc.)
- **application-local.yml**: Removed Elasticsearch configuration
- **src/test/resources/application-unittest.yml**: Removed Elasticsearch configuration

### 4. Source Code Deleted
#### Repositories
- `src/main/java/de/muenchen/dave/repositories/elasticsearch/` (entire directory)
  - CustomSuggestIndex.java
  - MessstelleIndex.java
  - ZaehlstelleIndex.java

#### Domain/Entities
- `src/main/java/de/muenchen/dave/domain/elasticsearch/` (entire directory)
  - CustomSuggest.java
  - Fahrbeziehung.java
  - Hochrechnungsfaktor.java
  - Knotenarm.java
  - PkwEinheit.java
  - Zaehlstelle.java
  - Zaehlung.java
  - detektor/Messfaehigkeit.java
  - detektor/Messstelle.java
  - detektor/Messquerschnitt.java

#### Configuration
- `src/main/java/de/muenchen/dave/configuration/CustomElasticsearchConfiguration.java`

#### Services
- `src/main/java/de/muenchen/dave/services/ZaehlstelleIndexService.java`
- `src/main/java/de/muenchen/dave/services/CustomSuggestIndexService.java`
- `src/main/java/de/muenchen/dave/services/messstelle/MessstelleIndexService.java`
- `src/main/java/de/muenchen/dave/services/SucheService.java` (search functionality)
- `src/main/java/de/muenchen/dave/services/messstelle/MessstelleService.java`

#### Controllers
- `src/main/java/de/muenchen/dave/controller/SucheController.java`
- `src/main/java/de/muenchen/dave/controller/MessstelleController.java`

#### Test Files
- `src/test/java/de/muenchen/dave/domain/elasticsearch/` (entire directory with test factories)

## Components Still Referencing Deleted Services

### Critical Services to Refactor
The following services still have dependencies on deleted Elasticsearch services and will need to be refactored:

1. **ZaehlstelleController** - depends on ZaehlstelleIndexService
2. **AuswertungSpitzenstundeService** - depends on ZaehlstelleIndexService
3. **LadeZaehldatenService** - depends on ZaehlstelleIndexService
4. **ChatMessageService** - depends on ZaehlstelleIndexService
5. **AuswertungZaehlstellenKoordinateService** - depends on ZaehlstelleIndexService
6. **ReportService** - depends on ZaehlstelleIndexService
7. **FillZeitreihePdfBeanService** - depends on ZaehlstelleIndexService
8. **ProcessZaehldatenZeitreiheService** - depends on ZaehlstelleIndexService
9. **DienstleisterService** - depends on ZaehlstelleIndexService
10. **GenerateCsvService** - depends on ZaehlstelleIndexService
11. **FillPdfBeanService** - depends on ZaehlstelleIndexService
12. **ZaehlungPersistierungsService** - depends on ZaehlstelleIndexService
13. **ExternalZaehlungPersistierungsService** - depends on ZaehlstelleIndexService
14. **EmailSendService** - depends on ZaehlstelleIndexService

## Next Steps Required

### 1. Create JPA Entities for SQL Database
You need to create JPA entities to replace the Elasticsearch documents:
- Create JPA entity for Zaehlstelle
- Create JPA entity for Messstelle  
- Create JPA entity for Zaehlung
- Create JPA entity for CustomSuggest (for search autocomplete)
- Create other related entities (Fahrbeziehung, Knotenarm, etc.)

### 2. Create JPA Repositories
- Create `ZaehlstelleRepository extends JpaRepository<Zaehlstelle, String>`
- Create `MessstelleRepository extends JpaRepository<Messstelle, String>`
- Create `CustomSuggestRepository extends JpaRepository<CustomSuggest, String>`

### 3. Recreate Index Services Using JPA
- Create new `ZaehlstelleIndexService` using JpaRepository instead of ElasticsearchRepository
- Create new `MessstelleIndexService` using JpaRepository
- Create new `CustomSuggestIndexService` using JpaRepository
- Implement search functionality using PostgreSQL full-text search or LIKE queries

### 4. Implement Search Functionality
For the search functionality that was provided by Elasticsearch, you have several options:
- Use PostgreSQL full-text search capabilities
- Use simple LIKE queries for basic search
- Consider using a dedicated Java search library like Apache Lucene
- Use @Query annotations with JPA for custom search queries

### 5. Update Database Schema
- Create Flyway migrations to add tables for Zaehlstelle, Messstelle, Zaehlung, etc.
- Add appropriate indexes for search performance

### 6. Rebuild and Test
After implementing the above changes:
- Run `mvn clean install` to rebuild the project
- Fix any remaining compilation errors
- Update and run tests
- Test search functionality thoroughly

## Impact Assessment

### Functionality Lost (needs reimplementation):
- **Search/Autocomplete**: The SucheService provided search-as-you-type functionality
- **Messstelle Management**: MessstelleController endpoints for managing measurement locations
- **Full-text search**: Elasticsearch provided advanced search capabilities that need replacement

### Performance Considerations:
- PostgreSQL full-text search may not be as fast as Elasticsearch for large datasets
- Consider adding appropriate indexes on frequently searched fields
- May need to implement caching for search results

## Recommendations

1. **Prioritize**: Start by implementing the core JPA entities and repositories
2. **Search Strategy**: Decide on the search implementation approach early (PostgreSQL FTS vs other)
3. **Testing**: Write comprehensive tests for the new SQL-based search functionality
4. **Data Migration**: If there's existing data in Elasticsearch, create a migration script
5. **Monitoring**: Monitor database performance after transitioning from Elasticsearch

## Build Status
⚠️ **Warning**: The project will not compile until the services referencing deleted components are refactored to use the new JPA-based implementation.
