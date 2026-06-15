package org.openmrs.module.appointmentscheduling.web.controller;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ControllerAuthorizationTest {

    private static final String CONTROLLER_PATH =
            "src/main/java/org/openmrs/module/appointmentscheduling/web/controller/";

    @Test
    public void allAppointmentSchedulingControllers_shouldHaveAuthorizedAnnotation() throws Exception {
        assertControllerHasPrivilege("AppointmentBlockCalendarController.java",
                "AppointmentUtils.PRIV_VIEW_APPOINTMENT_BLOCKS",
                "AppointmentUtils.PRIV_MANAGE_APPOINTMENT_BLOCKS");

        assertControllerHasPrivilege("AppointmentBlockFormController.java",
                "AppointmentUtils.PRIV_MANAGE_APPOINTMENT_BLOCKS");

        assertControllerHasPrivilege("AppointmentBlockListController.java",
                "AppointmentUtils.PRIV_VIEW_APPOINTMENT_BLOCKS",
                "AppointmentUtils.PRIV_MANAGE_APPOINTMENT_BLOCKS");

        assertControllerHasPrivilege("AppointmentDailyCountController.java",
                "AppointmentUtils.PRIV_VIEW_APPOINTMENTS_STATISTICS");

        assertControllerHasPrivilege("AppointmentFormController.java",
                "AppointmentUtils.PRIV_SCHEDULE_APPOINTMENTS");

        assertControllerHasPrivilege("AppointmentListController.java",
                "AppointmentUtils.PRIV_VIEW_APPOINTMENTS", "AppointmentUtils.PRIV_UPDATE_APPOINTMENT_STATES");

        assertControllerHasPrivilege("AppointmentRequisitionController.java",
                "AppointmentUtils.PRIV_REQUEST_APPOINTMENTS");

        assertControllerHasPrivilege("AppointmentSettingsFormController.java",
                "AppointmentUtils.PRIV_MANAGE_APPOINTMENTS_SETTINGS");

        assertControllerHasPrivilege("AppointmentsPortletController.java",
                "AppointmentUtils.PRIV_VIEW_APPOINTMENT_HISTORY_TAB");

        assertControllerHasPrivilege("AppointmentStatisticsFormController.java",
                "AppointmentUtils.PRIV_VIEW_APPOINTMENTS_STATISTICS");

        assertControllerHasPrivilege("AppointmentTypeFormController.java",
                "AppointmentUtils.PRIV_MANAGE_APPOINTMENT_TYPES");

        assertControllerHasPrivilege("AppointmentTypeListController.java",
                "AppointmentUtils.PRIV_VIEW_APPOINTMENT_TYPES");

        assertControllerHasPrivilege("PatientDashboardAppointmentExtController.java",
                "AppointmentUtils.PRIV_UPDATE_APPOINTMENT_STATES");
    }

    private void assertControllerHasPrivilege(String controllerFile, String... expectedPrivileges) throws Exception {
        File file = new File(CONTROLLER_PATH + controllerFile);
        String source = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        Assert.assertTrue(
                controllerFile + " moet @Authorized gebruiken",
                source.contains("@Authorized")
        );

        Assert.assertTrue(
                controllerFile + " moet de OpenMRS Authorized import gebruiken",
                source.contains("import org.openmrs.annotation.Authorized;")
        );

        for (String privilege : expectedPrivileges) {
            Assert.assertTrue(
                    controllerFile + " mist privilege: " + privilege,
                    source.contains(privilege)
            );
        }
    }
}