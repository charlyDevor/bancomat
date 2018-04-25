package ch.arc.ejbsessionfacade;

import ch.arc.metier.ejb.Demand;
import java.util.List;
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
public class DemandFacade extends AbstractFacade<Demand> {

    @PersistenceContext(unitName = "BancomatV1PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DemandFacade() {
        super(Demand.class);
    }
    
    public Integer acceptDemand(Integer id){
        Query q = em.createQuery("update Demand set accepter = 1 where id = :id");
        return q.setParameter("id", id).executeUpdate();
    }
    
    //All demands
    public List<Demand> findAllWaitingDemands(){
        TypedQuery<Demand> query = em.createNamedQuery("Demand.findByAccepter", Demand.class).setParameter("accepter", 0);
        List<Demand> demands = null;
        try{
            if(query  != null) {
                demands = query.getResultList();
                return demands;
            }
        }catch(Exception ex){
            ex.getMessage();
        }
        return null;
    }
    
    public List<Demand> findByAccepter(){
        //Brico
        try{
            List<Demand> demands = em.createNamedQuery("Demand.findByAccepter", Demand.class).setParameter("accepter", 0).getResultList();
            if(demands  != null) {
                return demands;
            }
        }catch(Exception ex){
            ex.getMessage();
        }
        return null;
    }
    
    //Demand with accept = 0
    public List<Demand> findByUserId(Integer userId, int accept){
        try{
            List<Demand> demands = em.createNamedQuery("Demand.findByUserId", Demand.class)
                    .setParameter("userId", userId).setParameter("accept", accept).getResultList();
            if(demands  != null) {
                return demands;
            }
        }catch(Exception ex){
            ex.getMessage();
        }
        return null;
    }
}
