package com.example.thread.livelock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {
	double balance;
	int id;
	Lock lock = new ReentrantLock();

	BankAccount(int id, double balance) {
		this.id = id;
		this.balance = balance;
	}

	boolean withdraw(double amount) {
		if (this.lock.tryLock()) {
			// Wait to simulate io like database access ...
			try {Thread.sleep(10l);} catch (InterruptedException e) {}
			balance -= amount;
			return true;
		}
		return false;
	}

	boolean deposit(double amount) {
		if (this.lock.tryLock()) {
			// Wait to simulate io like database access ...
			try {Thread.sleep(10l);} catch (InterruptedException e) {}
			balance += amount;
			return true;
		}
		return false;
	}

	public boolean tryTransfer(BankAccount destinationAccount, double amount) {
		if (this.withdraw(amount)) {
			if (destinationAccount.deposit(amount)) {
				return true;
			} else {
				// destination account busy, refund source account.
				this.deposit(amount);
			}
		}

		return false;
	}

	public static void main(String[] args) {
		final BankAccount fooAccount = new BankAccount(1, 500d);
		final BankAccount barAccount = new BankAccount(2, 500d);

		new Thread(new Transaction(fooAccount, barAccount, 10d), "transaction-1").start();
		new Thread(new Transaction(barAccount, fooAccount, 10d), "transaction-2").start();

	}

}
class Transaction implements Runnable {
	private BankAccount sourceAccount, destinationAccount;
	private double amount;

	Transaction(BankAccount sourceAccount, BankAccount destinationAccount, double amount) {
		this.sourceAccount = sourceAccount;
		this.destinationAccount = destinationAccount;
		this.amount = amount;
	}

	public void run() {
		while (!sourceAccount.tryTransfer(destinationAccount, amount))
			continue;
		System.out.printf("%s completed ", Thread.currentThread().getName());
	}

}
