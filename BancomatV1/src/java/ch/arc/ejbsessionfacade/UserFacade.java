package ch.arc.ejbsessionfacade;

import ch.arc.metier.ejb.User;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author Ombiche
 */
@Stateless
public class UserFacade extends AbstractFacade<User> {

    @PersistenceContext(unitName = "BancomatV1PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UserFacade() {
        super(User.class);
    }

    public Integer login(String username, String password) {
        try {
            User user = em.createNamedQuery("User.control", User.class).setParameter("username", username)
                    .setParameter("password", password).getSingleResult();
            if (user != null) {
                return user.getId();
            }
        } catch (Exception ex) {
            ex.getMessage();
        }
        return null;
    }

    public User findUserById(Integer id) {
        TypedQuery<User> query = em.createNamedQuery("User.findById", User.class).setParameter("id", id);
        User user = null;
        try {
            user = query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
        return user;
    }
    
     public User findByUsername(String username) {
        Query query = (Query) em.createQuery("SELECT * FROM USER u INNER JOIN V_USER_ROLE v ON u.username = v.username WHERE u.username =:username")
                .setParameter("username", username);
        User user = null;
        try {
            user = (User) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
        return user;
    }
}
