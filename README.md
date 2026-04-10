# 🚆 RailWiseAI — Intelligent Train Journey Planner

RailWiseAI is a backend system that intelligently computes and ranks train journeys between a source and destination based on multiple real-world factors such as route availability, seat status, delays, and connection feasibility.

It is designed as a modular, scalable Spring Boot application that simulates how modern travel recommendation engines evaluate and rank journey options.

---

## ✨ Key Features

### 🛤️ Route Intelligence
- Finds direct routes and one-stop (via) routes
- Uses train schedule data to build valid journey paths
- Handles multi-leg journey construction with connection validation

### 🎯 Scoring Engine
Routes are ranked based on a weighted scoring system:
- Seat Availability (AVL / RAC / WL)
- Train Delay Estimation
- Connection Buffer Time (for via routes)

This ensures that the final output is explainable and realistic, not random.

### 🚫 Hard Filtering Layer
Routes are filtered before scoring to eliminate invalid options:
- Removes routes with poor availability (high WL)
- Rejects unsafe connections (low buffer time)
- Prevents logically inconsistent journeys

### ⚡ Performance Optimization
- Uses Caffeine in-memory caching
- Implements TTL-based cache eviction (10 minutes)
- Reduces repeated computation for identical route queries
- Significantly improves API response time for frequent requests

### 🔁 Data Provider Abstraction
The system supports a pluggable data source layer:
- `LiveProvider` — API-based integration placeholder
- `MockProvider` — Used for development and testing

Fallback mechanism ensures system resilience when live data is unavailable.

---

## 🧠 System Architecture

```
JourneyController
       ↓
JourneyService        (Orchestrator)
       ↓
RouteFinderService    (Direct + Via routes)
       ↓
HardFilterService     (Reject invalid routes)
       ↓
ScoringEngine         (Rank by reliability)
       ↓
DataProviderService   (Live + Mock fallback)
```

---

## 🗄️ Database Layer

The system uses MySQL to store:
- Train master data
- Station information
- Train schedules (stop sequences)
- Delay records

> Seat availability is currently simulated via the Data Provider layer to mimic real-time external APIs.

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| ORM | Spring Data JPA |
| Database | MySQL |
| Caching | Caffeine |
| Build | Maven |

---

## 🛠️ Setup Instructions

### 1. Clone Repository

```bash
git clone https://github.com/KeyurWarkhedkar/railwise-ai.git
cd railwise-ai
```

### 2. Configure Database

Create a MySQL database:

```sql
CREATE DATABASE railwiseai;
```

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/railwiseai
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### 3. Run Application

```bash
.\mvnw spring-boot:run
```

Application starts on: `http://localhost:8080`

---

## 🧪 API Usage

### Get Best Journeys

```
GET /api/journey?src=MAS&dst=SBC&date=2026-04-10
```

### Sample Response

```json
{
    "routes": [
        {
            "routeType": "DIRECT",
            "score": 80,
            "reason": "AVL seat, 5 min delay",
            "route": {
                "trainNumber": "12001",
                "departureTime": "06:00:00",
                "arrivalTime": "10:00:00",
                "dayOffset": 0,
                "travelDate": "2026-04-10"
            }
        },
        {
            "routeType": "DIRECT",
            "score": 50,
            "reason": "RAC seat, 15 min delay",
            "route": {
                "trainNumber": "12002",
                "departureTime": "14:00:00",
                "arrivalTime": "19:00:00",
                "dayOffset": 0,
                "travelDate": "2026-04-10"
            }
        },
        {
            "routeType": "VIA",
            "score": 45,
            "reason": "AVL seat, 10 min delay, 60 min connection buffer",
            "route": {
                "leg1TrainNumber": "12004",
                "leg2TrainNumber": "12005",
                "connectionStation": "KPD",
                "connectionBufferMinutes": 60,
                "departureTime": "06:30:00",
                "arrivalTime": "12:30:00",
                "travelDate": "2026-04-10"
            }
        }
    ]
}
```

---

## 🗃️ Sample Database Setup

### Train Master Data

```sql
INSERT INTO train (train_number, train_name, type, origin_station, destination_station, total_duration_minutes) VALUES
('12001', 'Shatabdi Express',  'EXPRESS',   'MAS', 'SBC', 240),
('12002', 'Intercity Express', 'EXPRESS',   'MAS', 'SBC', 300),
('12003', 'Passenger Fast',    'PASSENGER', 'MAS', 'SBC', 420),
('12004', 'MAS-KPD Shuttle',   'PASSENGER', 'MAS', 'KPD', 120),
('12005', 'KPD-SBC Shuttle',   'PASSENGER', 'KPD', 'SBC', 180);
```

### Station Data

```sql
INSERT INTO station (station_code, station_name, zone, state) VALUES
('MAS', 'Chennai Central',    'SR',  'Tamil Nadu'),
('KPD', 'Katpadi Junction',   'SR',  'Tamil Nadu'),
('SBC', 'KSR Bengaluru City', 'SWR', 'Karnataka');
```

### Train Schedule Data

```sql
INSERT INTO train_schedule (train_number, station_code, stop_sequence, arrival_time, departure_time, day_offset) VALUES

-- Direct trains MAS → SBC
('12001', 'MAS', 1, NULL,         '06:00:00', 0),
('12001', 'SBC', 2, '10:00:00',   NULL,       0),

('12002', 'MAS', 1, NULL,         '14:00:00', 0),
('12002', 'SBC', 2, '19:00:00',   NULL,       0),

('12003', 'MAS', 1, NULL,         '22:00:00', 0),
('12003', 'SBC', 2, '06:00:00',   NULL,       1),

-- Via route: MAS → KPD → SBC
('12004', 'MAS', 1, NULL,         '06:30:00', 0),
('12004', 'KPD', 2, '08:30:00',   '08:35:00', 0),

('12005', 'KPD', 1, NULL,         '09:30:00', 0),
('12005', 'SBC', 2, '12:30:00',   NULL,       0);
```

### Train Delay Data

```sql
INSERT INTO train_delay (train_number, date, station_code, delay_minutes, source) VALUES
('12001', '2026-04-10', 'MAS',  5,  'ESTIMATED'),
('12002', '2026-04-10', 'MAS', 20,  'ESTIMATED'),
('12003', '2026-04-10', 'MAS', 60,  'ESTIMATED'),
('12004', '2026-04-10', 'MAS', 10,  'ESTIMATED'),
('12005', '2026-04-10', 'KPD', 15,  'ESTIMATED');
```

> **Note:** Seat availability is handled via the Data Provider layer (no DB insert required).
>
> Mock data used for testing:
> - `12001` → AVL 25, 5 min delay
> - `12002` → RAC 5, 15 min delay
> - `12003` → WL 10, 35 min delay
> - `12004` → AVL 40, 10 min delay
> - `12005` → WL 60, 60 min delay

---

## 🔮 Future Enhancements

This system is designed to be extensible. The following can be added without architectural changes:

### 🚦 Rate Limiting / API Throttling
A dedicated module for per-user request limiting and peak traffic handling.
> I have already implemented a similar system separately — see [API Shield](https://github.com/KeyurWarkhedkar/api-shield). It can be plugged in directly.

### ⚡ Distributed Caching (Redis)
Currently uses Caffeine (in-memory). In production this upgrades to:
- Redis cache
- Clustered, shared cache across multiple instances

### 📊 Real-Time Data Integration
- IRCTC / Railway API integration
- Live delay tracking
- Dynamic seat availability updates

### 🧠 Advanced Scoring
- ML-based route ranking
- Historical delay prediction
- Demand-based signals

---

## 💡 Note for Evaluators

The sample dataset is intentionally small and deterministic to:
- Ensure reproducible results
- Simplify evaluation
- Demonstrate the full pipeline: **route → filter → score → rank**

**Expected output for** `GET /api/journey?src=MAS&dst=SBC&date=2026-04-10`:
- `12001` ranks first — AVL seat, 5 min delay → score 80
- `12002` ranks second — RAC seat, 15 min delay → score 50
- `12004 → 12005` via KPD ranks third — AVL seat, 60 min connection buffer → score 45
- `12003` appears in results but scores low due to WL status and 35 min delay

---

## 👨‍💻 Author

**Built by Keyur**

Focus: Backend Systems · Spring Boot · System Design · Data Engineering
