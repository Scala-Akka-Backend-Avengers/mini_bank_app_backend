package com.fun.mini_bank
package persistence.bank_account.actor

import akka.actor.typed.ActorRef

/**
 * actor entity category: single bank account
 * actor hierarchy level: base of the hierarchy
 * description: this persistent bank account will be developed as a persistent actor out-of-the-box
 * operations:
 *  - receive messages (events)
 *  - persist messages (events): event-sourcing approach to persist the data
 *    (event-sourcing: unlike the traditional database where we store the latest state of the data in the database, in event-sourcing we store event (bits of data) which complies to journey to the latest state of the data at this moment. The latest state of the data can be obtained by playing the events one-by-one. This approach (event-sourcing) is a bit costly but it is also much more powerful and richer in information for a variety of reasons, for eg. in the case of event-sourcing for fault-tolerance, i.e. the application fails for some reason (it crashes) or if the actor dies, or if the server goes down, you will have the entire journey to the latest state of the application written in the database and we can simply replay the all the events and come back to the same state in the database. Also, in the case of a bank account, it is important to audit the entire journey of the bank account for auditing purpose and if some suspicious transaction take place in the past that you did not notice then you can always re-visit that figure out if something went wrong at that time or do something about it even if that transaction, withdrawal or deposit or whatever occurred earlier in time)
 *    So, fault tolerance and auditing are the two biggest reasons we want to use event-sourcing scheme in the database management
 *    commands: messages that the actors will communicate asynchronously
 *    events: data-structures to be persisted to Cassandra
 *    entity/state: internal state of the bank account, the data-structure of this persistent bank account actor
 *    responses: to be sent to whoever queries or tries to modify the bank account
 */
class PersistentBankAccount {

  

}
