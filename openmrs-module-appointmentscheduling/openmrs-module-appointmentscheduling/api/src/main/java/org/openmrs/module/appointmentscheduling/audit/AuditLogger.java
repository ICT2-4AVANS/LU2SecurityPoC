package org.openmrs.module.appointmentscheduling.audit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PII-safe audit logger for the Appointment Scheduling Module.
 *
 * Emits structured audit lines in the format prescribed by the logging
 * gap-analysis (docs/auditreport/02gapanalyselogging.md, section 7.2):
 *
 *   [AUDIT] who=&lt;uuid&gt; what=&lt;action&gt;:&lt;objectId&gt; where=&lt;component&gt;:&lt;remoteAddr&gt;
 *           when=&lt;iso8601-utc&gt; how=&lt;channel&gt;/&lt;outcome&gt;
 *
 * The API only accepts opaque identifiers (UUIDs, integer object IDs, class
 * names, IP addresses). It does not accept Patient names, birthdates,
 * identifiers or any other free-form field, which prevents PII leakage by
 * design (security backlog SR-01 / SR-08 / SR-09).
 */
public class AuditLogger {

    public enum Outcome {
        SUCCESS, DENIED, FAILED
    }

    public enum Channel {
        REST, DWR, WEB_UI, SERVICE, TASK
    }

    private static final String UNKNOWN = "unknown";

    private static final String ANONYMOUS = "anonymous";

    private final Log log;

    public AuditLogger() {
        this(LogFactory.getLog(AuditLogger.class));
    }

    public AuditLogger(Log log) {
        this.log = log;
    }

    public String format(String userUuid, String action, Integer objectId, String component,
            String remoteAddr, Channel channel, Outcome outcome, Date when) {

        String safeUser = (userUuid == null || userUuid.length() == 0) ? ANONYMOUS : userUuid;
        String safeAction = (action == null || action.length() == 0) ? UNKNOWN : action;
        String safeObject = (objectId == null) ? "-" : objectId.toString();
        String safeComponent = (component == null || component.length() == 0) ? UNKNOWN : component;
        String safeAddr = (remoteAddr == null || remoteAddr.length() == 0) ? UNKNOWN : remoteAddr;
        String safeWhen = formatIso8601(when != null ? when : new Date());
        String safeChannel = (channel == null) ? UNKNOWN : channel.name();
        String safeOutcome = (outcome == null) ? UNKNOWN : outcome.name().toLowerCase();

        return "[AUDIT] who=" + safeUser
                + " what=" + safeAction + ":" + safeObject
                + " where=" + safeComponent + ":" + safeAddr
                + " when=" + safeWhen
                + " how=" + safeChannel + "/" + safeOutcome;
    }

    public void logSuccess(String userUuid, String action, Integer objectId, String component,
            String remoteAddr, Channel channel) {
        log.info(format(userUuid, action, objectId, component, remoteAddr, channel,
                Outcome.SUCCESS, new Date()));
    }

    public void logDenied(String userUuid, String action, Integer objectId, String component,
            String remoteAddr, Channel channel) {
        log.warn(format(userUuid, action, objectId, component, remoteAddr, channel,
                Outcome.DENIED, new Date()));
    }

    public void logFailed(String userUuid, String action, Integer objectId, String component,
            String remoteAddr, Channel channel) {
        log.error(format(userUuid, action, objectId, component, remoteAddr, channel,
                Outcome.FAILED, new Date()));
    }

    private String formatIso8601(Date d) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return fmt.format(d);
    }
}
