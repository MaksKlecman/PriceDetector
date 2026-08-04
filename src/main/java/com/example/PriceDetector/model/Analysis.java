package com.example.PriceDetector.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "analysis")

public class Analysis {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

   // @NotBlank(message = "can not be empty brand")
  //  @NonNull
    private String itemBrand;
   // @NotBlank(message = "can not be empty name")
  //  @NonNull
    private String itemName;

    @Enumerated(EnumType.STRING)
    private Category category;

    //@NotBlank(message = "please write down condition of your item for better verdict")
   // @NonNull
    private String condition;

    //@NotBlank(message = "please write down description of your item for better verdict")
    //@NonNull
    private String description;

   // @Min(value = 0, message = "price of item must be grater than 0")
    //@Max(value = 99999, message = "price of item must be lower than 99999")
    private BigDecimal sellerPrice;


    private BigDecimal estimatedNewPrice;


    private BigDecimal estimatedResalePrice;

    @Enumerated(EnumType.STRING)
    private Verdict verdict;

    private BigDecimal suggestedPrice;

    @Column(columnDefinition = "TEXT")
    private String aiRawResponse;
















}
