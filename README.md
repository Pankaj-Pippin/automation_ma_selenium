# automation_ma_selenium
Java should install
    Java version is :
    java version "1.8.0_271"
    Java(TM) SE Runtime Environment (build 1.8.0_271-b09)
    Java HotSpot(TM) 64-Bit Server VM (build 25.271-b09, mixed mode)

Selenium tool Eclispse should install

maven version is:
    Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
    Maven home: C:\Venky\Automation scripts\apache-maven-3.6.3\bin\..
    Java version: 1.8.0_271, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk1.8.0_271\jre
    Default locale: en_IN, platform encoding: Cp1252
    OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"

Import the Maven project folder to eclipse or run below command in command prompt(sample command)
mvn clean install "-DOrderNo=197048" "-DAddress1=26 Cavendish Circle" "-DAddress2=null" "-DCity=Salem" "-DState=MA" "-DCounty=Barnstable" "-DZipCode=01970" "-DBuyerName=Mark F Healey" "-DCoBuyerName=null" "-DCompanyName=null"

Before run the command, edit the Config.properties file as per environment we need to execute code (Dev enviroment credentilas are available in Config.properties file)
Config.properties file:

    //DEV enviroment URL, Admin username and Password
    PPN_Url=https://devadmin.pippintitle.com/login
    PPN_UN=<Dev admin ID>
    PPN_PW=<DEV Admin Password>

    //AWS S3 DEV bucket name, credentials and folder name
    AWS_S3_Bucket=<S3 bucket name for DEV>
    AWS_S3_UN=<Secrete Key>
    AWS_S3_PW=<Secrete Key>
    S3folderName =AutomateMA

    //PACER site URL, username and Password
    PacerUN = <PACER Username>
    PacerPW= <PACER Password>
    PacerUrl=<PACER URL>

        Raaj

    //email to from credentials
    from = <mailid >
    password = <Password>
    mailUsername = <mailid>
    mailPassword = <Password>
    // IF ERROR OCCOUR- ENABLE TOGGLE IN "https://myaccount.google.com/u/2/lesssecureapps?pli=1&pageId=none"

Check the folder path is available or set correct path for download PDF or images folder and config.properties file