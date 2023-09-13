package com.fun.mini_bank
package persistence.bank.actor

class Bank {

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
