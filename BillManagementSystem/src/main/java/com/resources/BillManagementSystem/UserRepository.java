package com.resources.BillManagementSystem;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	//Zbiór metod do przeszukiwania bazy. Są to zapytania typu select dla tabeli USER

	//sprawdzenie loginu
	List<User> findByName(String username);
	
	//znajdź po nazwie użytkownika
	User findOneByName(String username);
	
	//znajdź po tokenie
	User findOneByToken(String token);
	
	//znajdź po tokenie, ale i posortuj (sort potrzebny jest jako parametr, nie daje nic bo jest tylko 1 user)
	User findOneByToken(String token,Sort sort);
	
	//znajdź użytkownika po adresie email
	User findOneByMail(String email);
	
	//znajdź użytkownika po jego id
	User findOneByUserId(int id);
	
	//sprawdzenie hasla
	List<User> findByPassw(String password);
	
	//sprawdzenie sprawdzenie tokenu
	List<User> findByToken(String token);
	
	//sprawdzenie telefonu
	List<User> findByPhone(String phone);
	

	
}

