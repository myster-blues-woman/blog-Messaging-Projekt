# Blog Management System - Messaging & Streaming

## Projektübersicht

Dieses Projekt ist ein verteiltes Blog-Management-System, das Quarkus und Kafka nutzt, um eine effiziente Kommunikation zwischen Microservices zu ermöglichen. Es besteht aus zwei Haupt-Services, von denen einer mit einer Datenbank verbunden ist.

### Architektur

-   **Blog Backend**:
    
    -   Verwaltung von Blogbeiträgen
    -   Versendet Validierungsanfragen für Blogeinträge über Kafka an den `validation-service`
    -   Speichert validierte bzw. nicht validierte Blogeinträge in der Datenbank
  -   **Kafka**:
    
    -   Nutzt zwei Topics zur Kommunikation:
        -   `validation-requests`: Für Anfragen zur Validierung von Blogeinträgen
        -   `validation-responses-out`: Für die Übermittlung der Validierungsergebnisse
  - **Text Validation Service**:
    
    -   Empfängt Nachrichten aus `validation-requests`
    -   Überprüft Namen und Beschreibungen von Blogs auf Gültigkeit
    -   Sendet die Validierungsergebnisse zurück über `validation-responses-out`


----------

## Schnellstart mit Docker

Das gesamte System kann mit Docker Compose gestartet werden. Eine entsprechende `docker-compose.yml`-Datei ist im Projekt enthalten.

### Repository klonen

```sh
git clone git@github.com:myster-blues-woman/blog-backend.git
cd blog-backend

```

### Docker-Images von GHCR herunterladen

Die Container-Images sind bereits in GHCR gespeichert. Die neuesten Versionen können mit folgendem Befehl heruntergeladen werden:

```sh
docker pull ghcr.io/myster-blues-woman/blog-backend:latest
docker pull ghcr.io/myster-blues-woman/validation-service:latest

```

### Dienste mit Docker Compose starten

```sh
docker-compose up -d

```

Dadurch werden folgende Dienste gestartet:

-   Blog Backend (`blog-backend`)
-   Text Validation Service (`text-validation-service`)
-   Kafka (`redpanda`)
-   PostgreSQL (Datenbank für `blog-backend`)

![image](https://github.com/user-attachments/assets/2f221edb-a1df-4c96-9e06-4c35f4124b1c)


### Dienste stoppen

```sh
docker-compose down

```

----------

## Validierungsregeln

Der `text-validation-service` stellt sicher, dass Blogeinträge bestimmten Anforderungen entsprechen, bevor sie in der Datenbank gespeichert werden. Folgende Validierungsregeln werden angewendet:

### Name-Validierung

-   Falls der Name eines Blogs bestimmte unzulässige Begriffe enthält, wird die Validierung abgelehnt.

### Beschreibung-Validierung

-   Die Beschreibung wird auf unerlaubte Inhalte oder ungültige Werte geprüft.

----------

## API-Anwendungsfälle

### Blog erstellen (mit erfolgreicher Validierung)

Request:

```sh
curl -X POST http://localhost:8080/blog \
     -H "Content-Type: application/json" \
     -d '{
           "name": "Mein erster Blog",
           "description": "Dies ist ein validierter Blogeintrag."
         }'

```

### Blog erstellen (mit fehlgeschlagener Namensvalidierung)

Request:

```sh
curl -X POST http://localhost:8080/blog \
     -H "Content-Type: application/json" \
     -d '{
           "name": "Ungültiger Name",
           "description": "Dieser Eintrag wird nicht validiert."
         }'

```

### Blog erstellen (mit fehlgeschlagener Beschreibungsvalidierung)

Request:

```sh
curl -X POST http://localhost:8080/blog \
     -H "Content-Type: application/json" \
     -d '{
           "name": "Validierter Name",
           "description": "Ungültiger Beschreibungstext."
         }'

```

### Liste aller Blogs abrufen

Request:

```sh
curl -X GET http://localhost:8080/blog

```

### Liste aller validierten Blogs abrufen

Request:

```sh
curl -X GET http://localhost:8080/blog/validated

```
