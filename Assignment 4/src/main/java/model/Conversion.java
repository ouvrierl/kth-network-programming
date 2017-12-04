package model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class Conversion implements ConversionDTO, Serializable {

    @EmbeddedId
    private ConversionIDs devises;
    @Column
    private double rate;

    public Conversion() {
    }

    public Conversion(String deviseFrom, String deviseTo, double rate) {
        this.devises = new ConversionIDs(deviseFrom, deviseTo);
        this.rate = rate;
    }

    @Override
    public double getRate() {
        return this.rate;
    }

}
