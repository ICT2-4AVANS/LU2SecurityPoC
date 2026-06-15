package org.openmrs.module.appointmentscheduling.api.db.hibernate;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class HibernateAppointmentDAOSqlInjectionAbuseTest {

    @Test
    public void searchAppointmentsByPatientName_shouldUseParameterizedHqlQuery() throws Exception {
        File daoFile = new File(
                "src/main/java/org/openmrs/module/appointmentscheduling/api/db/hibernate/HibernateAppointmentDAO.java"
        );

        String source = new String(Files.readAllBytes(daoFile.toPath()), StandardCharsets.UTF_8);

        Assert.assertTrue(
                "De HQL-query moet een named parameter gebruiken",
                source.contains(":patientName")
        );

        Assert.assertTrue(
                "De patientName moet via setParameter worden gebonden",
                source.contains(".setParameter(\"patientName\", patientName)")
        );

        Assert.assertFalse(
                "patientName mag niet meer direct met string-concatenatie in de query worden geplakt",
                source.contains("+ patientName +")
        );
    }
}