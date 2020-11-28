package com.example.puneeth.compositor.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewOffersResponse {
    private long offerId;
    private String itemName;
    private String itemWeight;
    private long itemCost;
    private String buyerName;
    private String buyerContact;
    private String weight;
    private String offerCost;
}
