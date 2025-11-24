# component/time_analytics.py
from io import BytesIO
from typing import Dict, Any, List, Optional

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle

from .pdf_generator import PdfGenerator

_GEN = PdfGenerator()

def _fmt_num(v: Optional[float]) -> str:
    if v is None: return ""
    try:
        n = float(v)
        return f"{int(n):,}" if n.is_integer() else f"{n:,.0f}"
    except Exception:
        return str(v)

def _headers(is_daily: bool) -> List[str]:
    if is_daily:
        return ["Store","시간대","요일","주문ID","주문금액","카테고리","메뉴","OrderType","OrderDate"]
    return ["Date","Store","시간대","요일","주문금액","OrderType"]

def _col_widths(is_daily: bool) -> List[float]:
    if is_daily:
        return [36*mm, 26*mm, 12*mm, 18*mm, 22*mm, 24*mm, 36*mm, 18*mm, 30*mm]
    return [22*mm, 36*mm, 22*mm, 12*mm, 22*mm, 18*mm]

def generate_time_pdf(payload: Dict[str, Any]) -> bytes:
    styles = _GEN.styles
    crit = payload.get("criteria", {}) or {}
    rows = payload.get("data", []) or []

    view_by = (crit.get("viewBy") or "DAY").upper()
    is_daily = (view_by == "DAY")

    title = crit.get("title", "시간·요일 분석 리포트")
    start = crit.get("startDate", "") or ""
    end   = crit.get("endDate", "") or ""

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
                r.get("storeName",""),
                r.get("hourSlot",""),
                r.get("dayOfWeek",""),
                ("" if r.get("orderId") is None else str(r.get("orderId"))),
                _fmt_num(r.get("orderAmount")),
                (r.get("category") or "-"),
                (r.get("menu") or "-"),
                (r.get("orderType") or "-"),
                r.get("orderDate",""),
            ])
        else:
            data.append([
                r.get("date",""),
                r.get("storeName",""),
                r.get("hourSlot",""),
                r.get("dayOfWeek",""),
                _fmt_num(r.get("orderAmount")),
                (r.get("orderType") or "-"),
            ])

    if len(data) == 1:
        data.append([""] * len(headers))

    table = Table(data, colWidths=_col_widths(is_daily), repeatRows=1)
    ts = [
        ("BACKGROUND",(0,0),(-1,0), colors.HexColor("#F3F3F3")),
        ("GRID",(0,0),(-1,-1), 0.5, colors.HexColor("#CED4DA")),
        ("FONTNAME",(0,0),(-1,0), "KR-Bold"),
        ("FONTNAME",(0,1),(-1,-1), "KR-Regular"),
        ("FONTSIZE",(0,0),(-1,-1), 9),
        ("ALIGN",(0,0),(-1,0), "CENTER"),
        ("VALIGN",(0,0),(-1,-1), "MIDDLE"),
        ("BOTTOMPADDING",(0,0),(-1,0), 3*mm),
        ("TOPPADDING",(0,0),(-1,0), 3*mm),
    ]
    if is_daily:
        ts += [
            ("ALIGN",(0,1),(2,-1), "LEFT"),   # Store, 시간대, 요일
            ("ALIGN",(3,1),(3,-1), "RIGHT"),  # 주문ID
            ("ALIGN",(4,1),(4,-1), "RIGHT"),  # 주문금액
            ("ALIGN",(5,1),(6,-1), "LEFT"),   # 카테고리, 메뉴
            ("ALIGN",(7,1),(8,-1), "LEFT"),   # OrderType, OrderDate
        ]
    else:
        ts += [
            ("ALIGN",(0,1),(1,-1), "LEFT"),   # Date, Store
            ("ALIGN",(2,1),(3,-1), "LEFT"),   # 시간대, 요일
            ("ALIGN",(4,1),(4,-1), "RIGHT"),  # 주문금액
            ("ALIGN",(5,1),(5,-1), "LEFT"),   # OrderType
        ]

    table.setStyle(TableStyle(ts))
    story.append(table)
    doc.build(story)

    return buf.getvalue()
