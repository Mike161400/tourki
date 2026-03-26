# Tour Management System

Spring Boot API для управления турами, направлениями, гидами, бронированиями и отзывами.

## Домен

Сущности проекта:

- `Destination` — направление (страна, город, описание).
- `Guide` — гид, который ведет туры.
- `Tour` — тур с датами, лимитом мест, ценой, гидом и набором направлений.
- `Booking` — бронь места в туре для клиента (`travelerUsername`).
- `Review` — отзыв участника о завершенном туре.

## Ключевые бизнес-правила

- Тур включает направления (`Tour` - `Destination`) и может иметь назначенного гида (`Tour` > `Guide`).
- Бронирование закрепляет место за клиентом: одна активная бронь на пользователя в рамках тура.
- Количество мест в туре ограничено `maxSeats`; при достижении лимита бронь отклоняется.
- Отзыв можно оставить только если:
  - тур имеет статус `COMPLETED`;
  - пользователь был участником тура (есть `CONFIRMED` бронь);
  - пользователь еще не оставлял отзыв по этому туру.

## Роли и безопасность

JWT + refresh token rotation.

Роли:

- `ROLE_ADMIN`
- `ROLE_GUIDE`
- `ROLE_TRAVELER`

Базовые ограничения:

- Публичные только `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`.
- Бронирование места: `ROLE_TRAVELER`, `ROLE_ADMIN`.
- Завершение тура: `ROLE_GUIDE`, `ROLE_ADMIN`.
- CRUD справочников/туров: преимущественно `ROLE_ADMIN` (часть тур-операций доступна `ROLE_GUIDE`).

## Бизнес-операции

- `POST /api/tours/{id}/auto-assign-guide` — автоназначение активного гида с минимальной загрузкой.
- `POST /api/tours/{id}/book-seat` — бронь места в туре.
- `PUT /api/tours/{id}/complete` — завершение тура (только после даты окончания).
- `POST /api/tours/{id}/reviews` — оставить отзыв по завершенному туру.
- `GET /api/tours/{id}/availability` — доступность мест.
- `GET /api/reports/guides/workload` — загрузка гидов.

## REST API (основные ресурсы)

- `/api/destinations`
- `/api/guides`
- `/api/tours`
- `/api/bookings`
- `/api/reviews`

## Быстрый старт

1. Поднять PostgreSQL и создать БД `tourdb`.
2. Настроить переменные окружения (можно взять из `.env.example`).
3. Запустить приложение:

```bash
mvn spring-boot:run
```

По умолчанию сервер работает на `http://localhost:8080` (SSL выключен).

Для запуска с HTTPS:

1. Сгенерировать `src/main/resources/keystore.p12` скриптом `generate-certs.sh`.
2. Включить переменные `SSL_ENABLED=true`, `SERVER_PORT=8443` и `SSL_KEY_STORE_PASSWORD=<пароль keystore>`.

## Стартовые пользователи

Создаются автоматически при первом запуске:

- `admin / Admin1234!` (`ROLE_ADMIN`)
- `guide1 / Guide1234!` (`ROLE_GUIDE`)
- `guide2 / Guide2234!` (`ROLE_GUIDE`)
- `traveler1 / Traveler1234!` (`ROLE_TRAVELER`)
- `traveler2 / Traveler2234!` (`ROLE_TRAVELER`)

## Postman

В репозитории есть коллекция `postman_collection.json` с готовыми запросами:

- Auth (register/login/refresh)
- Destinations
- Guides
- Tours
- Bookings
- Reviews
- Reports

Перед запуском запросов в Postman:

1. Выполнить `Auth -> Login Admin` или `Auth -> Login Traveler1`.
2. Убедиться, что переменная `accessToken` установилась автоматически тест-скриптом.
