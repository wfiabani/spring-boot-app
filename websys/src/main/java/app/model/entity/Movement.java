package app.model.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity(name = "wfs_movement")
public class Movement {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @OneToMany(targetEntity=MovementItem.class, mappedBy="movement", fetch=FetchType.LAZY, orphanRemoval=true)
    private Set<MovementItem> movementItems;
  
    @OneToMany(targetEntity=ServiceItem.class, mappedBy="service", fetch=FetchType.LAZY, orphanRemoval=true)
    private Set<ServiceItem> serviceItems;

    private String name;
    private String externalId;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Enumerated(EnumType.STRING)
    @Column(length=10)
    private MovementType type;

    @Column(precision=10, scale=2)
    private Double freight;
    
    @Column(precision=10, scale=2)
    private Double otherCosts;

    @Column(nullable=false)
    private Boolean orcamento;
    
    //se sim, considera o frete no total
    @Column(nullable=true)
    private Boolean enableCosts;

    @ManyToOne
    @JoinColumn(name="cadastro_id", nullable=false)
    private Cadastro cadastro;
    
    @Column(precision=10, scale=2, nullable=true)
    private Double total;


    //@Column(length=10485760)
    private String nfeXML;

    private String nfeChave;
    private Boolean nfeCancelada;
    private String obs;

    
    private Boolean enableEdit;
    

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
    }

    public String getNfeXML() {
        return this.nfeXML;
    }
    public void setNfeXML(String nfeXML) {
        this.nfeXML = nfeXML;
    }

    public String getObs() {
        return this.obs;
    }
    public void setObs(String obs) {
        this.obs = obs;
    }

    public Boolean getNfeCancelada() {
        return this.nfeCancelada;
    }
    public void setNfeCancelada(Boolean nfeCancelada) {
        this.nfeCancelada = nfeCancelada;
    }

    public String getNfeChave() {
        return this.nfeChave;
    }
    public void setNfeChave(String nfeChave) {
        this.nfeChave = nfeChave;
    }

    public Cadastro getCadastro() {
        return this.cadastro;
    }
    public void setCadastro(Cadastro cadastro) {
        this.cadastro = cadastro;
    }

    public Boolean getOrcamento() {
        return this.orcamento;
    }
    public void setOrcamento(Boolean orcamento) {
        this.orcamento = orcamento;
    }

    public Double getFreight() {
        return this.freight;
    }
    public void setFreight(Double freight) {
        this.freight = freight;
    }

    public Double getOtherCosts() {
        return this.otherCosts;
    }
    public void setOtherCosts(Double otherCosts) {
        this.otherCosts = otherCosts;
    }


    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }


    public MovementType getType() {
        return this.type;
    }
    public void setType(MovementType type) {
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

    public Boolean getEnableCosts() {
        return this.enableCosts;
    }
    public void setEnableCosts(Boolean enableCosts) {
        this.enableCosts = enableCosts;
    }

    public Double getTotal() {
        return this.total;
    }
    public void setTotal(Double total) {
        this.total = total;
    }




	public Set<MovementItem> getMovementItems() {
		return this.movementItems;
	}
	public void setMovementItems(Set<MovementItem> movementItems) {
		this.movementItems = movementItems;
	}


    public Set<ServiceItem> getServiceItems() {
        return this.serviceItems;
    }
    public void setServiceItems(Set<ServiceItem> serviceItems) {
        this.serviceItems = serviceItems;
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