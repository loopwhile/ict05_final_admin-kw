# uvicorn app:app --host 0.0.0.0 --port 8000 --reload

from fastapi import FastAPI, Response, HTTPException
from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional
import logging
from component import kpi_analytics, order_analytics, time_analytics, material_analytics

app = FastAPI(title="PDF Generation Service")
logger = logging.getLogger("orders-pdf")

# ---------- KPI ----------

class KpiRow(BaseModel):
    storeName: Optional[str] = None
    sales: Optional[float] = None
    transaction: Optional[int] = None
    upt: Optional[float] = None
    ads: Optional[float] = None
    aur: Optional[float] = None
    compMoM: Optional[float] = None
    compYoY: Optional[float] = None
    date: Optional[str] = None
    ratioVisit: Optional[float] = None
    ratioTakeout: Optional[float] = None
    ratioDelivery: Optional[float] = None

class KpiPayload(BaseModel):
    criteria: Dict[str, Any] = Field(default_factory=dict)
    data: List[KpiRow] = Field(default_factory=list)

@app.post("/pdf/kpi-report", summary="KPI 분석 리포트 PDF 생성")

def create_kpi_report(payload: KpiPayload):
    pdf_bytes = kpi_analytics.generate_kpi_pdf(payload.dict())
    if not pdf_bytes:
        raise HTTPException(status_code=500, detail="Empty KPI PDF generated")
    return Response(content=pdf_bytes, media_type="application/pdf")

# ---------- Orders ----------

class OrdersRow(BaseModel):
    date: Optional[str] = None
    orderDate: Optional[str] = None
    storeName: Optional[str] = None
    category: Optional[str] = None
    menu: Optional[str] = None
    menuCount: Optional[int] = 0
    menuSales: Optional[float] = 0
    orderCount: Optional[int] = 0
    orderSales: Optional[float] = 0
    orderType: Optional[str] = None

class OrdersPayload(BaseModel):
    criteria: Dict[str, Any] = Field(default_factory=dict)
    data: List[OrdersRow] = Field(default_factory=list)

@app.post("/pdf/orders", summary="주문 분석 리포트 PDF 생성")

def create_orders_report(payload: OrdersPayload):
    pdf_bytes = order_analytics.generate_orders_pdf(payload.dict())
    logger.info("orders.pdf length = %s bytes", 0 if not pdf_bytes else len(pdf_bytes))
    if not pdf_bytes:
        raise HTTPException(status_code=500, detail="Empty Orders PDF generated")
    return Response(content=pdf_bytes, media_type="application/pdf")

# ---------- Time (시간·요일) ----------

class TimeRow(BaseModel):
    date: Optional[str] = None        # MONTH에서 사용
    storeName: Optional[str] = None
    hourSlot: Optional[str] = None
    dayOfWeek: Optional[str] = None
    orderId: Optional[int] = None     # DAY에서 표시
    orderAmount: Optional[float] = 0  # 숫자
    category: Optional[str] = None    # DAY에서 표시
    menu: Optional[str] = None        # DAY에서 표시
    orderType: Optional[str] = None
    orderDate: Optional[str] = None   # DAY에서 표시

class TimePayload(BaseModel):
    criteria: Dict[str, Any] = Field(default_factory=dict)
    data: List[TimeRow] = Field(default_factory=list)

@app.post("/pdf/time", summary="시간·요일 분석 리포트 PDF 생성")

def create_time_report(payload: TimePayload):
    pdf_bytes = time_analytics.generate_time_pdf(payload.dict())
    if not pdf_bytes:
        raise HTTPException(status_code=500, detail="Empty PDF generated")
    return Response(content=pdf_bytes, media_type="application/pdf")

# ---------- Materials ----------

class MaterialsRow(BaseModel):
    orderDate: Optional[str] = None
    store: Optional[str] = None
    material: Optional[str] = None
    storeInventoryQty: Optional[int] = None
    purchaseOrderId: Optional[int] = None
    purchaseOrderDate: Optional[str] = None
    purchaseOrderQty: Optional[int] = None
    purchaseOrderAmount: Optional[float] = None
    turnoverRate: Optional[float] = None
    profit: Optional[float] = None
    margin: Optional[float] = None
    avgUsage: Optional[float] = None

class MaterialsPayload(BaseModel):
    criteria: Dict[str, Any] = Field(default_factory=dict)
    data: List[MaterialsRow] = Field(default_factory=list)

@app.post("/pdf/materials", summary="재료 분석 리포트 PDF 생성")

def create_materials_report(payload: MaterialsPayload):
    pdf_bytes = material_analytics.generate_materials_pdf(payload.dict())
    if not pdf_bytes:
        raise HTTPException(status_code=500, detail="Empty Materials PDF generated")
    return Response(content=pdf_bytes, media_type="application/pdf")