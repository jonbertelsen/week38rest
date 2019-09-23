package facades;

import entities.Address;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

/**
 *
 * Rename Class to a relevant name Add add relevant facade methods
 */
public class PersonFacade implements IPersonFacade {

    private static PersonFacade instance;
    private static EntityManagerFactory emf;
    
    //Private Constructor to ensure Singleton
    private PersonFacade() {}
    
    
    /**
     * 
     * @param _emf
     * @return an instance of this facade class.
     */
    public static PersonFacade getFacadeExample(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PersonFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public long getPersonCount(){
        EntityManager em = getEntityManager();
        try{
            long personCount = (long)em.createQuery("SELECT COUNT(r) FROM Person r").getSingleResult();
            return personCount;
        }finally{  
            em.close();
        } 
    }

    @Override
    public Person addPerson(String fName, String lName, String phone, String street, String zip, String city) throws MissingInputException {
        if ((fName.length() == 0) || (lName.length() == 0)){
           throw new MissingInputException("First Name and/or Last Name is missing"); 
        }
        EntityManager em = getEntityManager();
        Person person = new Person(fName, lName, phone, street, zip, city);
   
        try {
            em.getTransaction().begin();
            Query query = em.createQuery("SELECT a FROM Address a WHERE a.street = :street AND a.zip = :zip AND a.city = :city");
            query.setParameter("street", street);
            query.setParameter("zip", zip);
            query.setParameter("city", city);
            List<Address> addresses = query.getResultList();
            if (addresses.size() > 0){
                person.setAddress(addresses.get(0)); // The address already exists
            }
            em.persist(person);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return person;
    }

    @Override
    public Person deletePerson(long id) throws PersonNotFoundException {
         EntityManager em = getEntityManager();
          Person person = em.find(Person.class, id);
          if (person == null) {
            throw new PersonNotFoundException(String.format("Person with id: (%d) not found", id));
          }
       try {
           em.getTransaction().begin();
           em.remove(person);
           em.getTransaction().commit();
       } finally {
           em.close();
       }
       return person;
    }

    @Override
    public Person getPerson(long id) throws PersonNotFoundException {
       EntityManager em = getEntityManager();
       try {
           Person person = em.find(Person.class, id);
           if (person == null) {
                throw new PersonNotFoundException(String.format("Person with id: (%d) not found", id));
            }
           return person;
       } finally {
           em.close();
       }
    }

    @Override
    public List<Person> getAllPersons() {
      EntityManager em = getEntityManager();
        try{
            return em.createNamedQuery("Person.getAllRows").getResultList();
        }finally{  
            em.close();
        }   
    }

    @Override
    public Person editPerson(Person p) throws PersonNotFoundException, MissingInputException {
        if ((p.getFirstName().length() == 0) || (p.getLastName().length() == 0)){
           throw new MissingInputException("First Name and/or Last Name is missing"); 
        }
        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, p.getId());
        if (person == null) {
            throw new PersonNotFoundException(String.format("Person with id: (%d) not found", p.getId()));
        }
        
        person.setFirstName(p.getFirstName());
        person.setLastName(p.getLastName());
        person.setPhone(p.getPhone());
        person.setLastEdited();
        person.getAddress().setStreet(p.getAddress().getStreet());
        person.getAddress().setZip(p.getAddress().getZip());
        person.getAddress().setCity(p.getAddress().getCity());

        try {
            em.getTransaction().begin();
            em.merge(person);
            em.getTransaction().commit();
            return person;
        } finally {  
          em.close();
        }
        
        
    }

}
