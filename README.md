# quick explanation

It basically has been designed to fetch client's and customers'
data from a database named Bitrix, I have used its RESTFUL API to fetch and the script is using this
data to get e-mail addresses of these clients.
The main goal of the application was to send marketing e-mails to these clients from the application
and to ease sales department's work during working hours, the e-mails are being sent
automatically thanks to application.

*  The application implements background-task class which is probably using and based on JAVA threading library of JavaFX to make parallel ops.

**Full version of this mail application is running succesfully at MLC Europe GmbH / Germany and BD Electronics / Malta**


> Because oracle removed JavaFX from JDK11, JavaFX arrives as a stand-alone module. 
> To run the project, you have to add JavaFX as an external module from MAVEN into project.
 

