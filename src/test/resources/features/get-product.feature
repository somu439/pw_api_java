@system @regression
Feature: GET Product - DummyJSON API
  As a QA engineer
  I want to verify the GET product endpoint of dummyjson.com
  So that I can ensure product data is returned correctly

  Background:
    Given the base URL is configured

  Scenario: Get a single product by ID
    When I send a GET request to "/products/1"
    Then the response status should be 200
    And the response should be OK
    And the response header "content-type" should contain "application/json"
    And the response should contain field "id"
    And the response field "id" should equal 1
    And the response should contain field "title"
    And each review should have non-null field "reviewerEmail"
    And each review should have non-null field "reviewerName"
    And each review rating should be within the valid enum from "ratings"
