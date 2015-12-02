package controllers;
 
import models.*;
 
public class Security extends Secure.Security {
	
    static boolean authenticate(String username, String password) {
        if (User.isValidAdminLogin(username, password) != null)
        	return true;
        
        return false;
    }
    
}