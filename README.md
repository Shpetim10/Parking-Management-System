# Smart Parking Management & Billing System

A testable, modular **parking management system** with rich business logic, designed for the SWE 303 course.

The system manages:

- Users & vehicles  
- Eligibility to start parking sessions  
- Parking zones & spots  
- Session lifecycle & duration  
- Dynamic pricing and discounts  
- Penalties & blacklist logic  
- Monitoring & reporting  
- Exit authorization at the gate  

It is built to **maximize testability** (unit, integration, and system tests) and demonstrate strong **business logic**, not just simple CRUD.

---

## 1. Project Goals

- Model a **realistic parking system** with:
  - Dynamic tariffs and occupancy-based pricing
  - Penalties for overstay, lost tickets, and misuse
  - Blacklist logic based on penalty history
  - Strict exit authorization rules

- Show **good software engineering practice**:
  - Clean layered architecture
  - Clear separation of concerns
  - Rich domain model with invariants

- Support **advanced testing techniques**:
  - Boundary Value Testing (BVT)
  - Equivalence Class Testing (ECT)
  - Decision tables
  - MC/DC-style coverage
  - Integration (neighbourhood-based)
  - System-level scenario testing

---

## 2. High-Level System Overview

### 2.1 Main Concepts

- **User** – registered customer with a `UserStatus` and `AccountStanding`.
- **Vehicle** – identified by plate number, linked to a user.
- **ParkingZone** – logical area of type `STANDARD`, `EV`, or `VIP`.
- **ParkingSpot** – individual spot in a zone, with a `SpotState`.
- **ParkingSession** – represents a user parking event (entry → exit).
- **Tariff** – base hourly rate, caps, weekend/holiday surcharge, overnight options.
- **DynamicPricingConfig** – peak/off-peak multipliers, high-occupancy surge.
- **DiscountInfo / SubscriptionPlan** – subscription & promo discounts.
- **Penalty / PenaltyHistory** – overstay, lost ticket, misuse penalties.
- **BillingResult / BillingRecord** – all price components for one session.
- **Monitoring / BlacklistStatus** – monitors penalty history and account health.
- **ExitDecision** – final decision at the gate, with an `ExitFailureReason`.

### 2.2 System Boundary

Inside the system:

- Controllers (entry points)
- Services (business logic)
- Repositories (in-memory persistence)
- Domain models & value objects
- Settings & enums

Outside the system (simulated):

- Entry/exit gates and sensors
- Real payment provider
- Real database or external identity system

---

## 3. Functional Scope (System-Level)

### 3.1 User & Vehicle Management

- Register and store users with:
  - `UserStatus` ∈ {`ACTIVE`, `INACTIVE`, `BLACKLISTED`}
  - `AccountStanding` ∈ {`GOOD_STANDING`, `WARNING`, `SUSPENDED`}
- Associate vehicles (plate numbers) to users.
- Ensure each vehicle has **at most one** active session.

### 3.2 Parking Zones & Spots

- Manage `ParkingZone` with `ZoneType`:
  - `STANDARD`, `EV`, `VIP`
- Manage `ParkingSpot` with `SpotState`:
  - `FREE`, `RESERVED`, `OCCUPIED`
- Check availability and assign spots deterministically.
- Compute zone occupancy ratios for dynamic pricing and reporting.

### 3.3 Parking Sessions

- Start a session only if the user and vehicle are **eligible**.
- Track session lifecycle via `SessionState`:
  - `OPEN`, `PAYMENT_PENDING`, `PAID`, `CLOSED`, `CANCELLED`, `EXPIRED`
- Enforce rules:
  - Max active sessions per user
  - One active session per vehicle
  - No modifications after `CLOSED` / `CANCELLED` / `EXPIRED`

### 3.4 Eligibility & Entry Control

Before creating a session, the system computes an **EligibilityResult** based on:

- `UserStatus` (must be `ACTIVE`)
- `AccountStanding` (must not be `SUSPENDED`)
- Number of active sessions (user & vehicle)
- Unpaid sessions (if configured)
- Optional daily usage limits

If `EligibilityResult.allowed == false`, the session is **not created**.

### 3.5 Duration, Day Type & Time-of-Day Band

- Compute parking duration between entry and exit.
- Round duration **up** to full hours (e.g. `(minutes + 59)/60`).
- Detect overstay vs `maxDurationHours`.
- Classify:
  - `DayType` ∈ {`WEEKDAY`, `WEEKEND`, `HOLIDAY`}
  - `TimeOfDayBand` ∈ {`OFF_PEAK`, `PEAK`} using:
    - `START_PEAK_TIME = 11:00`
    - `END_PEAK_TIME   = 21:00`

### 3.6 Pricing & Dynamic Factors

Base price:

- `base = durationHours × Tariff.baseHourlyRate`.

Adjustments:

1. **Time-of-day**:
   - `OFF_PEAK` → multiply by `offPeakMultiplier`
   - `PEAK` → multiply by `peakHourMultiplier`
2. **High occupancy surge**:
   - If occupancyRatio ≥ highOccupancyThreshold, multiply by highOccupancyMultiplier.
3. **Weekend/holiday**:
   - If `DayType` ∈ {`WEEKEND`, `HOLIDAY`}, multiply by `(1 + weekendOrHolidaySurchargePercent)`.
4. **Caps**:
   - Apply `Tariff.dailyCap` if set.
   - Respect global `MAX_PRICE_CAPACITY` (1,000,000).

### 3.7 Discounts, Caps & Tax

Discounts are applied in this order:

1. Start with:
   - `amount = basePrice + penaltiesTotal`
2. Apply **subscription percent discount**.
3. Apply **promo percent discount**.
4. Apply **fixed promo discount**.
5. Clamp:
   - if amount < 0 → amount = 0
6. Apply **maxPriceCap**, if provided.
7. Compute tax:
   - `taxAmount = amount × TAX_RATIO` (e.g. 0.2 = 20%)
8. Compute:
   - `netPrice = amount`
   - `finalPrice = netPrice + taxAmount`
9. Enforce `finalPrice <= MAX_PRICE_CAPACITY`.

All values are rounded with fixed decimal scale (typically 2) and `RoundingMode.HALF_UP`.

### 3.8 Penalties & Blacklist

Penalty types:

- `PenaltyType.OVERSTAY` – extra hours beyond limit.
- `PenaltyType.LOST_TICKET` – fixed fee.
- `PenaltyType.MISUSE` – zone misuse fee.

Penalty history:

- `PenaltyHistory` per user with a list of penalties (type, amount, timestamp).

Blacklist:

- Uses `MAX_PENALTIES_ALLOWED` and `BLACKLIST_WINDOW` (30 days) to derive `BlacklistStatus`:
  - `NONE`, `CANDIDATE_FOR_BLACKLISTING`, `BLACKLISTED`
- Updates `AccountStanding` (e.g. to `SUSPENDED`) when thresholds are exceeded.
- `AccountStanding` + `UserStatus` influence future eligibility and possibly exit decisions.

### 3.9 Billing & Records

For each session, the system computes a `BillingResult` holding:

- `basePrice`
- `discountsTotal`
- `penaltiesTotal`
- `netPrice`
- `taxAmount`
- `finalPrice`

And stores a `BillingRecord` with:

- `sessionId`, `userId`, `zoneType`
- `entryTime`, `exitTime`
- `BillingResult`

Each session has **at most one** `BillingRecord`.

### 3.10 Exit Authorization

The system decides if the car can exit by returning an `ExitDecision`:

- `allowed : boolean`
- `failureReason : ExitFailureReason`

Where:

```java
public enum ExitFailureReason {
    NONE,
    USER_INACTIVE,
    SESSION_NOT_PAID,
    ALREADY_CLOSED,
    VEHICLE_MISMATCH
}
