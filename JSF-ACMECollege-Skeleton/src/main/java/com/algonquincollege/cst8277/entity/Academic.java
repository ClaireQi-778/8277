/********************************************************************************************************
 * File:  Academic.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * The persistent class for academic student clubs.
 */
@Entity
@DiscriminatorValue("1")
public class Academic extends StudentClub implements Serializable {
	private static final long serialVersionUID = 1L;

	public Academic() {
		super(true);
	}
	
}
