package ch.arc.managedbeanscontroller;

import ch.arc.metier.ejb.Account;
import ch.arc.managedbeanscontroller.util.JsfUtil;
import ch.arc.managedbeanscontroller.util.PaginationHelper;
import ch.arc.ejbsessionfacade.AccountFacade;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

@ManagedBean(name = "accountController")
@SessionScoped
public class AccountController implements Serializable {

    private Account current;
    private DataModel items = null;
    @EJB
    private ch.arc.ejbsessionfacade.AccountFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private String idAccount;

    public void setIdAccount(String idAccount) {
        this.idAccount = idAccount;
    }

    public String getIdAccount() {
        return idAccount;
    }

    //Form data (dialog)
    private double amount;
    private Integer accountReceiver;
    private String description;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


    public void setAccountReceiver(Integer accountReceiver) {
        this.accountReceiver = accountReceiver;
    }

    public Integer getAccountReceiver() {
        return accountReceiver;
    }

    public AccountController() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double am) {
        amount = am;
    }

    public List<Account> findByUserId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        List<Account> list = ejbFacade.findByUserId((Integer) session.getAttribute("userId"));
        if (list != null) {
            return list;
        }
        return null;
    }

    //All accounts
    public List<Account> all(){
        return ejbFacade.findAll();
    }
    
    public Account getSelected() {
        if (current == null) {
            current = new Account();
            selectedItemIndex = -1;
        }
        return current;
    }

    private AccountFacade getFacade() {
        return ejbFacade;
    }
    
    //Deposite function
    public void deposit() {
        String accountNum = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("accountNum");
        ejbFacade.deposit(Integer.parseInt(accountNum), amount, "Dépôt d'argent", 1);
    }
    
    //Withdrawal function
    public void withdrawal() {
        String accountNum = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("accountNum");
        ejbFacade.withdrawal(Integer.parseInt(accountNum), amount, "Retrait d'agent", 2);
    }
    
    //Transaction
    public void sendAmount(){
        String accountNum = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("accountNum");
        if(description != null)
            ejbFacade.sendAmount(Integer.parseInt(accountNum), accountReceiver, amount, description, 3);
        else
            ejbFacade.sendAmount(Integer.parseInt(accountNum), accountReceiver, amount, "Transaction", 3);
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
        current = (Account) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View_account";
    }

    public String prepareCreate() {
        current = new Account();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create(Account acc) {
        try {
            getFacade().create(acc);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AccountCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Account) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AccountUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Account) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AccountDeleted"));
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

    public Account getAccount(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Account.class)
    public static class AccountControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AccountController controller = (AccountController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "accountController");
            return controller.getAccount(getKey(value));
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
            if (object instanceof Account) {
                Account o = (Account) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Account.class.getName());
            }
        }

    }

}
