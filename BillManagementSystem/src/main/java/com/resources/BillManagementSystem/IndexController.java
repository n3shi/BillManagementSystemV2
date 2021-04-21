package com.resources.BillManagementSystem;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


// W TYM PLIKU ZNAJDUJĄ SIĘ WSZYSTKIE ENDPOINTY PO KTÓRYCH APLIKACJA MOŻE SIĘ SKOMUNIKOWAĆ Z SERWEREM

//Definiowanie ciała (pól) żądanych w czasie wykonywania zapytania
// np. endpoind '/login', wymaga podania w zapytaniu parametrów username i password typu string
class LoginForm {
    public String username;
    public String password;
}

// requestbody do weryfikacji (bez sortowania) -> WYŁĄCZONE
class VerifyForm {
    public String token;
}

// requestbody do weryfikacji (z jakimś wyrażeniem) -> AKTYWNE
class NewVerifyForm {
    public String token;
    public String descString;
}

// requestbody do otrzymania id wskazanego przez użytkownika paragonu
class BillForm {
	public String token;
	public int id;
    
}

// requestbody do wprowadzenia zmian w paragonie
class BillModifyForm {
	public String token;
	public int id;
	public String param;
	public String value;  
}

// requestbody do wysłanego przez użytkownika formularza rejestracji
class CreateUserForm{
	public String name;
	public String password;
	public String surname;
	public String phone; 
	public String email;
}

// requestbody do znalezienia wszystkich paragonów przypisanych do konta użytkownika
class GetBills{
	public String token;
	public String offset; //przesuniecie (liczysz od 1, czyli jak ktos chce wyswietlic 11-20, to wysylasz 11-20, choc w tablicy to jest 10-19)
	public String count; //ile pobrac 
	public String sortBy; // price, date
	public String sortMode; // ascending, descending
}

// requestbody do znalezienia wszystkich paragonów użytkownika posiadających wskazany przez użytkownika łańcuch znaków 
class GetSpecificBills{
	public String token;
	public String offset; //przesuniecie (liczysz od 1, czyli jak ktos chce wyswietlic 11-20, to wysylasz 11-20, choc w tablicy to jest 10-19)
	public String count; //ile pobrac 
	public String sortBy; // price, date
	public String sortMode; // ascending, descending
	public String descString; //jakieś wyrażenie z opisu (moze byc ze spacjami, dowolnie)
}

// requestbody do sprawdzenia czy uruchomić admin mode, czy zostać na userMode
class AdminMode{
	public String token;
	public String userId;
	public String userRole;
}


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class IndexController {
	
	//OBIEKTY POMOCNICZE
	@Autowired
	UserRepository userRepository; //obiekt posiadający zapytania dotyczące tabeli użytkownika
	
	@Autowired
	BillRepository billRepository; ////obiekt posiadający zapytania dotyczące tabeli paragony
	
	DataManager manager = new DataManager(); //obiekt posiadający metody sprawdzające poprawność danych, oraz tworzące odpowiedzi
											// string w składni JSON
	
	//**************
	//    USER - wszystkie endpointy odnoszące się do aktualizacji i wyszukań w obrębie użytkownika
	//**************
	
	//Logowanie użytkownika
	@PostMapping(value = "/login", headers="Content-Type=application/json")
	@ResponseBody
	public String login(@RequestBody LoginForm l) throws ParseException{
		
		
		//sprawdzenie czy jest user o takim emailu
		User user = userRepository.findOneByMail(l.username);
		if(user == null) {
			return sendMessage("error", "Incorrect username");
		}
		
		System.out.println("pass:"+ manager.test2(user.getPassw(),l.password));
		
		
		//sprawdzenie hasla
		if(manager.test2(user.getPassw(),l.password) == false)
			return sendMessage("error", "Incorrect password");
		
		
		user.setToken(manager.getNewToken());
		user.setTokenExpireDate(manager.newExpireDate());
		
		System.out.println(user.getToken() + user.getTokenExpireDate());
		userRepository.save(user);
		//funkcja przypisujaca token
		return sendMessage("token",user.getToken(), "admin", user.getRole().equals("admin") ? "ok" : "bad");
	}
	
	//Weryfikacja tokenu użytkownika
	@PostMapping(value = "/verify", headers="Content-Type=application/json")
	@ResponseBody
	public String login(@RequestBody VerifyForm l){
		
		return sendMessage("verify", verifyByToken(l), "admin", verifyIfAdminByToken(l));
	}
	
	//Rejestracja użytkownika
	@PostMapping(value = "/register", headers="Content-Type=application/json")
	@ResponseBody
	public String createUser(@RequestBody CreateUserForm u){
		
			DataManager manager = new DataManager();
			User user = userRepository.findOneByMail(u.email);
			if(user != null) {
				return sendMessage("error", "Account with that e-mail exist");
			}
			String pass = BCrypt.hashpw(u.password, BCrypt.gensalt());
			user = new User(u.name, pass, manager.getNewToken(), manager.getActualDate(),u.surname,u.phone,u.email);
			System.out.println(pass);
			System.out.println(";" + u.password + ";");
			try {
				userRepository.save(user);
			}catch(Exception e)
			{
				return sendMessage("error", "Error creating account - please try again later"); //znaczy ze cos w bazie lub polaczenie z baza nie dziala
			}
			return "{}";
	}
	
	//Wylogowanie z aplikacji
	@PostMapping("/logout")
	@ResponseBody
	public String logout(@RequestBody VerifyForm l){
		
		DataManager manager = new DataManager();
		User user = userRepository.findOneByToken(l.token);
		try {
			if(user.getToken() == l.token);
			user.setTokenExpireDate(manager.logout());
			userRepository.save(user);
			return sendMessage("verify", "ok");
		}catch(Exception e)
		{
			return sendMessage("verify", "bad");
		}
		//wylogowanie klienta po tokenie
	}
	
	
	//zmiana roli (admin główny może zmienić komuś z admin na user, albo z user na admin)
	@PostMapping("/addUserRole")
	@ResponseBody
	public String addUserRole(@RequestBody AdminMode a){
		
		DataManager manager = new DataManager();
		User user = userRepository.findOneByToken(a.token);
		try {
			if(user.getToken().equals(a.token));
			{
				if(user.getRole().equals("admin"))
				{
				user = userRepository.findOneByUserId(Integer.parseInt(a.userId));
				user.setRole(a.userRole);
				userRepository.save(user);
				return sendMessage("status", "user role changed");
				}
				else return sendMessage("error","no privileges");
			}
			
		}catch(Exception e)
		{
			return sendMessage("error", "disconected");
		}
	}
	
	//metoda zwraca wszystkich użytkowników (do podglądu dla admina)
	@PostMapping("/getUsers")
	@ResponseBody
	public String getUsers(@RequestBody VerifyForm l){
		
		DataManager manager = new DataManager();
		User user = userRepository.findOneByToken(l.token);
		try {
			if(user.getToken().equals(l.token) && user.getRole().equals("admin")) {
				List<User> users = userRepository.findAll();
				if (users.size() != 0)
				{
					return manager.getUsersStringJson(users);
				}
				else return "[]";
			}else {
				return sendMessage("error", "access denided");
			}
		}
		catch(Exception e)
		{
			return sendMessage("error", "disconected");
		}
	}
	
	
	//metoda usuwa użytkownika o wskazanym przez admina id
	@PostMapping("/deleteUserByAdmin")
	@ResponseBody
	public String deleteUserByAdmin(@RequestBody AdminMode a){
		
		DataManager manager = new DataManager();
		User user = userRepository.findOneByToken(a.token);
		try {
			if(user.getToken().equals(a.token))
			{
				if(user.getRole().equals("admin"))
				{

					System.out.println(user.getToken() + user.getRole());
					user = userRepository.findOneByUserId(Integer.parseInt(a.userId)); //nowy user (wskazuje na niego admin)
					List<Bill> bill = billRepository.findByUserId(user);   //lista paragonow
					
					
					//usuwanie paragonow
					try {
						for(Object o : bill)
						{
							Bill b = (Bill) o;
							billRepository.delete(b);
						}
					}catch(Exception e)
					{
						return sendMessage("error", "Cant delete bills");
					}
					
					//usuwanie usera
					try {
						userRepository.delete(user);
					}catch(Exception e)
					{
						return sendMessage("error", "Cant delete user");
					}
					userRepository.delete(user);
					
					return "{}";
				}
				else return sendMessage("error","no privileges");
			}	
		}catch(Exception e)
		{
			e.printStackTrace(System.out);
			return sendMessage("error", "disconected");
		}
		return null;
	}
	
	//metoda usuwa użytkownika (użytkownik sam usuwa swoje konto i paragony do niego przypisane)
	@PostMapping("/deleteUser")
	@ResponseBody
	public String deleteUser(@RequestBody VerifyForm a){
	
		User user = userRepository.findOneByToken(a.token);
		try {
			if(user.getToken().equals(a.token))
			{
				//List<Bill> bill = billRepository.findByUserId(user.getUserId());
				List<Bill> bill = billRepository.findByUserId(user);
				
				try {
					for(Object o : bill)
					{
						Bill b = (Bill) o;
						billRepository.delete(b);
					}
				}catch(Exception e)
				{
					return sendMessage("error", "Cant delete bills");
				}
				
				try {
					userRepository.delete(user);
				}catch(Exception e)
				{
					return sendMessage("error", "Cant delete user");
				}
				
				return "{}";
			}	
			else return sendMessage("error", "Disconected");
		}catch(Exception e)
		{

			e.printStackTrace(System.out);
			return sendMessage("error", "No user found");
		}
	}
	
	
	//*************************
	//        BILLS - wszystkie endpointy odnoszące się do aktualizacji i wyszukań w obrębie rachunku (paragonu)
	//*************************
	
	//Zwróć informację o liczbie paragonów przypisanych do użytkownika
	//Informacja ta jest potrzebna, aby aplikacja mogła przeliczyć jak ma je rozłożyć
	//(tj. na ile podstron je podzielić)
	@PostMapping(value = "/getBillCount", headers="Content-Type=application/json")
	@ResponseBody
	public String getBillCount(@RequestBody NewVerifyForm l){
		
		//String temp = verifyByTokenString(l.token);
		//System.out.println(temp);
		if(verifyByTokenString(l.token).equals("ok"))
		{
			//pobierz liste wszystkie paragony
			User user = userRepository.findOneByToken(l.token);
			try {
				//Set<Bill> bills = user.getBills();
				List<Bill> bills = billRepository.findByUserIdAndDescriptionContaining(user, l.descString, Sort.by("price").ascending());
				return sendMessage("count",String.valueOf(bills.size()));
			}catch(Exception e)
			{
				System.out.println(e);
				return sendMessage("error", "No logged user found");
			}
		}
		return sendMessage("count","0");
	}
	
	
	//metoda zwraca listę wszystkich paragonów przypisanych do użytkownika
	@PostMapping(value = "/getBills", headers="Content-Type=application/json")
	@ResponseBody
	public String getBills(@RequestBody GetBills l){
		
		//String temp = verifyByToken(l);
		//System.out.println(temp);
		if(verifyByTokenString(l.token).equals("ok"))
		{
			User user = userRepository.findOneByToken(l.token);
			try {
				List<Bill> bills; 
					switch(l.sortMode) {
					case "ascending": bills = billRepository.findByUserId(user,Sort.by(l.sortBy).ascending()); break;
					case "descending":  bills = billRepository.findByUserId(user,Sort.by(l.sortBy).descending()); break;
					default: return sendMessage("error", "Unknown command");
				}
					
				if(bills.size() > 0)
				{
					DataManager manager = new DataManager();
					
					int offset, count;
					offset = Integer.parseInt(l.offset);
					count = Integer.parseInt(l.count);
			
					List<Bill> newBills = new ArrayList<>();
					// petla wykonuje sie tyle razy jaki masz offset
					// oznacza to, że jeśli użytkownik chce wyświetlić 10 paragonów na stronie 3
					// to trzeba wyświetlić paragony z offest = 20, i count = 10.
					
					for(int i = offset -1; i < offset-1 + count; i++ ) 
					{
						if(i == bills.size())break; //zabezpieczenie zeby nie bylo ze pobierasz 1-10 a sa tylko 2 rekordy
						newBills.add(bills.get(i));
					}	
					
					return manager.getBillsStringJson(newBills.toArray());
				} 
			}catch(Exception e)
			{
				System.out.println(e);
				return sendMessage("error", "No logged user found");
			}
		
		}
		return "[]";
	}
	
	//metoda zwraca paragony uzytkownika spelniajace okreslone warunki (sortuje je, 
	//oraz sprawdza czy podany przez użytkownika ciąg się w nim zawiera )
	@PostMapping(value = "/getSpecificBills", headers="Content-Type=application/json")
	@ResponseBody
	public String getSpecificBills(@RequestBody GetSpecificBills l){
		
		if(verifyByTokenString(l.token).equals("ok"))
		{
			User user = userRepository.findOneByToken(l.token);
			try {
				List<Bill> bills; 
					switch(l.sortMode) {
					case "ascending": bills = billRepository.findByUserIdAndDescriptionContaining(user,l.descString, Sort.by(l.sortBy).ascending()); break;
					case "descending":  bills =billRepository.findByUserIdAndDescriptionContaining(user,l.descString, Sort.by(l.sortBy).descending()); break;
					default: return sendMessage("error", "Unknown command");
				}
					
				if(bills.size() > 0)
				{
					DataManager manager = new DataManager();
					
					int offset, count;
					offset = Integer.parseInt(l.offset);
					count = Integer.parseInt(l.count);
			
					List<Bill> newBills = new ArrayList<>();
					for(int i = offset -1; i < offset-1 + count; i++ )
					{
						if(i == bills.size())break; //zabezpieczenie zeby nie bylo ze pobierasz 1-10 a sa tylko 2 rekordy
						newBills.add(bills.get(i));
					}	
					
					return manager.getBillsStringJson(newBills.toArray());
				} 
			}catch(Exception e)
			{
				System.out.println(e);
				return sendMessage("error", "No logged user found");
			}
		
		}
		return "[]";
	}
	
	// zwraca szczegóły wskazanego przez użytkownika paragonu
	@PostMapping(value = "/getBill", headers="Content-Type=application/json")
	@ResponseBody
	public String getBill(@RequestBody BillForm l){
		
		//find user with token
		User user;
		
		//jezeli uzytkownik o podanym tokenie posiada rachunek o id = l.id to zwracamy mu szczegoly a jesli nie to 
		//zwracamy blad access denided {error: "access denided"}
		//jezeli  wystapil blad to zwracamy 
		
		try{
			user = userRepository.findOneByToken(l.token);
			Bill bill = user.getBillById(l.id);
			
			if(bill == null) {
				return "{}";
			}
			
			String t1,t2,t3,t4,t5;
			t1 = String.valueOf(bill.getId());
			t2 = bill.getDate();
			t3 = String.valueOf(bill.getPrice());
			t4 = bill.getDescription();
			t5 = new String(bill.getImage());
			
			String message = manager.getBillStringJson(t1,t2,t3,t4,t5);
			//nie bylo bledu
			return message;
			
		}catch(Exception e) {
			return sendMessage("error", "No logged user found");
		}		
	}
	
	//metoda pozwala na utworzenie nowego paragonu do konta użytkownika
	@PostMapping(value = "/createBill", headers="Content-Type=application/json")
	@ResponseBody
	public String createBill(@RequestBody VerifyForm l){
		
		//tworzymy rachuenk z domyslnymi parametrami dla klienta o podanym tokenie. jesli wszystko pojdzie ok to
		//zwracamy pusty obiekt a jesli nie to {error: <opis>}
		User user;
		DataManager manager = new DataManager();
		try{
			user = userRepository.findOneByToken(l.token);
			byte[] image = {};
			Bill bill = new Bill(user,image,0,"New bill", manager.getActualDate());
			
			try{
			billRepository.save(bill);
			}catch(Exception e )
			{
				return sendMessage("error", "Cant create new bill");
			}
			
			return "{}";
			
		}catch(Exception e) {
			return sendMessage("error", "No logged user found");
		}		
	}
	
	//metoda pozwala na wprowadzenie zmian do przypisanego do użytkownika paragonu
	@PostMapping(value = "/modifyBill", headers="Content-Type=application/json")
	@ResponseBody
	public String modifyBill(@RequestBody BillModifyForm l){

		User user;
		Bill bill;
		try{
			user = userRepository.findOneByToken(l.token);
			 bill = user.getBillById(l.id);
		}catch(Exception e) {
			return sendMessage("error", "Cannot find bill");
		}
		
		switch(l.param) {
		case "price": bill.setPrice(Float.parseFloat(l.value));break;
		case "desc":  bill.setDescription(l.value);break;
		case "date":  bill.setDate(l.value);break;
		case "photo": bill.setImage(l.value.getBytes()); break;
		default: return sendMessage("error", "Unknown command");
		}
		
		try {
		billRepository.save(bill);
		}catch(Exception e )
		{
			return sendMessage("error", "Cannot update bill");
		}
		//nie bylo bledu
		return "{}";
	}
	
	// metoda pozwala usunąć z bazy wskazany przez użytkownika paragon
	@PostMapping(value = "/deleteBill", headers="Content-Type=application/json")
	@ResponseBody
	public String deleteBill(@RequestBody BillForm l){
		
		
		User user;
		
		try{
			user = userRepository.findOneByToken(l.token);
			Bill bill = user.getBillById(l.id);
			
			//jezeli  wystapil blad to zwracamy {error: <opis>}
			if(bill == null) {
				return sendMessage("error", "Bill not found");
			}
			billRepository.delete(bill);
			return "{}";
			
		}catch(Exception e) {
			return sendMessage("error", "No logged user found");
		}	
	}
	
	
	
	//************
	// JSON CREATOR - metody pozwalające na szybkie tworzenie łańcuchów w formacie JSON, tak aby nie trzeba było tego robić
	//  			  za każdym razem ręcznie
	//************
	
	public String sendMessage(String name, String value, String name2, String value2)
	{
		String message = "{\"" + name + "\":\"" + value + "\",\""+name2 + "\":\"" + value2 + "\"}";
		return message;
	}
	
	public String sendMessage(String name, String value)
	{
		String message = "{\"" + name + "\":\"" + value + "\"}";
		return message;
	}
	
	//**************
	//WERYFIKACJA - weryfikowanie poprawności tokenu (admina i usera)
	//***************
	
	public String verifyByToken(VerifyForm l)
	{
		User user = userRepository.findOneByToken(l.token);
		try {
			if(user.getToken().equals(l.token));
			return "ok";
		}catch(Exception e)
		{
			return "bad";
		}
	}
	
	public String verifyIfAdminByToken(VerifyForm l)
	{
		User user = userRepository.findOneByToken(l.token);
		try {
			if(user.getRole().equals("admin"))
				return "ok";
			else
				return "bad";
		}catch(Exception e)
		{
			return "bad";
		}
	}
	
	public String verifyByTokenString(String token)
	{
		User user = userRepository.findOneByToken(token);
		try {
			if(user.getToken().equals(token));
			return "ok";
		}catch(Exception e)
		{
			return "bad";
		}
	}
	
	
	//***************
	// STATISTIC METHOD - metody użyte do zliczania danych o stronie (dostępne dla admina)
	//**************
	
	//metoda pobiera wszystkich użytkowników z bazy i zwraca wielkość pobranej listy
	@PostMapping(value = "/userCount", headers="Content-Type=application/json")
	@ResponseBody
	public String userCount(){
		List<User> user = userRepository.findAll();
		int size = user.size();
		//nie bylo bledu
		return "{\"userCount\":\"" + size + "\"}";
	}
	
	// metoda pobiera wszystkie paragony z bazy i zwraca ilość jej elementów
	@PostMapping(value = "/billCount", headers="Content-Type=application/json")
	@ResponseBody
	public String billCount(){
		
		return "{\"billCount\":\"" + billRepository.findAll().size() + "\"}";
	}
	
	//metoda przeszukuje listę wszystkich paragonów w poszukiwaniu najdroższego paragonu,
	//a następnie zwraca tą informację do admina
	@PostMapping(value = "/billMax", headers="Content-Type=application/json")
	@ResponseBody
	public String billMax(){
		
		List<Bill> bills = billRepository.findAll();
		
		float max = 0;
		
		for(Bill b : bills) {
			if(b.getPrice() > max){
				max = b.getPrice();
			}
		}
		
		return "{\"billMax\":\"" + max + "\"}";
	}
	
	//metoda zlicza sumę paragonów każdego użytkownika, a następnie zestawia je ze sobą
	// szukając największej wartości. Suma ta jest zwracana do admina
	@PostMapping(value = "/userSumMax", headers="Content-Type=application/json")
	@ResponseBody
	public String userSumMax(){
		
		List<User> users = userRepository.findAll();
		
		float max = 0;
		
		for(User u : users) {
			float buf = u.countBillsPrice();
			if(buf > max){
				max = buf;
			}
		}
		
		return "{\"userSumMax\":\"" + max + "\"}";
	}
}
