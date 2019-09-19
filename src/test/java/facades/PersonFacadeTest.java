package facades;

import utils.EMF_Creator;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Settings;
import utils.EMF_Creator.DbSelector;
import utils.EMF_Creator.Strategy;

//Uncomment the line below, to temporarily disable this test
//@Disabled
public class PersonFacadeTest {

    private static EntityManagerFactory emf;
    private static PersonFacade facade;
    private static Person p1, p2, p3;

    public PersonFacadeTest() {
    }

    //@BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactory(
                "pu",
                "jdbc:mysql://localhost:3307/startcode_test",
                "dev",
                "ax2",
                EMF_Creator.Strategy.CREATE);
        facade = PersonFacade.getFacadeExample(emf);
    }

    /*   **** HINT **** 
        A better way to handle configuration values, compared to the UNUSED example above, is to store those values
        ONE COMMON place accessible from anywhere.
        The file config.properties and the corresponding helper class utils.Settings is added just to do that. 
        See below for how to use these files. This is our RECOMENDED strategy
     */
    @BeforeAll
    public static void setUpClassV2() {
        emf = EMF_Creator.createEntityManagerFactory(DbSelector.TEST, Strategy.DROP_AND_CREATE);
        facade = PersonFacade.getFacadeExample(emf);
    }

    @AfterAll
    public static void tearDownClass() {
//        Clean up database after test is done or use a persistence unit with drop-and-create to start up clean on every test
    }

    // Setup the DataBase in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the script below to use YOUR OWN entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        p1 = new Person("Jønke", "Jensen", "1212122");
        p2 = new Person("Jørgen", "Fehår", "3232222");
        p3 = new Person("Blondie", "Jensen", "323232");

        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterEach
    public void tearDown() {
//        Remove any data after each test was run
    }

    // TODO: Delete or change this method 
    @Test
    public void testAllFacadeMethod() {
        assertEquals(3, facade.getPersonCount(), "Expects two rows in the database");
    }

    @Test
    public void testGetPerson() throws PersonNotFoundException {
        Person person = facade.getPerson(p1.getId());
        assertEquals("Jønke", person.getFirstName(), "Expects to find Jønke");
    }

    @Test
    public void testAddPerson() throws MissingInputException {
        Person p;
        
        // Method one: testing for a known exception
        try {
            p = facade.addPerson("", "Petersen", "131212");
        } catch (MissingInputException e){
            assertThat(e.getMessage(), is("First Name and/or Last Name is missing"));
        }
        
        // Method two: testing for a known exception with assertion
        Assertions.assertThrows(MissingInputException.class, () -> {
            final Person person = facade.addPerson("", "Petersen", "131212");
        });
        
        p = facade.addPerson("Jon", "Snow", "2112211");
        assertNotNull(p.getId());
        EntityManager em = emf.createEntityManager();
        try {
            List<Person> persons = em.createQuery("select p from Person p").getResultList();
            assertEquals(4, persons.size(), "Expects 4 persons in the DB");
        } finally {
            em.close();
        }
    }

    @Test
    public void testDeletePerson() throws PersonNotFoundException {
        long p1Id = p1.getId();
        long p2Id = p2.getId();
        facade.deletePerson(p1Id);
        EntityManager em = emf.createEntityManager();
        try {
            List<Person> persons = em.createQuery("select p from Person p").getResultList();
            assertEquals(2, persons.size(), "Expects 2 persons in the DB");

            persons = em.createQuery("select p from Person p WHERE p.id = " + p1Id).getResultList();
            assertEquals(0, persons.size(), "Expects 2 persons in the DB");
            Person p = em.find(Person.class, p1Id);
            assertNull(p, "Expects that person is removed and p is null");

            p = em.find(Person.class, p2Id);
            assertNotNull(p, "Expects that person is removed and p is null");
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testEditPerson() throws PersonNotFoundException, MissingInputException {
        p3.setLastName("Hansen");
        Person p1New = facade.editPerson(p3);
        assertEquals(p1New.getLastName(), p3.getLastName());
        assertNotEquals(p3.getLastName(),"Jensen");
    }

}
