package ch.arc.managedbeanscontroller;

import ch.arc.metier.ejb.User;
import ch.arc.managedbeanscontroller.util.JsfUtil;
import ch.arc.managedbeanscontroller.util.PaginationHelper;
import ch.arc.ejbsessionfacade.UserFacade;
import ch.arc.metier.ejb.Groups;
import ch.arc.metier.ejb.UserGroup;
import ch.arc.auth.utils.AuthentificationUtils;
import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.primefaces.context.RequestContext;

@ManagedBean(name = "userController")
@SessionScoped
public class UserController implements Serializable {

    private User current;
    private DataModel items = null;
    public static final int USER_GROUP = 2;
    private static Logger log = Logger.getLogger(UserController.class.getName());
    @EJB
    private ch.arc.ejbsessionfacade.UserFacade ejbFacade;
    @ManagedProperty(value = "#{userGroupController}")
    private UserGroupController userGroupController; //ATTENTION ATTENTION Très important pour éviter de perdre 10 ans. la variable doit avoir le même nom que la propriété oufffffff
    private PaginationHelper pagination;
    private int selectedItemIndex;

    //Inputs
    private String username;
    private String password;

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public UserGroupController getUserGroupController() {
        return userGroupController;
    }

    public void setUserGroupController(UserGroupController ugc) {
        this.userGroupController = ugc;
    }

    public UserController() {
    }

    public String name() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Integer userId = (Integer) session.getAttribute("userId");

        User user = ejbFacade.find(userId);
        return user.getFirstName() + " " + user.getLastName();
    }

    public Integer loggedUserId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            return userId;
        }
        return null;
    }

    public String login() {
        //Integer userId = ejbFacade.login(current.getUsername(), current.getPassword());
        //FacesContext facesContext = FacesContext.getCurrentInstance();
        //HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        /*if (userId != null) {
            //Set userId to the session
            session.setAttribute("userId", userId);
            return "secureUser/Account_list.xhtml?faces-redirect=true";
        }*/
        //RequestContext.getCurrentInstance().update("growl");
        //facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ooops", "Nom d'utilisateur ou mot de passe invalide"));

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        System.out.println("Request: "+request);
        try {
            System.out.println("User: "+username+" Pass: "+password);
            request.login(username, password);
        } catch (ServletException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login failed!", null));
            return "/Opening.xhtml";
        }
        Principal principal = request.getUserPrincipal();
        current = ejbFacade.findByUsername(principal.getName());
        log.info("Utilisateur connecté: " + principal.getName());
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        sessionMap.put("User", current);
        if (request.isUserInRole("User")) {
            return "/secureUser/Account_list.xhtml?faces-redirect=true";
        } else {
            return "";
        }
    }

    public List<User> all() {
        return ejbFacade.findAll();
    }
    
    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try {
            this.current = null;
            request.logout();
            // clear the session
            ((HttpSession) context.getExternalContext().getSession(false)).invalidate();
        } catch (ServletException ex) {
            ex.getMessage();
        }
        return "/Opening?faces-redirect=true";
    }

    public User getSelected() {
        if (current == null) {
            current = new User();
            selectedItemIndex = -1;
        }
        return current;
    }

    private UserFacade getFacade() {
        return ejbFacade;
    }

    public void validatePassword(ComponentSystemEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();
        UIComponent components = event.getComponent();

        // get password
        UIInput uiInputPassword = (UIInput) components.findComponent("password");
        String password = uiInputPassword.getLocalValue() == null ? "" : uiInputPassword.getLocalValue().toString();
        String passwordId = uiInputPassword.getClientId();

        // get confirm password
        UIInput uiInputConfirmPassword = (UIInput) components.findComponent("password_2");
        String confirmPassword = uiInputConfirmPassword.getLocalValue() == null ? "" : uiInputConfirmPassword.getLocalValue().toString();

        // Let required="true" do its job.
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            return;
        }
        if (!password.equals(confirmPassword)) {
            FacesMessage msg = new FacesMessage("Les mots de passe ne matchent pas");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fc.addMessage(passwordId, msg);
            fc.renderResponse();
        }
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
        current = (User) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new User();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.setPassword(AuthentificationUtils.encodeSHA256(current.getPassword()));
            getFacade().create(current);
            System.out.println(current.getPassword());
            //Create user groupe. 3 for customer
            UserGroup ug = new UserGroup();
            ug.setGroupId(new Groups(USER_GROUP));
            ug.setUserId(current);
            userGroupController.create(ug);
            //Set user Id in session
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            session.setAttribute("userId", current.getId());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UserCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (User) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UserUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (User) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "";
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UserDeleted"));
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

    public User getUser(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = User.class)
    public static class UserControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserController controller = (UserController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "userController");
            return controller.getUser(getKey(value));
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
            if (object instanceof User) {
                User o = (User) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + User.class.getName());
            }
        }

    }

}
