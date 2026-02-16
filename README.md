# Traffic Controller System (Java 17)

A Spring Boot-based **Traffic Light Management System** designed for high-reliability intersections. This system manages complex traffic phases, prevents conflicting green lights through a safety-first engine, and provides a real-time REST API for monitoring and control.

## 🚦 Key Features

* **State Machine Engine:** Robust transition logic handling `GREEN` -> `YELLOW` -> `RED` cycles.
* **Safety Failsafe:** Real-time conflict detection that automatically locks the intersection to `RED` if dangerous phase combinations are detected.
* **Thread-Safe Architecture:** Optimized for concurrency using `volatile` flags and `Atomic` variables.
* **High-Performance Cache:** Immutable data structures with "Atomic Swap" reloading to prevent latency during database reads.
* **Dynamic API:** Control the intersection flow, update sequences, and refresh data without downtime.

---

## 🏗️ Architecture & Package Structure

The project uses the `com.natwest.tc` base package for a streamlined, clean development experience.



* **`com.natwest.tc.controller`**: REST API endpoints for external control.
* **`com.natwest.tc.service`**: Core business logic (Traffic Engine and Cache management).
* **`com.natwest.tc.entity`**: Database entities (Phases, Directions, Conflicts).
* **`com.natwest.tc.repository`**: Data Access Objects (Spring Data JPA).
* **`com.natwest.tc.model`**: Shared state models and Enums.

---

## 🚀 Getting Started

### Prerequisites
* **Java 17** (LTS)
* **Maven 3.8+**
* **H2 Database** (In-memory, default configuration)

### Installation & Run
1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/natwest/traffic-controller.git](https://github.com/natwest/traffic-controller.git)
    cd traffic-controller
    ```
2.  **Build the Project:**
    ```bash
    mvn clean install
    ```
3.  **Run the Application:**
    ```bash
    mvn spring-boot:run
    ```

---

## 📡 API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/traffic/state` | Fetch current light color and active directions. |
| `POST` | `/api/traffic/sequence` | Update the phase sequence (e.g., `[1, 2, 3]`). |
| `POST` | `/api/traffic/pause` | Emergency stop (Forces all lights to RED). |
| `POST` | `/api/traffic/refresh-cache` | Reload reference data from DB to memory. |
| `GET` | `/api/traffic/history` | View log of all state changes and safety events. |

---

## 🧪 Testing Strategy

### Unit Tests (JUnit 5 + Mockito)
Verifies individual logic blocks, focusing on state transitions and conflict detection.
* Run: `mvn test`

### BDD Tests (Cucumber)
Uses Gherkin to define human-readable traffic scenarios for stakeholder verification.



---

## 🛠️ Tech Stack
* **Spring Boot 3.x**
* **Java 17**
* **Lombok**
* **Spring Data JPA**
* **Cucumber & JUnit 5**

---

## 📜 License
Internal Use Only - NatWest Technology.
