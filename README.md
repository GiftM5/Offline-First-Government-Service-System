# Offline-First Distributed Government Service System

(Java + Spring Boot + ActiveMQ)

1️⃣ What is this project? (Plain English)

This project simulates how government offices in areas with unreliable internet can continue to provide services without stopping operations, and later synchronize data safely with a central government system.

Instead of assuming:

"The internet is always available"

The system assumes:

"The internet is unreliable — design for failure."

This is exactly how real distributed systems are built, especially in developing regions.

2️⃣ Real-World Problem You Are Solving

In Mozambique (and many similar countries):

Local government offices often:

Lose internet

Have unstable power

Operate in remote areas

Citizens still need:

IDs

Permits

Grants

Most systems:

Stop working when offline

Lose data

Require manual re-entry later

❌ Traditional (Bad) Approach
Local Office → Internet → Central Server
(no internet = no service)

✅ Your Approach (Correct)
Local Office → Local Storage → Message Queue → Central Server


This ensures:

No downtime

No data loss

Automatic recovery

3️⃣ High-Level Architecture (Distributed System)
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
 │  (Message Broker)  │
 │                     │
 │ - Stores messages   │
 │ - Handles retries   │
 │ - Decouples nodes   │
 └─────────┬───────────┘
           │
 ┌─────────▼───────────┐
 │  Central Gov Server │
 │  (Always Online)   │
 │                     │
 │ - Spring Boot       │
 │ - PostgreSQL DB     │
 │ - Message Consumer  │
 └─────────────────────┘

4️⃣ Why This Is a Distributed System

Your system has multiple independent components that:

Run separately

Communicate asynchronously

Can fail independently

Recover automatically

That is the definition of a distributed system.

You are mimicking:

Network failures

Partial outages

Eventual consistency

Loose coupling

5️⃣ Core Components (Detailed)
🔹 A. Local Office Service (Distributed Node)

This represents one physical government office.

Responsibilities

Accept applications from citizens

Work with or without internet

Store data locally

Send data asynchronously to central system

What runs here

Spring Boot (embedded web server)

Minimal HTML pages (for non-technical users)

H2 local database

ActiveMQ producer

Why this is realistic

In real life:

Each office has its own computer

Internet may be down

Work must continue

🔹 B. Minimal Web Interface (HTML)

This is not a frontend project.

Its purpose is:

"Let a non-developer see that the system works."

Pages

Application Form

Citizen ID

Service type

Submit button

Status Page

Pending applications

Sync status

Last successful sync

This mimics what a clerk would use.

🔹 C. Local Database (H2)
Why local storage is critical

If the system crashes or power is lost:

Data is still safe

No reliance on memory

Example record
Application:
- id
- citizenId
- serviceType
- status
- synced = false
- createdAt


This DB is authoritative until synced.

6️⃣ Where ActiveMQ Comes In (VERY IMPORTANT)

This is the heart of the distributed system.

❓ Why Not Just REST Calls?

If you do:

Local Office → REST → Central Server


Then:

If the server is down → request fails

Data is lost or must be retried manually

Tight coupling

This is NOT resilient.

✅ Why ActiveMQ Is the Correct Tool

ActiveMQ acts as a buffer and mediator between services.

It ensures:

Messages are stored

Messages are retried

Systems are decoupled

7️⃣ How ActiveMQ Works in Your Project
Step-by-Step Flow
1️⃣ Citizen submits application

Via HTML form

Saved in local DB

2️⃣ Local Office publishes a message
Queue: application.sync.queue
Message:
{
  applicationId: 1,
  citizenId: "MZ123456",
  serviceType: "NATIONAL_ID"
}


This happens even if:

Central server is down

Network is unstable

3️⃣ ActiveMQ stores the message

On disk

Safely

Until consumed

This is guaranteed delivery.

4️⃣ Central Server consumes message

Listens to the queue

Processes application

Stores in central DB

5️⃣ Confirmation

Central server sends acknowledgment

Local record marked synced = true

8️⃣ What Happens When Things Go Wrong (Key Distributed Behavior)
Scenario 1: Central Server is Down ❌

Local office still accepts applications

ActiveMQ queues messages

Nothing breaks

Scenario 2: Network Restored ✅

Messages are delivered automatically

No human intervention

Eventual consistency achieved

9️⃣ Distributed Systems Concepts You Demonstrate
Concept	How you show it
Offline-first	Local DB + UI works without internet
Asynchronous communication	ActiveMQ queues
Fault tolerance	Messages survive failures
Loose coupling	Producer doesn't know consumer
Eventual consistency	Sync happens later
Scalability	Add more local offices
🔟 How You Will Demonstrate This Working
Demo Script

Start Local Office

Stop Central Server

Submit applications via browser

Show local DB has data

Start Central Server

Show ActiveMQ delivering messages

Show central DB updated

This is very impressive, even without fancy UI.

1️⃣1️⃣ What This Project Is NOT

❌ Not a CRUD app
❌ Not a UI project
❌ Not data science
❌ Not over-engineered

It is a systems design & backend engineering project.
