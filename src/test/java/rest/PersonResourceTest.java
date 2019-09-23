package rest;

import dtomappers.PersonDTO;
import entities.Person;
import dtomappers.PersonsDTO;
import utils.EMF_Creator;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.net.URI;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator.DbSelector;
import utils.EMF_Creator.Strategy;

//Uncomment the line below, to temporarily disable this test
//@Disabled
public class PersonResourceTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";
    //Read this line from a settings-file  since used several places
    private static final String TEST_DB = "jdbc:mysql://localhost:3307/startcode_test";

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;
    private static Person p1, p2, p3;
    
    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactory(DbSelector.TEST, Strategy.CREATE);

        //NOT Required if you use the version of EMF_Creator.createEntityManagerFactory used above        
        //System.setProperty("IS_TEST", TEST_DB);
        //We are using the database on the virtual Vagrant image, so username password are the same for all dev-databases
        
        httpServer = startServer();
        
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
   
        RestAssured.defaultParser = Parser.JSON;
    }
    
    @AfterAll
    public static void closeTestServer(){
        //System.in.read();
         httpServer.shutdownNow();
    }
    
    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the script below to use YOUR OWN entity class
    @BeforeEach
    public void setUp() {
       EntityManager em = emf.createEntityManager();
       p1 = new Person("Jønke", "Jensen", "1212122","Ndr Frihavnsgade 29","2100","Kbh Ø");
       p2 = new Person("Jørgen", "Fehår", "3232222","Østerbrogade 2", "2200","Kbh N");
       p3 = new Person("Blondie", "Jensen", "323232","Storegade 3","3700","Rønne");

        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            
            em.persist(p1);
            em.persist(p2.getAddress());
            em.persist(p2);
            em.persist(p2.getAddress());
            em.persist(p3);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testServerIsUp() {
        given().when().get("/person").then().statusCode(200);
    }
   
    //This test assumes the database contains two rows
    @Test
    public void testDummyMsg() throws Exception {
        given()
        .contentType("application/json")
        .get("/person/").then()
        .assertThat()
        .statusCode(HttpStatus.OK_200.getStatusCode())
        .body("msg", equalTo("Your Person API is up and running"));
    }
    
    @Test
    public void testCount() throws Exception {
        given()
        .contentType("application/json")
        .get("/person/count").then()
        .assertThat()
        .statusCode(HttpStatus.OK_200.getStatusCode())
        .body("count", equalTo(3));   
    }
    
    @Test
    public void getAllPersons(){
            List<PersonDTO> personsDTOs;
        
            personsDTOs = given()
                    .contentType("application/json")
                    .when()
                    .get("/person/all")
                    .then()
                    .extract().body().jsonPath().getList("all", PersonDTO.class);
            
            PersonDTO p1DTO = new PersonDTO(p1);
            PersonDTO p2DTO = new PersonDTO(p2);
            PersonDTO p3DTO = new PersonDTO(p3);
            
            assertThat(personsDTOs, containsInAnyOrder(p1DTO, p2DTO, p3DTO));
    }
    
    @Test
    public void addPerson(){
        given()
                .contentType(ContentType.JSON)
                .body(new PersonDTO("Jon", "Snow", "2112211","Bygaden 28","2100","Kbh Ø"))
                .when()
                .post("person")
                .then()
                .body("fName", equalTo("Jon"))
                .body("lName", equalTo("Snow"))
                .body("phone", equalTo("2112211"))
                .body("id", notNullValue());
    }
    
    @Test
    public void updatePerson(){
        PersonDTO person = new PersonDTO(p1);
        person.setPhone("1111");
 
        given()
                .contentType(ContentType.JSON)
                .body(person)
                .when()
                .put("person/"+ person.getId())
                .then()
                .body("fName", equalTo("Jønke"))
                .body("lName", equalTo("Jensen"))
                .body("phone", equalTo("1111"))
                .body("id", equalTo((int)person.getId()));
    }
    
      @Test
      public void testDelete() throws Exception {
        
        PersonDTO person = new PersonDTO(p1);
        
        given()
            .contentType("application/json")
            .delete("/person/" + person.getId())
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK_200.getStatusCode());
        
        List<PersonDTO> personsDTOs;
        
        personsDTOs = given()
                .contentType("application/json")
                .when()
                .get("/person/all")
                .then()
                .extract().body().jsonPath().getList("all", PersonDTO.class);


        PersonDTO p2DTO = new PersonDTO(p2);
        PersonDTO p3DTO = new PersonDTO(p3);

        assertThat(personsDTOs, containsInAnyOrder(p2DTO, p3DTO));
            
    }
    
    
}
