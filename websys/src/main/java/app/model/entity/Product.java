package app.model.entity;

import app.model.repository.ProductCategory;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.EnumType;

@Entity(name = "wfs_product")
public class Product {


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Column(unique=true)
	private String externalId;
	
	@OneToMany(targetEntity=MovementItem.class, mappedBy="product", fetch=FetchType.LAZY, orphanRemoval=true)
    private Set<MovementItem> movementItems;

	@Enumerated(EnumType.STRING)
    @Column(length=10)
    private UnidadeComercial uCom;
	
	@Enumerated(EnumType.STRING)
    @Column(length=10, nullable=true)
    private UnidadeComercial uTrib;
	
	@ManyToOne
    @JoinColumn(name="product_category_id", nullable=false)
	private ProductCategory productCategory;
	



	private String name;
	private String ncm;
	private String cest;
	private String ean;
	private String eanTrib;

	private String grupoTrib;

	@Column(nullable=false)
	private Boolean trackStock;

	@Column(precision=10, scale=2)
	private Double price;
	
	@Column(precision=10, scale=2)
	private Double cost;
	
	@Column(precision=10, scale=2)
	private Double priceUnTrib;





	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public Double getPrice() {
		return this.price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}

	public String getGrupoTrib() {
		return this.grupoTrib;
	}
	public void setGrupoTrib(String grupoTrib) {
		this.grupoTrib = grupoTrib;
	}

	public Double getPriceUnTrib() {
		return this.priceUnTrib;
	}
	public void setPriceUnTrib(Double priceUnTrib) {
		this.priceUnTrib = priceUnTrib;
	}

	public String getEAN() {
		return this.ean;
	}
	public void setEAN(String ean) {
		this.ean = ean;
	}

	public String getEANTrib() {
		return this.eanTrib;
	}
	public void setEANTrib(String eanTrib) {
		this.eanTrib = eanTrib;
	}

	public String getNCM() {
		return this.ncm;
	}
	public void setNCM(String ncm) {
		this.ncm = ncm;
	}


	public String getCEST() {
		return this.cest;
	}
	public void setCEST(String cest) {
		this.cest = cest;
	}


	public UnidadeComercial getUCom() {
		return this.uCom;
	}
	public void setUCom(UnidadeComercial uCom) {
		this.uCom = uCom;
	}

	public UnidadeComercial getUTrib() {
		return this.uTrib;
	}
	public void setUTrib(UnidadeComercial uTrib) {
		this.uTrib = uTrib;
	}

	public Double getCost() {
		return this.cost;
	}
	public void setCost(Double cost) {
		this.cost = cost;
	}

	public Boolean getTrackStock() {
		return this.trackStock;
	}
	public void setTrackStock(Boolean trackStock) {
		this.trackStock = trackStock;
	}

	public Set<MovementItem> getMovementItems() {
		return this.movementItems;
	}
	public void setMovementItems(Set<MovementItem> movementItems) {
		this.movementItems = movementItems;
	}


    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory (ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

}