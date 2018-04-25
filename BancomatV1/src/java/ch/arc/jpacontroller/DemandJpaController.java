/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.arc.jpacontroller;

import ch.arc.jpacontroller.exceptions.NonexistentEntityException;
import ch.arc.jpacontroller.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ch.arc.metier.ejb.AccountType;
import ch.arc.metier.ejb.Demand;
import ch.arc.metier.ejb.User;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Ombiche
 */
public class DemandJpaController implements Serializable {

    public DemandJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Demand demand) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            AccountType accountTypeId = demand.getAccountTypeId();
            if (accountTypeId != null) {
                accountTypeId = em.getReference(accountTypeId.getClass(), accountTypeId.getId());
                demand.setAccountTypeId(accountTypeId);
            }
            User userId = demand.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                demand.setUserId(userId);
            }
            em.persist(demand);
            if (accountTypeId != null) {
                accountTypeId.getDemandCollection().add(demand);
                accountTypeId = em.merge(accountTypeId);
            }
            if (userId != null) {
                userId.getDemandCollection().add(demand);
                userId = em.merge(userId);
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

    public void edit(Demand demand) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Demand persistentDemand = em.find(Demand.class, demand.getId());
            AccountType accountTypeIdOld = persistentDemand.getAccountTypeId();
            AccountType accountTypeIdNew = demand.getAccountTypeId();
            User userIdOld = persistentDemand.getUserId();
            User userIdNew = demand.getUserId();
            if (accountTypeIdNew != null) {
                accountTypeIdNew = em.getReference(accountTypeIdNew.getClass(), accountTypeIdNew.getId());
                demand.setAccountTypeId(accountTypeIdNew);
            }
            if (userIdNew != null) {
                userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
                demand.setUserId(userIdNew);
            }
            demand = em.merge(demand);
            if (accountTypeIdOld != null && !accountTypeIdOld.equals(accountTypeIdNew)) {
                accountTypeIdOld.getDemandCollection().remove(demand);
                accountTypeIdOld = em.merge(accountTypeIdOld);
            }
            if (accountTypeIdNew != null && !accountTypeIdNew.equals(accountTypeIdOld)) {
                accountTypeIdNew.getDemandCollection().add(demand);
                accountTypeIdNew = em.merge(accountTypeIdNew);
            }
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getDemandCollection().remove(demand);
                userIdOld = em.merge(userIdOld);
            }
            if (userIdNew != null && !userIdNew.equals(userIdOld)) {
                userIdNew.getDemandCollection().add(demand);
                userIdNew = em.merge(userIdNew);
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
                Integer id = demand.getId();
                if (findDemand(id) == null) {
                    throw new NonexistentEntityException("The demand with id " + id + " no longer exists.");
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
            Demand demand;
            try {
                demand = em.getReference(Demand.class, id);
                demand.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The demand with id " + id + " no longer exists.", enfe);
            }
            AccountType accountTypeId = demand.getAccountTypeId();
            if (accountTypeId != null) {
                accountTypeId.getDemandCollection().remove(demand);
                accountTypeId = em.merge(accountTypeId);
            }
            User userId = demand.getUserId();
            if (userId != null) {
                userId.getDemandCollection().remove(demand);
                userId = em.merge(userId);
            }
            em.remove(demand);
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

    public List<Demand> findDemandEntities() {
        return findDemandEntities(true, -1, -1);
    }

    public List<Demand> findDemandEntities(int maxResults, int firstResult) {
        return findDemandEntities(false, maxResults, firstResult);
    }

    private List<Demand> findDemandEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Demand.class));
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

    public Demand findDemand(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Demand.class, id);
        } finally {
            em.close();
        }
    }

    public int getDemandCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Demand> rt = cq.from(Demand.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
