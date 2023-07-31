
# DeviceTwin

## Start DeviceTwin Application
The DatabaseTwin (uri-resolver) runs without issues on JDK 11 while the DeviceTwin on JDK 14

Important : Before using the script you need to start mosquitto and DatabaseTwin
To run mosquitto simply run the command : 
	

 - Linux

    `sudo service mosquitto start`

 - Windows
	- Open CMD and go to the installation directory of mosquitto and using command `cd hereTheDirectory` replacing **hereTheDirectory**

	- Then run the command
	 `mosquitto`

 - ## Linux
    ### Instructions
	The script was tested on Ubuntu 20.04, so be **cautious** with other distros! 
	You also need to have gnome-terminal in order to run the script.
	To install gnome-terminal simply run the following command
 - Debian-based distros (example: Ubuntu, Kali, Linux Mint etc.)

    `sudo apt-get install gnome-terminal`

 - Arch-based distros (example: Manjaro Linux, ArcoLinux, Archlabs Linux etc.)

    `sudo pacman -S gnome-terminal`

 - Fedora-based distros (example: Korora, Chapeau, Hanthana etc.)
 
    `sudo yum install gnome-terminal`

 - OpenSUSE-based distros
 
    `sudo zypper install gnome-terminal`
    
    * We first have to add JAVA_HOME variable to the terminal, if not set :
        * Method 1 to set JAVA_HOME variable to the terminal
            * Open environment configs
                `sudo nano /etc/environment`
            * Add the following line
                `JAVA_HOME = "yourPathToJVM"`
                
                Replace **"yourPathToJVM"** with the path of your JVM installation and then save the file using CTRL+X

            * Then use source to load the variables, by running the command :
                `source /etc/environment`
                
            * To verify if the variable is set simply run :
                `echo $JAVA_HOME`
        * Method 2 to set JAVA_HOME variable to the terminal
            * Run the command to set JAVA_HOME
                `export JAVA_HOME=yourPathHere`
                
            * Replace **"yourPathToJVM"** with the path of your JVM installation.

    * Go to the root project and make **start.sh** executable. Open Terminal (**CTRL+ALT+T**) and type : 
        `sudo chmod +x start.sh`
        
    * You need to also make gradlew executable. 
        `sudo chmod +x gradlew`
        
    * At the end, simply run the start command and the script will take care of all classes that have to be run
        `./start.sh yourRootDirectoryForAutoEncoder`
        
        Replace **yourRootDirectoryForAutoEncoder** with the root directory of **AutoEncoder** Project. 
        
        For help : 
        `./start.sh --help`
    * The script should open four terminal windows as the scripts need to be run asynchronously : 
    
        * *EncoderMain.py*
        * *DecoderMain.py*
        * *API* 
        * *DeviceTwin*

    <br>
    
 - ## Windows
    ### Instructions
    * On Windows just run the following command
    `start.bat yourRootDirectoryForAutoEncoder`
    
    * The script opens four terminal window as the scripts need to be run asynchronously 
        * *EncoderMain.py*
        * *DecoderMain.py*
        * *API* 
        * *DeviceTwin*
