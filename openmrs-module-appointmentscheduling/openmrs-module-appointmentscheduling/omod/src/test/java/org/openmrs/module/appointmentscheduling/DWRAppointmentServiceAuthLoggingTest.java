package org.openmrs.module.appointmentscheduling;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that DWRAppointmentService logs unauthorized (anonymous) calls.
 * B-06 fix: elke methode die voorheen een silent return deed bij
 * {@code !Context.isAuthenticated()} moet nu een WARN-log produceren,
 * conform NEN-7510 A.8.15 (Logging en monitoring) en A.8.16 (Monitoring activiteiten).
 */
public class DWRAppointmentServiceAuthLoggingTest {

    private static final String DWR_SERVICE_PATH =
            "src/main/java/org/openmrs/module/appointmentscheduling/web/DWRAppointmentService.java";

    private String readSource() throws IOException {
        return new String(Files.readAllBytes(Paths.get(DWR_SERVICE_PATH)));
    }

    @Test
    public void loggerVeld_isGedeclareerd() throws IOException {
        String source = readSource();
        assertTrue("Een statisch Log-veld moet aanwezig zijn voor auth-logging",
                source.contains("private static final Log log = LogFactory.getLogger(DWRAppointmentService.class)")
                        || source.contains("private static final Log log = LogFactory.getLog(DWRAppointmentService.class)"));
    }

    @Test
    public void loggerImports_zijnAanwezig() throws IOException {
        String source = readSource();
        assertTrue("commons-logging Log import moet aanwezig zijn",
                source.contains("import org.apache.commons.logging.Log;"));
        assertTrue("commons-logging LogFactory import moet aanwezig zijn",
                source.contains("import org.apache.commons.logging.LogFactory;"));
    }

    @Test
    public void warnLogging_inGetAppointmentBlocksForCalendar() throws IOException {
        String source = readSource();
        Pattern p = Pattern.compile(
                "log\\.warn\\(\\s*\"Unauthorized DWR call to getAppointmentBlocksForCalendar[^\"]*\"\\s*\\)");
        assertTrue("getAppointmentBlocksForCalendar moet bij anonieme sessie een WARN-log produceren",
                p.matcher(source).find());
    }

    @Test
    public void warnLogging_inGetAppointmentBlocks() throws IOException {
        String source = readSource();
        Pattern p = Pattern.compile(
                "log\\.warn\\(\\s*\"Unauthorized DWR call to getAppointmentBlocks[^\"]*\"\\s*\\)");
        assertTrue("getAppointmentBlocks moet bij anonieme sessie een WARN-log produceren",
                p.matcher(source).find());
    }

    @Test
    public void warnLogging_inGetPatientsInAppointmentBlock() throws IOException {
        String source = readSource();
        Pattern p = Pattern.compile(
                "log\\.warn\\(\\s*\"Unauthorized DWR call to getPatientsInAppointmentBlock[^\"]*\"\\s*\\)");
        assertTrue("getPatientsInAppointmentBlock moet bij anonieme sessie een WARN-log produceren",
                p.matcher(source).find());
    }

    @Test
    public void warnLogging_inGetTimeSlotLength() throws IOException {
        String source = readSource();
        Pattern p = Pattern.compile(
                "log\\.warn\\(\\s*\"Unauthorized DWR call to getTimeSlotLength[^\"]*\"\\s*\\)");
        assertTrue("getTimeSlotLength moet bij anonieme sessie een WARN-log produceren",
                p.matcher(source).find());
    }

    @Test
    public void vierWarnRegels_aanwezig() throws IOException {
        String source = readSource();
        int hits = 0;
        for (String line : source.split("\\R")) {
            if (line.contains("log.warn(\"Unauthorized DWR call to")) {
                hits++;
            }
        }
        assertTrue("Er moeten minstens 4 WARN-log regels voor anonieme DWR-calls zijn, gevonden: " + hits,
                hits >= 4);
    }

    @Test
    public void warnLogging_bevatGeenPII() throws IOException {
        String source = readSource();
        // SR-01 / B-05: geen PII in logs. Check uitsluitend de inhoud
        // van de log.warn-regels zelf, niet de hele file.
        for (String line : source.split("\\R")) {
            if (!line.contains("log.warn(")) {
                continue;
            }
            assertFalse("Log-statement mag geen patiëntnaam loggen: " + line.trim(),
                    line.contains("getPersonName") || line.contains("personName"));
            assertFalse("Log-statement mag geen geboortedatum loggen: " + line.trim(),
                    line.contains("getBirthdate") || line.contains("birthdate"));
            assertFalse("Log-statement mag geen patient identifier loggen: " + line.trim(),
                    line.contains("getPatientIdentifier") || line.contains("patientIdentifier"));
            assertFalse("Log-statement mag geen gender loggen: " + line.trim(),
                    line.contains("getGender"));
        }
    }
}
