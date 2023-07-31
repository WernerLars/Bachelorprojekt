#!/bin/bash
function deleteOldDirectories() {
    sleep 1s
    if [ -d "$1" ]; then
      rm -r $1
      echo "Deleted the old folder '$1'!"
    else
      echo "Cannot delete the folder '$1'!"
    fi
  }

function startDigitalTwinIoTDevice() {
  sleep 1s
  echo "Starting DigitalTwinIoTDevice($1)"
  gnome-terminal --title="DigitalTwinIoTDevice $1" -e "./gradlew runDigitalTwinIoTDevice --args='$1'"
  sleep 8s
}

if [ "$1" = "--help" ]; then
  echo "This script run automatically EncoderMain.py, DecoderMain.py, DeviceTwin and the REST API with the web interface."
  echo "$(tput setaf 1)IMPORTANT: $(tput sgr 0) You should run the Database Twin (Uri-Resolver) and mosquitto before running the script!"
else

  deleteOldDirectories "decoderTwin-tcplocalhost"
  deleteOldDirectories "iotDeviceTwin1-tcplocalhost"
  deleteOldDirectories "iotDeviceTwin2-tcplocalhost"
  deleteOldDirectories "iotDeviceTwin3-tcplocalhost"
  deleteOldDirectories "iotDeviceTwin4-tcplocalhost"
  deleteOldDirectories "iotDeviceTwin5-tcplocalhost"
  deleteOldDirectories "twin1-tcplocalhost"
  deleteOldDirectories "userTwin-tcplocalhost"
  deleteOldDirectories "area51compositeTwin-tcplocalhost"
  if [ -f "$1/EncoderMain.py" ]; then
    echo "$(tput setaf 1)IMPORTANT: $(tput sgr 0) Have you already started mosquitto? [y/n]"
    read answerMosquitto
    if [ "$answerMosquitto" = "Y" ] || [ "$answerMosquitto" = "y" ]; then
      echo "$(tput setaf 1)IMPORTANT: $(tput sgr 0) Have you already started the DatabaseTwin? [y/n]"
      read answerDb
      if [ "$answerDb" = "Y" ] || [ "$answerDb" = "y" ]; then
        if [ -f "$1/EncoderMain.py" ]; then
          echo "Starting EncoderMain.py"
          sleep 1s
          gnome-terminal --title="EncoderMain.py" -e "python3 $1/EncoderMain.py"
          sleep 5s
          echo "Starting DecoderMain.py"
          sleep 1s
          gnome-terminal --title="DecoderMain.py" -e "python3 $1/DecoderMain.py"
          sleep 5s
        fi
        # Run gradlew for DeviceTwin
        echo "Starting the API Server and web interface..."
        sleep 1s
        echo "Access the web interface"
        echo "http://localhost:8080/"
        sleep 2s
        startDigitalTwinIoTDevice "1"
        startDigitalTwinIoTDevice "2"
        startDigitalTwinIoTDevice "3"
        startDigitalTwinIoTDevice "4"
        startDigitalTwinIoTDevice "5"
        ./gradlew runMain
      else
        echo "$(tput setaf 1)Please run the Database Twin to proceed! $(tput sgr 0)"
      fi
    else
      echo "$(tput setaf 1)Please start mosquitto running the command 'sudo service mosquitto start' $(tput sgr 0)"
    fi
  else
    echo "$(tput setaf 1)Please insert the root location of AutoEncoder project! $(tput sgr 0)"
  fi
fi
