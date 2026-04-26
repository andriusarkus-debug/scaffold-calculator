package com.scaffold.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Consolidates tube counts across all categories into a single delivery list
 * for the yard operator.
 *
 * Result is a map keyed by tube size (e.g. "5ft", "21ft") with the total tube
 * count summed across standards, ledgers, handrails, ledger braces, transoms,
 * and sway bracing. Sizes are returned in physical-length order so the list
 * matches how a yard operator stages tubes.
 */
public final class TubeDeliveryUtil {

    // Display order — shortest to longest. Sizes outside this list fall to the end.
    private static final String[] SIZE_ORDER = {"5ft", "6ft", "8ft", "10ft", "13ft", "16ft", "21ft"};

    // Transomai visada 5ft — fiksuotas double-row scaffold pločio dydis (1.524m).
    public static final String TRANSOM_TUBE_SIZE = "5ft";

    private TubeDeliveryUtil() {} // utility class

    public static Map<String, Integer> buildTubeSummary(
            Map<String, Integer> standardTubeSummary,
            Map<String, Integer> ledgerTubeSummary,
            Map<String, Integer> handrailTubeSummary,
            Map<String, Integer> ledgerBraceTubeSummary,
            int transomCount,
            String swayBraceTubeSize,
            int swayBraceTubeCount) {

        // LinkedHashMap su iš anksto įdėtais raktais — užtikrina rūšiavimą pagal ilgį.
        Map<String, Integer> total = new LinkedHashMap<>();
        for (String size : SIZE_ORDER) total.put(size, 0);

        mergeInto(total, standardTubeSummary);
        mergeInto(total, ledgerTubeSummary);
        mergeInto(total, handrailTubeSummary);
        mergeInto(total, ledgerBraceTubeSummary);

        if (transomCount > 0) {
            total.merge(TRANSOM_TUBE_SIZE, transomCount, Integer::sum);
        }
        if (swayBraceTubeSize != null && !swayBraceTubeSize.isBlank() && swayBraceTubeCount > 0) {
            total.merge(swayBraceTubeSize, swayBraceTubeCount, Integer::sum);
        }

        // Pašaliname dydžius su 0 vienetų (kad UI nerodytų tuščių eilučių).
        total.values().removeIf(v -> v == 0);
        return total;
    }

    private static void mergeInto(Map<String, Integer> target, Map<String, Integer> src) {
        if (src == null) return;
        src.forEach((size, count) -> target.merge(size, count, Integer::sum));
    }
}
