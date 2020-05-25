# SmartBot1
SmartBot (Software Project -18CS10016)
Download and install Android Studio. https://developer.android.com/studio/install. Refer to this link for installing and setting up Android studio. Assuming you have Android Studio installed ,download or clone the repository and open the the project in android studio, sync project files and run them on an actual device because Virtual devices do not support speech recognition and textToSpeech functionalities. To run the project on your device connect it to the system through a data cable. Enable developer options of the device and enable USB debugging options. Once device is detected by the system, run the app and install it into the device. After the app is launched ,as a first time user you will have to register yourself with requested credentials and then login. Make sure to check the checkbox if you want to stay logged in or else you will be automatically logged out. After logging in, enable required permission and now you are ready to use the application. It should be remembered that only some of the specific framings of users commands are comprehensible by SmartBot which is its biggest limitation(cannot be solved in this scope of project).However I tried making it as much generalised as possible by using keywords to match inorder to implement suitable methods and intents.
Few samples of user's commands comprehensible by SmartBot are given below :
call <name> , message <messagebody>, how<>, when<>, what<>,"set an alarm","create an event", open <appname>
Other points to be noted :
1.For the functionality of alarm 'pop up' notification make sure you have floating notifications and lockscreen notifications enabled for SmartBot.
2.Opening some of the applications might take a while ,please hold on while its searching for the app.
Further Enhancement :(out of SRS specifications)
1.I recently thought of implementing a new activity with current weather report and news report (which can be scrolled) using weather and news APIs as home activity including a floating action button which on clicked would redirect us to the chat window (present home activity). But the extra work couldn't be completed with in the duration of project.
2.SmartBot could further be advanced by using artificial intelligence, machine learning and nlp.


