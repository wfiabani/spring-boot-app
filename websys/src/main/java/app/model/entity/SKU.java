package app.model.entity;


import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity(name = "wfs_product_sku")
@NamedQuery(name = "SKU.xespa", query = "SELECT sku FROM wfs_product_sku sku WHERE (estoque <= estoqueMin)")
public class SKU {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;


	@ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;


    @OneToMany(targetEntity=MovementItem.class, mappedBy="sku", fetch=FetchType.LAZY, orphanRemoval=true)
    private Set<MovementItem> movementItems;


    private String name;

    @Column(unique=true)
    private String externalId;
    
	private Integer estoque;
	private Integer estoqueMin;
    

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
    }


    public Product getProduct() {
        return this.product;
    }
    public void setProduct(Product product) {
        this.product = product;
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

    public Integer getEstoque() {
        return this.estoque;
    }
    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Integer getEstoqueMin() {
        return this.estoqueMin;
    }
    public void setEstoqueMin(Integer estoqueMin) {
        this.estoqueMin = estoqueMin;
    }



	public Set<MovementItem> getMovementItems() {
		return this.movementItems;
	}
	public void setMovementItems(Set<MovementItem> movementItems) {
		this.movementItems = movementItems;
	}

}