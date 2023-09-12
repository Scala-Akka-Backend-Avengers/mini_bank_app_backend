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

  /*
    set of operations/commands this actor can receive and operate on
   */

  /**
   * purpose: - bind all the actions to be performed on this actor together using inheritance approach
   *          - use this trait as a tag for all the actions that can be performed with this actor
   *          - once this trait is defined as the root of any action to be performed on this actor, making it sealed to avoid definition of an action on this actor outside this class, thus preventing an external action definition for this actor
   */
  sealed trait Command

  /**
   * purpose: action/command for creation of the account in the bank
   *
   * @param user          : String -> username of the account holder user
   * @param currency      : String -> default currency of the account for attempting any transaction
   * @param initialAmount : Double -> the basic amount with which an account is created
   * @param replyTo
   */
  case class CreateBankAccount(user: String, currency: String, initialAmount: Double, replyTo: ActorRef[Response]) extends Command

  /**
   * purpose: action/command to update the balance in the bank account
   *
   * @param id       : Long -> uuid/id of the bank account
   * @param currency : String -> specify the currency type if the transaction is done with other currency (currency exchange)
   * @param amount   : Double -> amount involved in the transaction causing this update in the bank account. +ve for deposit and -ve for withdrawal
   * @param replyTo  :
   */
  case class UpdateBalance(id: Long, currency: String, amount: Double, replyTo: ActorRef[Response]) extends Command

  /**
   * purpose: action/command to get the details of the bank account
   *
   * @param id : String -> uuid/id of the account whose information is to be fetched
   * @param replyTo
   */
  case class GetBankAccount(id: String, replyTo: ActorRef[Response]) extends Command

  /*
    events to be triggered for persistence to Cassandra
   */

  /**
   * purpose: - bind all the events (triggered after completion of an actor action) to be persisted with Cassandra together using inheritance approach
   *          - use this trait as a tag for all the for all the persistence events for this actor
   *          - once this trait is defined as the root of any event that is triggered on performing an action on this actor, making it sealed to avoid definition of an event (relevant to an action) for this actor outside this class, thus preventing an external event definition for an action of this actor
   */
  sealed trait Event

  /**
   * purpose: response event after the bank account is created
   *
   * @param id       : String -> uuid/id assigned to the bank account on creation
   * @param user     : String -> username of the bank account holder
   * @param currency : String -> the currency with which user want to deposit/transact as default
   * @param balance  : Double -> the initial amount of deposition with which the account was created
   */
  case class BankAccountCreated(id: String, user: String, currency: String, balance: Double) extends Event

  /**
   * purpose: response event after the bank account's balance is updated
   *
   * @param id             : String -> uuid/id of the bank account
   * @param user           : String -> username of the bank account holder
   * @param updatedBalance : Double -> updated balance in the account after the 'UpdateBalance' command was executed on this actor
   * @param currency       : String -> the currency with which user did deposit/transact
   */
  case class BalanceUpdated(id: String, user: String, updatedBalance: Double, currency: String) extends Event

  sealed trait Response

}
