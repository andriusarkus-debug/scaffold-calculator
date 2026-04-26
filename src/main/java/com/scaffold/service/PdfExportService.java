package com.scaffold.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.scaffold.entity.Calculation;
import com.scaffold.entity.CalculationLift;
import com.scaffold.model.LadderTowerResult;
import com.scaffold.model.LoadingBayResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generates a PDF document with the full scaffold material breakdown for a Calculation.
 * Uses OpenPDF (LGPL/MPL fork of iText) — pure Java PDF generation, no HTML conversion.
 */
@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final AccessTowerService accessTowerService;

    // --- Fonts ---
    private static final Font TITLE_FONT   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(30, 58, 138));
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(30, 58, 138));
    private static final Font HEADER_FONT  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font CELL_FONT    = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font META_FONT    = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(75, 85, 99));

    private static final Color HEADER_BG = new Color(30, 58, 138);   // navy
    private static final Color ZEBRA_BG  = new Color(243, 244, 246); // light gray
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] generatePdf(Calculation calc) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            addTitle(document, calc);
            addInputParameters(document, calc);
            addLiftsTable(document, calc);
            addMainComponents(document, calc);
            addFittings(document, calc);
            addLoadingBay(document, calc);
            addLadderTower(document, calc);

            // Force page break — keep delivery + breakdowns together on a fresh page
            document.newPage();
            addDeliverySummary(document, calc);
            addTubeBreakdown(document, calc);
            addBoardBreakdown(document, calc);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Failed to generate PDF for calculation " + calc.getId(), e);
        }
    }

    // --- Sections ---

    private void addTitle(Document doc, Calculation calc) throws DocumentException {
        Paragraph title = new Paragraph("SCAFFOLD MATERIAL CALCULATION", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        doc.add(title);

        String project = calc.getProjectName() != null && !calc.getProjectName().isBlank()
                ? calc.getProjectName() : "(unnamed project)";
        String createdBy = calc.getUser() != null ? calc.getUser().getUsername() : "—";
        String createdAt = calc.getCreatedAt() != null ? calc.getCreatedAt().format(DATE_FMT) : "—";

        Paragraph meta = new Paragraph();
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.add(new Phrase("Project: " + project + "    |    User: " + createdBy + "    |    Date: " + createdAt, META_FONT));
        meta.setSpacingAfter(14);
        doc.add(meta);
    }

    private void addInputParameters(Document doc, Calculation calc) throws DocumentException {
        addSectionHeading(doc, "House Parameters");

        PdfPTable t = newKeyValueTable();
        addKV(t, "Shape",        calc.getHouseShape() != null ? calc.getHouseShape().name() : "—");
        addKV(t, "Length",       String.format("%.2f m", calc.getHouseLength()));
        addKV(t, "Width",        String.format("%.2f m", calc.getHouseWidth()));
        if (calc.getLCutLength() > 0 || calc.getLCutWidth() > 0) {
            addKV(t, "Cut length", String.format("%.2f m", calc.getLCutLength()));
            addKV(t, "Cut width",  String.format("%.2f m", calc.getLCutWidth()));
        }
        addKV(t, "Roof type",    calc.getRoofType() != null ? calc.getRoofType().name() : "—");
        addKV(t, "Roof pitch",   String.format("%.0f°", calc.getRoofPitch()));
        addKV(t, "Gable ends",   String.valueOf(calc.getGableEnds()));
        addKV(t, "Tube size",    calc.getTubeSize() != null ? calc.getTubeSize().name() : "—");
        addKV(t, "Ledger scen.", calc.getLedgerScenario() != null ? calc.getLedgerScenario().name() : "—");
        addKV(t, "Perimeter",    String.format("%.2f m", calc.getPerimeter()));
        addKV(t, "Total bays",   String.valueOf(calc.getBays()));
        addKV(t, "Total height", String.format("%.2f m", calc.getTotalHeight()));
        doc.add(t);
        spacer(doc, 10);
    }

    private void addLiftsTable(Document doc, Calculation calc) throws DocumentException {
        List<CalculationLift> lifts = calc.getLifts();
        if (lifts == null || lifts.isEmpty()) return;

        addSectionHeading(doc, "Lifts");

        PdfPTable t = new PdfPTable(new float[]{1, 2, 2, 2});
        t.setWidthPercentage(100);
        addHeaderRow(t, "Lift #", "Height", "Has boards", "Board size");
        for (CalculationLift lift : lifts) {
            addRow(t,
                    String.valueOf(lift.getLiftNumber()),
                    String.format("%.2f m", lift.getHeight()),
                    lift.isHasBoards() ? "Yes" : "No",
                    lift.getBoardSize() != null ? lift.getBoardSize().name() : "—");
        }
        doc.add(t);
        spacer(doc, 10);
    }

    private void addMainComponents(Document doc, Calculation calc) throws DocumentException {
        addSectionHeading(doc, "Main Components");

        // Use physical tube counts if available, otherwise fallback to logical
        int ledgersTotal   = sumOrFallback(calc.getLedgerTubeSummary(),   calc.getLedgers());
        int handrailsTotal = sumOrFallback(calc.getHandrailTubeSummary(), calc.getHandrails());

        PdfPTable t = newMaterialTable();
        addMaterialRow(t, "Standards",              calc.getStandards());
        addMaterialRow(t, "Ledgers",                ledgersTotal);
        addMaterialRow(t, "Handrails",              handrailsTotal);
        addMaterialRow(t, "Transoms (5ft)",         calc.getTransoms());
        addMaterialRow(t, "Boards",                 calc.getBoards());
        addMaterialRow(t, "Base plates",            calc.getBasePlates());
        addMaterialRow(t, "Sole boards",            calc.getSoleBoards());
        addMaterialRow(t, "Toeboards",              calc.getToeboards());
        addMaterialRow(t, "Advance guard rail sets", calc.getAdvanceGuardRailSets());
        addMaterialRow(t, "Sway bracing",           calc.getSwayBracing());
        addMaterialRow(t, "Ledger bracing",         calc.getLedgerBracing());
        if (calc.getGableStandards() > 0) {
            addMaterialRow(t, "Gable standards",    calc.getGableStandards());
        }
        doc.add(t);
        spacer(doc, 10);
    }

    private void addFittings(Document doc, Calculation calc) throws DocumentException {
        addSectionHeading(doc, "Fittings (Couplers)");

        PdfPTable t = newMaterialTable();
        addMaterialRow(t, "Right-angle couplers", calc.getRightAngleCouplers());
        addMaterialRow(t, "Swivel couplers",      calc.getSwivelCouplers());
        addMaterialRow(t, "Sleeve couplers",      calc.getSleeveCouplers());
        addMaterialRow(t, "Putlog couplers",      calc.getPutlogCouplers());
        if (calc.getGableCouplers() > 0) {
            addMaterialRow(t, "Gable couplers",   calc.getGableCouplers());
        }
        doc.add(t);
        spacer(doc, 10);
    }

    private void addLoadingBay(Document doc, Calculation calc) throws DocumentException {
        int liftCount = calc.getLifts() != null ? calc.getLifts().size() : 0;
        if (liftCount == 0) return;
        LoadingBayResult lb = accessTowerService.calculateLoadingBay(liftCount);

        addSectionHeading(doc, "Loading Bay (" + liftCount + " lifts)");

        PdfPTable t = new PdfPTable(new float[]{4, 1, 4, 1});
        t.setWidthPercentage(100);
        addHeaderRow(t, "Material", "Qty", "Material", "Qty");

        // Tubes
        addAccessRow(t, "Tubes 21ft", lb.getTubes21ft(),  "Tubes 13ft", lb.getTubes13ft());
        addAccessRow(t, "Tubes 10ft", lb.getTubes10ft(),  "Tubes 8ft",  lb.getTubes8ft());
        addAccessRow(t, "Tubes 6ft",  lb.getTubes6ft(),   "Tubes 5ft",  lb.getTubes5ft());
        // Boards & other
        addAccessRow(t, "Boards 13ft", lb.getBoards13ft(), "Boards 5ft", lb.getBoards5ft());
        addAccessRow(t, "Sole boards", lb.getSoleBoards(), "Loading bay gates", lb.getLoadingBayGates());
        // Fittings
        addAccessRow(t, "Right-angle couplers", lb.getRightAngleCouplers(),
                        "Putlog couplers",      lb.getPutlogCouplers());
        addAccessRow(t, "Swivel couplers", lb.getSwivelCouplers(), "", -1);

        doc.add(t);
        spacer(doc, 8);
    }

    private void addLadderTower(Document doc, Calculation calc) throws DocumentException {
        int liftCount = calc.getLifts() != null ? calc.getLifts().size() : 0;
        if (liftCount == 0) return;
        LadderTowerResult lt = accessTowerService.calculateLadderTower(liftCount);

        addSectionHeading(doc, "Ladder Tower (" + liftCount + " lifts)");

        PdfPTable t = new PdfPTable(new float[]{4, 1, 4, 1});
        t.setWidthPercentage(100);
        addHeaderRow(t, "Material", "Qty", "Material", "Qty");

        // Tubes
        addAccessRow(t, "Tubes 13ft", lt.getTubes13ft(), "Tubes 10ft", lt.getTubes10ft());
        addAccessRow(t, "Tubes 8ft",  lt.getTubes8ft(),  "Tubes 5ft",  lt.getTubes5ft());
        // Boards
        addAccessRow(t, "Boards 13ft", lt.getBoards13ft(), "Boards 8ft", lt.getBoards8ft());
        addAccessRow(t, "Boards 5ft",  lt.getBoards5ft(),  "", -1);
        // Access
        addAccessRow(t, "Ladders 4m", lt.getLadders4m(), "Ladder gates", lt.getLadderGates());
        // Fittings
        addAccessRow(t, "Right-angle couplers", lt.getRightAngleCouplers(),
                        "Putlog couplers",      lt.getPutlogCouplers());
        addAccessRow(t, "Swivel couplers", lt.getSwivelCouplers(), "", -1);

        doc.add(t);
        spacer(doc, 8);
    }

    /** Add a 4-column row (label-qty pair × 2). Pass qty=-1 + empty label for the right-side empty cell. */
    private void addAccessRow(PdfPTable t, String label1, int qty1, String label2, int qty2) {
        boolean zebra = (t.getRows().size() % 2 == 0);
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);

        PdfPCell l1 = new PdfPCell(new Phrase(label1, CELL_FONT));
        l1.setPadding(4);
        if (zebra) l1.setBackgroundColor(ZEBRA_BG);
        t.addCell(l1);

        PdfPCell q1 = new PdfPCell(new Phrase(String.valueOf(qty1), bold));
        q1.setPadding(4);
        q1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (zebra) q1.setBackgroundColor(ZEBRA_BG);
        t.addCell(q1);

        PdfPCell l2 = new PdfPCell(new Phrase(label2, CELL_FONT));
        l2.setPadding(4);
        if (zebra) l2.setBackgroundColor(ZEBRA_BG);
        t.addCell(l2);

        PdfPCell q2 = new PdfPCell(new Phrase(qty2 < 0 ? "" : String.valueOf(qty2), bold));
        q2.setPadding(4);
        q2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (zebra) q2.setBackgroundColor(ZEBRA_BG);
        t.addCell(q2);
    }

    private void addDeliverySummary(Document doc, Calculation calc) throws DocumentException {
        Map<String, Integer> tubes = calc.getTubeDeliverySummary();
        Map<String, Integer> boards = calc.getBoardSummary();

        boolean hasTubes  = tubes  != null && !tubes.isEmpty();
        boolean hasBoards = boards != null && !boards.isEmpty();
        if (!hasTubes && !hasBoards) return;

        addSectionHeading(doc, "Delivery Summary — Tubes & Boards by Size");

        // Side-by-side: tubes on left, boards on right
        PdfPTable wrap = new PdfPTable(2);
        wrap.setWidthPercentage(100);
        wrap.setSpacingAfter(10);

        // Tubes column
        PdfPCell tubesCell = new PdfPCell();
        tubesCell.setBorder(0);
        tubesCell.setPaddingRight(8);
        if (hasTubes) {
            PdfPTable t = new PdfPTable(new float[]{2, 2});
            t.setWidthPercentage(100);
            addHeaderRow(t, "Tubes (all)", "Total qty");
            int total = 0;
            for (Map.Entry<String, Integer> e : tubes.entrySet()) {
                addRow(t, e.getKey(), String.valueOf(e.getValue()));
                total += e.getValue();
            }
            addTotalRow(t, "TOTAL", total);
            tubesCell.addElement(t);
        }
        wrap.addCell(tubesCell);

        // Boards column
        PdfPCell boardsCell = new PdfPCell();
        boardsCell.setBorder(0);
        boardsCell.setPaddingLeft(8);
        if (hasBoards) {
            PdfPTable b = new PdfPTable(new float[]{2, 2});
            b.setWidthPercentage(100);
            addHeaderRow(b, "Boards", "Total qty");
            int total = 0;
            for (Map.Entry<String, Integer> e : boards.entrySet()) {
                addRow(b, e.getKey(), String.valueOf(e.getValue()));
                total += e.getValue();
            }
            addTotalRow(b, "TOTAL", total);
            boardsCell.addElement(b);
        }
        wrap.addCell(boardsCell);

        doc.add(wrap);
    }

    private void addTotalRow(PdfPTable t, String label, int total) {
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setPadding(4);
        labelCell.setBackgroundColor(ZEBRA_BG);
        PdfPCell valCell = new PdfPCell(new Phrase(String.valueOf(total), boldFont));
        valCell.setPadding(4);
        valCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valCell.setBackgroundColor(ZEBRA_BG);
        t.addCell(labelCell);
        t.addCell(valCell);
    }

    private void addTubeBreakdown(Document doc, Calculation calc) throws DocumentException {
        Map<String, String> wallGroupLedgers = calc.getWallGroupLedgerBreakdown();
        if (wallGroupLedgers == null || wallGroupLedgers.isEmpty()) return;

        addSectionHeading(doc, "Ledger Tubes per Wall Group (all levels)");

        PdfPTable t = new PdfPTable(new float[]{2, 5});
        t.setWidthPercentage(100);
        addHeaderRow(t, "Wall group", "Tubes needed");
        for (Map.Entry<String, String> e : wallGroupLedgers.entrySet()) {
            addRow(t, e.getKey(), e.getValue());
        }
        doc.add(t);
        spacer(doc, 10);
    }

    private void addBoardBreakdown(Document doc, Calculation calc) throws DocumentException {
        Map<String, String> wallGroupBoards = calc.getWallGroupBoardBreakdown();
        if (wallGroupBoards == null || wallGroupBoards.isEmpty()) return;

        addSectionHeading(doc, "Boards per Wall Group (1 platform)");

        PdfPTable t = new PdfPTable(new float[]{2, 5});
        t.setWidthPercentage(100);
        addHeaderRow(t, "Wall group", "Boards needed");
        for (Map.Entry<String, String> e : wallGroupBoards.entrySet()) {
            addRow(t, e.getKey(), e.getValue());
        }
        doc.add(t);
    }

    // --- Helpers ---

    private void addSectionHeading(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, SECTION_FONT);
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private PdfPTable newMaterialTable() {
        PdfPTable t = new PdfPTable(new float[]{4, 1});
        t.setWidthPercentage(100);
        addHeaderRow(t, "Material", "QTY");
        return t;
    }

    private PdfPTable newKeyValueTable() {
        PdfPTable t = new PdfPTable(new float[]{2, 3, 2, 3});
        t.setWidthPercentage(100);
        return t;
    }

    private void addHeaderRow(PdfPTable t, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            t.addCell(cell);
        }
    }

    private void addRow(PdfPTable t, String... values) {
        boolean zebra = (t.getRows().size() % 2 == 0);
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v, CELL_FONT));
            cell.setPadding(4);
            if (zebra) cell.setBackgroundColor(ZEBRA_BG);
            t.addCell(cell);
        }
    }

    private void addMaterialRow(PdfPTable t, String label, int qty) {
        boolean zebra = (t.getRows().size() % 2 == 0);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, CELL_FONT));
        labelCell.setPadding(4);
        if (zebra) labelCell.setBackgroundColor(ZEBRA_BG);

        PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(qty),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK)));
        qtyCell.setPadding(4);
        qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (zebra) qtyCell.setBackgroundColor(ZEBRA_BG);

        t.addCell(labelCell);
        t.addCell(qtyCell);
    }

    private void addKV(PdfPTable t, String key, String value) {
        PdfPCell k = new PdfPCell(new Phrase(key, META_FONT));
        k.setPadding(3);
        k.setBorder(0);
        PdfPCell v = new PdfPCell(new Phrase(value, CELL_FONT));
        v.setPadding(3);
        v.setBorder(0);
        t.addCell(k);
        t.addCell(v);
    }

    private int sumOrFallback(Map<String, Integer> map, int fallback) {
        if (map == null || map.isEmpty()) return fallback;
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }

    private void spacer(Document doc, float height) throws DocumentException {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(height);
        doc.add(p);
    }
}
