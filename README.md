# CineVault — Movie Reservation System

A full-stack cinema reservation system built with Spring Boot and React for the CO3201 Computer Science Final Year Project at the University of Leicester.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.2, Spring Security, JPA/Hibernate |
| Frontend | React 18, React Router, Stripe.js |
| Database | MySQL 8 |
| Cache | Redis |
| Payments | Stripe |
| Email | Gmail SMTP |
| Deployment | Docker, Docker Compose |

---

## Features

- JWT authentication with role-based access control (USER, ADMIN, SUPER_ADMIN)
- Visual seat map with colour-coded availability (Standard, VIP, Taken)
- Seat hold mechanism with 15-minute expiry and countdown timer
- Stripe payment integration with webhook confirmation
- Snack pre-order system with size options (Small, Medium, Large)
- Unique booking reference codes (e.g. CV-2026-A3X9K)
- Booking confirmation and cancellation emails
- Password reset via email with time-limited tokens
- Account settings — update name and change password
- Admin panel with movies, theaters, showtimes, snacks, reports and user management
- Super Admin role with protected account that cannot be edited or deleted
- Redis caching for seat availability with automatic cache eviction
- Accessibility features (WCAG 2.1) — high contrast mode, font scaling, ARIA labels, skip link
- Docker containerisation with docker-compose

---

## Prerequisites

To run locally you will need:

- Java 21
- Node.js 18+
- MySQL 8
- Redis
- Maven
- A Stripe account (for payments)
- A Gmail account with App Password (for emails)

---

## Running Locally

### 1. Clone the repository

```bash
git clone https://campus.cs.le.ac.uk/gitlab/io60/movie-reservation.git
cd movie-reservation
```

### 2. Set up the database

Open MySQL Workbench and create a new database:

```sql
CREATE DATABASE movie_reservation;
```

### 3. Configure credentials

Create a file at `backend/src/main/resources/application-local.properties` with your credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/movie_reservation
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
jwt.secret=your_jwt_secret
stripe.api.key=your_stripe_secret_key
stripe.webhook.secret=your_stripe_webhook_secret
spring.mail.username=your_gmail@gmail.com
spring.mail.password=your_gmail_app_password
```

This file is gitignored and will never be committed.

### 4. Start Redis

```bash
brew services start redis
```

### 5. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`. Spring Boot will automatically create all database tables on first run.

### 6. Start the frontend

Open a new terminal:

```bash
cd frontend
npm install
npm start
```

The frontend will start on `http://localhost:3000`.

### 7. Set up Stripe webhooks (for payment confirmation)

Install the Stripe CLI and run:

```bash
stripe listen --forward-to localhost:8080/api/payments/webhook
```

---

## Running with Docker

Make sure Docker Desktop is running, then from the root of the project:

### 1. Create a .env file

Create a `.env` file in the root of the project:

```
JWT_SECRET=your_jwt_secret
STRIPE_API_KEY=your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=your_stripe_webhook_secret
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

### 2. Build and start all containers

```bash
docker compose build
docker compose up
```

This will start MySQL, Redis, the Spring Boot backend and the React frontend automatically.

The app will be available at `http://localhost:3000`.

### 3. Stop all containers

```bash
docker compose down
```

---

## Default Super Admin Account

A Super Admin account is created automatically on first run:

| Field | Value |
|-------|-------|
| Email | admin@cinevault.com |
| Password | Admin@123 |

This account has full admin access and cannot be edited or deleted by other admins.

---

## Test Payment Cards

Use these Stripe test cards to test payments:

| Card | Number |
|------|--------|
| Success | 4242 4242 4242 4242 |
| Declined | 4000 0000 0000 0002 |

Use any future expiry date and any 3-digit CVC.

---

## Database Schema

### users
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | Primary key |
| email | VARCHAR | Unique, not null |
| password | VARCHAR | BCrypt hashed |
| first_name | VARCHAR | Not null |
| last_name | VARCHAR | Not null |
| role | ENUM | USER, ADMIN, SUPER_ADMIN |
| created_at | DATETIME | Set on creation |
| reset_token | VARCHAR | Password reset token |
| reset_token_expiry | DATETIME | Token expiry (1 hour) |

### movies
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | Primary key |
| title | VARCHAR | Not null |
| description | TEXT | |
| genre | VARCHAR | |
| duration_minutes | INT | |
| director | VARCHAR | |
| cast | TEXT | |
| poster_url | VARCHAR | |
| trailer_url | VARCHAR | |
| rating | DOUBLE | 0-10 |
| release_year | VARCHAR | |
| status | ENUM | NOW_SHOWING, COMING_SOON, ENDED |

### theaters
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | Primary key |
| name | VARCHAR | Not null |
| total_rows | INT | Not null |
| seats_per_row | INT | Not null |

### seats
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | Primary key |
| theater_id | BIGINT | Foreign key → theaters |
| row_label | VARCHAR | e.g. A, B, C |
| seat_number | INT | |
| type | ENUM | STANDARD, VIP, WHEELCHAIR |

### showtimes
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | Primary key |
| movie_id | BIGINT | Foreign key → movies |
| theater_id | BIGINT | Foreign key → theaters |
| start_time | DATETIME | Not null |
| end_time | DATETIME | Not null |
| ticket_price | DECIMAL(8,2) | Not null |
| status | ENUM | SCHEDULED, ONGOING, COMPLETED, CANCELLED |

### reservations
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | Primary key |
| user_id | BIGINT | Foreign key → users |
| showtime_id | BIGINT | Foreign key → showtimes |
| total_price | DECIMAL(10,2) | Includes seats and snacks |
| status | ENUM | HELD, PENDING, CONFIRMED, CANCELLED, REFUNDED |
| stripe_payment_intent_id | VARCHAR | |
| stripe_client_secret | VARCHAR | |
| created_at | DATETIME | |
| paid_at | DATETIME | Set on payment confirmation |
| held_until | DATETIME | UTC expiry for seat hold |
| booking_reference | VARCHAR | Unique e.g. CV-2026-A3X9K |

### reservation_seats
| Column | Type | Notes |
|--------|------|-------|
| reservation_id | BIGINT | Foreign key → reservations |
| seat_id | BIGINT | Foreign key → seats |

### reservation_snacks
| Column | Type | Notes |
|--------|------|-------|
| reservation_id | BIGINT | Foreign key → reservations |
| snack_id | BIGINT | Foreign key → snacks |
| quantity | INT | |

### snacks
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | Primary key |
| name | VARCHAR | Not null |
| price | DECIMAL(10,2) | Not null |
| description | VARCHAR | |
| emoji | VARCHAR | |
| available | BOOLEAN | Default true |
| size | ENUM | SMALL, MEDIUM, LARGE |

---

## API Endpoints

### Authentication — `/api/auth` (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT token |
| POST | `/api/auth/forgot-password` | Send password reset email |
| POST | `/api/auth/reset-password` | Reset password using token |

### Movies — `/api/movies`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/movies` | Public | Get all movies |
| GET | `/api/movies/{id}` | Public | Get movie by ID |
| POST | `/api/movies` | ADMIN | Create a movie |
| PUT | `/api/movies/{id}` | ADMIN | Update a movie |
| DELETE | `/api/movies/{id}` | ADMIN | Delete a movie |

### Showtimes — `/api/showtimes`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/showtimes/movie/{movieId}` | Public | Get showtimes for a movie |
| GET | `/api/showtimes/{id}` | Public | Get showtime by ID |
| POST | `/api/showtimes` | ADMIN | Create a showtime |
| DELETE | `/api/showtimes/{id}` | ADMIN | Delete a showtime |

### Seats — `/api/seats`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/seats/theater/{theaterId}` | Public | Get all seats for a theater |
| GET | `/api/seats/showtime/{showtimeId}/all` | Public | Get all seats for a showtime (Redis cached) |

### Reservations — `/api/reservations`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/reservations/seats/{showtimeId}` | Public | Get available seats for a showtime |
| POST | `/api/reservations` | USER | Create a reservation with seats and snacks |
| GET | `/api/reservations/my` | USER | Get current user's reservations |
| GET | `/api/reservations/{id}` | USER | Get a specific reservation |
| DELETE | `/api/reservations/{id}` | USER | Cancel a reservation |

### Payments — `/api/payments`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/payments/create-intent/{reservationId}` | USER | Create Stripe PaymentIntent |
| POST | `/api/payments/webhook` | Public | Stripe webhook for payment confirmation |

### Snacks — `/api/snacks`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/snacks` | Public | Get all available snacks |
| GET | `/api/snacks/all` | ADMIN | Get all snacks including unavailable |
| POST | `/api/snacks` | ADMIN | Create a snack |
| PUT | `/api/snacks/{id}` | ADMIN | Update a snack |
| DELETE | `/api/snacks/{id}` | ADMIN | Delete a snack |

### Users — `/api/users`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users/profile` | USER | Get current user profile |
| PUT | `/api/users/profile` | USER | Update first and last name |
| PUT | `/api/users/password` | USER | Change password |

### Admin — `/api/admin`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/theaters` | ADMIN | Get all theaters |
| POST | `/api/admin/theaters` | ADMIN | Create a theater with auto seat generation |
| GET | `/api/admin/showtimes` | ADMIN | Get all showtimes |
| GET | `/api/admin/users` | ADMIN | Get all users |
| PUT | `/api/admin/users/{id}/role` | ADMIN | Update user role |
| DELETE | `/api/admin/users/{id}` | ADMIN | Delete a user |
| GET | `/api/admin/reports` | ADMIN | Get analytics and occupancy reports |

---

## User Flow

```
1. BROWSE
   └── User visits CineVault
   └── Browses movies on the Browse Films page
   └── Filters by title or genre
   └── Clicks a movie to view details

2. SELECT SHOWTIME
   └── User views movie details and available showtimes
   └── Filters showtimes by date using the dropdown
   └── Clicks a showtime time slot

3. SELECT SEATS
   └── Visual seat map loads showing all seats
   └── Green = Available, Purple = VIP, Dark = Taken
   └── User clicks seats to select them
   └── Order summary updates with seat prices in real time
   └── User clicks Proceed to Payment

4. PRE-ORDER SNACKS (Optional)
   └── Snack selection popup appears automatically
   └── User selects snacks using + and - quantity buttons
   └── Running total updates in real time
   └── User clicks Confirm or Skip Snacks

5. CHECKOUT
   └── Reservation created with HELD status
   └── 15-minute countdown timer starts
   └── Booking summary shows seats and snacks with prices
   └── User enters card details via Stripe Elements
   └── User clicks Pay

6. PAYMENT CONFIRMATION
   └── Stripe processes payment
   └── Stripe webhook fires to Spring Boot backend
   └── Reservation status updated to CONFIRMED
   └── Unique booking reference generated (e.g. CV-2026-A3X9K)
   └── Confirmation email sent with booking details, snacks and reference code

7. MY TICKETS
   └── User views confirmed tickets
   └── Booking reference displayed on each confirmed ticket
   └── Snacks listed with names, quantities and prices
   └── Held tickets show countdown timer and Complete Payment button
   └── User can cancel tickets
```

---

## Admin Flow

```
1. LOGIN
   └── Admin logs in with ADMIN or SUPER_ADMIN account
   └── Admin link appears in navbar

2. MOVIES TAB
   └── View all movies
   └── Add new movie with title, genre, duration, cast, poster URL, rating, status
   └── Edit existing movies
   └── Delete movies

3. THEATERS TAB
   └── View all theaters
   └── Add new theater with name, number of rows and seats per row
   └── Seats are auto-generated on creation (rows x seats per row)

4. SHOWTIMES TAB
   └── View all showtimes
   └── Add new showtime by selecting movie, theater, start time and ticket price
   └── End time calculated automatically from movie duration
   └── Delete showtimes

5. SNACKS TAB
   └── View all snacks
   └── Add new snack with name, price, emoji, size (Small/Medium/Large), description
   └── Toggle availability on/off
   └── Edit and delete snacks

6. REPORTS TAB
   └── View total revenue, total bookings, bookings today, cancellation rate
   └── Most popular movie
   └── Revenue per movie breakdown
   └── Occupancy percentage per showtime with visual progress bars
   └── Recent 10 bookings with customer, movie, seats and total

7. USERS TAB
   └── View all registered users with name, email, role and join date
   └── Change user role between USER and ADMIN
   └── Delete users
   └── Super Admin account shows crown icon and is protected from editing or deletion
```

---

## Project Structure

```
movie-reservation/
├── backend/                  # Spring Boot application
│   ├── src/main/java/com/moviereservation/
│   │   ├── config/           # Security, Redis, CORS, DataInitializer
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Data transfer objects
│   │   ├── entity/           # JPA entities
│   │   ├── repository/       # Spring Data repositories
│   │   ├── security/         # JWT filter and authentication
│   │   └── service/          # Business logic
│   └── src/main/resources/
│       └── application.properties
├── frontend/                 # React application
│   └── src/
│       ├── components/       # Reusable components (Navbar, SeatMap)
│       ├── context/          # Auth context
│       └── pages/            # Page components
├── docker-compose.yml
└── README.md
```

---

## Student

**Name:** Iyenoma Osa
**Student ID:** io60
**Module:** CO3201 Computer Science Project
**University:** University of Leicester
**Academic Year:** 2025/2026
