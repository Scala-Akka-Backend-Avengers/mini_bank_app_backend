package com.fun.mini_bank
package persistence.bank_account.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

import persistence.bank_account.entity.BankAccount

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
  import PersistentBankAccount._
  /*
    There are three important parts of the implementation of an Actor class:
      1. Command/Action handler (Upon receiving a command/action/message, the corresponding (command/action/message) handler will persist an event)
      2. Event handler (Once the event is persisted in the persistent store, i.e. Cassandra in this case, the event handler will update state)
      3. Actor state (Every persistent actor has some state. The state update (if any) done by the event handler will then be used as the latest state for the next command/action on the actor)
   */

  //region Command/Action handler
  /**
   * purpose: this function takes in command with the latest actor's state to:
   *          1. Trigger an event to be persisted in the database
   */
  private val commandHandler: (BankAccount, Command) => Effect[Event, BankAccount] =
    (actorCurrentState, commandForActor) => commandForActor match {
      /*
          The stages in which this works is as below:
            1. Root bank actor creates the PersistentBankAccount actor using the apply() method
            2. Bank actor sends the CreateBankAccount command to PersistentBankAccount actor
            3. (From inside commandHandler) PersistentBankAccount actor triggers an event BankAccountCreated and persists it
            4. (From inside eventHandler) PersistentBankAccount actor updates its state referring to the event persisted
            5. Replies back to the root bank account actor with the BankAccountCreatedResponse response
            6. The root bank actor surfaces the response to the HTTP server
       */
      case CreateBankAccount(user, currency, initialAmount, rootBankActor) =>
        val id = actorCurrentState.id
        Effect.persist[Event, BankAccount](BankAccountCreated(BankAccount(id, user, currency, initialAmount))).thenReply[Response](rootBankActor)((_: BankAccount) => BankAccountCreatedResponse(id)
        )
      case UpdateBalance(_, _, deltaAmount, rootBankActor) =>
        val updatedBalance = actorCurrentState.balance + deltaAmount
        /*
           this scenario is not possible where we withdraw amount more than the balance in the account
           So, no event is triggered to persist, we are only replying to the actor
         */
        if (updatedBalance < 0)
          Effect.reply[Response, Event, BankAccount](rootBankActor)(BankAccountBalanceUpdatedResponse(None))
        else
          Effect.persist[Event, BankAccount](BalanceUpdated(deltaAmount)).thenReply[Response](rootBankActor)((balanceUpdatedBankAccount: BankAccount) => BankAccountBalanceUpdatedResponse(Some(balanceUpdatedBankAccount)))
      /*
         - this being a case of a simple read operation, we can directly use the reply() method
         - there is nothing to persist
         - we just need to return Some(currentState) to the right-full actor which is having the ID as mentioned in the command, so the ID value can be ignored writing it as '_' in the command
       */
      case GetBankAccount(_, rootBankActor) =>
        Effect.reply[Response, Event, BankAccount](rootBankActor)(GetBankAccountResponse(Some(actorCurrentState)))
    }
  //endregion

  //region Event handler
  /**
   * purpose: this function takes in the event just persisted in the database with the current state of the actor to:
   *          1. Update the state of the actor based on the command/action performed and generate latest state for the actor
   */
  private val eventHandler: (BankAccount, Event) => BankAccount =
    (actorCurrentState, event) => event match {
      case BankAccountCreated(newBankAccount) => newBankAccount
      case BalanceUpdated(deltaAmount) => actorCurrentState.copy(balance = actorCurrentState.balance + deltaAmount)
    }
  //endregion

  def apply(id: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, BankAccount](
      persistenceId = PersistenceId.ofUniqueId(id),
      commandHandler = commandHandler,
      eventHandler = eventHandler,
      emptyState = BankAccount(id, "", "", 0.0)
    )

}

object PersistentBankAccount {
  //region actor-actions
  /*
    set of operations/commands this actor can receive and operate on
   */

  /**
   * purpose: - bind all the actions to be performed on this actor together using inheritance approach
   *          - use this trait as a tag for all the actions that can be performed with this actor
   *          - once this trait is defined as the root of any action to be performed on this actor, making it sealed to avoid definition of an action on this actor outside this class, thus preventing an external action definition for this actor
   */
  private sealed trait Command

  /**
   * purpose: action/command for creation of the account in the bank
   *
   * @param user          : String -> username of the account holder user
   * @param currency      : String -> default currency of the account for attempting any transaction
   * @param initialAmount : Double -> the basic amount with which an account is created
   * @param replyTo       : ActorRef[Response] -> one of the decorated responses of type Response is sent to the root bank actor
   */
  private case class CreateBankAccount(user: String, currency: String, initialAmount: Double, replyTo: ActorRef[Response]) extends Command

  /**
   * purpose: action/command to update the balance in the bank account
   *
   * @param id       : Long -> uuid/id of the bank account
   * @param currency : String -> specify the currency type if the transaction is done with other currency (currency exchange)
   * @param amount   : Double -> amount involved in the transaction causing this update in the bank account. +ve for deposit and -ve for withdrawal
   * @param replyTo  : ActorRef[Response] -> one of the decorated responses of type Response is sent to the root bank actor
   */
  private case class UpdateBalance(id: String, currency: String, amount: Double, replyTo: ActorRef[Response]) extends Command

  /**
   * purpose: action/command to get the details of the bank account
   *
   * @param id      : String -> uuid/id of the account whose information is to be fetched
   * @param replyTo : ActorRef[Response] -> one of the decorated responses of type Response is sent to the root bank actor
   */
  private case class GetBankAccount(id: String, replyTo: ActorRef[Response]) extends Command
  //endregion

  //region actor-events
  /*
    events to be triggered for persistence to Cassandra
   */

  /**
   * purpose: - bind all the events (triggered after completion of an actor action) to be persisted with Cassandra together using inheritance approach
   *          - use this trait as a tag for all the for all the persistence events for this actor
   *          - once this trait is defined as the root of any event that is triggered on performing an action on this actor, making it sealed to avoid definition of an event (relevant to an action) for this actor outside this class, thus preventing an external event definition for an action of this actor
   */
  private sealed trait Event

  /**
   * purpose: response event after the bank account is created
   *
   * @param bankAccount : BankAccount -> this is the (case class) instance of the bank account with parameters as below:
   *                    id       : String -> uuid/id assigned to the bank account on creation
   *                    user     : String -> username of the bank account holder
   *                    currency : String -> the currency with which user want to deposit/transact as default
   *                    balance  : Double -> the initial amount of deposition with which the account was created
   */
  private case class BankAccountCreated(bankAccount: BankAccount) extends Event

  /**
   * purpose: response event after the bank account's balance is updated
   *
   * @param updatedBalanceBy : Double -> amount by which the balance was updated in the account after the 'UpdateBalance' command was executed on this actor
   */
  private case class BalanceUpdated(updatedBalanceBy: Double) extends Event
  //endregion

  //region actor-responses
  /*
    set of responses this actor will generate to reply after performing any of the commands associated with this actor 'PersistentBankAccount'
   */

  /**
   * purpose: - bind all the responses (generated after completion of an actor action) to be replied to another actor using inheritance approach
   *          - use this trait as a tag for all the for all the responses of an action on this actor
   *          - once this trait is defined as the root of response that is generated on completion of an action on this actor, making it sealed to avoid definition of a response (relevant to an action) for this actor outside this class, thus preventing an external response definition for an action of this actor
   */
  private sealed trait Response

  /**
   * purpose: response to be generated on successful bank account creation
   *
   * @param id : String -> uuid/id of the newly created bank account
   */
  private case class BankAccountCreatedResponse(id: String) extends Response

  /**
   * purpose: response to be generated on bank account update action completion
   *
   * @param possibleBankAccount : Option[BankAccount] -> If the bank account balance update operation is performed a non-existent bank account (using invalid bank account id), then the value of this parameter is None; else if the balance update operation is performed on an existent bank account (using valid bank account id), then the value of this parameter will be Some[BankAccount]
   */
  private case class BankAccountBalanceUpdatedResponse(possibleBankAccount: Option[BankAccount]) extends Response

  /**
   * purpose: response to be generated on request completion of getting details of a bank account
   *
   * @param possibleBankAccount : Option[BankAccount] -> If the bank account details are requested for a non-existent bank account (using invalid bank account id), then the value of this parameter is None; else if the account details are requested for an existent bank account (using valid bank account id), then the value of this parameter will be Some[BankAccount]
   */
  private case class GetBankAccountResponse(possibleBankAccount: Option[BankAccount]) extends Response
  //endregion
}
