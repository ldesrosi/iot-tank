wsk trigger create sessionStarted
wsk package create tank
wsk action create tank/manageMovement tank-action-0.0.1-SNAPSHOT.jar 
wsk action create tank/convertToPayload utils-0.0.1-SNAPSHOT.jar 