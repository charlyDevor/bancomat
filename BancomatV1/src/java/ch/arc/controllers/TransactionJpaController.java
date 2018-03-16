/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.arc.controllers;

import ch.arc.controllers.exceptions.NonexistentEntityException;
import ch.arc.controllers.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ch.arc.entities.Account;
import ch.arc.entities.Transaction;
import ch.arc.entities.TransactionType;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Ombiche
 */
public class TransactionJpaController implements Serializable {

    public TransactionJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Transaction transaction) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Account accountId = transaction.getAccountId();
            if (accountId != null) {
                accountId = em.getReference(accountId.getClass(), accountId.getId());
                transaction.setAccountId(accountId);
            }
            TransactionType transactionTypeId = transaction.getTransactionTypeId();
            if (transactionTypeId != null) {
                transactionTypeId = em.getReference(transactionTypeId.getClass(), transactionTypeId.getId());
                transaction.setTransactionTypeId(transactionTypeId);
            }
            em.persist(transaction);
            if (accountId != null) {
                accountId.getTransactionCollection().add(transaction);
                accountId = em.merge(accountId);
            }
            if (transactionTypeId != null) {
                transactionTypeId.getTransactionCollection().add(transaction);
                transactionTypeId = em.merge(transactionTypeId);
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

    public void edit(Transaction transaction) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Transaction persistentTransaction = em.find(Transaction.class, transaction.getId());
            Account accountIdOld = persistentTransaction.getAccountId();
            Account accountIdNew = transaction.getAccountId();
            TransactionType transactionTypeIdOld = persistentTransaction.getTransactionTypeId();
            TransactionType transactionTypeIdNew = transaction.getTransactionTypeId();
            if (accountIdNew != null) {
                accountIdNew = em.getReference(accountIdNew.getClass(), accountIdNew.getId());
                transaction.setAccountId(accountIdNew);
            }
            if (transactionTypeIdNew != null) {
                transactionTypeIdNew = em.getReference(transactionTypeIdNew.getClass(), transactionTypeIdNew.getId());
                transaction.setTransactionTypeId(transactionTypeIdNew);
            }
            transaction = em.merge(transaction);
            if (accountIdOld != null && !accountIdOld.equals(accountIdNew)) {
                accountIdOld.getTransactionCollection().remove(transaction);
                accountIdOld = em.merge(accountIdOld);
            }
            if (accountIdNew != null && !accountIdNew.equals(accountIdOld)) {
                accountIdNew.getTransactionCollection().add(transaction);
                accountIdNew = em.merge(accountIdNew);
            }
            if (transactionTypeIdOld != null && !transactionTypeIdOld.equals(transactionTypeIdNew)) {
                transactionTypeIdOld.getTransactionCollection().remove(transaction);
                transactionTypeIdOld = em.merge(transactionTypeIdOld);
            }
            if (transactionTypeIdNew != null && !transactionTypeIdNew.equals(transactionTypeIdOld)) {
                transactionTypeIdNew.getTransactionCollection().add(transaction);
                transactionTypeIdNew = em.merge(transactionTypeIdNew);
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
                Integer id = transaction.getId();
                if (findTransaction(id) == null) {
                    throw new NonexistentEntityException("The transaction with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Transaction transaction;
            try {
                transaction = em.getReference(Transaction.class, id);
                transaction.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The transaction with id " + id + " no longer exists.", enfe);
            }
            Account accountId = transaction.getAccountId();
            if (accountId != null) {
                accountId.getTransactionCollection().remove(transaction);
                accountId = em.merge(accountId);
            }
            TransactionType transactionTypeId = transaction.getTransactionTypeId();
            if (transactionTypeId != null) {
                transactionTypeId.getTransactionCollection().remove(transaction);
                transactionTypeId = em.merge(transactionTypeId);
            }
            em.remove(transaction);
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

    public List<Transaction> findTransactionEntities() {
        return findTransactionEntities(true, -1, -1);
    }

    public List<Transaction> findTransactionEntities(int maxResults, int firstResult) {
        return findTransactionEntities(false, maxResults, firstResult);
    }

    private List<Transaction> findTransactionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Transaction.class));
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

    public Transaction findTransaction(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Transaction.class, id);
        } finally {
            em.close();
        }
    }

    public int getTransactionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Transaction> rt = cq.from(Transaction.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
