from io import BytesIO
from typing import Dict, Any
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
from reportlab.lib import colors

from .pdf_generator import PdfGenerator
_GEN = PdfGenerator()

def generate_kpi_pdf(payload: Dict[str, Any]) -> bytes:
    crit = payload.get("criteria", {}) or {}
    rows = payload.get("data", []) or []
    buf = BytesIO()
    doc = SimpleDocTemplate(buf, pagesize=landscape(A4),
                            leftMargin=10*mm, rightMargin=10*mm, topMargin=15*mm, bottomMargin=15*mm)
    story = []
    story.append(Paragraph(crit.get("title","KPI 리포트"), _GEN.styles["TitleKR"]))
    story.append(Spacer(1, 6*mm))

    headers = ["Date","Store","Sales","Transaction","UPT","ADS","AUR","Comp.MoM","Comp.YoY"]
    data = [headers]
    for r in rows:
        data.append([
            r.get("date",""), r.get("storeName",""), r.get("sales",""), r.get("transaction",""),
            r.get("upt",""), r.get("ads",""), r.get("aur",""), r.get("compMoM",""), r.get("compYoY",""),
        ])
    if len(data) == 1:
        data.append([""]*len(headers))

    t = Table(data, repeatRows=1)
    t.setStyle(TableStyle([
        ("BACKGROUND",(0,0),(-1,0), colors.HexColor("#F3F3F3")),
        ("GRID",(0,0),(-1,-1), 0.5, colors.HexColor("#CED4DA")),
        ("FONTNAME",(0,0),(-1,0), "KR-Bold"),
        ("FONTNAME",(0,1),(-1,-1), "KR-Regular"),
    ]))
    story.append(t)
    doc.build(story)
    return buf.getvalue()
