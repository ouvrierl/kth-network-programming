package model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ConversionIDs {

    @Column
    private String deviseFrom;
    @Column
    private String deviseTo;

    public ConversionIDs() {
    }

    public ConversionIDs(String deviseFrom, String deviseTo) {
        this.deviseFrom = deviseFrom;
        this.deviseTo = deviseTo;
    }

}
