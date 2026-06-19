package org.openmrs.module.appointmentscheduling;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;

/**
 * B-02 — Verifieert dat AppointmentActivator geen hardcoded credentials bevat.
 * NEN-7510 A.8.5 — Authenticatie-informatie mag niet in broncode staan.
 */
public class AppointmentActivatorHardcodedCredentialsTest {

    private static final String KNOWN_PASSWORD = "Appt@Export2021!";
    private static final String KNOWN_JDBC_URL  = "jdbc:mysql://hl7-reports.hospital.internal";

    @Test
    public void activator_magGeenHardcodedWachtwoordBevatten() {
        for (Field field : AppointmentActivator.class.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getType() == String.class) {
                try {
                    String value = (String) field.get(null);
                    if (value != null) {
                        assertFalse(
                            "Hardcoded wachtwoord gevonden in veld: " + field.getName(),
                            value.contains(KNOWN_PASSWORD)
                        );
                        assertFalse(
                            "Hardcoded JDBC URL gevonden in veld: " + field.getName(),
                            value.contains(KNOWN_JDBC_URL)
                        );
                    }
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    // niet-statische velden overslaan
                }
            }
        }
    }

    @Test
    public void activator_gebruiktGlobalPropertySleutels() {
        assertFalse(AppointmentActivator.GP_HL7_EXPORT_HOST.isEmpty());
        assertFalse(AppointmentActivator.GP_HL7_EXPORT_USER.isEmpty());
        assertFalse(AppointmentActivator.GP_HL7_EXPORT_PASSWORD.isEmpty());
    }
}
