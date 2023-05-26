package komsos.wartaparoki.exception;

public class DuplicateResourceException extends RuntimeException{

    private String customMessage;

    public DuplicateResourceException(String arg0, String customMessage) {
        super(arg0);
        this.customMessage = customMessage;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    
}
