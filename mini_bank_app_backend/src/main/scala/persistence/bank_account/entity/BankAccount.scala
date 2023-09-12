package com.fun.mini_bank
package persistence.bank_account.entity

/*
  entity/state of the persistent bank account actor
 */

/**
 * purpose: entity of the application
 * deliver the current state of the bank account (actor) at any moment
 *
 * @param id       : String -> uuid/id of the bank account
 * @param user     : String -> username of the bank account holder
 * @param currency : String -> default currency of the account for attempting any transaction
 * @param balance  : Double -> current balance in the bank account based on the default currency of the bank account
 */
case class BankAccount(id: String, user: String, currency: String, balance: Double)
