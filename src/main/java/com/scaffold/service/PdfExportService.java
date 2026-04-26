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
            // Force page break — Loading Bay + Ladder Tower on a fresh page
            // (prevents orphan split where heading + 1 row land at bottom of previous page)
            document.newPage();
            addLoadingBay(document, calc);
            addLadderTower(document, calc);

            // Force page break — Lorry Loading Summary + breakdowns on a fresh page
            document.newPage();
            addLorryLoadingSummary(document, calc);
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
        t.setKeepTogether(true);
        t.setSplitLate(true);
        t.setHeaderRows(1);
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
        t.setKeepTogether(true);
        t.setSplitLate(true);
        t.setHeaderRows(1);
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
        t.setKeepTogether(true);
        t.setSplitLate(true);
        t.setHeaderRows(1);
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

    /**
     * Lorry Loading Summary — pilnas yard operator pick list įsigant kroviniui:
     * tubes (visi šaltiniai), boards (visi šaltiniai), fittings (visi couplers),
     * other (ladders, gates, base plates, sole boards, toeboards, ir t.t.).
     */
    private void addLorryLoadingSummary(Document doc, Calculation calc) throws DocumentException {
        int liftCount = calc.getLifts() != null ? calc.getLifts().size() : 0;
        LoadingBayResult lb = liftCount > 0 ? accessTowerService.calculateLoadingBay(liftCount) : null;
        LadderTowerResult lt = liftCount > 0 ? accessTowerService.calculateLadderTower(liftCount) : null;

        // --- Konsoliduoti TUBES (pridedam access tower vamzdžius prie wall scaffold) ---
        Map<String, Integer> tubes = new java.util.LinkedHashMap<>(
                calc.getTubeDeliverySummary() != null ? calc.getTubeDeliverySummary() : Map.of());
        if (lb != null) {
            mergeTube(tubes, "5ft", lb.getTubes5ft());
            mergeTube(tubes, "6ft", lb.getTubes6ft());
            mergeTube(tubes, "8ft", lb.getTubes8ft());
            mergeTube(tubes, "10ft", lb.getTubes10ft());
            mergeTube(tubes, "13ft", lb.getTubes13ft());
            mergeTube(tubes, "21ft", lb.getTubes21ft());
        }
        if (lt != null) {
            mergeTube(tubes, "5ft", lt.getTubes5ft());
            mergeTube(tubes, "8ft", lt.getTubes8ft());
            mergeTube(tubes, "10ft", lt.getTubes10ft());
            mergeTube(tubes, "13ft", lt.getTubes13ft());
        }
        tubes = sortTubeSizes(tubes);

        // --- Konsoliduoti BOARDS ---
        Map<String, Integer> boards = new java.util.LinkedHashMap<>(
                calc.getBoardSummary() != null ? calc.getBoardSummary() : Map.of());
        if (lb != null) {
            mergeTube(boards, "13ft", lb.getBoards13ft());
            mergeTube(boards, "5ft", lb.getBoards5ft());
        }
        if (lt != null) {
            mergeTube(boards, "13ft", lt.getBoards13ft());
            mergeTube(boards, "8ft", lt.getBoards8ft());
            mergeTube(boards, "5ft", lt.getBoards5ft());
        }
        boards = sortTubeSizes(boards);

        // --- Konsoliduoti FITTINGS (couplers) ---
        int totalRA = calc.getRightAngleCouplers()
                + (lb != null ? lb.getRightAngleCouplers() : 0)
                + (lt != null ? lt.getRightAngleCouplers() : 0);
        int totalSwivel = calc.getSwivelCouplers()
                + (lb != null ? lb.getSwivelCouplers() : 0)
                + (lt != null ? lt.getSwivelCouplers() : 0);
        int totalSleeve = calc.getSleeveCouplers();
        int totalPutlog = calc.getPutlogCouplers()
                + (lb != null ? lb.getPutlogCouplers() : 0)
                + (lt != null ? lt.getPutlogCouplers() : 0);

        addSectionHeading(doc, "Lorry Loading Summary — Yard Pick List");
        Paragraph note = new Paragraph(
                "Total quantities including wall scaffold + loading bay + ladder tower",
                META_FONT);
        note.setSpacingAfter(6);
        doc.add(note);

        // 2x2 grid of mini-tables: TUBES | BOARDS / FITTINGS | OTHER
        PdfPTable grid = new PdfPTable(2);
        grid.setWidthPercentage(100);
        grid.setSpacingAfter(10);
        grid.setKeepTogether(true);

        // TUBES
        grid.addCell(buildSummaryCell("Tubes (all)", tubes, true));
        // BOARDS
        grid.addCell(buildSummaryCell("Boards (all)", boards, true));

        // FITTINGS
        Map<String, Integer> fittings = new java.util.LinkedHashMap<>();
        if (totalRA > 0)     fittings.put("Right-angle couplers", totalRA);
        if (totalSwivel > 0) fittings.put("Swivel couplers",      totalSwivel);
        if (totalSleeve > 0) fittings.put("Sleeve couplers",      totalSleeve);
        if (totalPutlog > 0) fittings.put("Putlog couplers",      totalPutlog);
        grid.addCell(buildSummaryCell("Fittings (couplers)", fittings, true));

        // OTHER COMPONENTS
        Map<String, Integer> other = new java.util.LinkedHashMap<>();
        if (calc.getStandards() > 0)            other.put("Standards (vamzdžiai)", calc.getStandards());
        int basePlates = calc.getBasePlates();
        int soleBoards = calc.getSoleBoards() + (lb != null ? lb.getSoleBoards() : 0);
        if (basePlates > 0) other.put("Base plates", basePlates);
        if (soleBoards > 0) other.put("Sole boards", soleBoards);
        if (calc.getToeboards() > 0)            other.put("Toeboards", calc.getToeboards());
        if (calc.getAdvanceGuardRailSets() > 0) other.put("Advance guard rail sets", calc.getAdvanceGuardRailSets());
        if (lt != null && lt.getLadders4m() > 0)        other.put("Ladders 4m", lt.getLadders4m());
        if (lt != null && lt.getLadderGates() > 0)      other.put("Ladder gates", lt.getLadderGates());
        if (lb != null && lb.getLoadingBayGates() > 0)  other.put("Loading bay gates", lb.getLoadingBayGates());
        grid.addCell(buildSummaryCell("Other components", other, false)); // no TOTAL row — these are heterogeneous items

        doc.add(grid);
    }

    private PdfPCell buildSummaryCell(String title, Map<String, Integer> data, boolean showTotal) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setPadding(4);
        if (data == null || data.isEmpty()) {
            cell.addElement(new Phrase(""));
            return cell;
        }
        PdfPTable t = new PdfPTable(new float[]{3, 1});
        t.setWidthPercentage(100);
        t.setKeepTogether(true);
        t.setSplitLate(true);
        addHeaderRow(t, title, "Qty");
        int total = 0;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            addRow(t, e.getKey(), String.valueOf(e.getValue()));
            total += e.getValue();
        }
        if (showTotal) {
            addTotalRow(t, "TOTAL", total);
        }
        cell.addElement(t);
        return cell;
    }

    private static void mergeTube(Map<String, Integer> map, String key, int qty) {
        if (qty <= 0) return;
        map.merge(key, qty, Integer::sum);
    }

    /** Rūšiuoja vamzdžių dydžius pagal fizinį ilgį (5ft, 6ft, 8ft, 10ft, 13ft, 16ft, 21ft). */
    private static Map<String, Integer> sortTubeSizes(Map<String, Integer> input) {
        java.util.List<String> order = java.util.List.of("5ft", "6ft", "8ft", "10ft", "13ft", "16ft", "21ft");
        Map<String, Integer> sorted = new java.util.LinkedHashMap<>();
        for (String size : order) {
            Integer v = input.get(size);
            if (v != null && v > 0) sorted.put(size, v);
        }
        // any other sizes not in known order — append at end
        for (Map.Entry<String, Integer> e : input.entrySet()) {
            if (!order.contains(e.getKey()) && e.getValue() != null && e.getValue() > 0) {
                sorted.put(e.getKey(), e.getValue());
            }
        }
        return sorted;
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
        t.setKeepTogether(true);
        t.setSplitLate(true);
        t.setHeaderRows(1);
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
        t.setKeepTogether(true);
        t.setSplitLate(true);
        t.setHeaderRows(1);
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
        t.setKeepTogether(true);   // sekcija turi tilpti viename puslapyje (jei netelpa, perkeliama nauja)
        t.setSplitLate(true);
        t.setHeaderRows(1);        // jei vis dėl to nepavyksta — antraštė kartojasi sekančiame puslapyje
        addHeaderRow(t, "Material", "QTY");
        return t;
    }

    private PdfPTable newKeyValueTable() {
        PdfPTable t = new PdfPTable(new float[]{2, 3, 2, 3});
        t.setWidthPercentage(100);
        t.setKeepTogether(true);
        t.setSplitLate(true);
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
