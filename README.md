# Read Me First
### The Bidding System  
This bidding system enables the users to place the bid for running auctions.  

# Getting Started

## Tech Stack

* Spring Boot for rapid development. 

* Hibernate Persistence API for Database interactions - MySQL. 

* Using JWT for web security & Login

* RESTful API

* HATEOAS for the API responses

* JUnit - API Test

* Swagger UI

# Guide

## Build Application with the following command  

`bidding-system> mvn clean install`  

## Run the Application with following commands

`bidding-system\target> java -jar bidding-system-0.0.1-SNAPSHOT.jar` 

# Facts

* Before running the application make sure you have MySQL server up and running on your machine.  

* When you run the jar file, the schema will be created along with the following users.  
UserName|Password|Role  
Divakar|cars24-bidding-system|ROLE_ADMIN  
Shiv|cars24-bidding-system|ROLE_USER  
Mozammil|cars24-bidding-system|ROLE_MODERATOR  

* The following Auctions will be created   
itemName, basePrice, stepRate, status   
"Chair", 1000.0f, 10.0f, AuctionStatus.RUNNING  
"Table", 5000.0f, 50.0f, AuctionStatus.OVER    
"Cot", 10000.0f, 100.0f, AuctionStatus.OVER  
"Car", 50000.0f, 500.0f, AuctionStatus.RUNNING    
"Bus", 100000.0f, 1000.0f, AuctionStatus.RUNNING  
"Bike", 8000.0f, 80.0f, AuctionStatus.RUNNING  
"Mobile oppo", 10000.0f, 100.0f, AuctionStatus.RUNNING  
"Computor", 35000.0f, 35.0f, AuctionStatus.RUNNING  
"Key board", 800.0f, 8.0f, AuctionStatus.YET_TO_START  
"Cattle", 9900.0f, 9.0f, AuctionStatus.RUNNING  

* Placing Bid is synchronized so its thread safe. 

* Created a detailed document with Postman screenshots [Bidding_System_Document_with_screenshots](Bidding_System_Document_with_screenshots.docx).

# The following operations are allowed

### Public Resources

* Fetch API for all Running Auctions - API will be paginated in nature.  
`GET /api/auctions?status=RUNNING&page=0&size=10`  

* Fetch API for Specific Auction  
`GET/api/auctions/5`  

* You will have to signin to place bidding. 

* Bonus - User On boarding service is also available.

* Signup  
`POST /api/auth/signup`  
Payload Info  
`Content-Type: application/json`  
`{
    "username": "DivakarK",
    "password": "cars24-bidding-system",
    "email": "c@c.com"
}`  

* Signin  
`POST /api/auth/signin`  
Payload Info  
`Content-Type: application/json`  
`{
    "username": "DivakarK",
    "password": "cars24-bidding-system"
}`

### Authorized Resource

* Use `Authorization` key for bidding as below.  
`POST /api/auctions/{itemCode}/bid`  
Payload Info  
`Content-Type: application/json`  
`Authorization: Bearer <authrization_key_obtained_on_successfull_login>`  
`{
    "bidAmount": 10000
}`

## Bonus

* User with `admin` role can Add & Update Auctions  
If you sign up with the following payload you will be Admin
Signup  
`POST /api/auth/signup`  
Payload Info  
`Content-Type: application/json`  
`{
    "username": "DivakarKummara",
    "password": "cars24-bidding-system",
    "email": "c@y.com",
    "role": ["admin"]
}`  
Since the user’s role is Admin, he has the right to add/Update Auctions. Same is shown in _links

* Create Auction  
`POST /api/auctions`  
`Content-Type: application/json`  
`Authorization: Bearer <authrization_key_obtained_on_admin_successfull_login>`  
`{
    "itemName": "Boat",
    "basePrice": 40000,
    "stepRate": 10,
    "status": "YET_TO_START"
}`  

* Start/Stop Auctions  
`PUT /api/auctions/{itemCode}`  
`Content-Type: application/json`  
`Authorization: Bearer <authrization_key_obtained_on_admin_successfull_login>`  
`{
    "status": "RUNNING"
}`  

