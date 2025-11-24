from io import BytesIO
from typing import Dict, Any, List, Optional

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle

from .pdf_generator import PdfGenerator

_GEN = PdfGenerator()  # 폰트/스타일 공용 초기화

def _fmt_num(v: Optional[float], percent: bool = False) -> str:
    if v is None:
        return ""
    try:
        n = float(v)
        if percent:
            return f"{n:.1f}%"
        if n.is_integer():
            return f"{int(n):,}"
        return f"{n:,.0f}"
    except (TypeError, ValueError):
        return str(v)

def _headers(view_by_day: bool) -> List[str]:
    if view_by_day:
        return ["Date","OrderDate","Store","Category","Menu",
                "MenuCount","MenuSales","OrderCount","OrderSales","OrderType"]
    return ["Date","Store","MenuCount","MenuSales","OrderCount","OrderSales"]

def _col_widths(view_by_day: bool) -> List[float]:
    if view_by_day:
        # 10개 컬럼
        return [22*mm, 28*mm, 36*mm, 24*mm, 42*mm,
                18*mm, 24*mm, 18*mm, 24*mm, 20*mm]
    # 6개 컬럼
    return [28*mm, 42*mm, 22*mm, 28*mm, 22*mm, 28*mm]

def generate_orders_pdf(payload: Dict[str, Any]) -> bytes:
    """
    Orders 리포트 PDF 생성 (일별/월별 자동 전환)
    payload = {"criteria": {...}, "data": [ {...}, ... ] }
    """
    styles = _GEN.styles
    crit = payload.get("criteria", {}) or {}
    rows = payload.get("data", []) or []

    title = crit.get("title", "주문 분석 리포트")
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
                r.get("date",""), r.get("orderDate",""), r.get("storeName",""),
                r.get("category","-") or "-", r.get("menu","-") or "-",
                _fmt_num(r.get("menuCount")), _fmt_num(r.get("menuSales")),
                _fmt_num(r.get("orderCount")), _fmt_num(r.get("orderSales")),
                r.get("orderType","-") or "-"
            ])
        else:
            data.append([
                r.get("date",""), r.get("storeName",""),
                _fmt_num(r.get("menuCount")), _fmt_num(r.get("menuSales")),
                _fmt_num(r.get("orderCount")), _fmt_num(r.get("orderSales"))
            ])

    # 데이터가 0행이면 테이블 오류 방지를 위해 최소 1행 보정
    if len(data) == 1:
        data.append([""] * len(headers))

    table = Table(data, colWidths=_col_widths(is_daily), repeatRows=1)

    ts = [
        ("BACKGROUND",(0,0),(-1,0), colors.HexColor("#F3F3F3")),
        ("TEXTCOLOR",(0,0),(-1,0), colors.black),
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
            ("ALIGN",(0,1),(1,-1), "LEFT"),  # Date, OrderDate
            ("ALIGN",(2,1),(4,-1), "LEFT"),  # Store, Category, Menu
            ("ALIGN",(9,1),(9,-1), "LEFT"),  # OrderType
            ("ALIGN",(5,1),(8,-1), "RIGHT"), # 숫자열
        ]
    else:
        ts += [
            ("ALIGN",(0,1),(1,-1), "LEFT"),  # Date, Store
            ("ALIGN",(2,1),(5,-1), "RIGHT"), # 숫자열
        ]

    table.setStyle(TableStyle(ts))
    story.append(table)

    doc.build(story)
    return buf.getvalue()
