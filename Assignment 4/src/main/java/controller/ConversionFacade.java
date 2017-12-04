package controller;

import integration.ConversionDAO;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import model.Conversion;

@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class ConversionFacade {

    @EJB
    ConversionDAO ConversionDB;

    public Conversion createConversion(String deviseFrom, String deviseTo, double rate) {
        Conversion newConversion = new Conversion(deviseFrom, deviseTo, rate);
        ConversionDB.storeConversion(newConversion);
        return newConversion;
    }

    public double getConversionRate(String deviseFrom, String deviseTo) {
        Conversion conversion = ConversionDB.findConversion(deviseFrom, deviseTo);
        return conversion.getRate();
    }

}
