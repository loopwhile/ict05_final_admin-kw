from io import BytesIO
from typing import Dict, Any, List, Optional

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle

from .pdf_generator import PdfGenerator

_GEN = PdfGenerator()  # 폰트/스타일 공용 초기화

def _fmt_num(v: Optional[float], precision: int = 0) -> str:
    if v is None:
        return ""
    try:
        n = float(v)
        if n.is_integer() and precision == 0:
            return f"{int(n):,}"
        return f"{n:,.{precision}f}"
    except (TypeError, ValueError):
        return str(v)

def _fmt_pct(v: Optional[float], precision: int = 2) -> str:
    if v is None:
        return ""
    try:
        return f"{float(v):.{precision}%}"
    except (TypeError, ValueError):
        return str(v)


def _headers(view_by_day: bool) -> List[str]:
    if view_by_day:
        return ["Date", "Store", "Material", "Inv Qty", "PO ID", "PO Date", "PO Qty", "PO Amount", "Turnover", "Profit", "Margin", "Avg Usage"]
    return ["Date", "Store", "Material", "Inv Qty", "PO Qty", "PO Amount", "Turnover", "Profit", "Margin", "Avg Usage"]

def _col_widths(view_by_day: bool) -> List[float]:
    if view_by_day:
        # 12개 컬럼
        return [20*mm, 30*mm, 35*mm, 15*mm, 15*mm, 20*mm, 15*mm, 20*mm, 18*mm, 20*mm, 18*mm, 18*mm]
    # 10개 컬럼
    return [22*mm, 35*mm, 40*mm, 18*mm, 18*mm, 22*mm, 20*mm, 22*mm, 20*mm, 20*mm]

def generate_materials_pdf(payload: Dict[str, Any]) -> bytes:
    """
    Materials 리포트 PDF 생성 (일별/월별 자동 전환)
    payload = {"criteria": {...}, "data": [ {...}, ... ] }
    """
    styles = _GEN.styles
    crit = payload.get("criteria", {}) or {}
    rows = payload.get("data", []) or []

    title = crit.get("title", "재료 분석 리포트")
    start = crit.get("startDate", "") or ""
    end   = crit.get("endDate", "") or ""
    view_by = (crit.get("viewBy") or "DAY").upper()
    is_daily = (view_by == "DAY")

    buf = BytesIO()
    doc = SimpleDocTemplate(
        buf, pagesize=landscape(A4),
        leftMargin=10*mm, rightMargin=10*mm, topMargin=15*mm, bottomMargin=15*mm
    )

    story = []
    story.append(Paragraph(title, styles["TitleKR"]))
    story.append(Spacer(1, 4*mm))

    info = f"기간: {start} ~ {end}"
    extra = []
    if "rowCount" in crit and "totalCount" in crit:
        extra.append(f"Rows: {crit['rowCount']}/{crit['totalCount']}")
    if crit.get("truncated"):
        extra.append("(PDF에는 최대 행수만 포함)")
    if extra:
        info += "  |  " + " ".join(extra)

    story.append(Paragraph(info, styles["BodyRight"]))
    story.append(Spacer(1, 6*mm))

    headers = _headers(is_daily)
    data = [headers]

    for r in rows:
        if is_daily:
            data.append([
                r.get("orderDate", ""),
                r.get("store", ""),
                r.get("material", ""),
                _fmt_num(r.get("storeInventoryQty")),
                r.get("purchaseOrderId", "-") or "-",
                r.get("purchaseOrderDate", "-") or "-",
                _fmt_num(r.get("purchaseOrderQty")),
                _fmt_num(r.get("purchaseOrderAmount")),
                _fmt_num(r.get("turnoverRate"), 2),
                _fmt_num(r.get("profit")),
                _fmt_pct(r.get("margin"), 2),
                _fmt_num(r.get("avgUsage"), 2)
            ])
        else: # 월별
            data.append([
                r.get("orderDate", ""),
                r.get("store", ""),
                r.get("material", ""),
                _fmt_num(r.get("storeInventoryQty")),
                _fmt_num(r.get("purchaseOrderQty")),
                _fmt_num(r.get("purchaseOrderAmount")),
                _fmt_num(r.get("turnoverRate"), 2),
                _fmt_num(r.get("profit")),
                _fmt_pct(r.get("margin"), 2),
                _fmt_num(r.get("avgUsage"), 2)
            ])

    if len(data) == 1:
        data.append([""] * len(headers))

    table = Table(data, colWidths=_col_widths(is_daily), repeatRows=1)

    ts = [
        ("BACKGROUND",(0,0),(-1,0), colors.HexColor("#F3F3F3")),
        ("TEXTCOLOR",(0,0),(-1,0), colors.black),
        ("GRID",(0,0),(-1,-1), 0.5, colors.HexColor("#CED4DA")),
        ("FONTNAME",(0,0),(-1,0), "KR-Bold"),
        ("FONTNAME",(0,1),(-1,-1), "KR-Regular"),
        ("FONTSIZE",(0,0),(-1,-1), 8),
        ("ALIGN",(0,0),(-1,0), "CENTER"),
        ("VALIGN",(0,0),(-1,-1), "MIDDLE"),
        ("BOTTOMPADDING",(0,0),(-1,0), 3*mm),
        ("TOPPADDING",(0,0),(-1,0), 3*mm),
        ("ALIGN",(0,1),(2,-1), "LEFT"),      # Date, Store, Material
    ]
    
    # 숫자열 오른쪽 정렬
    if is_daily:
        ts.append(("ALIGN",(3,1),(-1,-1), "RIGHT"))
    else:
        ts.append(("ALIGN",(3,1),(-1,-1), "RIGHT"))


    table.setStyle(TableStyle(ts))
    story.append(table)

    doc.build(story)
    return buf.getvalue()
