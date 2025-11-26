/********************************************************************************************************
 * File:  NewStudentClubView.java
 * Course Materials CST 8277
 * 
 * @author Mike Norman
 * @author Teddy Yap
 * 
 */
package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;

import com.algonquincollege.cst8277.entity.StudentClub;
import com.algonquincollege.cst8277.entity.Academic;
import com.algonquincollege.cst8277.entity.NonAcademic;

import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("newStudentClub")
@ViewScoped
public class NewStudentClubView implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String name;
    protected String description;
    protected boolean isAcademic;
    
    @Inject
    @ManagedProperty("#{studentClubController}")
    protected StudentClubController studentClubController;

    public NewStudentClubView() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean getIsAcademic() {
        return isAcademic;
    }
    
    public void setIsAcademic(boolean isAcademic) {
        this.isAcademic = isAcademic;
    }

    public void addStudentClub() {
        StudentClub theNewStudentClub;
        if (isAcademic) {
            theNewStudentClub = new Academic();
        } else {
            theNewStudentClub = new NonAcademic();
        }
        theNewStudentClub.setName(getName());
        theNewStudentClub.setDesc(getDescription());
        studentClubController.addNewStudentClub(theNewStudentClub);
        
        studentClubController.toggleAdding();
        setName(null);
        setDescription(null);
        setIsAcademic(false);
    }
}
