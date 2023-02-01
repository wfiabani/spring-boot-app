package app.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "wfs_payment")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private Integer fixedAccountId;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    private String scope;

    @Column(precision=10, scale=2)
    private Double value;
    

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
    }

    public Integer getFixedAcccountId() {
        return this.fixedAccountId;
    }
    public void setFixedAcccountId(Integer fixedAccountId) {
        this.fixedAccountId = fixedAccountId;
    }

    public Date getPaymentDate() {
        return this.paymentDate;
    }
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }


    public Date getCreationDate() {
        return this.creationDate;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getScope() {
        return this.scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }

    public Double getValue() {
        return this.value;
    }
    public void setValue(Double value) {
        this.value = value;
    }

}