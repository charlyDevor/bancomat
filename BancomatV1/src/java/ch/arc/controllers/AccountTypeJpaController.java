/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.arc.controllers;

import ch.arc.controllers.exceptions.IllegalOrphanException;
import ch.arc.controllers.exceptions.NonexistentEntityException;
import ch.arc.controllers.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ch.arc.entities.Account;
import ch.arc.entities.AccountType;
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
public class AccountTypeJpaController implements Serializable {

    public AccountTypeJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(AccountType accountType) throws RollbackFailureException, Exception {
        if (accountType.getAccountCollection() == null) {
            accountType.setAccountCollection(new ArrayList<Account>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Account> attachedAccountCollection = new ArrayList<Account>();
            for (Account accountCollectionAccountToAttach : accountType.getAccountCollection()) {
                accountCollectionAccountToAttach = em.getReference(accountCollectionAccountToAttach.getClass(), accountCollectionAccountToAttach.getId());
                attachedAccountCollection.add(accountCollectionAccountToAttach);
            }
            accountType.setAccountCollection(attachedAccountCollection);
            em.persist(accountType);
            for (Account accountCollectionAccount : accountType.getAccountCollection()) {
                AccountType oldAccountTypeIdOfAccountCollectionAccount = accountCollectionAccount.getAccountTypeId();
                accountCollectionAccount.setAccountTypeId(accountType);
                accountCollectionAccount = em.merge(accountCollectionAccount);
                if (oldAccountTypeIdOfAccountCollectionAccount != null) {
                    oldAccountTypeIdOfAccountCollectionAccount.getAccountCollection().remove(accountCollectionAccount);
                    oldAccountTypeIdOfAccountCollectionAccount = em.merge(oldAccountTypeIdOfAccountCollectionAccount);
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

    public void edit(AccountType accountType) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            AccountType persistentAccountType = em.find(AccountType.class, accountType.getId());
            Collection<Account> accountCollectionOld = persistentAccountType.getAccountCollection();
            Collection<Account> accountCollectionNew = accountType.getAccountCollection();
            List<String> illegalOrphanMessages = null;
            for (Account accountCollectionOldAccount : accountCollectionOld) {
                if (!accountCollectionNew.contains(accountCollectionOldAccount)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Account " + accountCollectionOldAccount + " since its accountTypeId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Account> attachedAccountCollectionNew = new ArrayList<Account>();
            for (Account accountCollectionNewAccountToAttach : accountCollectionNew) {
                accountCollectionNewAccountToAttach = em.getReference(accountCollectionNewAccountToAttach.getClass(), accountCollectionNewAccountToAttach.getId());
                attachedAccountCollectionNew.add(accountCollectionNewAccountToAttach);
            }
            accountCollectionNew = attachedAccountCollectionNew;
            accountType.setAccountCollection(accountCollectionNew);
            accountType = em.merge(accountType);
            for (Account accountCollectionNewAccount : accountCollectionNew) {
                if (!accountCollectionOld.contains(accountCollectionNewAccount)) {
                    AccountType oldAccountTypeIdOfAccountCollectionNewAccount = accountCollectionNewAccount.getAccountTypeId();
                    accountCollectionNewAccount.setAccountTypeId(accountType);
                    accountCollectionNewAccount = em.merge(accountCollectionNewAccount);
                    if (oldAccountTypeIdOfAccountCollectionNewAccount != null && !oldAccountTypeIdOfAccountCollectionNewAccount.equals(accountType)) {
                        oldAccountTypeIdOfAccountCollectionNewAccount.getAccountCollection().remove(accountCollectionNewAccount);
                        oldAccountTypeIdOfAccountCollectionNewAccount = em.merge(oldAccountTypeIdOfAccountCollectionNewAccount);
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
                Integer id = accountType.getId();
                if (findAccountType(id) == null) {
                    throw new NonexistentEntityException("The accountType with id " + id + " no longer exists.");
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
            AccountType accountType;
            try {
                accountType = em.getReference(AccountType.class, id);
                accountType.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The accountType with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Account> accountCollectionOrphanCheck = accountType.getAccountCollection();
            for (Account accountCollectionOrphanCheckAccount : accountCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This AccountType (" + accountType + ") cannot be destroyed since the Account " + accountCollectionOrphanCheckAccount + " in its accountCollection field has a non-nullable accountTypeId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(accountType);
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

    public List<AccountType> findAccountTypeEntities() {
        return findAccountTypeEntities(true, -1, -1);
    }

    public List<AccountType> findAccountTypeEntities(int maxResults, int firstResult) {
        return findAccountTypeEntities(false, maxResults, firstResult);
    }

    private List<AccountType> findAccountTypeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(AccountType.class));
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

    public AccountType findAccountType(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(AccountType.class, id);
        } finally {
            em.close();
        }
    }

    public int getAccountTypeCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<AccountType> rt = cq.from(AccountType.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
