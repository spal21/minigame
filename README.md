###################### ScoreStore API ######################
HTTP-based mini game back-end in Java which registers game scores for different users and levels, with the capability to return high score lists per level


How to Run - Build - using maven - mvn clean package 

Launcher class - 
>com.king.services.scorestore.app.Launcher

JavaDocs Generated - 
>http://localhost:63342/minigame/target/apidocs/index.html


API Details -

1. http://localhost:8081/<LoginID>/login GET
2. http://localhost:8081/<Level>/score?sessionkey=<SessionID> POST
    Payload <Score>
3. http://localhost:8081/<Level>/highscorelist GET


Technical Details -

Technology Used- 
Development - Java 8
Unit test - Junit,Mockito, 
Integration Test - ApacheHttpClient
Build - Maven 

Model : User

Architecture - N-layered 

DAO Layer, Service Layer, Service Interface(Controller) & Processor for Async processing

Implementation -

DAO Layer - In-memory DAO implementation is done using ConcurrentHashMap.

Services Layer - Facilitates the data to the controller
	
Service Interface(Controller)- Exposes REST interfaces

Server - JDK Implementation of HttpServer


Thoughts and Considerations - 

For Functional Requirements - 

Users and levels are created “ad-hoc”, the first time they are referenced.

Login and Fetch HighestScore are sync call, where Login ID, Level and Score can take 31 bit unsigned Integer.

Score Registration is implemented using Async Processing (since It doesn't require to return anything ) with In-Memory queue.


FetchScore Handler
>com.king.services.scorestore.handler.FetchHighScoreHttpHandler
 * Http Handler used for Fetching Highest Scores for a given level
 * Returns 200 for Successful fetch irrespective of Scores available for the given Level and Payload in comma separated format of LoginID=Score in Desc Order.               The number of records to be returned is controlled with Constants.SCORE_LIST_FETCH_SIZE = 15 (Default);
 * Returns 400 for Level Info  missing or not a 31 bit unsigned Int
 * Returns 500 for Exceptions from Service Layer or other Exceptions

Login Handler
>com.king.services.scorestore.handler.LoginHttpHandler
 * Http Handler used for Login Action
 * Returns 200 for Successful fetch along with Payload of SessionID . The sessionID is a random String whose length is controlled with Constants.SESSION_ID_LENGTH =7 (default)
 * Returns 400 for Login ID  missing or not a 31 bit unsigned Int
 * Returns 500 for Exceptions from Service Layer or other Exceptions

RegisterScoreHttpAsync Handler
>com.king.services.scorestore.handler.RegisterScoreHttpAsyncHandler
 * Http Handler used for Registering Score in Async Mode
 * The Async processing is delegated to QueueProcessor Implementation.
 * Returns 200 for Successful Delegation of Score Registration. The cases of Expired SessionID are Handled in the AsyncProcessor
 * Returns 400 for SessionID missing , Level or Score Info missing or not a 31 bit unsigned Int
 * Returns 500 for other Exceptions


DAO Implementation - 
> com.king.services.scorestore.dao.impl.InMemoryScoreStoreDAOImpl
 * Uses InMemory Implementation of DAO layer.
 * Uses Concurrent HashMap Implementation to persist Key Value pairs.
 
 Service Implementation -  
 > com.king.services.scorestore.service.impl.ScoreStoreServiceImpl
 * Implementation of the Service Layer. Fetching Data is delegated to the DAO layer Implementation.
 * Uses Reentrant ReadWriteLock for Transaction control.
 
 AsyncProcessor Impl -
 >com.king.services.scorestore.processor.impl.InMemoryQueueProcessorImpl
  * InMemory Implementation of the QueueProcessor for Async Processing of User Objects
  
 >com.king.services.scorestore.processor.impl.RegisterScoreQueueConsumerImpl
  * Queue Consumer Implementation for Async Processing for Registering Scores for a given Level and SessionID
  * Uses Fixed Thread Pool From Executor Service to processing in separate Thread. 
 
 
Application - 
>com.king.services.scorestore.app.Application
 * Main Application to Instantiate Http Server and register context paths
 * For starting and stopping the server gracefully

Http Server Implementation - 
>com.king.services.scorestore.server

Since in JDK Implementation, the Path Param values can only be processed if they are after a path fragment,
a slight modification is done to the existing JDK implementation and the existing implementation is copied.