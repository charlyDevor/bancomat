package ch.arc.managedbeanscontroller;

import ch.arc.metier.ejb.Demand;
import ch.arc.managedbeanscontroller.util.JsfUtil;
import ch.arc.managedbeanscontroller.util.PaginationHelper;
import ch.arc.ejbsessionfacade.DemandFacade;
import ch.arc.metier.ejb.Account;
import ch.arc.metier.ejb.AccountType;
import ch.arc.metier.ejb.User;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

@ManagedBean(name= "demandController")
@SessionScoped
public class DemandController implements Serializable {

    private Demand current;
    private DataModel items = null;
    @EJB
    private ch.arc.ejbsessionfacade.DemandFacade ejbFacade;
    @ManagedProperty(value="#{accountController}")
    private AccountController accountController; //ATTENTION le nom de la variable est le même que la valeur de la propriété

    public void setAccountController(AccountController accountController) {
        this.accountController = accountController;
    }

    public AccountController getAccountController() {
        return accountController;
    }
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public DemandController() {
    }

    public Demand getSelected() {
        if (current == null) {
            current = new Demand();
            selectedItemIndex = -1;
        }
        return current;
    }
    
    //All demands
    public List<Demand> all(){
        return ejbFacade.findAllWaitingDemands();
    }
    
    //Waiting demand
    public Integer findWaitingDemand() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        List<Demand> list = ejbFacade.findByUserId((Integer) session.getAttribute("userId"), 0);
        return list.size();
    }
    
    private DemandFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Demand) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View_demand";
    }

    public String prepareCreate() {
        current = new Demand();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            //Get user id
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            Integer userId = (Integer) session.getAttribute("userId");
            //Set user id
            current.setUserId(new User(userId));
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("DemandCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Demand) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit_demand";
    }
    
    public void acceptDemand(Integer id, Integer act, Integer userId){
        Integer result = ejbFacade.acceptDemand(id);
        if(result != null){
            //Create account for user 
            Account ac = new Account();
            ac.setAccountTypeId(new AccountType(act));
            ac.setUserId(new User(userId));
            ac.setBalance(0.0);
            //Génération simple du numéro de compte (peu faire mieux)
            Random ran = new Random();
            Integer accNum = id+act+userId+ran.nextInt(100);
            ac.setAccountNumber(accNum);
            accountController.create(ac);
        } 
    }

    /*public List<Demand> findByAccepter(){
        if (ejbFacade.findByAccepter()!= null){
            return "";
        }
        return "";
    }*/
    
    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("DemandUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Demand) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("DemandDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false, "");
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true, "");
    }

    public Demand getDemand(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Demand.class)
    public static class DemandControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DemandController controller = (DemandController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "demandController");
            return controller.getDemand(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Demand) {
                Demand o = (Demand) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Demand.class.getName());
            }
        }

    }
}
