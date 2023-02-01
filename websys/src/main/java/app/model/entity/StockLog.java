package app.model.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "wfs_stock_log")
public class StockLog {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "movement_id")
    private Movement movement;
    
    @ManyToOne
    @JoinColumn(name = "sku_id")
    private SKU sku;

    private Integer currentQtd;

    private Integer newQtd;

    private String descr;

    private Date date;

    private String nf;





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

    public String getNF() {
        return this.nf;
    }
    public void setNF(String nf) {
        this.nf = nf;
    }

    public SKU getSKU() {
        return this.sku;
    }
    public void setSKU(SKU sku) {
        this.sku = sku;
    }

    public Integer getCurrentQtd() {
        return this.currentQtd;
    }
    public void setCurrentQtd(Integer currentQtd) {
        this.currentQtd = currentQtd;
    }

    public Integer getNewQtd() {
        return this.newQtd;
    }
    public void setNewQtd(Integer newQtd) {
        this.newQtd = newQtd;
    }

    public String getDescr() {
        return this.descr;
    }
    
    public void setDescr(String descr) {
        this.descr = descr;
    }

    public Date getDate() {
        return this.date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public Movement getMovement() {
        return this.movement;
    }
    public void setMovement(Movement movement) {
        this.movement = movement;
    }

}