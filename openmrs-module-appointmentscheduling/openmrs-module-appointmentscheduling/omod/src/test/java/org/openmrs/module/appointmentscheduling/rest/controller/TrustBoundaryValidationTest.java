package org.openmrs.module.appointmentscheduling.rest.controller;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class TrustBoundaryValidationTest {

    private static final String CONTROLLER_PATH =
            "src/main/java/org/openmrs/module/appointmentscheduling/web/controller/";

    @Test
    public void appointmentBlockListController_shouldValidateRequestInputBeforeWritingToSession() throws Exception {
        String source = readController("AppointmentBlockListController.java");

        assertContains(source, "isAllowedListAction(action)");
        assertContains(source, "fromDate == null || toDate == null || !fromDate.before(toDate)");
        assertContains(source, "isValidLocation(location)");
        assertContains(source, "isValidProvider(providerId)");
        assertContains(source, "isValidAppointmentType(appointmentService, appointmentTypeId)");
        assertContains(source, "isValidAppointmentBlock(appointmentService, appointmentBlockId)");

        assertContains(source, "private boolean isAllowedListAction(String action)");
        assertContains(source, "\"changeToCalendarView\".equals(action)");
        assertContains(source, "\"void\".equals(action)");
        assertContains(source, "\"purge\".equals(action)");

        assertValidationBeforeSessionWrite(source,
                "isAllowedListAction(action)",
                "httpSession.setAttribute(\"chosenLocation\", validatedLocation)");

        assertValidationBeforeSessionWrite(source,
                "isValidAppointmentBlock(appointmentService, appointmentBlockId)",
                "httpSession.setAttribute(\"chosenProvider\", providerId)");

        assertValidationBeforeSessionWrite(source,
                "isValidAppointmentBlock(appointmentService, appointmentBlockId)",
                "httpSession.setAttribute(\"chosenType\", appointmentTypeId)");
    }

    @Test
    public void appointmentBlockCalendarController_shouldValidateRequestInputBeforeWritingToSession() throws Exception {
        String source = readController("AppointmentBlockCalendarController.java");

        assertContains(source, "isAllowedCalendarAction(action)");
        assertContains(source, "fromDate == null || toDate == null || fromDate < 0 || toDate < 0 || fromDate > toDate");
        assertContains(source, "\"editAppointmentBlock\".equals(action) && appointmentBlockId == null");
        assertContains(source, "isValidLocation(location)");
        assertContains(source, "isValidProvider(providerId)");
        assertContains(source, "isValidAppointmentType(appointmentService, appointmentTypeId)");
        assertContains(source, "isValidAppointmentBlock(appointmentService, appointmentBlockId)");

        assertContains(source, "private boolean isAllowedCalendarAction(String action)");
        assertContains(source, "\"addNewAppointmentBlock\".equals(action)");
        assertContains(source, "\"changeToTableView\".equals(action)");
        assertContains(source, "\"editAppointmentBlock\".equals(action)");

        assertValidationBeforeSessionWrite(source,
                "isAllowedCalendarAction(action)",
                "httpSession.setAttribute(\"chosenLocation\", validatedLocation)");

        assertValidationBeforeSessionWrite(source,
                "isValidAppointmentBlock(appointmentService, appointmentBlockId)",
                "httpSession.setAttribute(\"chosenProvider\", providerId)");

        assertValidationBeforeSessionWrite(source,
                "isValidAppointmentBlock(appointmentService, appointmentBlockId)",
                "httpSession.setAttribute(\"chosenType\", appointmentTypeId)");
    }

    private String readController(String controllerFile) throws Exception {
        File file = new File(CONTROLLER_PATH + controllerFile);
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    private void assertContains(String source, String expected) {
        Assert.assertTrue(
                "Expected source to contain: " + expected,
                source.contains(expected)
        );
    }

    private void assertValidationBeforeSessionWrite(String source, String validation, String sessionWrite) {
        int validationIndex = source.indexOf(validation);
        int sessionWriteIndex = source.indexOf(sessionWrite);

        Assert.assertTrue("Validation not found: " + validation, validationIndex >= 0);
        Assert.assertTrue("Session write not found: " + sessionWrite, sessionWriteIndex >= 0);
        Assert.assertTrue(
                "Validation must happen before session write",
                validationIndex < sessionWriteIndex
        );
    }
}