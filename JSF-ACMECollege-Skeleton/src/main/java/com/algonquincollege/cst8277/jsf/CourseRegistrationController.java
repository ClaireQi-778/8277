/********************************************************************************************************
 * File:  CourseRegistrationController.java
 * Course Materials CST 8277
 * 
 * @author (original) Mike Norman
 * @author Teddy Yap
 *
 */
package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import com.algonquincollege.cst8277.utility.MyConstants;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.rest.resource.MyObjectMapperProvider;

@Named("courseRegistrationController")
@SessionScoped
public class CourseRegistrationController implements Serializable, MyConstants {
    private static final long serialVersionUID = 1L;

    @Inject
    protected FacesContext facesContext;
    @Inject
    protected ExternalContext externalContext;
    @Inject
    protected ServletContext sc;
    @Inject
    protected LoginBean loginBean;
    
    @Inject
    protected StudentController studentController;
    
    @Inject
    protected CourseController courseController;
    
    @Inject
    protected ProfessorController professorController;

    protected int selectedStudentId;
    protected int selectedCourseId;
    protected int selectedProfessorId;
    protected int year;
    protected String semester;
    protected String letterGrade;
    protected List<String> listOfLetterGrades;
    
    static URI uri;
    static HttpAuthenticationFeature auth;
    protected Client client;
    protected WebTarget webTarget;

    public CourseRegistrationController() {
        super();
    }
    
    @PostConstruct
    public void initialize() {
        uri = UriBuilder
                .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
                .scheme(HTTP_SCHEMA)
                .host(HOST)
                .port(PORT)
                .build();
        
        auth = HttpAuthenticationFeature.basic(loginBean.getUsername(), loginBean.getPassword());
        
        client = ClientBuilder.newClient(
                new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        
        webTarget = client.target(uri);
    }

    public int getSelectedStudentId() {
        return selectedStudentId;
    }

    public void setSelectedStudentId(int selectedStudentId) {
        this.selectedStudentId = selectedStudentId;
    }

    public int getSelectedCourseId() {
        return selectedCourseId;
    }

    public void setSelectedCourseId(int selectedCourseId) {
        this.selectedCourseId = selectedCourseId;
    }

    public int getSelectedProfessorId() {
        return selectedProfessorId;
    }

    public void setSelectedProfessorId(int selectedProfessorId) {
        this.selectedProfessorId = selectedProfessorId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }

    public String registerStudentForCourse() {
        CourseRegistration registration = new CourseRegistration();
        
        Student student = new Student();
        student.setId(selectedStudentId);
        registration.setStudent(student);
        
        Course course = new Course();
        course.setId(selectedCourseId);
        registration.setCourse(course);
        
        registration.setYear(year);
        registration.setSemester(semester);
        
        Response response = webTarget
                .register(auth)
                .path(COURSEREGISTRATION_RESOURCE_NAME)
                .request()
                .post(Entity.json(registration));
        
        return MAIN_PAGE_REDIRECT;
    }

    public String assignProfessor() {
        Professor professor = new Professor();
        professor.setId(selectedProfessorId);
        
        Response response = webTarget
                .register(auth)
                .path(COURSEREGISTRATION_RESOURCE_NAME + "/student/" + selectedStudentId + "/course/" + selectedCourseId)
                .request()
                .put(Entity.json(professor));
        
        return MAIN_PAGE_REDIRECT;
    }

    public String assignGrade() {
        Response response = webTarget
                .register(auth)
                .path(COURSEREGISTRATION_RESOURCE_NAME + "/student/" + selectedStudentId + "/course/" + selectedCourseId)
                .request()
                .put(Entity.text(letterGrade));
        
        return MAIN_PAGE_REDIRECT;
    }

    public List<String> getLetterGrades() {
        if (listOfLetterGrades == null) {
            Response response = webTarget
                    .register(auth)
                    .path(COURSEREGISTRATION_RESOURCE_NAME + LETTERGRADE_RESOURCE_PATH)
                    .request()
                    .get();
            listOfLetterGrades = response.readEntity(new GenericType<List<String>>(){});
        }
        return listOfLetterGrades;
    }
}
