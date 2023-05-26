package komsos.wartaparoki.exception;

public class CustomUnauthorizedException extends RuntimeException{
    private String customMessage;
    
    public CustomUnauthorizedException(String arg0, String customMessage) {
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
