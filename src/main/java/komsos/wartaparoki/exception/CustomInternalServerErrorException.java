package komsos.wartaparoki.exception;

public class CustomInternalServerErrorException extends RuntimeException{
    private String customMessage;

    public CustomInternalServerErrorException(String arg0, String customMessage) {
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
