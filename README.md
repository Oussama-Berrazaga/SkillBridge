# SkillBridge

SkillBridge is a Microservice Ecosystem for On-Demand Technical Services.

![Architecture Diagram](docs/architecture.drawio.svg)


🏗️ System Architecture
The platform is built on a Cloud-Native Microservices Architecture using Spring Cloud. Each service is independently deployable, possesses its own database (Database-per-Service pattern), and communicates via REST or asynchronous events.

🛠️ Infrastructure Services
- Config Server: Centralized configuration management using a Git-based repository. Provides environment-specific settings for all services.

- Discovery Service (Eureka): Service Registry that allows microservices to find and communicate with each other dynamically without hardcoded IPs.

- API Gateway: The single entry point for the system. Handles request routing, security, and rate limiting.

💼 Business Services
- User Service (Identity): Manages authentication, authorization, and user profiles (Client/Technician).

- Listing Service (Marketplace): Handles the creation of job posts, hierarchical categories (Sub-categories), and the application/bid process.

- Chat Service: Facilitates real-time negotiation between Clients and Technicians once an application is accepted.

- Booking Service: Manages the "Service Contract"—handling visit proposals, scheduling, and intervention tracking.

- Payment Service: Securely processes "Visit Fees" and job payments (Integration with Stripe).

- Notification Service: An event-driven service consuming Kafka topics to send emails, SMS, and push notifications asynchronously.
