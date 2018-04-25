package ch.arc.ejbsessionfacade;

import ch.arc.metier.ejb.Account;
import ch.arc.metier.ejb.Transaction;
import ch.arc.metier.ejb.TransactionType;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Ombiche
 */
@Stateless
public class AccountFacade extends AbstractFacade<Account> {

    @EJB
    private ch.arc.ejbsessionfacade.TransactionFacade transactionFacade;

    @PersistenceContext(unitName = "BancomatV1PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AccountFacade() {
        super(Account.class);
    }

    //Account
    public List<Account> findByUserId(Integer userId) {
        List<Account> accList = em.createNamedQuery("Account.findByUserId", Account.class).setParameter("userId", userId)
                .getResultList();
        return accList;
    }

    //Deposit
    public void deposit(Integer accountNumber, double amount,String description, int type) {
        Account account = this.getAccount(accountNumber);
        account.setBalance(account.getBalance() + amount);
        //em.merge(account);
        this.edit(account);
        //Create Transaction
        transaction(amount, type, description, account.getId());
    }
    //Withdrawal
    public int withdrawal(Integer accountNumber, double amount, String description, int type) {
        Account account = this.getAccount(accountNumber);
        if (account.getBalance() >= amount && account.getAccountTypeId().getWithdrawalLimit() >= amount) {
            account.setBalance(account.getBalance() - amount);
            //em.merge(account);
            this.edit(account);
            //Create Transaction
            transaction(amount, type, description, account.getId());
            return 1;
        }
        return 0;
    }

    //Return account
    public Account getAccount(Integer accountNumber) {
        return em.createNamedQuery("Account.findByAccountNumber", Account.class)
                .setParameter("accountNumber", accountNumber).getSingleResult();
    }

    //Send 
    public void sendAmount(Integer accountNumberCurrent, Integer accountNumberDestination, double amount, String description,int type) {
        if (withdrawal(accountNumberCurrent, amount, "Verssement au compte "+accountNumberDestination+" Motif: "+description, type) == 1) {
            deposit(accountNumberDestination, amount, "Reçu de "+accountNumberCurrent+") Motif: "+description, type);
        }
    }

    //Save transaction
    public void transaction(double amount, int trType, String description, Integer accId) {
        try {
            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setTransactionTypeId(new TransactionType(trType));
            transaction.setDescription(description);
            transaction.setAccountId(new Account(accId));
            transaction.setTransactionDate(new Date());
            transactionFacade.create(transaction);
            
            
        } catch (Exception ex) {
            ex.getMessage();
        }
    }
}
