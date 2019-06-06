# Liquid-Galaxy-for-Education-Controller

### Overview:
Liquid Galaxy for Education Controller is an Android application which is used to remotely control the master chromebook that Liquid Galaxy is installed. LGxEDU Controller inherits properties from Liquid-Galaxy-POIs-Controller which enables the users to use the traditional Liquid Galaxy on chromebooks. 

More information about Liquid-Galaxy-POIs-Controller: https://github.com/LiquidGalaxyLAB/Liquid-Galaxy-POIs-Controller

### At Startup:
The app asks the user to login with their Google account. If the user had already logged in, LGxEDU Controller asks for Google Drive permissions. This is because the app uses the Google Drive in order to export and import games.

### Main Menu:
LGxEDU Controller greets the users with two main options that are navigate and play. 

#### a) Settings:
On the top right corner of the main screen, the settings menu has options to change language, help, about, and administration tools. With administration tools option, the user can start using the traditional liquid galaxy and also manage the games of LGxEDU.

#### b) Navigate:
The navigation screen is a blank screen with instructions on it. When the user touches the screen, the instructions disappear. The navigation of LGxEDU Controller is built by simulating a mouse. This fake mouse tool is called xdotool that runs on Linux platforms:
http://www.linuxcertif.com/man/1/xdotool/
Without touching the chromebooks, the users can navigate with the Google Earth by performing finger movements with one or two fingers. 

#### c) Play:
The play option lists the games that are available on the system. These games can have various types. However we only have quiz option right now.
The quizzes have four-choice questions that are currently related to geography. More categories will be added.

### Game Manager:
Game Manager panel is used to view the lesson categories and the games as a treeview list. In this panel, adding and removing questions to an existing game can be performed. This option is currently not available.

### New Quiz:
The new quizzes are added to Google Drive of the account that is initially logged in to the app. The quiz files are stored in a JSON format. Currently, the user has to specify how many questions there will be in the particular quiz.

### New Question:
In order to create a question, the user ( probably the teacher) has to insert the POIs of all four locations. This is because all answers are locations, and wrong answers are also needed to be known by the students. When the green tick button in the lower right corner of the screen is clicked, the next empty question page shows up.

### During Quiz:
A progresss bar shows how long the quiz will be. In every question there are four choices and each choice has their POIs. When the user answers a question wrong, the correct POI is displayed after the wrong POI. Also, a piece of information about the answer POI is displayed.

### After Quiz: 
The success rate is shown and the user can look back at the answers or play another game.
