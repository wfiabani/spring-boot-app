package app.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "wfs_movement_item")
public class MovementItem {


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	

	@ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    
    @ManyToOne
    @JoinColumn(name = "sku_id")
    private SKU sku;
    
    
    @ManyToOne
    @JoinColumn(name = "movement_id")
    private Movement movement;

    private Integer qtd;


    @Column(precision=10, scale=2)
    private Double unitPrice;
    
    @Column(precision=10, scale=2)
    private Double subTotal;




	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
    }

    public Double getUnitPrice() {
        return this.unitPrice;
    }
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getSubTotal() {
        return this.subTotal;
    }
    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
    }

    public int getQtd() {
        return this.qtd;
    }
    public void setQtd(int qtd) {
        this.qtd = qtd;
    }


    public Product getProduct() {
        return this.product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }

    public SKU getSKU() {
        return this.sku;
    }
    public void setSKU(SKU sku) {
        this.sku = sku;
    }

    public Movement getMovement() {
        return this.movement;
    }
    public void setMovement(Movement movement) {
        this.movement = movement;
    }
}