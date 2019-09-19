package facades;

import entities.Person;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

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
    public Person addPerson(String fName, String lName, String phone) {
        EntityManager em = getEntityManager();
        Person person = new Person(fName, lName, phone);
        
       try {
           em.getTransaction().begin();
           em.persist(person);
           em.getTransaction().commit();
       } finally {
           em.close();
       }
       return person;
    }

    @Override
    public Person deletePerson(long id) {
         EntityManager em = getEntityManager();
          Person person = em.find(Person.class, id);
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
    public Person getPerson(long id) {
       EntityManager em = getEntityManager();
       try {
           Person person = em.find(Person.class, id);
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
    public Person editPerson(Person p) {
        
        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, p.getId());
        
        person.setFirstName(p.getFirstName());
        person.setLastName(p.getLastName());
        person.setPhone(p.getPhone());
        person.setLastEdited();
        
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
