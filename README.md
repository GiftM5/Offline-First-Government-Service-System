# Offline-First Distributed Government Service System

**Java + Spring Boot + ActiveMQ**

---

## 1. What is this project? (Plain English)

This project simulates how government offices in areas with unreliable internet can continue to provide services without stopping operations, and later synchronize data safely with a central government system.

Instead of assuming:

> "The internet is always available."

The system assumes:

> "The internet is unreliable. Design for failure."

This is exactly how real distributed systems are designed, especially in developing regions.

---

## 2. Real-World Problem You Are Solving

In Mozambique and many similar countries, local government offices may:

* Lose internet connectivity
* Have unstable power
* Operate in remote areas

However, citizens still need access to services such as:

* National IDs
* Permits
* Grants
* Other government services

Most traditional systems stop working when they go offline, potentially resulting in:

* Lost data
* Interrupted services
* Manual data re-entry
* Delays in processing applications

### Traditional Approach ❌

```text
Local Office → Internet → Central Server

(No internet = No service)
```

### Your Approach ✅

```text
Local Office → Local Storage → Message Queue → Central Server
```

This approach provides:

* No interruption to local operations
* Reduced risk of data loss
* Automatic recovery
* Automatic synchronization when connectivity returns

---

## 3. High-Level Architecture

```text
 ┌─────────────────────┐
 │  Local Office Node  │
 │  (Offline-First)    │
 │                     │
 │  - Spring Boot      │
 │  - Minimal HTML UI  │
 │  - Local H2 DB      │
 │  - ActiveMQ Client  │
 └─────────┬───────────┘
           │
     (Async Messages)
           │
 ┌─────────▼───────────┐
 │     ActiveMQ        │
 │   (Message Broker)  │
 │                     │
 │  - Stores messages  │
 │  - Handles retries  │
 │  - Decouples nodes  │
 └─────────┬───────────┘
           │
 ┌─────────▼───────────┐
 │  Central Gov Server │
 │    (Always Online)  │
 │                     │
 │  - Spring Boot      │
 │  - PostgreSQL DB    │
 │  - Message Consumer │
 └─────────────────────┘
```

---

## 4. Why This Is a Distributed System

The system consists of multiple independent components that:

* Run separately
* Communicate asynchronously
* Can fail independently
* Recover automatically

This demonstrates the principles of a distributed system.

The project simulates real distributed-system challenges such as:

* Network failures
* Partial outages
* Eventual consistency
* Loose coupling
* Fault tolerance

---

## 5. Core Components

### A. Local Office Service (Distributed Node)

The Local Office Service represents a physical government office.

#### Responsibilities

It must:

* Accept applications from citizens
* Continue operating with or without internet connectivity
* Store data locally
* Send data asynchronously to the central government system
* Track whether applications have been synchronized

#### What runs here?

* Spring Boot
* Embedded web server
* Minimal HTML interface
* H2 local database
* ActiveMQ producer

#### Why is this realistic?

In a real-world scenario:

* Each government office may have its own computer or local server
* Internet connectivity may be unreliable
* Employees still need to process citizens' applications
* Applications should not be lost simply because the network is unavailable

---

### B. Minimal Web Interface (HTML)

This is **not a frontend-focused project**.

The purpose of the interface is simply:

> "Let a non-developer see that the system works."

#### Pages

**Application Form**

* Citizen ID
* Service type
* Submit button

**Status Page**

* Pending applications
* Synchronization status
* Last successful synchronization

This represents the interface that a government clerk could use.

---

### C. Local Database (H2)

#### Why is local storage critical?

The local database ensures that application data remains available even if:

* The internet goes down
* The central server is unavailable
* The application temporarily loses connectivity
* The system needs to retry synchronization

Instead of keeping important data only in memory, it is persisted locally.

#### Example Record

```text
Application:
- id
- citizenId
- serviceType
- status
- synced = false
- createdAt
```

The local database is authoritative for the application until the application has been successfully synchronized with the central system.

---

## 6. Where ActiveMQ Comes In (VERY IMPORTANT)

ActiveMQ is the heart of the distributed communication layer.

### Why not just use REST calls?

A simple architecture could be:

```text
Local Office → REST → Central Server
```

However, this introduces problems.

If the central server is unavailable:

* The request fails
* The local system needs to handle retries
* The producer becomes dependent on the availability of the consumer
* Additional retry logic is required
* Services become more tightly coupled

This is less resilient.

### Why ActiveMQ?

ActiveMQ acts as a **message broker** between the local office and the central server.

Instead of requiring the local office and central server to communicate directly, the local office publishes a message to a queue.

ActiveMQ can then hold the message until the central server is available to process it.

This provides:

* Message persistence
* Asynchronous communication
* Decoupling between services
* Retry and redelivery capabilities
* Better fault tolerance

---

## 7. How ActiveMQ Works in Your Project

### Step-by-Step Flow

#### 1. Citizen submits an application

The citizen's application is submitted through the HTML form.

The application is first saved in the local H2 database.

```text
Citizen
   ↓
HTML Form
   ↓
Spring Boot
   ↓
Local H2 Database
```

---

#### 2. Local Office publishes a message

The Local Office Service publishes an event/message to an ActiveMQ queue.

**Queue:**

```text
application.sync.queue
```

**Example message:**

```json
{
  "applicationId": 1,
  "citizenId": "MZ123456",
  "serviceType": "NATIONAL_ID"
}
```

The local application does not need the central server to be immediately available in order to continue processing the application.

---

#### 3. ActiveMQ stores the message

ActiveMQ acts as a buffer between the Local Office and Central Server.

The message can remain in the queue until the consumer is available.

Conceptually:

```text
Local Office
     ↓
ActiveMQ
     ↓
[Message waiting]
     ↓
Central Server becomes available
     ↓
Message delivered
```

---

#### 4. Central Server consumes the message

The Central Government Server listens to the queue.

When a message becomes available, the server:

1. Receives the message
2. Validates the application
3. Processes it
4. Saves it to the central PostgreSQL database

---

#### 5. Confirmation

After successful processing, the message is acknowledged.

The local application can then update its local record:

```text
synced = true
```

This allows the local office to know that the application has successfully reached the central system.

---

## 8. What Happens When Things Go Wrong?

This is one of the most important parts of the project because it demonstrates **distributed-system behavior**.

### Scenario 1: Central Server Is Down ❌

The local office can continue accepting applications.

```text
Citizen
   ↓
Local Office
   ↓
Local H2 Database
   ↓
ActiveMQ
   ↓
[Message waiting]
```

The local office does not have to stop operating just because the central server is temporarily unavailable.

---

### Scenario 2: Network Connectivity Is Restored ✅

Once communication between the systems is restored, queued messages can be delivered to the central server.

```text
ActiveMQ
   ↓
Central Server
   ↓
PostgreSQL
```

The data eventually becomes consistent between the local and central systems.

This demonstrates **eventual consistency**.

---

## 9. Distributed Systems Concepts You Demonstrate

| Concept                        | How Your Project Demonstrates It                                             |
| ------------------------------ | ---------------------------------------------------------------------------- |
| **Offline-first**              | Local database and UI continue working without relying on the central server |
| **Asynchronous communication** | ActiveMQ queues messages between services                                    |
| **Fault tolerance**            | Applications can survive temporary service/network failures                  |
| **Loose coupling**             | The producer does not need to directly call the consumer                     |
| **Eventual consistency**       | Local and central databases become synchronized later                        |
| **Message persistence**        | Messages can remain queued until they can be processed                       |
| **Scalability**                | Additional local government offices can be added                             |
| **Independent failure**        | A local office, network, broker, or central server can fail independently    |
| **Recovery**                   | Systems can resume processing after failures                                 |

---

## 10. How You Will Demonstrate This Working

A strong demonstration would intentionally create a failure.

### Demo Script

#### Step 1: Start the Local Office

Start the Spring Boot Local Office Service.

Open the application in a browser.

---

#### Step 2: Stop the Central Server

Stop the Central Government Server.

The central system is now unavailable.

---

#### Step 3: Submit Applications

Use the browser to submit several government service applications.

For example:

```text
Citizen ID: MZ123456
Service: NATIONAL_ID
```

The applications should still be accepted.

---

#### Step 4: Show the Local Database

Demonstrate that the applications have been saved locally.

For example:

```text
Application ID: 1
Citizen ID: MZ123456
Service: NATIONAL_ID
Synced: false
```

This proves that the local office can continue operating while the central system is unavailable.

---

#### Step 5: Start the Central Server

Start the Central Government Server again.

---

#### Step 6: Show ActiveMQ Processing the Messages

The queued messages should be consumed by the central server.

```text
ActiveMQ
   ↓
Central Server
   ↓
PostgreSQL
```

---

#### Step 7: Show the Central Database

The applications should now appear in the central PostgreSQL database.

The local records can then be marked:

```text
synced = true
```

This demonstrates:

**Offline operation → Local persistence → Asynchronous messaging → Recovery → Synchronization**

---

## 11. What This Project Is NOT

This project is:

❌ Not just a CRUD application
❌ Not primarily a UI project
❌ Not a data-science project
❌ Not an unnecessarily over-engineered application

Instead, it is primarily a:

> **Distributed systems and backend engineering project.**

The main focus is on designing a system that continues to function when parts of the system fail.

---

## 12. The Main Idea in One Sentence

> **Build a government service system that assumes the network will fail, allows local offices to continue working, stores data safely, and synchronizes with the central government system when connectivity is restored.**

---

## 13. Technologies

| Technology      | Purpose                                       |
| --------------- | --------------------------------------------- |
| **Java**        | Primary programming language                  |
| **Spring Boot** | Backend services and REST APIs                |
| **H2**          | Local/offline database                        |
| **PostgreSQL**  | Central government database                   |
| **ActiveMQ**    | Message broker and asynchronous communication |
| **HTML/CSS**    | Minimal user interface                        |
| **Maven**       | Build and dependency management               |
| **Git/GitHub**  | Version control                               |
| **Docker**      | Optional containerization                     |

---

## 14. The Architecture in Simple Terms

Think of the system like this:

```text
                 GOVERNMENT SYSTEM

     ┌──────────────────────────────┐
     │       LOCAL OFFICE           │
     │                              │
     │  Clerk submits application   │
     │             ↓                │
     │        Spring Boot           │
     │             ↓                │
     │          H2 DB               │
     │             ↓                │
     │       ActiveMQ Producer      │
     └──────────────┬───────────────┘
                    │
                    │ Message
                    ▼
           ┌─────────────────┐
           │     ActiveMQ    │
           │                 │
           │  Message Queue  │
           └────────┬────────┘
                    │
                    │ Message
                    ▼
     ┌──────────────────────────────┐
     │      CENTRAL SERVER          │
     │                              │
     │       Spring Boot            │
     │             ↓                │
     │       Message Consumer       │
     │             ↓                │
     │       PostgreSQL             │
     └──────────────────────────────┘
```

The important design principle is:

> **The local office should not depend on the central server being available in order to continue serving citizens.**
