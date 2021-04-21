package com.resources.BillManagementSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime; // Import the LocalDateTime class
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;


// Klasa przechowująca wszystkie pomocnicze funkcje używane w IndexController.
// Są tu zarówno te dotyczące walidacji danych jak i tworzenia określonych łańcuchów znaków w różnych formatach i składniach
// w zależności od potrzeb klasy IndexController
public class DataManager {

	@Autowired
	UserRepository userRepository;
	
	//testowa metoda -> zwraca pełnego użytkownika na podstawie jego nazwy
	public List<User> findByName(String name)
	{
		return userRepository.findByName(name);
	}
	
	//zwraca "logged" jesli uzytkownik jest zalogowany lub "notlogged" jesli nie jest
	public String checkIfLogged(User user) throws ParseException
	{
		String formattedDate = getActualDate();
		 Date d1 = null;
		 Date d2 = null;
		
		 SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		 d1 = sdf.parse(user.getTokenExpireDate());
		 d2 = sdf.parse(formattedDate);
		 
		 Calendar now = Calendar.getInstance();
		 
		 String date1 = sdf.format(d1);
		 String date2 = sdf.format(d2);
		 
		 System.out.println(date1 + "  " +  date2);
		 
		 
		 //czy data tokena jest pozniejsza niz data aktualna?
		 boolean check = d1.after(d2);
		//jesli jego data jest starsza niz aktualna, to znaczy ze nie jest zalogowany
		if(check) return "logged";
		else return "notlogged"; 
	}
	
	//wyznacza nową datę ważności tokenu. Po tym czasie kolejna próba wykonania akcji zakończy się niepowodzeniem
	public String newExpireDate() throws ParseException
	{
		 SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

		 Calendar now = Calendar.getInstance();
		 now.add(Calendar.MINUTE, 10);
		 Date teenMinutesFromNow = now.getTime();
		 String neewDate = sdf.format(teenMinutesFromNow);

		 return neewDate;	
	}
	
	//metoda zwraca nieważną już datę tokenu, tak by móc się zalogować ponownie
	public String logout() throws ParseException
	{
		 SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

		 Calendar now = Calendar.getInstance();
		 now.add(Calendar.MINUTE, -10);
		 Date teenMinutesFromNow = now.getTime();
		 String neewDate = sdf.format(teenMinutesFromNow);

		 return neewDate;	
	}
	
	//generowanie nowego tokenu
	public String getNewToken()
	{
		 SecureRandom secureRandom = new SecureRandom(); //threadsafe
		 Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
		
		 byte[] randomBytes = new byte[24];
		 secureRandom.nextBytes(randomBytes);
		 String token = base64Encoder.encodeToString(randomBytes);
		 
		 return token;
	}
	
	//zwrócenie aktualnej daty
	public String getActualDate()
	{
		LocalDateTime myDateObj = LocalDateTime.now();
		DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
		String formattedDate = myDateObj.format(myFormatObj);
		
		return formattedDate;
	}
	
	// **********************
	// JSON BUILDER - wszystkie metody poniżej pozwalają na budowanie stringu w formacie JSON przy podaniu tylko parametrów
	//     			- przyspiesza to pracę w IndexController i polepsza czytelność kodu
	// **********************

	// metoda zwraca zbudowany ciąg zawierający pełne dane paragonu
	public String getBillStringJson(String id, String date, String price, String descr, String photo)
	{
		String command = "{\"id\":\"" + id + "\",\"date\":\"" + date + "\", "
				+ "\"price\":\"" + price+ "\", \"desc\":\"" + descr+ "\",\"photo\":\"" + photo + "\"}";
		
		return command;
	}
	
	// metoda zwraca zbudowany ciąg zawierający listę wszystkich paragonów 
	public String getBillsStringJson(Object bills[])
	{
        String completeJSON = "[";
        
        for(@SuppressWarnings("unused") Object o : bills)
        {
        	Bill newBill = (Bill)o;
        
        	String t1,t2,t3;
        	t1 = String.valueOf(newBill.getId());
        	t2 = newBill.getDate();
        	
        	t3 = String.valueOf(newBill.getPrice());
     
            String temp = getTempJsonBills(t1,t2,t3);
            
            completeJSON += temp;
        }
        completeJSON =  completeJSON.substring(0, completeJSON.length()-1);
		return completeJSON + "]";
	}
	
	//metoda zwraca zbudowany ciąg zawierający dane nowo przypisanego paragonu
	public String getTempJsonBills(String id, String date, String price )
	{
		String command = "{\"id\":\"" + id + "\",\"date\":\"" + date + "\", "
				+ "\"price\":\"" + price+ "\"},";;
				
				return command;
	}
	
	// metoda zwraca zbudowany ciąg zawierający listę wszystkich użytkowników (wraz z ich danymi)
	public String getUsersStringJson(List<User> user)
	{
		String completeJSON = "[";
		for(Object o : user)
		{
			User tempUser = (User) o;
			String t1,t2,t3,t4,t5,t6;
			t1 = String.valueOf(tempUser.getUserId());
			t2 = tempUser.getMail();
			t3 = tempUser.getName();
			t4 = tempUser.getSurname();
			t5 = tempUser.getPhone();
			t6 = tempUser.getRole();
			
			String temp  = getTempJsonUser(t1,t2,t3,t4,t5,t6);
			completeJSON += temp;
		}
		 completeJSON =  completeJSON.substring(0, completeJSON.length()-1);
		return completeJSON + "]";
	}
	
	// metoda zwraca zbudowany ciąg zawierający dane w formacie JSON dla pojedynczego użytkownika 
	// (używane w metodzie getUsersStringJson )
	public String getTempJsonUser(String id, String email, String name, String sourname, String phone, String role)
	{
		String command = "{\"id\":\"" + id + "\",\"email\":\"" + email + "\", "
				+ "\"name\":\"" + name+ "\",\"surname\":\"" + sourname + "\",\"phone\":\"" +
				phone + "\",\"role\":\"" + role +"\"},";
				
				return command;
	}
	
	// sprawdzenie zahashowanego hasła otrzymanego od użytkownika z hasłem z bazy (wersja 1) -> NIEAKTYWNA
	public Boolean test(User user,String text)
	{

		System.out.println("'"+text + "'" + user.getPassw()+"'");
		if (BCrypt.checkpw(text, user.getPassw()))
			return true;
		else
			return false;
	}
	
	
	// sprawdzenie zahashowanego hasła otrzymanego od użytkownika z hasłem z bazy (wersja 2) -> AKTYWNA
	public Boolean test2(String text1,String text)
	{

		System.out.println(text + text1);
		if (BCrypt.checkpw(text, text1))
			return true;
		else
			return false;
	}

	// metoda sortuje listę paragonów względem podanego sposobu sortowania
	public void sortBills(Set<Bill> bills, String sortBy) {
		
		List<Object> newbills = Arrays.asList(bills.toArray());

		
		Comparator<Object> compareByPrice = (Object o1, Object o2) -> Float.compare(((Bill) o1).getPrice(), ((Bill) o1).getPrice()); 
		newbills.sort(compareByPrice);
		
		//int i = 0;
		for(int i =0; i<newbills.size(); i++)
		{
			Bill tempBill = (Bill) newbills.get(i);
			System.out.println(tempBill.getPrice());
		}
	}
	
	// Generic function to convert set to list
	public static <T> List<T> convertToList(Set<T> set)
	{

		List<T> items = new ArrayList<>();
		for (T e : set)
			items.add(e);

		return items;
	}
	
	
}
