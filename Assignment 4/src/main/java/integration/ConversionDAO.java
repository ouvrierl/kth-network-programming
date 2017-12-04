package integration;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import model.Conversion;
import model.ConversionIDs;

@TransactionAttribute(TransactionAttributeType.MANDATORY)
@Stateless
public class ConversionDAO {

    @PersistenceContext(unitName = "conversionPU")
    private EntityManager em;

    public void storeConversion(Conversion conversion) {
        em.persist(conversion);
    }

    public Conversion findConversion(String deviseFrom, String deviseTo) {
        Conversion conversion = em.find(Conversion.class, new ConversionIDs(deviseFrom, deviseTo));
        return conversion;
    }

}
