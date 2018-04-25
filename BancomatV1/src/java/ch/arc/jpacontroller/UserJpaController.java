/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.arc.jpacontroller;

import ch.arc.jpacontroller.exceptions.IllegalOrphanException;
import ch.arc.jpacontroller.exceptions.NonexistentEntityException;
import ch.arc.jpacontroller.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ch.arc.metier.ejb.Demand;
import java.util.ArrayList;
import java.util.Collection;
import ch.arc.metier.ejb.UserRole;
import ch.arc.metier.ejb.UserGroup;
import ch.arc.metier.ejb.Account;
import ch.arc.metier.ejb.User;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Ombiche
 */
public class UserJpaController implements Serializable {

    public UserJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(User user) throws RollbackFailureException, Exception {
        if (user.getDemandCollection() == null) {
            user.setDemandCollection(new ArrayList<Demand>());
        }
        if (user.getUserRoleCollection() == null) {
            user.setUserRoleCollection(new ArrayList<UserRole>());
        }
        if (user.getUserGroupCollection() == null) {
            user.setUserGroupCollection(new ArrayList<UserGroup>());
        }
        if (user.getAccountCollection() == null) {
            user.setAccountCollection(new ArrayList<Account>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Demand> attachedDemandCollection = new ArrayList<Demand>();
            for (Demand demandCollectionDemandToAttach : user.getDemandCollection()) {
                demandCollectionDemandToAttach = em.getReference(demandCollectionDemandToAttach.getClass(), demandCollectionDemandToAttach.getId());
                attachedDemandCollection.add(demandCollectionDemandToAttach);
            }
            user.setDemandCollection(attachedDemandCollection);
            Collection<UserRole> attachedUserRoleCollection = new ArrayList<UserRole>();
            for (UserRole userRoleCollectionUserRoleToAttach : user.getUserRoleCollection()) {
                userRoleCollectionUserRoleToAttach = em.getReference(userRoleCollectionUserRoleToAttach.getClass(), userRoleCollectionUserRoleToAttach.getId());
                attachedUserRoleCollection.add(userRoleCollectionUserRoleToAttach);
            }
            user.setUserRoleCollection(attachedUserRoleCollection);
            Collection<UserGroup> attachedUserGroupCollection = new ArrayList<UserGroup>();
            for (UserGroup userGroupCollectionUserGroupToAttach : user.getUserGroupCollection()) {
                userGroupCollectionUserGroupToAttach = em.getReference(userGroupCollectionUserGroupToAttach.getClass(), userGroupCollectionUserGroupToAttach.getId());
                attachedUserGroupCollection.add(userGroupCollectionUserGroupToAttach);
            }
            user.setUserGroupCollection(attachedUserGroupCollection);
            Collection<Account> attachedAccountCollection = new ArrayList<Account>();
            for (Account accountCollectionAccountToAttach : user.getAccountCollection()) {
                accountCollectionAccountToAttach = em.getReference(accountCollectionAccountToAttach.getClass(), accountCollectionAccountToAttach.getId());
                attachedAccountCollection.add(accountCollectionAccountToAttach);
            }
            user.setAccountCollection(attachedAccountCollection);
            em.persist(user);
            for (Demand demandCollectionDemand : user.getDemandCollection()) {
                User oldUserIdOfDemandCollectionDemand = demandCollectionDemand.getUserId();
                demandCollectionDemand.setUserId(user);
                demandCollectionDemand = em.merge(demandCollectionDemand);
                if (oldUserIdOfDemandCollectionDemand != null) {
                    oldUserIdOfDemandCollectionDemand.getDemandCollection().remove(demandCollectionDemand);
                    oldUserIdOfDemandCollectionDemand = em.merge(oldUserIdOfDemandCollectionDemand);
                }
            }
            for (UserRole userRoleCollectionUserRole : user.getUserRoleCollection()) {
                User oldUserIdOfUserRoleCollectionUserRole = userRoleCollectionUserRole.getUserId();
                userRoleCollectionUserRole.setUserId(user);
                userRoleCollectionUserRole = em.merge(userRoleCollectionUserRole);
                if (oldUserIdOfUserRoleCollectionUserRole != null) {
                    oldUserIdOfUserRoleCollectionUserRole.getUserRoleCollection().remove(userRoleCollectionUserRole);
                    oldUserIdOfUserRoleCollectionUserRole = em.merge(oldUserIdOfUserRoleCollectionUserRole);
                }
            }
            for (UserGroup userGroupCollectionUserGroup : user.getUserGroupCollection()) {
                User oldUserIdOfUserGroupCollectionUserGroup = userGroupCollectionUserGroup.getUserId();
                userGroupCollectionUserGroup.setUserId(user);
                userGroupCollectionUserGroup = em.merge(userGroupCollectionUserGroup);
                if (oldUserIdOfUserGroupCollectionUserGroup != null) {
                    oldUserIdOfUserGroupCollectionUserGroup.getUserGroupCollection().remove(userGroupCollectionUserGroup);
                    oldUserIdOfUserGroupCollectionUserGroup = em.merge(oldUserIdOfUserGroupCollectionUserGroup);
                }
            }
            for (Account accountCollectionAccount : user.getAccountCollection()) {
                User oldUserIdOfAccountCollectionAccount = accountCollectionAccount.getUserId();
                accountCollectionAccount.setUserId(user);
                accountCollectionAccount = em.merge(accountCollectionAccount);
                if (oldUserIdOfAccountCollectionAccount != null) {
                    oldUserIdOfAccountCollectionAccount.getAccountCollection().remove(accountCollectionAccount);
                    oldUserIdOfAccountCollectionAccount = em.merge(oldUserIdOfAccountCollectionAccount);
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

    public void edit(User user) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            User persistentUser = em.find(User.class, user.getId());
            Collection<Demand> demandCollectionOld = persistentUser.getDemandCollection();
            Collection<Demand> demandCollectionNew = user.getDemandCollection();
            Collection<UserRole> userRoleCollectionOld = persistentUser.getUserRoleCollection();
            Collection<UserRole> userRoleCollectionNew = user.getUserRoleCollection();
            Collection<UserGroup> userGroupCollectionOld = persistentUser.getUserGroupCollection();
            Collection<UserGroup> userGroupCollectionNew = user.getUserGroupCollection();
            Collection<Account> accountCollectionOld = persistentUser.getAccountCollection();
            Collection<Account> accountCollectionNew = user.getAccountCollection();
            List<String> illegalOrphanMessages = null;
            for (Demand demandCollectionOldDemand : demandCollectionOld) {
                if (!demandCollectionNew.contains(demandCollectionOldDemand)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Demand " + demandCollectionOldDemand + " since its userId field is not nullable.");
                }
            }
            for (UserRole userRoleCollectionOldUserRole : userRoleCollectionOld) {
                if (!userRoleCollectionNew.contains(userRoleCollectionOldUserRole)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain UserRole " + userRoleCollectionOldUserRole + " since its userId field is not nullable.");
                }
            }
            for (UserGroup userGroupCollectionOldUserGroup : userGroupCollectionOld) {
                if (!userGroupCollectionNew.contains(userGroupCollectionOldUserGroup)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain UserGroup " + userGroupCollectionOldUserGroup + " since its userId field is not nullable.");
                }
            }
            for (Account accountCollectionOldAccount : accountCollectionOld) {
                if (!accountCollectionNew.contains(accountCollectionOldAccount)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Account " + accountCollectionOldAccount + " since its userId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Demand> attachedDemandCollectionNew = new ArrayList<Demand>();
            for (Demand demandCollectionNewDemandToAttach : demandCollectionNew) {
                demandCollectionNewDemandToAttach = em.getReference(demandCollectionNewDemandToAttach.getClass(), demandCollectionNewDemandToAttach.getId());
                attachedDemandCollectionNew.add(demandCollectionNewDemandToAttach);
            }
            demandCollectionNew = attachedDemandCollectionNew;
            user.setDemandCollection(demandCollectionNew);
            Collection<UserRole> attachedUserRoleCollectionNew = new ArrayList<UserRole>();
            for (UserRole userRoleCollectionNewUserRoleToAttach : userRoleCollectionNew) {
                userRoleCollectionNewUserRoleToAttach = em.getReference(userRoleCollectionNewUserRoleToAttach.getClass(), userRoleCollectionNewUserRoleToAttach.getId());
                attachedUserRoleCollectionNew.add(userRoleCollectionNewUserRoleToAttach);
            }
            userRoleCollectionNew = attachedUserRoleCollectionNew;
            user.setUserRoleCollection(userRoleCollectionNew);
            Collection<UserGroup> attachedUserGroupCollectionNew = new ArrayList<UserGroup>();
            for (UserGroup userGroupCollectionNewUserGroupToAttach : userGroupCollectionNew) {
                userGroupCollectionNewUserGroupToAttach = em.getReference(userGroupCollectionNewUserGroupToAttach.getClass(), userGroupCollectionNewUserGroupToAttach.getId());
                attachedUserGroupCollectionNew.add(userGroupCollectionNewUserGroupToAttach);
            }
            userGroupCollectionNew = attachedUserGroupCollectionNew;
            user.setUserGroupCollection(userGroupCollectionNew);
            Collection<Account> attachedAccountCollectionNew = new ArrayList<Account>();
            for (Account accountCollectionNewAccountToAttach : accountCollectionNew) {
                accountCollectionNewAccountToAttach = em.getReference(accountCollectionNewAccountToAttach.getClass(), accountCollectionNewAccountToAttach.getId());
                attachedAccountCollectionNew.add(accountCollectionNewAccountToAttach);
            }
            accountCollectionNew = attachedAccountCollectionNew;
            user.setAccountCollection(accountCollectionNew);
            user = em.merge(user);
            for (Demand demandCollectionNewDemand : demandCollectionNew) {
                if (!demandCollectionOld.contains(demandCollectionNewDemand)) {
                    User oldUserIdOfDemandCollectionNewDemand = demandCollectionNewDemand.getUserId();
                    demandCollectionNewDemand.setUserId(user);
                    demandCollectionNewDemand = em.merge(demandCollectionNewDemand);
                    if (oldUserIdOfDemandCollectionNewDemand != null && !oldUserIdOfDemandCollectionNewDemand.equals(user)) {
                        oldUserIdOfDemandCollectionNewDemand.getDemandCollection().remove(demandCollectionNewDemand);
                        oldUserIdOfDemandCollectionNewDemand = em.merge(oldUserIdOfDemandCollectionNewDemand);
                    }
                }
            }
            for (UserRole userRoleCollectionNewUserRole : userRoleCollectionNew) {
                if (!userRoleCollectionOld.contains(userRoleCollectionNewUserRole)) {
                    User oldUserIdOfUserRoleCollectionNewUserRole = userRoleCollectionNewUserRole.getUserId();
                    userRoleCollectionNewUserRole.setUserId(user);
                    userRoleCollectionNewUserRole = em.merge(userRoleCollectionNewUserRole);
                    if (oldUserIdOfUserRoleCollectionNewUserRole != null && !oldUserIdOfUserRoleCollectionNewUserRole.equals(user)) {
                        oldUserIdOfUserRoleCollectionNewUserRole.getUserRoleCollection().remove(userRoleCollectionNewUserRole);
                        oldUserIdOfUserRoleCollectionNewUserRole = em.merge(oldUserIdOfUserRoleCollectionNewUserRole);
                    }
                }
            }
            for (UserGroup userGroupCollectionNewUserGroup : userGroupCollectionNew) {
                if (!userGroupCollectionOld.contains(userGroupCollectionNewUserGroup)) {
                    User oldUserIdOfUserGroupCollectionNewUserGroup = userGroupCollectionNewUserGroup.getUserId();
                    userGroupCollectionNewUserGroup.setUserId(user);
                    userGroupCollectionNewUserGroup = em.merge(userGroupCollectionNewUserGroup);
                    if (oldUserIdOfUserGroupCollectionNewUserGroup != null && !oldUserIdOfUserGroupCollectionNewUserGroup.equals(user)) {
                        oldUserIdOfUserGroupCollectionNewUserGroup.getUserGroupCollection().remove(userGroupCollectionNewUserGroup);
                        oldUserIdOfUserGroupCollectionNewUserGroup = em.merge(oldUserIdOfUserGroupCollectionNewUserGroup);
                    }
                }
            }
            for (Account accountCollectionNewAccount : accountCollectionNew) {
                if (!accountCollectionOld.contains(accountCollectionNewAccount)) {
                    User oldUserIdOfAccountCollectionNewAccount = accountCollectionNewAccount.getUserId();
                    accountCollectionNewAccount.setUserId(user);
                    accountCollectionNewAccount = em.merge(accountCollectionNewAccount);
                    if (oldUserIdOfAccountCollectionNewAccount != null && !oldUserIdOfAccountCollectionNewAccount.equals(user)) {
                        oldUserIdOfAccountCollectionNewAccount.getAccountCollection().remove(accountCollectionNewAccount);
                        oldUserIdOfAccountCollectionNewAccount = em.merge(oldUserIdOfAccountCollectionNewAccount);
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
                Integer id = user.getId();
                if (findUser(id) == null) {
                    throw new NonexistentEntityException("The user with id " + id + " no longer exists.");
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
            User user;
            try {
                user = em.getReference(User.class, id);
                user.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The user with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Demand> demandCollectionOrphanCheck = user.getDemandCollection();
            for (Demand demandCollectionOrphanCheckDemand : demandCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the Demand " + demandCollectionOrphanCheckDemand + " in its demandCollection field has a non-nullable userId field.");
            }
            Collection<UserRole> userRoleCollectionOrphanCheck = user.getUserRoleCollection();
            for (UserRole userRoleCollectionOrphanCheckUserRole : userRoleCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the UserRole " + userRoleCollectionOrphanCheckUserRole + " in its userRoleCollection field has a non-nullable userId field.");
            }
            Collection<UserGroup> userGroupCollectionOrphanCheck = user.getUserGroupCollection();
            for (UserGroup userGroupCollectionOrphanCheckUserGroup : userGroupCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the UserGroup " + userGroupCollectionOrphanCheckUserGroup + " in its userGroupCollection field has a non-nullable userId field.");
            }
            Collection<Account> accountCollectionOrphanCheck = user.getAccountCollection();
            for (Account accountCollectionOrphanCheckAccount : accountCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the Account " + accountCollectionOrphanCheckAccount + " in its accountCollection field has a non-nullable userId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(user);
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

    public List<User> findUserEntities() {
        return findUserEntities(true, -1, -1);
    }

    public List<User> findUserEntities(int maxResults, int firstResult) {
        return findUserEntities(false, maxResults, firstResult);
    }

    private List<User> findUserEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(User.class));
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

    public User findUser(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    public int getUserCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<User> rt = cq.from(User.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
