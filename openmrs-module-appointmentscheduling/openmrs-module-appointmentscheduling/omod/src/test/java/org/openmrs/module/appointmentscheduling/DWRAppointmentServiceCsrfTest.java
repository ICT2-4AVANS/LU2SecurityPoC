package org.openmrs.module.appointmentscheduling;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Verifies that DWRAppointmentService contains CSRF protection via X-Requested-With header check.
 * B-07 fix: alle publieke DWR-methoden moeten requireXmlHttpRequest() aanroepen.
 */
public class DWRAppointmentServiceCsrfTest {

    private static final String DWR_SERVICE_PATH =
            "src/main/java/org/openmrs/module/appointmentscheduling/web/DWRAppointmentService.java";

    private String readSource() throws IOException {
        return new String(Files.readAllBytes(Paths.get(DWR_SERVICE_PATH)));
    }

    @Test
    public void requireXmlHttpRequest_methodeAanwezig() throws IOException {
        String source = readSource();
        assertTrue("requireXmlHttpRequest() moet gedefinieerd zijn in DWRAppointmentService",
                source.contains("private void requireXmlHttpRequest()"));
    }

    @Test
    public void csrfCheck_aanwezig_inGetPatientDescription() throws IOException {
        String source = readSource();
        assertTrue("getPatientDescription moet requireXmlHttpRequest() aanroepen",
                source.contains("public PatientData getPatientDescription") &&
                source.contains("requireXmlHttpRequest()"));
    }

    @Test
    public void csrfCheck_aanwezig_inGetAppointmentBlocksForCalendar() throws IOException {
        String source = readSource();
        assertTrue("getAppointmentBlocksForCalendar moet requireXmlHttpRequest() aanroepen",
                source.contains("public List<AppointmentBlockData> getAppointmentBlocksForCalendar") &&
                source.contains("requireXmlHttpRequest()"));
    }

    @Test
    public void csrfCheck_kontroleertHeaderNaam() throws IOException {
        String source = readSource();
        assertTrue("CSRF check moet X-Requested-With header valideren",
                source.contains("X-Requested-With"));
        assertTrue("CSRF check moet XMLHttpRequest verwachten",
                source.contains("XMLHttpRequest"));
    }

    @Test
    public void csrfCheck_gooit_SecurityException_bij_ontbrekende_header() throws IOException {
        String source = readSource();
        assertTrue("CSRF check moet SecurityException gooien",
                source.contains("throw new SecurityException"));
    }

    @Test
    public void geenDirecteStringConcatenatie_inCsrfCheck() throws IOException {
        String source = readSource();
        assertFalse("CSRF check mag geen header waarde concateneren in query",
                source.contains("getHeader(\"X-Requested-With\") +"));
    }
}
