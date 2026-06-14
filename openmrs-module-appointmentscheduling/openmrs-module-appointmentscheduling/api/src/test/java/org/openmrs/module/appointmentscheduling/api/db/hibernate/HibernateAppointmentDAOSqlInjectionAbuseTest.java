package org.openmrs.module.appointmentscheduling.api.db.hibernate;

import org.junit.Assert;
import org.junit.Test;

public class HibernateAppointmentDAOSqlInjectionAbuseTest {

    @Test
    public void searchAppointmentsByPatientName_shouldShowHqlInjectionPayloadBeforeFix() {
        String patientName = "' OR '1'='1";

        String hql = "from Appointment ap where ap.visit.patient.personName.givenName = '"
                + patientName + "' or ap.visit.patient.personName.familyName = '"
                + patientName + "'";

        System.out.println("B-01 abuse payload = " + patientName);
        System.out.println("B-01 vulnerable HQL = " + hql);

        Assert.assertTrue(
                "Payload moet als HQL-code in de query terechtkomen",
                hql.contains("OR '1'='1")
        );
    }
}