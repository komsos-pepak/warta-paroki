package komsos.wartaparoki.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import komsos.wartaparoki.helper.CustomPage;

@Component
@RequiredArgsConstructor
public class Utils {
    
    private final ModelMapper modelMapper;

    private DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
    private DecimalFormatSymbols formatRp = new DecimalFormatSymbols();

    public <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream().map(element -> modelMapper.map(element, targetClass)).collect(Collectors.toList());
    }

    /**
     * Maps the Page {@code entities} of <code>T</code> type which have to be mapped as input to {@code dtoClass} Page
     * of mapped object with <code>D</code> type.
     *
     * @param <D> - type of objects in result page
     * @param <T> - type of entity in <code>entityPage</code>
     * @param entities - page of entities that needs to be mapped
     * @param dtoClass - class of result page element
     * @return page - mapped page with objects of type <code>D</code>.
     * @NB <code>dtoClass</code> must has NoArgsConstructor!
     */

    public <D, T> CustomPage<D> mapEntityPageIntoDtoPage(Page<T> entities, Class<D> dtoClass) {
        Page<D> customPage = entities.map(objectEntity -> modelMapper.map(objectEntity, dtoClass));
        return new CustomPage<D>(customPage);

        // return entities.map(objectEntity -> modelMapper.map(objectEntity, dtoClass));
    }

    public static List<?> convertObjectToList(Object obj) {
        List<?> list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[])obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>)obj);
        }
        return list;
    }

    public String formatIdrCur(BigDecimal currency) {
        formatRp.setCurrencySymbol("Rp. ");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');
        kursIndonesia.setDecimalFormatSymbols(formatRp);
        String result = kursIndonesia.format(currency);
        return result;
    }

    public String localDateToString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateStr = date.format(formatter);
        return dateStr;
    }

    public String localDateTimeToString(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String dateStr = date.format(formatter);
        return dateStr;
    }

    public Integer getUmumFromFullFormat(String umur) {
        String[] splited = umur.split("\\s+");
        Integer umurTahun = Integer.valueOf(splited[0]);
        return umurTahun;
    }

    public String encodeStringToBase64(String string){
        String originalInput = string;
        String encodedString = Base64.getUrlEncoder().encodeToString(originalInput.getBytes());
        return encodedString;
    }
    
    public String decodeBase64ToString(String string){
        byte[] decodedBytes = Base64.getUrlDecoder().decode(string);
        String decodedUrl = new String(decodedBytes);
        return decodedUrl;
    }

    public String getCurrentDateTime() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        return timeStamp;
    }

    

}
