@echo off

if "%1" == "" GOTO noArgumentGiven
if exist %1\EncoderMain.py GOTO askForMosquitto
if not exist %1\EncoderMain.py GOTO fileDoesNotExist

:fileDoesNotExist
echo "ERROR: script can't find EncoderMain.py and DecoderMain.py"
echo "Please consider running gradle on the uri resolver project in order to generate a gradlew file, required for the script!"
exit/b

:noArgumentGiven
echo "ERROR: Please add the argument which represents the root location of the Autoencoder project"
exit/b

:askForMosquitto
set /p id="Have you already started mosquitto? [y/n]"
if "%id%" == "y" GOTO askForDatabaseTwin
if "%id%" == "Y" GOTO askForDatabaseTwin
echo "Please start mosquitto to proceed!"
exit/b

:askForDatabaseTwin
set /p id="Have you already started DatabaseTwin? [y/n]"
if "%id%" == "y" GOTO startEncoderMain
if "%id%" == "Y" GOTO startEncoderMain
echo "Please start DatabaseTwin to proceed!"
exit/b

:startEncoderMain
start "EncoderMain.py" cmd /k Call python3 %1\EncoderMain.py
timeout 5
GOTO startDecoderMain
exit/b

:startDecoderMain
start "DecoderMain.py" cmd /k Call python3 %1\DecoderMain.py
timeout 5
GOTO startDeviceTwin
exit/b

:startDeviceTwin
start "DigitalTwin Main" cmd /k Call gradlew.bat runMain
exit/b