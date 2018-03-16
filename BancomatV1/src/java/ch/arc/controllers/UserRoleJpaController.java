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
import ch.arc.entities.User;
import ch.arc.entities.Roles;
import ch.arc.entities.UserRole;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Ombiche
 */
public class UserRoleJpaController implements Serializable {

    public UserRoleJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(UserRole userRole) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            User userId = userRole.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                userRole.setUserId(userId);
            }
            Roles roleId = userRole.getRoleId();
            if (roleId != null) {
                roleId = em.getReference(roleId.getClass(), roleId.getId());
                userRole.setRoleId(roleId);
            }
            em.persist(userRole);
            if (userId != null) {
                userId.getUserRoleCollection().add(userRole);
                userId = em.merge(userId);
            }
            if (roleId != null) {
                roleId.getUserRoleCollection().add(userRole);
                roleId = em.merge(roleId);
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

    public void edit(UserRole userRole) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            UserRole persistentUserRole = em.find(UserRole.class, userRole.getId());
            User userIdOld = persistentUserRole.getUserId();
            User userIdNew = userRole.getUserId();
            Roles roleIdOld = persistentUserRole.getRoleId();
            Roles roleIdNew = userRole.getRoleId();
            if (userIdNew != null) {
                userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
                userRole.setUserId(userIdNew);
            }
            if (roleIdNew != null) {
                roleIdNew = em.getReference(roleIdNew.getClass(), roleIdNew.getId());
                userRole.setRoleId(roleIdNew);
            }
            userRole = em.merge(userRole);
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getUserRoleCollection().remove(userRole);
                userIdOld = em.merge(userIdOld);
            }
            if (userIdNew != null && !userIdNew.equals(userIdOld)) {
                userIdNew.getUserRoleCollection().add(userRole);
                userIdNew = em.merge(userIdNew);
            }
            if (roleIdOld != null && !roleIdOld.equals(roleIdNew)) {
                roleIdOld.getUserRoleCollection().remove(userRole);
                roleIdOld = em.merge(roleIdOld);
            }
            if (roleIdNew != null && !roleIdNew.equals(roleIdOld)) {
                roleIdNew.getUserRoleCollection().add(userRole);
                roleIdNew = em.merge(roleIdNew);
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
                Integer id = userRole.getId();
                if (findUserRole(id) == null) {
                    throw new NonexistentEntityException("The userRole with id " + id + " no longer exists.");
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
            UserRole userRole;
            try {
                userRole = em.getReference(UserRole.class, id);
                userRole.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The userRole with id " + id + " no longer exists.", enfe);
            }
            User userId = userRole.getUserId();
            if (userId != null) {
                userId.getUserRoleCollection().remove(userRole);
                userId = em.merge(userId);
            }
            Roles roleId = userRole.getRoleId();
            if (roleId != null) {
                roleId.getUserRoleCollection().remove(userRole);
                roleId = em.merge(roleId);
            }
            em.remove(userRole);
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

    public List<UserRole> findUserRoleEntities() {
        return findUserRoleEntities(true, -1, -1);
    }

    public List<UserRole> findUserRoleEntities(int maxResults, int firstResult) {
        return findUserRoleEntities(false, maxResults, firstResult);
    }

    private List<UserRole> findUserRoleEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(UserRole.class));
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

    public UserRole findUserRole(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(UserRole.class, id);
        } finally {
            em.close();
        }
    }

    public int getUserRoleCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<UserRole> rt = cq.from(UserRole.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
