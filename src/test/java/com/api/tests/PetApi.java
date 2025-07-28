package com.api.tests;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static io.restassured.RestAssured.*;
import POJO.CategoryPOJO;
import POJO.PetPOJO;
import POJO.TagPOJO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

public class PetApi {
	
	ExtentReports report;
	ExtentTest test;
	long petId = 12345;
	PetPOJO pet, updated;
	
	ObjectMapper om = new ObjectMapper();
	@BeforeClass
	public void reportSetup()
	{
		report = new ExtentReports();
		ExtentSparkReporter spark = new ExtentSparkReporter(System.getProperty("user.dir")+"/target/ExtentReports/PetStoreTestngReport.html");
		report.attachReporter(spark);
		RestAssured.baseURI="https://petstore.swagger.io/v2";
	}
	
	@Test(priority=1)
	public void postNewPet()
	{
		test = report.createTest("Creating a new pet using post request");
		PetPOJO petDetails = new PetPOJO();
		petDetails.setId(petId);
		petDetails.setName("Bruno");
		petDetails.setStatus("available");
		petDetails.setPhotoUrls(Collections.singletonList("C:\\Users\\pc\\Pictures\\pic.png"));
		CategoryPOJO categoryDet = new CategoryPOJO();
		categoryDet.setId(15);
		categoryDet.setName("Friendly");
		petDetails.setCategory(categoryDet);
		TagPOJO tagDet = new TagPOJO();
		tagDet.setId(23);
		tagDet.setName("#Cute_Puppy");
		petDetails.setTags(Collections.singletonList(tagDet));
		
		PetPOJO petRes = given().header("accept","application/json").contentType(ContentType.JSON)
				.body(petDetails)
				.when().post("/pet")
				.then().log().all().statusCode(200).extract().as(PetPOJO.class);
		test.pass("Creation of new pet "+petRes.getName());
		Assert.assertEquals(petRes.getName(), "Bruno");
		test.log(Status.PASS, petRes.getName());
		test.log(Status.INFO, "Created new pet "+petRes.getName());
	}
	
	@Test(dependsOnMethods="postNewPet", priority=2)
	public void getNewPet()
	{
		System.out.println(petId);		
		test = report.createTest("Getting newly added pet");
		pet = given().header("accept","application/json")
				.when().get("/pet/"+petId)
				.then().log().all().statusCode(200).extract().response().as(PetPOJO.class);
		test.pass("Retrieving the details of the newly added pet "+pet.getId());
		Assert.assertEquals(pet.getId(),petId);
		Assert.assertEquals(pet.getName(), "Bruno");
		Assert.assertEquals(pet.getStatus(), "available");
		test.log(Status.INFO, "Displaying newly added pet"+ pet.getName());
	}
	
	@Test(dependsOnMethods="getNewPet", priority=3)
	public void putPet() throws JsonMappingException, JsonProcessingException
	{
		test=report.createTest("Updating details of newly added pet");
		pet.setStatus("sold");
		/*PetPOJO updatedPetDetails = given().log().all().header("accept","application/json").contentType(ContentType.JSON)
		.body(pet)
		.when().put("/pet")
		.then().log().all().statusCode(200).extract().response().as(PetPOJO.class);*/
		String updatedPet = given().log().all().header("accept","application/json").contentType(ContentType.JSON)
				.body(pet)
				.when().put("/pet")
				.then().log().all().statusCode(200).extract().asString();
		updated = om.readValue(updatedPet, PetPOJO.class);
	Assert.assertEquals(updated.getName(), pet.getName());
	System.out.println(updated.getStatus());
	test.log(Status.PASS, "Successfully updated pet status");
	}
	
	@Test(priority=4)
	public void getPetStatus()
	{
		test=report.createTest("Getting all animals sold");
		int soldOutPets = given().log().all().header("accept","application/json").queryParam("status", pet.getStatus())
		.when().get("/pet/findByStatus")
		.then().log().all().statusCode(200).extract().jsonPath().getList("$").size();
		System.out.println(soldOutPets);
		test.log(Status.PASS, "Getting the size of the sold out pets");
	}
	
	@Test(priority=5)
	public void deletePet()
	{
		String deletedPet = given().header("accept","application/json").pathParam("petId", pet.getId())
		.when().delete("/pet/{petId}")
		.then().log().all().statusCode(200).extract().asString();
		JsonPath js = new JsonPath(deletedPet);
		String id = String.valueOf(pet.getId());
		Assert.assertEquals(js.get("message"),id,"Comparing the created pet is deleting or not");
	}
	
	@AfterClass()
	public void reportTearDown()
	{
		report.flush();
	}
	
}
