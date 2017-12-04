package view;

import common.Constants;
import controller.ConversionFacade;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

@Named(value = "ConversionManager")
@SessionScoped
public class ConversionManager implements Serializable {

    @EJB
    ConversionFacade conversionFacade;
    private String deviseFrom;
    private String deviseTo;
    private String amount;
    private String result;

    @Inject
    private Conversation conversation;

    private void startConversation() {
        if (this.conversation.isTransient()) {
            this.conversation.begin();
        }
    }

    private void stopConversation() {
        if (!this.conversation.isTransient()) {
            this.conversation.end();
        }
    }

    public String getDeviseFrom() {
        return this.deviseFrom;
    }

    public void setDeviseFrom(String deviseFrom) {
        this.deviseFrom = deviseFrom;
    }

    public String getDeviseTo() {
        return this.deviseTo;
    }

    public void setDeviseTo(String deviseTo) {
        this.deviseTo = deviseTo;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void convert() {
        try {
            this.startConversation();
            Double amountDouble = Double.parseDouble(this.amount);
            double rate = this.conversionFacade.getConversionRate(this.deviseFrom, this.deviseTo);
            this.result = Double.toString(amountDouble * rate);
            this.amount = "";
        } catch (NumberFormatException e) {
            FacesContext.getCurrentInstance().addMessage("form:amount", new FacesMessage(Constants.ERROR_DOUBLE, Constants.ERROR_DOUBLE));
            this.amount = "";
            this.result = "";
        } finally {
            this.stopConversation();
        }
    }

    @PostConstruct
    public void init() {
        this.conversionFacade.createConversion(Constants.SEK, Constants.SEK, 1);
        this.conversionFacade.createConversion(Constants.EUR, Constants.EUR, 1);
        this.conversionFacade.createConversion(Constants.USD, Constants.USD, 1);
        this.conversionFacade.createConversion(Constants.GBP, Constants.GBP, 1);
        this.conversionFacade.createConversion(Constants.SEK, Constants.EUR, 0.1007);
        this.conversionFacade.createConversion(Constants.EUR, Constants.SEK, 9.93073);
        this.conversionFacade.createConversion(Constants.SEK, Constants.USD, 0.11979);
        this.conversionFacade.createConversion(Constants.USD, Constants.SEK, 8.34768);
        this.conversionFacade.createConversion(Constants.SEK, Constants.GBP, 0.0889);
        this.conversionFacade.createConversion(Constants.GBP, Constants.SEK, 11.2482);
        this.conversionFacade.createConversion(Constants.EUR, Constants.GBP, 0.88287);
        this.conversionFacade.createConversion(Constants.GBP, Constants.EUR, 1.13267);
        this.conversionFacade.createConversion(Constants.EUR, Constants.USD, 1.18964);
        this.conversionFacade.createConversion(Constants.USD, Constants.EUR, 0.84059);
        this.conversionFacade.createConversion(Constants.GBP, Constants.USD, 1.34747);
        this.conversionFacade.createConversion(Constants.USD, Constants.GBP, 0.74213);
    }

}
