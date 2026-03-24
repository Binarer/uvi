# UVI Project Mindmap

```mermaid
mindmap
  root((UVI Backend))
    Domain
      User Management
        User Profile
        User Interests
        SMS Verification
        Two-Factor Auth
      Family & Social
        Family Groups
        Family Members
        Invitations
      Geolocation
        Real-time Tracking
        Location History
        Nearby Users
      Places & GIS
        Point of Interests
        Place Tags
        Nearby Search
        Personalized Recommendations
      Routes
        OSM Road Network
        Dijkstra Algorithm
        Modes: DRIVING, PEDESTRIAN, PT
      Messaging
        Device Tokens
        Push Notifications
    Infrastructure
      Security
        JWT Auth
        CSRF Protection
        CORS Policy
        Rate Limiting (Bucket4j)
      Communication
        MQTT (Mosquitto)
        STOMP / WebSockets
      Performance
        Virtual Threads (Java 25)
        Caffeine Cache
        HikariCP
      Persistence
        PostgreSQL / PostGIS
        Hibernate Spatial
        pgRouting
    External Integrations
      SMS Aero API
      OpenStreetMap (OSM)
    DevOps
      Docker & Docker Compose
      OSM Loading Scripts
      Spring Boot Actuator
      Swagger / OpenAPI
```

## Краткое описание архитектуры
- **Backend**: Spring Boot 4.0.3 + Java 25 (Loom).
- **GIS**: Использование PostGIS для сложных пространственных запросов и pgRouting для построения маршрутов по графу дорог Екатеринбурга.
- **Real-time**: Комбинация MQTT и WebSockets для мгновенной передачи геопозиции.
- **Security**: Многоуровневая защита (SMS -> 2FA -> JWT) с ограничением частоты запросов.
