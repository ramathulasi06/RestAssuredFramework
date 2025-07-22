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
import static io.restassured.RestAssured.*;
import POJO.CategoryPOJO;
import POJO.PetPOJO;
import POJO.TagPOJO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class PetApi {
	
	ExtentReports report;
	ExtentTest test;
	long petId = 12345;
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
		petDetails.setStatus("Available");
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
		test = report.createTest("Getiing newly added pet");
		PetPOJO res = given().header("accept","application/json")
				.when().get("/pet/"+petId)
				.then().log().all().statusCode(200).extract().response().as(PetPOJO.class);
		test.pass("Retrieving the details of the newly added pet "+res.getId());
		Assert.assertEquals(res.getId(),petId);
		Assert.assertEquals(res.getName(), "Bruno");
		Assert.assertEquals(res.getStatus(), "Available");
		test.log(Status.INFO, "Displaying new added pet"+ res.getName());
	}
	
	@AfterClass()
	public void reportTearDown()
	{
		report.flush();
	}
	
}
