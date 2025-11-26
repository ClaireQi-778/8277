package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.CourseRegistration;

@Path("courseregistration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseRegistrationResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getCourseRegistrations() {
        LOG.debug("retrieving all course registrations ...");
        List<CourseRegistration> courseRegistrations = service.getAllCourseRegistrations();
        return Response.ok(courseRegistrations).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addCourseRegistration(CourseRegistration newCourseRegistration) {
        CourseRegistration created = service.persistCourseRegistration(newCourseRegistration);
        return Response.ok(created).build();
    }

    @PUT
    @Path("{studentId}/{courseId}/professor/{professorId}")
    @RolesAllowed({ADMIN_ROLE})
    public Response assignProfessor(@PathParam("studentId") int studentId, 
                                    @PathParam("courseId") int courseId,
                                    @PathParam("professorId") int professorId) {
        CourseRegistration updated = service.assignProfessorToCourseRegistration(studentId, courseId, professorId);
        return Response.ok(updated).build();
    }

    @PUT
    @Path("{studentId}/{courseId}/grade/{grade}")
    @RolesAllowed({ADMIN_ROLE})
    public Response assignGrade(@PathParam("studentId") int studentId, 
                                @PathParam("courseId") int courseId,
                                @PathParam("grade") String grade) {
        CourseRegistration updated = service.assignGradeToCourseRegistration(studentId, courseId, grade);
        return Response.ok(updated).build();
    }
}
