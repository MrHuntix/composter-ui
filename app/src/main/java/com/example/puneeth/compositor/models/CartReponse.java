package com.example.puneeth.compositor.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartReponse {
    private String itemName;
    private String weight;
    private String cost;
}
