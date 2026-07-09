@system @regression
Feature: GET Product - DummyJSON API for Array
  As a QA engineer
  I want to verify the Array GET product endpoint of dummyjson.com
  So that I can ensure product data is returned correctly

  Background:
    Given the base URL is "http://localhost:8080"

  Scenario: Get a single product by ID
    When I send a GET request to "/array/123"
    Then the response status should be 200
    And the response should be OK
    And the response header "content-type" should contain "application/json"
    And the response should contain field "id"
    And the response field "id" should equal 1
    And the response should contain field "title"
    And each of the following fields should be non-null:
      | reviews[*].reviewerEmail |
      | reviews[*].reviewerName  |
      | title                    |
    And each value at "reviews[*].rating" should be within the valid list from CSV "ratings"
    And each value at "reviews[*].comment" should be within the valid list from CSV "comment"
    And the value of the element "body.dimensions.depth" is "22.99"
    And the value of the element "body.tags[1]" is "mascara"
    And the value of the element "body.tags[*]" contains "beauty"
    And the value of the element "body.sku" is "BEA-ESS-ESS-001"
