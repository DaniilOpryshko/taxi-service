## TAXI-SERVICE
## Content
* [Project description](#description)
* [Project achitecture](#architecture)
* [Steps to reproduce?](#start)
* [Authors](#author)

## <a name="description"></a>Description
This is a simple WEB application made for educational and demonstration purposes. It supports authentication, registration, logging, managing cars, drivers, manufactures and other CRUD operations.
Application supports next functions:
- display all cars;
- display all drivers;
- display all manufactures;
- display all cars of currently logged driver;
- create/delete car;
- create/delete manufacturer;
- register new driver;
- add/delete driver to/from car;

You can access all this functions from main page ```{application context}/index``` or ```{application context}/```
But firstly you need to sign in/register on ```{application context}/login``` page (on which you will be redirected unless you are logged in)
After successful login you will have access to all functionality.

---
## <a name="architecture"></a>3-layer architecture
1. DAO - Data access layer
2. Service - Application logic layer
3. Controllers - Presentation layer

---
## <a name="technologies"></a>Used Technologies
- Java 11
- Apache Maven
- Apache TomCat - version 9.0.59
- Http Servlet
- Apache Log4j2 - version 2.17.2
- MySQL
- JDBC
- JSTL - version 1.2
- JSP
- HTML, CSS, XML

---
## <a name="start"></a>Steps to reproduce
1. Configure Tomcat for your IDE. I used Tomcat 9.0.59 and Intellij Idea Ultimate
2. To set up database run script located in ```src/main/resources/init_db.sql```
3. In ```src/main/java/taxi/util/ConnectionUtil.java``` replace ```USERNAME``` ```PASSWORD``` ```DATABASE_URL``` and ```DRIVER_CLASS_NAME``` with your database params
4. In the ```src/main/resources/log4j2.xml``` at line 7 you also need to replace ```ABSOLUTE_PATH_TO_YOUR_LOG_FILE``` with absolute path to your ```.log``` file
5. Run the web app

---
## <a name="author"></a>Author
@Danielele88 [Telegram](https://t.me/Danielele88)  
I am open for new opportunities as Junior Java Developer