@ui @regression
Feature: ParaBank Login
  As a ParaBank customer
  I want to log in with my username and password
  So that I can access my online banking account

  Background:
    Given I am on the ParaBank login page

  Scenario: Successful login with valid credentials
    When I log in with valid credentials
    Then I should be logged in successfully
    And the page title should be "ParaBank | Accounts Overview"

  Scenario: Login fails with invalid credentials
    When I log in with username "sreeni0412" and password "wrongpassword"
    Then I should see a login error message
