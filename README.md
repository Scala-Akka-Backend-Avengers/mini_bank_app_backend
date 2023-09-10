									** MINI-BANK APPLICATION **
										(Scala-Akka)

+ Major technologies:
	* Akka
	* Cats-Effect (Data validation)
	* Cassandra (persistence data store)
	* Docker

+ Akka Ecosystem:
	* Akka Typed Actors
	* Akka Persistence
	* Akka HTTP

+ Application details:
	* Name: Mini-Bank application
	* Operations:
		- Bank account:
			# Create
			# Update
			# Destroy
			# Update account balance ..and so on..
	* Design:
		- Actors: building blocks of the application
		- Server: built using Akka HTTP
		- Storage:
			# Cassandra as persistence system
			# Persistent event will be stored in Cassandra for long term storage
			# Also Cassandra can be used for event sourcing in case if we need to replay those events in case of failure
		- Bank Account (individual actors per bank account):
			# Each bank account (bank account per person) will be a persistent actor
			# This actor will store all the relevant events of an account to Cassandra
			# Event will be available from an event-source
			# As a result, all the events are available with Cassandra and they are replayed if one of the actor dies or the entire application needs to be restarted
		- Bank (major root actor):
			# This will be another bigger actor which will manage all the account (actors)
		- HTTP REST API (Akka HTTP server):
			# The entire system will communicate and function over HTTP REST API
			# The HTTP REST API will be built using Akka HTTP
			# Akka HTTP server will be developed and used to interact with the bank account (major root bank actor) and all the bank account (actors) underneath it
		- REST APIs:
			# / (POST) -> create a bank account
				~ 201 Created, Location: /bank-accounts/<uuid>
				~ 400 Bad Request
			# /<uuid> (GET) -> get the details of a bank account using its UUID (Universally Unique Identifier)
				~ 200 OK, JSON with bank account details
				~ 404 Not found
			# /<uuid> (PUT) -> update the details of the bank account using its UUID
				~ 200 OK, JSON with new (updated) bank account details
				~ 400 Bad Request
				~ 404 Not Found
