package com.alhakim.ecommerce.common;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.ArrayList;
import java.util.List;

public class PageUtil {
    public static List<Order> parseSortOrderRequest(String[] sort) {
        List<Order> orders = new ArrayList<>();
        if (sort[0].contains(",")) {
            for (String s: sort) {
                String[] split = s.split(",");
                String field = split[0];
                String direction = (split.length > 1) ? split[1] : "asc";
                orders.add(new Sort.Order(getSortDirection(direction), field));
            }
        } else {
            String field = sort[0];
            String direction = (sort.length > 1) ? sort[1] : "asc";
            orders.add(new Sort.Order(getSortDirection(direction), field));
        }

        return orders;
    }

    private static Sort.Direction getSortDirection(String direction) {
        if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }
}
