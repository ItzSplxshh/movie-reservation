# Project Log — Movie Reservation System  

---

## Week 1 — 29 September to 5 October 2025

**Activities:**
- Met with supervisor to discuss project ideas
- Decided on a Movie Reservation System as the project topic
- Began researching existing cinema reservation systems such as Cineworld and Vue to understand industry standards
- Read about key capabilities of modern enterprise systems including real-time seat availability, integrated payments and analytics
- Created initial project plan and drafted aims and objectives

**Reflection:**
This week was primarily about scoping the project. I wanted to make sure the system was complex enough for a final year project while still being achievable. Reading about real cinema systems helped me understand what features to prioritise. I decided early on to use Spring Boot and MySQL as the core backend technologies as they were taught during my degree, which would let me focus on complexity rather than learning entirely new tools.

---

## Week 2 — 6 to 12 October 2025

**Activities:**
- Researched Spring Boot architecture and microservices patterns
- Investigated Stripe and PayPal as payment gateway options
- Decided on Stripe due to better developer experience and webhook support
- Began researching Redis for caching and React for the frontend
- Started drafting the software architecture diagram

**Reflection:**
Choosing between Stripe and PayPal was an important early decision. Stripe's webhook system for confirming payments asynchronously was a key factor — it handles the case where a user pays but closes the browser before the confirmation screen appears. I also decided on a monolithic architecture rather than microservices, as the microservices approach in my interim report was ambitious and I felt a well-structured monolith would be more achievable and still demonstrate enterprise-level design.

---

## Week 3 — 13 to 19 October 2025

**Activities:**
- Designed the database schema — entities: User, Movie, Theater, Seat, Showtime, Reservation
- Researched JWT authentication and Spring Security
- Looked at existing database schema designs for cinema systems
- Found a reference schema from Redgate which informed my initial design
- Began writing the interim report

**Reflection:**
Database design was one of the most important early decisions. I spent significant time making sure the relationships between entities were correct — particularly the many-to-many relationship between reservations and seats. Getting this right early saved a lot of refactoring later. I also decided to use an enum for reservation status (PENDING, CONFIRMED, CANCELLED, REFUNDED) which made state management much cleaner.

---

## Week 4 — 20 to 26 October 2025

**Activities:**
- Continued writing interim report
- Researched WCAG 2.1 accessibility guidelines
- Researched role-based access control patterns in Spring Security
- Finalised use case diagram showing Customer, Administrator, Theatre Manager and System actors
- Planned sprint structure for semester 2

**Reflection:**
The use case diagram was very useful for clarifying the scope of the project. Identifying the four actors — Customer, Administrator, Theatre Manager and System — helped me understand what endpoints and features were needed. The System actor in particular highlighted the need for automated background tasks like cleaning expired reservations, which I implemented later as a scheduled job.

---

## Week 5 — 27 October to 2 November 2025

**Activities:**
- Submitted interim report
- Set up Spring Boot project structure with Maven
- Added initial dependencies: Spring Web, Spring Data JPA, Spring Security, MySQL Connector
- Created JPA entities for all core domain objects

**Reflection:**
Getting the project structure right from the start was important. I followed a standard layered architecture with controllers, services and repositories. Using Lombok annotations reduced boilerplate code significantly. I also made the decision to use `spring.jpa.hibernate.ddl-auto=update` during development so the schema would update automatically as I added fields.

---

## Week 6 — 3 to 9 November 2025

**Activities:**
- Implemented JWT authentication with register and login endpoints
- Added Spring Security configuration with CORS settings
- Implemented role-based access control (USER and ADMIN roles)
- Added JPA repositories for all entities
- Tested authentication endpoints with Postman

**Reflection:**
Spring Security had a steeper learning curve than expected. The filter chain configuration in particular required careful reading of the documentation. I decided to use stateless sessions with JWT rather than session-based authentication, which is more appropriate for a REST API and scales better. The `@PreAuthorize` annotation made securing individual endpoints straightforward.

---

## Week 7 — 10 to 16 November 2025

**Activities:**
- Added REST controllers for movies, showtimes and seats
- Implemented theater service with automatic seat generation on creation
- Added reservation system with seat availability checking
- Implemented VIP seat surcharge logic

**Reflection:**
Auto-generating seats when a theater is created was a good design decision — it means admins don't have to manually add every seat. I used row labels (A, B, C...) and seat numbers to create a realistic seat map. The VIP surcharge added pricing complexity that made the system more realistic.

---

## Week 8 — 17 to 23 November 2025

**Activities:**
- Integrated Stripe PaymentIntents with webhook confirmation
- Set up Stripe CLI for local webhook testing
- Implemented secure credential management using gitignored properties file
- Added payment status flow: PENDING → CONFIRMED on successful payment

**Reflection:**
Stripe integration was one of the most technically challenging parts of the project. The webhook pattern — where Stripe calls back to the server to confirm payment — required careful handling. I had to ensure the webhook secret was kept out of version control, which led me to implement the `application-local.properties` pattern where sensitive credentials are stored in a gitignored file with placeholder values in the committed file.

---

## Week 9 — 24 to 30 November 2025

**Activities:**
- Set up React frontend with routing, authentication context and Axios client
- Added Navbar and MovieCard components with cinema dark theme
- Implemented Home and Movies browse pages
- Added Login and Register pages

**Reflection:**
React Router v6 had some differences from v5 that required adjustment. The authentication context pattern using React Context API worked well for sharing the logged-in user state across components. I chose a dark cinema aesthetic for the UI which felt appropriate for the domain.

---

## Week 10 — 1 to 7 December 2025

**Activities:**
- Added seat selection page with interactive visual seat map
- Implemented Stripe checkout page with payment form
- Added My Tickets page for viewing reservations
- Built Admin panel for managing movies, theaters and showtimes

**Reflection:**
The seat map was one of the most satisfying features to implement. Colour coding seats by availability (available, selected, taken, VIP) gives users immediate visual feedback. Integrating Stripe Elements into the checkout page required careful configuration of the appearance options to match the dark cinema theme.

---

## Week 11 — Christmas and January Break (December 2025 — January 2026)

**Activities:**
- Reviewed progress against interim report requirements
- Planned semester 2 features
- Researched seat holding mechanisms used by real cinema systems
- Read about Redis distributed locking for seat availability
- Decided to implement a simpler scheduled expiry approach rather than Redis for the seat hold

**Reflection:**
During the break I reviewed my interim report against what I had built. Most of the core features were in place but several advanced features still needed implementing. I decided to prioritise user-facing features over infrastructure improvements like Redis, as these would better demonstrate the system's functionality. The seat holding decision was particularly important — real systems like Ticketmaster hold seats for 10-15 minutes, so I planned a similar 15-minute mechanism.

---

## Week 12 — 2 to 8 February 2026

**Activities:**
- Resumed active development after break
- Set up GitHub repository with main and main-edit branches
- Made first series of commits establishing the full codebase on version control
- Reviewed all existing code and planned remaining features

**Reflection:**
Setting up proper branching strategy was important — using main-edit for active development and main for stable releases mirrors professional practice. All commits from this point were made with detailed messages to clearly document what was implemented and why.

---

## Week 13 — 9 to 15 February 2026

**Activities:**
- Added complete React frontend with all pages
- Implemented interactive seat map with real-time availability
- Completed admin panel with full CRUD for movies, theaters and showtimes
- Polished UI styling across all pages

**Reflection:**
This was a very productive week. Having the backend mostly complete allowed me to focus on the frontend. The admin panel in particular required significant work — the modal forms for adding and editing movies needed careful state management. I used a single `form` state object with spread operator updates which kept the code clean.

---

## Week 14 — 16 to 22 February 2026 to March 2026

**Activities:**
- Testing of all existing features end to end
- Fixed various bugs discovered during testing
- Reviewed against functional requirements from interim report
- Planned next sprint: seat hold mechanism, cancellation, notifications

**Reflection:**
End to end testing revealed several edge cases I had not considered — for example what happens when a user tries to pay for a reservation that has already been cancelled. Adding proper error handling and user-friendly messages improved the robustness of the system significantly.

---

## Week 15 — 1 April 2026

**Activities:**
- Completed Stripe webhook integration for payment confirmation
- Fixed race condition where payment could succeed without reservation being confirmed
- Thoroughly tested payment flow end to end with Stripe test cards

**Reflection:**
The webhook integration was revisited to fix an edge case where the reservation status was not being updated correctly. Using the Stripe CLI to forward webhooks to localhost was essential for testing. This commit represented a significant milestone — the core booking flow was now fully functional end to end.

---

## Week 16 — 14 to 20 April 2026

**Activities:**
- Implemented seat hold mechanism with 15-minute expiry (HELD status)
- Added countdown timer on checkout page with colour-coded warnings
- Created scheduled background job to auto-expire held reservations
- Updated MySQL enum to include HELD status
- Updated payment service to accept HELD reservations

**Reflection:**
This was one of the more complex features to implement as it touched multiple layers — the entity, repository, service, controller and frontend all needed changes. The timezone issue (BST vs UTC) was an interesting bug where the timer showed 75 minutes instead of 15. The fix was to store times in UTC on the backend using `ZoneOffset.UTC` and append 'Z' on the frontend when parsing. This taught me to always store timestamps in UTC and convert to local time only for display.

---

## Week 17 — 21 to 22 April 2026

**Activities:**
- Added movie search by title and genre (frontend filtering)
- Implemented password reset via email using Gmail SMTP
- Created ForgotPasswordPage and ResetPasswordPage
- Added reset token with 1-hour expiry to User entity
- Added "Forgot password?" link to login page
- Added booking confirmation email sent on successful payment
- Added cancellation email sent when confirmed ticket is cancelled
- Fixed double PaymentIntent bug by returning existing intent if one already exists

**Reflection:**
Password reset required careful security consideration — the reset token must be random, single-use and time-limited. Using `UUID.randomUUID()` for token generation and storing an expiry timestamp in the database met these requirements. The Gmail App Password setup was straightforward but required enabling 2-factor authentication first. Storing email credentials in the gitignored `application-local.properties` file maintained security best practices. Email notifications replaced an earlier plan to use Twilio SMS, which was blocked by UK regulatory bundle requirements.

---

## Week 17 — 22 to 23 April 2026

**Activities:**
- Added admin reports tab with occupancy per showtime, revenue per movie, recent bookings and summary cards
- Added admin user management tab (view all users, change roles, delete users)
- Fixed createdAt field using @PrePersist to correctly populate join date on registration
- Added accessibility features: high contrast mode (bright white WCAG-compliant theme), font size controls, skip link, focus indicators, ARIA labels
- Fixed navbar remaining dark and unreadable in high contrast mode when scrolled
- Added countdown timer to held tickets on My Tickets page with colour-coded warnings
- Added Complete Payment button for HELD status tickets on My Tickets page
- Added showtime filter by date dropdown on movie detail page
- Fixed cast column data truncation error by altering column type to TEXT
- Fixed NullPointerException in ShowtimeService for movies without duration set
- Mirrored repository to university GitLab

**Reflection:**
The admin reports feature required complex Java stream operations to aggregate data — grouping reservations by movie to calculate revenue per film and calculating occupancy percentages per showtime. The accessibility work was guided by WCAG 2.1 guidelines. The original high contrast mode used a dark-on-dark approach which did not actually help users with visual impairments — switching to a bright white theme with black text aligns with WCAG 2.1 and how operating systems like Windows implement high contrast. The navbar was particularly tricky as it uses a hardcoded RGBA background with backdrop blur which required explicit CSS overrides. The countdown timer on My Tickets gives users urgency awareness without requiring them to navigate to checkout first. One known limitation identified during this sprint is that Stripe Elements does not fully respond to the high contrast CSS as it sandboxes its iframe for security — the stripeAppearance object was used to partially address this but complete theming is not possible, which will be noted in the dissertation as a known limitation.

---

## Week 17 — 23 April 2026 (Final touches)

**Activities:**
- Added Super Admin role with protected account that cannot be edited or deleted by other admins
- Added DataInitializer to automatically create Super Admin account on startup
- Added unique booking reference code generated on payment confirmation (e.g. CV-2026-A3X9K)
- Added booking reference to confirmation email and My Tickets page
- Implemented Redis caching for seat availability with automatic cache eviction on booking and cancellation
- Fixed seat map showing booked seats disappearing instead of greying out
- Added full snack pre-order system with admin management, size options and snack selection popup
- Included snack costs in Stripe payment total and booking confirmation email
- Added Docker containerisation with multi-stage Dockerfiles and docker-compose
- Added account settings page allowing users to update their name and change their password
- Sent confirmation email when password is changed
- Updated README with full database schema, API endpoints, user flow and admin flow
- Merged main-edit branch into main

**Reflection:**
This was the most productive single day of the project. Starting at 7am and finishing at noon, all remaining planned features were implemented along with several additional features identified during development. The snack pre-order system was the most substantial addition, touching every layer of the application from the database entity through to the confirmation email. Redis caching required moving the cache annotation from the controller to the service layer to avoid Spring Security proxy conflicts — an important lesson about how Spring AOP works. Docker containerisation demonstrated that the application is production-ready and deployable in any environment. The DataInitializer ensures a Super Admin account always exists on a fresh database, which is particularly important for the Docker deployment where the database starts empty.

---

## Week 17 — 24 April 2026

**Activities:**
- Added Javadoc comments to ReservationController, JwtAuthFilter, Reservation, PaymentService, SeatService and BookingConfirmationService
- Added JUnit unit test suite with 33 passing tests across 6 test classes
- Added k6 load testing script — all thresholds passed under 50 concurrent users
- Fixed VIP surcharge display in checkout booking summary

**Reflection:**
The k6 load testing produced excellent results with an average response time of 5.85ms under 50 concurrent users and a 95th percentile of 8.53ms, well within the 500ms threshold. The JUnit test suite covers the core business logic across all major service and controller classes.

---

## Summary of Key Technical Decisions

| Decision | Chosen | Alternative Considered | Reason |
|----------|--------|----------------------|--------|
| Backend framework | Spring Boot | Node.js, Django | Type safety, transaction management, university familiarity |
| Database | MySQL | PostgreSQL, MongoDB | Relational data, ACID compliance, familiarity |
| Frontend | React | Vue, Angular | Component architecture, large ecosystem |
| Payment gateway | Stripe | PayPal | Better developer experience, webhook support |
| Authentication | JWT (stateless) | Session-based | Scalable, appropriate for REST API |
| Email service | Gmail SMTP | SendGrid, Mailgun | Free, no additional accounts needed |
| Seat hold | Scheduled expiry | Redis distributed lock | Simpler, sufficient for requirements |
| Architecture | Monolith | Microservices | More achievable, still enterprise-grade design |

---

## Challenges and Solutions

| Challenge | Solution |
|-----------|----------|
| Stripe webhook not confirming reservation | Fixed double PaymentIntent creation by returning existing intent |
| Countdown timer showing 75 minutes instead of 15 | Backend storing BST instead of UTC — fixed with ZoneOffset.UTC |
| Frontend showing stale reservation status | Used window.location.href instead of navigate() to force full reload |
| GitLab authentication failing | Required Personal Access Token instead of password |
| Skip link not appearing on first Tab press | Moved skip link outside AppRoutes to be first DOM element |
| Twilio SMS implementation blocked by regulatory requirements | Twilio requires A2P 10DLC registration for US numbers and a regulatory bundle for UK numbers, both of which require business verification. Switched to email notifications via Gmail SMTP instead, which achieved the same goal of notifying users about bookings without the regulatory overhead |
| Stripe Elements not responding to high contrast CSS | Used stripeAppearance object to detect high contrast class and switch to flat theme — noted as known limitation as Stripe sandboxes its iframe for security |

---

## Features Implemented

- [x] User registration and login with JWT authentication
- [x] Role-based access control (USER and ADMIN)
- [x] Movie management (admin CRUD)
- [x] Theatre and showtime management
- [x] Visual seat map with colour-coded availability
- [x] Reservation system with seat availability checking
- [x] Seat hold mechanism with 15-minute expiry
- [x] Stripe payment integration with webhook confirmation
- [x] Booking confirmation email
- [x] Cancellation email
- [x] Password reset via email
- [x] Movie search by title and genre
- [x] Admin reports (occupancy, revenue, recent bookings)
- [x] Accessibility features (WCAG 2.1)
- [x] GitLab version control with meaningful commit history
- [x] Admin user management (view, change role, delete)
- [x] Showtime filter by date on movie detail page
- [x] Held ticket countdown timer on My Tickets page

---

## Skills Gained

- Spring Boot and Java enterprise development
- React frontend development with hooks and context
- Payment gateway integration (Stripe)
- JWT authentication and Spring Security
- Database design with JPA and Hibernate
- Git version control with branching strategies
- Email service integration (SMTP)
- Accessibility implementation (WCAG 2.1)
- Agile/Scrum self-management as a solo developer
