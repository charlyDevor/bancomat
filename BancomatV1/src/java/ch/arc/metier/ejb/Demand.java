package ch.arc.metier.ejb;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Ombiche
 */
@Entity
@Table(name = "demand")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Demand.findAll", query = "SELECT d FROM Demand d")
    , @NamedQuery(name = "Demand.findByUserId", query = "SELECT d FROM Demand d WHERE d.userId.id = :userId and d.accepter =:accept")
    , @NamedQuery(name = "Demand.findById", query = "SELECT d FROM Demand d WHERE d.id = :id")
    , @NamedQuery(name = "Demand.findByAccepter", query = "SELECT d FROM Demand d WHERE d.accepter = :accepter")})
public class Demand implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "accepter")
    private int accepter;
    @JoinColumn(name = "account_type_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private AccountType accountTypeId;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private User userId;

    public Demand() {
    }

    public Demand(Integer id) {
        this.id = id;
    }

    public Demand(Integer id, int accepter) {
        this.id = id;
        this.accepter = accepter;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getAccepter() {
        return accepter;
    }

    public void setAccepter(int accepter) {
        this.accepter = accepter;
    }

    public AccountType getAccountTypeId() {
        return accountTypeId;
    }

    public String getAccountTypeName() {
        return accountTypeId.getName();
    }
    
    public void setAccountTypeId(AccountType accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }
    
    public void setUserId_(Integer userId){
        this.userId.setId(userId);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Demand)) {
            return false;
        }
        Demand other = (Demand) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.arc.metier.ejb.Demand[ id=" + id + " ]";
    }
    
}
