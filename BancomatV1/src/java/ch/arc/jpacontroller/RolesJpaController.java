/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.arc.jpacontroller;

import ch.arc.jpacontroller.exceptions.IllegalOrphanException;
import ch.arc.jpacontroller.exceptions.NonexistentEntityException;
import ch.arc.jpacontroller.exceptions.RollbackFailureException;
import ch.arc.metier.ejb.Roles;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ch.arc.metier.ejb.UserRole;
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
public class RolesJpaController implements Serializable {

    public RolesJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Roles roles) throws RollbackFailureException, Exception {
        if (roles.getUserRoleCollection() == null) {
            roles.setUserRoleCollection(new ArrayList<UserRole>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<UserRole> attachedUserRoleCollection = new ArrayList<UserRole>();
            for (UserRole userRoleCollectionUserRoleToAttach : roles.getUserRoleCollection()) {
                userRoleCollectionUserRoleToAttach = em.getReference(userRoleCollectionUserRoleToAttach.getClass(), userRoleCollectionUserRoleToAttach.getId());
                attachedUserRoleCollection.add(userRoleCollectionUserRoleToAttach);
            }
            roles.setUserRoleCollection(attachedUserRoleCollection);
            em.persist(roles);
            for (UserRole userRoleCollectionUserRole : roles.getUserRoleCollection()) {
                Roles oldRoleIdOfUserRoleCollectionUserRole = userRoleCollectionUserRole.getRoleId();
                userRoleCollectionUserRole.setRoleId(roles);
                userRoleCollectionUserRole = em.merge(userRoleCollectionUserRole);
                if (oldRoleIdOfUserRoleCollectionUserRole != null) {
                    oldRoleIdOfUserRoleCollectionUserRole.getUserRoleCollection().remove(userRoleCollectionUserRole);
                    oldRoleIdOfUserRoleCollectionUserRole = em.merge(oldRoleIdOfUserRoleCollectionUserRole);
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

    public void edit(Roles roles) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Roles persistentRoles = em.find(Roles.class, roles.getId());
            Collection<UserRole> userRoleCollectionOld = persistentRoles.getUserRoleCollection();
            Collection<UserRole> userRoleCollectionNew = roles.getUserRoleCollection();
            List<String> illegalOrphanMessages = null;
            for (UserRole userRoleCollectionOldUserRole : userRoleCollectionOld) {
                if (!userRoleCollectionNew.contains(userRoleCollectionOldUserRole)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain UserRole " + userRoleCollectionOldUserRole + " since its roleId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<UserRole> attachedUserRoleCollectionNew = new ArrayList<UserRole>();
            for (UserRole userRoleCollectionNewUserRoleToAttach : userRoleCollectionNew) {
                userRoleCollectionNewUserRoleToAttach = em.getReference(userRoleCollectionNewUserRoleToAttach.getClass(), userRoleCollectionNewUserRoleToAttach.getId());
                attachedUserRoleCollectionNew.add(userRoleCollectionNewUserRoleToAttach);
            }
            userRoleCollectionNew = attachedUserRoleCollectionNew;
            roles.setUserRoleCollection(userRoleCollectionNew);
            roles = em.merge(roles);
            for (UserRole userRoleCollectionNewUserRole : userRoleCollectionNew) {
                if (!userRoleCollectionOld.contains(userRoleCollectionNewUserRole)) {
                    Roles oldRoleIdOfUserRoleCollectionNewUserRole = userRoleCollectionNewUserRole.getRoleId();
                    userRoleCollectionNewUserRole.setRoleId(roles);
                    userRoleCollectionNewUserRole = em.merge(userRoleCollectionNewUserRole);
                    if (oldRoleIdOfUserRoleCollectionNewUserRole != null && !oldRoleIdOfUserRoleCollectionNewUserRole.equals(roles)) {
                        oldRoleIdOfUserRoleCollectionNewUserRole.getUserRoleCollection().remove(userRoleCollectionNewUserRole);
                        oldRoleIdOfUserRoleCollectionNewUserRole = em.merge(oldRoleIdOfUserRoleCollectionNewUserRole);
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
                Integer id = roles.getId();
                if (findRoles(id) == null) {
                    throw new NonexistentEntityException("The roles with id " + id + " no longer exists.");
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
            Roles roles;
            try {
                roles = em.getReference(Roles.class, id);
                roles.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The roles with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<UserRole> userRoleCollectionOrphanCheck = roles.getUserRoleCollection();
            for (UserRole userRoleCollectionOrphanCheckUserRole : userRoleCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Roles (" + roles + ") cannot be destroyed since the UserRole " + userRoleCollectionOrphanCheckUserRole + " in its userRoleCollection field has a non-nullable roleId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(roles);
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

    public List<Roles> findRolesEntities() {
        return findRolesEntities(true, -1, -1);
    }

    public List<Roles> findRolesEntities(int maxResults, int firstResult) {
        return findRolesEntities(false, maxResults, firstResult);
    }

    private List<Roles> findRolesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Roles.class));
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

    public Roles findRoles(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Roles.class, id);
        } finally {
            em.close();
        }
    }

    public int getRolesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Roles> rt = cq.from(Roles.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
