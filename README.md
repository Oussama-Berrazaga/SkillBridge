# SkillBridge

SkillBridge is a Microservice Ecosystem for On-Demand Technical Services.

![Architecture Diagram](docs/architecture.drawio.svg)


🏗️ System Architecture
This project follows a microservices architecture to ensure high scalability and decoupling.

🧩 Microservices
- Gateway Service: Single entry point for all clients; handles routing and cross-cutting concerns.

- User Service (Identity): Manages user profiles, roles (Client/Technician), and authentication.

- Listing Service (Marketplace): Core business logic for creating postings, managing categories, and handling technician applications.

- Chat Service (Negotiation): Real-time WebSocket communication between users to negotiate service details.

- Booking Service (Commitment): Manages visit proposals, scheduling, and formal service contracts.

- Notification Service (Kafka): Event-driven service that consumes messages to send Emails/SMS/Push notifications asynchronously.

