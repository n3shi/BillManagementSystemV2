package com.resources.BillManagementSystem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

//Klasa przedstawiająca rachunek(paragon) 
//Oprócz definicji pól i metod są tu jeszcze settery i gettery
@Entity
@Table(name = "bill")
public class Bill {
	

	//DEFINICJE PÓL KLASY (a tym samym kolumn tabeli bill w bazie)
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    

	//na UserId utworzono mapowanie, ponieważ schemat bazy, to baza relacyjna
    //@ManyToOne(targetEntity = User.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="userid", referencedColumnName="userId")
	private User userId;
    
    @Column(name="image", columnDefinition="blob")
	private byte image[];
    
    @Column(name="price")
	private float price;
    
    @Column(name="description")
	private String description;
    
    @Column(name="date")
	private String date;
	
    Bill(){};
	
	//Konstruktor pozwalajacy utworzyc nowy rachunek z ustalonymi wartosciami parametrow
	Bill(User userId, byte image[], int price, String description, String date)
	{
		this.userId = userId;
		this.image = image;
		this.price = price;
		this.description = description;
		this.date = date;
		
	}
	
	
	
	//SETTERY I GETTERY
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getUserId() {
		return userId;
	}


	public void setUserId(User userId) {
		this.userId = userId;
	}


	public byte[] getImage() {
		return image;
	}


	public void setImage(byte[] image) {
		this.image = image;
	}


	public float getPrice() {
		return price;
	}


	public void setPrice(float price) {
		this.price = price;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getDate() {
		return date;
	}


	public void setDate(String date) {
		this.date = date;
	}
}
