from fastapi import FastAPI, Body
from sqlalchemy import create_engine, Column, Integer, String, Float, Date, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import date, datetime
import uvicorn

# 1. Docker PostgreSQL Connection
DATABASE_URL = "postgresql://postgres:data1234@localhost:5432/postgres"

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# 2. DB Table
class AppUsage(Base):
    __tablename__ = "app_usage_history"
    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String)
    package_name = Column(String)
    wifi_mb = Column(Float)
    mobile_mb = Column(Float)
    log_date = Column(Date, default=date.today)
    created_at = Column(DateTime, default=datetime.now)

app = FastAPI()

# 3. Android Data
@app.post("/api/usage")
async def receive_usage(data: dict = Body(...)):
    db = SessionLocal()
    try:
        device_id = data.get("deviceId")
        usages = data.get("usages", [])

        for item in usages:
            new_record = AppUsage(
                device_id=device_id,
                package_name=item.get("packageName"),
                wifi_mb=item.get("wifiMB"),
                mobile_mb=item.get("mobileMB")
            )
            db.add(new_record)
        
        db.commit()
        return {"status": "success", "message": f"{len(usages)} kayıt Docker'a yazıldı!"}
    except Exception as e:
        db.rollback()
        return {"status": "error", "message": str(e)}
    finally:
        db.close()

# 4. List all data
@app.get("/api/data")
async def get_data():
    db = SessionLocal()
    try:
        records = db.query(AppUsage).all()
        return records
    finally:
        db.close()

# 5. Start app or create table (always on bottom)
if __name__ == "__main__":
    
    Base.metadata.create_all(bind=engine)
    print("🚀 Table checked/created. Starging at server port 8000...")
    uvicorn.run(app, host="0.0.0.0", port=8000)