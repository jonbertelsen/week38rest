/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facades;

import entities.Person;
import java.util.List;

/**
 *
 * @author jobe
 */
public interface IPersonFacade {
  public Person addPerson(String fName, String lName, String phone);  
  public Person deletePerson(int id);  
  public Person getPerson(int id);  
  public List<Person> getAllPersons();  
  public Person editPerson(Person p);  
}
