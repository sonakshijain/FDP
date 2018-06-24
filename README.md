# FDP Management

## Introduction
FDP (**F**aculty **D**evelopment **P**rogram) Management System  is a software to create a user friendly and interactive environment to be used by the faculties of any college to generate and faculty development program requests, get it reviewed and accepted by the respective Head of Departments’.

This project was made as an assignment for the Semester IV of the B.Tech at [Symbiosis Institute of Technology, Pune](http://sitpune.edu.in). 

## Features
The FDP Management system has features that enable the Administrator to manage a database containing the records of all the faculties in any institution and thus manage their data. It also enables the each and every faculty to submit an FDP Request and thus get it approved by the concerned HOD.

The FDP Management Software contains the following key features of different roles:  
1.  ***Administrator***

	1) The Administrator can view, delete and create a user (which can be a Faculty, an  Admin or an HOD) with the following data:
		1.  Faculty ID  
		2.  Department
		3.  Gender
		4.  Qualifications
		5.  Courses
		6.  Address
		7.  Contact Number
		8.  Email ID
	2)  The Administrator can update the credentials of any user in the database, including himself.
	3)  Only the Administrator can delete the record of any faculty.
	4) The Administrator can export the contents of the database into a Spreadsheet.
	5) The Administrator can search any content in the database as per any of the details that a user enters for registering himself into the database.

2. ***Faculty***
	1) The faculty members can generate an FDP request to be viewed by the HOD.
	2)  They can also see all the FDP’s that they have submitted and search them in the database.
	3) They can be notified if their FDP’s have been approved or rejected by the HOD.
	4)  They can update their personal credentials in the database.
	5)  They can export their list of FDP into a Spreadsheet.

3. ***Head of Department***
	1) The HOD will be notified if any FDP have been made by any faculty of their respective department.
	2) They can view the FDP and hence choose to approve or reject the FDP.
	3) For all the FDP they haven’t attended yet, they can find them in separate database called Pending FDP.
	4)  They can update their personal credentials.
	5) They can export the list of all the FDP they approved and rejected into a spreadsheet against all the faculties in the department.

## Operating Environments
Windows operating system has been used to both develop and test the software. We have used JavaFX for frontend, Core Java for backend and PostgreSQL for the database.
Out software being developed will be running under Windows only, for now.

## Design and Implementation Constraints
i. This design doesn’t give a realtime notification service from the PostgreSQL Database but rather gives a look-alike of the real time notification system.
ii. Right now there isn’t any feature to select any FDP Request and to export with certain conditions implied like From and To Date, according to user who generated the request or for what number of days was the request for.

## Usage
Right now, we are working on a program to make a sample PSQL database according to the ER Diagram below: 
![](https://lh3.googleusercontent.com/ykJB-08gy_bqOUXfjk_uDPul2ivaY7XgbJEMvp1Zl84A0l4TMpZurZuA8L-Mj3pAy8ILCpJFi_q_TF5xUkfvCzLflAPwYDOrUTWn1h4Ju417TvJhjhqdvx_W4LCsth1n0Oe8-09n)
 > Meanwhile, the program will show a fatal error on starting up if it doesn't find a PSQL Server. If you can make a program to make it simple, please let me know. :)
