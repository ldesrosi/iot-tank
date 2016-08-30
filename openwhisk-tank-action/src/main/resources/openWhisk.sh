wsk trigger create sessionStarted
wsk trigger create movementUpdate
wsk trigger create turnComplete
wsk package create tank
wsk action create tank/createSession tank-action-0.0.1-SNAPSHOT.jar 
wsk action create tank/move tank-distance-action-0.0.1-SNAPSHOT.jar
wsk action create tank/convertToPayload utils-0.0.1-SNAPSHOT.jar 