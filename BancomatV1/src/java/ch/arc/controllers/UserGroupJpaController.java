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
import ch.arc.entities.Groups;
import ch.arc.entities.User;
import ch.arc.entities.UserGroup;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Ombiche
 */
public class UserGroupJpaController implements Serializable {

    public UserGroupJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(UserGroup userGroup) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Groups groupId = userGroup.getGroupId();
            if (groupId != null) {
                groupId = em.getReference(groupId.getClass(), groupId.getId());
                userGroup.setGroupId(groupId);
            }
            User userId = userGroup.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                userGroup.setUserId(userId);
            }
            em.persist(userGroup);
            if (groupId != null) {
                groupId.getUserGroupCollection().add(userGroup);
                groupId = em.merge(groupId);
            }
            if (userId != null) {
                userId.getUserGroupCollection().add(userGroup);
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

    public void edit(UserGroup userGroup) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            UserGroup persistentUserGroup = em.find(UserGroup.class, userGroup.getId());
            Groups groupIdOld = persistentUserGroup.getGroupId();
            Groups groupIdNew = userGroup.getGroupId();
            User userIdOld = persistentUserGroup.getUserId();
            User userIdNew = userGroup.getUserId();
            if (groupIdNew != null) {
                groupIdNew = em.getReference(groupIdNew.getClass(), groupIdNew.getId());
                userGroup.setGroupId(groupIdNew);
            }
            if (userIdNew != null) {
                userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
                userGroup.setUserId(userIdNew);
            }
            userGroup = em.merge(userGroup);
            if (groupIdOld != null && !groupIdOld.equals(groupIdNew)) {
                groupIdOld.getUserGroupCollection().remove(userGroup);
                groupIdOld = em.merge(groupIdOld);
            }
            if (groupIdNew != null && !groupIdNew.equals(groupIdOld)) {
                groupIdNew.getUserGroupCollection().add(userGroup);
                groupIdNew = em.merge(groupIdNew);
            }
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getUserGroupCollection().remove(userGroup);
                userIdOld = em.merge(userIdOld);
            }
            if (userIdNew != null && !userIdNew.equals(userIdOld)) {
                userIdNew.getUserGroupCollection().add(userGroup);
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
                Integer id = userGroup.getId();
                if (findUserGroup(id) == null) {
                    throw new NonexistentEntityException("The userGroup with id " + id + " no longer exists.");
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
            UserGroup userGroup;
            try {
                userGroup = em.getReference(UserGroup.class, id);
                userGroup.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The userGroup with id " + id + " no longer exists.", enfe);
            }
            Groups groupId = userGroup.getGroupId();
            if (groupId != null) {
                groupId.getUserGroupCollection().remove(userGroup);
                groupId = em.merge(groupId);
            }
            User userId = userGroup.getUserId();
            if (userId != null) {
                userId.getUserGroupCollection().remove(userGroup);
                userId = em.merge(userId);
            }
            em.remove(userGroup);
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

    public List<UserGroup> findUserGroupEntities() {
        return findUserGroupEntities(true, -1, -1);
    }

    public List<UserGroup> findUserGroupEntities(int maxResults, int firstResult) {
        return findUserGroupEntities(false, maxResults, firstResult);
    }

    private List<UserGroup> findUserGroupEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(UserGroup.class));
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

    public UserGroup findUserGroup(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(UserGroup.class, id);
        } finally {
            em.close();
        }
    }

    public int getUserGroupCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<UserGroup> rt = cq.from(UserGroup.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
