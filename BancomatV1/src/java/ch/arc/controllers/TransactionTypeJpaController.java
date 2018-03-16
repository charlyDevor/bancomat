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
import ch.arc.entities.Transaction;
import ch.arc.entities.TransactionType;
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
public class TransactionTypeJpaController implements Serializable {

    public TransactionTypeJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TransactionType transactionType) throws RollbackFailureException, Exception {
        if (transactionType.getTransactionCollection() == null) {
            transactionType.setTransactionCollection(new ArrayList<Transaction>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Transaction> attachedTransactionCollection = new ArrayList<Transaction>();
            for (Transaction transactionCollectionTransactionToAttach : transactionType.getTransactionCollection()) {
                transactionCollectionTransactionToAttach = em.getReference(transactionCollectionTransactionToAttach.getClass(), transactionCollectionTransactionToAttach.getId());
                attachedTransactionCollection.add(transactionCollectionTransactionToAttach);
            }
            transactionType.setTransactionCollection(attachedTransactionCollection);
            em.persist(transactionType);
            for (Transaction transactionCollectionTransaction : transactionType.getTransactionCollection()) {
                TransactionType oldTransactionTypeIdOfTransactionCollectionTransaction = transactionCollectionTransaction.getTransactionTypeId();
                transactionCollectionTransaction.setTransactionTypeId(transactionType);
                transactionCollectionTransaction = em.merge(transactionCollectionTransaction);
                if (oldTransactionTypeIdOfTransactionCollectionTransaction != null) {
                    oldTransactionTypeIdOfTransactionCollectionTransaction.getTransactionCollection().remove(transactionCollectionTransaction);
                    oldTransactionTypeIdOfTransactionCollectionTransaction = em.merge(oldTransactionTypeIdOfTransactionCollectionTransaction);
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

    public void edit(TransactionType transactionType) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            TransactionType persistentTransactionType = em.find(TransactionType.class, transactionType.getId());
            Collection<Transaction> transactionCollectionOld = persistentTransactionType.getTransactionCollection();
            Collection<Transaction> transactionCollectionNew = transactionType.getTransactionCollection();
            List<String> illegalOrphanMessages = null;
            for (Transaction transactionCollectionOldTransaction : transactionCollectionOld) {
                if (!transactionCollectionNew.contains(transactionCollectionOldTransaction)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Transaction " + transactionCollectionOldTransaction + " since its transactionTypeId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Transaction> attachedTransactionCollectionNew = new ArrayList<Transaction>();
            for (Transaction transactionCollectionNewTransactionToAttach : transactionCollectionNew) {
                transactionCollectionNewTransactionToAttach = em.getReference(transactionCollectionNewTransactionToAttach.getClass(), transactionCollectionNewTransactionToAttach.getId());
                attachedTransactionCollectionNew.add(transactionCollectionNewTransactionToAttach);
            }
            transactionCollectionNew = attachedTransactionCollectionNew;
            transactionType.setTransactionCollection(transactionCollectionNew);
            transactionType = em.merge(transactionType);
            for (Transaction transactionCollectionNewTransaction : transactionCollectionNew) {
                if (!transactionCollectionOld.contains(transactionCollectionNewTransaction)) {
                    TransactionType oldTransactionTypeIdOfTransactionCollectionNewTransaction = transactionCollectionNewTransaction.getTransactionTypeId();
                    transactionCollectionNewTransaction.setTransactionTypeId(transactionType);
                    transactionCollectionNewTransaction = em.merge(transactionCollectionNewTransaction);
                    if (oldTransactionTypeIdOfTransactionCollectionNewTransaction != null && !oldTransactionTypeIdOfTransactionCollectionNewTransaction.equals(transactionType)) {
                        oldTransactionTypeIdOfTransactionCollectionNewTransaction.getTransactionCollection().remove(transactionCollectionNewTransaction);
                        oldTransactionTypeIdOfTransactionCollectionNewTransaction = em.merge(oldTransactionTypeIdOfTransactionCollectionNewTransaction);
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
                Integer id = transactionType.getId();
                if (findTransactionType(id) == null) {
                    throw new NonexistentEntityException("The transactionType with id " + id + " no longer exists.");
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
            TransactionType transactionType;
            try {
                transactionType = em.getReference(TransactionType.class, id);
                transactionType.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The transactionType with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Transaction> transactionCollectionOrphanCheck = transactionType.getTransactionCollection();
            for (Transaction transactionCollectionOrphanCheckTransaction : transactionCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TransactionType (" + transactionType + ") cannot be destroyed since the Transaction " + transactionCollectionOrphanCheckTransaction + " in its transactionCollection field has a non-nullable transactionTypeId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(transactionType);
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

    public List<TransactionType> findTransactionTypeEntities() {
        return findTransactionTypeEntities(true, -1, -1);
    }

    public List<TransactionType> findTransactionTypeEntities(int maxResults, int firstResult) {
        return findTransactionTypeEntities(false, maxResults, firstResult);
    }

    private List<TransactionType> findTransactionTypeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TransactionType.class));
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

    public TransactionType findTransactionType(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TransactionType.class, id);
        } finally {
            em.close();
        }
    }

    public int getTransactionTypeCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TransactionType> rt = cq.from(TransactionType.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
