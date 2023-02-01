package app.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "wfs_account")
@NamedQuery(name = "Account.findByScope", query = "SELECT account FROM wfs_account account WHERE date_trunc('month', expiration_date) = to_date(?1, 'MM/YYYY')")
public class Account {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Enumerated(EnumType.STRING)
    @Column(length=10, nullable=true)
    private AccountType type;

    @Column(precision=10, scale=2)
    private Double value;
    
    @Column(nullable=false)
    private Boolean paid;

    @Column(nullable=false)
    private Boolean orcamento;

    
    @Column(nullable=false)
    private Integer movementId;
    

    private String name;
    private String externalId;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
    }

    public Boolean getPaid() {
        return this.paid;
    }
    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return this.value;
    }
    public void setValue(Double value) {
        this.value = value;
    }

    public AccountType getType() {
        return this.type;
    }
    public void setType(AccountType type) {
        this.type = type;
    }

    
    public String getExternalId() {
        return this.externalId;
    }
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Boolean getOrcamento() {
        return this.orcamento;
    }
    public void setOrcamento(Boolean orcamento) {
        this.orcamento = orcamento;
    }

    public Integer getMovementId() {
        return this.movementId;
    }
    public void setMovementId(Integer movementId) {
        this.movementId = movementId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return this.lastModifiedDate;
    }
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getExpirationDate() {
        return this.expirationDate;
    }
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }


    @PrePersist
    protected void onCreate() {
        this.creationDate = new Date();
        this.lastModifiedDate = new Date();
    }

    
    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedDate = new Date();
    }
    

}