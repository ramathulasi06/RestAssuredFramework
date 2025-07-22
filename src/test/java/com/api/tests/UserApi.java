package com.api.tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers.*;
import POJO.UserPOJO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
public class UserApi {
	
	ExtentReports report;
	ExtentTest test;
	UserPOJO us;
	ObjectMapper mapper = new ObjectMapper();
	String username;
	
	@BeforeClass
	public void reportSetup()
	{
		ExtentSparkReporter spark = new ExtentSparkReporter(System.getProperty("user.dir")+"/target/ExtentReports/UserCreatingReport.html");
		report= new ExtentReports();
		report.attachReporter(spark);
		RestAssured.baseURI="https://petstore.swagger.io/v2";
	}
	@Test(priority=1)
	public void postUser() throws JsonProcessingException
	{
		test=report.createTest("Creating a new user");
		us=new UserPOJO();
		us.setId(24);
		us.setUsername("rchintha");
		us.setFirstname("rama");
		us.setLastname("thulasi");
		us.setEmail("hello@gmail.com");
		us.setPhone("(+91) 123 456 7898");
		us.setPassword("Hello123");
		us.setUserStatus("4");
		String reqJson = mapper.writeValueAsString(us);
		Response re = given().header("accept","application/json").contentType(ContentType.JSON)
				.body(reqJson)
				.when().post("/user")
				.then().log().all().statusCode(200).extract().response();
	
		test.log(Status.PASS,"User is created");
			username = us.getUsername();
			System.out.println(username);
	}
	
	@Test(dependsOnMethods="postUser", priority=2)
	public void getUser() throws JsonMappingException, JsonProcessingException
	{
		System.out.println(username);
		test=report.createTest("Getting user created");
		Response res = given().header("accept","application/json")
				.when().get("/user/"+username)
		.then().log().all().statusCode(200).extract().response();
		us=mapper.readValue(res.asString(), UserPOJO.class);
		Assert.assertEquals(username, us.getUsername());
		test.log(Status.INFO,"Created user is "+res.asString());
		
	}
	@AfterClass
	public void reportTearDown()
	{
	
		report.flush();
	}

}
