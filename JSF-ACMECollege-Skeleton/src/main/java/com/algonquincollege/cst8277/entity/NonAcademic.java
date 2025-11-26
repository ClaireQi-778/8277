/********************************************************************************************************
 * File:  NonAcademic.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * The persistent class for non-academic student clubs.
 */
@Entity
@DiscriminatorValue("0")
public class NonAcademic extends StudentClub implements Serializable {
	private static final long serialVersionUID = 1L;

	public NonAcademic() {
		super(false);
	}
	
}
