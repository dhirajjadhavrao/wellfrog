# 🐸 Wellfrog — Backend API

> **Personal Daily Life & Activity Tracker Backend**  
> Built with **Spring Boot 3.3.5 (Java 21)**, **Spring Data JPA**, **Spring Security (JWT)**, and persistent **H2 / PostgreSQL**.

---

### **Core Tracking Domains**
1. **💰 Finance (Daily Spend):** Track online vs. offline spend, categories, receipts.
2. **🏦 Loans & EMIs:** Track active loans, monthly EMI liabilities, due dates, and payment status (`PAID`, `PENDING`, `FAILED`).
3. **🏋️ Workouts:** Track workout sessions, routines, durations, and exercise notes.
4. **💼 Office Work:** Log daily office hours and deliverables/tasks.
5. **🎯 Naukri / Career Tracker:** Manage job applications, stages (`APPLIED`, `SHORTLISTED`, `INTERVIEW`, `OFFER`), and follow-ups.
6. **➕ Dynamic Activities:** Full support for user-created custom activities and nested sub-activities.

---

### **Authentication & Multi-User**
* **Google Identity / Gmail Sign-in:** Validates Google ID tokens via `POST /api/auth/google`.
* **Dev Fast-Login:** Instant 1-click test login via `POST /api/auth/dev-login`.
* **Data Isolation:** Every record is strictly scoped by `user_id`.

---

### **How to Run**

```bash
# Build & package
mvn clean package -DskipTests

# Run application
mvn spring-boot:run
```
* **API Server:** `http://localhost:8080`
* **H2 Database Console:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/wellfrogdb`, user: `sa`, password: `[blank]`)
