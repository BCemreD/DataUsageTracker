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
