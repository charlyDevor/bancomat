package ch.arc.jpacontroller;

import ch.arc.jpacontroller.exceptions.IllegalOrphanException;
import ch.arc.jpacontroller.exceptions.NonexistentEntityException;
import ch.arc.jpacontroller.exceptions.RollbackFailureException;
import ch.arc.metier.ejb.Account;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ch.arc.metier.ejb.AccountType;
import ch.arc.metier.ejb.User;
import ch.arc.metier.ejb.Transaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Ombiche
 */
public class AccountJpaController implements Serializable {

    public AccountJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Account account) throws RollbackFailureException, Exception {
        if (account.getTransactionCollection() == null) {
            account.setTransactionCollection(new ArrayList<Transaction>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            AccountType accountTypeId = account.getAccountTypeId();
            if (accountTypeId != null) {
                accountTypeId = em.getReference(accountTypeId.getClass(), accountTypeId.getId());
                account.setAccountTypeId(accountTypeId);
            }
            User userId = account.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                account.setUserId(userId);
            }
            Collection<Transaction> attachedTransactionCollection = new ArrayList<Transaction>();
            for (Transaction transactionCollectionTransactionToAttach : account.getTransactionCollection()) {
                transactionCollectionTransactionToAttach = em.getReference(transactionCollectionTransactionToAttach.getClass(), transactionCollectionTransactionToAttach.getId());
                attachedTransactionCollection.add(transactionCollectionTransactionToAttach);
            }
            account.setTransactionCollection(attachedTransactionCollection);
            em.persist(account);
            if (accountTypeId != null) {
                accountTypeId.getAccountCollection().add(account);
                accountTypeId = em.merge(accountTypeId);
            }
            if (userId != null) {
                userId.getAccountCollection().add(account);
                userId = em.merge(userId);
            }
            for (Transaction transactionCollectionTransaction : account.getTransactionCollection()) {
                Account oldAccountIdOfTransactionCollectionTransaction = transactionCollectionTransaction.getAccountId();
                transactionCollectionTransaction.setAccountId(account);
                transactionCollectionTransaction = em.merge(transactionCollectionTransaction);
                if (oldAccountIdOfTransactionCollectionTransaction != null) {
                    oldAccountIdOfTransactionCollectionTransaction.getTransactionCollection().remove(transactionCollectionTransaction);
                    oldAccountIdOfTransactionCollectionTransaction = em.merge(oldAccountIdOfTransactionCollectionTransaction);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Account account) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Account persistentAccount = em.find(Account.class, account.getId());
            AccountType accountTypeIdOld = persistentAccount.getAccountTypeId();
            AccountType accountTypeIdNew = account.getAccountTypeId();
            User userIdOld = persistentAccount.getUserId();
            User userIdNew = account.getUserId();
            Collection<Transaction> transactionCollectionOld = persistentAccount.getTransactionCollection();
            Collection<Transaction> transactionCollectionNew = account.getTransactionCollection();
            List<String> illegalOrphanMessages = null;
            for (Transaction transactionCollectionOldTransaction : transactionCollectionOld) {
                if (!transactionCollectionNew.contains(transactionCollectionOldTransaction)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Transaction " + transactionCollectionOldTransaction + " since its accountId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (accountTypeIdNew != null) {
                accountTypeIdNew = em.getReference(accountTypeIdNew.getClass(), accountTypeIdNew.getId());
                account.setAccountTypeId(accountTypeIdNew);
            }
            if (userIdNew != null) {
                userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
                account.setUserId(userIdNew);
            }
            Collection<Transaction> attachedTransactionCollectionNew = new ArrayList<Transaction>();
            for (Transaction transactionCollectionNewTransactionToAttach : transactionCollectionNew) {
                transactionCollectionNewTransactionToAttach = em.getReference(transactionCollectionNewTransactionToAttach.getClass(), transactionCollectionNewTransactionToAttach.getId());
                attachedTransactionCollectionNew.add(transactionCollectionNewTransactionToAttach);
            }
            transactionCollectionNew = attachedTransactionCollectionNew;
            account.setTransactionCollection(transactionCollectionNew);
            account = em.merge(account);
            if (accountTypeIdOld != null && !accountTypeIdOld.equals(accountTypeIdNew)) {
                accountTypeIdOld.getAccountCollection().remove(account);
                accountTypeIdOld = em.merge(accountTypeIdOld);
            }
            if (accountTypeIdNew != null && !accountTypeIdNew.equals(accountTypeIdOld)) {
                accountTypeIdNew.getAccountCollection().add(account);
                accountTypeIdNew = em.merge(accountTypeIdNew);
            }
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getAccountCollection().remove(account);
                userIdOld = em.merge(userIdOld);
            }
            if (userIdNew != null && !userIdNew.equals(userIdOld)) {
                userIdNew.getAccountCollection().add(account);
                userIdNew = em.merge(userIdNew);
            }
            for (Transaction transactionCollectionNewTransaction : transactionCollectionNew) {
                if (!transactionCollectionOld.contains(transactionCollectionNewTransaction)) {
                    Account oldAccountIdOfTransactionCollectionNewTransaction = transactionCollectionNewTransaction.getAccountId();
                    transactionCollectionNewTransaction.setAccountId(account);
                    transactionCollectionNewTransaction = em.merge(transactionCollectionNewTransaction);
                    if (oldAccountIdOfTransactionCollectionNewTransaction != null && !oldAccountIdOfTransactionCollectionNewTransaction.equals(account)) {
                        oldAccountIdOfTransactionCollectionNewTransaction.getTransactionCollection().remove(transactionCollectionNewTransaction);
                        oldAccountIdOfTransactionCollectionNewTransaction = em.merge(oldAccountIdOfTransactionCollectionNewTransaction);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = account.getId();
                if (findAccount(id) == null) {
                    throw new NonexistentEntityException("The account with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Account account;
            try {
                account = em.getReference(Account.class, id);
                account.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The account with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Transaction> transactionCollectionOrphanCheck = account.getTransactionCollection();
            for (Transaction transactionCollectionOrphanCheckTransaction : transactionCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Account (" + account + ") cannot be destroyed since the Transaction " + transactionCollectionOrphanCheckTransaction + " in its transactionCollection field has a non-nullable accountId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            AccountType accountTypeId = account.getAccountTypeId();
            if (accountTypeId != null) {
                accountTypeId.getAccountCollection().remove(account);
                accountTypeId = em.merge(accountTypeId);
            }
            User userId = account.getUserId();
            if (userId != null) {
                userId.getAccountCollection().remove(account);
                userId = em.merge(userId);
            }
            em.remove(account);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Account> findAccountEntities() {
        return findAccountEntities(true, -1, -1);
    }

    public List<Account> findAccountEntities(int maxResults, int firstResult) {
        return findAccountEntities(false, maxResults, firstResult);
    }

    private List<Account> findAccountEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Account.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Account findAccount(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Account.class, id);
        } finally {
            em.close();
        }
    }

    public int getAccountCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Account> rt = cq.from(Account.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
