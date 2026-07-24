@ui @regression
Feature: ParaBank Accounts Overview Dashboard
  As a logged-in ParaBank customer
  I want to view my accounts overview dashboard
  So that I can review my account balances

  Background:
    Given I am on the ParaBank login page
    And I log in with valid credentials

  Scenario: View accounts overview dashboard after login
    Then I should be logged in successfully
    And the dashboard should display a welcome message for "Sreenivasulu Somu"
    And the "Accounts Overview" table should be visible
    And the "Accounts Overview" navigation link should be visible
    And the "Log Out" navigation link should be visible
