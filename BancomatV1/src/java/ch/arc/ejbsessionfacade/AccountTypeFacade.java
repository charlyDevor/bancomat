/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.arc.ejbsessionfacade;

import ch.arc.metier.ejb.AccountType;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Ombiche
 */
@Stateless
public class AccountTypeFacade extends AbstractFacade<AccountType> {

    @PersistenceContext(unitName = "BancomatV1PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AccountTypeFacade() {
        super(AccountType.class);
    }
    
}
