package komsos.wartaparoki.helper;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class CustomPage<T> {
    List<T> content;
    CustomPageable pageable;

    public CustomPage(Page<T> page) {
        this.content = page.getContent();
        this.pageable = new CustomPageable(page.getPageable().getPageNumber(),
                page.getPageable().getPageSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Data
    class CustomPageable {
        int pageNumber;
        int pageSize;
        int totalPages;
        long totalElements;

        public CustomPageable(int pageNumber, int pageSize, long totalElements, int totalPages) {

            this.pageNumber = pageNumber+1;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
    }
}
