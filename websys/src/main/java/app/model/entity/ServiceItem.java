package app.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "wfs_movement_service_item")
public class ServiceItem {


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private String name;

	@ManyToOne
    @JoinColumn(name = "service_id")
    private Servico service;
    
    @ManyToOne
    @JoinColumn(name = "movement_id")
    private Movement movement;

    @Column(precision=10, scale=2)
    private Double subTotal;




	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }


    public Servico getServico() {
        return this.service;
    }
    public void setService(Servico service) {
        this.service = service;
    }

    public Movement getMovement() {
        return this.movement;
    }
    public void setMovement(Movement movement) {
        this.movement = movement;
    }

    public Double getSubTotal() {
        return this.subTotal;
    }
    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
    }
}