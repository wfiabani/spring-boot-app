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

@Entity(name = "wfs_fixed_account")
@NamedQuery(name = "FixedAccount.findByScope", query = "SELECT account FROM wfs_fixed_account account WHERE date_trunc('month', enabled_date) <= to_date(?1, 'MM/YYYY') AND (date_trunc('month', disabled_date) >= to_date(?1, 'MM/YYYY') OR disabled_date IS NULL)")
public class FixedAccount {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date enabledDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable=true)
    private Date disabledDate;

    @Enumerated(EnumType.STRING)
    @Column(length=10, nullable=true)
    private AccountType type;

    @Column(precision=10, scale=2)
    private Double value;
    

    private String name;
    private String externalId;
    private Integer expirationDay;


    private Boolean repeatJAN;
    private Boolean repeatFEV;
    private Boolean repeatMAR;
    private Boolean repeatAPR;
    private Boolean repeatMAY;
    private Boolean repeatJUN;
    private Boolean repeatJUL;
    private Boolean repeatAGO;
    private Boolean repeatSET;
    private Boolean repeatOCT;
    private Boolean repeatNOV;
    private Boolean repeatDEC;



	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
    }
    
    public Integer getExpirationDay() {
        return this.expirationDay;
    }
    public void setExpirationDay(Integer expirationDay) {
        this.expirationDay = expirationDay;
    }

    public Date getEnabledDate() {
        return this.enabledDate;
    }
    public void setEnabledDate(Date enabledDate) {
        this.enabledDate = enabledDate;
    }

    public Date getDisabledDate() {
        return this.disabledDate;
    }
    public void setDisabledDate(Date disabledDate) {
        this.disabledDate = disabledDate;
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

    public Boolean getRepeatJAN() {
        return this.repeatJAN;
    }
    public void setRepeatJAN(Boolean repeatJAN) {
        this.repeatJAN = repeatJAN;
    }

    public Boolean getRepeatFEV() {
        return this.repeatFEV;
    }
    public void setRepeatFEV(Boolean repeatFEV) {
        this.repeatFEV = repeatFEV;
    }

    public Boolean getRepeatMAR() {
        return this.repeatMAR;
    }
    public void setRepeatMAR(Boolean repeatMAR) {
        this.repeatMAR = repeatMAR;
    }

    public Boolean getRepeatAPR() {
        return this.repeatAPR;
    }
    public void setRepeatAPR(Boolean repeatAPR) {
        this.repeatAPR = repeatAPR;
    }

    public Boolean getRepeatMAY() {
        return this.repeatMAY;
    }
    public void setRepeatMAY(Boolean repeatMAY) {
        this.repeatMAY = repeatMAY;
    }

    public Boolean getRepeatJUN() {
        return this.repeatJUN;
    }
    public void setRepeatJUN(Boolean repeatJUN) {
        this.repeatJUN = repeatJUN;
    }

    public Boolean getRepeatJUL() {
        return this.repeatJUL;
    }
    public void setRepeatJUL(Boolean repeatJUL) {
        this.repeatJUL = repeatJUL;
    }

    public Boolean getRepeatAGO() {
        return this.repeatAGO;
    }
    public void setRepeatAGO(Boolean repeatAGO) {
        this.repeatAGO = repeatAGO;
    }

    public Boolean getRepeatSET() {
        return this.repeatSET;
    }
    public void setRepeatSET(Boolean repeatSET) {
        this.repeatSET = repeatSET;
    }

    public Boolean getRepeatOCT() {
        return this.repeatOCT;
    }
    public void setRepeatOCT(Boolean repeatOCT) {
        this.repeatOCT = repeatOCT;
    }

    public Boolean getRepeatNOV() {
        return this.repeatNOV;
    }
    public void setRepeatNOV(Boolean repeatNOV) {
        this.repeatNOV = repeatNOV;
    }

    public Boolean getRepeatDEC() {
        return this.repeatDEC;
    }
    public void setRepeatDEC(Boolean repeatDEC) {
        this.repeatDEC = repeatDEC;
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