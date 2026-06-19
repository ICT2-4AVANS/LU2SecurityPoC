package org.openmrs.module.appointmentscheduling.rest.controller;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.module.appointmentscheduling.Appointment;
import org.openmrs.module.appointmentscheduling.web.controller.AppointmentFormController;

public class AppointmentFormControllerIdorFixTest {

    @Test
    public void validateAppointmentPatientAccess_shouldAllowAppointmentForSamePatient() {
        AppointmentFormController controller = new AppointmentFormController();

        Patient patient = new Patient();
        patient.setPatientId(1);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);

        controller.validateAppointmentPatientAccess(appointment, 1);
    }

    @Test(expected = APIAuthenticationException.class)
    public void validateAppointmentPatientAccess_shouldBlockAppointmentForDifferentPatient() {
        AppointmentFormController controller = new AppointmentFormController();

        Patient patientFromAppointment = new Patient();
        patientFromAppointment.setPatientId(2);

        Appointment appointment = new Appointment();
        appointment.setPatient(patientFromAppointment);

        controller.validateAppointmentPatientAccess(appointment, 1);
    }
}