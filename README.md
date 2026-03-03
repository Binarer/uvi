# UVI — Family Location & Safety Backend

> **Spring Boot 4** REST API для семейной геолокации с маршрутизацией по реальным дорогам, MQTT-трекингом и двухфакторной аутентификацией.

---

## ✨ Возможности

| Категория | Детали |
|-----------|--------|
| 🔐 **Аутентификация** | SMS OTP → JWT (access + refresh rotation) → опциональный Google 2FA (TOTP) |
| 📍 **Геолокация** | Сохранение, история треков, поиск пользователей в радиусе (PostGIS geography) |
| 🗺️ **Маршрутизация** | Пешеходный / Авто / Общественный транспорт по OSM-данным через pgRouting + Dijkstra |
| 👨‍👩‍👧 **Семьи** | Создание семей, роли (ADMIN / MEMBER), инвайты по номеру телефона |
| 📌 **Места** | Кастомные точки интереса с фото, тегами, геометрией (JTS Point) |
| 📡 **Real-time** | MQTT (Mosquitto) публикация + подписка, WebSocket/STOMP для клиентов |
| 📟 **Устройства** | Регистрация iOS/Android устройств, push-уведомления |
| 🏷️ **Теги и интересы** | Привязка тегов к местам, интересы пользователей |
| 🛡️ **Безопасность** | Rate limiting (Bucket4j), stateless JWT, BCrypt, soft-delete |
| ⚡ **Производительность** | Java 25 Virtual Threads, Caffeine Cache, CDS в Docker |

---

## 🛠️ Технологический стек

### Backend
- **Java 25** + **Spring Boot 4.0**
- **Spring Security** — stateless JWT-аутентификация
- **Spring Data JPA** / **Hibernate** — ORM
- **Spring Integration MQTT** — MQTTv5 брокер (Mosquitto)
- **Spring WebSocket** — STOMP поверх WebSocket с JWT-авторизацией

### База данных
- **PostgreSQL 16** + **PostGIS 3.4** — пространственные запросы
- **pgRouting** — маршрутизация по дорожному графу OSM
- **JTS Topology Suite** — геометрические объекты на стороне Java

### Инфраструктура
- **Docker** / **Docker Compose** — полный стек одной командой
- **Eclipse Mosquitto 2.0** — MQTT-брокер
- **Gradle 8** — сборка

### Инструменты разработки
- **Lombok** — снижение бойлерплейта
- **SpringDoc OpenAPI 3 (Swagger UI)** — автодокументация API
- **JUnit 5** — юнит-тесты сервисов
- **Bucket4j** — rate limiting
- **Caffeine** — in-memory кэш
- **JJWT** — работа с JWT
- **TOTP (GoogleAuth)** — двухфакторная аутентификация

---

## 🚀 Быстрый старт

### Требования
- [Docker](https://docs.docker.com/get-docker/) ≥ 24
- [Docker Compose](https://docs.docker.com/compose/) ≥ 2.20

### 1. Клонировать репозиторий

```bash
git clone https://github.com/Binarer/uvi.git
cd uvi
```

### 2. Настроить окружение

Скопируйте пример конфигурации и заполните переменные:

```bash
cp application.properties.example src/main/resources/application.properties
```

Обязательно измените следующие значения:

```properties
# JWT
jwt.secret=YOUR_VERY_LONG_SECRET_KEY_AT_LEAST_32_CHARACTERS

# SMS-провайдер (или оставьте development mode)
sms.development-mode=true

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/uvi
spring.datasource.username=postgres
spring.datasource.password=your_password

# MQTT
mqtt.broker.url=tcp://localhost:1883
mqtt.client.id=uvi-server
```

### 3. Запустить через Docker Compose

```bash
docker compose up --build
```

Это поднимет:
- 🟢 **API** → `http://localhost:8080`
- 🐘 **PostgreSQL + PostGIS** → `localhost:5432`
- 📡 **Mosquitto MQTT** → `localhost:1883` (WebSocket: `9001`)

### 4. Swagger UI

После запуска документация API доступна по адресу:

```
http://localhost:8080/swagger-ui.html
```

---

## 📂 Структура проекта

```
src/main/java/org/example/uvi/
├── App/
│   ├── Domain/
│   │   ├── Enums/              # Перечисления (роли, статусы, типы мест)
│   │   ├── Models/             # JPA-сущности (User, Family, Place, Device…)
│   │   ├── Repository/         # Spring Data репозитории
│   │   └── Services/           # Бизнес-логика
│   └── Infrastructure/
│       ├── Components/Mqtt/    # MQTT publisher / subscriber
│       ├── Config/             # Security, WebSocket, MQTT, Cache конфиги
│       ├── Http/
│       │   ├── Controller/     # REST-контроллеры
│       │   ├── Dto/            # Request / Response DTO
│       │   ├── Exception/      # Глобальный обработчик ошибок
│       │   └── Mapper/         # DTO ↔ Entity маппинг
│       ├── RateLimit/          # Bucket4j interceptor
│       └── Security/           # JwtService, JwtAuthFilter
└── UviApplication.java
```

---

## 🔑 Аутентификация

Поток аутентификации — трёхшаговый:

```
POST /api/v1/auth/send-code      → отправить SMS с 6-значным кодом
POST /api/v1/auth/verify-code    → проверить код → получить access + refresh token
POST /api/v1/auth/refresh        → обновить токены (refresh rotation)
POST /api/v1/auth/logout         → отозвать все refresh-токены
```

Опциональный Google Authenticator (2FA):

```
POST /api/v1/auth/2fa/setup      → получить QR-код для Google Authenticator
POST /api/v1/auth/2fa/confirm    → подтвердить первый TOTP-код
POST /api/v1/auth/2fa/verify     → верифицировать TOTP при входе
DELETE /api/v1/auth/2fa          → отключить 2FA
```

Все защищённые эндпоинты требуют заголовок:

```
Authorization: Bearer <access_token>
```

---

## 🗺️ Маршрутизация

Маршруты строятся по реальной дорожной сети на базе данных **OpenStreetMap** через алгоритм **Dijkstra** (pgRouting).

```
POST /api/v1/routes/calculate
```

```json
{
  "startLat": 56.8389,
  "startLon": 60.6057,
  "endLat": 56.8519,
  "endLon": 60.6122,
  "mode": "PEDESTRIAN"
}
```

Поддерживаемые режимы:

| Режим | Описание |
|-------|----------|
| `PEDESTRIAN` | Пешеходные пути и тротуары |
| `DRIVING` | Автомобильные дороги |
| `PUBLIC_TRANSPORT` | Маршруты общественного транспорта |

### Загрузка OSM-данных

Для маршрутизации необходимо загрузить данные OSM в PostgreSQL:

```powershell
# Скачать .osm.pbf файл вашего региона с https://download.geofabrik.de/
./scripts/load-osm-to-postgres.ps1
```

---

## 📡 Real-time геолокация

### MQTT

Мобильные клиенты публикуют локацию в топик:

```
location/<userId>
```

Пример сообщения (JSON):

```json
{
  "userId": 42,
  "latitude": 56.8389,
  "longitude": 60.6057,
  "accuracy": 10.5,
  "batteryLevel": 87,
  "speed": 1.2,
  "timestamp": "2026-03-03T16:57:45Z"
}
```

### WebSocket / STOMP

Клиенты могут подписываться на обновления через WebSocket:

```
ws://localhost:8080/ws
STOMP CONNECT с заголовком: Authorization: Bearer <token>
Подписка: /topic/location/<userId>
```

---

## 🧪 Тесты

```bash
./gradlew test
```

Тесты покрывают сервисы:
- `AuthService`, `FamilyService`, `FamilyInvitationService`
- `PlaceService`, `TagService`, `UserService`, `UserInterestService`

---

## 🐳 Docker

### Сборка образа

```bash
docker build -t uvi:latest .
```

Dockerfile использует многоступенчатую сборку:
1. **Builder** — Gradle сборка с кэшированием зависимостей
2. **Runtime** — BellSoft Liberica JRE 25, Spring Layered JARs, CDS для быстрого старта

### Переменные окружения

| Переменная | Описание | Пример |
|-----------|----------|--------|
| `SPRING_DATASOURCE_URL` | JDBC URL PostgreSQL | `jdbc:postgresql://postgres:5432/uvi` |
| `SPRING_DATASOURCE_USERNAME` | Пользователь БД | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Пароль БД | `secret` |
| `MQTT_BROKER_URL` | URL MQTT-брокера | `tcp://mosquitto:1883` |
| `MQTT_CLIENT_ID` | ID MQTT-клиента | `uvi-server` |
| `JWT_SECRET` | Секрет для подписи JWT (≥32 символов) | — |
| `JWT_EXPIRATION_MS` | Время жизни access-токена (мс) | `900000` (15 мин) |
| `SMS_DEVELOPMENT_MODE` | Режим разработки (коды в логах) | `true` |

---

## 📋 Основные API эндпоинты

| Метод | URL | Описание |
|-------|-----|----------|
| `POST` | `/api/v1/auth/send-code` | Отправить SMS |
| `POST` | `/api/v1/auth/verify-code` | Верифицировать код |
| `POST` | `/api/v1/auth/refresh` | Обновить токены |
| `GET` | `/api/v1/users/me` | Профиль текущего пользователя |
| `PUT` | `/api/v1/users/me` | Обновить профиль |
| `POST` | `/api/v1/families` | Создать семью |
| `GET` | `/api/v1/families/{id}` | Получить семью |
| `POST` | `/api/v1/families/{id}/members` | Добавить участника |
| `POST` | `/api/v1/invitations` | Отправить приглашение |
| `POST` | `/api/v1/locations` | Сохранить геолокацию |
| `GET` | `/api/v1/locations/latest` | Последняя геолокация |
| `GET` | `/api/v1/locations/nearby` | Пользователи рядом |
| `POST` | `/api/v1/routes/calculate` | Построить маршрут |
| `POST` | `/api/v1/places` | Создать место |
| `GET` | `/api/v1/places/nearby` | Ближайшие места |
| `GET` | `/api/v1/tags` | Список тегов |
| `POST` | `/api/v1/devices` | Зарегистрировать устройство |

Полная документация: **Swagger UI** → `http://localhost:8080/swagger-ui.html`

---

## 📄 Лицензия

MIT License © 2026
