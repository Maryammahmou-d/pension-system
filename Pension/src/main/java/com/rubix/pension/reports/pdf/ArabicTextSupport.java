package com.rubix.pension.reports.pdf;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;

/**
 * Prepares Arabic for OpenPDF when drawing LTR (no RUN_DIRECTION_RTL).
 * Shape ligatures, then reorder to visual order so glyphs connect and read correctly.
 */
public final class ArabicTextSupport {

    private ArabicTextSupport() {
    }

    public static String forPdf(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        try {
            ArabicShaping shaper = new ArabicShaping(
                    ArabicShaping.LETTERS_SHAPE | ArabicShaping.LENGTH_FIXED_SPACES_AT_END
            );
            String shaped = shaper.shape(text);
            Bidi bidi = new Bidi(shaped, Bidi.DIRECTION_RIGHT_TO_LEFT);
            return bidi.writeReordered(Bidi.DO_MIRRORING | Bidi.KEEP_BASE_COMBINING);
        } catch (ArabicShapingException ex) {
            return text;
        }
    }
}
