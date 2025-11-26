/********************************************************************************************************
 * File:  ACMECollegeService.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * */
package com.algonquincollege.cst8277.ejb;

import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_KEY_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_SALT_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PREFIX;
import static com.algonquincollege.cst8277.utility.MyConstants.PARAM1;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_KEY_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_SALT_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.PU_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.SecurityRole;
import com.algonquincollege.cst8277.entity.SecurityUser;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;

@SuppressWarnings("unused")
@Singleton
public class ACMECollegeService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String READ_ALL_PROGRAMS = "SELECT name FROM program";
    
    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Student> getAllStudents() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Student> cq = cb.createQuery(Student.class);
        cq.select(cq.from(Student.class));
        return em.createQuery(cq).getResultList();
    }

    public Student getStudentById(int id) {
        return em.find(Student.class, id);
    }

    /**
     * Persist a new student and automatically create their user account.
     * This happens in ONE transaction to prevent errors and orphaned records.
     */
    @Transactional
    public Student persistStudent(Student newStudent) {
        em.persist(newStudent);
        // Now that student is managed/persisted, creating the user will work correctly
        buildUserForNewStudent(newStudent);
        return newStudent;
    }

    @Transactional
    public void buildUserForNewStudent(Student newStudent) {
        SecurityUser userForNewStudent = new SecurityUser();
        userForNewStudent.setUsername(
            DEFAULT_USER_PREFIX + "_" + newStudent.getFirstName() + "." + newStudent.getLastName());
        
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        
        userForNewStudent.setPwHash(pwHash);
        userForNewStudent.setStudent(newStudent);
        
        SecurityRole userRole = em.createNamedQuery(SecurityRole.SECURITY_ROLE_BY_NAME, SecurityRole.class)
                                    .setParameter(PARAM1, USER_ROLE)
                                    .getSingleResult();
        
        userForNewStudent.getRoles().add(userRole);
        userRole.getUsers().add(userForNewStudent);
        
        em.persist(userForNewStudent);
    }

    @Transactional
    public Student updateStudentById(int id, Student studentWithUpdates) {
        Student studentToBeUpdated = getStudentById(id);
        if (studentToBeUpdated != null) {
            em.refresh(studentToBeUpdated);
            // Update specific fields to ensure managed entity state is preserved
            studentToBeUpdated.setFirstName(studentWithUpdates.getFirstName());
            studentToBeUpdated.setLastName(studentWithUpdates.getLastName());
            studentToBeUpdated.setEmail(studentWithUpdates.getEmail());
            studentToBeUpdated.setPhone(studentWithUpdates.getPhone());
            studentToBeUpdated.setProgram(studentWithUpdates.getProgram());
            em.merge(studentToBeUpdated);
            em.flush();
        }
        return studentToBeUpdated;
    }

    @Transactional
    public Student deleteStudentById(int id) {
        Student student = getStudentById(id);
        if (student != null) {
            em.refresh(student);
            TypedQuery<SecurityUser> findUser = em.createNamedQuery("SecurityUser.userByStudentId", SecurityUser.class)
                                                  .setParameter(PARAM1, id);
            // Use getResultList to safely check for user existence
            List<SecurityUser> sUsers = findUser.getResultList();
            if (!sUsers.isEmpty()) {
                em.remove(sUsers.get(0));
            }
            em.remove(student);
        }
        return student;
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getAllPrograms() {
        List<String> programs = new ArrayList<>();
        try {
            programs = (List<String>) em.createNativeQuery(READ_ALL_PROGRAMS).getResultList();
        }
        catch (Exception e) {
        }
        return programs;
    }

    // --- Course CRUD ---
    public List<Course> getAllCourses() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Course> cq = cb.createQuery(Course.class);
        cq.select(cq.from(Course.class));
        return em.createQuery(cq).getResultList();
    }

    public Course getCourseById(int id) {
        return em.find(Course.class, id);
    }

    @Transactional
    public Course persistCourse(Course newCourse) {
        em.persist(newCourse);
        return newCourse;
    }

    @Transactional
    public Course updateCourseById(int id, Course courseWithUpdates) {
        Course courseToBeUpdated = getCourseById(id);
        if (courseToBeUpdated != null) {
            em.refresh(courseToBeUpdated);
            courseToBeUpdated.setCourseCode(courseWithUpdates.getCourseCode());
            courseToBeUpdated.setCourseTitle(courseWithUpdates.getCourseTitle());
            courseToBeUpdated.setCreditUnits(courseWithUpdates.getCreditUnits());
            courseToBeUpdated.setOnline(courseWithUpdates.getOnline());
            em.merge(courseToBeUpdated);
            em.flush();
        }
        return courseToBeUpdated;
    }

    @Transactional
    public Course deleteCourseById(int id) {
        Course course = getCourseById(id);
        if (course != null) {
            em.refresh(course);
            em.remove(course);
        }
        return course;
    }

    // --- Professor CRUD ---
    public List<Professor> getAllProfessors() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Professor> cq = cb.createQuery(Professor.class);
        cq.select(cq.from(Professor.class));
        return em.createQuery(cq).getResultList();
    }

    public Professor getProfessorById(int id) {
        return em.find(Professor.class, id);
    }

    @Transactional
    public Professor persistProfessor(Professor newProfessor) {
        em.persist(newProfessor);
        return newProfessor;
    }

    @Transactional
    public Professor updateProfessorById(int id, Professor professorWithUpdates) {
        Professor professorToBeUpdated = getProfessorById(id);
        if (professorToBeUpdated != null) {
            em.refresh(professorToBeUpdated);
            professorToBeUpdated.setFirstName(professorWithUpdates.getFirstName());
            professorToBeUpdated.setLastName(professorWithUpdates.getLastName());
            professorToBeUpdated.setDegree(professorWithUpdates.getDegree());
            em.merge(professorToBeUpdated);
            em.flush();
        }
        return professorToBeUpdated;
    }

    @Transactional
    public Professor deleteProfessorById(int id) {
        Professor professor = getProfessorById(id);
        if (professor != null) {
            em.refresh(professor);
            em.remove(professor);
        }
        return professor;
    }

    // --- StudentClub CRUD ---
    public List<StudentClub> getAllStudentClubs() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<StudentClub> cq = cb.createQuery(StudentClub.class);
        cq.select(cq.from(StudentClub.class));
        return em.createQuery(cq).getResultList();
    }

    public StudentClub getStudentClubById(int id) {
        return em.find(StudentClub.class, id);
    }

    @Transactional
    public StudentClub persistStudentClub(StudentClub newStudentClub) {
        em.persist(newStudentClub);
        return newStudentClub;
    }

    @Transactional
    public StudentClub updateStudentClubById(int id, StudentClub studentClubWithUpdates) {
        StudentClub studentClubToBeUpdated = getStudentClubById(id);
        if (studentClubToBeUpdated != null) {
            em.refresh(studentClubToBeUpdated);
            studentClubToBeUpdated.setName(studentClubWithUpdates.getName());
            studentClubToBeUpdated.setDesc(studentClubWithUpdates.getDesc());
            em.merge(studentClubToBeUpdated);
            em.flush();
        }
        return studentClubToBeUpdated;
    }

    @Transactional
    public StudentClub deleteStudentClubById(int id) {
        StudentClub studentClub = getStudentClubById(id);
        if (studentClub != null) {
            em.refresh(studentClub);
            em.remove(studentClub);
        }
        return studentClub;
    }

    // --- CourseRegistration CRUD ---
    public List<CourseRegistration> getAllCourseRegistrations() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CourseRegistration> cq = cb.createQuery(CourseRegistration.class);
        cq.select(cq.from(CourseRegistration.class));
        return em.createQuery(cq).getResultList();
    }

    @Transactional
    public CourseRegistration persistCourseRegistration(CourseRegistration newCourseRegistration) {
        em.persist(newCourseRegistration);
        return newCourseRegistration;
    }

    @Transactional
    public CourseRegistration assignProfessorToCourseRegistration(int studentId, int courseId, int professorId) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);
        Professor professor = getProfessorById(professorId);
        
        if (student != null && course != null && professor != null) {
            for (CourseRegistration cr : student.getCourseRegistrations()) {
                if (cr.getCourse().getId() == courseId) {
                    cr.setProfessor(professor);
                    em.merge(cr);
                    em.flush();
                    return cr;
                }
            }
        }
        return null;
    }

    @Transactional
    public CourseRegistration assignGradeToCourseRegistration(int studentId, int courseId, String grade) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);
        
        if (student != null && course != null) {
            for (CourseRegistration cr : student.getCourseRegistrations()) {
                if (cr.getCourse().getId() == courseId) {
                    cr.setLetterGrade(grade);
                    em.merge(cr);
                    em.flush();
                    return cr;
                }
            }
        }
        return null;
    }
}