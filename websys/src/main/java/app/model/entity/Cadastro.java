package app.model.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.EnumType;

@Entity(name = "wfs_cadastro")
public class Cadastro {


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @OneToMany(targetEntity=Movement.class, mappedBy="cadastro", fetch=FetchType.LAZY, orphanRemoval=true)
    private Set<Movement> movements;
    

	private String name;
	private String nameFant;
	private String ie;
	private String cnpj;
    private String cpf;
    private String email;
    private String telefone;
    
    @Column(nullable=true)
    private String cep;

    @Column(nullable=true)
    private String pais;

    @Column(nullable=true)
    private String cPais;

    private String lgr;
    private String num;
    private String bairro;
    private String codMun;
    
    private String municipio;

    @Column(nullable=true)
    private int indIEDest;
    
    @Enumerated(EnumType.STRING)
    @Column(length=10, nullable=true)
    private UF uf;


    
    


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

    public int getIndIEDest() {
        return this.indIEDest;
    }
    public void setIndIEDest(int indIEDest) {
        this.indIEDest = indIEDest;
    }
    
    public String getCNPJ() {
        return this.cnpj;
    }
    public void setCNPJ(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getCPais() {
        return this.cPais;
    }
    public void setCPais(String cPais) {
        this.cPais = cPais;
    }

    public String getPais() {
        return this.pais;
    }
    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCEP() {
        return this.cep;
    }
    public void setCEP(String cep) {
        this.cep = cep;
    }

    public String getCPF() {
        return this.cpf;
    }
    public void setCPF(String cpf) {
        this.cpf = cpf;
    }

    public String getNameFant() {
        return this.nameFant;
    }
    public void setNameFant(String nameFant) {
        this.nameFant = nameFant;
    }

    public String getIE() {
        return this.ie;
    }
    public void setIE(String ie) {
        this.ie = ie;
    }

    public String getLgr() {
        return this.lgr;
    }
    public void setLgr(String lgr) {
        this.lgr = lgr;
    }

    public String getNum() {
        return this.num;
    }
    public void setNum(String num) {
        this.num = num;
    }

    public String getBairro() {
        return this.bairro;
    }
    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCodMun() {
        return this.codMun;
    }
    public void setCodMun(String codMun) {
        this.codMun = codMun;
    }

    public String getMunicipio() {
        return this.municipio;
    }
    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public UF getUF() {
        return this.uf;
    }
    public void setUF(UF uf) {
        this.uf = uf;
    }


    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return this.telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }



    public Set<Movement> getMovements() {
        return this.movements;
    }

    public void setMovements(Set<Movement> movements) {
        this.movements = movements;
    }


}