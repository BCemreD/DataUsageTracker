# 📊 DataUsageTracker

DataUsageTracker is an end-to-end data monitoring solution. It tracks app-specific data usage (WiFi and Mobile) on Android devices, stores it locally, and synchronizes the data with a **PostgreSQL** database running in a **Docker** container via a **Python FastAPI** backend.



## 🚀 Key Features
* **Android (Java):** Leverages `UsageStatsManager` to collect precise app usage statistics.
* **Local Storage:** Implements **Room Database** for offline data persistence and caching.
* **Backend (Python):** A high-performance **FastAPI** server to process and route incoming data.
* **Containerization:** Fully containerized **PostgreSQL** database for easy deployment and scalability.
* **Automation:** Integrated **WorkManager** to trigger background synchronization every 15 minutes.

## 📁 Project Structure
```text
DataUsageTracker/
├── android/          # Android Studio Project (Java, Room DB, Retrofit)
├── backend/          # Python Backend (FastAPI, SQLAlchemy, Uvicorn)
└── README.md         # Project Documentation
```
## 🛠️ Getting Started
### 1. Database Setup (Docker)
Pull and run the PostgreSQL image:

``` Bash

docker run --name aura-postgres -e POSTGRES_PASSWORD=data1234 -p 5432:5432 -d postgres
```
### 2. Backend Setup
Navigate to the backend directory, install dependencies, and start the server:

``` Bash

cd backend
pip install -r requirements.txt
python main.py
```
### 3. Android App
Open the /android folder in Android Studio.

Update the BASE_URL in RetrofitClient with your local IP address (use 10.0.2.2 for the Android Emulator).

Build and run the app on your device or emulator.

## 📡 API Endpoints
POST /api/usage: Receives usage data from the Android client and saves it to Docker.

GET /api/data: Returns a JSON list of all synchronized usage records.

## 🛠 Technologies Used
- Mobile: Java, Room DB, Retrofit, WorkManager, MPAndroidChart.
- Backend: Python, FastAPI, SQLAlchemy.
- DevOps: Docker, PostgreSQL.
