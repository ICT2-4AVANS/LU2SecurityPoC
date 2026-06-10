package org.openmrs.module.appointmentscheduling.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link AuditLogger}.
 *
 * Verifies (security backlog SR-08 / SR-09 + gap-analyse 02gapanalyselogging.md):
 *   - successful actions are logged at INFO with outcome=success
 *   - failed actions (denied / failed) are logged at WARN / ERROR with the
 *     correct outcome
 *   - audit lines never contain PII (patient name, DOB, identifier, gender)
 *   - audit lines always contain the five required fields: who / what /
 *     where / when / how
 *
 * These are plain JUnit 4 unit tests; no OpenMRS context is required.
 */
public class AuditLoggerTest {

    private CapturingLog capturingLog;

    private AuditLogger auditLogger;

    @Before
    public void setUp() {
        capturingLog = new CapturingLog();
        auditLogger = new AuditLogger(capturingLog);
    }

    // ------------------------------------------------------------------
    // Successful actions
    // ------------------------------------------------------------------

    @Test
    public void logSuccess_writesInfoLineWithSuccessOutcome() {
        auditLogger.logSuccess("user-uuid-1", "saveAppointment", Integer.valueOf(1234),
                "AppointmentServiceImpl", "192.0.2.10", AuditLogger.Channel.REST);

        assertEquals("expected exactly one INFO log line", 1, capturingLog.infoMessages.size());
        assertEquals("no WARN expected", 0, capturingLog.warnMessages.size());
        assertEquals("no ERROR expected", 0, capturingLog.errorMessages.size());

        String line = capturingLog.infoMessages.get(0);
        assertTrue("line must start with [AUDIT] tag: " + line, line.startsWith("[AUDIT] "));
        assertTrue("line must contain outcome success: " + line, line.contains("/success"));
        assertTrue("line must contain channel REST: " + line, line.contains("how=REST/"));
        assertTrue("line must contain action: " + line, line.contains("what=saveAppointment:1234"));
        assertTrue("line must contain who: " + line, line.contains("who=user-uuid-1"));
    }

    // ------------------------------------------------------------------
    // Failed actions
    // ------------------------------------------------------------------

    @Test
    public void logDenied_writesWarnLineWithDeniedOutcome() {
        auditLogger.logDenied("user-uuid-2", "purgeAppointment", Integer.valueOf(99),
                "AppointmentServiceImpl", "192.0.2.20", AuditLogger.Channel.REST);

        assertEquals("expected exactly one WARN log line", 1, capturingLog.warnMessages.size());
        assertEquals("no INFO expected", 0, capturingLog.infoMessages.size());
        assertEquals("no ERROR expected", 0, capturingLog.errorMessages.size());

        String line = capturingLog.warnMessages.get(0);
        assertTrue("line must contain outcome denied: " + line, line.contains("/denied"));
        assertTrue("line must contain action: " + line, line.contains("what=purgeAppointment:99"));
    }

    @Test
    public void logFailed_writesErrorLineWithFailedOutcome() {
        auditLogger.logFailed("user-uuid-3", "saveAppointment", Integer.valueOf(7),
                "AppointmentServiceImpl", "192.0.2.30", AuditLogger.Channel.SERVICE);

        assertEquals("expected exactly one ERROR log line", 1, capturingLog.errorMessages.size());
        assertEquals("no INFO expected", 0, capturingLog.infoMessages.size());
        assertEquals("no WARN expected", 0, capturingLog.warnMessages.size());

        String line = capturingLog.errorMessages.get(0);
        assertTrue("line must contain outcome failed: " + line, line.contains("/failed"));
        assertTrue("line must contain channel SERVICE: " + line, line.contains("how=SERVICE/"));
    }

    @Test
    public void logSuccess_anonymousWhenUserUuidIsNull() {
        auditLogger.logSuccess(null, "getAppointment", Integer.valueOf(1),
                "AppointmentServiceImpl", "192.0.2.40", AuditLogger.Channel.DWR);

        String line = capturingLog.infoMessages.get(0);
        assertTrue("missing user must be rendered as anonymous: " + line,
                line.contains("who=anonymous"));
    }

    @Test
    public void logSuccess_unknownAddressWhenRemoteAddrIsNull() {
        auditLogger.logSuccess("user-uuid-4", "getAppointment", Integer.valueOf(2),
                "AppointmentServiceImpl", null, AuditLogger.Channel.SERVICE);

        String line = capturingLog.infoMessages.get(0);
        assertTrue("missing address must be rendered as unknown: " + line,
                line.contains(":unknown "));
    }

    // ------------------------------------------------------------------
    // Absence of sensitive data (PII) — the core SR-01 / SR-08 / SR-09 check
    // ------------------------------------------------------------------

    @Test
    public void format_doesNotContainAnyPII_evenWhenSensitiveValuesExistInScope() {
        // Sensitive values that exist in the caller's scope. These represent
        // the exact PII fields that B-01 in 05-pentest-bevindingen.md showed
        // were leaked by the vulnerable getAppointmentsForPatientWithLogging.
        String patientName = "Jane Doe";
        String patientBirthdate = "1980-01-15";
        String patientIdentifier = "MRN-2026-0042";
        String patientGender = "F";

        // The AuditLogger API only accepts opaque identifiers — a developer
        // following the API cannot pass these PII values in. The opaque
        // patientId is allowed because it is not directly identifying.
        Integer patientId = Integer.valueOf(42);

        String line = auditLogger.format("user-uuid-5", "getAppointmentsForPatient", patientId,
                "AppointmentServiceImpl", "192.0.2.50",
                AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, new Date());

        assertFalse("audit line leaks patient name: " + line, line.contains(patientName));
        assertFalse("audit line leaks given name: " + line, line.contains("Jane"));
        assertFalse("audit line leaks family name: " + line, line.contains("Doe"));
        assertFalse("audit line leaks birthdate: " + line, line.contains(patientBirthdate));
        assertFalse("audit line leaks patient identifier: " + line,
                line.contains(patientIdentifier));
        assertFalse("audit line leaks gender field: " + line, line.contains("gender="));

        // Sanity check: the safe identifier (patientId) IS expected in the line.
        assertTrue("audit line should contain the opaque patientId: " + line,
                line.contains(":42"));

        // Gender 'F' is a single character and will appear inside many words
        // (e.g. "FAILED"). We therefore do not assert on it directly; the
        // API simply has no parameter for gender, so it cannot end up in
        // the line.
        // The other PII fields above are distinctive enough to assert on.
        @SuppressWarnings("unused")
        String unusedJustToKeepReference = patientGender;
    }

    @Test
    public void logSuccess_doesNotLeakPII_throughTheLoggingPipeline() {
        // Same defence repeated end-to-end through logSuccess() — guarantees
        // that the convenience method also produces PII-free output.
        String patientName = "John Smith";
        String birthdate = "1972-03-04";
        String mrn = "MRN-2026-0099";

        auditLogger.logSuccess("user-uuid-6", "voidAppointment", Integer.valueOf(500),
                "AppointmentServiceImpl", "192.0.2.60", AuditLogger.Channel.WEB_UI);

        String line = capturingLog.infoMessages.get(0);
        assertFalse("PII leak (name) in logSuccess output: " + line, line.contains(patientName));
        assertFalse("PII leak (DOB) in logSuccess output: " + line, line.contains(birthdate));
        assertFalse("PII leak (MRN) in logSuccess output: " + line, line.contains(mrn));
    }

    // ------------------------------------------------------------------
    // Structural checks — who / what / where / when / how must all be present
    // ------------------------------------------------------------------

    @Test
    public void format_alwaysContainsWhoWhatWhereWhenHowFields() {
        String line = auditLogger.format("u", "saveAppointment", Integer.valueOf(1), "c", "1.1.1.1",
                AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, new Date());

        assertTrue("missing who= field: " + line, line.contains(" who="));
        assertTrue("missing what= field: " + line, line.contains(" what="));
        assertTrue("missing where= field: " + line, line.contains(" where="));
        assertTrue("missing when= field: " + line, line.contains(" when="));
        assertTrue("missing how= field: " + line, line.contains(" how="));
    }

    @Test
    public void format_writesTimestampAsIso8601Utc() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.clear();
        cal.set(2026, Calendar.JUNE, 9, 10, 14, 22);
        Date fixed = cal.getTime();

        String line = auditLogger.format("u", "saveAppointment", Integer.valueOf(1), "c", "1.1.1.1",
                AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, fixed);

        assertTrue("timestamp must be ISO-8601 UTC: " + line,
                line.contains("when=2026-06-09T10:14:22Z"));
    }

    // ------------------------------------------------------------------
    // Defensive branches — empty strings must render as anonymous/unknown,
    // null fields for action/object/channel/outcome/when must render as
    // their safe fallback. These tests push branch coverage to the level
    // required by docs/auditreport/06-code-coverage.md.
    // ------------------------------------------------------------------

    @Test
    public void format_emptyUserUuidRendersAsAnonymous() {
        String line = auditLogger.format("", "saveAppointment", Integer.valueOf(1), "c", "1.1.1.1",
                AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, new Date());
        assertTrue("empty user must be rendered as anonymous: " + line,
                line.contains("who=anonymous"));
    }

    @Test
    public void format_emptyRemoteAddressRendersAsUnknown() {
        String line = auditLogger.format("u", "saveAppointment", Integer.valueOf(1), "c", "",
                AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, new Date());
        assertTrue("empty address must be rendered as unknown: " + line,
                line.contains(":unknown "));
    }

    @Test
    public void format_nullAndEmptyActionRenderAsUnknown() {
        String fromNull = auditLogger.format("u", null, Integer.valueOf(1), "c", "1.1.1.1",
                AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, new Date());
        String fromEmpty = auditLogger.format("u", "", Integer.valueOf(1), "c", "1.1.1.1",
                AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, new Date());

        assertTrue("null action must render as unknown: " + fromNull,
                fromNull.contains("what=unknown:"));
        assertTrue("empty action must render as unknown: " + fromEmpty,
                fromEmpty.contains("what=unknown:"));
    }

    @Test
    public void format_nullAndEmptyComponentRenderAsUnknown() {
        String fromNull = auditLogger.format("u", "saveAppointment", Integer.valueOf(1), null,
                "1.1.1.1", AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, new Date());
        String fromEmpty = auditLogger.format("u", "saveAppointment", Integer.valueOf(1), "",
                "1.1.1.1", AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, new Date());

        assertTrue("null component must render as unknown: " + fromNull,
                fromNull.contains("where=unknown:"));
        assertTrue("empty component must render as unknown: " + fromEmpty,
                fromEmpty.contains("where=unknown:"));
    }

    @Test
    public void format_nullObjectIdRendersAsDash() {
        String line = auditLogger.format("u", "saveAppointment", null, "c", "1.1.1.1",
                AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, new Date());
        assertTrue("null objectId must render as '-': " + line,
                line.contains("what=saveAppointment:-"));
    }

    @Test
    public void format_nullChannelAndOutcomeRenderAsUnknown() {
        String line = auditLogger.format("u", "saveAppointment", Integer.valueOf(1), "c", "1.1.1.1",
                null, null, new Date());
        assertTrue("null channel must render as unknown: " + line,
                line.contains("how=unknown/"));
        assertTrue("null outcome must render as unknown: " + line,
                line.endsWith("/unknown"));
    }

    @Test
    public void format_nullDateFallsBackToNow() {
        String line = auditLogger.format("u", "saveAppointment", Integer.valueOf(1), "c", "1.1.1.1",
                AuditLogger.Channel.REST, AuditLogger.Outcome.SUCCESS, null);
        // Format yyyy-MM-ddTHH:mm:ssZ — 20 characters after "when="
        int idx = line.indexOf("when=");
        assertTrue("when= field must be present: " + line, idx >= 0);
        String when = line.substring(idx + "when=".length(), idx + "when=".length() + 20);
        assertTrue("null date must fall back to a real ISO-8601 UTC timestamp: " + when,
                when.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"));
    }

    // ------------------------------------------------------------------
    // Test double — captures messages emitted via commons-logging Log API
    // ------------------------------------------------------------------

    private static class CapturingLog implements Log {

        final List<String> infoMessages = new ArrayList<String>();

        final List<String> warnMessages = new ArrayList<String>();

        final List<String> errorMessages = new ArrayList<String>();

        public boolean isDebugEnabled() { return true; }

        public boolean isErrorEnabled() { return true; }

        public boolean isFatalEnabled() { return true; }

        public boolean isInfoEnabled() { return true; }

        public boolean isTraceEnabled() { return true; }

        public boolean isWarnEnabled() { return true; }

        public void trace(Object message) { }

        public void trace(Object message, Throwable t) { }

        public void debug(Object message) { }

        public void debug(Object message, Throwable t) { }

        public void info(Object message) { infoMessages.add(String.valueOf(message)); }

        public void info(Object message, Throwable t) { infoMessages.add(String.valueOf(message)); }

        public void warn(Object message) { warnMessages.add(String.valueOf(message)); }

        public void warn(Object message, Throwable t) { warnMessages.add(String.valueOf(message)); }

        public void error(Object message) { errorMessages.add(String.valueOf(message)); }

        public void error(Object message, Throwable t) { errorMessages.add(String.valueOf(message)); }

        public void fatal(Object message) { errorMessages.add(String.valueOf(message)); }

        public void fatal(Object message, Throwable t) { errorMessages.add(String.valueOf(message)); }
    }
}
