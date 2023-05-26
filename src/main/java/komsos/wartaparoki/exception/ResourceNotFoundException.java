package komsos.wartaparoki.exception;

public class ResourceNotFoundException extends RuntimeException {

    private String customMessage;
    
    public ResourceNotFoundException(String arg0, String customMessage) {
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
