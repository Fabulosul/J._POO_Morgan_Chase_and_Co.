**Tudor Robert-Fabian**
**Anul 2024-2025**
**Grupa 322CAa**

# Proiect Etapa 2 - J. POO Morgan Chase & Co.

## Description:

* The purpose of this assignment is to refactorize the code from the first stage of the project 
to add the new functionalities required in this stage and to make it more modular, scalable and 
readable while using the concepts learned in the first 9 laboratories. The project is in essence a 
banking system that allows users to create bank accounts, cards, make transactions, generate reports
and so on. Some of the new functionalities added in this stage are: adding commissions for certain
transactions based on some user plans, adding a new type of account called "business account" that
is a shared account between multiple users and the posibility to receive different types of amounts
as cashback from making payments to commerciants.

</br>

## Explanations:

### Project structure:

* The project is divided into 10 packages, 1 main class and 1 test class:
    * `bank` - package that contains the classes that define the bank and its functionalities:
        * __BankManager__ - class that has a static method called 'openBank' used to process the 
                                input and then does the action required by each command
        * __Bank__ - class that defines the bank and functionalities like creating or deleting a
                        bank account, finding a bank account by its IBAN or a card by its number, 
                        converting an amount of money from one currency to another, keeping track of
                        split payments and commerciants
    
    * `bankaccounts` - package that contains the classes representing the 3 different types of bank 
                        accounts:
        * __BankAccount__ - class that defines the common fields and methods for all the bank 
                                accounts while representing the classic account
        * __SavingsAccount__ - class that defines the savings account and its functionalities like
                                adding the interest rate, changing the interest rate ans so on
        * __BusinessAccount__ - class that defines the business account and its methods liek adding
                                a business associate, changing the spending or deposit limit for 
                                employees and so on

        **Note:** The savings account and business account extend the bank account class.
    
    * `businessusers` - package that contains the classes representing the 3 different types of 
                        users associated with a business account and a superclass for all of them:
        * __Owner__ - class that defines the owner of the business account which has full rights
        * __Manager__ - class that defines the manager of the business account which can add funds,
                            make payments, create cards and so on
        * __Employee__ - class that defines the employee of the business account which can only make
                            payments below a certain limit
        * __BusinessUser__ - superclass for all the business users defined above
                   
    * `card` - package that contains the class represending the card and its functionalities:
        * __Card__ - class used to check different information about the card, like the type of 
                            the card, if the card is frozer or one-time and so on

    * `cashback` - package containing an Observer Design Pattern used to notify the users when they
                    receive cashback from making payments to commerciants:
        * __CashbackObserver__ - interface used to implement the update method and add a few
                                    common functionalities for the observers
        * __Commerciant__ - class that represent a commerciant and all the information about it
        * __NrOfTransactionsObserver__ - class that implements the CashbackObserver interface and
                                            notifies the users when they receive cashback based on
                                            the number of transactions made to a commerciant
        * __SpendingThresholdObserver__ - class that implements the CashbackObserver interface and
                                            notifies the users when they receive cashback based on
                                            the amount of money spent to a type of commerciants
        * __PaymentDetails__ - class used to parse the payment details and to store them in a more 
                                organized way
        * __Voucher__ - class used to store the information about the cashback given by the 
                            number of transactions to apply it later

     * `commands` - package that contains the classes representing the commands that can be executed
                     inside the bank, the super class for the commands, one factory class and one 
                     interface used to implement the Command Design Pattern:
        * __AcceptSplitPayment__ - set the status of a user in a split payment to accepted
        * __AddAccount__ - creates and adds a new account (savings or classic) to a certain user
        * __AddFunds__ - adds a certain amount a money to an opened account
        * __AddInterest__ - adds the interest rate to a savings account
        * __AddNewBusinessAssociate__ - add a manager or an employee to a business account
        * __BusinessReport__ - make a report for the business account between two timestamps
        * __CashWithdrawal__ - withdraws a certain amount of money from a bank account
        * __ChangeDepositLimit__ - changes the deposit limit for a certain business account
        * __ChangeInterestRate__ - changes the interest rate of a savings account
        * __ChangeSpendingLimit__ - changes the spending limit for a certain business account
        * __CheckCardStatus__ - checks if the bank account corresponding to the card is below the 
                                minimum balance and if it is, it freezes the card
        * __Command__ - super class for all the commands that contains the execute method
        * __CommandFactory__ - factory class that creates a new command object based on the command
                                name
        * __CommandHandler__ - class used to execute the commands
        * __CommandInterface__ - interface used to implement the Command Design Pattern that 
                                    contains the execute method
        * __CreateCard__ - creates a new classic card for a certain bank account
        * __DeleteAccount__ - deletes a certain account from a user
        * __DeleteCard__ - deletes a certain card from a bank account
        * __PayOnline__ - makes an online payment using a card
        * __PrintTransaction__ - adds all the transactions performed by an user to the output
        * __PrintUsers__ - adds all the users with all their accounts and cards to the output
        * __RejectSplitPayment__ - set the status of a user in a split payment to rejected and 
                                    deletes the split payment
        * __Report__ - generates a report of the transactions performed inside a bank account 
                        between two timestamps
        * __SendMoney__ - transfers a certain amount of money from a bank account to another and 
                             does the necessary conversions if the accounts have different 
                             currencies
        * __SetAlias__ - sets an alias for a certain bank account to make it easier to find it for a
                             user
        * __SetMinBalance__ - sets the minimum balance for a certain bank account and if the balance
                                 is below this
                                value, the user is notified
        * __SpendingsReport__ - generates a report of the card transactions performed inside a 
                                  spendings bank account between two timestamps
        * __SplitPayment__ - makes a payment where all the participants pay an equal amount of money
                             from the total sum
        * __UpgradePlan__ - upgrades the plan of a certain user to a new one
        * __WithdrawSavings__ - moves money from a savings account to a classic account

    * `serviceplans` - package that contains the classes representing the 4 different types of plans
                        that a user can have:
        * __UserPlan__ - interface used to implement the Strategy Design Pattern and to define the 
                            functionalities of the plans
        * __StandardPlan__ - class used to define comision for a certain transaction for a standard 
                                user
        * __StudentPlan__ - class that defines the student plan and has 0 commission for all the 
                                transactions
        * __SilverPlan__ - class that defines the silver plan and has a small commission for big 
                            transactions
        * __GoldPlan__ - class that defines the gold plan and has a no commission

        **Note:** The standard plan, student plan, silver plan and gold plan implement the UserPlan 
                    interface.

    * `splitpayment` - package that contains the classes representing the split payments and the 
                        users that participate in them:
        * __SplitPaymentDetails__ - class that defines the split payment and its functionalities 
                                    like adding a user, accepting or rejecting the payment and so on
        * __Participant__ - class used to keep information about each user that participates in a 
                            split payment

    * `transactions` - package that contains the class representing the transactions:
        * __Transaction__ - class that that used a Builder Design Pattern to create a transaction 
                            object and to set the required fields depending on the type of the 
                            transaction and context

    * `user` - package that contains the class representing the users:
        * __User__ - class that defines the user and its functionalities like adding a new account, 
                        deleting an account, upgrading the plan and so on

    * `Main` - represents the main class of the project that processes the input and calls the 
                BankManager to open the bank and execute the commands.
   
    * `Test` - class used to debug the project and to test the functionalities of the classes and methods.


### Design Patterns:

* `Command Design Pattern` - used to encapsulate a command as an object, therefore allowing to add 
new commands without changing the existing code.
    - Location: commands package
* `Factory Design Pattern` - used to create a new command object based on the command name which 
makes possible the addition of new actions in the bank without modifying the project structure. It 
is also used when assigning a new plan to a user and when creating a new account.
    - Location: commands package, user class, addAccount class
* `Builder Design Pattern` - used to create a transaction object and to set the required fields
depending on the type of the transaction and context and can be easily extended to add new fields.
    - Location: transaction class in the transactions package
*  `Observer Design Pattern` - used to notify the users when they receive cashback from making 
an online payment or sending money to a commerciant. It is used to automatically apply the cashback
to the users when they meet the criteria or for the nrOfTransactionsObserver to consume the vouchers
when the payment is eligible for it without the need for the user to do anything.
    - Location: cashback package
* `Strategy Design Pattern` - used for the user plans to define the different comissions for the
transactions based on the user plan. Its main purpose is the ability to define it by its 
interface and to easily swap between the different plans. Also, it can be easily extended to add
new plans in the future.
    - Location: serviceplans package


### General observations:

* The project is split into multiple packages to make it more modular and to make it easier to
understand the functionalities of each class and to make it easier to extend it in the future.

* The project uses HashMaps for faster access to the certain information and to store the it in a 
more organized way. However, it does not use HashMaps for all the information due to the fact that 
it would drastically increase the space complexity and would make it harder to understand certain 
functionalities. 

* The project uses Javadoc comments to explain in detail the need and the use of each method and to 
make it easier for a future reader to understand the implementation of the code.

</br>

## Conclusion:

* This second part of the assignment was a great opportunity to understand how important is to write
modular and scalable code because if the code is not written in a good way, it can be very hard to
add new functionalities. However, by using the concepts learned in the first 9 laboratories, I was
able to refactorize the code from the first stage of the project and to add the new implementations
required in this stage. It is important to mention that the project is meant to be extended in the 
future and that is why I split the code into multiple packages and used design patterns like 
Command, Factory, Builder, Observer and Strategy, to ensure flexibility.



















