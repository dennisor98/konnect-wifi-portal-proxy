package net.sasakonnect.wifi_portal;

import org.springframework.data.domain.Page;
import java.util.HashMap;
import java.util.Map;

public abstract class BasePagingUtility<T> {

    /**
     * Generates pagination metadata from a given Page object.
     * 
     * @param page The Page object containing entity data
     * @return A Map with pagination details
     */
    protected Map<String, Object> getPaginationInfo(Page<T> page) {
        Map<String, Object> paginationInfo = new HashMap<>();
        paginationInfo.put("currentPage", page.getNumber());
        paginationInfo.put("totalItems", page.getTotalElements());
        paginationInfo.put("totalPages", page.getTotalPages());
        paginationInfo.put("pageSize", page.getSize());
        paginationInfo.put("isFirst", page.isFirst());
        paginationInfo.put("isLast", page.isLast());
        paginationInfo.put("hasNextPage", page.hasNext());
        paginationInfo.put("hasPreviousPage", page.hasPrevious());
        paginationInfo.put("nextPage", page.hasNext() ? page.getNumber() + 1 : null);
        paginationInfo.put("previousPage", page.hasPrevious() ? page.getNumber() - 1 : null);
        return paginationInfo;
    }
}
