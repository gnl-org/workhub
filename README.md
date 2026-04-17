# Work Hub

**Work Hub** is a centralized platform designed to help teams manage tasks, projects, and collaboration more efficiently. It streamlines project management by providing a clear overview of ongoing work, assigning responsibilities, tracking progress, and generating reports—all in one place.

## 🚀 Key Features

### Core Management
- **Task Management:** Full CRUD operations with priority tracking, due dates, and soft-delete protection.
- **Project Tracking:** Monitor project health and deadlines in real-time through a centralized dashboard.
- **Collaboration:** Integrated member assignment and project-specific roles to streamline team communication.

### Insights & Communication
- **Notifications & Alerts:** Automated system to keep team members informed about approaching deadlines and project updates.
- **Reports & Analytics:** Advanced data aggregation providing insights on team productivity, task distribution, and project completion status.

## 🛠️ Technical Architecture (Infrastructure Phase)

The backend is built with **Spring Boot 4.0** and **Java 21**, following enterprise-grade design patterns.

### 🔐 Security & Access Control
* **Stateless Authentication:** Implemented via **Spring Security** and **JWT**, ensuring secure and scalable session management.
* **RBAC (Role-Based Access Control):** * **Global Roles:** `ADMIN`, `USER`.
    * **Project Roles:** `OWNER`, `MEMBER`.
* **Security Context Integration:** Automatic injection of user identity from JWT claims into the persistence layer for seamless auditing.

### 📜 Data Integrity & Auditing
* **Automated JPA Auditing:** All entities inherit from a `@MappedSuperclass` to automatically track `createdAt`, `updatedAt`, `createdBy`, and `lastModifiedBy`.
* **Soft Deletion:** Hibernate-level `@Where` filtering to protect business-critical data from accidental removal.
* **Activity & Audit Logging:** Dual-layer logging—**Activity Logs** for user-facing history and **Audit Logs** for system-level accountability.

### 🔌 API Infrastructure
* **Standardized Error Handling:** Global `@ControllerAdvice` providing a consistent JSON error contract across the entire API.
* **Performance Optimization:** Server-side **Pagination and Sorting** via Spring Data `Pageable` to handle high-volume data efficiently.
* **Auto-Documentation:** Interactive API explorer via **Swagger/OpenAPI 3.0**.

## 💻 Tech Stack
- **Backend:** Spring Boot 4, Spring Security, Spring Data JPA
- **Frontend:** React (State Management & Component-driven UI)
- **Database:** PostgreSQL (Relational schema with optimized FK relationships)
- **Utilities:** MapStruct, Lombok, Net-DataFaker (Seeding)
- **Documentation:** Swagger/OpenAPI 3.0
- **Infrastructure:** AWS (Hosting & Cloud persistence)

---
*Work Hub is designed for scalability and professional team collaboration.*