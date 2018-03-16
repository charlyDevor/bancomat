/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.arc.controllers;

import ch.arc.controllers.exceptions.IllegalOrphanException;
import ch.arc.controllers.exceptions.NonexistentEntityException;
import ch.arc.controllers.exceptions.RollbackFailureException;
import ch.arc.entities.Groups;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ch.arc.entities.UserGroup;
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
public class GroupsJpaController implements Serializable {

    public GroupsJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Groups groups) throws RollbackFailureException, Exception {
        if (groups.getUserGroupCollection() == null) {
            groups.setUserGroupCollection(new ArrayList<UserGroup>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<UserGroup> attachedUserGroupCollection = new ArrayList<UserGroup>();
            for (UserGroup userGroupCollectionUserGroupToAttach : groups.getUserGroupCollection()) {
                userGroupCollectionUserGroupToAttach = em.getReference(userGroupCollectionUserGroupToAttach.getClass(), userGroupCollectionUserGroupToAttach.getId());
                attachedUserGroupCollection.add(userGroupCollectionUserGroupToAttach);
            }
            groups.setUserGroupCollection(attachedUserGroupCollection);
            em.persist(groups);
            for (UserGroup userGroupCollectionUserGroup : groups.getUserGroupCollection()) {
                Groups oldGroupIdOfUserGroupCollectionUserGroup = userGroupCollectionUserGroup.getGroupId();
                userGroupCollectionUserGroup.setGroupId(groups);
                userGroupCollectionUserGroup = em.merge(userGroupCollectionUserGroup);
                if (oldGroupIdOfUserGroupCollectionUserGroup != null) {
                    oldGroupIdOfUserGroupCollectionUserGroup.getUserGroupCollection().remove(userGroupCollectionUserGroup);
                    oldGroupIdOfUserGroupCollectionUserGroup = em.merge(oldGroupIdOfUserGroupCollectionUserGroup);
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

    public void edit(Groups groups) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Groups persistentGroups = em.find(Groups.class, groups.getId());
            Collection<UserGroup> userGroupCollectionOld = persistentGroups.getUserGroupCollection();
            Collection<UserGroup> userGroupCollectionNew = groups.getUserGroupCollection();
            List<String> illegalOrphanMessages = null;
            for (UserGroup userGroupCollectionOldUserGroup : userGroupCollectionOld) {
                if (!userGroupCollectionNew.contains(userGroupCollectionOldUserGroup)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain UserGroup " + userGroupCollectionOldUserGroup + " since its groupId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<UserGroup> attachedUserGroupCollectionNew = new ArrayList<UserGroup>();
            for (UserGroup userGroupCollectionNewUserGroupToAttach : userGroupCollectionNew) {
                userGroupCollectionNewUserGroupToAttach = em.getReference(userGroupCollectionNewUserGroupToAttach.getClass(), userGroupCollectionNewUserGroupToAttach.getId());
                attachedUserGroupCollectionNew.add(userGroupCollectionNewUserGroupToAttach);
            }
            userGroupCollectionNew = attachedUserGroupCollectionNew;
            groups.setUserGroupCollection(userGroupCollectionNew);
            groups = em.merge(groups);
            for (UserGroup userGroupCollectionNewUserGroup : userGroupCollectionNew) {
                if (!userGroupCollectionOld.contains(userGroupCollectionNewUserGroup)) {
                    Groups oldGroupIdOfUserGroupCollectionNewUserGroup = userGroupCollectionNewUserGroup.getGroupId();
                    userGroupCollectionNewUserGroup.setGroupId(groups);
                    userGroupCollectionNewUserGroup = em.merge(userGroupCollectionNewUserGroup);
                    if (oldGroupIdOfUserGroupCollectionNewUserGroup != null && !oldGroupIdOfUserGroupCollectionNewUserGroup.equals(groups)) {
                        oldGroupIdOfUserGroupCollectionNewUserGroup.getUserGroupCollection().remove(userGroupCollectionNewUserGroup);
                        oldGroupIdOfUserGroupCollectionNewUserGroup = em.merge(oldGroupIdOfUserGroupCollectionNewUserGroup);
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
                Integer id = groups.getId();
                if (findGroups(id) == null) {
                    throw new NonexistentEntityException("The groups with id " + id + " no longer exists.");
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
            Groups groups;
            try {
                groups = em.getReference(Groups.class, id);
                groups.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The groups with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<UserGroup> userGroupCollectionOrphanCheck = groups.getUserGroupCollection();
            for (UserGroup userGroupCollectionOrphanCheckUserGroup : userGroupCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Groups (" + groups + ") cannot be destroyed since the UserGroup " + userGroupCollectionOrphanCheckUserGroup + " in its userGroupCollection field has a non-nullable groupId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(groups);
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

    public List<Groups> findGroupsEntities() {
        return findGroupsEntities(true, -1, -1);
    }

    public List<Groups> findGroupsEntities(int maxResults, int firstResult) {
        return findGroupsEntities(false, maxResults, firstResult);
    }

    private List<Groups> findGroupsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Groups.class));
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

    public Groups findGroups(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Groups.class, id);
        } finally {
            em.close();
        }
    }

    public int getGroupsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Groups> rt = cq.from(Groups.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
