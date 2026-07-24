@system @regression
Feature: GET Product - DummyJSON API for node
  As a QA engineer
  I want to verify the node GET product endpoint of dummyjson.com
  So that I can ensure product data is returned correctly

  Background:
    Given the base URL is "http://localhost:8080"

  Scenario: Get a single product by ID
    When I send a GET request to "/node/123"
    Then the response status should be 200
    And the response should be OK
    And the response header "content-type" should contain "application/json"
    # And the response should contain field "id"
    And the response field "myresponse[0].body.id" should equal 1
    And the response should contain field "myresponse[*].body.title"
    And each of the following fields should be non-null:
      | myresponse[0].body.reviews[*].reviewerEmail |
      | myresponse[0].body.reviews[*].reviewerName  |
      | myresponse[0].body.title                    |
    And each value at "myresponse[0].body.reviews[*].rating" should be within the valid list from CSV "ratings"
    And each value at "myresponse[0].body.reviews[*].comment" should be within the valid list from CSV "comment"
    And the value of the element "myresponse[0].body.dimensions.depth" is "22.99"
    And the value of the element "myresponse[0].body.tags[1]" is "mascara"
    And the value of the element "myresponse[0].body.tags[*]" contains
      |beauty|
      |mascara|
    And the value of the element "myresponse[0].body.sku" is "BEA-ESS-ESS-001"
    And the following fields should match for "myresponse[0].body.reviews[*].comment" and "myresponse[0].body.reviews[*].reviewerName"
      |Highly impressed!|Eleanor Collins|
      |Very satisfied!|Lucas Gordon|
    And each value at "myresponse[0].body.reviews[*].date" should contain valid date format "MM-DD-YYYY"
    
