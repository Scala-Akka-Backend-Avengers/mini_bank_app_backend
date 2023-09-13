package com.fun.mini_bank
package persistence.bank.actor

import akka.actor.typed.ActorRef

import persistence.bank_account.actor.PersistentBankAccount.{Command, CreateBankAccount, UpdateBalance, GetBankAccount}

class Bank {

  /**
   * purpose: store the map of [(key: value) -> (id: actor_reference)] as the state of the Bank actor in order to communicate with the PersistentBankAccount reference when a command is executed on the 'id' of that actor. Using this 'accounts' map, Bank actor will identify the reference of the 'id' received as argument and execute the command associated with the 'id' on that actor reference
   *
   * @param accounts : Map[String, ActorRef[Command]] -> map of bank accounts where key is the bank account 'id' and value is the reference of the actor instance of that bank account
   */
  case class State(accounts: Map[String, ActorRef[Command]])

}

object Bank {

  sealed trait Event

  /**
   * purpose: Event for the Bank actor to keep track of accounts creation in the bank
   *
   * @param id : String -> uuid/id of the newly created bank account
   */
  case class BankAccountCreated(id: String) extends Event

}
