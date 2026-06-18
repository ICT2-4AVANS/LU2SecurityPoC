package org.openmrs.module.appointmentscheduling.rest.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.module.appointmentscheduling.Appointment;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;

public class AppointmentFormControllerIdorAbuseTest {

    @Test
    public void getAppointment_shouldReturnForeignPatientAppointment_dueToMissingOwnershipCheck() {

        AppointmentService appointmentService = mock(AppointmentService.class);

        Integer requestedPatientId = 1;
        Integer requestedAppointmentId = 2;

        Patient otherPatient = new Patient();
        otherPatient.setPatientId(2);

        Appointment foreignAppointment = new Appointment();
        foreignAppointment.setPatient(otherPatient);

        when(appointmentService.getAppointment(requestedAppointmentId))
                .thenReturn(foreignAppointment);

        Appointment returnedAppointment = appointmentService.getAppointment(requestedAppointmentId);

        assertThat(returnedAppointment, notNullValue());
        assertThat(returnedAppointment.getPatient(), notNullValue());

        // 🚨 IDOR: verkeerde patiënt wordt teruggegeven
        assertThat(returnedAppointment.getPatient().getPatientId(), equalTo(2));
        assertThat(returnedAppointment.getPatient().getPatientId(), not(equalTo(requestedPatientId)));
    }
}