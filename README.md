# Hotel Room Reservation REST API

A clean, layered REST API for managing hotel rooms and reservations, built with **Java 21** and **Spring Boot 3.3.5**.

## Features
- Add rooms and search for rooms available in a date range
- Create reservations with automatic total price calculation (`nights * pricePerNight`)
- Overlap protection: a room cannot be booked twice for intersecting dates
- Cancel reservations (cancelled bookings free the room again)
- Bean Validation on all inputs and a global exception handler with consistent JSON errors
- Unit tests with JUnit 5 and Mockito

## Tech Stack
Java 21 · Spring Boot 3.3.5 · Spring Data JPA · Bean Validation · Lombok · H2 (dev) · PostgreSQL · Maven

## Architecture
```
controller  ->  service  ->  repository  ->  database
   (DTOs in/out)   (business rules)   (Spring Data JPA)
exception: custom exceptions + @RestControllerAdvice
```

## Run locally
```bash
mvn spring-boot:run                                        # H2 in-memory database
mvn spring-boot:run -Dspring-boot.run.profiles=postgres    # PostgreSQL (see application-postgres.yml)
mvn test
```
H2 console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:hoteldb`, user: `sa`, empty password)

## API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/api/v1/rooms` | Create a room |
| GET    | `/api/v1/rooms` | List all rooms |
| GET    | `/api/v1/rooms/{id}` | Get a room |
| GET    | `/api/v1/rooms/available?checkIn=&checkOut=` | List available rooms for a date range |
| POST   | `/api/v1/reservations` | Create a reservation |
| GET    | `/api/v1/reservations` | List all reservations |
| GET    | `/api/v1/reservations/{id}` | Get a reservation |
| PATCH  | `/api/v1/reservations/{id}/cancel` | Cancel a reservation |

## Example requests
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"roomNumber":"101","type":"DOUBLE","pricePerNight":120.00}'

curl "http://localhost:8080/api/v1/rooms/available?checkIn=2026-12-20&checkOut=2026-12-25"

curl -X POST http://localhost:8080/api/v1/reservations \
  -H "Content-Type: application/json" \
  -d '{"roomId":1,"guest":{"fullName":"Aysel Mammadova","email":"aysel@example.com","phone":"+994501234567"},"checkInDate":"2026-12-20","checkOutDate":"2026-12-25"}'

curl -X PATCH http://localhost:8080/api/v1/reservations/1/cancel
```

## Error format
```json
{
  "timestamp": "2026-11-16T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Room 1 is already booked between 2026-12-22 and 2026-12-24",
  "validationErrors": null
}
```

| Exception | HTTP status |
|-----------|-------------|
| RoomNotFoundException, ReservationNotFoundException | 404 |
| RoomAlreadyBookedException, RoomNotActiveException, DuplicateRoomNumberException | 409 |
| InvalidDateRangeException, validation errors, malformed input | 400 |

## Design notes
- **Overlap rule:** `existing.checkIn < new.checkOut AND existing.checkOut > new.checkIn`. Ranges are `[checkIn, checkOut)`, so same-day turnover is allowed.
- **Concurrency:** the room row is locked with a pessimistic write lock while a reservation is created, so two simultaneous requests cannot double-book a room.
- **DTOs / records:** entities are never exposed directly; mapping happens inside the transaction (`open-in-view: false`).
  
