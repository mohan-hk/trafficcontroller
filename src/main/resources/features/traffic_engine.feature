Feature: Traffic Engine Transitions and Safety

  Scenario: Normal light transition from Green to Yellow
    Given the traffic engine is started with sequence "1, 2"
    And Phase 1 is currently "GREEN"
    And Phase 1 duration is 5 seconds
    When 5 seconds pass
    Then the light for Phase 1 should turn "YELLOW"

  Scenario: Safety failsafe triggers on conflicting directions
    Given Phase 2 has conflicting directions "North" and "South"
    And the traffic engine is transitioning to Phase 2
    When the safety check is performed
    Then the system should "PAUSE"
    And all lights should be "RED"