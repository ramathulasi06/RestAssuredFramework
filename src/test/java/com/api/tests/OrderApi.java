package com.api.tests;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentReporter;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class OrderApi {
	
	ExtentReports report;
	ExtentTest test;
	int id;
	@BeforeClass
	public void reportSetup()
	{
		ExtentSparkReporter spark = new ExtentSparkReporter(System.getenv("user.dir")+"OrderPlacingAutomationResults.html");
		report = new ExtentReports();
		report.attachReporter(spark);
		
		RestAssured.baseURI="https://petstore.swagger.io/v2";
	}
	
	@Test(priority=1)
	public void postOrder()
	{
		test=report.createTest("Creating new order");
		Response orderRes = given().header("accept","application/json").contentType(ContentType.JSON)
				.body("{\r\n"
						+ "  \"id\": 2,\r\n"
						+ "  \"petId\": 2,\r\n"
						+ "  \"quantity\": 1,\r\n"
						+ "  \"shipDate\": \"2025-07-22T01:22:09.374Z\",\r\n"
						+ "  \"status\": \"placed\",\r\n"
						+ "  \"complete\": true\r\n"
						+ "}")
				.when().post("/store/order")
				.then().log().all().statusCode(200).extract().response();
		JsonPath js = new JsonPath(orderRes.asString());
		id = js.getInt("id");
		test.log(Status.PASS,"Order placed");
	}
	
	@Test(dependsOnMethods ="postOrder", priority =2)
	public void getOrder()
	{
		test=report.createTest("Getting the order which is placed");
		Response res = given().header("accept", "application/json")
				.when().get("/store/order/"+id)
				.then().log().all().statusCode(200).extract().response();
		JsonPath js = new JsonPath(res.asString());
		Assert.assertEquals(js.getInt("id"), 2);
		test.log(Status.PASS, "Retrieving order");
	}
	
	@AfterClass
	public void reportTearDown()
	{
		report.flush();
	}

}
