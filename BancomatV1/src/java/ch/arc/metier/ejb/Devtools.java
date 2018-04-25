/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.arc.metier.ejb;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Ombiche
 */
@Entity
@Table(name = "devtools")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Devtools.findAll", query = "SELECT d FROM Devtools d")
    , @NamedQuery(name = "Devtools.findById", query = "SELECT d FROM Devtools d WHERE d.id = :id")
    , @NamedQuery(name = "Devtools.findByType", query = "SELECT d FROM Devtools d WHERE d.type = :type")
    , @NamedQuery(name = "Devtools.findByRank", query = "SELECT d FROM Devtools d WHERE d.rank = :rank")
    , @NamedQuery(name = "Devtools.findByIduser", query = "SELECT d FROM Devtools d WHERE d.iduser = :iduser")
    , @NamedQuery(name = "Devtools.findByCreatedAt", query = "SELECT d FROM Devtools d WHERE d.createdAt = :createdAt")
    , @NamedQuery(name = "Devtools.findByUpdatedAt", query = "SELECT d FROM Devtools d WHERE d.updatedAt = :updatedAt")
    , @NamedQuery(name = "Devtools.findByUrl", query = "SELECT d FROM Devtools d WHERE d.url = :url")
    , @NamedQuery(name = "Devtools.findByImagePath", query = "SELECT d FROM Devtools d WHERE d.imagePath = :imagePath")})
public class Devtools implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 9)
    @Column(name = "type")
    private String type;
    @Column(name = "rank")
    private Integer rank;
    @Column(name = "iduser")
    private Integer iduser;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Lob
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    @Size(max = 255)
    @Column(name = "url")
    private String url;
    @Size(max = 255)
    @Column(name = "image_path")
    private String imagePath;

    public Devtools() {
    }

    public Devtools(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getIduser() {
        return iduser;
    }

    public void setIduser(Integer iduser) {
        this.iduser = iduser;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
        if (!(object instanceof Devtools)) {
            return false;
        }
        Devtools other = (Devtools) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.arc.metier.ejb.Devtools[ id=" + id + " ]";
    }
    
}
