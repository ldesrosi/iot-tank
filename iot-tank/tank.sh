#!/bin/bash 
# Absolute path to this script, e.g. /home/user/bin/foo.sh
SCRIPT=$(readlink -f "$0")

# Absolute path this script is in, thus /home/user/bin
SCRIPTPATH=$(dirname "$SCRIPT")

cd $SCRIPTPATH/..
git pull git@github.com:ldesrosi/iot-tank.git

cd iot-tank
mvn package
sudo env "PATH=$PATH" mvn test
sudo env "PATH=$PATH" mvn exec:java -Dexec.mainClass="com.ibm.iot.tank.App" -Dexec.classpathScope=runtime 