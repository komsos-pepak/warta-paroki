package komsos.wartaparoki.helper;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ResponseDto<T> {
    private Boolean status;
    private String message;
    private List<String> errorMessage = new ArrayList<>();
    private T payload;
}
