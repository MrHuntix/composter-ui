package com.example.puneeth.compositor;

import android.graphics.Bitmap;

class RowItem {
    private String compostId,compostName,sellerName,cost,weight,date,contact;
    private Bitmap image;

     Bitmap getImage(){
        return image;
    }

    void setImage(Bitmap image){
        this.image=image;
    }

    void setCompostId(String compostId) {
        this.compostId = compostId;
    }

    String getCompostId() {
        return compostId;
    }

    String getCompostName() {
        return compostName;
    }

    String getContact() {
        return contact;
    }

    public String getCost() {
        return cost;
    }

    String getDate() {
        return date;
    }

    String getSellerName() {
        return sellerName;
    }

    public String getWeight() {
        return weight;
    }

    void setCompostName(String compostName) {
        this.compostName = compostName;
    }

    void setContact(String contact) {
        this.contact = contact;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    void setDate(String date) {
        this.date = date;
    }

    void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

}
